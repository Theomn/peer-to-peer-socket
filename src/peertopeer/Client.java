package peertopeer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    private ArrayList<String> files;
    private String path;
    private ServerInfo centralServer;
    private ServerInfo selfInfo;
    private ServerSocket serverSocket;

    private static final String ADD_COMMAND = "add";
    private static final String LOGIN_COMMAND = "login";
    private static final String QUERY_COMMAND = "query";
    private static final String DOWNLOAD_COMMAND = "dl";
    private static final String LOGOUT_COMMAND = "logout";

    public Client(ServerInfo centralServer, ServerInfo selfInfo, String path){
        try{
            this.centralServer = centralServer;
            this.selfInfo = selfInfo;
            this. path = path;
            files = new ArrayList<String>();
            serverSocket = new ServerSocket(selfInfo.port);
            Runnable server = () -> {
                try{
                    while(true){
                        Thread t = new Thread(new ConnectionHandler(serverSocket.accept()));
                        t.start();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            };
            new Thread(server).start();
            System.out.println("Client " + selfInfo.name + " online (" + selfInfo.ip + ":" + selfInfo.port + ")");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void commandPrompt(){
        Scanner in = new Scanner(System.in);
        String command;
        String[] split;

        while(true){
            System.out.print(selfInfo.name + "$ ");
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
                try{
                    serverSocket.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
                break;
            }
            else{
                System.out.println("Unknown command");
            }
        }
    }

    private class ConnectionHandler implements Runnable{

        private Socket socket;

        public ConnectionHandler(Socket socket){
            this.socket = socket;
        }

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
                    System.out.println("Sending byte array");
                    out.write(file);
                }
                else {
                    dataOut.writeInt(0);
                }
                socket.close();
                System.out.println("Server> Everything sent");

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void login(){
        File folder = new File(path);
        File[] filesList = folder.listFiles();
        for (File file : filesList){
            if (file.isFile() && !files.contains(file.getName())){
                files.add(file.getName());
            }
        }
        try{
            Socket socket = new Socket(centralServer.ip, centralServer.port);
            socket.getOutputStream().write(Server.LOGIN);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(selfInfo);
            out.writeObject(files);
            socket.close();
            System.out.println("Successfully registered " + files.size() + " files");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private ServerInfo query(String filename){
        ServerInfo fileLocation = null;
        try{
            Socket socket = new Socket(centralServer.ip, centralServer.port);
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

    private void download(String filename, ServerInfo fileLocation){
        try{
            Socket socket = new Socket(fileLocation.ip, fileLocation.port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            InputStream in = socket.getInputStream();
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            out.writeObject(filename);
            int size = dataIn.readInt();
            if (size > 0) {
                byte[] file = new byte[size];
                System.out.println("receiving byte array");
                in.read(file);
                FileOutputStream fos = new FileOutputStream("recv/" + filename);
                fos.write(file);
                fos.close();
            } else {
                System.out.println("No file found");
            }
            socket.close();
            System.out.println("Client> Everything reveiced");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
