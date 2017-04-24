package peertopeer;

/**
 * Launches a client
 */
public class ClientLauncher {
    public static void main(String argv[]) throws Exception {
        ServerInfo remoteServer = new ServerInfo("Central", "localhost", 5555);
        ServerInfo localServer = new ServerInfo("client", "localhost", 5556);
        Client client = new Client(remoteServer, localServer, "files/");
        client.commandPrompt();
    }
}
