package peertopeer;

/**
 * Created by Theo on 13/04/17.
 */
public class ClientLauncher {
    public static void main(String argv[]) throws Exception {
        ServerInfo centralServer = new ServerInfo("localhost", 5555);
        ServerInfo info = new ServerInfo("client", "localhost", 5556);
        Client client = new Client(centralServer, info);
        client.commandPrompt();
    }
}
