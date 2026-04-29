import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class LoadBalancer {
    // Ring: hash position -> server (one entry per virtual node).
    private final TreeMap<Long, Server> ring = new TreeMap<>();

    // For removals: exact ring positions that belong to a server.
    private final Map<Server, List<Long>> serverToVirtualNodeHashes = new HashMap<>();

    private final int virtualNodesPerServer;

    public LoadBalancer(int virtualNodesPerServer) {
        if (virtualNodesPerServer <= 0) {
            throw new IllegalArgumentException("virtualNodesPerServer must be > 0");
        }
        this.virtualNodesPerServer = virtualNodesPerServer;
    }

    public void addServer(Server server) {
        Objects.requireNonNull(server, "server");
        if (serverToVirtualNodeHashes.containsKey(server)) {
            // Idempotent: avoid duplicating virtual nodes for the same server.
            return;
        }

        List<Long> positions = new ArrayList<>(virtualNodesPerServer);
        for (int i = 0; i < virtualNodesPerServer; i++) {
            // Virtual node token must be deterministic so the ring is stable.
            long position = hash64(buildVirtualNodeKey(server, i));

            // (Extremely unlikely) resolve ring-position collisions deterministically.
            int attempt = 0;
            while (ring.containsKey(position)) {
                attempt++;
                position = hash64(buildVirtualNodeKey(server, i) + "#salt" + attempt);
            }

            ring.put(position, server);
            positions.add(position);
        }

        serverToVirtualNodeHashes.put(server, positions);
    }

    public void removeServer(Server server) {
        if (server == null) return;
        List<Long> positions = serverToVirtualNodeHashes.remove(server);
        if (positions == null) return;

        // Remove exactly the positions that were inserted for this server.
        for (Long position : positions) {
            ring.remove(position);
        }
    }

    // Routes an arbitrary key (e.g., userId) to the next clockwise server on the ring.
    public Server route(String key) {
        return getServerForKey(key);
    }

    public Server getServerForKey(String key) {
        Objects.requireNonNull(key, "key");
        if (ring.isEmpty()) {
            throw new IllegalStateException("No servers available");
        }
        long keyHash = hash64(key);
        Map.Entry<Long, Server> entry = ring.ceilingEntry(keyHash);
        if (entry == null) {
            // Wrap around the ring.
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    public int getServerCount() {
        return serverToVirtualNodeHashes.size();
    }

    public int getVirtualNodeCount() {
        return ring.size();
    }

    private static String buildVirtualNodeKey(Server server, int replicaIndex) {
        return server.getId() + "#" + replicaIndex;
    }

    // Simple deterministic 64-bit hash (FNV-1a style).
    private static long hash64(String s) {
        final long prime = 1099511628211L;
        long hash = 1469598103934665603L;
        for (int i = 0; i < s.length(); i++) {
            hash ^= s.charAt(i);
            hash *= prime;
        }
        return hash;
    }
}