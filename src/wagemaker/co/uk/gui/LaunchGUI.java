package wagemaker.co.uk.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.validator.routines.UrlValidator;

import wagemaker.co.uk.util.LaunchBrowser;
import wagemaker.co.uk.crypto.InvalidPasswordException;
import wagemaker.co.uk.db.AccountInformation;
import wagemaker.co.uk.db.ProblemReadingDatabaseFile;
import wagemaker.co.uk.util.FontTheme;
import wagemaker.co.uk.util.Preferences;
import wagemaker.co.uk.util.Translator;
import wagemaker.co.uk.util.Util;
import wagemaker.co.uk.util.Values;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Boolean.parseBoolean;

public class LaunchGUI extends JFrame implements ActionListener {

	public static final long serialVersionUID = 8136879593106151785L;
	private static final String applicationName = Values.TITLE;

	public static final String NEW_DATABASE_TXT = "newDatabaseMenuItem";
	public static final String OPEN_DATABASE_TXT = "openDatabaseMenuItem";
	public static final String CHANGE_MASTER_PASSWORD_TXT = "changeMasterPasswordMenuItem";
	public static final String ADD_ACCOUNT_TXT = "addAccountMenuItem";
	public static final String EDIT_ACCOUNT_TXT = "editAccountMenuItem";
	public static final String DELETE_ACCOUNT_TXT = "deleteAccountMenuItem";
	public static final String VIEW_ACCOUNT_TXT = "viewAccountMenuItem";
	public static final String COPY_USERNAME_TXT = "copyUsernameMenuItem";
	public static final String COPY_PASSWORD_TXT = "copyPasswordMenuItem";
	public static final String LAUNCH_URL_TXT = "launchURLMenuItem";
	public static final String OPTIONS_TXT = "optionsMenuItem";
	public static final String ABOUT_TXT = "aboutMenuItem";
	public static final String RESET_SEARCH_TXT = "resetSearchMenuItem";
	public static final String EXIT_TXT = "exitMenuItem";
	public static final String EXPORT_TXT = "exportMenuItem";
	public static final String IMPORT_TXT = "importMenuItem";

	private JButton addAccountButton;
	private JButton editAccountButton;
	private JButton deleteAccountButton;
	private JButton copyUsernameButton;
	private JButton copyPasswordButton;
	private JButton launchURLButton;
	private JButton optionsButton;
	private JTextField searchField;
	private JButton resetSearchButton;
	private JLabel searchIcon;

	private JMenu databaseMenu;
	private JMenuItem newDatabaseMenuItem;
	private JMenuItem openDatabaseMenuItem;
	private JMenuItem changeMasterPasswordMenuItem;
	private JMenuItem exitMenuItem;
	private final ThreadLocal<JMenu> helpMenu = new ThreadLocal<>();
	private JMenuItem aboutMenuItem;
	private JMenu accountMenu;
	private JMenuItem addAccountMenuItem;
	private JMenuItem editAccountMenuItem;
	private JMenuItem deleteAccountMenuItem;
	private JMenuItem viewAccountMenuItem;
	private JMenuItem copyUsernameMenuItem;
	private JMenuItem copyPasswordMenuItem;
	private JMenuItem launchURLMenuItem;
	private JMenuItem exportMenuItem;
	private JMenuItem importMenuItem;

	private JList<?> accountsListview;
	final private JLabel statusBar = new JLabel(" ");
	private JPanel databaseFileChangedPanel;
	public static LaunchGUI AppWindow;

	final private DatabaseActions dbActions;

	public LaunchGUI(String title) {
		super(title);
		
		setIconImage(Util.loadImage("padlock-orange.png").getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dbActions = new DatabaseActions(this);

		// Set up the content pane.
		addComponentsToPane();

		// Add listener to store current position and size on closing
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				storeWindowBounds();
				try {
					Preferences.save();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

		});

		// Display the window.
		pack();
		setLocationRelativeTo(null);
		
		boolean restore = Preferences.get(Preferences.ApplicationOptions.REMEMBER_WINDOW_POSITION, "true").equals("true");
		if (restore) {
			restoreWindowBounds();
		} else {
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			double dW = (dim.width);
			double dH = (dim.height);
			double w = (dW / 2);
			double h = (dH / 2);
	        double x = (dim.width-w)/ 2;
	        double y = (dim.height-h)/ 2;
	        this.setBounds((int)x, (int)y, (int) (w), (int) (h));
		}

		boolean appAlwaysonTop = parseBoolean(Preferences.get(Preferences.ApplicationOptions.MAINWINDOW_ALWAYS_ON_TOP, "false"));
		setAlwaysOnTop(appAlwaysonTop);
		setVisible(true);

		try {
			// Load the startup database if it's configured
			String db = Preferences.get(Preferences.ApplicationOptions.DB_TO_LOAD_ON_STARTUP);
			if (db != null && !db.equals("")) {
				File dbFile = new File(db);
				if (!dbFile.exists()) {
					Preferences.set(Preferences.ApplicationOptions.DB_TO_LOAD_ON_STARTUP, "");
					dbActions.errorHandler(new Exception(Translator.translate("dbDoesNotExist", db)));
				} else {
					dbActions.openDatabase(db);
				}
			}
		} catch (Exception ignored) {}
		searchField.requestFocusInWindow();
	}

	public static void setAppAlwaysonTop(boolean val) {
		AppWindow.setAlwaysOnTop(val);
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(() -> {

			try {
				// Use the System look and feel
				Preferences.load();
				Translator.initialise();
					UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
					AppWindow = new LaunchGUI(applicationName);

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void addComponentsToPane() {

		// Ensure the layout manager is a BorderLayout
		if (!(getContentPane().getLayout() instanceof GridBagLayout)) {
			getContentPane().setLayout(new GridBagLayout());
		}

		// Create the menubar
		setJMenuBar(createMenuBar());

		GridBagConstraints c = new GridBagConstraints();

		// The toolbar Row
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(0, 0, 0, 0);
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		Component toolbar = createToolBar();
		toolbar.setBackground(Values.ORANGE);
		getContentPane().add(toolbar, c);

		// Keep the frame background color consistent
		getContentPane().setBackground(toolbar.getBackground());

		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 0, 0, 0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(new JSeparator(), c);

		// The search field row
		searchIcon = new JLabel(Util.loadImage("search.png"));
		searchIcon.setDisabledIcon(Util.loadImage("search_d.png"));
		searchIcon.setEnabled(false);
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(5, 1, 5, 1);
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		getContentPane().add(searchIcon, c);

		searchField = new JTextField(30);
		searchField.setEnabled(false);
		searchField.setMinimumSize(searchField.getPreferredSize());
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				// This method never seems to be called
			}

			public void insertUpdate(DocumentEvent e) {
				dbActions.filter();
			}

			public void removeUpdate(DocumentEvent e) {
				dbActions.filter();
			}
		});
		searchField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					dbActions.resetSearch();
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (accountsListview.getModel().getSize() == 1) {
						viewAccountMenuItem.doClick();
					}
				}
			}
		});
		
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(5, 1, 5, 1);
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		getContentPane().add(searchField, c);

		resetSearchButton = new JButton(Util.loadImage("stop.png"));
		resetSearchButton.setDisabledIcon(Util.loadImage("stop_d.png"));
		resetSearchButton.setEnabled(false);
		resetSearchButton.setToolTipText(Translator.translate(RESET_SEARCH_TXT));
		resetSearchButton.setActionCommand(RESET_SEARCH_TXT);
		resetSearchButton.addActionListener(this);
		resetSearchButton.setBorder(BorderFactory.createEmptyBorder());
		resetSearchButton.setFocusable(false);
		resetSearchButton.setBackground(Values.ORANGE);
		c.gridx = 2;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(5, 1, 5, 1);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		getContentPane().add(resetSearchButton, c);

		// The accounts listview row
		accountsListview = new JList<>();
		accountsListview.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		accountsListview.setSelectedIndex(0);
		accountsListview.setVisibleRowCount(10);
//		accountsListview.setForeground(Values.ORANGE); // ADDED
		accountsListview.setFont(FontTheme.size17p); // ADDED
		accountsListview.setModel(new SortedListModel());
		JScrollPane accountsScrollList = 
				new JScrollPane(accountsListview, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		accountsListview.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				if (accountsListview.getModel().getSize() > 0 && accountsListview.getSelectedIndex() == -1) {					
					accountsListview.setSelectionInterval(0, 0);
				}
			}
		});
		accountsListview.addListSelectionListener(e -> dbActions.setButtonState());
		accountsListview.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					//System.out.println("TEST "+dbActions.getSelectedAccount().getAccountName());
					viewAccountMenuItem.doClick();
				}
			}
		});
		accountsListview.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					viewAccountMenuItem.doClick();
				}
			}
		});

		accountsListview.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {

					try {
						dbActions.reloadDatabaseBefore(new DeleteAccountAction());
					} catch (InvalidPasswordException | ProblemReadingDatabaseFile | IOException e1) {
						e1.printStackTrace();
					}

				}
			}
		});

		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0, 1, 1, 1);
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		getContentPane().add(accountsScrollList, c);

		// The "File Changed" panel
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0, 1, 0, 1);
		c.ipadx = 3;
		c.ipady = 3;
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		databaseFileChangedPanel = new JPanel();
		databaseFileChangedPanel.setLayout(new BoxLayout(databaseFileChangedPanel, BoxLayout.X_AXIS));
		databaseFileChangedPanel.setBackground(new Color(249, 172, 60));
		databaseFileChangedPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JLabel fileChangedLabel = new JLabel("Database file changed");
		fileChangedLabel.setAlignmentX(LEFT_ALIGNMENT);
		databaseFileChangedPanel.add(fileChangedLabel);
		databaseFileChangedPanel.add(Box.createHorizontalGlue());
		JButton reloadButton = new JButton("Reload");
		reloadButton.addActionListener(e -> {
			try {
				dbActions.reloadDatabaseFromDisk();
			} catch (Exception ex) {
				dbActions.errorHandler(ex);
			}
		});
		databaseFileChangedPanel.add(reloadButton);
		databaseFileChangedPanel.setVisible(false);
		getContentPane().add(databaseFileChangedPanel, c);

		// Add the statusbar
		c.gridx = 0;
		c.gridy = 5;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0, 1, 1, 1);
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(statusBar, c);

	}

	public void setFileChangedPanelVisible(boolean visible) {
		databaseFileChangedPanel.setVisible(visible);
	}

	private JToolBar createToolBar() {
		
		UIManager.put("OptionPane.buttonOrientation", 0);		
		UIManager.put("OptionPane.buttonFont", FontTheme.size14b);
		UIManager.put("OptionPane.messageFont", FontTheme.size12p);
		UIManager.put("OptionPane.messageForeground",Values.WHITE);
		UIManager.put("Panel.background", Values.ORANGE);
		UIManager.put("OptionPane.background", Values.ORANGE);
		UIManager.put("Button.background", Values.ORANGE); 
		

		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setOpaque(true);
		toolbar.setBackground(Values.ORANGE);

		// The "Add Account" button
		addAccountButton = new JButton();
		addAccountButton.setOpaque(true);
		addAccountButton.setBackground(Values.ORANGE);
		addAccountButton.setToolTipText(Translator.translate(ADD_ACCOUNT_TXT));
		addAccountButton.setIcon(Util.loadImage("add_account.png"));
		addAccountButton.setDisabledIcon(Util.loadImage("add_account_d.png"));
		addAccountButton.addActionListener(this);
		addAccountButton.setEnabled(false);
		addAccountButton.setActionCommand(ADD_ACCOUNT_TXT);
		toolbar.add(addAccountButton);

		// The "Edit Account" button
		editAccountButton = new JButton();
		editAccountButton.setToolTipText(Translator.translate(EDIT_ACCOUNT_TXT));
		editAccountButton.setIcon(Util.loadImage("edit_account.png"));
		editAccountButton.setDisabledIcon(Util.loadImage("edit_account_d.png"));
//		editAccountButton.setBackground(Values.ORANGE);

		editAccountButton.addActionListener(this);
		editAccountButton.setEnabled(false);
		editAccountButton.setActionCommand(EDIT_ACCOUNT_TXT);
		toolbar.add(editAccountButton);

		// The "Delete Account" button
		deleteAccountButton = new JButton();
		deleteAccountButton.setToolTipText(Translator.translate(DELETE_ACCOUNT_TXT));
		deleteAccountButton.setIcon(Util.loadImage("delete_account.png"));
		deleteAccountButton.setDisabledIcon(Util.loadImage("delete_account_d.png"));
//		deleteAccountButton.setBackground(Values.ORANGE);

		deleteAccountButton.addActionListener(this);
		deleteAccountButton.setEnabled(false);
		deleteAccountButton.setActionCommand(DELETE_ACCOUNT_TXT);
		toolbar.add(deleteAccountButton);


		// The "Copy Username" button
		copyUsernameButton = new JButton();
		copyUsernameButton.setToolTipText(Translator.translate(COPY_USERNAME_TXT));
		copyUsernameButton.setIcon(Util.loadImage("copy_username.png"));
		copyUsernameButton.setDisabledIcon(Util.loadImage("copy_username_d.png"));
		copyUsernameButton.setBackground(Values.ORANGE);

		copyUsernameButton.addActionListener(e -> copyUsernameToClipboard());
		copyUsernameButton.setEnabled(false);
		toolbar.add(copyUsernameButton);

		// The "Copy Password" button
		copyPasswordButton = new JButton();
		copyPasswordButton.setToolTipText(Translator.translate(COPY_PASSWORD_TXT));
		copyPasswordButton.setIcon(Util.loadImage("copy_password.png"));
		copyPasswordButton.setDisabledIcon(Util.loadImage("copy_password_d.png"));
//		copyPasswordButton.setBackground(Values.ORANGE);

		copyPasswordButton.addActionListener(e -> copyPasswordToClipboard());
		copyPasswordButton.setEnabled(false);
		toolbar.add(copyPasswordButton);

		// The "Launch URL" button
		launchURLButton = new JButton();
		launchURLButton.setToolTipText(Translator.translate(LAUNCH_URL_TXT));
		launchURLButton.setIcon(Util.loadImage("launch_URL.png"));
		launchURLButton.setDisabledIcon(Util.loadImage("launch_URL_d.png"));
		launchURLButton.setBackground(Values.ORANGE);
		launchURLButton.addActionListener(e -> {

			AccountInformation accInfo = dbActions.getSelectedAccount();
			String uRl = accInfo.getUrl();

			if ((uRl == null) || (uRl.length() == 0)) {
				JOptionPane.showMessageDialog(launchURLButton.getParent(),
						Translator.translate("EmptyUrlJoptionpaneMsg"),
						Translator.translate("UrlErrorJoptionpaneTitle"), JOptionPane.WARNING_MESSAGE);
			} else if (!(urlIsValid(uRl))) {
				JOptionPane.showMessageDialog(launchURLButton.getParent(),
						Translator.translate("InvalidUrlJoptionpaneMsg"),
						Translator.translate("UrlErrorJoptionpaneTitle"), JOptionPane.WARNING_MESSAGE);
			} else {
				LaunchSelectedURL(uRl);

			}
		});
		launchURLButton.setEnabled(false);
		toolbar.add(launchURLButton);

		// The "Option" button
		optionsButton = new JButton();
		optionsButton.setToolTipText(Translator.translate(OPTIONS_TXT));
		optionsButton.setIcon(Util.loadImage("options.png"));
		optionsButton.setDisabledIcon(Util.loadImage("options_d.png"));
		optionsButton.setBackground(Values.ORANGE);
		optionsButton.addActionListener(this);
		optionsButton.setEnabled(true);
		optionsButton.setActionCommand(OPTIONS_TXT);
		toolbar.add(optionsButton);

		return toolbar;
	}

	private JMenuBar createMenuBar() {

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(Values.ORANGE);

		databaseMenu = new JMenu(Translator.translate("databaseMenu"));
		databaseMenu.setMnemonic(KeyEvent.VK_D);
		databaseMenu.setBackground(Values.ORANGE);
		menuBar.add(databaseMenu);

		newDatabaseMenuItem = new JMenuItem(Translator.translate(NEW_DATABASE_TXT), KeyEvent.VK_N);
		newDatabaseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		databaseMenu.add(newDatabaseMenuItem);
		newDatabaseMenuItem.addActionListener(this);
		newDatabaseMenuItem.setActionCommand(NEW_DATABASE_TXT);
		newDatabaseMenuItem.setBackground(Values.WHITE);

		openDatabaseMenuItem = new JMenuItem(Translator.translate(OPEN_DATABASE_TXT), KeyEvent.VK_O);
		openDatabaseMenuItem.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		databaseMenu.add(openDatabaseMenuItem);
		openDatabaseMenuItem.addActionListener(this);
		openDatabaseMenuItem.setActionCommand(OPEN_DATABASE_TXT);
		openDatabaseMenuItem.setBackground(Values.WHITE);


		changeMasterPasswordMenuItem = new JMenuItem(Translator.translate(CHANGE_MASTER_PASSWORD_TXT), KeyEvent.VK_G);
		changeMasterPasswordMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		databaseMenu.add(changeMasterPasswordMenuItem);
		changeMasterPasswordMenuItem.addActionListener(this);
		changeMasterPasswordMenuItem.setEnabled(false);
		changeMasterPasswordMenuItem.setActionCommand(CHANGE_MASTER_PASSWORD_TXT);
		changeMasterPasswordMenuItem.setBackground(Values.WHITE);

		exportMenuItem = new JMenuItem(Translator.translate(EXPORT_TXT), KeyEvent.VK_X);
		exportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		databaseMenu.add(exportMenuItem);
		exportMenuItem.addActionListener(this);
		exportMenuItem.setEnabled(false);
		exportMenuItem.setActionCommand(EXPORT_TXT);
		exportMenuItem.setBackground(Values.WHITE);

		importMenuItem = new JMenuItem(Translator.translate(IMPORT_TXT), KeyEvent.VK_I);
		importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		databaseMenu.add(importMenuItem);
		importMenuItem.addActionListener(this);
		importMenuItem.setEnabled(false);
		importMenuItem.setActionCommand(IMPORT_TXT);
		importMenuItem.setBackground(Values.WHITE);

		accountMenu = new JMenu(Translator.translate("accountMenu"));
		accountMenu.setMnemonic(KeyEvent.VK_A);
		accountMenu.setBackground(Values.WHITE);
		menuBar.add(accountMenu);

		addAccountMenuItem = new JMenuItem(Translator.translate(ADD_ACCOUNT_TXT), KeyEvent.VK_A);
		addAccountMenuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		accountMenu.add(addAccountMenuItem);
		addAccountMenuItem.addActionListener(this);
		addAccountMenuItem.setEnabled(false);
		addAccountMenuItem.setActionCommand(ADD_ACCOUNT_TXT);
		addAccountMenuItem.setBackground(Values.WHITE);

		editAccountMenuItem = new JMenuItem(Translator.translate(EDIT_ACCOUNT_TXT), KeyEvent.VK_E);
		editAccountMenuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		accountMenu.add(editAccountMenuItem);
		editAccountMenuItem.addActionListener(this);
		editAccountMenuItem.setEnabled(false);
		editAccountMenuItem.setActionCommand(EDIT_ACCOUNT_TXT);
		editAccountMenuItem.setBackground(Values.WHITE);

		deleteAccountMenuItem = new JMenuItem(Translator.translate(DELETE_ACCOUNT_TXT), KeyEvent.VK_D);
		deleteAccountMenuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		accountMenu.add(deleteAccountMenuItem);
		deleteAccountMenuItem.addActionListener(this);
		deleteAccountMenuItem.setEnabled(false);
		deleteAccountMenuItem.setActionCommand(DELETE_ACCOUNT_TXT);
		deleteAccountMenuItem.setBackground(Values.WHITE);

		viewAccountMenuItem = new JMenuItem(Translator.translate(VIEW_ACCOUNT_TXT), KeyEvent.VK_V);
		viewAccountMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		accountMenu.add(viewAccountMenuItem);
		viewAccountMenuItem.addActionListener(this);
		viewAccountMenuItem.setEnabled(false);
		viewAccountMenuItem.setActionCommand(VIEW_ACCOUNT_TXT);
		viewAccountMenuItem.setBackground(Values.WHITE);

		copyUsernameMenuItem = new JMenuItem(Translator.translate(COPY_USERNAME_TXT), KeyEvent.VK_U);
		copyUsernameMenuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		accountMenu.add(copyUsernameMenuItem);
		copyUsernameMenuItem.addActionListener(e -> copyUsernameToClipboard());
		copyUsernameMenuItem.setEnabled(false);
		copyUsernameMenuItem.setActionCommand(COPY_USERNAME_TXT);
		copyUsernameMenuItem.setBackground(Values.WHITE);

		copyPasswordMenuItem = new JMenuItem(Translator.translate(COPY_PASSWORD_TXT), KeyEvent.VK_P);
		copyPasswordMenuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		accountMenu.add(copyPasswordMenuItem);
		copyPasswordMenuItem.addActionListener(e -> copyPasswordToClipboard());

		copyPasswordMenuItem.setEnabled(false);
		copyPasswordMenuItem.setActionCommand(COPY_PASSWORD_TXT);
		copyPasswordMenuItem.setBackground(Values.WHITE);

		launchURLMenuItem = new JMenuItem(Translator.translate(LAUNCH_URL_TXT), KeyEvent.VK_B);
		launchURLMenuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		accountMenu.add(launchURLMenuItem);
		launchURLMenuItem.addActionListener(e -> {

			AccountInformation accInfo = dbActions.getSelectedAccount();
			String uRl = accInfo.getUrl();

			if ((uRl == null) || (uRl.length() == 0)) {
				JOptionPane.showMessageDialog(accountMenu.getParent().getParent(),
						Translator.translate("EmptyUrlJoptionpaneMsg"),
						Translator.translate("UrlErrorJoptionpaneTitle"), JOptionPane.WARNING_MESSAGE);

			} else if (!(urlIsValid(uRl))) {
				JOptionPane.showMessageDialog(accountMenu.getParent().getParent(),
						Translator.translate("InvalidUrlJoptionpaneMsg"),
						Translator.translate("UrlErrorJoptionpaneTitle"), JOptionPane.WARNING_MESSAGE);
			} else {
				LaunchSelectedURL(uRl);

			}
		});

		launchURLMenuItem.setEnabled(false);
		launchURLMenuItem.setActionCommand(LAUNCH_URL_TXT);
		launchURLMenuItem.setBackground(Values.WHITE);

		exitMenuItem = new JMenuItem(Translator.translate(EXIT_TXT), KeyEvent.VK_Q);
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		exitMenuItem.addActionListener(this);
		exitMenuItem.setActionCommand(EXIT_TXT);
		exitMenuItem.setBackground(Values.WHITE);

		aboutMenuItem = new JMenuItem(Translator.translate(ABOUT_TXT), KeyEvent.VK_4);
		aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		aboutMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				About.Main();
			}
		});
		aboutMenuItem.setActionCommand(ABOUT_TXT);
		aboutMenuItem.setBackground(Values.WHITE);
		
		//New Menu Items donate/Donate
		JMenuItem mnuItemPayPal = new JMenuItem(("Donate"), KeyEvent.VK_1); // Sub-Menu About Entry
		mnuItemPayPal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		mnuItemPayPal.setBackground(Values.WHITE);
		
		mnuItemPayPal.addActionListener(event -> {
			try
			{
				LaunchBrowser.launcher("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=N7QMVEZ8VCMAY");
			}
			catch (Exception ignored) {}

		});
		
		//New Menu Items support
		JMenuItem mnuItemSupport = new JMenuItem(("Support"), KeyEvent.VK_2); // Sub-Menu About Entry
		mnuItemSupport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		mnuItemSupport.setBackground(Values.WHITE);
		
		mnuItemSupport.addActionListener(event -> {
			try
			{
				LaunchBrowser.launcher("https://www.wagemaker.co.uk/");
			}
			catch (Exception ignored) {

		}
	});
	 
		//New Menu Items twitter
		JMenuItem mnuItemTwitter = new JMenuItem(("Twitter"), KeyEvent.VK_3); // Sub-Menu About Entry
		mnuItemTwitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		mnuItemTwitter.setBackground(Values.WHITE);
		mnuItemTwitter.addActionListener(event -> {
			try
			{
				LaunchBrowser.launcher("https://twitter.com/gcclinux");
			}
			catch (Exception ignored) {}

		});
		
        databaseMenu.add(exitMenuItem);

        helpMenu.set(new JMenu(Translator.translate("helpMenu")));
        helpMenu.get().setMnemonic(KeyEvent.VK_H);
        helpMenu.get().add(mnuItemPayPal);
        helpMenu.get().add(mnuItemSupport);
        helpMenu.get().add(mnuItemTwitter);
        helpMenu.get().add(aboutMenuItem);
        menuBar.add(helpMenu.get());

		return menuBar;

	}

	public JList<?> getAccountsListview() {
		return accountsListview;
	}

	private void copyUsernameToClipboard() {
		AccountInformation accInfo = dbActions.getSelectedAccount();
		copyToClipboard(accInfo.getUserId());
	}

	private void copyPasswordToClipboard() {
		AccountInformation accInfo = dbActions.getSelectedAccount();
		copyToClipboard(accInfo.getPassword());
	}

	private void copyToClipboard(String s) {
		StringSelection stringSelection = new StringSelection(s);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, stringSelection);
	}

	private boolean urlIsValid(String urL) {

		UrlValidator urlValidator = new UrlValidator();
		return urlValidator.isValid(urL);
	}
	
	// Method that get(as input) the selected Account URL and open this URL via
	// the default browser of our platform

	private void LaunchSelectedURL(String url) {

		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();

			try {
				desktop.browse(new URI(url));

			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
			// Linux and Mac specific code in order to launch url
		} else {
			Runtime runtime = Runtime.getRuntime();

			try {
				runtime.exec("xdg-open " + url);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * Writes current window position and size to the preferences
	 */
	private void storeWindowBounds() {
		Preferences.set(Preferences.ApplicationOptions.XLOC, Integer.toString(this.getX()));
		Preferences.set(Preferences.ApplicationOptions.YLOC, Integer.toString(this.getY()));
		Preferences.set(Preferences.ApplicationOptions.WWIDTH, Integer.toString(this.getWidth()));
		Preferences.set(Preferences.ApplicationOptions.WHEIGHT, Integer.toString(this.getHeight()));
	}

	/**
	 * Restores the window position and size to those found in the preferences
	 * Checks if the window can still be displayed, if not, revert to default
	 * position
	 */
	private void restoreWindowBounds() {
		int x = Preferences.getInt(Preferences.ApplicationOptions.XLOC, this.getX());
		int y = Preferences.getInt(Preferences.ApplicationOptions.YLOC, this.getY());

		if (getGraphicsConfigurationContaining(x, y) == null) {
			x = this.getX();
			y = this.getY();
		}
		int width = Preferences.getInt(Preferences.ApplicationOptions.WWIDTH, (1000));
		int height = Preferences.getInt(Preferences.ApplicationOptions.WHEIGHT, (600));
		
		this.setBounds(x, y, width, height);
		
	}

	/**
	 * Utility function for restoreWindowBounds
	 */
	private GraphicsConfiguration getGraphicsConfigurationContaining(int x, int y) {
		ArrayList<GraphicsConfiguration> configs = new ArrayList<>();
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = env.getScreenDevices();
		for (GraphicsDevice device : devices) {
			GraphicsConfiguration[] gconfigs = device.getConfigurations();
			configs.addAll(Arrays.asList(gconfigs));
		}
		for (GraphicsConfiguration graphicsConfiguration : configs) {
			Rectangle bounds = graphicsConfiguration.getBounds();
			if (bounds.contains(x, y)) {
				return graphicsConfiguration;
			}
		}
		return null;
	}

	/**
	 * Convenience method to iterate over all graphics configurations.
	 */
	@SuppressWarnings("unused")
	private static ArrayList<GraphicsConfiguration> getConfigs() {
		ArrayList<GraphicsConfiguration> result = new ArrayList<>();
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = env.getScreenDevices();
		for (GraphicsDevice device : devices) {
			GraphicsConfiguration[] configs = device.getConfigurations();
			result.addAll(Arrays.asList(configs));
		}
		return result;
	}

	public JButton getCopyPasswordButton() {
		return copyPasswordButton;
	}

	public JButton getLaunchURLButton() {
		return launchURLButton;
	}

	public JButton getCopyUsernameButton() {
		return copyUsernameButton;
	}

	public JButton getEditAccountButton() {
		return editAccountButton;
	}

	public JButton getAddAccountButton() {
		return addAccountButton;
	}

	public JButton getDeleteAccountButton() {
		return deleteAccountButton;
	}

	public JTextField getSearchField() {
		return searchField;
	}

	public JLabel getSearchIcon() {
		return searchIcon;
	}

	public JButton getResetSearchButton() {
		return resetSearchButton;
	}

	public JMenuItem getCopyPasswordMenuItem() {
		return copyPasswordMenuItem;
	}

	public JMenuItem getLaunchURLMenuItem() {
		return launchURLMenuItem;
	}

	public JMenuItem getCopyUsernameMenuItem() {
		return copyUsernameMenuItem;
	}

	public JMenuItem getDeleteAccountMenuItem() {
		return deleteAccountMenuItem;
	}

	public JMenuItem getViewAccountMenuItem() {
		return viewAccountMenuItem;
	}

	public JMenuItem getEditAccountMenuItem() {
		return editAccountMenuItem;
	}

	public static String getApplicationName() {
		return applicationName;
	}

	public JMenuItem getAddAccountMenuItem() {
		return addAccountMenuItem;
	}

	public JMenuItem getChangeMasterPasswordMenuItem() {
		return changeMasterPasswordMenuItem;
	}


	public void actionPerformed(ActionEvent event) {

		try {
			if (event.getActionCommand().equals(LaunchGUI.NEW_DATABASE_TXT)) {
				dbActions.newDatabase();
			} else if (event.getActionCommand().equals(LaunchGUI.OPEN_DATABASE_TXT)) {
				dbActions.openDatabase();
			} else if (event.getActionCommand().equals(LaunchGUI.ADD_ACCOUNT_TXT)) {
				dbActions.reloadDatabaseBefore(new AddAccountAction());
			} else if (event.getActionCommand().equals(LaunchGUI.EDIT_ACCOUNT_TXT)) {
				String selectedAccName = (String) this.accountsListview.getSelectedValue();
				dbActions.reloadDatabaseBefore(new EditAccountAction(selectedAccName));
			} else if (event.getActionCommand().equals(LaunchGUI.DELETE_ACCOUNT_TXT)) {
				dbActions.reloadDatabaseBefore(new DeleteAccountAction());
			} else if (event.getActionCommand().equals(LaunchGUI.VIEW_ACCOUNT_TXT)) {
				dbActions.viewAccount();
			} else if (event.getActionCommand().equals(LaunchGUI.OPTIONS_TXT)) {
				dbActions.options();
			} else if (event.getActionCommand().equals(LaunchGUI.ABOUT_TXT)) {
				dbActions.showAbout();
			} else if (event.getActionCommand().equals(LaunchGUI.RESET_SEARCH_TXT)) {
				dbActions.resetSearch();
			} else if (event.getActionCommand().equals(LaunchGUI.CHANGE_MASTER_PASSWORD_TXT)) {
				dbActions.reloadDatabaseBefore(new ChangeMasterPasswordAction());
			} else if (event.getActionCommand().equals(LaunchGUI.EXIT_TXT)) {
				dbActions.exitApplication();
			} else if (event.getActionCommand().equals(LaunchGUI.EXPORT_TXT)) {
				dbActions.export();
			} else if (event.getActionCommand().equals(LaunchGUI.IMPORT_TXT)) {
				dbActions.reloadDatabaseBefore(new ImportAccountsAction());
			}
		} catch (Exception e) {
			dbActions.errorHandler(e);
		}
	}

	public JMenuItem getExportMenuItem() {
		return exportMenuItem;
	}

	public JMenuItem getImportMenuItem() {
		return importMenuItem;
	}

	public JLabel getStatusBar() {
		return statusBar;
	}

	public JPanel getDatabaseFileChangedPanel() {
		return databaseFileChangedPanel;
	}

	public void initialiseControlsWithDefaultLanguage() {
		databaseMenu.setText(Translator.translate("databaseMenu"));
		newDatabaseMenuItem.setText(Translator.translate(NEW_DATABASE_TXT));
		openDatabaseMenuItem.setText(Translator.translate(OPEN_DATABASE_TXT));
		changeMasterPasswordMenuItem.setText(Translator.translate(CHANGE_MASTER_PASSWORD_TXT));
		accountMenu.setText(Translator.translate("accountMenu"));
		addAccountMenuItem.setText(Translator.translate(ADD_ACCOUNT_TXT));
		editAccountMenuItem.setText(Translator.translate(EDIT_ACCOUNT_TXT));
		deleteAccountMenuItem.setText(Translator.translate(DELETE_ACCOUNT_TXT));
		viewAccountMenuItem.setText(Translator.translate(VIEW_ACCOUNT_TXT));
		copyUsernameMenuItem.setText(Translator.translate(COPY_USERNAME_TXT));
		copyPasswordMenuItem.setText(Translator.translate(COPY_PASSWORD_TXT));
		launchURLMenuItem.setText(Translator.translate(LAUNCH_URL_TXT));
		exitMenuItem.setText(Translator.translate(EXIT_TXT));
		aboutMenuItem.setText(Translator.translate(ABOUT_TXT));
		exportMenuItem.setText(Translator.translate(EXPORT_TXT));
		importMenuItem.setText(Translator.translate(IMPORT_TXT));

		addAccountButton.setToolTipText(Translator.translate(ADD_ACCOUNT_TXT));
		editAccountButton.setToolTipText(Translator.translate(EDIT_ACCOUNT_TXT));
		deleteAccountButton.setToolTipText(Translator.translate(DELETE_ACCOUNT_TXT));
		copyUsernameButton.setToolTipText(Translator.translate(COPY_USERNAME_TXT));
		copyPasswordButton.setToolTipText(Translator.translate(COPY_PASSWORD_TXT));
		launchURLButton.setToolTipText(Translator.translate(LAUNCH_URL_TXT));
		optionsButton.setToolTipText(Translator.translate(OPTIONS_TXT));
		resetSearchButton.setToolTipText(Translator.translate(RESET_SEARCH_TXT));
	}

	public interface ChangeDatabaseAction {
		void doAction();
	}

	private class EditAccountAction implements ChangeDatabaseAction {
		private final String accountToEdit;

		public EditAccountAction(String accountToEdit) {
			this.accountToEdit = accountToEdit;
		}

		public void doAction() {
			try {
				dbActions.editAccount(accountToEdit);
			} catch (Exception e) {
				dbActions.errorHandler(e);
			}
		}
	}

	private class ChangeMasterPasswordAction implements ChangeDatabaseAction {
		public void doAction() {
			try {
				dbActions.changeMasterPassword();
			} catch (Exception e) {
				dbActions.errorHandler(e);
			}
		}
	}

	private class DeleteAccountAction implements ChangeDatabaseAction {
		public void doAction() {
			try {
				dbActions.deleteAccount();
			} catch (Exception e) {
				dbActions.errorHandler(e);
			}
		}
	}

	private class AddAccountAction implements ChangeDatabaseAction {
		public void doAction() {
			try {
				dbActions.addAccount();
			} catch (Exception e) {
				dbActions.errorHandler(e);
			}
		}
	}


	private class ImportAccountsAction implements ChangeDatabaseAction {
		public void doAction() {
			try {
				dbActions.importAccounts();
			} catch (Exception e) {
				dbActions.errorHandler(e);
			}
		}
	}

}
