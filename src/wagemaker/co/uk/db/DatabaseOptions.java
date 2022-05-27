package wagemaker.co.uk.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseOptions extends FlatPackObject {

    private String remoteLocation;
    private String authDBEntry;

    
    public DatabaseOptions() {
        remoteLocation = "";
        authDBEntry = "";
    }
    
    
    public DatabaseOptions(InputStream is) throws IOException, ProblemReadingDatabaseFile {
        remoteLocation = getString(is);
        authDBEntry = getString(is);
    }
    
    
    public void setRemoteLocation(String remoteLocation) {
        if (remoteLocation == null) {
            remoteLocation = "";
        }
        this.remoteLocation = remoteLocation;
    }
    
    
    public String getRemoteLocation() {
        return remoteLocation;
    }
    
    
    public void flatPack(OutputStream os) throws IOException {
        os.write(flatPack(remoteLocation));
        os.write(flatPack(authDBEntry));
    }


    public String getAuthDBEntry() {
        return authDBEntry;
    }


    public void setAuthDBEntry(String authDBEntry) {
        if (authDBEntry == null) {
            authDBEntry = "";
        }
        this.authDBEntry = authDBEntry;
    }

}
