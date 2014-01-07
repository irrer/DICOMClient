package edu.umro.dicom.client;

/*
 * Copyright 2012 Regents of the University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicArrowButton;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;

import edu.umro.util.Log;
import edu.umro.util.OpSys;
import edu.umro.util.General;

/**
 * Main class that shows a GUI to let the user upload DICOM files.
 * 
 * This dialog constructs a list of patients based on the DICOM files
 * specified by the user.  The <code>Patient</code> objects contain <code>Study</code> objects,
 * that in turn contain <code>Series</code> objects.
 *
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
public class DicomClient implements ActionListener, FileDrop.Listener, ChangeListener, DocumentListener {

    /** The portion of a DICOM file that must be read to get the metadata required to get general information about it. */
    private static final int DICOM_METADATA_LENGTH = 4 * 1024;

    /** Name that appears in title bar of window. */
    public static final String PROJECT_NAME = "DICOM+";
    
    /** Possible processing modes for main window. */
    public static enum ProcessingMode {
        ANONYMIZE,
        UPLOAD,
        ANONYMIZE_THEN_UPLOAD
    }

    /** String indicating that no PACS were available for uploading to.  If the DICOM service fails
     * to provide a list of PACS, then this is shown and the user is not permitted to upload files.
     */
    private static final String NO_PACS = "<Choose PACS>";

    /** Preferred size of screen. */
    private static final Dimension PREFERRED_SIZE = new Dimension(1000, 800);

    /** Font for small text. */
    public static final Font FONT_TINY = new Font("SansSerif", Font.PLAIN, 10);

    /** Font for normal text and buttons. */
    public static final Font FONT_MEDIUM = new Font("SansSerif", Font.PLAIN, 12);

    /** Large font that makes things stand out. */
    public static final Font FONT_LARGE = new Font("SansSerif", Font.PLAIN, 18);

    /** Large font that makes things stand out. */
    public static final Font FONT_HUGE = new Font("SansSerif", Font.PLAIN, 30);

    /** Italicized version of huge font. */
    public static final Font FONT_HUGE_ITALICS = new Font("SansSerif", Font.ITALIC, 30);

    /** Foreground color for drawing text. */
    public static final Color COLOR_FONT = new Color(80, 80, 80);

    /** Scroll bar increment. */
    private static final int SCROLL_INCREMENT = 12;

    /** Size of mode panel. */
    private static final Dimension MODE_PANEL_DIMENSION = new Dimension(200, 100);

    /** Number of regular files to process. */
    private int fileCount = 0;

    /** Index of currently selected PACS. */
    private int currentPacs = -1;

    /** Displays the anonymizeDestination PACS. */
    private JLabel pacsLabel = null;

    private JLabel uploadCountLabel = null;
    private long uploadCount = 0;

    private JCheckBox koManifest = null;

    /** Selects next PACS choice above. */
    private BasicArrowButton pacsNorth = null;

    /** Selects next PACS choice below. */
    private BasicArrowButton pacsSouth = null;

    /** Button for showing anonymizeGui options. */
    private JButton anonymizeOptionsButton = null;

    /** Button that clears the patient list. */
    private JButton clearButton = null;

    /** Button that shows help. */
    private JButton helpButton = null;

    /** Button that exits the application. */
    private JButton exitButton = null;

    /** Button that processes all series that have been loaded into the program. */
    private JButton processAllButton = null;

    /** Label that shows whether or not all files have been processed. */
    private JLabel processAllIcon = null;

    /** Panel containing the list of patients. */
    private JPanel patientListPanel = null;

    /** Scroll pane containing the list of patients. */
    private JScrollPane patientScrollPane = null;

    /** Label that is shown by default if the application is not initially given
     * DICOM files for uploading.  It is removed when the first DICOM file is loaded.
     * It is meant to be instructional, for users that start the application
     * without really knowing what they are doing.
     */
    private JLabel dragHereLabel = null;

    /** Text that shows the messages (errors) encountered by the user.  Usually these
     * indicate that the user has tried to load a file that was not a DICOM file.
     */
    private JTextArea messageTextArea = null;

    /**
     * Scroll pane around the <code>messageTextArea</code>.
     */
    private JScrollPane messageScrollPane = null;

    /** Field where user enters their login id. */
    private JTextField loginNameTextField = null;

    /** Field where user enters their password. */
    private JPasswordField loginPasswordTextField = null;

    /** Button to attempt login. */
    private JButton loginButton = null;

    /** Contains both the login and PACS panels. */
    private JPanel loginPacsPanel = null;

    /** Allows changing between login and PACS mode. */
    private CardLayout loginPacsCardLayout = null;

    /** Value of most recently set card for <code>loginPacsCardLayout</code>. */
    private String loginPacsCard = CARD_PACS;

    /** Puts the loginPacsCardLayout to login mode. */
    private static final String CARD_LOGIN = "login";

    /** Puts the loginPacsCardLayout to PACS mode. */
    private static final String CARD_PACS = "pacs";

    /** Indicates a login problem. */
    private JLabel loginStatusLabel = null;

    /** True if the application is in command line mode (no GUI) */
    private static boolean commandLineMode = false;

    /** If the application is in command line mode (no GUI) then this is the flag indicating that
     * AttributeTag details should be shown in the text dump. */
    public static boolean showDetails = false;

    /** The default patient ID to use. */
    private static String defaultPatientId = null;

    /** Put single anonymized file here. */
    private static File commandParameterOutputFile = null;

    /** Put multiple anonymized files here. */
    private static File commandParameterOutputDirectory = null;

    
    private JFrame frame = null;

    private JPanel modePanel = null;
    private JRadioButton anonymizeRadioButton = null;
    private JRadioButton uploadRadioButton = null;
    private JRadioButton anonymizeThenUploadRadioButton = null;
    private ButtonGroup modeButtonGroup = null;
    private JTextField anonymizeDestinationText = null;
    private JButton anonymizeDestinationBrowseButton = null;

    /** Chooses directory for anonymized files. */
    private volatile JFileChooser directoryChooser = null;

    /** Destination directory for anonymized files. */
    private volatile File anonymizeDestination = null;


    /** The preview dialog box that shows DICOM files as images or text. */
    private volatile Preview preview = null;

    /** The instance of this class.  This class is effectively used as
     * a singleton.
     */
    private volatile static DicomClient dicomClient = null;

    /** The accumulated (error) messages. */
    private volatile StringBuffer showMessageText = new StringBuffer();

    /** True if it is ok to make the previewer visible. */
    private volatile boolean previewEnableable = true;

    /** If true, shorten attribute names to 32 characters.  Required for SAS interpretation of XML. */ 
    private static boolean restrictXmlTagsToLength32 = false;

    /** If true, replace each control characters in DICOM attribute values with a space.  Required for SAS interpretation of XML. */ 
    private static boolean replaceControlCharacters = false;

    /** If true, activate the <AggressiveAnonymization> tags in configuration file. */ 
    private static boolean aggressivelyAnonymize = false;

    /** Last time that updates were made to the screen. */
    long lastRepaint = 0;


    /**
     * Append a message to the list of messages and show
     * it to the user.
     * 
     * @param message Message to add.
     */
    public void showMessage(String message) {
        messageTextArea.setVisible(true);
        String text = messageTextArea.getText();
        if (showMessageText.length() != 0) {
            showMessageText.append("\n");
        }
        showMessageText.append(message);
        messageTextArea.setText(showMessageText.toString());
        text = messageTextArea.getText();
        if (!inCommandLineMode()) {
            System.out.println("----\n" + text + "\n----");
        }
    }


    /**
     * Get the currently selected AE Title, or return
     * null if there is none.
     * 
     * @return Currently selected AE Title or null.
     */
    public String getSelectedAeTitle() {
        String aeTitle = ((String)(pacsLabel.getText())).trim();
        return aeTitle.equals(NO_PACS) ? null : aeTitle;
    }


    /**
     * Determine if the user has access rights to the PACS list, and if so,
     * then they have rights to upload DICOM files.
     * 
     * @return True if the user has access.
     */
    private boolean checkPacsListAccess() {
        String baseUrl = ClientConfig.getInstance().getServerBaseUrl();

        String url = baseUrl + "/pacs?media_type=" + MediaType.TEXT_XML.getName() + "&user_id=" + loginNameTextField.getText().trim();
        Log.get().info("Getting list of pacs.  URL: " + url);
        Request request = new Request(Method.GET, url);
        setChallengeResponse(request);
        org.restlet.Component component = new org.restlet.Component();
        Context context = component.getContext();
        Client client = new Client(Protocol.HTTP);
        client.setContext(context);
        Response response = client.handle(request);

        if (response.getStatus().getCode() == Status.SUCCESS_OK.getCode()) {
            return true;
        }
        
        if (response.getStatus().getCode() == Status.CLIENT_ERROR_UNAUTHORIZED.getCode()) {
            String msg = response.getEntityAsText();
            if (!msg.startsWith("User")) {
                msg = "User name or password is incorrect.";
            }
            showMessage(msg);
            Log.get().logrb(Level.SEVERE, this.getClass().getCanonicalName(), "getPacsList", null, msg);
            loginStatusLabel.setText("<html><font style='color: red'>" + msg.replaceAll("\\n", "<br>") + "</font></html>");
        }
        else {
            String msg = "Bad response from server at " + url + " to get list of PACS   Error: " + response;
            showMessage(msg);
            Log.get().logrb(Level.SEVERE, this.getClass().getCanonicalName(), "getPacsList", null, msg);
            loginStatusLabel.setText("<html><font style='color: red'>" + msg + "</font></html>");
        }

        return false;
    }
    
    /**
     * Set the challenge scheme, user name, and password in a HTTP request.  The
     * challenge scheme is the Basic HTTP scheme.
     * 
     * @param request Request to set up.
     */
    public void setChallengeResponse(Request request) {
        ChallengeResponse authentication = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, loginNameTextField.getText(), loginPasswordTextField.getPassword());
        request.setChallengeResponse(authentication);
    }


    /**
     * Get the user's login name.
     * 
     * @return The user's login name.
     */
    public String getLoginName() {
        return loginNameTextField.getText();
    }

    /**
     * Set the size of the <code>pacsLabel</code>.
     */
    private void setPacsLabelSize() {
        Graphics graphics = getMainContainer().getGraphics();
        FontMetrics metrics = graphics.getFontMetrics(FONT_HUGE);
        int height = metrics.getHeight();
        int width = metrics.stringWidth(".");
        for (PACS pacs : PACSConfig.getInstance().getPacsList()) {
            int w = metrics.stringWidth(pacs.aeTitle);
            width = (w > width) ? w : width;
        }

        if (!inCommandLineMode()) {
            Dimension dimension = new Dimension(width+8, height+2);
            pacsLabel.setPreferredSize(dimension);
            pacsLabel.invalidate();
            frame.pack();
        }
    }


    /**
     * Build the GUI spinner that controls the list of PACS.
     * 
     * @return The GUI component for viewing and selecting the PACS.
     */
    private JComponent buildPacsSelector() {

        pacsNorth = new BasicArrowButton(BasicArrowButton.NORTH);
        pacsSouth = new BasicArrowButton(BasicArrowButton.SOUTH);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(pacsNorth, BorderLayout.NORTH);
        buttonPanel.add(pacsSouth, BorderLayout.SOUTH);
        pacsNorth.addActionListener(this);
        pacsSouth.addActionListener(this);

        pacsLabel = new JLabel(NO_PACS);
        pacsLabel = new JLabel(NO_PACS, JLabel.CENTER);
        pacsLabel.setFont(FONT_HUGE);
        pacsLabel.setForeground(COLOR_FONT);
        pacsLabel.setVerticalAlignment(JLabel.CENTER);

        JPanel panel = new JPanel();
        //panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(pacsLabel);
        panel.add(buttonPanel);

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));

        JLabel uploadLabel = new JLabel("Upload DICOM files to");
        uploadLabel.setFont(FONT_TINY);
        uploadLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel southPanel = new JPanel();

        uploadCountLabel = new JLabel("Upload Count: 0");
        uploadCountLabel.setToolTipText("<html>Total number of<br>files uploaded.</html>");


        koManifest = new JCheckBox("Manifest");
        koManifest.setToolTipText("<html>If checked, then send a manfest in the form of a DICOM<br>KO preceeding each series to allow the completeness<br>of the series to be verified.</html>");
        koManifest.setSelected(ClientConfig.getInstance().getKoManifestDefault());

        southPanel.add(koManifest);
        southPanel.add(new JLabel("    "));
        southPanel.add(uploadCountLabel);

        outerPanel.add(uploadLabel);
        outerPanel.add(panel);
        outerPanel.add(southPanel);


        outerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        return outerPanel;
    }


    /**
     * Build the panel that collects login information.
     * 
     * @return GUI for login.
     */
    private JComponent buildLoginPanel() {
        String toolTipText = "<html>Use your Level 2 login<br>and password</html>";
        JPanel loginPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(3, 2);
        gridLayout.setVgap(5);
        loginPanel.setLayout(gridLayout);
        loginPanel.setToolTipText(toolTipText);

        JLabel userLabel = new JLabel("Level 2 User:");
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userLabel.setVerticalAlignment(SwingConstants.CENTER);
        loginPanel.add(userLabel);

        loginNameTextField = new JTextField(12);
        loginNameTextField.setText(System.getProperty("user.name").toLowerCase());
        loginNameTextField.addActionListener(this);
        loginNameTextField.setToolTipText(toolTipText);
        loginPanel.add(loginNameTextField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        passwordLabel.setVerticalAlignment(SwingConstants.CENTER);
        loginPanel.add(passwordLabel);

        loginPasswordTextField = new JPasswordField(12);
        loginPasswordTextField.addActionListener(this);
        loginPasswordTextField.setToolTipText(toolTipText);
        loginPanel.add(loginPasswordTextField);

        loginStatusLabel = new JLabel("");
        loginStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        loginStatusLabel.setVerticalAlignment(SwingConstants.CENTER);
        loginPanel.add(new JLabel(""));

        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        JPanel loginButtonPanel = new JPanel();
        loginButtonPanel.add(loginButton);
        loginPanel.add(loginButtonPanel);

        loginPanel.setPreferredSize(MODE_PANEL_DIMENSION);
        loginPanel.setMaximumSize(MODE_PANEL_DIMENSION);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(loginPanel);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        JPanel loginStatusPanel = new JPanel();
        loginStatusPanel.add(loginStatusLabel);
        panel.add(loginStatusPanel);

        return panel;
    }

    private void setLoginPacsCard(String card) {
        loginPacsCard = card;
        loginPacsCardLayout.show(loginPacsPanel, card);
    }
    
    /**
     * Build the northern (upper) part of the dialog that flips between login and PACS mode.
     * 
     * @return Panel containing login and PACS interfaces.
     */
    private JPanel buildUpload() {
        loginPacsPanel = new JPanel();
        loginPacsCardLayout = new CardLayout();
        loginPacsPanel.setLayout(loginPacsCardLayout);

        loginPacsPanel.add(buildLoginPanel(), CARD_LOGIN);
        loginPacsPanel.add(buildPacsSelector(), CARD_PACS);

        String card = (ClientConfig.getInstance().getServerBaseUrl() == null) ? CARD_PACS : CARD_LOGIN;
        setLoginPacsCard(card);

        return loginPacsPanel;
    }


    private JPanel buildAnonymizeDirectorySelector() {
        JLabel label = new JLabel("Destination: ");

        anonymizeDestinationText = new JTextField(40);
        anonymizeDestinationText.setToolTipText("<html>Where anonymized files will be<br>put. Created if necessary</html>");
        anonymizeDestinationText.getDocument().addDocumentListener(this);
        anonymizeDestinationBrowseButton = new JButton("Browse...");
        anonymizeDestinationBrowseButton.addActionListener(this);
        anonymizeDestinationBrowseButton.setToolTipText("<html>Choose a directory for anonymized files.<br>Directory will be created if necessary.</html>");

        directoryChooser = new JFileChooser();

        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (commandParameterOutputDirectory != null) {
            directoryChooser.setSelectedFile(commandParameterOutputDirectory);
        }

        if (commandParameterOutputFile != null) {
            anonymizeDestinationText.setText(commandParameterOutputFile.getAbsolutePath());
            updateDestination();
        }

        JPanel panel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        gridBagLayout.setConstraints(label, c);
        panel.add(label);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        gridBagLayout.setConstraints(anonymizeDestinationText, c);
        panel.add(anonymizeDestinationText);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0;
        gridBagLayout.setConstraints(anonymizeDestinationBrowseButton, c);
        panel.add(new JLabel(" "));

        c.fill = GridBagConstraints.NONE;
        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0;
        gridBagLayout.setConstraints(anonymizeDestinationBrowseButton, c);
        panel.add(anonymizeDestinationBrowseButton);

        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        return panel;
    }


    /**
     * Build panel that contains anonymizeGui directory
     * selector and options button.
     * 
     * @return Anonymize panel.
     */
    /*
    private JPanel buildAnonymize() {
        JPanel anonPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(2, 1);
        gridLayout.setVgap(5);

        anonPanel.setLayout(gridLayout);
        anonPanel.add(buildAnonymizeDirectorySelector());

        //anonPanel.add(buttonPanel);

        JPanel outerPanel = new JPanel();
        outerPanel.add(anonPanel);

        return outerPanel;
    }
    */

    /**
     * Build the panel that contains the mode buttons for
     * selecting whether to be anonymizing or uploading files.
     * 
     * @return Mode selector panel.
     */
    private JPanel buildModeSelector() {
        modeButtonGroup = new ButtonGroup();

        anonymizeRadioButton = new JRadioButton("Anonymize");
        anonymizeRadioButton.setSelected(true);
        anonymizeRadioButton.addActionListener(this);
        anonymizeRadioButton.setToolTipText("<html>Mode for anonymizing<br>DICOM files</html>");
        modeButtonGroup.add(anonymizeRadioButton);

        uploadRadioButton  = new JRadioButton("Upload");
        uploadRadioButton.setSelected(false);
        uploadRadioButton.addActionListener(this);
        uploadRadioButton.setToolTipText("<html>Mode for uploading<br>DICOM files to the PACS<br>of your choice.</html>");
        modeButtonGroup.add(uploadRadioButton);

        anonymizeThenUploadRadioButton = new JRadioButton("Anonymize then Upload");
        anonymizeThenUploadRadioButton.setSelected(false);
        anonymizeThenUploadRadioButton.addActionListener(this);
        anonymizeThenUploadRadioButton.setToolTipText("<html>Anonymize then Upload files</html>");
        modeButtonGroup.add(anonymizeThenUploadRadioButton);
        
        modePanel = new JPanel();
        modePanel.add(anonymizeRadioButton);
        modePanel.add(uploadRadioButton);
        modePanel.add(anonymizeThenUploadRadioButton);
        
        modePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        modePanel.setVisible(ClientConfig.getInstance().getShowUploadCapability());
        return modePanel;
    }

    
    private JPanel buildAnonOptionsButton() {
        anonymizeOptionsButton = new JButton("Anonymize Options...");
        anonymizeOptionsButton.addActionListener(this);
        JPanel panel = new JPanel();
        panel.add(anonymizeOptionsButton);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        return panel;
    }
    
    private JPanel buildModeSelectorAndAnonOptButton() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(buildModeSelector(), BorderLayout.WEST);
        panel.add(buildAnonOptionsButton(), BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return panel;
    }
    

    /**
     * Build north (upper) sub-panel.
     * 
     * @return Panel containing top part of GUI.
     */
    private JPanel buildNorth() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1));
        centerPanel.add(buildModeSelectorAndAnonOptButton());
        centerPanel.add(buildAnonymizeDirectorySelector());

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buildUpload(), BorderLayout.EAST);

        return panel;
    }



    /**
     * Build the central part of the dialog.
     * 
     * @return Component containing the central components.
     */
    private Component buildCenter() {
        JPanel panel = new JPanel();

        patientListPanel = new JPanel();

        BoxLayout patientListLayout = new BoxLayout(patientListPanel, BoxLayout.Y_AXIS);
        patientListPanel.setLayout(patientListLayout);

        dragHereLabel = new JLabel("Drag DICOM files and folders here");
        dragHereLabel = new JLabel("<html><h1> <p> <br> <p> <center>Drag DICOM files and folders here</center></h1></html>");
        dragHereLabel.setFont(FONT_HUGE_ITALICS);
        dragHereLabel.setAlignmentX((float) 0.5);
        dragHereLabel.setAlignmentY((float) 0.5);
        dragHereLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dragHereLabel.setVerticalAlignment(SwingConstants.CENTER);
        patientListPanel.add(dragHereLabel);

        JPanel borderPanel = new JPanel();
        borderPanel.setLayout(new BorderLayout());
        borderPanel.add(patientListPanel, BorderLayout.NORTH);
        borderPanel.add(new JLabel(), BorderLayout.CENTER);
        patientScrollPane = new JScrollPane(borderPanel);
        patientScrollPane.setBorder(null);
        Component parent = patientListPanel.getParent();
        while (parent != null) {
            parent = parent.getParent();
        }

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(patientScrollPane);

        patientScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);

        panel.setBorder(BorderFactory.createLineBorder(COLOR_FONT, 1));
        return panel;
    }


    /**
     * Build the southern (lower) part of the dialog.
     * 
     * @return Component containing the southern components.
     */
    private JPanel buildSouth() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messageTextArea = new JTextArea();
        messageTextArea.setRows(5);
        messageTextArea.setVisible(false);
        messageTextArea.setToolTipText("<html>List of errors.<br>Can be copy-and-pasted.</html>");
        messageScrollPane = new JScrollPane(messageTextArea);
        messageScrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        messageScrollPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        messageTextArea.setFont(FONT_TINY);
        messageTextArea.setEditable(false);
        messageTextArea.setBackground(panel.getBackground());
        panel.add(messageScrollPane);

        // build the buttons
        JPanel buttonPanel = new JPanel();
        FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
        layout.setHgap(30);
        layout.setVgap(30);
        buttonPanel.setLayout(layout);

        exitButton = new JButton("Exit");
        exitButton.setFont(FONT_MEDIUM);
        buttonPanel.add(exitButton);
        exitButton.addActionListener(this);

        helpButton = new JButton("Help");
        helpButton.setFont(FONT_MEDIUM);
        helpButton.addActionListener(this);
        buttonPanel.add(helpButton);

        processAllButton = new JButton("Upload All");
        processAllButton.setFont(FONT_MEDIUM);
        processAllButton.addActionListener(this);
        processAllButton.setToolTipText("<html>Select a PACS server<br>to enable uploading.</html>");
        buttonPanel.add(processAllButton);

        clearButton = new JButton("Clear");
        clearButton.setFont(FONT_MEDIUM);
        buttonPanel.add(clearButton);
        clearButton.addActionListener(this);
        clearButton.setToolTipText("<html>Clear all patients<br>from display</html>");

        processAllIcon = new JLabel(PreDefinedIcons.getEmpty());
        buttonPanel.add(processAllIcon);

        panel.add(buttonPanel);

        return panel;
    }

    /**
     * Get the list of patients.
     * 
     * @return List of patients.
     */
    private ArrayList<Patient> getPatientList() {
        ArrayList<Patient> patientList = new ArrayList<Patient>();
        for (Component component : patientListPanel.getComponents()) {
            if (component instanceof Patient) patientList.add((Patient)component);
        }
        return patientList;
    }
    
    private Patient findPatient(String patientId) {
        for (Patient patient : getPatientList()) if (patient.getPatientId().equals(patientId)) return patient;
        return null;
    }


    /**
     * Recursively set the color of all containers to the same one
     * so that the whole application looks consistent.
     * 
     * @param container
     */
    static public void setColor(Container container) {

        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                setColor((Container)component);
            }
            if (component instanceof JComponent) {
                ((JComponent)component).setForeground(COLOR_FONT);
            }
        }
    }


    public Container getMainContainer() {
        return inCommandLineMode() ? headlessPanel : frame.getContentPane();
    }


    public JFrame getFrame() {
        return inCommandLineMode() ? null : frame;
    }


    private JPanel headlessPanel = null;
    private void buildHeadless() {
        headlessPanel = new JPanel();

        headlessPanel.add(buildNorth(), BorderLayout.NORTH);
        headlessPanel.add(buildCenter(), BorderLayout.CENTER);
        headlessPanel.add(buildSouth(), BorderLayout.SOUTH);

        if (headlessPanel == null) {
            System.err.println("Could not build environment without GUI");
            System.exit(1);
        }
    }
    
    /**
     * Set the enabled status of container and all of its children.
     *  
     * @param container Top level container.
     * 
     * @param enable True to enable, false to disable (diabled == greyed out).
     */
    public static void setEnabledRecursively(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                setEnabledRecursively((Container)component, enable);
            }
        }
    }

    /**
     * Top level that builds the GUI.
     */
    private void buildMain() {
        if (inCommandLineMode()) {
            buildHeadless();
        }
        else {
            frame = new JFrame();
            frame.setTitle(PROJECT_NAME);

            if (OpSys.getOpSysId() == OpSys.OpSysId.WINDOWS) {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                }
                catch (Exception ex) {
                    // ignore any problems setting look and feel
                }
            }

            Container container = frame.getContentPane();

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());

            panel.add(buildNorth(), BorderLayout.NORTH);
            panel.add(buildCenter(), BorderLayout.CENTER);
            panel.add(buildSouth(), BorderLayout.SOUTH);

            container.add(panel);

            new FileDrop(System.out, panel, this);

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setPreferredSize(PREFERRED_SIZE);

            setColor(frame);

            if (!inCommandLineMode()) {
                frame.pack();
                frame.setVisible(true);
            }
            setMode();
        }
    }

    /**
     * Determine if the user has entered a valid user name and password.  If so,
     * show the list of PACS that allows them to upload.
     */
    private void authenticate() {

        boolean authorized = (ClientConfig.getInstance().getServerBaseUrl() == null) || checkPacsListAccess();

        if (authorized) {
            ArrayList<PACS> pacsList = PACSConfig.getInstance().getPacsList();

            if (pacsList != null) {
                loginPacsCardLayout.show(loginPacsPanel, CARD_PACS);
                if (!inCommandLineMode()) {
                    frame.setTitle(PROJECT_NAME + "          User: " + loginNameTextField.getText());
                }
                currentPacs = pacsList.size() - 1;
                pacsLabel.setText(pacsList.get(currentPacs).aeTitle);
            }
        }
    }


    private void resetAnonymizeDestination() {
        anonymizeDestinationText.setText("");
        anonymizeDestination = null;
    }

    
    /**
     * Remove the given patient.
     * 
     * @param patient
     */
    public void clearPatient(Patient patient) {
        if (preview != null) preview.setVisible(false);
        patient.setVisible(false);
        patientListPanel.remove(patient);
        if (patientListPanel.getComponentCount() == 0) {
            resetAnonymizeDestination();
        }
        setProcessedStatus();
    }


    private void clearAll() {
        for (Patient patient : getPatientList()) clearPatient(patient);
        messageTextArea.setText("");
        showMessageText.delete(0, showMessageText.length());
    }


    @Override
    public void actionPerformed(ActionEvent ev) {
        Object source = ev.getSource();

        if (source.equals(exitButton)) {
            System.exit(0);
        }

        if (source.equals(clearButton)) {
            clearAll();
        }

        if (source.equals(helpButton)) {
            new Help(ClientConfig.getInstance().getShowUploadCapability());
        }

        if (source.equals(processAllButton)) {
            Series.processOk = true;
            if (ensureAnonymizeDirectoryExists()) { 
                processAll();
            }
        }

        if (source.equals(pacsNorth) || source.equals(pacsSouth)) {
            ArrayList<PACS> pacsList = PACSConfig.getInstance().getPacsList();
            if (pacsList.size() > 1) {
                setPacsLabelSize();
                int increment = source.equals(pacsNorth) ? -1 : 1;
                currentPacs = (currentPacs + pacsList.size() + increment) % pacsList.size();
                String pacs = pacsList.get(currentPacs).aeTitle;
                pacsLabel.setText(pacs);
                setProcessedStatus();
            }
        }

        if (source.equals(loginButton) || source.equals(loginNameTextField) || source.equals(loginPasswordTextField)) {
            authenticate();
        }

        if (source.equals(anonymizeOptionsButton)) {
            AnonymizeGUI.getInstance().getDialog().setVisible(!inCommandLineMode());
        }

        if (source.equals(anonymizeRadioButton) || source.equals(uploadRadioButton) || source.equals(anonymizeThenUploadRadioButton)) {
            setMode();
            setProcessedStatus();
        }

        if (source.equals(anonymizeDestinationBrowseButton)) {
            updateDestination();
            switch(directoryChooser.showOpenDialog(getMainContainer())) {
                case JFileChooser.APPROVE_OPTION:
                    anonymizeDestination = directoryChooser.getSelectedFile();
                    anonymizeDestinationText.setText(anonymizeDestination.getAbsolutePath());
                    Log.get().info("Destination for anonymized file: " + anonymizeDestination.getAbsolutePath());
                    break;
                case JFileChooser.CANCEL_OPTION:
                    break;
            }
        }

    }

    public PACS getCurrentPacs() {
        return PACSConfig.getInstance().getPacsList().get(currentPacs);
    }

    public boolean ensureAnonymizeDirectoryExists() {
        boolean ok = false;
        if (DicomClient.getInstance().getProcessingMode() != ProcessingMode.UPLOAD) {
            File newDir = getDestinationDirectory();
            if (newDir.isDirectory()) ok = true;
            else {
                newDir.mkdirs();
                ok = newDir.isDirectory();
            }
            if (!ok) {
                String msg =
                    "<html><p>Unable to create anonymization destination<p><br> &nbsp; &nbsp; &nbsp; " + newDir.getAbsolutePath() +
                    "<p><br><p>It is most likely that you do not have<br>permission to write to this directory.</html>";
                new Alert(msg, "Can Not Create Directory");
            }
        }
        else ok = true;
        return ok;
    }



    /**
     * Set the mode to reflect anonymizing, uploading, or anonThenUpload.
     */
    private void setMode() {
        switch (getProcessingMode()) {
        case ANONYMIZE:
            processAllButton.setText("Anonymize All");
            processAllButton.setEnabled(true);
            break;
        case UPLOAD:
            processAllButton.setText("Upload All");
            processAllButton.setEnabled(uploadEnabled());
            break;
        case ANONYMIZE_THEN_UPLOAD:
            processAllButton.setText("Anonymize then Upload All");
            processAllButton.setEnabled(uploadEnabled());
            break;
        }
        setProcessedStatus();
        // For convenience, if the user is switching to upload mode and has not yet authenticated, set
        // the mouse focus to the password field so they can just start typing without having to click
        // on it first.
        if ((getProcessingMode() != ProcessingMode.ANONYMIZE) && (loginPacsCard.equals(CARD_LOGIN))) loginPasswordTextField.grabFocus();
    }



    /**
     * If the destination directory has not already been chosen, then use
     * the given one.
     * 
     * @param directory Directory to make default.
     */
    public void setDefaultDestination(File directory) {
        if (anonymizeDestination == null) {
            anonymizeDestination = directory;
        }
        anonymizeDestinationText.setText(anonymizeDestination.getAbsolutePath());
        directoryChooser.setSelectedFile(anonymizeDestination);
    }



    public synchronized void incrementUploadCount(int size) {
        uploadCount += size;
        uploadCountLabel.setText("Upload Count: " + uploadCount);
        Container container = uploadCountLabel.getParent();
        Graphics graphics = container.getGraphics();
        container.paintAll(graphics);
    }


    /**
     * Find all of the Series and reset their done icons to reflect
     * the proper status.
     * 
     * @param container
     */
    static private boolean setUploadStatusWorker(Container container, boolean all) {
        ProcessingMode mode = getInstance().getProcessingMode();
        for (Component component : container.getComponents()) {
            if (component instanceof Series) {
                all = ((Series)component).setProcessedStatus(mode) && all;
            }
            if (component instanceof Patient) {
                ((Patient)component).setMode(mode);
            }
            if (component instanceof Container) {
                all = setUploadStatusWorker((Container)component, all) && all;
            }
        }
        return all;
    }


    /**
     * Determine if it is ok to upload.
     * 
     * @return True if ok.
     */
    public boolean uploadEnabled() {
        return !pacsLabel.getText().equals(NO_PACS);
    }


    /**
     * Set the upload status (green circle with white plus) associated with the 'Upload All' button.
     */
    public void setProcessedStatus() {
        boolean all = setUploadStatusWorker(getMainContainer(), true) && !(getPatientList().isEmpty());
        if (getProcessingMode() == ProcessingMode.UPLOAD) {
            AnonymizeGUI.getInstance().getDialog().setVisible(false);
        }
        processAllIcon.setIcon(all ? PreDefinedIcons.getOk() : PreDefinedIcons.getEmpty());
        boolean enabled = uploadEnabled() || (getProcessingMode() == ProcessingMode.ANONYMIZE);
        processAllButton.setEnabled(enabled);
        processAllButton.setToolTipText(enabled ? "" : "Select a PACS to enable");
        pacsLabel.setToolTipText(pacsLabel.isEnabled() ? "<html>Files will be uploaded<br>to this PACS</html>" : "<html>Select <b>Upload</b> or<br><b>Anonymize then Upload</b><br>to enable</html>");
        setEnabledRecursively(loginPacsPanel, getProcessingMode() != ProcessingMode.ANONYMIZE);
    }
    
    
    /**
     * Find the series containing one of the files.
     * 
     * @param container
     * @param seriesInstanceUID
     * @return
     */
    public Series findSeries(Container container, ArrayList<File> filesCreated) {
        Series series = null;
        if (container == null) container = getMainContainer();
        for (Component component : container.getComponents()) {
            if (component instanceof Series) {
                series = (Series) component;
                if (series.containsFile(filesCreated.get(0))) return series;
            }
            if (component instanceof Container) {
                series = findSeries((Container) component, filesCreated);
                if (series != null) return series;
            }
        }
        return series;
    }


    /**
     * Get the previewer.
     * 
     * @return The previewer.
     */
    public Preview getPreview() {
        if (preview == null) {
            preview = new Preview();
        }
        return preview;
    }


    @Override
    public void stateChanged(ChangeEvent ev) {
        setProcessedStatus();
    }


    /**
     * Get number of DICOM files specified by user.
     * @return
     */
    public int getFileCount() {
        return fileCount;
    }


    /**
     * Read at a minimum the first portion of the given DICOM file.  The
     * 'portion' is defined to be long enough to get the basic meta-data.
     * 
     * @param fileName
     * 
     * @return The contents of the file
     */
    public AttributeList readDicomFileX(String fileName) {  // TODO should be private (avoid compiler warning)
        AttributeList attributeList = new AttributeList();

        FileInputStream fis = null;
        try {
            if (inCommandLineMode()) {
                // this does not show any annoying messages in the log
                attributeList.read(fileName);
            }
            else {
                // The following is faster than
                // <code>attributeList.read(fileName);</code>, as it only reads
                // the first part of every DICOM file, but
                // it also produces a lot of error messages because of the
                // 'ragged end' of each file.
                // fis = new FileInputStream(new File(fileName));
                // DicomInputStream dis = new DicomInputStream(fis);

                // attributeList.read(dis,DicomClientReadStrategy.dicomClientReadStrategy);
                // attributeList.read(dis);
                attributeList.read(new File(fileName));
            }
        }
        catch (Exception e) {
            // Exceptions do not matter because
            // 1: If reading a partial file, there will always be an exception
            // 2: The content is checked anyway
            Log.get().severe("Error reading DICOM file " + fileName + " : " + e);
        }
        finally {
            if (fis != null) try {
                fis.close();
            }
            catch (Exception e) {
                Log.get().warning("Unable to close stream for file " + fileName);
            }
        }
        return attributeList;
    }

    /**
     * Read at a minimum the first portion of the given DICOM file.  The
     * 'portion' is defined to be long enough to get the basic meta-data.
     * 
     * @param fileName
     * 
     * @return The contents of the file
     */
    private AttributeList readDicomFile(File file) {
        AttributeList attributeList = new AttributeList(); 

        FileInputStream fis = null;
        try {
            if (inCommandLineMode()) {
                // this does not show any annoying messages in the log
                attributeList.read(file);
            }
            else {
                // The following is faster than <code>attributeList.read(fileName);</code>, as it only reads the first part of every DICOM file, but
                // it also produces a lot of error messages because of the 'ragged end' of each file.
                fis = new FileInputStream(file);
                byte[] buffer = new byte[DICOM_METADATA_LENGTH];
                DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(buffer, 0, fis.read(buffer)));
                attributeList.read(dis); // TODO change read to only get what is needed
                // attributeList.read(dis, TagFromName.InstanceNumber); This
                // (0x0020,0x0013) : InstanceNumber
                // does not get:
                //     (0x3006,0x0008) : StructureSetDate
                //     (0x3006,0x0009) : StructureSetTime
                //     (0x300a,0x0006) : RTPlanDate
                //     (0x300a,0x0007) : RTPlanTime
            }
        }
        catch (Exception e) {
            // Exceptions do not matter because
            //     1: If reading a partial file, there will always be an exception
            //     2: The content is checked anyway
            Log.get().severe("Error reading DICOM file " + file.getAbsolutePath() + " : " + e);
        }
        finally {
            if (fis != null) try {
                fis.close();
            }
            catch (Exception e) {
                Log.get().warning("Unable to close stream for file " + file.getAbsolutePath());
            }
        }
        return attributeList;
    }

    /**
     * Determine if an attribute list has a valid SOP instance UID.
     * 
     * @param attributeList
     * 
     * @return True if valid.
     */
    public static boolean hasValidSOPInstanceUID(AttributeList attributeList) {
        Attribute sopInstanceUIDAttr = attributeList.get(TagFromName.SOPInstanceUID);
        if (sopInstanceUIDAttr == null) return false;

        String sopInstanceUID = sopInstanceUIDAttr.getSingleStringValueOrNull();
        if (sopInstanceUID == null) return false;
        if (sopInstanceUID.length() < 10) return false;

        return true;
    }

    private void ensureSOPInstanceUID(File file, AttributeList attributeList) {
        if (!hasValidSOPInstanceUID(attributeList)) {
            String msg = "No valid SOP Instance UID found.  Making random one for DICOM file: " + file.getAbsolutePath();
            try {
                Attribute attribute = AttributeFactory.newAttribute(TagFromName.SOPInstanceUID);
                attribute.addValue(Util.getUID());
                attributeList.put(attribute);
                showMessage(msg);
                if (inCommandLineMode()) {
                    System.err.println(msg);
                }
            }
            catch (DicomException e) {
                ;
            }
        }
    }

    
    /**
     * Add the given file to the list of loaded files.  If the file is not
     * a DICOM file or can not be read, then show a message and ignore it.
     * The file must be readable (read permission) and a valid DICOM file.
     * There are many checks that could be made to validate it as a DICOM
     * file, but the only ones enforced here are that it must specify a
     * patient ID and modality.
     * 
     * @param fileName Name of a DICOM file.
     * 
     * @param descend True if it should descend into subdirectories and process files there.
     */
    public synchronized void addDicomFile(File file, boolean descend) {
        System.out.println();
        try {
            fileCount++;
            setPreviewEnableable(false);
            if (file.isDirectory()) {
                if (descend) {
                    for (File child : file.listFiles()) {
                        addDicomFile(child, false);
                    }
                }
                else {
                    showMessage(file.getAbsolutePath() + " is a directory and is being ignored.");
                }
                return;
            }
            AttributeList attributeList = readDicomFile(file);
            
            if (attributeList.isEmpty()) {
                showMessage(file.getAbsolutePath() + " does not appear to be a DICOM file and is being ignored.");
                return;
            }
            
            ensureSOPInstanceUID(file, attributeList);

            String patientId = "none";
            Attribute patientAttribute = attributeList.get(TagFromName.PatientID);
            if (patientAttribute != null) {
                patientId = patientAttribute.getSingleStringValueOrNull();
                if (patientId == null) {
                    patientId = "none";
                }
            }

            Patient patient = findPatient(patientId);
            if (patient == null) {
                patient = new Patient(file, attributeList, makeNewPatientId());
                patientListPanel.add(patient);
                setColor(patientListPanel);
                JScrollBar scrollBar = patientScrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
            }
            else {
                patient.addStudy(file, attributeList);
            }

            if (dragHereLabel != null) {
                Container parent = dragHereLabel.getParent();
                parent.remove(dragHereLabel);
                dragHereLabel = null;
            }

            // We have a valid DICOM file, so use its directory in determining where to put files.
            File parent = (file.getParentFile() == null) ? new File(".") : file.getParentFile();
            File anonymizedDirectory = new File(file.isDirectory() ? file : parent, "output");

            synchronized (anonymizedDirectory) {
                if (anonymizeDestination == null) {
                    anonymizeDestination = anonymizedDirectory;
                    if (!inCommandLineMode()) {
                        anonymizeDestinationText.setText(anonymizeDestination.getAbsolutePath());
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                directoryChooser.setSelectedFile(anonymizeDestination);
                            }
                        }
                        );
                    }
                }
            }
        }
        catch (Exception e) {
            Log.get().severe("Unexpected error in DicomClient.addDicomFile: " + Log.fmtEx(e));
        }
        finally {
            setPreviewEnableable(true);
            // Need to set color for all of the new Swing components added.
            setColor(getMainContainer());
            setProcessedStatus();

            long now = System.currentTimeMillis();
            if ((now - lastRepaint) > 200) {
                JScrollBar scrollBar = patientScrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
                patientScrollPane.paintAll(patientScrollPane.getGraphics());
                lastRepaint = now;
            }
        }
    }


    @Override
    public void filesDropped(File[] fileList) {
        class Add implements Runnable {
            File[] fileList = null;
            Add(File[] fileList) {
                this.fileList = fileList;
            }

            @Override
            public void run() {
                for (File file : fileList) {
                    addDicomFile(file, true);
                }
            }            
        }
        SwingUtilities.invokeLater(new Thread(new Add(fileList)));
    }


    /**
     * Constructor to build the GUI.
     */
    private DicomClient() {
        if (inCommandLineMode()) {
            buildMain();
        }
        else {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    buildMain();
                }
            });
        }
    }

    
    static public ArrayList<Series> getAllSeries(Container container, ArrayList<Series> seriesList) {
        if (container == null) container = DicomClient.getInstance().getMainContainer();
        if (seriesList == null) seriesList = new ArrayList<Series>();
        for (Component component : container.getComponents()) {
            if (component instanceof Series) {
                seriesList.add((Series)component);
            }
            if (component instanceof Container) {
                getAllSeries((Container)component, seriesList);
            }
        }
        return seriesList;
    }
    

    /**
     * Process all series in the given container to the currently
     * specified PACS.
     * 
     * @param container GUI component to be searched for series.
     */
    static private void processAll() {
        for (Series series : getAllSeries(null, null)) series.processSeries();
        getInstance().setProcessedStatus();
    }


    /**
     * Scroll the main list so that the given series is visible.
     * 
     * @param series
     */
    public static void scrollToSeries(Series series) {
        // Position patientScrollPane so as to show the given series.
    }


    /**
     * Determine what kind processing the user intends to perform.
     * 
     * @return Type of processing.
     */
    public ProcessingMode getProcessingMode() {
        if (anonymizeRadioButton.isSelected())
            return ProcessingMode.ANONYMIZE;
        if (uploadRadioButton.isSelected())
            return ProcessingMode.UPLOAD;
        if (anonymizeThenUploadRadioButton.isSelected())
            return ProcessingMode.ANONYMIZE_THEN_UPLOAD;
        throw new RuntimeException("DicomClient.getProcessingMode: Unexpected processing mode state.");
    }


    /**
     * Get the instance of this class.
     * @return
     */
    public static DicomClient getInstance() {
        if (dicomClient == null) {
            dicomClient = new DicomClient();
        }
        return dicomClient;
    }


    /**
     * Make a new patient ID for anonymizing.
     * 
     * @return A new patient ID.
     */
    private static String makeNewPatientId() {
        String patientId = getDefaultPatientId();
        if (patientId == null) {
            return Anonymize.makeUniquePatientId();
        }

        defaultPatientId = General.increment(patientId);
        return patientId;
    }


    public static boolean inCommandLineMode() {
        return commandLineMode;
    }

    public static String getDefaultPatientId() {
        return defaultPatientId; 
    }

    public File getCommandParameterOutputFile() {
        return commandParameterOutputFile;
    }

    public File getDestinationDirectory() {
        if (commandParameterOutputDirectory != null) {
            directoryChooser.setSelectedFile(commandParameterOutputDirectory);
        }
        return directoryChooser.getSelectedFile();
    }

    /**
     * Check to see if no file exists in the user specified directory with the
     * given prefix and each of the given suffixes.
     * 
     * @param dir
     *            Directory to search.
     * 
     * @param prefix
     *            Base name of file(s).
     * 
     * @param suffixList
     *            Suffix for each file(s).
     * 
     * @return True if none of the files exist, and the prefix may be used to
     *         create new files without overwriting existing files.
     */
    boolean testPrefix(String prefix, ArrayList<String> suffixList) {
        File dir = getDestinationDirectory();
        for (int s = 0; s < suffixList.size(); s++) {
            File file = new File(dir, prefix + suffixList.get(s));
            if (file.exists()) return false;
        }
        return true;
    }

    /**
     * Get an available file prefix to be written to. No file should exist with
     * this prefix and any of the suffixes provided. The file prefix will also
     * represent the content of the attribute list.  The point of this is to be
     * able to create a set of files with the given prefix and suffixes without
     * overwriting any existing files.
     * 
     * @param attributeList
     *            Content that will be written.
     * 
     * @param suffixList
     *            List of suffixes needed. Suffixes are expected be provided
     *            with a leading '.' if desired by the caller.
     * @return A file prefix that, when appended with each of the prefixes, does
     *         not exist in the user specified directory.
     */
    public String getAvailableFilePrefix(AttributeList attributeList, ArrayList<String> suffixList) throws SecurityException {

        String patientIdText = Util.getAttributeValue(attributeList, TagFromName.PatientID);
        String modalityText = Util.getAttributeValue(attributeList, TagFromName.Modality);
        String seriesNumberText = Util.getAttributeValue(attributeList, TagFromName.SeriesNumber);
        String instanceNumberText = Util.getAttributeValue(attributeList, TagFromName.InstanceNumber);

        while ((instanceNumberText != null) && (instanceNumberText.length() < 4)) {
            instanceNumberText = "0" + instanceNumberText;
        }

        String name = "";
        name += (patientIdText == null) ? "" : patientIdText;
        name += (modalityText == null) ? "" : ("_" + modalityText);
        name += (seriesNumberText == null) ? "" : ("_" + seriesNumberText);
        name += (instanceNumberText == null) ? "" : ("_" + instanceNumberText);

        name = name.replace(' ', '_');

        // try the prefix without an extra number to make it unique
        if (testPrefix(name, suffixList)) return name;

        // keep trying different unique numbers until one is found that is not
        // taken
        int count = 1;
        while (true) {
            String uniquifiedName = name + "_" + count;
            if (testPrefix(uniquifiedName, suffixList)) return uniquifiedName;
            count++;
        }

    }

    
    private static void usage(String msg) {
        System.err.println(msg);
        String usage =
            "Usage:\n\n" +
            "    DICOMClient [ -c ] [ -P patient_id ] [ -o output_file ] [ -3 ] [ -z ] [ -g ] inFile1 inFile2 ...\n" + 
            "        -c Run in command line mode (without GUI)\n" +
            "        -P Specify new patient ID for anonymization\n" +
            "        -o Specify output file for anonymization (single file only, command line only)\n" +
            "        -d Specify output directory for anonymization (can not be used with -o option)\n" +
            "        -3 Restrict generated XML to 32 character tag names, as required by the SAS software package\n" +
            "        -t Show attribute tag details in text dump (effective in command line mode only)\n" +
            "        -z Replace each control character in generated XML files that describe DICOM attributes with a blank.  Required by SAS\n" +
            "        -g Perform aggressive anonymization - anonymize fields that are not marked for\n" +
            "           anonymization but contain strings found in fields that are marked for anonymization.\n";
        System.err.println(usage);
        System.exit(1);
    }


    public static boolean getRestrictXmlTagsToLength32() {
        return restrictXmlTagsToLength32;
    }


    public static boolean getReplaceControlCharacters() {
        return replaceControlCharacters;
    }


    public static boolean getAggressivelyAnonymize() {
        return aggressivelyAnonymize;
    }


    public boolean getKoManifestPolicy() {
        return koManifest.isSelected();
    }


    private static String[] parseArguments(String[] args) {
        String[] fileList = new String[0];
        try {
            for (int a = 0; a < args.length; a++) {
                if (args[a].equals("-P")) {
                    a++;
                    defaultPatientId = args[a];
                }
                else {
                    if (args[a].equals("-o")) {
                        a++;
                        commandParameterOutputFile = new File(args[a]);
                        Log.get().info("Output file: " + commandParameterOutputFile.getAbsolutePath());
                    }
                    else {
                        if (args[a].equals("-d")) {
                            a++;
                            commandParameterOutputDirectory = new File(args[a]);
                            Log.get().info("Output directory: " + commandParameterOutputDirectory.getAbsolutePath());
                        }
                        else {
                            if (args[a].equals("-c")) {
                                commandLineMode = true;
                            }
                            else {
                                if (args[a].equals("-t")) {
                                    showDetails = true;
                                }

                                else {
                                    if (args[a].equals("-3")) {
                                        restrictXmlTagsToLength32 = true;
                                    }
                                    else {
                                        if (args[a].equals("-z")) {
                                            replaceControlCharacters = true;
                                        }
                                        else {
                                            if (args[a].equals("-g")) {
                                                aggressivelyAnonymize = true;
                                            }
                                            else {
                                                if (args[a].startsWith("-")) {
                                                    usage("Invalid argument: " + args[a]);
                                                    System.exit(1);
                                                }
                                                else {
                                                    fileList = new String[args.length - a];
                                                    int f = 0;
                                                    for (; a < args.length; a++) {
                                                        fileList[f] = args[a];
                                                        f++;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            usage("Unable to parse command line arguments.");
        }
        if ((commandParameterOutputFile != null) && (commandParameterOutputDirectory != null)) {
            usage("Can not specify both -o and -d options.");
            System.exit(1);
        }
        if ((commandParameterOutputFile != null) && (fileList.length != 1)) {
            usage("Can one specify -o option with exactly one input file.");
            System.exit(1);
        }
        if ((commandParameterOutputFile != null) && (!inCommandLineMode())) {
            usage("Can one specify -o option in command line mode.");
            System.exit(1);
        }
        return fileList;
    }


    private static void logPrelude() {
        Log.get().info("Starting DicomClient at " + new Date());
        Log.get().info("User: " + OpSys.getUser());

        Log.get().info("Build Date: " + Util.getBuildDate());
        Log.get().info("Built by: " + Util.getBuiltBy());
        Log.get().info("Version: " + Util.getImplementationVersion());
        Log.get().info("Organization: " + Util.getImplementationVendor());
    }


    private void setupTrustStore() {
        if (ClientConfig.getInstance().getServerBaseUrl() == null) {
            Log.get().info("No DICOM server URL specified, so Java key store not initialized");
            return;
        }
        ArrayList<File> javaKeyStoreList = ClientConfig.getInstance().getJavaKeyStoreList();

        for (File javaKeyStore : javaKeyStoreList) {
            String fileName = javaKeyStore.getAbsolutePath();
            Log.get().info("Trying java keystore file: " + fileName + " ...");
            if (javaKeyStore.canRead()) {
                System.setProperty("javax.net.ssl.trustStore", fileName);
                System.setProperty("javax.net.ssl.trustStoreType", "JKS");
                Log.get().info("Using java keystore file: " + fileName);
                return;
            }
            else {
                Log.get().info("Unable to read java key store file " + fileName);
            }
        }
        showMessage("Could not find java keystore (*.jks) file to initialize secure communications with server at " + 
                ClientConfig.getInstance().getServerBaseUrl() + " so you will not be able to upload files.");
        return;
    }



    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("DICOM+ starting.  Using jar file: " + new java.io.File(DicomClient.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName());

        {
            System.out.println("DicomPlus-------------------------");
            System.getProperties().list(System.out);
            System.out.println("\nDicomPlus-------------------------");
        }
        try {
            logPrelude();

            args = parseArguments(args);

            // This disables the host name verification for certificate
            // authentication.
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            CustomAttributeList.setDictionary(CustomDictionary.getInstance());

            Anonymize.setTemplate(ClientConfig.getInstance().getAnonPatientIdTemplate());

            DicomClient dicomClient = getInstance();
            dicomClient.setupTrustStore();

            // If in command line mode, then anonymize all files and exit
            // happily
            if (inCommandLineMode()) {
                for (String fileName : args) {
                    dicomClient.addDicomFile(new File(fileName), true);
                }
                Series.processOk = true;
                processAll();
                System.exit(0);
            }
            else {
                File[] fileList = new File[args.length];
                int f = 0;
                for (String fileName : args) {
                    fileList[f] = new File(fileName);
                    f++;
                }
                dicomClient.filesDropped(fileList);
            }
        }
        catch (Exception e) {
            Log.get().severe("Unexpected exception: " + Log.fmtEx(e));
            System.err.println("Unexpected failure.  Stack trace follows:");
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Control whether it is ok to show the previewer.
     * 
     * @param previewEnableable Set to true to enable previewing.
     */
    public void setPreviewEnableable(boolean previewEnableable) {
        this.previewEnableable = previewEnableable;
    }


    /**
     * Determine if it ok to show the previewer.
     * 
     * @return True if ok.
     */
    public boolean isPreviewEnableable() {
        return previewEnableable;
    }


    /**
     * Update the anonymize destination to what the user specified.
     */
    private void updateDestination() {
        anonymizeDestinationText.getText();
        anonymizeDestination = new File(anonymizeDestinationText.getText());
        directoryChooser.setCurrentDirectory(anonymizeDestination);
    }


    @Override
    public void insertUpdate(DocumentEvent e) {
        updateDestination();
    }


    @Override
    public void removeUpdate(DocumentEvent e) {
        updateDestination();
    }


    @Override
    public void changedUpdate(DocumentEvent e) {
        updateDestination();
    }
    
    /**
     * Set the enabled state of the main dialog.
     * 
     * @param enabled True to enable, false to disable.
     */
    public void setEnabled(boolean enabled) {
        setEnabledRecursively(frame.getContentPane(), enabled);
    }

}
