package peertopeer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    private ArrayList<String> files;
    private String name;
    private ServerInfo centralServer;
    private ServerInfo selfInfo;
    private ServerSocket serverSocket;

    private static final String ADD_COMMAND = "add";
    private static final String LOGIN_COMMAND = "login";
    private static final String QUERY_COMMAND = "query";
    private static final String LOGOUT_COMMAND = "logout";

    public Client(ServerInfo centralServer, ServerInfo selfInfo){
        try{
            this.centralServer = centralServer;
            this.selfInfo = selfInfo;
            files = new ArrayList<String>();
            serverSocket = new ServerSocket(selfInfo.port);
            System.out.println("Client " + selfInfo.name + " online (" + selfInfo.ip + ":" + selfInfo.port + ")");
            serverSocket.close();
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
            else if (split[0].equals(LOGOUT_COMMAND)){
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
            // TODO
        }
    }

    private void login(){
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
            // TODO
            socket.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getName(){
        return name;
    }
}
