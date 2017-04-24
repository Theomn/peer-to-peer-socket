package peertopeer;

/**
 * Launches a server
 */
public class ServerLauncher {
    public static void main(String argv[]) throws Exception {
        ServerInfo info = new ServerInfo("Central", "localhost", 5555);
        Server server = new Server(info);
        server.run();
    }
}
