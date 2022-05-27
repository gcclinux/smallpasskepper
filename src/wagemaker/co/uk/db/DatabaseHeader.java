package wagemaker.co.uk.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DatabaseHeader extends FlatPackObject {

    private int majorVersion;
    private int minorVersion;
    private int patchVersion;
    
    
    public DatabaseHeader(InputStream is) throws IOException, ProblemReadingDatabaseFile {
        assemble(is);
    }
    
    
    public DatabaseHeader(int majorVersion, int minorVersion, int patchVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
    }
    
    
    public void flatPack(OutputStream os) throws IOException {
        os.write(flatPack(String.valueOf(majorVersion)));
        os.write(flatPack(String.valueOf(minorVersion)));
        os.write(flatPack(String.valueOf(patchVersion)));
    }

    
    private void assemble(InputStream is) throws IOException, ProblemReadingDatabaseFile {
        majorVersion = getInt(is);
        minorVersion = getInt(is);
        patchVersion = getInt(is);
    }    

    public String getVersion() {
        StringBuffer buf = new StringBuffer();
        buf.append(majorVersion);
        buf.append('.');
        buf.append(minorVersion);
        buf.append('.');
        buf.append(patchVersion);
        return buf.toString();
    }


    public int getMajorVersion() {
        return majorVersion;
    }


    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }


    public int getMinorVersion() {
        return minorVersion;
    }


    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }


    public int getPatchVersion() {
        return patchVersion;
    }


    public void setPatchVersion(int patchVersion) {
        this.patchVersion = patchVersion;
    }

}
