# Load Balancing and Consistent Hashing

This folder contains a small Java walkthrough of:

1. Basic hashing
2. Why naive modulo-based routing breaks when the server count changes
3. How consistent hashing solves that problem
4. Why virtual nodes help distribute traffic more evenly

## Files in this folder

- `HashingDemo.java`
  A tiny demo showing that hashing is deterministic and that collisions can happen.

- `User.java`
  A simple model used in the hashing examples.

- `UserRepository.java`
  A small in-memory repository for `User` objects.

- `Server.java`
  Represents a physical backend server with `host`, `port`, and `id`.

- `LoadBalancer.java`
  The main consistent-hashing implementation.
  It supports:
  - adding a server
  - removing a server
  - mapping a request key to the correct server
  - using virtual nodes for better load distribution

- `ConsistentHashingDemo.java`
  A runnable example that adds servers, routes keys, removes a server, and routes again.

- `notes.md`
  Class notes and design intuition behind the code in this folder.

## How the load balancer works

`LoadBalancer` uses a `TreeMap<Long, Server>` as a sorted hash ring.

- Each server is placed on the ring multiple times using virtual nodes.
- A virtual node is created by hashing a token like `serverId#replicaIndex`.
- Every virtual node points back to the same physical `Server`.

When a request comes in:

1. The request key is hashed.
2. The load balancer finds the first virtual node clockwise on the ring.
3. The server stored at that position handles the request.

If the key hash is larger than every position in the ring, the lookup wraps around to the first entry.

## Why virtual nodes are useful

Without virtual nodes, each physical server gets only one position on the ring.
That often creates uneven traffic distribution because some servers may own a much larger section of the ring than others.

With virtual nodes:

- the same server appears at multiple ring positions
- load is spread more evenly
- adding or removing one physical server affects smaller ranges across the ring instead of one large block

## What happens when a server is added

When `addServer(server)` is called:

1. The server gets `virtualNodesPerServer` virtual nodes.
2. Each virtual node is hashed and inserted into the ring.
3. The load balancer stores the inserted positions so they can be removed later.

Only the keys that now fall into the new server's virtual-node ranges move.
Most keys remain on the same server.

## What happens when a server is removed

When `removeServer(server)` is called:

1. The load balancer looks up the ring positions that belong to that server.
2. It removes those positions from the ring.
3. Requests that used to land on those virtual nodes now move to the next clockwise server.

Again, only part of the keyspace moves.

## How to run

Compile everything:

```bash
javac *.java
```

Run the basic hashing demo:

```bash
java HashingDemo
```

Run the consistent hashing demo:

```bash
java ConsistentHashingDemo
```

## Good starting point for reading

If you are new to this folder, read in this order:

1. `README.md`
2. `notes.md`
3. `Server.java`
4. `LoadBalancer.java`
5. `ConsistentHashingDemo.java`
