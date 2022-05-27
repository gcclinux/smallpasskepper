package wagemaker.co.uk.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


public class Translator {

    public static Locale[] SUPPORTED_LOCALES = {Locale.ENGLISH, new Locale("nl")};
	
//	public static Locale[] SUPPORTED_LOCALES = {Locale.ENGLISH};
        
    private static ResourceBundle resourceBundle;
    private static MessageFormat formatter;


    public static String translate(String messageName, Object[] params) {
        formatter.applyPattern(resourceBundle.getString(messageName));
        return formatter.format(params);
    }


    public static String translate(String messageName, Object param) {
        formatter.applyPattern(resourceBundle.getString(messageName));
        return formatter.format(new Object[] {param});
    }


    public static String translate(String messageName) {
        return resourceBundle.getString(messageName);
    }


    public static Locale getCurrentLocale() {
        return resourceBundle.getLocale();
    }


    public static void initialise() {
        Locale locale = new Locale("en");
        String localePreference = Preferences.get(Preferences.ApplicationOptions.LOCALE);
        if (localePreference != null) {
            locale = new Locale(localePreference);
        }
        loadBundle(locale);
    }


    public static void loadBundle(Locale locale) {
        resourceBundle = ResourceBundle.getBundle("lang/file", locale);
        formatter = new MessageFormat("");
        formatter.setLocale(locale);
    }

}
