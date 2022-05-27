package wagemaker.co.uk.util;

import java.io.File;

public class LaunchBrowser {

	  @SuppressWarnings("unused")
	public static void launcher(String address)
	  {
		String url = address;
		String os = System.getProperty("os.name").toLowerCase();
	    Runtime rt = Runtime.getRuntime();

		try{

		    if (os.indexOf( "win" ) >= 0) {
		    	
		        rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);
		        
		    } else if (os.indexOf( "mac" ) >= 0) {

		        rt.exec( "open " + url);

	        } else if (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0) {
	        	
	        	String path = new File(getMyOS.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).toString();
      			String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror", "netscape", "opera", "links", "lynx", "google-chrome", "chromium-browser", "chromium"};
      			File xdg = new File("/usr/bin/xdg-open");	
      			   			
				if (path.indexOf("/snap/") >= 0) {
//					System.out.println("Launching: xdg-open "+url);
					rt.exec("xdg-open "+ url);
					return;
				} else {
	    			  if (getMyOS.getOsName().equals("ubuntu")) {
	    				  if (xdg.canExecute()) {	
//	    					  System.out.println("Launching: xdg-open "+url);
	    					  rt.exec(xdg+" "+ url);
	    					  return;
	    				  } else {
//	    					  System.out.println("generic");
	    				        StringBuffer cmd = new StringBuffer();
	    				        for (int i=0; i<browsers.length; i++) {
	    				            cmd.append( (i==0  ? "" : " || " ) + browsers[i] +" \"" + url + "\" ");
	    				        	rt.exec(new String[] { "sh", "-c", cmd.toString() });
	    				        	return;
	    				        }
	    				  	}
	    			  	} 
					}  
      		  } else {
	                return;
	           }
	       } catch (Exception e){
		    return;
	       }
	      return;
	}
}