package peertopeer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileInfo {
    public String filename;
    public String path;

    public FileInfo(String filename){
        this.filename = filename;
        this.path = "";
    }

    public byte[] loadFile() throws IOException{
        return Files.readAllBytes(new File(path + filename).toPath());
    }
}
