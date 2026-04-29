import java.util.Objects;

public class Server {
    private final String host;
    private final int port;
    private final int id;

    public Server(String host, int port, int id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        // Must be consistent with equals(); otherwise HashSet/Map behavior breaks.
        return Objects.hash(host, port, id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Server)) return false;
        Server server = (Server) other;
        return id == server.id && port == server.port && host.equals(server.host);
    }

    @Override
    public String toString() {
        return id + "(" + host + ":" + port + ")";
    }
}