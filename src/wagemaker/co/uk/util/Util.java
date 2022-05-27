package wagemaker.co.uk.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import javax.swing.ImageIcon;


public class Util {

    public static String lpad(int i, int length, char c) {
        StringBuffer buf = new StringBuffer(String.valueOf(i));
        while (buf.length() < length) {
            buf.insert(0, c);
        }
        return buf.toString();
    }

    public static ImageIcon loadImage(String name) {
        URL imageURL = Util.class.getResource("/images/" + name);
        return new ImageIcon(imageURL);
    }


    @SuppressWarnings("resource")
	public static void copyFile(File srcFile, File destFile) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destinationChannel = null;
        try {
            sourceChannel = new FileInputStream(srcFile).getChannel();
            destinationChannel = new FileOutputStream(destFile).getChannel();
            destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }
            if (destinationChannel != null) {
                destinationChannel.close();
            }
        }
    }

    public static Charset defaultCharset() {
        return Charset.forName(
                new OutputStreamWriter(
                        new ByteArrayOutputStream()).getEncoding());
    }

    public static boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
      }

}
