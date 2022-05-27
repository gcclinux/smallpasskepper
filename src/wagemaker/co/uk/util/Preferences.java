package wagemaker.co.uk.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Preferences {

    private static final Log log = LogFactory.getLog(Preferences.class);

    public class ApplicationOptions {
        public static final String DB_TO_LOAD_ON_STARTUP= "DBToLoadOnStartup";
        public static final String ACCOUNT_HIDE_PASSWORD="account.hidePassword";
        public static final String ACCOUNT_PASSWORD_LENGTH="account.passwordLenght";
        public static final String INCLUDE_ESCAPE_CHARACTERS="account.inclescapechars";
        public static final String ACCOUNT_PASSWORD_RETENTION = "account.passwordRetention";
        public static final String MAINWINDOW_ALWAYS_ON_TOP="mainwindow.alwaysontop";
        public static final String DATABASE_AUTO_LOCK="database.auto_lock";
        public static final String DATABASE_AUTO_LOCK_TIME = "database.auto_lock_time";
        public static final String REMEMBER_WINDOW_POSITION="window.store_position";
        public static final String XLOC = "window.location.x";
        public static final String YLOC = "window.location.y";
        public static final String WWIDTH = "window.width";
        public static final String WHEIGHT = "window.height";
        public static final String LOCALE="locale";
    }
    
    public class DatabaseOptions {
    }


    private static final String PREF_FILE = System.getProperty("user.home") 
    		+ System.getProperty("file.separator") 
    		+ ".smallpasskeeper"
    		+ System.getProperty("file.separator") 
    		+ "config.conf";
    private static final String PREF_FILE_SYS_PROP = "config.conf";
    private static Properties preferences;
    private static String propertiesFile;


    public static String get(String name, String defaultValue) {
        String retVal = preferences.getProperty(name, defaultValue);
        return retVal;
    }

    public static int getInt(String name, int defaultValue) {
        String cfgVal = preferences.getProperty(name);
        int retVal = defaultValue;
        if (cfgVal != null && Util.isNumeric(cfgVal)) {
            retVal = Integer.parseInt(cfgVal);
        }
        return retVal;
    }

    public static String get(String name) {
        return get(name, null);
    }


    public static void set(String name, String value) {
        preferences.setProperty(name, value);
    }


    public static void load() throws FileNotFoundException, IOException {

		if(!System.getProperties().containsKey(PREF_FILE_SYS_PROP)) {
			if(SystemOS.getOS() == "lin" || SystemOS.getOS() == "win") {
				String configBase = System.getenv("XDG_CONFIG_HOME");
				if(null == configBase || configBase.trim().equals("")) {
					configBase = System.getProperty("user.home") 
							+ System.getProperty("file.separator")
				    		+ ".smallpasskeeper";
				}
				System.setProperty(PREF_FILE_SYS_PROP, configBase 
						+ System.getProperty("file.separator") 
						+ "config.conf");
			}
		}

        propertiesFile = System.getProperty(PREF_FILE_SYS_PROP);
        if (propertiesFile == null || propertiesFile.trim().equals("")) {
            propertiesFile = PREF_FILE;
        }

		// Create propertiesFile directories if it doesn't exist
		File prefs = new File(propertiesFile);
		prefs.getParentFile().mkdirs();

        //Attempt to load the properties
        try {
            preferences = new Properties();
            preferences.load(new FileInputStream(propertiesFile));
        } catch (FileNotFoundException e) {
        }
    }


    public static void save() throws IOException  {
        if (log.isDebugEnabled()) {
            log.debug("Saving properties to the file [" + PREF_FILE + "]");
        }
        preferences.store(new FileOutputStream(propertiesFile), "SmallPassKepper Preferences");
    }

}
