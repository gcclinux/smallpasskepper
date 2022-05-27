package wagemaker.co.uk.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wagemaker.co.uk.crypto.CryptoException;
import wagemaker.co.uk.crypto.InvalidPasswordException;
import wagemaker.co.uk.db.AccountInformation;
import wagemaker.co.uk.db.AccountsCSVMarshaller;
import wagemaker.co.uk.db.DBException;
import wagemaker.co.uk.db.ExportException;
import wagemaker.co.uk.db.ImportException;
import wagemaker.co.uk.db.PasswordDatabase;
import wagemaker.co.uk.db.PasswordDatabasePersistence;
import wagemaker.co.uk.db.ProblemReadingDatabaseFile;
import wagemaker.co.uk.gui.LaunchGUI.ChangeDatabaseAction;
import wagemaker.co.uk.util.FileChangedCallback;
import wagemaker.co.uk.util.FileMonitor;
import wagemaker.co.uk.util.FontTheme;
import wagemaker.co.uk.util.LaunchBrowser;
import wagemaker.co.uk.util.Preferences;
import wagemaker.co.uk.util.Translator;
import wagemaker.co.uk.util.Values;



public class DatabaseActions {

    private static Log LOG = LogFactory.getLog(DatabaseActions.class);

    private LaunchGUI launchGUI;
    private PasswordDatabase database;
    private ArrayList<String> accountNames;
    private boolean localDatabaseDirty = true;
    private PasswordDatabasePersistence dbPers;
    private FileMonitor fileMonitor;
    private boolean databaseNeedsReload = false;

    private boolean lockIfInactive;
    private int msToWaitBeforeClosingDB;

    @SuppressWarnings("unused")
	private boolean runSetDBDirtyThread = true;


    public DatabaseActions(LaunchGUI launchGUI) {
        this.launchGUI = launchGUI;
    }


    @SuppressWarnings("deprecation")
	public void newDatabase() throws IOException, CryptoException {
    	
		UIManager.put("OptionPane.buttonOrientation", 0);		
		UIManager.put("OptionPane.buttonFont", FontTheme.size14b);
		UIManager.put("OptionPane.messageFont", FontTheme.size12p);
		UIManager.put("OptionPane.messageForeground",Values.WHITE);
		UIManager.put("Panel.background", Values.ORANGE);
		UIManager.put("OptionPane.background", Values.ORANGE);
		UIManager.put("Button.background", Values.ORANGE); 

        File newDatabaseFile = getSaveAsFile(Translator.translate("newPasswordDatabase"));
        if (newDatabaseFile == null) {
            return;
        }

        final JPasswordField masterPassword = new JPasswordField("");
        boolean passwordsMatch = false;
        do {
            //Get a new master password for this database from the user 
            JPasswordField confirmedMasterPassword = new JPasswordField("");
            JOptionPane pane = new JOptionPane(new Object[] {Translator.translate("enterMasterPassword"), masterPassword, Translator.translate("confirmation"), confirmedMasterPassword}, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            JDialog dialog = pane.createDialog(launchGUI, Translator.translate("masterPassword"));
            dialog.addWindowFocusListener(new WindowAdapter() {
                public void windowGainedFocus(WindowEvent e) {
                    masterPassword.requestFocusInWindow();
                }
            });
            dialog.show();

            if (pane.getValue().equals(new Integer(JOptionPane.OK_OPTION))) {
                if (!Arrays.equals(masterPassword.getPassword(), confirmedMasterPassword.getPassword())) {
                    JOptionPane.showMessageDialog(launchGUI, Translator.translate("passwordsDontMatch"));
                } else {
                    passwordsMatch = true;
                }
            } else {
                return;
            }

        } while (passwordsMatch == false);

        if (newDatabaseFile.exists()) {
            newDatabaseFile.delete();
        }

        database = new PasswordDatabase(newDatabaseFile);
        dbPers = new PasswordDatabasePersistence(masterPassword.getPassword());
        saveDatabase();
        accountNames = new ArrayList<String>();
        doOpenDatabaseActions();

        if (Preferences.get(Preferences.ApplicationOptions.DB_TO_LOAD_ON_STARTUP) == null ||
                Preferences.get(Preferences.ApplicationOptions.DB_TO_LOAD_ON_STARTUP).equals("")) {
            int option = JOptionPane.showConfirmDialog(launchGUI,
                    Translator.translate("setNewLoadOnStartupDatabase"),
                    Translator.translate("newPasswordDatabase"),
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                Preferences.set(
                        Preferences.ApplicationOptions.DB_TO_LOAD_ON_STARTUP,
                        newDatabaseFile.getAbsolutePath());
                Preferences.save();
            }
        }
    }


    @SuppressWarnings("deprecation")
	public void changeMasterPassword() throws IOException, ProblemReadingDatabaseFile, CryptoException, PasswordDatabaseException, DBException {

		UIManager.put("OptionPane.buttonOrientation", 0);		
		UIManager.put("OptionPane.buttonFont", FontTheme.size14b);
		UIManager.put("OptionPane.messageFont", FontTheme.size12p);
		UIManager.put("OptionPane.messageForeground",Values.WHITE);
		UIManager.put("Panel.background", Values.ORANGE);
		UIManager.put("OptionPane.background", Values.ORANGE);
		UIManager.put("Button.background", Values.ORANGE); 
    	
        if (getLatestVersionOfDatabase()) {
            //The first task is to get the current master password
            boolean passwordCorrect = false;
            boolean okClicked = true;
            do {
                char[] password = askUserForPassword(Translator.translate("enterDatabasePassword"));
                if (password == null) {
                    okClicked = false;
                } else {
                    try {
                        dbPers.load(database.getDatabaseFile(), password);
                        passwordCorrect = true;
                    } catch (InvalidPasswordException e) {
                        JOptionPane.showMessageDialog(launchGUI, Translator.translate("incorrectPassword"));
                    }
                }
            } while (!passwordCorrect && okClicked);

            //If the master password was entered correctly then the next step is to get the new master password
            if (passwordCorrect == true) {

                    final JPasswordField masterPassword = new JPasswordField("");
                    boolean passwordsMatch = false;
                    Object buttonClicked;

                    //Ask the user for the new master password
                    //This loop will continue until the two passwords entered match or until the user hits the cancel button
                    do {


                        JPasswordField confirmedMasterPassword = new JPasswordField("");
                        JOptionPane pane = new JOptionPane(new Object[] {Translator.translate("enterNewMasterPassword"), masterPassword, Translator.translate("confirmation"), confirmedMasterPassword}, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                        JDialog dialog = pane.createDialog(launchGUI, Translator.translate("changeMasterPassword"));
                        dialog.addWindowFocusListener(new WindowAdapter() {
                            public void windowGainedFocus(WindowEvent e) {
                                masterPassword.requestFocusInWindow();
                            }
                        });
                        dialog.show();

                        buttonClicked = pane.getValue();
                        if (buttonClicked.equals(new Integer(JOptionPane.OK_OPTION))) {
                            if (!Arrays.equals(masterPassword.getPassword(), confirmedMasterPassword.getPassword())) {
                                JOptionPane.showMessageDialog(launchGUI, Translator.translate("passwordsDontMatch"));
                            } else {
                                passwordsMatch = true;
                            }
                        }

                    } while (buttonClicked.equals(new Integer(JOptionPane.OK_OPTION)) && !passwordsMatch);

                    //If the user clicked OK and the passwords match then change the database password
                    if (buttonClicked.equals(new Integer(JOptionPane.OK_OPTION)) && passwordsMatch) {
                        this.dbPers.getEncryptionService().initCipher(masterPassword.getPassword());
                        saveDatabase();
                    }

            }
        }

    }


    public void errorHandler(Exception e) {
        e.printStackTrace();
        String errorMessage = e.getMessage();
        if (errorMessage == null) {
            errorMessage = e.getClass().getName();
        }
        JOptionPane.showMessageDialog(launchGUI, errorMessage, Translator.translate("error"), JOptionPane.ERROR_MESSAGE);
    }

    private void doCloseDatabaseActions() {
        launchGUI.getAddAccountButton().setEnabled(false);
        launchGUI.getAddAccountMenuItem().setEnabled(false);
        launchGUI.getSearchField().setEnabled(false);
        launchGUI.getSearchField().setText("");
        launchGUI.getSearchIcon().setEnabled(false);
        launchGUI.getResetSearchButton().setEnabled(false);
        launchGUI.getChangeMasterPasswordMenuItem().setEnabled(false);
        launchGUI.getExportMenuItem().setEnabled(false);
        launchGUI.getImportMenuItem().setEnabled(false);

        launchGUI.setTitle(LaunchGUI.getApplicationName());

        launchGUI.getStatusBar().setText(""); //<--
        databaseNeedsReload = false;

        SortedListModel listview = (SortedListModel) launchGUI.getAccountsListview().getModel();
        listview.clear();

        launchGUI.getEditAccountButton().setEnabled(false);
        launchGUI.getCopyUsernameButton().setEnabled(false);
        launchGUI.getCopyPasswordButton().setEnabled(false);
        launchGUI.getLaunchURLButton().setEnabled(false);
        launchGUI.getDeleteAccountButton().setEnabled(false);
        launchGUI.getEditAccountMenuItem().setEnabled(false);
        launchGUI.getCopyUsernameMenuItem().setEnabled(false);
        launchGUI.getCopyPasswordMenuItem().setEnabled(false);
        launchGUI.getLaunchURLMenuItem().setEnabled(false);
        launchGUI.getDeleteAccountMenuItem().setEnabled(false);
        launchGUI.getViewAccountMenuItem().setEnabled(false);
    }


    private void doOpenDatabaseActions() {
        launchGUI.getAddAccountButton().setEnabled(true);
        launchGUI.getAddAccountMenuItem().setEnabled(true);
        launchGUI.getSearchField().setEnabled(true);
        launchGUI.getSearchField().setText("");
        launchGUI.getSearchIcon().setEnabled(true);
        launchGUI.getResetSearchButton().setEnabled(true);
        launchGUI.getChangeMasterPasswordMenuItem().setEnabled(true);
        launchGUI.getExportMenuItem().setEnabled(true);
        launchGUI.getImportMenuItem().setEnabled(true);

//        launchGUI.setTitle(database.getDatabaseFile() + " - " + LaunchGUI.getApplicationName());
        launchGUI.setTitle(LaunchGUI.getApplicationName());

        setLocalDatabaseDirty(true);
        databaseNeedsReload = false;

        accountNames = getAccountNames();
        populateListview(accountNames);

        // Start a thread to listen for changes to the db file
        FileChangedCallback callback = new FileChangedCallback() {
            public void fileChanged(File file) {
                databaseNeedsReload = true;
                launchGUI.setFileChangedPanelVisible(true);
            }
        };
        fileMonitor = new FileMonitor(database.getDatabaseFile(), callback);
        Thread thread = new Thread(fileMonitor);
        thread.start();

        // If the user asked for the db to close after a period of
        // inactivity then register a listener to capture window focus
        // events.
        configureAutoLock();

        // Give the search field focus.
        // I'm using requestFocusInWindow() rather than
        // requestFocus() because the javadocs recommend it.
        launchGUI.getSearchField().requestFocusInWindow();

        launchGUI.getDatabaseFileChangedPanel().setVisible(false);
    }

    private void configureAutoLock() {
        lockIfInactive = Preferences.get(
                Preferences.ApplicationOptions.DATABASE_AUTO_LOCK, "false").
                    equals("true");
        msToWaitBeforeClosingDB = Preferences.getInt(
                Preferences.ApplicationOptions.DATABASE_AUTO_LOCK_TIME, 5)
                    * 60 * 1000;

        if (lockIfInactive) {
            LOG.debug("Enabling autoclose when focus lost");
            if (launchGUI.getWindowFocusListeners().length == 0) {
                launchGUI.addWindowFocusListener(new AutoLockDatabaseListener());
            }
        } else {
            LOG.debug("Disabling autoclose when focus lost");
            for (int i=0; i<launchGUI.getWindowFocusListeners().length; i++) {
                launchGUI.removeWindowFocusListener(
                        launchGUI.getWindowFocusListeners()[i]);
            }
        }
    }

    public ArrayList<String> getAccountNames() {
        ArrayList<?> dbAccounts = database.getAccounts();
        ArrayList<String> accountNames = new ArrayList<String>();
        for (int i=0; i<dbAccounts.size(); i++) {
            AccountInformation ai = (AccountInformation) dbAccounts.get(i);
            String accountName = (String) ai.getAccountName();
            accountNames.add(accountName);
        }
        return accountNames;
    }

    @SuppressWarnings("deprecation")
	private char[] askUserForPassword(String message) {
    	
		UIManager.put("OptionPane.buttonOrientation", 0);		
		UIManager.put("OptionPane.buttonFont", FontTheme.size14b);
		UIManager.put("OptionPane.messageFont", FontTheme.size12p);
		UIManager.put("OptionPane.messageForeground",Values.WHITE);
		UIManager.put("Panel.background", Values.ORANGE);
		UIManager.put("OptionPane.background", Values.ORANGE);
		UIManager.put("Button.background", Values.ORANGE); 
		
        char[] password = null;

        final JPasswordField masterPassword = new JPasswordField("");
        JOptionPane pane = new JOptionPane(new Object[] {message, masterPassword }, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(launchGUI, Translator.translate("masterPassword"));
        dialog.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                masterPassword.requestFocusInWindow();
            }
        });
        dialog.show();

        if (pane.getValue() != null && pane.getValue().equals(new Integer(JOptionPane.OK_OPTION))) {
            password = masterPassword.getPassword();
        }

        return password;
    }


    public void openDatabase(String databaseFilename) throws IOException, ProblemReadingDatabaseFile, CryptoException {
        openDatabase(databaseFilename, null);
    }


    public void openDatabase(String databaseFilename, char[] password) throws IOException, ProblemReadingDatabaseFile, CryptoException {

		UIManager.put("OptionPane.buttonOrientation", 0);		
		UIManager.put("OptionPane.buttonFont", FontTheme.size14b);
		UIManager.put("OptionPane.messageFont", FontTheme.size12p);
		UIManager.put("OptionPane.messageForeground",Values.WHITE);
		UIManager.put("Panel.background", Values.ORANGE);
		UIManager.put("OptionPane.background", Values.ORANGE);
		UIManager.put("Button.background", Values.ORANGE); 
		
        boolean passwordCorrect = false;
        boolean okClicked = true;
        while (!passwordCorrect && okClicked) {
            // If we weren't given a password then ask the user to enter one
            if (password == null) {
                password = askUserForPassword(Translator.translate("enterDatabasePassword"));
                if (password == null) {
                    okClicked = false;
                }
            } else {
                okClicked = true;
            }

            if (okClicked) {
                try {
                    dbPers = new PasswordDatabasePersistence();
                    database = dbPers.load(new File(databaseFilename), password);
                    passwordCorrect = true;
                } catch (InvalidPasswordException e) {
                    JOptionPane.showMessageDialog(launchGUI, Translator.translate("incorrectPassword"));
                    password = null;
                }
            }
        }

        if (passwordCorrect) {
            doOpenDatabaseActions();
        }

    }


    public void openDatabase() throws IOException, ProblemReadingDatabaseFile, CryptoException {
    	
		UIManager.put("OptionPane.buttonOrientation", 0);		
		UIManager.put("OptionPane.buttonFont", FontTheme.size14b);
		UIManager.put("OptionPane.messageFont", FontTheme.size12p);
		UIManager.put("OptionPane.messageForeground",Values.WHITE);
		UIManager.put("Panel.background", Values.ORANGE);
		UIManager.put("OptionPane.background", Values.ORANGE);
		UIManager.put("Button.background", Values.ORANGE); 
		
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(Translator.translate("openDatabase"));
        int returnVal = fc.showOpenDialog(launchGUI);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File databaseFile = fc.getSelectedFile();
            if (databaseFile.exists()) {
                openDatabase(databaseFile.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(launchGUI, Translator.translate("fileDoesntExistWithName", databaseFile.getAbsolutePath()), Translator.translate("fileDoesntExist"), JOptionPane.ERROR_MESSAGE);
            }
        }

        // Stop any "SetDBDirtyThread"s that are running
        runSetDBDirtyThread = false;
    }


    public void deleteAccount() throws IOException, CryptoException, DBException, ProblemReadingDatabaseFile, PasswordDatabaseException {

		UIManager.put("OptionPane.buttonOrientation", 0);		
		UIManager.put("OptionPane.buttonFont", FontTheme.size14b);
		UIManager.put("OptionPane.messageFont", FontTheme.size12p);
		UIManager.put("OptionPane.messageForeground",Values.WHITE);
		UIManager.put("Panel.background", Values.ORANGE);
		UIManager.put("OptionPane.background", Values.ORANGE);
		UIManager.put("Button.background", Values.ORANGE); 
		
        if (getLatestVersionOfDatabase()) { //TODO
            SortedListModel listview = (SortedListModel) launchGUI.getAccountsListview().getModel();
            String selectedAccName = (String) launchGUI.getAccountsListview().getSelectedValue();

            int buttonSelected = JOptionPane.showConfirmDialog(launchGUI, Translator.translate("askConfirmDeleteAccount", selectedAccName.substring(3)), Translator.translate("confirmDeleteAccount"), JOptionPane.YES_NO_OPTION);
            if (buttonSelected == JOptionPane.OK_OPTION) {
                listview.removeElement(selectedAccName.substring(3));
                int i = accountNames.indexOf(selectedAccName.substring(3));
                accountNames.remove(i);
                database.deleteAccount(selectedAccName.substring(3));
                saveDatabase();
                filter();
            }
        }

    }


    @SuppressWarnings("deprecation")
	public void addAccount() throws IOException, CryptoException, DBException, ProblemReadingDatabaseFile, PasswordDatabaseException {

        if (getLatestVersionOfDatabase()) {

            //Initialise the AccountDialog
            AccountInformation accInfo = new AccountInformation();
            AccountDialog accDialog = new AccountDialog(accInfo, launchGUI, false, accountNames);
            accDialog.pack();
            accDialog.setLocationRelativeTo(launchGUI);
            accDialog.show();

            //If the user press OK then save the new account to the database
            if (accDialog.okClicked()) {
                database.deleteAccount(accInfo.getAccountName());
                database.addAccount(accInfo);
                saveDatabase();
                accountNames.add(accInfo.getAccountName());
                filter();
            }

        }

    }


    public AccountInformation getSelectedAccount() { // TODO cleaning listview
        String selectedAccName = (String) launchGUI.getAccountsListview().getSelectedValue();     
        return database.getAccount(selectedAccName.substring(3));
    }


    @SuppressWarnings("unused")
	private boolean getLatestVersionOfDatabase() throws DBException, ProblemReadingDatabaseFile, IOException, CryptoException, PasswordDatabaseException {
        boolean latestVersionDownloaded = false;

        // Ensure we're working with the latest version of the database
        if (databaseHasRemoteInstance() && localDatabaseDirty) {
            int answer = JOptionPane.showConfirmDialog(launchGUI, Translator.translate("askSyncWithRemoteDB"), Translator.translate("syncDatabase"), JOptionPane.YES_NO_OPTION);
        } else {
            latestVersionDownloaded = true;
        }

        return latestVersionDownloaded;
    }


    private boolean databaseHasRemoteInstance() {
        if (database.getDbOptions().getRemoteLocation().equals("")) {
            return false;
        } else {
            return true;
        }
    }


    @SuppressWarnings("deprecation")
	public void viewAccount() {
        AccountInformation accInfo = getSelectedAccount();
        AccountDialog accDialog = new AccountDialog(accInfo, launchGUI, true, accountNames);
        accDialog.pack();
        accDialog.setLocationRelativeTo(launchGUI);
        accDialog.show();
    }


    @SuppressWarnings("deprecation")
	public void editAccount(String accountName) throws DBException,
            ProblemReadingDatabaseFile, IOException, CryptoException,
            PasswordDatabaseException, InvalidPasswordException, OPException {

        if (getLatestVersionOfDatabase()) {
            AccountInformation accInfo = database.getAccount(accountName.substring(3)); //TODO
            if (accInfo == null) {
                throw new OPException(
                        Translator.translate(
                                "accountDoesntExist", accountName));
            }

            AccountDialog accDialog = new AccountDialog(accInfo, launchGUI, false, accountNames);
            accDialog.pack();
            accDialog.setLocationRelativeTo(launchGUI);
            accDialog.show();

            if (accDialog.okClicked() && accDialog.getAccountChanged()) {
                accInfo = accDialog.getAccount();
                
                database.deleteAccount(accountName.trim());
                database.addAccount(accInfo);
                if (!accInfo.getAccountName().equals(accountName.trim())) {
                    if (accountName.trim().equals(database.getDbOptions().getAuthDBEntry())) {
                        database.getDbOptions().setAuthDBEntry(accInfo.getAccountName());
                    }
                    int i = accountNames.indexOf(accountName.trim());
                    accountNames.remove(i);
                    accountNames.add(accInfo.getAccountName());
                    filter();
                }
                saveDatabase();
            }
        }

    }


    public void filter() {
        String filterStr = launchGUI.getSearchField().getText().toLowerCase();

        ArrayList<String> filteredAccountsList = new ArrayList<String>();
        for (int i=0; i<accountNames.size(); i++) {
            String accountName = (String) accountNames.get(i);
            if (filterStr.equals("") || accountName.toLowerCase().indexOf(filterStr) != -1) {
                filteredAccountsList.add(accountName);
            }
        }

        populateListview(filteredAccountsList);

        //If there's only one item in the listview then select it
        if (launchGUI.getAccountsListview().getModel().getSize() == 1) {
            launchGUI.getAccountsListview().setSelectedIndex(0);
        }
    }

    // TODO list view with account Name -- Create a Label and add "listview"
    
    public void populateListview(ArrayList<String> accountNames) {

        SortedListModel listview = (SortedListModel) launchGUI.getAccountsListview().getModel();

        listview.clear();
        launchGUI.getAccountsListview().clearSelection();
        
        
        for (int i=0; i<accountNames.size(); i++) {	
//        	listview.addElement(new ImageIcon(Util.loadImage("padlock-16x16.png").getImage()));
        	listview.addElement("   "+accountNames.get(i));
        }

        setButtonState();           
     
    }


    public void setButtonState() {
        if (launchGUI.getAccountsListview().getSelectedValue() == null) {
            launchGUI.getEditAccountButton().setEnabled(false);
            launchGUI.getCopyUsernameButton().setEnabled(false);
            launchGUI.getCopyPasswordButton().setEnabled(false);
            launchGUI.getLaunchURLButton().setEnabled(false);
            launchGUI.getDeleteAccountButton().setEnabled(false);
            launchGUI.getEditAccountMenuItem().setEnabled(false);
            launchGUI.getCopyUsernameMenuItem().setEnabled(false);
            launchGUI.getCopyPasswordMenuItem().setEnabled(false);
            launchGUI.getLaunchURLMenuItem().setEnabled(false);
            launchGUI.getDeleteAccountMenuItem().setEnabled(false);
            launchGUI.getViewAccountMenuItem().setEnabled(false);
        } else {
            launchGUI.getEditAccountButton().setEnabled(true);
            launchGUI.getCopyUsernameButton().setEnabled(true);
            launchGUI.getCopyPasswordButton().setEnabled(true);
            launchGUI.getLaunchURLButton().setEnabled(true);
            launchGUI.getDeleteAccountButton().setEnabled(true);
            launchGUI.getEditAccountMenuItem().setEnabled(true);
            launchGUI.getCopyUsernameMenuItem().setEnabled(true);
            launchGUI.getCopyPasswordMenuItem().setEnabled(true);
            launchGUI.getLaunchURLMenuItem().setEnabled(true);
            launchGUI.getDeleteAccountMenuItem().setEnabled(true);
            launchGUI.getViewAccountMenuItem().setEnabled(true);
        }
    }


    @SuppressWarnings("deprecation")
	public void options() {
        OptionsDialog oppDialog = new OptionsDialog(launchGUI);
        oppDialog.pack();
        oppDialog.setLocationRelativeTo(launchGUI);
        oppDialog.show();

        configureAutoLock();

        if (oppDialog.hasLanguageChanged()) {
            launchGUI.initialiseControlsWithDefaultLanguage();
            if (database != null) {
                setStatusBarText(); // <--
            }
        }
    }

    public void showAbout() throws IOException {
    	LaunchBrowser.launcher("http://www.wagemaker.co.uk/?page_id=939");
    }

    public void resetSearch() {
        launchGUI.getSearchField().setText("");
    }

    public void reloadDatabase()
            throws InvalidPasswordException, ProblemReadingDatabaseFile, IOException {
        PasswordDatabase reloadedDb = null;
        try {
            reloadedDb = dbPers.load(database.getDatabaseFile());
        } catch (InvalidPasswordException e) {
            // The password for the reloaded database is different to that of
            // the open database
            boolean okClicked = false;
            do {
                char[] password = askUserForPassword(Translator.translate("enterDatabasePassword"));
                if (password == null) {
                    okClicked = false;
                } else {
                    okClicked = true;
                    try {
                        reloadedDb = dbPers.load(database.getDatabaseFile(), password);
                    } catch (InvalidPasswordException invalidPassword) {
                        JOptionPane.showMessageDialog(launchGUI, Translator.translate("incorrectPassword"));
                    } catch (CryptoException e1) {
                        errorHandler(e);
                    }
                }
            } while (okClicked && reloadedDb == null);
        }

        if (reloadedDb != null) {
            database = reloadedDb;
            doOpenDatabaseActions();
        }
    }

    public void reloadDatabaseBefore(ChangeDatabaseAction editAction)
            throws InvalidPasswordException, ProblemReadingDatabaseFile,
            IOException {
        boolean proceedWithAction = false;
        if (this.databaseNeedsReload) {
            int answer = JOptionPane.showConfirmDialog(launchGUI,
                    Translator.translate("askReloadDatabase"),
                    Translator.translate("reloadDatabase"),
                    JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                proceedWithAction = reloadDatabaseFromDisk();
            }
        } else {
            proceedWithAction = true;
        }

        if (proceedWithAction) {
            editAction.doAction();
        }
    }

    public boolean reloadDatabaseFromDisk() throws InvalidPasswordException,
            ProblemReadingDatabaseFile, IOException {
        boolean reloadSuccessful = false;

        PasswordDatabase reloadedDb = null;
        try {
            reloadedDb = dbPers.load(database.getDatabaseFile());
        } catch (InvalidPasswordException e) {
            // The password for the reloaded database is different to that of
            // the open database
            boolean okClicked = false;
            do {
                char[] password = askUserForPassword(Translator
                        .translate("enterDatabasePassword"));
                if (password == null) {
                    okClicked = false;
                } else {
                    okClicked = true;
                    try {
                        reloadedDb = dbPers.load(database.getDatabaseFile(),
                                password);
                    } catch (InvalidPasswordException invalidPassword) {
                        JOptionPane.showMessageDialog(launchGUI,
                                Translator.translate("incorrectPassword"));
                    } catch (CryptoException e1) {
                        errorHandler(e);
                    }
                }
            } while (okClicked && reloadedDb == null);
        }

        if (reloadedDb != null) {
            database = reloadedDb;
            doOpenDatabaseActions();
            reloadSuccessful = true;
        }

        return reloadSuccessful;
    }

    public void exitApplication() {
        System.exit(0);
    }


    public void export() {
        File exportFile = getSaveAsFile(Translator.translate("exportFile"));
        if (exportFile == null) {
            return;
        }

        if (exportFile.exists()) {
            exportFile.delete();
        }

        AccountsCSVMarshaller marshaller = new AccountsCSVMarshaller();
        try {
            marshaller.marshal(this.database.getAccounts(), exportFile);
        } catch (ExportException e) {
            JOptionPane.showMessageDialog(launchGUI, e.getMessage(), Translator.translate("problemExporting"), JOptionPane.ERROR_MESSAGE);
        }
    }


    public void importAccounts() throws DBException, ProblemReadingDatabaseFile, IOException, CryptoException, PasswordDatabaseException {
        if (getLatestVersionOfDatabase()) {
            // Prompt for the file to import
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle(Translator.translate("import"));
            int returnVal = fc.showOpenDialog(launchGUI);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File csvFile = fc.getSelectedFile();

                // Unmarshall the accounts from the CSV file
                try {
                    AccountsCSVMarshaller marshaller = new AccountsCSVMarshaller();
                    ArrayList<?> accountsInCSVFile = marshaller.unmarshal(csvFile);
                    ArrayList<AccountInformation> accountsToImport = new ArrayList<AccountInformation>();

                    boolean importCancelled = false;
                    // Add each account to the open database. If the account
                    // already exits the prompt to overwrite
                    for (int i=0; i<accountsInCSVFile.size(); i++) {
                        AccountInformation importedAccount = (AccountInformation) accountsInCSVFile.get(i);
                        if (database.getAccount(importedAccount.getAccountName()) != null) {
                            Object[] options = {"Overwrite Existing", "Keep Existing", "Cancel"};
                            int answer = JOptionPane.showOptionDialog(
                                    launchGUI,
                                    Translator.translate("importExistingQuestion", importedAccount.getAccountName()),
                                    Translator.translate("importExistingTitle"),
                                    JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    options,
                                    options[1]);

                            if (answer == 1) {
                                continue; // If keep existing then continue to the next iteration
                            } else if (answer == 2) {
                                importCancelled = true;
                                break; // Cancel the import
                            }
                        }

                        accountsToImport.add(importedAccount);
                    }

                    if (!importCancelled && accountsToImport.size() > 0) {
                        for (int i=0; i<accountsToImport.size(); i++) {
                            AccountInformation accountToImport = (AccountInformation) accountsToImport.get(i);
                            database.deleteAccount(accountToImport.getAccountName());
                            database.addAccount(accountToImport);
                        }
                        saveDatabase();
                        accountNames = getAccountNames();
                        filter();
                    }

                } catch (ImportException e) {
                    JOptionPane.showMessageDialog(launchGUI, e.getMessage(), Translator.translate("problemImporting"), JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(launchGUI, e.getMessage(), Translator.translate("problemImporting"), JOptionPane.ERROR_MESSAGE);
                } catch (CryptoException e) {
                    JOptionPane.showMessageDialog(launchGUI, e.getMessage(), Translator.translate("problemImporting"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private File getSaveAsFile(String title) {
    	
		UIManager.put("OptionPane.buttonOrientation", 0);		
		UIManager.put("OptionPane.buttonFont", FontTheme.size14b);
		UIManager.put("OptionPane.messageFont", FontTheme.size12p);
		UIManager.put("OptionPane.messageForeground",Values.WHITE);
		UIManager.put("Panel.background", Values.ORANGE);
		UIManager.put("OptionPane.background", Values.ORANGE);
		UIManager.put("Button.background", Values.ORANGE); 
    	
        File selectedFile;

        boolean gotValidFile = false;
        do {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle(title);
            int returnVal = fc.showSaveDialog(launchGUI);

            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return null;
            }

            selectedFile = new File(fc.getSelectedFile()+".db");

            //Warn the user if the database file already exists
            if (selectedFile.exists()) {
                Object[] options = {"Yes", "No"};
                int i = JOptionPane.showOptionDialog(launchGUI,
                        Translator.translate("fileAlreadyExistsWithFileName", selectedFile.getAbsolutePath()) + '\n' +
                            Translator.translate("overwrite"),
                            Translator.translate("fileAlreadyExists"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[1]);
                if (i == JOptionPane.YES_OPTION) {
                    gotValidFile = true;
                }
            } else {
                gotValidFile = true;
            }

        } while (!gotValidFile);

        return selectedFile;
    }


    private void saveDatabase() throws IOException, CryptoException {
    	dbPers.save(database);
        if (fileMonitor != null) {
            fileMonitor.start();
        }
        if (databaseHasRemoteInstance()) {
            setLocalDatabaseDirty(true);
        } else {
            setLocalDatabaseDirty(false);
        }
    }


    private void setLocalDatabaseDirty(boolean dirty) {
        localDatabaseDirty = dirty;
        setStatusBarText(); // <--
    }


    private void setStatusBarText() {
        String status = null;
        status = Translator.translate("localDatabase"); 
        launchGUI.getStatusBar().setText(database.getDatabaseFile() + " - " + status); //<--
        launchGUI.getStatusBar().setForeground(Values.WHITE);
    }


    private class AutoLockDatabaseListener implements WindowFocusListener {

        private String databaseClosedOnTimer;
        private Timer closeDBTimer;

        public synchronized void windowGainedFocus(WindowEvent we) {
            if (closeDBTimer != null) {
                LOG.debug("Stopping closeDBTimer");
                closeDBTimer.removeActionListener(
                        closeDBTimer.getActionListeners()[0]);
                closeDBTimer = null;
            }
            if (databaseClosedOnTimer != null) {
                try {
                    openDatabase(databaseClosedOnTimer);
                } catch (Exception e) {
                    errorHandler(e);
                }
                databaseClosedOnTimer = null;
            }
        }

        public synchronized void windowLostFocus(WindowEvent e) {
            // If the window receiving focus is within this application then the
            // app isn't not losing focus so no further action is required.
            if (e.getOppositeWindow() != null &&
                    e.getOppositeWindow().getOwner() == launchGUI) {
                LOG.debug("Focus switched to another window within this app");
                return;
            }

            if (database != null && closeDBTimer == null){
                closeDBTimer = new Timer(msToWaitBeforeClosingDB , null);
                closeDBTimer.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        LOG.debug("Closing database due to inactivity");
                        databaseClosedOnTimer = database.getDatabaseFile().getAbsolutePath();
                        doCloseDatabaseActions();
                        database = null;
                        closeDBTimer = null;
                    }
                });
                closeDBTimer.setRepeats(false);
                closeDBTimer.start();
                LOG.debug("Started lost focus timer, " + msToWaitBeforeClosingDB);
            }
        }

    }
}
