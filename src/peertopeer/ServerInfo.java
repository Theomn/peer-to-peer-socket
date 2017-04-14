package peertopeer;

import java.io.Serializable;

public class ServerInfo implements Serializable{
    public String name;
    public String ip;
    public int port;

    public ServerInfo(String ip, int port){
        name = "defaultname";
        this.ip = ip;
        this.port = port;
    }

    public ServerInfo(String name, String ip, int port){
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

}
