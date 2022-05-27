package wagemaker.co.uk.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SystemOS {

	private static String CheckOS = System.getProperty("os.name").toLowerCase();
	private static String OS;
	private static String Hostname;
	
	public static String getHostname() {
		try {
			Hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return Hostname;
	}
	
	public static String getOS() {
		if (OS == null) {
			setOS();
		}
		return OS;
	}
	
	public static void setOS() {
		if (isWindowsXP()) {
			OS = "winxp";
		} else if (isWindows7()) {
			OS = "win7";
		} else if (isWindows8()) {
			OS = "win8";
		} else if (isWindows81()) {
			OS = "win8";
		} else if (isWindows10()) {
			OS = "win10";
		} else if (isMac()) {
			OS = "mac";
		} else if (isUnix()) {
			OS = "linux";
		} else if (isSolaris()) {
			OS = "solaris";
		} else {
			OS = "unknown";
		}
	}
	
	private static boolean isWindowsXP() {
		return (CheckOS.equals("windows xp"));
	}
	private static boolean isWindows7() {
		return (CheckOS.equals("windows 7"));
	}
	private static boolean isWindows8() {
		return (CheckOS.equals("windows 8"));
	}
	private static boolean isWindows81() {
		return (CheckOS.equals("windows 8"));
	}
	private static boolean isWindows10() {
		return (CheckOS.equals("windows 10"));
	}
	private static boolean isMac() {
		return (CheckOS.indexOf("mac") >= 0);
	}
	private static boolean isUnix() {
		return (CheckOS.indexOf("nix") >= 0 || CheckOS.indexOf("nux") >= 0 || CheckOS.indexOf("aix") >= 0);
	}
	private static boolean isSolaris() {
		return (CheckOS.indexOf("sunos") >= 0);
	}
}
