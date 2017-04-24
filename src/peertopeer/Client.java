package peertopeer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The peer to peer servent
 */
public class Client {

    private ArrayList<String> files;
    private String path;
    private ServerInfo remoteServerInfo;
    private ServerInfo localServerInfo;
    private Thread localServer;

    private static final String ADD_COMMAND = "add";
    private static final String LOGIN_COMMAND = "login";
    private static final String QUERY_COMMAND = "query";
    private static final String DOWNLOAD_COMMAND = "dl";
    private static final String LOGOUT_COMMAND = "logout";
    private static final String HELP_COMMAND = "help";

    /**
     * Creates a client and starts its server socket
     * @param remoteServerInfo the central server
     * @param localServerInfo information about this client to be sent to the central server
     * @param path the folder containing the files to share on the network
     */
    public Client(ServerInfo remoteServerInfo, ServerInfo localServerInfo, String path){
        this.remoteServerInfo = remoteServerInfo;
        this.localServerInfo = localServerInfo;
        this. path = path;
        files = new ArrayList<String>();
        Runnable ls = () -> {
            try{
                ServerSocket serverSocket = new ServerSocket(localServerInfo.port);
                while(true){
                    Thread t = new Thread(new ConnectionHandler(serverSocket.accept()));
                    t.start();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        };
        localServer = new Thread(ls);
        localServer.start();
        System.out.println("Client " + localServerInfo + " online");
    }

    /**
     * Starts the client interface
     */
    public void commandPrompt(){
        Scanner in = new Scanner(System.in);
        String command;
        String[] split;

        System.out.println("type \"help\" for a list of commands");
        while(true){
            System.out.print(localServerInfo.name + "$ ");
            command = in.nextLine();
            split = command.split(" ");
            if (split[0].equals(ADD_COMMAND)){
                if (split.length < 2){
                    System.out.println("Missing filename");
                } else if (!files.contains(split[1])){
                    files.add(split[1]);
                }
            }
            else if (split[0].equals(LOGIN_COMMAND)){
                login();
            }
            else if (split[0].equals(QUERY_COMMAND)){
                if (split.length < 2){
                    System.out.println("Missing filename");
                } else {
                    query(split[1]);
                }
            }
            else if (split[0].equals(DOWNLOAD_COMMAND)){
                if (split.length < 2){
                    System.out.println("Missing filename");
                } else {
                    ServerInfo info = query(split[1]);
                    if (info != null) {
                        download(split[1], info);
                    }
                }
            }
            else if (split[0].equals(LOGOUT_COMMAND)){
                logout();
                localServer.interrupt();
                break;
            }
            else if (split[0].equals(HELP_COMMAND)){
                System.out.println("===== COMMAND LIST =====");
                System.out.println("login - connect to the server");
                System.out.println("dl [filename] - download specified file");
                System.out.println("query [filename] - ask server if it can find specified file");
                System.out.println("logout - logs out from the server");
            }
            else{
                System.out.println("Unknown command");
            }
        }
    }

    /**
     * private runnable to handle incoming connections to that servent's server socket
     */
    private class ConnectionHandler implements Runnable{

        private Socket socket;

        public ConnectionHandler(Socket socket){
            this.socket = socket;
        }

        /**
         * handles incoming download protocol
         */
        public void run(){
            try {
                String filename;
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                OutputStream out = socket.getOutputStream();
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                filename = (String)in.readObject();
                if (files.contains(filename)){
                    byte[] file = Files.readAllBytes(new File(path + filename).toPath());
                    dataOut.writeInt(file.length);
                    out.write(file);
                }
                else {
                    dataOut.writeInt(0);
                }
                socket.close();

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * log this servent's files to the central server
     */
    private void login(){
        File folder = new File(path);
        File[] filesList = folder.listFiles();
        for (File file : filesList){
            if (file.isFile() && !files.contains(file.getName())){
                files.add(file.getName());
            }
        }
        Socket socket;
        try {
            socket = new Socket(remoteServerInfo.ip, remoteServerInfo.port);
        } catch (IOException e){
            System.out.println("Could not connect to " + remoteServerInfo);
            return;
        }
        try{
            socket.getOutputStream().write(Server.LOGIN);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(localServerInfo);
            out.writeObject(files);
            socket.close();
            System.out.println("Successfully registered " + files.size() + " file(s) to " + remoteServerInfo);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Ask the server for the location of a file
     * @param filename the name of the file to find
     * @return
     */
    private ServerInfo query(String filename){
        ServerInfo fileLocation = null;
        Socket socket;
        try {
            socket = new Socket(remoteServerInfo.ip, remoteServerInfo.port);
        } catch (IOException e){
            System.out.println("Could not connect to " + remoteServerInfo);
            return null;
        }
        try{
            socket.getOutputStream().write(Server.QUERY);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(filename);
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            fileLocation = (ServerInfo)in.readObject();
            socket.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        if (fileLocation != null){
            System.out.println("Server found location for " + filename);
        } else {
            System.out.println("Server could not find " + filename);
        }
        return fileLocation;
    }

    /**
     * Download a file from another servent
     * @param filename
     * @param fileLocation
     */
    private void download(String filename, ServerInfo fileLocation){
        Socket socket;
        try {
            socket = new Socket(fileLocation.ip, fileLocation.port);
        } catch (IOException e){
            System.out.println("Could not connect to " + fileLocation);
            return;
        }

        try{
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            InputStream in = socket.getInputStream();
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            out.writeObject(filename);
            int size = dataIn.readInt();
            if (size > 0) {
                byte[] file = new byte[size];
                in.read(file);
                FileOutputStream fos = new FileOutputStream("recv/" + filename);
                fos.write(file);
                fos.flush();
                fos.close();
            } else {
                System.out.println(filename + " not found on client \"" + fileLocation);
            }
            socket.close();
            System.out.println("Successfully downloaded " + filename);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * log files out from the central server
     */
    private void logout(){
        Socket socket;
        try {
            socket = new Socket(remoteServerInfo.ip, remoteServerInfo.port);
        } catch (IOException e){
            System.out.println("Could not connect to " + remoteServerInfo);
            return;
        }
        try{
            socket.getOutputStream().write(Server.LOGOUT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(localServerInfo);
            socket.close();
            System.out.println("Successfully unregistered from " + remoteServerInfo);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
