package wagemaker.co.uk.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import wagemaker.co.uk.crypto.CryptoException;
import wagemaker.co.uk.crypto.DESDecryptionService;
import wagemaker.co.uk.crypto.EncryptionService;
import wagemaker.co.uk.crypto.InvalidPasswordException;
import wagemaker.co.uk.util.Util;

public class PasswordDatabasePersistence {

    private static final String FILE_HEADER = "RJW";
    private static final int DB_VERSION = 3;

    private EncryptionService encryptionService;

    public PasswordDatabasePersistence() {
    }

    public PasswordDatabasePersistence(char[] password) throws CryptoException {
        encryptionService = new EncryptionService(password);
    }

    public PasswordDatabase load(File databaseFile) throws InvalidPasswordException, ProblemReadingDatabaseFile, IOException {

        byte[] fullDatabase = readFile(databaseFile);

        // Check the database is a minimum length
        if (fullDatabase.length < EncryptionService.SALT_LENGTH) {
            throw new ProblemReadingDatabaseFile("This file doesn't appear to be a OrangePass database");
        }

        PasswordDatabase passwordDatabase = null;
        ByteArrayInputStream is = null;
        Revision revision = null;
        DatabaseOptions dbOptions = null;
        HashMap<String, AccountInformation> accounts = null;
        Charset charset = Charset.forName("UTF-8");

        // Ensure this is a real UPM database by checking for the existence of 
        // the string "UPM" at the start of the file
        byte[] header = new byte[FILE_HEADER.getBytes().length];
        System.arraycopy(fullDatabase, 0, header, 0, header.length);
        if (Arrays.equals(header, FILE_HEADER.getBytes())) {

            // Calculate the positions of each item in the file
            int dbVersionPos      = header.length;
            int saltPos           = dbVersionPos + 1;
            int encryptedBytesPos = saltPos + EncryptionService.SALT_LENGTH;

            // Get the database version 
            byte dbVersion = fullDatabase[dbVersionPos];

            if (dbVersion == 2 || dbVersion == 3) {
                byte[] salt = new byte[EncryptionService.SALT_LENGTH];
                System.arraycopy(fullDatabase, saltPos, salt, 0, EncryptionService.SALT_LENGTH);
                int encryptedBytesLength = fullDatabase.length - encryptedBytesPos;
                byte[] encryptedBytes = new byte[encryptedBytesLength]; 
                System.arraycopy(fullDatabase, encryptedBytesPos, encryptedBytes, 0, encryptedBytesLength);

                if (dbVersion < 3) {
                    charset = Util.defaultCharset();
                }

                //Attempt to decrypt the database information
                byte[] decryptedBytes;
                try {
                    decryptedBytes = encryptionService.decrypt(encryptedBytes);
                } catch (CryptoException e1) {
                    throw new InvalidPasswordException();
                }

                //If we've got here then the database was successfully decrypted 
                is = new ByteArrayInputStream(decryptedBytes);
                try {
                    revision = new Revision(is);
                    dbOptions = new DatabaseOptions(is);
    
                    // Read the remainder of the database in now
                    accounts = new HashMap<String, AccountInformation>();
                    try {
                        while (true) { //keep loading accounts until an EOFException is thrown
                            AccountInformation ai = new AccountInformation(is, charset);
                            accounts.put(ai.getAccountName(), ai);
                        }
                    } catch (EOFException e) {
                        //just means we hit eof
                    }
                    is.close();
                } catch (IOException e) {
                    throw new ProblemReadingDatabaseFile(e.getMessage(), e);
                }

                passwordDatabase = new PasswordDatabase(revision, dbOptions, accounts, databaseFile);

            } else {
                throw new ProblemReadingDatabaseFile("Don't know how to handle database version [" + dbVersion + "]");
            }

        } else {
            
            throw new InvalidPasswordException();
        }
                
        return passwordDatabase;

    }

    public PasswordDatabase load(File databaseFile, char[] password) throws IOException, ProblemReadingDatabaseFile, InvalidPasswordException, CryptoException {

        byte[] fullDatabase;
        fullDatabase = readFile(databaseFile);

        // Check the database is a minimum length
        if (fullDatabase.length < EncryptionService.SALT_LENGTH) {
            throw new ProblemReadingDatabaseFile("This file doesn't appear to be a OrangePass database");
        }

        ByteArrayInputStream is = null;
        Revision revision = null;
        DatabaseOptions dbOptions = null;
        Charset charset = Charset.forName("UTF-8");

        // Ensure this is a real UPM database by checking for the existence of 
        // the string "UPM" at the start of the file
        byte[] header = new byte[FILE_HEADER.getBytes().length];
        System.arraycopy(fullDatabase, 0, header, 0, header.length);
        if (Arrays.equals(header, FILE_HEADER.getBytes())) {

            // Calculate the positions of each item in the file
            int dbVersionPos      = header.length;
            int saltPos           = dbVersionPos + 1;
            int encryptedBytesPos = saltPos + EncryptionService.SALT_LENGTH;

            // Get the database version 
            byte dbVersion = fullDatabase[dbVersionPos];

            if (dbVersion == 2 || dbVersion == 3) {
                byte[] salt = new byte[EncryptionService.SALT_LENGTH];
                System.arraycopy(fullDatabase, saltPos, salt, 0, EncryptionService.SALT_LENGTH);
                int encryptedBytesLength = fullDatabase.length - encryptedBytesPos;
                byte[] encryptedBytes = new byte[encryptedBytesLength]; 
                System.arraycopy(fullDatabase, encryptedBytesPos, encryptedBytes, 0, encryptedBytesLength);

                // From version 3 onwards Strings in AccountInformation are
                // encoded using UTF-8. To ensure we can still open older dbs
                // we default back to the then character set, the system default
                if (dbVersion < 3) {
                    charset = Util.defaultCharset();
                }

                //Attempt to decrypt the database information
                encryptionService = new EncryptionService(password, salt);
                byte[] decryptedBytes;
                try {
                    decryptedBytes = encryptionService.decrypt(encryptedBytes);
                } catch (CryptoException e) {
                    throw new InvalidPasswordException();
                }

                //If we've got here then the database was successfully decrypted 
                is = new ByteArrayInputStream(decryptedBytes);
                revision = new Revision(is);
                dbOptions = new DatabaseOptions(is);
            } else {
                throw new ProblemReadingDatabaseFile("Don't know how to handle database version [" + dbVersion + "]");
            }

        } else {
            
            // This might be an old database (pre version 2) so try loading it using the old database format
            
            // Check the database is a minimum length
            if (fullDatabase.length < EncryptionService.SALT_LENGTH) {
                throw new ProblemReadingDatabaseFile("This file doesn't appear to be a OrangePass database");
            }
            
            //Split up the salt and encrypted bytes
            byte[] salt = new byte[EncryptionService.SALT_LENGTH];
            System.arraycopy(fullDatabase, 0, salt, 0, EncryptionService.SALT_LENGTH);
            int encryptedBytesLength = fullDatabase.length - EncryptionService.SALT_LENGTH;
            byte[] encryptedBytes = new byte[encryptedBytesLength]; 
            System.arraycopy(fullDatabase, EncryptionService.SALT_LENGTH, encryptedBytes, 0, encryptedBytesLength);

            byte[] decryptedBytes = null;
            //Attempt to decrypt the database information
            try {
                decryptedBytes = DESDecryptionService.decrypt(password, salt, encryptedBytes);
            } catch (CryptoException e) {
                throw new InvalidPasswordException();
            }

            //We'll get to here if the password was correct so load up the decryped byte
            is = new ByteArrayInputStream(decryptedBytes);
            DatabaseHeader dh = new DatabaseHeader(is);

            // At this point we'll check to see what version the database is and load it accordingly
            if (dh.getVersion().equals("1.1.0")) {
                // Version 1.1.0 introduced a revision number & database options so read that in now
                revision = new Revision(is);
                dbOptions = new DatabaseOptions(is);
            } else if (dh.getVersion().equals("1.0.0")) {
                revision = new Revision();
                dbOptions = new DatabaseOptions();
            } else {
                throw new ProblemReadingDatabaseFile("Don't know how to handle database version [" + dh.getVersion() + "]");
            }

            // Initialise the EncryptionService so that it's ready for the "save" operation
            encryptionService = new EncryptionService(password);

        }
        
        // Read the remainder of the database in now
        HashMap<String, AccountInformation> accounts = new HashMap<String, AccountInformation>();
        try {
            while (true) { //keep loading accounts until an EOFException is thrown
                AccountInformation ai = new AccountInformation(is, charset);
                accounts.put(ai.getAccountName(), ai);
            }
        } catch (EOFException e) {
            //just means we hit eof
        }
        is.close();

        PasswordDatabase passwordDatabase = new PasswordDatabase(revision, dbOptions, accounts, databaseFile);
        
        return passwordDatabase;

    }

    public void save(PasswordDatabase database) throws IOException, CryptoException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        // Flatpack the database revision and options
        database.getRevisionObj().increment();
        database.getRevisionObj().flatPack(os);
        database.getDbOptions().flatPack(os);

        // Flatpack the accounts
        Iterator<?> it = database.getAccountsHash().values().iterator();
        while (it.hasNext()) {
            AccountInformation ai = (AccountInformation) it.next();
            ai.flatPack(os);
        }
        os.close();
        byte[] dataToEncrypt = os.toByteArray();

        //Now encrypt the database data
        byte[] encryptedData = encryptionService.encrypt(dataToEncrypt);
        
        //Write the salt and the encrypted data out to the database file
        FileOutputStream fos = new FileOutputStream(database.getDatabaseFile());
        fos.write(FILE_HEADER.getBytes());
        fos.write(DB_VERSION);
        fos.write(encryptionService.getSalt());
        fos.write(encryptedData);
        fos.close();
    }

    public EncryptionService getEncryptionService() {
        return encryptionService;
    }

    private byte[] readFile(File file) throws IOException {
        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (IOException e) {
            throw new IOException("There was a problem with opening the file", e);
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) file.length()];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        
        try {
            while (offset < bytes.length
                    && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
    
            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
        } finally {
            is.close();
        }

        return bytes;
    }

}
