package peertopeer;

public class ClientLauncher {
    public static void main(String argv[]) throws Exception {
        ServerInfo remoteServer = new ServerInfo("Central", "localhost", 5555);
        ServerInfo localServer = new ServerInfo("client", "96.22.242.48", 5556);
        Client client = new Client(remoteServer, localServer, "files/");
        client.commandPrompt();
    }
}
