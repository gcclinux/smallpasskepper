package wagemaker.co.uk.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class PasswordDatabase {

    private File databaseFile;
    private Revision revision;
    private DatabaseOptions dbOptions;
    private HashMap<String, AccountInformation> accounts;

    
    public PasswordDatabase(Revision revision, DatabaseOptions dbOptions, HashMap<String, AccountInformation> accounts, File databaseFile) {
        this.revision = revision;
        this.dbOptions = dbOptions;
        this.accounts = accounts;
        this.databaseFile = databaseFile;
    }


    public PasswordDatabase(File dbFile) {
        this.revision = new Revision();
        this.dbOptions = new DatabaseOptions();
        this.accounts = new HashMap<String, AccountInformation>();
        this.databaseFile = dbFile;
    }
    

    public void addAccount(AccountInformation ai) {
        accounts.put(ai.getAccountName(), ai);
    }
    

    public void deleteAccount(String accountName) {
        accounts.remove(accountName);
    }

    
    public AccountInformation getAccount(String name) {
        return (AccountInformation) accounts.get(name);
    }


    public ArrayList<AccountInformation> getAccounts() {
        return new ArrayList<AccountInformation>(accounts.values());
    }


    public HashMap<String, AccountInformation> getAccountsHash() {
        return accounts;
    }
    
    
    public File getDatabaseFile() {
        return databaseFile;
    }


    public DatabaseOptions getDbOptions() {
        return dbOptions;
    }


    public Revision getRevisionObj() {
        return revision;
    }

       
    public int getRevision() {
        return revision.getRevision();
    }

}
