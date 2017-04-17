package peertopeer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server {

    public static final int LOGIN = 1;
    public static final int QUERY = 2;
    public static final int LOGOUT = 4;

    private HashMap<String, ArrayList<ServerInfo>> index;
    private ServerInfo selfInfo;

    public Server(ServerInfo selfInfo){
        index = new HashMap<String, ArrayList<ServerInfo>>();
        this.selfInfo = selfInfo;
    }

    public void run(){
        try{
            ServerSocket serverSocket = new ServerSocket(selfInfo.port);
            System.out.println("Server " + selfInfo + " online");
            while(true){
                Thread t = new Thread(new ConnectionHandler(serverSocket.accept()));
                t.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private class ConnectionHandler implements Runnable{

        private Socket socket;

        public ConnectionHandler(Socket socket){
            this.socket = socket;
        }

        public void run(){
            try {
                int protocol = socket.getInputStream().read();
                switch (protocol){
                    case LOGIN:
                        System.out.println(selfInfo.name + "> Incoming LOGIN protocol");
                        login();
                        break;
                    case QUERY:
                        System.out.println(selfInfo.name + "> Incoming QUERY protocol");
                        query();
                        break;
                    case LOGOUT:
                        System.out.println(selfInfo.name + "> Incoming LOGOUT protocol");
                        logout();
                        break;
                    default:
                        System.out.println(selfInfo.name + "> Incoming connection of unknown protocol");
                        break;
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void login() throws IOException, ClassNotFoundException{
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ServerInfo clientInfo = (ServerInfo)in.readObject();
            ArrayList<String> clientFiles = (ArrayList<String>)in.readObject();
            for (String file : clientFiles){
                if (!index.containsKey(file))
                    index.put(file, new ArrayList<ServerInfo>());
                if (!index.get(file).contains(clientInfo))
                    index.get(file).add(clientInfo);
            }
            System.out.println(selfInfo.name + "> Client " + clientInfo + " successfully registered " + clientFiles.size() + " files.");
        }

        private void query() throws IOException, ClassNotFoundException{
            String filename;
            ServerInfo fileLocation = null;
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            filename = (String)in.readObject();
            if (index.containsKey(filename)){
                fileLocation = index.get(filename).get(0);
                System.out.println(selfInfo.name + "> Successfully found file \"" + filename + "\"");
            }
            else {
                System.out.println(selfInfo.name + "> No result for \"" + filename + "\"");
            }
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(fileLocation);
        }

        private void logout() throws IOException, ClassNotFoundException{
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ServerInfo clientInfo = (ServerInfo)in.readObject();
            Iterator entry = index.entrySet().iterator();
            while (entry.hasNext()) {
                Map.Entry pair = (Map.Entry)entry.next();
                ArrayList<ServerInfo> clients = (ArrayList<ServerInfo>)pair.getValue();
                if (clients.contains(clientInfo)){
                    if (clients.size() == 1){
                        entry.remove();
                    } else {
                        clients.remove(clientInfo);
                    }
                }
            }
            System.out.println(selfInfo.name + "> Client " + clientInfo + " successfully unregistered");
        }
    }
}
