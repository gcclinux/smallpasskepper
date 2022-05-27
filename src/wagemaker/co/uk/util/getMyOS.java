package wagemaker.co.uk.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


public class getMyOS {
	
	public static String checkOS = System.getProperty("os.name").toLowerCase();
	public static String osName = null;
	
	public static String getOsName() throws URISyntaxException, IOException {
		
		String path = new File(getMyOS.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).toString();
		File os_release = new File("/etc/os-release");
		FileInputStream languageFILE = null;		
		languageFILE = new FileInputStream(os_release);
		
		final ResourceBundle languageRes = new PropertyResourceBundle(languageFILE);
		
		if (checkOS.indexOf( "linux") >=0) {	
			if (os_release.exists()) {
				osName = languageRes.getString("NAME").toLowerCase().replaceAll("^\"|\"$", "");
				if (osName.indexOf("ubuntu") >= 0) {
					if (path.indexOf("/snap/") >= 0) {
						osName = "snap";
					} 
				} else {
					String singleName[] = languageRes.getString("NAME").toLowerCase().replaceAll("^\"|\"$", "").toString().split(" ", 2);
					osName = singleName[0];
				}
			} 
		} else if (checkOS.indexOf( "win" ) >= 0) {
			osName = checkOS;
		}
		return osName;
	}
}
