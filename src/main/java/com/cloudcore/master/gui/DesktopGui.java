package com.cloudcore.master.gui;

import com.cloudcore.master.Main;
import com.cloudcore.master.Slaves;
import com.cloudcore.master.StatusParser;
import com.cloudcore.master.core.CloudCoin;
import com.cloudcore.master.core.Config;
import com.cloudcore.master.core.FileSystem;
import com.cloudcore.master.utils.CoinUtils;
import com.cloudcore.master.utils.FileUtils;
import com.cloudcore.master.utils.SimpleLogger;
import com.cloudcore.master.utils.Utils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class DesktopGui extends JFrame {


    /* Constant Fields */

    public static final Color COLOR_PURPLE_HEADER = Color.decode("#494CA8");

    public static final Color COLOR_PURPLE_BACKGROUND = Color.decode("#3A3a80");
    public static final Color COLOR_PURPLE_MIDGROUND = Color.decode("#343079");
    public static final Color COLOR_PURPLE_FOREGROUND = Color.decode("#4c50ab");

    public static final Color COLOR_FONT = Color.decode("#ffdc50");

    public static final Color COLOR_OFFWHITE = Color.decode("#dde1e4");
    private static final Color COLOR_PURPLE_HOVER = Color.decode("#5b668b");
    private static final Color COLOR_PURPLE_PRESS = Color.decode("#474f6c");

    public static final Color COLOR_LIGHT_GRAY = Color.decode("#e4e4e4");
    public static final Color COLOR_GRAY = Color.decode("#cccccc");

    public static final Color COLOR_RED = Color.decode("#f00b0b");

    private static final String[] MENU_ICONS = new String[] {"home.png", "wrench.png", "gear.png"};
    private static final String[][] MENU_TITLES = new String[][] {
            new String[]{"Deposit", "Withdraw"},
            new String[]{"Backup", "Eraser", "List Serials", "Re-Authenticate", "Store in Vault"},
            new String[]{"Change Workspace", "Show Folders"}};
    private static final String[] DENOMINATIONS = new String[] {"1's", "5's", "25's", "100's", "250's"};

    private static final int WINDOW_WIDTH = 1080;
    private static final int WINDOW_HEIGHT = 720;
    private static final int WINDOW_WIDTH_MENU = 200;
    private static final int WINDOW_HEIGHT_HEADER = 50;
    private static final int WINDOW_WIDTH_DIALOG = 240;
    private static final int WINDOW_HEIGHT_DIALOG = 320;

    private static int WINDOW_CENTER_WIDTH;
    private static int WINDOW_CENTER_HEIGHT;


    /* Fields */

    public static boolean performingCriticalOperation = false;

    // Servants
    ArrayList<Process> servants = new ArrayList<>(16);

    // Save data
    private static String lastFolderImport = Paths.get("").toString();

    // Pages
    private JPanel card;
    private CardLayout cardLayout;
    private JPanel[] pages = new JPanel[7];

    // Menus
    private MenuButton[] icons;
    private MenuButton[] buttons;
    private Font buttonFont;

    private int[] coinTotals = new int[6];

    // Total Amount
    private JLabel totalAmount;

    // Account Balance
    private DefaultTableModel accountCoinsTable;
    private JTextField[] accountTotals;

    // Echo RAIDA
    private JLabel[][] echoResultLabels;

    // Import
    private JLabel depositWarningLabel;
    private JProgressBar importProgress;
    private Thread importProgressThread;
    private JLabel importLabel;
    private boolean isOnImportPage;

    public static final String DEPOSIT_RESULT_MESSAGE = "Deposited %s CloudCoins out of %s";
    private JPanel depositTable;
    private JLabel[] depositTotals;
    private JLabel depositResultLabel;

    // Export Coins
    private JTextField exportInputTotal;
    private JSpinner[] exportInputs;
    private JTextField[] exportLabels;
    private JTextField tagInput;
    private boolean ignoreTotalListener;
    private boolean ignoreListenerDenom;

    // Synchronize Coins
    private String fixResultsMessage = "Synchronized %s CloudCoins!";
    private JLabel fixResults;
    private JLabel fixWarning;


    /* Constructor */

    public DesktopGui() {
        // Main Window rules
        setLayout(new MigLayout());
        setTitle("CloudCoin Advanced Client");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        getContentPane().setBackground(COLOR_PURPLE_HEADER);

        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Window Icon
        setIconImage(new ImageIcon(getClass().getResource("/icons/logo_16.png")).getImage());

        initializeData();
        initializeLayout();
        //initializeLayoutSquare();
        startAllServants();

        //pack();
        finalInitialization();
        setVisible(true);

        getTotalBank();
    }

    /* Methods */

    private void initializeData() {
        // Root Path
        String rootPath = Utils.loadData("RootPath");
        if (rootPath == null || rootPath.length() != 0) {
            System.out.println("custom root path");
            FileSystem.changeRootPath(rootPath);
        }
        else {
            System.out.println("default root path");
            FileSystem.setDefaultRootPath();
            FileSystem.createDirectories();
        }

        // Save Data
        lastFolderImport = Utils.loadData("lastFolderImport");
        if (!new File(lastFolderImport).exists()) {
            lastFolderImport = System.getProperty("user.home") + File.separatorChar + "Downloads" + File.separatorChar;
            Utils.saveData("lastFolderImport", lastFolderImport);
        }

        Slaves.createCommandShowCoins(null);
    }

    private void initializeLayoutSquareHeader() {
        // Header
        JPanel headerPanel = new JPanel(new MigLayout("insets 0, aligny 50%"));
        headerPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT_HEADER));
        headerPanel.setBackground(COLOR_PURPLE_FOREGROUND);
        add(headerPanel, "north");

        // Header Left
        JPanel headerLeft = new JPanel(new MigLayout("insets 0"));
        headerLeft.setPreferredSize(new Dimension(WINDOW_WIDTH / 2, WINDOW_HEIGHT_HEADER));
        headerLeft.setBackground(COLOR_PURPLE_FOREGROUND);
        headerPanel.add(headerLeft, "");
        // Logo
        JLabel logo = new JLabel(new ImageIcon(getClass().getResource("/icons/logo_white_35.png")));
        headerLeft.add(logo, "gapleft 10px");
        // Brand
        JLabel brand = new JLabel("CloudCoin Master", SwingConstants.LEFT);
        brand.setFont(new Font(brand.getFont().getName(), Font.BOLD, 24));
        brand.setForeground(COLOR_OFFWHITE);
        headerLeft.add(brand, "");

        // Header Right
        JPanel headerRight = new JPanel(new MigLayout("insets 0, aligny 50%, rtl"));
        headerRight.setPreferredSize(new Dimension(WINDOW_WIDTH / 2, WINDOW_HEIGHT_HEADER));
        headerRight.setBackground(COLOR_PURPLE_FOREGROUND);
        headerPanel.add(headerRight);
        // CC Text
        JLabel ccText = new JLabel(" CC");
        Font totalFont = new Font(ccText.getFont().getName(), Font.BOLD, 18);
        ccText.setFont(totalFont);
        ccText.setForeground(COLOR_OFFWHITE);
        headerRight.add(ccText, "gapleft 10px");
        // Total Amount
        totalAmount = new JLabel("0");
        totalAmount.setFont(totalFont);
        totalAmount.setForeground(COLOR_OFFWHITE);
        headerRight.add(totalAmount);
        // Total Label
        JLabel totalLabel = new JLabel("CloudCoins: ");
        totalLabel.setFont(totalFont);
        totalLabel.setForeground(COLOR_OFFWHITE);
        headerRight.add(totalLabel, "");
    }
    private void initializeLayoutSquare() {
        initializeLayoutSquareHeader();
        // Grid
        JPanel grid = new JPanel(new MigLayout("fill, wrap 3"));
        grid.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT - WINDOW_HEIGHT_HEADER));
        grid.setBackground(COLOR_PURPLE_BACKGROUND);
        add(grid, "grow");
        // Squares
        addGridButton(grid, "POWN", e -> createPopupPown());
        addGridButton(grid, "REPOWN", e -> repown());
        addGridButton(grid, "EXPORT", e -> createPopupExport());
        addGridButton(grid, "BACKUP", e -> Slaves.createCommandBackupper(null, FileSystem.folderChooser()));
        addGridButton(grid, "VAULT", e -> createPopupVault());
        addGridButton(grid, "REAUTHENTICATE", e -> Slaves.createCommandReauthenticator(null));
        addGridButton(grid, "ERASE", e -> Slaves.createCommandEraser(null));
        addGridButton(grid, "CHANGE FOLDER", e -> Slaves.changeWorkspaceFolder(this));
        addGridButton(grid, "TRANSLATE", e -> Slaves.createCommandTranslate("english"));
    }
    private void addGridButton(JPanel grid, String buttonText, ActionListener actionListener) {
        JPanel square = new JPanel(new MigLayout("fill, insets 0"));
        square.setBackground(COLOR_PURPLE_MIDGROUND);

        JButton button = new JButton(buttonText);
        button.setBackground(COLOR_PURPLE_FOREGROUND);
        button.setForeground(COLOR_FONT);
        button.addActionListener(actionListener);
        square.add(button, "grow, gap 10px 10px 10px 10px");
        grid.add(square, "grow, width 30%, height 30%");
    }

    private void initializeLayout() {
        createLayouts();
        createLayoutMenu();
    }
    private void createLayouts() {
        // Tables
        UIManager.put("TableHeader.cellBorder", COLOR_PURPLE_BACKGROUND);
        UIManager.put("TableHeader.background", COLOR_LIGHT_GRAY);

        // Header
        JPanel headerPanel = new JPanel(new MigLayout("fill"));
        headerPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT_HEADER));
        headerPanel.setBackground(COLOR_PURPLE_HEADER);
        add(headerPanel, "north");

        // Header Left
        JPanel headerLeft = new JPanel(new MigLayout("insets 0 10px 0 0, fill, wrap 2"));
        headerLeft.setPreferredSize(new Dimension(WINDOW_WIDTH / 2, WINDOW_HEIGHT_HEADER));
        headerLeft.setBackground(COLOR_PURPLE_HEADER);
        headerPanel.add(headerLeft);
        // Logo
        JLabel logo = new JLabel(new ImageIcon(getClass().getResource("/icons/logo_white_35.png")));
        headerLeft.add(logo);
        // Brand
        JLabel brand = new JLabel("CloudCoin Master", SwingConstants.LEFT);
        brand.setFont(new Font(brand.getFont().getName(), Font.BOLD, 32));
        brand.setForeground(COLOR_OFFWHITE);
        headerLeft.add(brand, "growx");

        // Header Right
        JPanel headerRight = new JPanel(new MigLayout("insets 0 10px 0 0, fill, wrap 2"));
        headerRight.setPreferredSize(new Dimension(WINDOW_WIDTH / 2, WINDOW_HEIGHT_HEADER));
        headerRight.setBackground(COLOR_PURPLE_HEADER);
        headerPanel.add(headerRight);
        // Total Label
        JLabel totalLabel = new JLabel("Total CloudCoins: ");
        Font totalFont = new Font(totalLabel.getFont().getName(), Font.BOLD, 18);
        totalLabel.setFont(totalFont);
        totalLabel.setForeground(COLOR_OFFWHITE);
        headerPanel.add(totalLabel, "growx");
        // Total Amount
        totalAmount = new JLabel("0");
        totalAmount.setFont(totalFont);
        totalAmount.setForeground(COLOR_OFFWHITE);
        headerPanel.add(totalAmount);
        // CC Text
        JLabel ccText = new JLabel(" CC");
        ccText.setFont(totalFont);
        ccText.setForeground(COLOR_OFFWHITE);
        headerPanel.add(ccText);

        Dimension fullSize = new Dimension(WINDOW_WIDTH - WINDOW_WIDTH_MENU, WINDOW_HEIGHT - WINDOW_HEIGHT_HEADER);

        // Right Layout
        JPanel rightPanel = new JPanel(new MigLayout("insets 0", "", ""));
        rightPanel.setPreferredSize(fullSize);
        rightPanel.setBackground(COLOR_PURPLE_BACKGROUND);
        add(rightPanel, "east");

        // Card Holder
        pages = new JPanel[14];
        for (int i = 0; i < pages.length; i++) {
            pages[i] = new JPanel();
            pages[i].setBackground(COLOR_PURPLE_BACKGROUND);
            pages[i].setPreferredSize(new Dimension(WINDOW_WIDTH - WINDOW_WIDTH_MENU - 50, WINDOW_HEIGHT - WINDOW_HEIGHT_HEADER));
        }
        card = new JPanel(new CardLayout());
        cardLayout = (CardLayout) (card.getLayout());
        rightPanel.add(card);

        // Main Pages
        createPageHome(pages[0]);
        createPageDeposit(pages[1]);
        createPageWithdraw(pages[2]);
        //Backup
        //Eraser
        createPageListSerials(pages[5]);
        createPageReauthenticate(pages[6]);
        createPageHome(pages[7]);
        createPageHome(pages[8]);
        createPageHome(pages[9]);
        createPageTools(pages[10]);
        createPageSettings(pages[11]);
        createPageHelp(pages[12]);

        card.add(pages[0], "Home");
        int c = 1;
        for (int i = 0; i < MENU_TITLES.length; i++) {
            for (int j = 0; j < MENU_TITLES[i].length; j++) {
                card.add(pages[c], MENU_TITLES[i][j]);
                c++;
            }
        }
        card.add(pages[10], "Tools");
        card.add(pages[11], "Settings");
        card.add(pages[12], "Help");
    }
    private void createLayoutMenu() {
        setBackground(COLOR_PURPLE_BACKGROUND);

        // Left Side
        JPanel leftSide = new JPanel(new MigLayout("flowy, insets 0 4px 0 4px", "", ""));
        leftSide.setPreferredSize(new Dimension((WINDOW_WIDTH_MENU / 2) - 50, WINDOW_HEIGHT - WINDOW_HEIGHT_HEADER));
        leftSide.setBackground(COLOR_PURPLE_HEADER);
        add(leftSide, "west");

        // Left ViewMargin 1
        JPanel leftMargin1 = new JPanel(new MigLayout("insets 0", "", ""));
        leftMargin1.setPreferredSize(new Dimension(75, WINDOW_HEIGHT - WINDOW_HEIGHT_HEADER));
        leftMargin1.setBackground(COLOR_PURPLE_BACKGROUND);
        add(leftMargin1, "west");

        // Menu Card Holder
        JPanel menuList = new JPanel(new CardLayout());
        menuList.setBackground(COLOR_PURPLE_FOREGROUND);
        CardLayout menuListLayout = (CardLayout) (menuList.getLayout());
        add(menuList, "aligny 0%, gapleft 2px, gapright 2px, gaptop 20px");

        // Create Buttons and Fonts
        int menuTitleLength = 0;
        for (String[] MENU_TITLE : MENU_TITLES)
            menuTitleLength += MENU_TITLE.length;
        buttons = new MenuButton[menuTitleLength];

        buttonFont = new Font(leftSide.getFont().getName(), java.awt.Font.BOLD, 17);
        HashMap<TextAttribute, Object> attr = new HashMap<>();
        attr.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        final Font underlineFont = buttonFont.deriveFont(attr);

        Border borderless = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 0), 1),
                BorderFactory.createEmptyBorder(0, 5, 0, 0));

        // Main Menu buttons
        String[] menu_titles = new String[] {"Home", "Tools", "Settings", "Help"};
        for (int i = 0; i < menu_titles.length; i++) {
            JButton button = new MenuButton(menu_titles[i], COLOR_PURPLE_PRESS, COLOR_PURPLE_HOVER);
            button.setFont(buttonFont);
            button.setHorizontalAlignment(SwingConstants.CENTER);
            button.setPreferredSize(new Dimension(200, 50));
            button.setBackground(COLOR_PURPLE_MIDGROUND);
            button.setForeground(COLOR_FONT);
            button.setBorder(borderless);
            button.setFocusPainted(false);
            leftSide.add(button);
            final String menuIndex = Integer.toString(i);
            final int index = i;
            button.addActionListener(e -> {
                menuListLayout.show(menuList, menuIndex);
                String title = "Home";
                if ("1".equals(menuIndex))
                    title = "Tools";
                else if ("2".equals(menuIndex))
                    title = "Settings";
                else if ("3".equals(menuIndex))
                    title = "Help";
                cardLayout.show(card, title);
            });
        }

        // Sub Menu
        JPanel[] menus = new JPanel[MENU_ICONS.length];
        for (int i = 0; i < MENU_ICONS.length; i++) {
            menus[i] = new JPanel(new MigLayout("flowy, insets 0 0 0 0"));
            menus[i].setBackground(COLOR_PURPLE_HEADER);
            for (int j = 0; j < MENU_TITLES[i].length; j++) {
                // Create Button
                MenuButton button = new MenuButton(MENU_TITLES[i][j], COLOR_PURPLE_PRESS, COLOR_PURPLE_HOVER);
                button.setFont(buttonFont);
                button.setHorizontalAlignment(SwingConstants.CENTER);
                button.setPreferredSize(new Dimension(200, 50));
                button.setBackground(COLOR_PURPLE_MIDGROUND);
                button.setForeground(COLOR_FONT);
                button.setBorder(borderless);
                button.setFocusPainted(false);
                menus[i].add(button);
                buttons[--menuTitleLength] = button;

                // Create Menu Button actions
                String title = MENU_TITLES[i][j];
                if ("Show Folders".equals(title))
                    button.addActionListener(e -> Main.showFolder());
                else if ("Change Workspace".equals(title))
                    button.addActionListener(e -> {
                        Slaves.changeWorkspaceFolder(this);
                    });
                else if ("Backup".equals(title))
                    button.addActionListener(e -> {
                        File folder = FileSystem.folderChooser();
                        if (Slaves.createCommandBackupper(null, folder)) {
                            createPopup(this, "CloudCoins backed up!");
                            Main.showSpecificFolder(folder.getAbsolutePath());
                        }
                    });
                else if ("Eraser".equals(title))
                    button.addActionListener(e -> {
                        Slaves.createCommandEraser(null);
                        createPopup(this, "All log files erased!");
                    });
                else if ("Store in Vault".equals(title))
                    button.addActionListener(e -> {
                        cardLayout.show(card, "Tools");
                        createPopupVault();
                    });
                else
                    button.addActionListener(e -> {
                        if (isOnImportPage && "Deposit".equals(title))
                            ;//beginImport();
                        else
                            isOnImportPage = false;
                        System.out.println("Switching to layout " + title);
                        cardLayout.show(card, title);
                        for (MenuButton button1 : buttons)
                            button1.setFont(buttonFont);
                        button.setFont(underlineFont);
                    });
            }
            menuList.add(menus[i], Integer.toString(i));
        }
    }


    private void startAllServants() {
        try {
            final Process process = openJarProgram("authenticator_servant");
            openJarProgram("echoer_servant_v0.02"); // Creates "Commands" Folder instead of "Commands"
            openJarProgram("frackfixer_servant_v0.02");
            openJarProgram("backupper_servant");
            openJarProgram("eraser_servant");
            openJarProgram("exporter_servant");
            openJarProgram("grader_servant");
            openJarProgram("lostfixer_servant");
            openJarProgram("reauthenticator_servant");
            openJarProgram("showcoins_servant");
            openJarProgram("unpacker_servant");
            openJarProgram("vaulter_servant");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Closing all Servants");
                for (Process servant : servants)
                    servant.destroy();
                System.out.println("All Servants closed");
            }));

            new Thread(() -> {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                try {
                    while ((line = input.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Process openJarProgram(String moduleName) throws Exception {
        if (isProcessRunning(moduleName))
            killProcess(moduleName);

        Process process = Runtime.getRuntime().exec("java -jar " + moduleName + ".jar " + FileSystem.RootPath);
        servants.add(process);

        //OutputStream out = process.getOutputStream();
        //String command = "Hello program, run this command: ExportCoins (Much faster than .txt files!)";
        //out.write(command.getBytes());

        return process;
    }

    public static final String TASKLIST = "tasklist";
    public static final String KILL = "taskkill /F /IM ";

    public static boolean isProcessRunning(String moduleName) throws Exception {
        Process process = Runtime.getRuntime().exec(TASKLIST);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            //System.out.println(line);
            if (line.contains(moduleName))
                return true;
        }
        return false;
    }

    public static void killProcess(String serviceName) throws Exception {
        Runtime.getRuntime().exec(KILL + serviceName);
    }

    private void finalInitialization() {
        // Center
        WINDOW_CENTER_WIDTH = WINDOW_WIDTH / 2;
        WINDOW_CENTER_HEIGHT = WINDOW_HEIGHT_HEADER + ((WINDOW_HEIGHT + 2) / 2);
    }

    private void createPopupPown() {
        final File[] files = FileSystem.fileChooser(FileSystem.ImportFolder);
        if (files == null || files.length == 0) {
            cardLayout.show(card, "Home");
            return;
        }

        for (File file : files)
            FileSystem.moveFile(file.getName(), file.getAbsolutePath().replace(file.getName(), ""), FileSystem.ImportFolder, true);
        repown();
    }

    private void repown() {
        final JFrame popup = createPopupImport();
        startImportProgressThread();
        new Thread(() -> {
            try {
                Thread.sleep(4000);
                importProgressThread.interrupt();
                importProgress.setValue(100);
                popup.dispose();
                createPopup(this, "CloudCoin successfully deposited!");
                cardLayout.show(card, "Home");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void createPopupExport() {
        String newWorkspace;

        final JFrame parent = new JFrame();
        parent.setLayout(new MigLayout("insets 0, align 50%, wrap 2"));
        parent.setSize(WINDOW_WIDTH_DIALOG, WINDOW_HEIGHT_DIALOG);
        parent.setBackground(COLOR_PURPLE_BACKGROUND);
        parent.setResizable(false);
        parent.setLocationRelativeTo(null);

        String[] denominations = new String[] {"1's:", "5's:", "25's:", "100's:", "250's:"};
        final JTextField[] textField = new JTextField[5];
        JLabel label;
        for (int i = 0; i < 5; i++) {
            label = new JLabel(denominations[i], SwingConstants.RIGHT);
            textField[i] = new JTextField("0");
            parent.add(label, "width 25%, gapleft 10%");
            parent.add(textField[i], "width 55%, gapright 10%");
        }

        String[] options = new String[] {"Individual Stacks", "Single Stack"};
        final JComboBox<String> dropdown = new JComboBox<>(options);
        parent.add(dropdown, "alignx 50%, span 2, gaptop 5%");

        label = new JLabel("Tag:", SwingConstants.RIGHT);
        final JTextField tagField = new JTextField("");
        parent.add(label, "width 25%, gapleft 10%, gaptop 5%");
        parent.add(tagField, "width 55%, gapright 5%");

        JButton submit = new JButton("Export CloudCoins");
        parent.add(submit, "alignx 50%, span 2, gaptop 5%");
        submit.addActionListener(e -> {
            boolean allZeros = true;
            for (int i = 0; i < denominations.length; i++)
                if (!textField[i].getText().equals("0") && textField[i].getText().length() != 0) {
                    allZeros = false;
                    break;
                }
            if (allZeros) {
                createPopup(parent, "You must select CloudCoins to export.");
                return;
            }

            int totalAmount = Utils.parseInt(textField[0].getText()) + Utils.parseInt(textField[1].getText()) * 5 +
                    Utils.parseInt(textField[2].getText()) * 25 + Utils.parseInt(textField[3].getText()) * 100 +
                    Utils.parseInt(textField[4].getText()) * 250;
            Slaves.createCommandExporter(null, totalAmount, dropdown.getSelectedIndex(), tagField.getText());
            parent.dispose();
        });

        parent.setVisible(true);
    }

    public static void createPopupVault() {
        String newWorkspace;

        final JFrame parent = new JFrame();
        parent.setLayout(new MigLayout("insets 0, align 50%, wrap 2"));
        parent.setSize(WINDOW_WIDTH_DIALOG, WINDOW_HEIGHT_DIALOG);
        parent.setBackground(COLOR_PURPLE_BACKGROUND);
        parent.setResizable(false);
        parent.setLocationRelativeTo(null);

        String[] denominations = new String[] {"1's:", "5's:", "25's:", "100's:", "250's:"};
        final JTextField[] textField = new JTextField[5];
        JLabel label;
        for (int i = 0; i < 5; i++) {
            label = new JLabel(denominations[i], SwingConstants.RIGHT);
            textField[i] = new JTextField("0");
            parent.add(label, "width 25%, gapleft 10%");
            parent.add(textField[i], "width 55%, gapright 10%");
        }

        label = new JLabel("Password:", SwingConstants.RIGHT);
        final JTextField passwordField = new JTextField("");
        parent.add(label, "width 25%, gapleft 10%, gaptop 10%");
        parent.add(passwordField, "width 55%, gapright 10%");

        String[] options = new String[] {"To Vault", "From Vault"};
        final JComboBox<String> dropdown = new JComboBox<>(options);
        parent.add(dropdown, "alignx 50%, span 2, gaptop 5%");

        JButton submit = new JButton("Vault CloudCoins");
        parent.add(submit, "alignx 50%, span 2, gaptop 5%");
        submit.addActionListener(e -> {
            String password = passwordField.getText();
            if (password == null || password.length() == 0) {
                createPopup(parent, "You must enter a password to encrypt your CloudCoins.");
                return;
            }

            boolean allZeros = true;
            for (int i = 0; i < denominations.length; i++)
                if (!textField[i].getText().equals("0") && textField[i].getText().length() != 0) {
                    allZeros = false;
                    break;
                }
            if (allZeros) {
                createPopup(parent, "You must select CloudCoins to encrypt.");
                return;
            }

            String command = (dropdown.getSelectedIndex() == 0) ? "toVault" : "fromVault";

            int totalAmount = Utils.parseInt(textField[0].getText()) + Utils.parseInt(textField[1].getText()) * 5 +
                    Utils.parseInt(textField[2].getText()) * 25 + Utils.parseInt(textField[3].getText()) * 100 +
                    Utils.parseInt(textField[4].getText()) * 250;
            Slaves.createCommandToVault(command, null, Integer.toString(totalAmount), password);
            parent.dispose();
        });

        parent.setVisible(true);
    }

    private void createPageHome(JPanel page) {
        // Help page
        page.setLayout(new MigLayout("insets 0, alignx 50%, fillx"));

        // Messages
        String versionLabel = "CloudCoin Advanced Client";
        String[] labels = new String[] { "Used to authenticate, store, and payout CloudCoins.",
                "This software is provided as is, with all faults, defects, errors,",
                "and without warranty of any kind. Free of charge from the",
                "CloudCoin Consortium." };

        // Labels
        JLabel header = new JLabel(versionLabel, SwingConstants.CENTER);
        header.setForeground(COLOR_OFFWHITE);
        header.setFont(new Font(header.getFont().getName(), Font.BOLD, 32));

        // Add to page
        page.add(header, "alignx, growx, wrap, gapy 150px 50px");
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i], SwingConstants.CENTER);
            label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 18));
            label.setForeground(COLOR_OFFWHITE);
            page.add(label, "alignx, growx, wrap");
        }
    }
    private void createPageTools(JPanel page) {
        // Help page
        page.setLayout(new MigLayout("insets 0, alignx 50%, fillx"));

        // Messages
        String headerTitle = "Tools";
        String message1 = "Show Folders will show you the folder structure used by";
        String message2 = "the CloudCoin Safe on your hard drive.";
        String message3 = "Synchronize - Used to heal CloudCoins that have been";
        String message4 = "fracked or that some RAIDA think are fake";

        // Labels
        JLabel header = new JLabel(headerTitle, SwingConstants.CENTER);
        formatJLabel(header, COLOR_OFFWHITE, 32);
        JLabel label1 = new JLabel(message1, SwingConstants.CENTER);
        formatJLabel(label1, COLOR_OFFWHITE, 18);
        JLabel label2 = new JLabel(message2, SwingConstants.CENTER);
        formatJLabel(label2, COLOR_OFFWHITE, 18);
        JLabel label3 = new JLabel(message3, SwingConstants.CENTER);
        formatJLabel(label3, COLOR_OFFWHITE, 18);
        JLabel label4 = new JLabel(message4, SwingConstants.CENTER);
        formatJLabel(label4, COLOR_OFFWHITE, 18);

        // Add to page
        page.add(header, "alignx, growx, wrap, gapy 150px 50px");
        page.add(label1, "alignx, growx, wrap");
        page.add(label2, "alignx, growx, wrap, gapy 0px 25px");
        page.add(label3, "alignx, growx, wrap");
        page.add(label4, "alignx, growx, wrap");
    }
    private void createPageSettings(JPanel page) {
        // Help page
        page.setLayout(new MigLayout("insets 0, alignx 50%, fillx"));

        // Messages
        String headerTitle = "Settings";
        String message1 = "Change Workspace will let you choose where to find CloudCoins.";
        String message2 = "Show Folders - Show location of the folders with CloudCoins. ";

        // Labels
        JLabel header = new JLabel(headerTitle, SwingConstants.CENTER);
        formatJLabel(header, COLOR_OFFWHITE, 32);
        JLabel label1 = new JLabel(message1, SwingConstants.CENTER);
        formatJLabel(label1, COLOR_OFFWHITE, 18);
        JLabel label2 = new JLabel(message2, SwingConstants.CENTER);
        formatJLabel(label2, COLOR_OFFWHITE, 18);

        // Add to page
        page.add(header, "alignx, growx, wrap, gapy 50px 50px");
        page.add(label1, "alignx, growx, wrap");
        page.add(label2, "alignx, growx, wrap, gapy 25px 0");
    }
    private void createPageHelp(JPanel page) {
        // Help page
        page.setLayout(new MigLayout("insets 0, alignx 50%, fillx"));

        // Clipboard
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // Messages
        String helpMessage = "Customer Service:";
        String hoursMessage = "9:00am - 11:00pm PST";
        String phoneTrim = "15305002646";
        String phone = "<html><a href=''>1 (530) 762-1361</a></html>";
        String emailTrim = "CloudCoinSupport@Protonmail.com";
        String email = "<html><a href=''>" + emailTrim + "</a></html>";
        String url = "http://cloudcoinconsortium.com/use.html";

        // Labels
        JLabel helpLabel = new JLabel(helpMessage, SwingConstants.CENTER);
        formatJLabel(helpLabel, COLOR_OFFWHITE, 32);
        JLabel hoursLabel = new JLabel(hoursMessage, SwingConstants.CENTER);
        formatJLabel(hoursLabel, COLOR_OFFWHITE, 18);

        final JFrame topWindow = this;

        JLabel phoneLabel = new JLabel(phone, SwingConstants.CENTER);
        formatJLabel(phoneLabel, COLOR_OFFWHITE, 18);
        JLabel emailLabel = new JLabel(email, SwingConstants.CENTER);
        formatJLabel(emailLabel, COLOR_OFFWHITE, 18);
        JLabel urlLabel = new JLabel(url, SwingConstants.CENTER);
        formatJLabel(urlLabel, COLOR_OFFWHITE, 18);

        // Add to page
        page.add(helpLabel, "alignx, growx, wrap, gapy 150px 25px");
        page.add(hoursLabel, "alignx, growx, wrap");
        page.add(phoneLabel, "alignx, growx, wrap");
        page.add(emailLabel, "alignx, growx, wrap");
        page.add(urlLabel, "alignx, growx");
    }

    private void createPageDeposit(JPanel page) {
        // Help page
        page.setLayout(new MigLayout("insets 0, alignx 50%, fillx"));

        // Messages
        String versionLabel = "Depositing CloudCoins...";
        String warningLabel = "Do not close application until all CloudCoins are deposited!";

        // Labels
        JLabel header = createJLabel(versionLabel, SwingConstants.CENTER, COLOR_RED, 32);
        depositWarningLabel = createJLabel(warningLabel, SwingConstants.CENTER, COLOR_RED, 18);

        // Add to page
        page.add(header, "alignx, growx, wrap, gapy 25px 25px");
        page.add(depositWarningLabel, "alignx, growx, wrap");

        page.addComponentListener(new ComponentAdapter() {
            @Override public void componentHidden(ComponentEvent evt) {}
            @Override
            public void componentShown(ComponentEvent evt) {
                isOnImportPage = true;
                depositWarningLabel.setVisible(true);
                createPopupPown();
            }
        });
    }
    private void createPageWithdraw(JPanel page) {
        // Export Coins
        page.setLayout(new MigLayout("insets 0, align 50%, fillx, wrap 5"));

        // Row: Title
        JPanel rowTitle = new JPanel(new MigLayout("insets 0, align 50%, fillx"));
        rowTitle.setBackground(COLOR_PURPLE_BACKGROUND);
        page.add(rowTitle, "alignx, gapy 25px 25px, growx, wrap, span 5");

        JLabel title = new JLabel("Withdraw", SwingConstants.CENTER);
        title.setForeground(COLOR_PURPLE_BACKGROUND);
        title.setFont(new Font(title.getFont().getName(), java.awt.Font.BOLD, 24));
        rowTitle.add(title, "growx, width 10%");

        // Row: Denominations
        JPanel section = new JPanel(new MigLayout("insets 0, alignx 50%, fillx"));
        section.setBackground(COLOR_PURPLE_BACKGROUND);
        page.add(section, "alignx, growx, wrap, span 5");

        exportLabels = new JTextField[DENOMINATIONS.length];
        exportInputs = new JSpinner[DENOMINATIONS.length];
        for (int i = 0; i < DENOMINATIONS.length; i++) {
            // Denomination Label
            JLabel label = new JLabel(DENOMINATIONS[i]);
            label.setHorizontalAlignment(JLabel.RIGHT);
            label.setForeground(COLOR_OFFWHITE);
            section.add(label, "alignx, gapy 8%, width 20%");
            // Total Label
            exportLabels[i] = new JTextField(coinTotals[i]);
            exportLabels[i].setHorizontalAlignment(JTextField.RIGHT);
            exportLabels[i].setEditable(false);
            section.add(exportLabels[i], "gapx 2.5% 2.5%, width 25%");
            // Number Input
            final int index = i;
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, coinTotals[i], 1);
            exportInputs[i] = new JSpinner(spinnerModel);
            section.add(exportInputs[i], "alignx, gapx 0 25%, width 25%, wrap");
            exportInputs[i].addChangeListener(e -> updateExportFromDenomination(exportInputs[index], index));
        }

        // Row: Total
        JPanel rowTotal = new JPanel(new MigLayout("insets 0, aligny 50%, wrap 2"));
        rowTotal.setBackground(COLOR_PURPLE_BACKGROUND);
        rowTotal.setPreferredSize(page.getPreferredSize());
        page.add(rowTotal, "growx, wrap, span 5");

        JLabel totalLabel = new JLabel("Withdraw Total:", SwingConstants.RIGHT);
        totalLabel.setForeground(COLOR_OFFWHITE);
        totalLabel.setFont(new Font(totalLabel.getFont().getName(), Font.BOLD, 16));
        rowTotal.add(totalLabel, "gapx 0 2%, width 48%");
        exportInputTotal = new JTextField("0");
        exportInputTotal.setEditable(false);
        exportInputTotal.setForeground(COLOR_OFFWHITE);
        exportInputTotal.setBackground(COLOR_PURPLE_BACKGROUND);
        //exportInputTotal.setHorizontalAlignment(JTextField.RIGHT);
        rowTotal.add(exportInputTotal, "gapx 0 25%, width 25%");

        ((AbstractDocument) exportInputTotal.getDocument()).setDocumentFilter(new InputDigitFilter());

        // Row: Tag
        JPanel rowTag = new JPanel(new MigLayout("insets 0, aligny 0%, wrap 2"));
        rowTag.setBackground(COLOR_PURPLE_BACKGROUND);
        rowTag.setPreferredSize(page.getPreferredSize());
        page.add(rowTag, "growx, wrap, span 5");

        JLabel tagLabel = new JLabel("Tag:", SwingConstants.RIGHT);
        tagLabel.setForeground(COLOR_OFFWHITE);
        rowTag.add(tagLabel, "gapy 20% 10%, width 25%");
        tagInput = new JTextField();
        tagInput.setHorizontalAlignment(JTextField.RIGHT);
        tagInput.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (tagInput.getText().length() >= 16)
                    e.consume();
            }
        });
        rowTag.add(tagInput, "gapx 0 25%, width 50%, wrap");

        // Row: Available Coins
        resetExportTotals();

        // Row: Export Stack
        JPanel rowSubmit = new JPanel(new MigLayout("insets 0, align 50%"));
        rowSubmit.setBackground(COLOR_PURPLE_BACKGROUND);
        page.add(rowSubmit, "south, gapy 0 25px, growx, span 5");

        JButton export = new JButton("Withdraw CloudCoins");
        export.setFont(new Font(export.getFont().getName(), export.getFont().getStyle(), 18));
        rowSubmit.add(export, "width 20%");
        export.addActionListener(e -> {
            int amount = Utils.parseInt(exportInputTotal.getText());
            if (amount <= 0) return;

            int totalAvailable = FileSystem.getTotalCoinsBank()[5];
            System.out.println(amount + " < " + totalAvailable);
            if (amount > totalAvailable) {
                createPopup(this, "Cannot withdraw that amount, there are not enough denominations.");
                return;
            }

            Slaves.createCommandExporter(null, amount, 1, tagInput.getText());
            Main.showFolder(FileSystem.ExportPath);
        });

        page.addComponentListener(new ComponentAdapter() {
            @Override public void componentHidden(ComponentEvent evt) {}
            @Override
            public void componentShown(ComponentEvent evt) {
                resetExportTotals();
            }
        });
    }
    private void createPageListSerials(JPanel page) {
        // Show Coins
        page.setLayout(new MigLayout("insets 0 25px 0 25px, align 50%, fillx, wrap 5"));

        // Table
        Object[] headers = new Object[] {"Serial Number", "Denomination", "Expires"};
        Border tableBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, COLOR_GRAY);
        Border cellBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, COLOR_PURPLE_BACKGROUND);

        accountCoinsTable = new DefaultTableModel(headers, 0);
        JTable table = new JTable(accountCoinsTable);
        table.setFillsViewportHeight(true);
        table.setBorder(cellBorder);
        table.setShowVerticalLines(false);
        table.setRowHeight(26);
        table.setIntercellSpacing(new Dimension(10, 0));

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new TableHeaderRenderer());
        header.setPreferredSize(new Dimension(page.getWidth(), 26));

        JScrollPane scrollPane = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(tableBorder);
        page.add(scrollPane, "gap 0 0 10px 10px, grow, span 5");

        page.addComponentListener(new ComponentAdapter() {
            @Override public void componentHidden(ComponentEvent evt) {}
            @Override
            public void componentShown(ComponentEvent evt) {
                Slaves.createCommandShowCoins(null);
                getTotalBank();
            }
        });
    }
    private void createPageReauthenticate(JPanel page) {
        // Help page
        page.setLayout(new MigLayout("insets 0, alignx 50%, fillx"));

        // Messages
        String versionLabel = "Checking health of your CloudCoins...";
        String warningLabel = "Do not close application until all CloudCoins are checked!";

        // Labels
        JLabel header = createJLabel(versionLabel, SwingConstants.CENTER, COLOR_OFFWHITE, 32);
        depositWarningLabel = createJLabel(warningLabel, SwingConstants.CENTER, COLOR_RED, 18);

        // Add to page
        page.add(header, "alignx, growx, wrap, gapy 25px 25px");
        page.add(depositWarningLabel, "alignx, growx, wrap");

        page.addComponentListener(new ComponentAdapter() {
            @Override public void componentHidden(ComponentEvent evt) {}
            @Override
            public void componentShown(ComponentEvent evt) {
                isOnImportPage = true;
                Slaves.createCommandReauthenticator(null);
                repown();
            }
        });
    }

    private JFrame createPopupImport() {
        // Settings Window
        JFrame importer = new JFrame();
        importer.setLayout(new MigLayout("insets 0, alignx 50%, fillx"));
        importer.setTitle("Deposit");
        importer.setSize(WINDOW_HEIGHT_DIALOG, WINDOW_WIDTH_DIALOG);
        importer.getContentPane().setBackground(COLOR_OFFWHITE);

        importer.setResizable(false);
        importer.setLocationRelativeTo(this);

        Point center = getLocation();
        center.x += WINDOW_CENTER_WIDTH - importer.getWidth() / 2;
        center.y += WINDOW_CENTER_HEIGHT - importer.getHeight() / 2;
        importer.setLocation(center);

        // Row: Title
        JPanel rowTitle = new JPanel(new MigLayout("insets 10px, align 50%, fillx"));
        rowTitle.setFont(new Font(rowTitle.getFont().getName(), rowTitle.getFont().getStyle(), 18));
        rowTitle.setBackground(COLOR_OFFWHITE);
        importer.add(rowTitle, "alignx, growx, wrap, span 5");

        importLabel = new JLabel("Connecting to the RAIDA...", SwingConstants.CENTER);
        importLabel.setFont(new Font(importLabel.getFont().getName(), java.awt.Font.BOLD, 14));
        rowTitle.add(importLabel, "gapy 10px, growx, width 10%");

        // Row: Progress Bar
        importProgress = new JProgressBar(0, 100);
        importer.add(importProgress, "alignx 50%, gapy 20px");

        importer.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println("can close: " + !performingCriticalOperation);
                if (performingCriticalOperation)
                    importer.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                else
                    importer.dispose();
            }
        });

        importer.setVisible(true);
        return importer;
    }
    private void startImportProgressThread() {
        if (importProgressThread != null) {
            importProgressThread.interrupt();
            importProgressThread = null;
        }

        importProgressThread = new Thread(() -> {
            try {
                int progress = 0;
                importProgress.setValue(progress);
                while (progress < 100) {
                    Thread.sleep(32);
                    progress++;
                    importProgress.setValue(progress);
                }
            } catch (InterruptedException e) {}
        });
        importProgressThread.start();
    }

    private void resetExportTotals() {
        setExportTotalInput("0");

        coinTotals = FileSystem.getTotalCoinsBank();
        for (int i = 0; i < DENOMINATIONS.length; i++) {
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, coinTotals[i], 1);
            exportInputs[i].setModel(spinnerModel);
            exportInputs[i].setValue(0);
            exportLabels[i].setText(String.valueOf(coinTotals[i]));
        }
    }
    private void updateExportFromDenomination(JSpinner input, int index) {
        if (ignoreTotalListener || ignoreListenerDenom)
            return;

        int amount = (int) input.getValue();
        int newTotal = getTotalFromExportInputs();
        final String finalTotal = Integer.toString(newTotal);

        ignoreTotalListener = true;
        new Thread(() -> {
            exportInputTotal.setText(finalTotal);
            calculateNotesForTotal(finalTotal, index, amount);
            ignoreTotalListener = false;
        }).start();

    }
    private int getTotalFromExportInputs() {
        int total = 0;
        for (int i = 0; i < exportInputs.length; i++) {
            total += ((int) exportInputs[i].getValue()) * CoinUtils.getDenomination(i);
        }
        return total;
    }
    private int[] calculateNotesForTotal(String inputTotal, int denominationIndex, int denominationAmount) {
        if (inputTotal == null || inputTotal.length() == 0)
            return new int[0];

        int amount = 0;
        try {
            amount = Integer.valueOf(inputTotal);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return new int[0];
        }

        coinTotals = FileSystem.getTotalCoinsBank();
        int[] notesToExport = new int[coinTotals.length];

        if (denominationIndex != -1 && denominationAmount != -1) {
            notesToExport[denominationIndex] = denominationAmount;
            amount -= denominationAmount * 1;
        }

        if (amount >= 250 && coinTotals[4] > 0 && denominationIndex != 4) {
            notesToExport[4] = ((amount / 250) < (coinTotals[4])) ? (amount / 250) : (coinTotals[4]);
            amount -= (notesToExport[4] * 250);
        }
        if (amount >= 100 && coinTotals[3] > 0 && denominationIndex != 3) {
            notesToExport[3] = ((amount / 100) < (coinTotals[3])) ? (amount / 100) : (coinTotals[3]);
            amount -= (notesToExport[3] * 100);
        }
        if (amount >= 25 && coinTotals[2] > 0 && denominationIndex != 2) {
            notesToExport[2] = ((amount / 25) < (coinTotals[2])) ? (amount / 25) : (coinTotals[2]);
            amount -= (notesToExport[2] * 25);
        }
        if (amount >= 5 && coinTotals[1] > 0 && denominationIndex != 1) {
            notesToExport[1] = ((amount / 5) < (coinTotals[1])) ? (amount / 5) : (coinTotals[1]);
            amount -= (notesToExport[1] * 5);
        }
        if (amount >= 1 && coinTotals[0] > 0 && denominationIndex != 0) {
            notesToExport[0] = (amount < (coinTotals[0])) ? amount : (coinTotals[0]);
            amount -= (notesToExport[0]);
        }

        if (amount != 0) {
            if (denominationIndex != -1 && denominationAmount != -1) {
                SimpleLogger.QuickLog("notes: " + Arrays.toString(notesToExport));
                amount = notesToExport[0] + notesToExport[1] * 5 + notesToExport[2] * 25 + notesToExport[3] * 100 + notesToExport[4] * 250;
                String value = Integer.toString(amount);
                exportInputTotal.setText(value);
                //exportInputTotal.setCaretPosition(value.length());
            }
            else
                return new int[0];
        }

        return notesToExport;
    }
    private void setExportTotalInput(String input) {
        boolean toggleBoolean = !ignoreTotalListener;
        if (toggleBoolean)
            ignoreTotalListener = true;

        exportInputTotal.setText(input);
        //exportInputTotal.setCaretPosition(input.length());

        if (toggleBoolean)
            ignoreTotalListener = false;
    }

    private void getTotalBank() {
        int[] totals = StatusParser.getTotalBank();
        updateTotalBank(totals);
    }
    private void updateTotalBank(int[] totals) {
        // List Serials table
        for (int i = 0, j = accountCoinsTable.getRowCount(); i < j; i++)
            accountCoinsTable.removeRow(0);

        ArrayList<CloudCoin> bankCoins = FileSystem.loadFolderCoins(FileSystem.BankFolder);
        bankCoins.addAll(FileSystem.loadFolderCoins(FileSystem.FrackedFolder));

        int total = 0;
        for (CloudCoin coin : bankCoins) {
            // SN, NN, Denomination, ED
            accountCoinsTable.addRow(new Object[] {String.valueOf(coin.getSn()),
                    String.valueOf(CoinUtils.getDenomination(coin)),
                    coin.getEd()});
            total += CoinUtils.getDenomination(coin);
        }

        // Header total
        totalAmount.setText(Integer.toString(total));
    }

    public static JFrame createPopup(JFrame topWindow, String message) {
        final JFrame parent = new JFrame();
        parent.setBackground(COLOR_OFFWHITE);
        parent.setResizable(false);
        parent.setLocationRelativeTo(null);

        Point center = topWindow.getLocation();
        center.x += WINDOW_CENTER_WIDTH - WINDOW_WIDTH_DIALOG / 2;
        center.y += WINDOW_CENTER_HEIGHT - WINDOW_HEIGHT_DIALOG / 2;
        parent.setLocation(center);

        JOptionPane.showMessageDialog(parent, message);

        if (message.startsWith("Error"))
            System.exit(0);

        return parent;
    }

    public static JLabel createJLabel(String message, int swingConstant, Color color, int size) {
        JLabel label = new JLabel(message, swingConstant);
        formatJLabel(label, color, size);
        return label;
    }
    public static void formatJLabel(JLabel label, Color color, int size) {
        label.setFont(new Font(label.getFont().getName(), Font.BOLD, size));
        label.setForeground(color);
    }
}
