package peertopeer;

import java.io.Serializable;


/**
 * Contains informations about a server
 */
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

    @Override
    public boolean equals(Object obj) {
        ServerInfo info = (ServerInfo)obj;
        return (info.name.equals(name)) && (info.ip.equals(ip)) && (info.port == port);
    }

    @Override
    public String toString() {
        return "\"" + name + "\" (" + ip + ":" + port + ")";
    }
}
