package wagemaker.co.uk.db;

import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import wagemaker.co.uk.util.Util;

public abstract class FlatPackObject {

    private static int LENGTH_FIELD_NUM_CHARS = 4;
    protected byte[] flatPack(String s) throws UnsupportedEncodingException {
        return flatPack(s.getBytes("UTF-8"));
    }
    

    protected byte[] flatPack(byte[] bytesToFlatPack) throws UnsupportedEncodingException {
        //Create a byte array populated with the field length 
        String l = Util.lpad(bytesToFlatPack.length, LENGTH_FIELD_NUM_CHARS, '0');
        byte[] fieldLengthBytes = l.getBytes("UTF-8");
        
        //Declare the buffer we're going to return
        byte[] returnBuffer = new byte[fieldLengthBytes.length + bytesToFlatPack.length];

        //Populate the return buffer with the 'field length' bytes and 'field contents' bytes
        System.arraycopy(fieldLengthBytes, 0, returnBuffer, 0, fieldLengthBytes.length);
        System.arraycopy(bytesToFlatPack, 0, returnBuffer, fieldLengthBytes.length, bytesToFlatPack.length);

        return returnBuffer;
    }


    public byte[] getBytes(InputStream is) throws IOException, ProblemReadingDatabaseFile {
        
        byte[] fieldContents = null;
        
        //Get the length of the next field
        byte[] fieldLength = new byte[LENGTH_FIELD_NUM_CHARS];
        int bytesRead = is.read(fieldLength);
        if (bytesRead == -1 || bytesRead != LENGTH_FIELD_NUM_CHARS) {
            throw new EOFException();
        }
        String s = new String(fieldLength);
        try {
            int i = Integer.parseInt(s);

            //Read the field
            fieldContents = new byte[i];
            
            //Had to do it this way because the next section (commented out)
            //didn't read in the correct number of bytes
            for (int j=0; j<i; j++) {
                fieldContents[j] = (byte) is.read();
                if (fieldContents[j] == -1) {
                    throw new EOFException();
                }
            }
            
            /*if (i > 0) {
                bytesRead = is.read(fieldContents);
                //I had to comment this next line out because the CipherInputStream reads one to few bytes for
                //the last field in the file, don't know why??? Problem now is I'm not checking that the number
                //of bytes read is correct
                //if (bytesRead == -1 || bytesRead != i) { 
                if (bytesRead == -1) {
                    throw new EOFException();
                }
            }*/
            
        } catch (NumberFormatException e) {
            throw new ProblemReadingDatabaseFile("A field length had invalid characters", e);
        }
                
        return fieldContents;
        
    }
    
    
    public int getInt(InputStream is) throws IOException, ProblemReadingDatabaseFile {
        return Integer.parseInt(getString(is));
    }


    public String getString(InputStream is) throws IOException, ProblemReadingDatabaseFile {
        return new String(getBytes(is), "UTF-8");
    }


    public String getString(InputStream is, Charset charset) throws IOException, ProblemReadingDatabaseFile {
        return new String(getBytes(is), charset.name());
    }

    public abstract void flatPack(OutputStream os) throws IOException;
    
}
