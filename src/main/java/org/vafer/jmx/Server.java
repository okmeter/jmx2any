package org.vafer.jmx;

public class Server {

    private final String id;

    private final String host;

    private final String port;

    private final String nsPath;

    public Server(String nsHostPort) throws Exception {
        this.id = nsHostPort;
        String[] parts = nsHostPort.split(":");
        switch (parts.length) {
            case 2:
                this.nsPath = "";
                this.host = parts[0];
                this.port = parts[1];
                break;
            case 3:
                this.nsPath = parts[0];
                this.host = parts[1];
                this.port = parts[2];
                break;
            default:
                throw new IllegalArgumentException("Invalid server: " + nsHostPort);
        }
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getNsPath() {
        return nsPath;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Server server = (Server) o;
        return id.equals(server.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Server{host='%s', port='%s', ns='%s'}", host, port, nsPath);
    }
}
