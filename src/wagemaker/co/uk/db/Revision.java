package wagemaker.co.uk.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Revision extends FlatPackObject {

    private int revision;
    
    
    public Revision() {
        revision = 0;
    }
    
    
    public int increment() {
        return ++revision;
    }
    
    public Revision(InputStream is) throws IOException, ProblemReadingDatabaseFile {
        revision = getInt(is);
    }

    
    public void flatPack(OutputStream os) throws IOException {
        os.write(flatPack(String.valueOf(revision)));
    }

    
    public int getRevision() {
        return revision;
    }
    
    
    public void setRevision(int revision) {
        this.revision = revision;
    }

}
