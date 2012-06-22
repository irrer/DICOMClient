package edu.umro.dicom.client;

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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import org.restlet.data.Status;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.TagFromName;

import edu.umro.dicom.common.Anonymize;
import edu.umro.dicom.common.Util;
import edu.umro.util.Log;
import edu.umro.util.UMROException;
import edu.umro.util.Utility;
import edu.umro.util.XML;

/**
 * Main class that shows a GUI to let the user upload DICOM files.
 * 
 * This dialog constructs a list of patients based on the DICOM files
 * specified by the user.  The <code>Patient</code> objects contain <code>Study</code> objects,
 * that in turn contain <code>Series</code> objects.
 *
 * @author irrer
 *
 */
public class DicomClient extends JFrame implements ActionListener, FileDrop.Listener, ChangeListener, DocumentListener {

    /** Default ID. */
    private static final long serialVersionUID = 1L;

    /** Name that appears in title bar of window. */
    private static final String WINDOW_NAME = "DICOM Utility";

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
    public static final Font FONT_HUGE = new Font("SansSerif", Font.PLAIN, 30);

    /** Italicized version of huge font. */
    public static final Font FONT_HUGE_ITALICS = new Font("SansSerif", Font.ITALIC, 30);

    /** Foreground color for drawing text. */
    public static final Color COLOR_FONT = new Color(80, 80, 80);

    /** Tool tip for Upload All button. */
    private static final String UPLOAD_ALL_BUTTON_TOOLTIP_TEXT_ENABLED = "<html>Upload all series<br>listed to the<br>given PACS server.</html>";

    /** Tool tip for Upload All button. */
    private static final String UPLOAD_ALL_BUTTON_TOOLTIP_TEXT_DISABLED = "<html>Select a PACS server<br>to enable uploading.</html>";

    /** Scroll bar increment. */
    private static final int SCROLL_INCREMENT = 12;

    /** Size of mode panel. */
    private static final Dimension MODE_PANEL_DIMENSION = new Dimension(200, 100);

    /** Time in milliseconds to wait for authentication to complete. */
    private static final String AUTHENTICATE_TIMEOUT = "" + (15 * 1000);

    /** List of patients that user has given for possible uploading.  This list
     * is extracted from the DICOM files given by the user.
     */
    private TreeMap<String, Patient> patientList = new TreeMap<String, Patient>();

    /** List of available PACS, retrieved from the DICOM server. */
    private ArrayList<String> pacsList = null;

    /** Index of currently selected PACS. */
    private int currentPacs = -1;

    /** Displays the anonymizeDestination PACS. */
    private JLabel pacsLabel = null;

    /** Selects next PACS choice above. */
    private BasicArrowButton pacsNorth = null;

    /** Selects next PACS choice below. */
    private BasicArrowButton pacsSouth = null;

    /** Button for showing anonymizeGui options. */
    private JButton anonymizeOptionsButton = null;

    /** Button that shows help. */
    private JButton helpButton = null;

    /** Button that exits the application. */
    private JButton exitButton = null;

    /** Button that uploads all series that have been loaded into the program. */
    private JButton uploadAllButton = null;

    /** Label that shows whether or not all files have been uploaded. */
    private JLabel uploadAllIcon = null;

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

    /** Puts the loginPacsCardLayout to login mode. */
    private static final String CARD_LOGIN = "login";

    /** Puts the loginPacsCardLayout to PACS mode. */
    private static final String CARD_PACS = "pacs";

    /** Indicates a login problem. */
    private JLabel loginStatusLabel = null;

    private JPanel modePanel = null;
    private CardLayout modeCardLayout = null;
    private JRadioButton anonymizeRadioButton = null;
    private JRadioButton uploadRadioButton = null;
    private ButtonGroup modeButtonGroup = null;
    private static final String CARD_ANONYMIZE = "anonymizeGui";
    private static final String CARD_UPLOAD = "upload";
    private JTextField anonymizeDestinationText = null;
    private JButton anonymizeDestinationBrowseButton = null;

    /** Chooses directory for anonymized files. */
    private JFileChooser directoryChooser = null;

    /** Destination directory for anonymized files. */
    private File anonymizeDestination = null;


    /** The preview dialog box that shows DICOM files as images or text. */
    private Preview preview = null;

    /** The instance of this class.  This class is effectively used as
     * a singleton.
     */
    private static DicomClient dicomClient = null;

    /** The accumulated (error) messages. */
    private StringBuffer showMessageText = new StringBuffer();

    /** AnonymizeGUI settings. */
    private AnonymizeGUI anonymizeGui = null;

    /** True if it is ok to make the previewer visible. */
    private boolean previewEnableable = true;


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
        System.out.println("----\n" + text + "\n----");
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
     * Parse XML text containing a list of PACS into PACS AETitles.
     * If there is an error, a list containing only the 'no PACS'
     * entry is returned.
     * 
     * @param xml Text containing a list of PACS into PACS AETitles.
     * 
     * @return List of PACS AETitles.
     */
    private ArrayList<String> parsePacsList(String xml) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            Document document = XML.parseToDocument(xml.toString());
            NodeList nodeList = XML.getMultipleNodes(document, "/PacsList/PACS/@AETitle");
            for (int n = 0; n < nodeList.getLength(); n++) {
                String  aeTitle = nodeList.item(n).getNodeValue();
                list.add(aeTitle);
            }
        }
        catch (UMROException ex) {
            showMessage("Error interpreting list of PACS from DICOM ReST server at " + ClientConfig.getInstance().getServerBaseUrl() + " .  It may be down. : " + ex.getMessage());
        }
        catch (ResourceException ex) {
            showMessage("Error requesting list of PACS from DICOM ReST server at " + ClientConfig.getInstance().getServerBaseUrl() + " .  It may be down. : " + ex.getMessage());
        }
        list.add(NO_PACS);
        return list;
    }


    /**
     * Get the list of PACS from the DICOM service.
     * 
     * @return The list of PACS.  If there is a problem, a message
     * is shown and an empty list is returned.
     */
    private ArrayList<String> getPacsList() {
        String baseUrl = ClientConfig.getInstance().getServerBaseUrl();

        String url = baseUrl + "/pacs?media_type=" + MediaType.TEXT_XML.getName() + "&user_id=" + loginNameTextField.getText();
        Log.get().info("Getting list of pacs.  URL: " + url);
        Request request = new Request(Method.GET, url);
        setChallengeResponse(request);
        org.restlet.Component component = new org.restlet.Component();
        Context context = component.getContext();
        Client client = new Client(Protocol.HTTP);
        client.setContext(context);
        Response response = client.handle(request);
        if (response.getStatus().getCode() == Status.SUCCESS_OK.getCode()) {
            try {
                return parsePacsList(Utility.readInputStream(response.getEntity().getStream()));
            }
            catch (IOException e) {
                String msg = "Failed to read input stream from URL " + url + " to get XML formatted list of PACS: " + e;
                showMessage(msg);
                Log.get().logrb(Level.SEVERE, this.getClass().getCanonicalName(), "getPacsList", null, msg);
            }
        }
        else {
            String msg = "Bad response from URL " + url + " to get XML formatted list of PACS: " + response;
            showMessage(msg);
            Log.get().logrb(Level.SEVERE, this.getClass().getCanonicalName(), "getPacsList", null, msg);
        }
        ArrayList<String> list = new ArrayList<String>();
        list.add(NO_PACS);
        return list;
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
        Graphics graphics = this.getGraphics();
        FontMetrics metrics = graphics.getFontMetrics(FONT_HUGE);
        int height = metrics.getHeight();
        int width = metrics.stringWidth(".");
        for (String aet : pacsList) {
            int w = metrics.stringWidth(aet);
            width = (w > width) ? w : width;
        }

        Dimension dimension = new Dimension(width+8, height+2);
        pacsLabel.setPreferredSize(dimension);
        pacsLabel.invalidate();
        pack();
    }

    /**
     * Put the list of PACS into the GUI so that the user
     * can see them and select one.
     */
    private void populatePacsList() {
        pacsList = getPacsList();
        currentPacs = pacsList.size() - 1;
        pacsLabel.setText(pacsList.get(currentPacs));
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
        outerPanel.add(uploadLabel);
        outerPanel.add(panel);
        outerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        return outerPanel;
    }


    /**
     * Build the panel that collects login information.
     * 
     * @return GUI for login.
     */
    private JComponent buildLoginPanel() {
        String toolTipText = "<html>Use your ARIA login<br>and password</html>";
        JPanel loginPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(3, 2);
        gridLayout.setVgap(5);
        loginPanel.setLayout(gridLayout);
        loginPanel.setToolTipText(toolTipText);

        JLabel userLabel = new JLabel("Aria User:");
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

        loginPacsCardLayout.show(loginPacsPanel, CARD_LOGIN);

        return loginPacsPanel;
    }


    private JPanel buildAnonymizeDirectorySelector() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Destination: "));
        anonymizeDestinationText = new JTextField(40);
        anonymizeDestinationText.setToolTipText("<html>Where anonymized files will be<br>put. Created if necessary</html>");
        panel.add(anonymizeDestinationText);
        anonymizeDestinationText.getDocument().addDocumentListener(this);
        anonymizeDestinationBrowseButton = new JButton("Browse...");
        anonymizeDestinationBrowseButton.addActionListener(this);
        anonymizeDestinationBrowseButton.setToolTipText("<html>Choose a directory for anonymized files.<br>Directory will be created if necessary.</html>");

        directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        panel.add(anonymizeDestinationBrowseButton);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        return panel;
    }


    /**
     * Build panel that contains anonymizeGui directory
     * selector and options button.
     * 
     * @return Anonymize panel.
     */
    private JPanel buildAnonymize() {
        JPanel anonPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(2, 1);
        gridLayout.setVgap(5);

        anonPanel.setLayout(gridLayout);
        anonPanel.add(buildAnonymizeDirectorySelector());

        anonymizeOptionsButton = new JButton("Anonymize Options...");
        anonymizeOptionsButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(anonymizeOptionsButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        anonPanel.add(buttonPanel);

        JPanel outerPanel = new JPanel();
        outerPanel.add(anonPanel);

        return outerPanel;
    }


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

        JPanel modePanel = new JPanel();
        modePanel.add(anonymizeRadioButton);
        modePanel.add(uploadRadioButton);

        modePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        modePanel.setVisible(ClientConfig.getInstance().getShowUploadCapability());
        return modePanel;
    }


    /**
     * Build the panel that controls whether the user
     * is in anonymizeGui or upload mode. 
     * 
     * @return
     */
    private JPanel buildModeCard() {
        modePanel = new JPanel();
        modeCardLayout = new CardLayout();
        modePanel.setLayout(modeCardLayout);
        modePanel.add(buildAnonymize(), CARD_ANONYMIZE);
        modePanel.add(buildUpload(), CARD_UPLOAD);
        modeCardLayout.show(modePanel, CARD_ANONYMIZE);
        return modePanel;
    }


    /**
     * Build panel containing each operating mode.
     * 
     * @return Panel containing mode GUI.
     */
    private JPanel buildMode() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(buildModeSelector(), BorderLayout.NORTH);
        panel.add(buildModeCard(), BorderLayout.CENTER);

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
            System.out.println("    parent: " + parent);
            parent = parent.getParent();
        }
        System.out.println("    scrollPane: " + patientScrollPane);
        System.out.println("    panel: " + panel);

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

        uploadAllButton = new JButton("Upload All");
        uploadAllButton.setFont(FONT_MEDIUM);
        uploadAllButton.addActionListener(this);
        uploadAllButton.setToolTipText(UPLOAD_ALL_BUTTON_TOOLTIP_TEXT_DISABLED);
        buttonPanel.add(uploadAllButton);

        uploadAllIcon = new JLabel(PreDefinedIcons.getEmpty());
        buttonPanel.add(uploadAllIcon);

        panel.add(buttonPanel);

        return panel;
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


    /**
     * Top level that builds the GUI.
     */
    private void buildMain() {
        setTitle(WINDOW_NAME);

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch (Exception ex) {
            // ignore any problems setting look and feel
        }

        Container container = getContentPane();

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(buildMode(), BorderLayout.NORTH);
        panel.add(buildCenter(), BorderLayout.CENTER);
        panel.add(buildSouth(), BorderLayout.SOUTH);

        container.add(panel);

        new FileDrop(System.out, panel, this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(PREFERRED_SIZE);

        setColor(this);

        pack();
        setVisible(true);
        setMode();
    }


    // --------------------------------------------------------------------------------


    /**
     * Determine if the user has entered a valid user name and password.  If so,
     * show the list of PACS that allows them to upload.
     */
    private void authenticate() {

        boolean authenticated = false;
        loginStatusLabel.setText("");
        Reference reference = new Reference(ClientConfig.getInstance().getServerBaseUrl());
        org.restlet.Component component = new org.restlet.Component();
        Context context = component.getContext();
        Client client = new Client(Protocol.HTTPS);
        client.setContext(context);
        context.getParameters().add(new Parameter("readTimeout", AUTHENTICATE_TIMEOUT));

        Request request = new Request(Method.GET, reference);
        setChallengeResponse(request);
        Response response = client.handle(request);
        authenticated = Status.isSuccess(response.getStatus().getCode());

        Log.get().info("authenticateResponse for user " + loginNameTextField.getText() + " : " + authenticated);

        if (authenticated) {
            loginPacsCardLayout.show(loginPacsPanel, CARD_PACS);
            setTitle(WINDOW_NAME + "          User: " + loginNameTextField.getText());
            populatePacsList();
            setPacsLabelSize();
        }
        else {
            String msg = ClientConfig.getInstance().getServerBaseUrl() + " : " + response.getStatus().toString();
            int code = response.getStatus().getCode();
            if (code == Status.CLIENT_ERROR_UNAUTHORIZED.getCode()) {
                msg = "ARIA user or password is incorrect.";
            }
            loginStatusLabel.setText("<html><font style='color: red'>" + msg + "</font></html>");
        }
    }


    @Override
    public void actionPerformed(ActionEvent ev) {
        Object source = ev.getSource();

        if (source.equals(exitButton)) {
            System.exit(0);
        }

        if (source.equals(helpButton)) {
            new Help(ClientConfig.getInstance().getShowUploadCapability());
        }

        if (source.equals(uploadAllButton)) {
            processAll(this);
        }

        if (source.equals(pacsNorth) || source.equals(pacsSouth)) {
            if (pacsList.size() > 1) {
                int increment = source.equals(pacsNorth) ? -1 : 1;
                currentPacs = (currentPacs + pacsList.size() + increment) % pacsList.size();
                String pacs = pacsList.get(currentPacs);
                pacsLabel.setText(pacs);
                setProcessedStatus();
            }
        }

        if (source.equals(loginButton) || source.equals(loginNameTextField) || source.equals(loginPasswordTextField)) {
            authenticate();
        }

        if (source.equals(anonymizeOptionsButton)) {
            getAnonymizeGui().setVisible(true);
        }

        if (source.equals(anonymizeRadioButton) || source.equals(uploadRadioButton)) {
            setMode();
        }

        if (source.equals(anonymizeDestinationBrowseButton)) {
            updateDestination();
            switch(directoryChooser.showOpenDialog(this)) {
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


    /**
     * Set the mode to reflect anonymizing or uploading.
     */
    private void setMode() {
        modeCardLayout.show(modePanel, getAnonymizeMode() ? CARD_ANONYMIZE : CARD_UPLOAD);
        uploadAllButton.setText(getAnonymizeMode() ? "Anonymize All" : "Upload All");
        setProcessedStatus();
    }


    /**
     * Get the destination directory.  If null, then the
     * user has not yet chosen one.
     * 
     * @return The destination directory.
     */
    public File getDestination() {
        if (anonymizeDestination == null) {
            anonymizeDestination = directoryChooser.getSelectedFile();
        }
        return anonymizeDestination;
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





    /**
     * Get the instance of the anonymizeGui GUI.
     * 
     * @return The instance of the anonymizeGui GUI.
     */
    public AnonymizeGUI getAnonymizeGui() {
        if (anonymizeGui == null) {
            anonymizeGui = new AnonymizeGUI();
        }
        return anonymizeGui;
    }


    /**
     * Find all of the Series and reset their done icons to reflect
     * the proper status.
     * 
     * @param container
     */
    static private boolean setUploadStatusWorker(Container container, boolean all) {
        for (Component component : container.getComponents()) {
            if (component instanceof Series) {
                all = ((Series)component).setProcessedStatus() && all;
            }
            if (component instanceof Patient) {
                ((Patient)component).setMode();
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
        boolean all = setUploadStatusWorker(this, true) && !(patientList.isEmpty());
        if (!getAnonymizeMode()) {
            getAnonymizeGui().setVisible(false);
        }
        uploadAllIcon.setIcon(all ? PreDefinedIcons.getOk() : PreDefinedIcons.getEmpty());
        uploadAllButton.setEnabled(uploadEnabled() || getAnonymizeMode());
        uploadAllButton.setToolTipText(uploadEnabled() ? UPLOAD_ALL_BUTTON_TOOLTIP_TEXT_ENABLED : UPLOAD_ALL_BUTTON_TOOLTIP_TEXT_DISABLED);
        pacsLabel.setToolTipText(uploadEnabled() ? "<html>Files will be uploaded<br>to this PACS</html>" : "<html>Select a PACS to<br>enable upload buttons</html>");
        loginPacsPanel.setVisible(!getAnonymizeMode());
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
     * Add the given file to the list of loaded files.  If the file is not
     * a DICOM file or can not be read, then show a message and ignore it.
     * The file must be readable (read permission) and a valid DICOM file.
     * There are many checks that could be made to validate it as a DICOM
     * file, but the only ones enforced here are that it must specify a
     * patient ID and modality.
     * 
     * @param fileName Name of a DICOM file.
     */
    private void addDicomFile(File file) {
        try {
            setPreviewEnableable(false);
            String fileName = file.getAbsolutePath();
            if (new File(fileName).isDirectory()) {
                showMessage(new File(fileName).getAbsolutePath() + " is a directory and is being ignored.");
                return;
            }
            AttributeList attributeList = new AttributeList();
            try {
                attributeList.read(fileName);
            }
            catch (IOException ex) {
                showMessage("Unable to read file " + fileName + " : " + ex.getMessage());
                return;
            }
            catch (DicomException ex) {
                showMessage("Error interpreting file " + fileName + " as DICOM : " + ex.getMessage());
                return;
            }

            String patientId = "none";
            Attribute patientAttribute = attributeList.get(TagFromName.PatientID);
            if (patientAttribute != null) {
                patientId = patientAttribute.getSingleStringValueOrNull();
                if (patientId == null) {
                    patientId = "none";
                }
            }

            // There should be at least 3 attributes to be considered valid DICOM
            if (attributeList.size() < 4) {
                showMessage("Invalid DICOM file.  No Media Storage SOP Class UID value for file " + fileName);
                return;
            }

            Patient patient = patientList.get(patientId);
            if (patient == null) {
                patient = new Patient(fileName, attributeList);
                patientList.put(patientId, patient);
                patientListPanel.add(patient);
                setColor(patientListPanel);
            }
            else {
                patient.addStudy(fileName, attributeList);
            }
            if (dragHereLabel != null) {
                Container parent = dragHereLabel.getParent();
                parent.remove(dragHereLabel);
                dragHereLabel = null;
            }

            // We have a valid DICOM file, so use its directory in determining where to put files.
            File anonymizedDirectory = new File(file.isDirectory() ? file : file.getParentFile(), "anonymized");
            setDefaultDestination(anonymizedDirectory);
        }
        finally {
            setPreviewEnableable(true);
        }
    }





    /**
     * Take a file specification from the user and add it to
     * the list of loaded files.  If a regular file is given,
     * then load all files in the same directory.  If a directory
     * is given, then load all files in it.
     * 
     * Files are generally given as the command line arguments
     * or dragged onto the application.
     * 
     * @param fileName File name given by user.
     */
    private void addFile(String fileName) {
        Log.get().info("Processing file: " + fileName + " ...");
        File file = new File(fileName);

        if (!file.exists()) {
            showMessage("File " + fileName + " does not exist.");
            return;
        }
        if (!file.canRead()) {
            showMessage("Do not have permission to read " + fileName);
            return;
        }
        if (file.isDirectory()) {
            String[] fileNameList = file.list();
            for (String fn : fileNameList) {
                File childFile = new File(file, fn);
                addDicomFile(childFile);
            }
        }
        else {
            addFile(file.getParentFile().getAbsolutePath());
        }

        // Need to set color for all of the new Swing components added.
        setColor(this);
        setProcessedStatus();
    }


    @Override
    public void filesDropped(File[] fileList) {
        for (File file : fileList) {
            addFile(file.getAbsolutePath());
        }
    }


    /**
     * Constructor to build the GUI.
     */
    private DicomClient() {
        buildMain();
    }


    /**
     * Process all series in the given container to the currently
     * specified PACS.
     * 
     * @param container GUI component to be searched for series.
     */
    static private void processAll(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof Series) {
                ((Series)component).processSeries();
            }
            if (component instanceof Container) {
                processAll((Container)component);
            }
        }
        getInstance().setProcessedStatus();
    }


    /**
     * Scroll the main list so that the given series is visible.
     * @param series
     */
    public static void scrollToSeries(Series series) {
        // Position patientScrollPane so as to show the given series.
    }


    /**
     * Determine if we are in anonymizeGui mode.
     * 
     * @return True if anonymizeGui, false if upload.
     */
    public boolean getAnonymizeMode() {
        return anonymizeRadioButton.isSelected();
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
     * @param args
     */
    public static void main(String[] args) {
        try {
            // print one line right away to indicate that the program started.
            System.out.println("Starting DicomClient at " + new Date());

            Log.get().info("Build Date: " + Util.getBuildDate());
            Log.get().info("Built by: " + Util.getBuiltBy());
            Log.get().info("Version: " + Util.getImplementationVersion());
            Log.get().info("Organization: " + Util.getImplementationVendor());

            // This disables the host name verification for certificate authentication.
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            ClientConfig.getInstance().setupTrustStore();
            
            Anonymize.setTemplate(ClientConfig.getInstance().getAnonPatientIdTemplate());
            Anonymize.setRootGuid(ClientConfig.getInstance().getRootGuid());

            DicomClient dicomClient = getInstance();
            for (String fileName : args) {
                dicomClient.addFile(fileName);
            }
        }
        catch (Exception ex) {
            Log.get().severe("Unexpected exception: " + ex);
            System.err.println("Unexpected failure.  Stack trace follows:");
            ex.printStackTrace();
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

}
