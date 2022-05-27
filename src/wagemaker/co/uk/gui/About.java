package wagemaker.co.uk.gui;

/******************************************************************************************************
 * @author by Ricardo Wagemaker (["java"] + "@" + "wagemaker.co.uk") 2010-2020
 * @title SmallPassKeeper
 * @version 1.0.0
 * @since   2010 - 2020
 * @License MIT
 ******************************************************************************************************/

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

import wagemaker.co.uk.util.*;

public class About {
	@SuppressWarnings({ "static-access", "unchecked", "rawtypes" })
	public static void Main() {
		
		UIManager UI=new UIManager();
		UI.put("OptionPane.buttonOrientation", 0);		
		UI.put("OptionPane.buttonFont", FontTheme.size15b);
		UI.put("OptionPane.messageFont", FontTheme.size15i);
		UI.put("OptionPane.messageForeground", Values.BLACK);
		UI.put("Panel.background",  Values.ORANGE);
		UI.put("OptionPane.background", Values.ORANGE);
		
		JFrame About = new JFrame();
		About.setLayout(null);
		About.setTitle("About");
		About.setSize(350, 350);
		About.setResizable(false);
		About.setLocationRelativeTo(null); 
		About.setUndecorated(true);
		About.setShape(new RoundRectangle2D.Double(3,0, 300,300, 30,30));
		About.setAlwaysOnTop (true);
		About.setVisible(true);
		
		JLabel label = new JLabel(Values.TITLE, JLabel.CENTER);
		label.setFont(FontTheme.size20b);
		label.setForeground(Values.WHITE);
        label.setSize(300, 30);
        label.setLocation(0, 15);
        
		JLabel label2 = new JLabel(Values.BUILD, JLabel.CENTER);
		label2.setFont(FontTheme.size15i);
		label2.setForeground(Values.WHITE);
        label2.setSize(300, 30);
        label2.setLocation(0, 40);
               
		JLabel label4 = new JLabel(Values.DEVELOPER, JLabel.CENTER);
		label4.setFont(FontTheme.size15i);
		label4.setForeground(Values.WHITE);
        label4.setSize(300, 30);
        label4.setLocation(0, 60);
        
		JLabel label5 = new JLabel("Java", JLabel.CENTER);
		label5.setFont(FontTheme.size20b);
		label5.setForeground(Values.WHITE);
        label5.setSize(300, 30);
        label5.setLocation(0,95);
        
		JLabel label6 = new JLabel(System.getProperty("java.version"), JLabel.CENTER);
		label6.setFont(FontTheme.size15i);
		label6.setForeground(Values.WHITE);
        label6.setSize(300, 30);
        label6.setLocation(0,120);
        
		JLabel label3 = new JLabel("SmallTextPad is under", JLabel.CENTER);
		label3.setFont(FontTheme.size15i);
		label3.setForeground(Values.WHITE);
        label3.setSize(300, 30);
        label3.setLocation(0, 155);
        
		JLabel label7 = new JLabel("The MIT License “Agreement”", JLabel.CENTER);
		label7.setFont(FontTheme.size15i);
		label7.setForeground(Values.WHITE);
        label7.setSize(300, 30);
        label7.setLocation(0, 180);
        
        JLabel support = new JLabel("Website", JLabel.CENTER);
        support.setFont(FontTheme.size17p);
        support.setForeground(Values.WHITE);
        support.setSize(300, 30);
        support.setLocation(0, 220);
        
		JButton b=new JButton("Quit");
		//b.setRolloverEnabled(false);
		b.setFocusable(false);
		//b.setContentAreaFilled(false);
		b.setFont(FontTheme.size17p);
		b.setForeground(Values.WHITE);
		b.setSize(90, 30);
		b.setLocation(105, 260);

		b.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
				int modifiers = mouseEvent.getModifiersEx();
				if ((modifiers & InputEvent.BUTTON1_DOWN_MASK ) == InputEvent.BUTTON1_DOWN_MASK) {
					b.setBackground(Color.GREEN);
				}

			}
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				b.setBackground(Color.RED);
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				b.setBackground(Values.ORANGE);
			}
		});
 
        Font font = support.getFont();
        Map attributes = font.getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        support.setFont(font.deriveFont(attributes));
	        
	        support.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	        support.addMouseListener(new MouseAdapter() {
	          public void mouseClicked(MouseEvent e) {
	             if (e.getClickCount() > 0) {
	            	 
	        		  LaunchBrowser.launcher(Values.remoteLicense);
	        		  
	             }
	          }
	       });

		b.addActionListener(e -> About.dispose());

		About.add(label);
		About.add(label2);
		About.add(label4);
		About.add(label5);
		About.add(label6);
		About.add(label3);
		About.add(label7);
		About.add(support);
		About.add(b);

		// Adding Application ICON
		 
		 ImageIcon ImageIcon = new ImageIcon(Util.loadImage("gcclinux.png").getImage());
		 Image Logo = ImageIcon.getImage();
		 About.setIconImage(Logo);
 
	}

}