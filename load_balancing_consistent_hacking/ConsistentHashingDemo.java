public class ConsistentHashingDemo {
    public static void main(String[] args) {
        LoadBalancer loadBalancer = new LoadBalancer(3);

        Server server1 = new Server("127.0.0.1", 8001, 1);
        Server server2 = new Server("127.0.0.1", 8002, 2);
        Server server3 = new Server("127.0.0.1", 8003, 3);

        loadBalancer.addServer(server1);
        loadBalancer.addServer(server2);

        String[] keys = {"alice", "bob", "carol", "dave", "eve"};

        System.out.println("== Initial routing ==");
        printAssignments(loadBalancer, keys);

        System.out.println("\n== Remove server " + server2 + " ==");
        loadBalancer.removeServer(server2);
        printAssignments(loadBalancer, keys);

        System.out.println("\n== Add server " + server3 + " ==");
        loadBalancer.addServer(server3);
        printAssignments(loadBalancer, keys);
    }

    private static void printAssignments(LoadBalancer loadBalancer, String[] keys) {
        for (String key : keys) {
            System.out.println(key + " -> " + loadBalancer.getServerForKey(key));
        }
    }
}
