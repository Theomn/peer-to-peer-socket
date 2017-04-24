package peertopeer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 * Contains information about a file
 */
public class FileMetadata {
    public String filename;
    public int size;

    public FileMetadata(String filename){
        this.filename = filename;
    }

    public byte[] loadFile() throws IOException{
        return Files.readAllBytes(new File(filename).toPath());
    }

    @Override
    public boolean equals(Object obj) {
        FileMetadata other = (FileMetadata)obj;
        return filename.equals(other.filename);
    }
}
