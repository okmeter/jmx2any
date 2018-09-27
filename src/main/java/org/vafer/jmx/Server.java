package org.vafer.jmx;

public class Server {

    private final String id;

    private final String hostPort;

    private final String nsPath;

    public Server(String hostPort, String nsPath) throws Exception {
        this.hostPort = hostPort;
        this.nsPath = nsPath;
        if (nsPath != null) {
            this.id = nsPath + ":" + hostPort;
        } else {
            this.id = hostPort;
        }
    }

    public String getHostPort() {
        return hostPort;
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
        return String.format("Server{hostPort='%s', ns='%s'}", hostPort, nsPath);
    }
}
