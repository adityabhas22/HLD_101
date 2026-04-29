# Load Balancing and Consistent Hashing

Tue, 28 Apr 26

### Why Vertical Scaling Falls Short

- Good enough for limited, predictable scale
- Hardware has a ceiling — cores, RAM, storage all cap out
- Doesn’t handle user spikes well
- Solution: distribute data across multiple smaller systems (horizontal scaling)

### Load Balancing Architecture

- Load balancer sits in front of application servers; its IP registered in DNS
- All user requests route through the load balancer, which redirects to the appropriate server
- Single load balancer = single point of failure
  - Fix: deploy multiple load balancers
  - DNS holds a list of all load balancer IPs and returns the nearest one based on the user’s IP
- Load balancers must stay lightweight — no searching, no shared state, no inter-LB coordination
  - Coordinating shared data between LBs introduces locking (like DB transactions) and increases latency unacceptably
  - Each LB should independently compute which server to route to

### Naïve Hashing and Its Problem

- Simple approach: server = userID % N (where N = number of servers)
  - No extra data storage needed; all LBs produce identical routing decisions
  - Equal distribution of users across servers
- Critical flaw: adding or removing a server changes N, which changes the output for almost every user
  - Existing users get routed to wrong servers (their data isn’t there)
  - Requires full data redistribution and application downtime to fix
  - Downtime is a real cost — many services historically did this at midnight

### Consistent Hashing

- Core idea: hash both server IDs and user IDs into the same large fixed range (e.g. 0 to 2^80)
  - Servers are placed on a conceptual ring at their hash values
  - Each user is routed to the next server clockwise on the ring
- When a server is added or removed, only the users in the adjacent range are affected — not all users
- Problem with basic consistent hashing: unequal range sizes mean unequal load
  - One server may own a very large arc; another a tiny one
- Solution: virtual nodes (replicas)
  - Each physical server is hashed multiple times using different hash functions (or salted versions)
  - One server appears at multiple positions on the ring
  - More virtual nodes → better load distribution
  - Practical sweet spot: ~64 virtual nodes per server
    - Fewer → uneven distribution
    - More → diminishing returns, negligible practical improvement
- All LBs implement the same hash function and maintain the same sorted list of virtual node positions — no coordination needed

### Key Properties Achieved

- Adding a server: reduces load from all existing servers roughly equally (its virtual nodes are spread across the ring)
- Removing/failing a server: its load distributes across all remaining servers roughly equally
- No data movement required for unaffected users
- No shared mutable state between load balancers
- Some data migration on server add/remove is unavoidable — but it is minimised to only the adjacent ranges

### Open Questions / Next Steps

- How to handle server health checks so LBs don’t route to downed servers
  - Two mechanisms: LB polls servers (active), or servers send heartbeats to LB (passive)
  - If no heartbeat received within X time, LB marks server as down
- Data replication and durability during server transitions — to be addressed in future sessions
- Tradeoff between availability and consistency under failure — CAP-style decisions deferred to future classes

---

Chat with meeting transcript: https://notes.granola.ai/t/8efbebcc-9bea-4115-9997-7b7f5657e8b3-00demib2
