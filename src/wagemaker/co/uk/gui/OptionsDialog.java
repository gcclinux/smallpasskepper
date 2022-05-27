package wagemaker.co.uk.gui;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import wagemaker.co.uk.util.Preferences;
import wagemaker.co.uk.util.Translator;
import wagemaker.co.uk.util.Util;
import wagemaker.co.uk.util.Values;

public class OptionsDialog extends EscapeDialog {

	private static final long serialVersionUID = 1L;

	private JTextField dbToLoadOnStartup;
	private JCheckBox hideAccountPasswordCheckbox;
	private JCheckBox inclEscCharstoPassCheckbox;
	private JCheckBox storeWindowPosCheckbox;
	private JCheckBox appAlwaysonTopCheckbox;
	private JLabel accountPasswordLengthLabel;
	private JTextField accountPasswordLength;
	private JCheckBox databaseAutoLockCheckbox;
	private JTextField databaseAutoLockTime;
	private JComboBox<?> localeComboBox;
	private boolean okClicked = false;
	private JFrame parentFrame;
	private boolean languageChanged;
	
	private JLabel passwordRetentionL;
	private JTextField passwordRetentionT;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public OptionsDialog(JFrame frame) {
		super(frame, Translator.translate("options"), true);
		
		Container container = getContentPane();
		
		// Create a pane with an empty border for spacing
		Border emptyBorder = BorderFactory.createEmptyBorder(2, 5, 5, 5);
		JPanel emptyBorderPanel = new JPanel();
		emptyBorderPanel.setLayout(new BoxLayout(emptyBorderPanel, BoxLayout.Y_AXIS));
		emptyBorderPanel.setBorder(emptyBorder);
		container.add(emptyBorderPanel);

		Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Border etchedTitleBorder = BorderFactory.createTitledBorder(etchedBorder,' ' + Translator.translate("general") + ' ');
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(etchedTitleBorder);
		mainPanel.setBackground(Values.ORANGE);
		emptyBorderPanel.add(mainPanel);
		

		GridBagConstraints c = new GridBagConstraints();
		
		// The "Database to Load on Startup" row
		JLabel urlLabel = new JLabel(Translator.translate("dbToLoadOnStartup"));
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 5, 3, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.NONE;
		mainPanel.add(urlLabel, c);

		// The "Database to Load on Startup" input field row
		dbToLoadOnStartup = new JTextField(Preferences.get(Preferences.ApplicationOptions.DB_TO_LOAD_ON_STARTUP), 25);
		dbToLoadOnStartup.setHorizontalAlignment(JTextField.LEFT);
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 5, 5, 5);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(dbToLoadOnStartup, c);

		JButton dbToLoadOnStartupButton = new JButton("...");
		dbToLoadOnStartupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getDBToLoadOnStartup();
			}
		});
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.insets = new Insets(0, 0, 5, 5);
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		mainPanel.add(dbToLoadOnStartupButton, c);

		// The "Language" label row
		JLabel localeLabel = new JLabel(Translator.translate("language"));
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 5, 3, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.NONE;
		mainPanel.add(localeLabel, c);

		// The "Locale" field row
		localeComboBox = new JComboBox(getSupportedLocaleNames());
		for (int i = 0; i < localeComboBox.getItemCount(); i++) {
			// If the locale language is blank then set it to the English
			// language
			// I'm not sure why this happens. Maybe it's because the default
			// locale
			// is English???
			String currentLanguage = Translator.getCurrentLocale().getLanguage();
			if (currentLanguage.equals("")) {
				currentLanguage = "en";
			}

			if (currentLanguage.equals(Translator.SUPPORTED_LOCALES[i].getLanguage())) {
				localeComboBox.setSelectedIndex(i);
				break;
			}
		}
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 5, 8, 5);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(localeComboBox, c);

		// The "Hide account password" row
		Boolean hideAccountPassword = new Boolean(
				Preferences.get(Preferences.ApplicationOptions.ACCOUNT_HIDE_PASSWORD, "true"));
		hideAccountPasswordCheckbox = new JCheckBox(Translator.translate("hideAccountPassword"),
				hideAccountPassword.booleanValue());
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 2, 5, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		hideAccountPasswordCheckbox.setBackground(Values.ORANGE);
		mainPanel.add(hideAccountPasswordCheckbox, c);

		// The "Database auto lock" row
		Boolean databaseAutoLock = new Boolean(Preferences.get(Preferences.ApplicationOptions.DATABASE_AUTO_LOCK, "false"));
		databaseAutoLockCheckbox = new JCheckBox(Translator.translate("databaseAutoLock"),databaseAutoLock.booleanValue());
		c.gridx = 0;
		c.gridy = 5;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 2, 5, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		mainPanel.add(databaseAutoLockCheckbox, c);
		databaseAutoLockCheckbox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				databaseAutoLockTime.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		// The "Database auto lock" field row
		databaseAutoLockTime = new JTextField(Preferences.get(Preferences.ApplicationOptions.DATABASE_AUTO_LOCK_TIME),5);
		c.gridx = 1;
		c.gridy = 5;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 5, 5, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		databaseAutoLockCheckbox.setBackground(Values.ORANGE);
		mainPanel.add(databaseAutoLockTime, c);
		databaseAutoLockTime.setEnabled(databaseAutoLockCheckbox.isSelected());

		// The "Generated password length" row
		accountPasswordLengthLabel = new JLabel(Translator.translate("generatedPasswodLength"));
		c.gridx = 0;
		c.gridy = 6;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 2, 5, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		mainPanel.add(accountPasswordLengthLabel, c);

		accountPasswordLength = new JTextField(Preferences.get(Preferences.ApplicationOptions.ACCOUNT_PASSWORD_LENGTH, "8"), 5);
		c.gridx = 1;
		c.gridy = 6;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 5, 5, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(accountPasswordLength, c);
		
		
		// The "Password retention" row //TODO
//		private JLabel passwordRetentionL;
//		private JTextField passwordRetentionT;
		
		passwordRetentionL = new JLabel(Translator.translate("passwordRetention"));
		c.gridx = 0;
		c.gridy = 7;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 2, 5, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		mainPanel.add(passwordRetentionL, c);

		passwordRetentionT = new JTextField(Preferences.get(Preferences.ApplicationOptions.ACCOUNT_PASSWORD_RETENTION, "30"), 5);
		c.gridx = 1;
		c.gridy = 7;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 5, 5, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(passwordRetentionT, c);

		// The "Include Escape Characters to Generated Passwords" row
		Boolean inclEscCharstoPass = new Boolean(
				Preferences.get(Preferences.ApplicationOptions.INCLUDE_ESCAPE_CHARACTERS, "true"));
		inclEscCharstoPassCheckbox = new JCheckBox((Translator.translate("includePunctuationCharacters")),
				inclEscCharstoPass.booleanValue());
		c.gridx = 0;
		c.gridy = 8;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 2, 5, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		inclEscCharstoPassCheckbox.setBackground(Values.ORANGE);
		mainPanel.add(inclEscCharstoPassCheckbox, c);

		// The "Store Window position" row
		Boolean storeWindowPos = Boolean
				.valueOf(Preferences.get(Preferences.ApplicationOptions.REMEMBER_WINDOW_POSITION, "false"));
		storeWindowPosCheckbox = new JCheckBox((Translator.translate("storeWindowPosition")),
				storeWindowPos.booleanValue());
		c.gridx = 0;
		c.gridy = 9;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 2, 5, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		storeWindowPosCheckbox.setBackground(Values.ORANGE);
		mainPanel.add(storeWindowPosCheckbox, c);

		// The "Application always on top" row
		Boolean appAlwaysonTop = new Boolean(
				Preferences.get(Preferences.ApplicationOptions.MAINWINDOW_ALWAYS_ON_TOP, "false"));
		appAlwaysonTopCheckbox = new JCheckBox((Translator.translate("applicationAlwaysonTop")),
				appAlwaysonTop.booleanValue());
		c.gridx = 0;
		c.gridy = 10;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 2, 5, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		appAlwaysonTopCheckbox.setBackground(Values.ORANGE);
		mainPanel.add(appAlwaysonTopCheckbox, c);

		// Some spacing
		emptyBorderPanel.add(Box.createVerticalGlue());

		// The buttons row
		JPanel buttonPanel = new JPanel(new FlowLayout());
		emptyBorderPanel.add(buttonPanel);
		JButton okButton = new JButton(Translator.translate("ok"));
		// Link Enter key to okButton
		getRootPane().setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButtonAction();
			}
		});
		buttonPanel.add(okButton);

		JButton cancelButton = new JButton(Translator.translate("cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		buttonPanel.add(cancelButton);
	}

	public boolean okClicked() {
		return okClicked;
	}

	private void okButtonAction() {
		try {
			if (databaseAutoLockCheckbox.isSelected()) {
				if (databaseAutoLockTime.getText() == null || databaseAutoLockTime.getText().trim().equals("")
						|| !Util.isNumeric(databaseAutoLockTime.getText())) {
					JOptionPane.showMessageDialog(OptionsDialog.this,
							Translator.translate("invalidValueForDatabaseAutoLockTime"),
							Translator.translate("problem"), JOptionPane.ERROR_MESSAGE);
					databaseAutoLockTime.requestFocusInWindow();
					return;
				}
			}

			if (accountPasswordLength.getText() == null || accountPasswordLength.getText().trim().equals("")
					|| !Util.isNumeric(accountPasswordLength.getText())) {
				JOptionPane.showMessageDialog(OptionsDialog.this,
						Translator.translate("invalidValueForAccountPasswordLength"), Translator.translate("problem"),
						JOptionPane.ERROR_MESSAGE);
				databaseAutoLockTime.requestFocusInWindow();
				return;
			}

			Preferences.set(Preferences.ApplicationOptions.DB_TO_LOAD_ON_STARTUP, dbToLoadOnStartup.getText());
			Preferences.set(Preferences.ApplicationOptions.ACCOUNT_HIDE_PASSWORD,String.valueOf(hideAccountPasswordCheckbox.isSelected()));
			Preferences.set(Preferences.ApplicationOptions.INCLUDE_ESCAPE_CHARACTERS,String.valueOf(inclEscCharstoPassCheckbox.isSelected()));
			Preferences.set(Preferences.ApplicationOptions.REMEMBER_WINDOW_POSITION,String.valueOf(storeWindowPosCheckbox.isSelected()));
			Preferences.set(Preferences.ApplicationOptions.MAINWINDOW_ALWAYS_ON_TOP,String.valueOf(appAlwaysonTopCheckbox.isSelected()));
			Preferences.set(Preferences.ApplicationOptions.DATABASE_AUTO_LOCK,String.valueOf(databaseAutoLockCheckbox.isSelected()));
			Preferences.set(Preferences.ApplicationOptions.DATABASE_AUTO_LOCK_TIME, databaseAutoLockTime.getText());
			Preferences.set(Preferences.ApplicationOptions.ACCOUNT_PASSWORD_LENGTH, accountPasswordLength.getText());
			Preferences.set(Preferences.ApplicationOptions.ACCOUNT_PASSWORD_RETENTION, passwordRetentionT.getText());

			LaunchGUI.setAppAlwaysonTop(appAlwaysonTopCheckbox.isSelected());

			// Save the new language and set a flag if it has changed
			String beforeLocale = Preferences.get(Preferences.ApplicationOptions.LOCALE);
			Locale selectedLocale = Translator.SUPPORTED_LOCALES[localeComboBox.getSelectedIndex()];
			String afterLocale = selectedLocale.getLanguage();
			if (!afterLocale.equals(beforeLocale)) {
				Preferences.set(Preferences.ApplicationOptions.LOCALE, selectedLocale.getLanguage());
				Translator.loadBundle(selectedLocale);
				languageChanged = true;
			}

			Preferences.save();
			setVisible(false);
			dispose();
			okClicked = true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parentFrame, e.getStackTrace(), Translator.translate("error"),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void getDBToLoadOnStartup() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle(Translator.translate("dbToOpenOnStartup"));
		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File databaseFile = fc.getSelectedFile();
			dbToLoadOnStartup.setText(databaseFile.getAbsoluteFile().toString());
		}
	}

	private Object[] getSupportedLocaleNames() {
		Object[] names = new Object[Translator.SUPPORTED_LOCALES.length];

		for (int i = 0; i < Translator.SUPPORTED_LOCALES.length; i++) {
			names[i] = Translator.SUPPORTED_LOCALES[i].getDisplayLanguage(Translator.getCurrentLocale()) + " ("
					+ Translator.SUPPORTED_LOCALES[i].getDisplayLanguage(Translator.SUPPORTED_LOCALES[i]) + ')';
		}

		return names;
	}

	public boolean hasLanguageChanged() {
		return languageChanged;
	}

}