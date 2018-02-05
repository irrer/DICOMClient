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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

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
import javax.swing.plaf.basic.BasicArrowButton;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.TagFromName;

import edu.umro.dicom.client.AnonymizeDateTime.DateMode;
import edu.umro.util.Exec;
import edu.umro.util.Log;
import edu.umro.util.OpSys;
import edu.umro.util.General;

/**
 * Main class that shows a GUI to let the user upload DICOM files.
 * 
 * This dialog constructs a list of patients based on the DICOM files
 * specified by the user. The <code>Patient</code> objects contain <code>Study</code> objects,
 * that in turn contain <code>Series</code> objects.
 * 
 * @author Jim Irrer irrer@umich.edu
 * 
 */
public class DicomClient implements ActionListener, FileDrop.Listener, ChangeListener {

    /** Possible processing modes for main window. */
    public static enum ProcessingMode {
        ANONYMIZE, ANONYMIZE_THEN_LOAD, UPLOAD, ANONYMIZE_THEN_UPLOAD
    }

    /** Possible strategies for storing files. */
    public static enum OutputFileOrganization {
        FLAT, // Put all files in one directory as specified by the destination
        TREE, // Put all files in one directory as specified by the destination but organize them by Patient ID and then
              // series
        LOCAL // Put all files in one directory as specified by the destination put them in an output directory under
              // the directory of the input file
    }

    /** A DICOM file must have at least this many attributes to be considered a DICOM file. */
    public static final int MIN_ATTRIBUTE_COUNT = 2;

    /**
     * String indicating that no PACS were available for uploading to. If the DICOM service fails
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
    public static final Font FONT_HUGE = new Font("SansSerif", Font.PLAIN, 20);

    /** Italicized version of huge font. */
    public static final Font FONT_HUGE_ITALICS = new Font("SansSerif", Font.ITALIC, 30);

    /** Foreground color for drawing text. */
    public static final Color COLOR_FONT = new Color(80, 80, 80);

    /** Scroll bar increment. */
    private static final int SCROLL_INCREMENT = 12;

    /** Number of regular files to process. */
    private int fileCount = 0;

    /** Index of currently selected PACS. */
    private int currentPacs = -1;

    /** Displays the anonymizeDestination PACS. */
    private JLabel pacsLabel = null;

    private long uploadCount = 0;

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

    /**
     * Information that is shown by default if the application is not initially given
     * DICOM files for uploading. It is removed when the first DICOM file is loaded.
     * It is meant to be instructional, for users that start the application
     * without really knowing what they are doing.
     */
    private JComponent dragHereTarget = null;

    /**
     * Text that shows the messages (errors) encountered by the user. Usually these
     * indicate that the user has tried to load a file that was not a DICOM file.
     */
    private JTextArea messageTextArea = null;

    /**
     * Scroll pane around the <code>messageTextArea</code>.
     */
    private JScrollPane messageScrollPane = null;

    /** Field where user enters their login id. */
    private JTextField loginNameTextField = null;

    /** True if the application is in command line mode (no GUI) */
    private static boolean commandLineMode = false;

    /**
     * True if, when writing output files, they should be put in a tree with patient ID as directory containing series
     * directories.
     */
    private static OutputFileOrganization outputOrgMode = OutputFileOrganization.FLAT;

    /** True if, when reading files, they subdirectories should be recursively searched. */
    private static boolean searchSubdirs = false;

    /**
     * If the application is in command line mode (no GUI) then this is the flag indicating that
     * AttributeTag details should be shown in the text dump.
     */
    public static boolean showDetails = false;

    /** The default patient ID to use. */
    private static String defaultPatientId = null;

    /** Put single anonymized file here. */
    private static File commandParameterOutputFile = null;

    /** Put multiple anonymized files here. */
    private static File commandParameterOutputDirectory = null;

    /** Once the user specified where files are to be put, do not use defaults to set the output directory. */
    private volatile static boolean hasSpecifiedOutputDirectory = false;

    private JFrame frame = null;

    private JPanel modePanel = null;
    private JRadioButton anonymizeRadioButton = null;
    private JRadioButton anonymizeThenLoadRadioButton = null;
    private JRadioButton uploadRadioButton = null;
    private JRadioButton anonymizeThenUploadRadioButton = null;
    private ButtonGroup modeButtonGroup = null;
    private JLabel anonymizeDestinationText = null;
    private JButton anonymizeDestinationBrowseButton = null;
    private JCheckBox searchSubdirsCheckbox = null;

    private JRadioButton outputOrgFlat = null;
    private JRadioButton outputOrgTree = null;
    private JRadioButton outputOrgLocal = null;
    private ButtonGroup outputOrgGroup = null;

    /** Chooses directory for anonymized files. */
    private volatile JFileChooser directoryChooser = null;

    /** The preview dialog box that shows DICOM files as images or text. */
    private volatile Preview preview = null;

    private JLabel loadedStatisticsLabel = new JLabel("");
    private JLabel processedStatisticsLabel = new JLabel("");

    /**
     * The instance of this class. This class is effectively used as
     * a singleton.
     */
    private volatile static DicomClient dicomClient = null;

    /** The accumulated (error) messages. */
    private volatile StringBuffer showMessageText = new StringBuffer();

    /** True if it is ok to make the previewer visible. */
    private volatile boolean previewEnableable = true;

    /** If true, shorten attribute names to 32 characters. Required for SAS interpretation of XML. */
    private static boolean restrictXmlTagsToLength32 = false;

    /**
     * If true, replace each control characters in DICOM attribute values with a space. Required for SAS interpretation
     * of XML.
     */
    private static boolean replaceControlCharacters = false;

    /** If true, activate the <AggressiveAnonymization> tags in configuration file. */
    private static boolean aggressivelyAnonymize = false;

    /** Last time that updates were made to the screen. */
    long lastRepaint = 0;

    /**
     * Append a message to the list of messages and show
     * it to the user.
     * 
     * @param message
     *            Message to add.
     */
    public void showMessage(String message) {
        if (!inCommandLineMode()) {
            messageTextArea.setVisible(true);
            if (showMessageText.length() != 0) {
                showMessageText.append("\n");
            }
            showMessageText.append(message);
            messageTextArea.setText(showMessageText.toString());
        }
        Log.get().info("User message: " + message);
    }

    /**
     * If user does not specify an output directory, then assume one.
     */
    private File defaultOutputDirectory() {
        File dir = new File(System.getProperty("user.home"));
        return new File(dir, "output");
    }

    /**
     * Get the currently selected AE Title, or return
     * null if there is none.
     * 
     * @return Currently selected AE Title or null.
     */
    public String getSelectedAeTitle() {
        String aeTitle = ((String) (pacsLabel.getText())).trim();
        return aeTitle.equals(NO_PACS) ? null : aeTitle;
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
        int width = metrics.stringWidth(NO_PACS);
        for (PACS pacs : PACSConfig.getInstance().getPacsList()) {
            int w = metrics.stringWidth(pacs.aeTitle);
            width = (w > width) ? w : width;
        }

        if (!inCommandLineMode()) {
            Dimension dimension = new Dimension(width + 8, height + 2);
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
        panel.setLayout(new BorderLayout());
        panel.add(pacsLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 50));
        return panel;
    }

    private JComponent buildNorthEast() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(buildPacsSelector(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildOutputOrg() {

        JPanel panel = new JPanel();
        outputOrgGroup = new ButtonGroup();

        outputOrgFlat = new JRadioButton("Flat");
        outputOrgFlat.setSelected(true);
        outputOrgFlat.addActionListener(this);
        outputOrgFlat.setToolTipText("<html>Store created files into the same directory</html>");
        outputOrgGroup.add(outputOrgFlat);
        panel.add(outputOrgFlat);

        outputOrgTree = new JRadioButton("Tree");
        outputOrgTree.setSelected(false);
        outputOrgTree.addActionListener(this);
        outputOrgTree.setToolTipText("<html>Store created files in patient ID / series<br/>tree under specified directory</html>");
        outputOrgGroup.add(outputOrgTree);
        panel.add(outputOrgTree);

        outputOrgLocal = new JRadioButton("Local");
        outputOrgLocal.setSelected(false);
        outputOrgLocal.addActionListener(this);
        outputOrgLocal
                .setToolTipText("<html>Store created files in local directory as a child of their source<br/>directory.  Requires write access to source directories.</html>");
        outputOrgGroup.add(outputOrgLocal);
        panel.add(outputOrgLocal);

        return panel;
    }

    /**
     * Build the northern (upper) part of the dialog that flips between login and PACS mode.
     * 
     * @return Panel containing login and PACS interfaces.
     * 
     *         private JPanel buildUpload() {
     *         loginPacsPanel = new JPanel();
     *         loginPacsCardLayout = new CardLayout();
     *         loginPacsPanel.setLayout(loginPacsCardLayout);
     * 
     *         loginPacsPanel.add(buildLoginPanel(), CARD_LOGIN);
     *         loginPacsPanel.add(buildPacsSelector(), CARD_PACS);
     * 
     *         String card = (ClientConfig.getInstance().getServerBaseUrl() == null) ? CARD_PACS : CARD_LOGIN;
     *         setLoginPacsCard(card);
     * 
     *         return loginPacsPanel;
     *         }
     */
    private JPanel buildOutputDirectorySelector() {
        String toolTip = "<html>Where anonymized files will be<br>put. Created if necessary</html>";

        JLabel destinationLabel = new JLabel("Destination: ");
        destinationLabel.setToolTipText(toolTip);

        anonymizeDestinationText = new JLabel("");
        anonymizeDestinationText.setToolTipText(toolTip);

        anonymizeDestinationBrowseButton = new JButton("Browse...");
        anonymizeDestinationBrowseButton.addActionListener(this);
        anonymizeDestinationBrowseButton.setToolTipText("<html>Choose a directory for anonymized and edited files.<br>Directory will be created if necessary.</html>");

        directoryChooser = new JFileChooser();

        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (commandParameterOutputDirectory != null) {
            directoryChooser.setSelectedFile(commandParameterOutputDirectory);
            setAnonymizeDestinationText(commandParameterOutputDirectory.getAbsolutePath());
        }

        if (commandParameterOutputFile != null) {
            directoryChooser.setSelectedFile(commandParameterOutputFile.getParentFile());
            setAnonymizeDestinationText(commandParameterOutputFile.getAbsolutePath());
        }

        JPanel panel = new JPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        gridBagLayout.setConstraints(destinationLabel, c);
        panel.add(destinationLabel);

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

        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0;
        JPanel outOrg = buildOutputOrg();
        gridBagLayout.setConstraints(outOrg, c);
        panel.add(outOrg);

        // panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        return panel;
    }

    /**
     * Build panel that contains anonymizeGui directory
     * selector and options button.
     * 
     * @return Anonymize panel.
     */
    /*
     * private JPanel buildAnonymize() {
     * JPanel anonPanel = new JPanel();
     * GridLayout gridLayout = new GridLayout(2, 1);
     * gridLayout.setVgap(5);
     * 
     * anonPanel.setLayout(gridLayout);
     * anonPanel.add(buildAnonymizeDirectorySelector());
     * 
     * //anonPanel.add(buttonPanel);
     * 
     * JPanel outerPanel = new JPanel();
     * outerPanel.add(anonPanel);
     * 
     * return outerPanel;
     * }
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

        anonymizeThenLoadRadioButton = new JRadioButton("Anonymize then Load");
        anonymizeThenLoadRadioButton.setSelected(true);
        anonymizeThenLoadRadioButton.addActionListener(this);
        anonymizeThenLoadRadioButton.setToolTipText("<html>Mode for anonymizing then automatically<br>loading DICOM files</html>");
        modeButtonGroup.add(anonymizeThenLoadRadioButton);

        uploadRadioButton = new JRadioButton("Upload");
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
        modePanel.add(anonymizeThenLoadRadioButton);
        modePanel.add(uploadRadioButton);
        modePanel.add(anonymizeThenUploadRadioButton);

        if (ClientConfig.getInstance().getAnonymizingReplacementList().size() == 0) {
            anonymizeRadioButton.setEnabled(false);
            anonymizeThenLoadRadioButton.setEnabled(false);
            anonymizeThenUploadRadioButton.setEnabled(false);
            uploadRadioButton.setSelected(true);
        }

        modePanel.setVisible(ClientConfig.getInstance().getShowUploadCapability());
        return modePanel;
    }

    private JPanel buildAnonOptionsButtonPanel() {
        anonymizeOptionsButton = new JButton("Anonymize Options...");
        anonymizeOptionsButton.addActionListener(this);
        anonymizeOptionsButton.setToolTipText("Control which DICOM fields will be anonymized.");
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(anonymizeOptionsButton, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSearchSubdirsPanel() {
        searchSubdirsCheckbox = new JCheckBox("Search Subdirs");
        searchSubdirsCheckbox.addActionListener(this);
        searchSubdirsCheckbox.setToolTipText("When reading files, recursively search sub-directories.");
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(searchSubdirsCheckbox, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLoadStatisticsPanel() {
        continuallyUpdateStatistics();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        loadedStatisticsLabel.setFont(DicomClient.FONT_MEDIUM);
        loadedStatisticsLabel.setToolTipText("Summary of loaded files.");
        panel.add(loadedStatisticsLabel, BorderLayout.WEST);

        return panel;
    }

    private JPanel buildProcessedStatisticsPanel() {
        JPanel panel = new JPanel();
        processedStatisticsLabel.setFont(DicomClient.FONT_MEDIUM);
        processedStatisticsLabel.setToolTipText("Summary of processed files.");
        processedStatisticsLabel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));
        panel.add(processedStatisticsLabel);
        return panel;
    }

    /**
     * Build north (upper) sub-panel.
     * 
     * @return Panel containing top part of GUI.
     */
    private JPanel buildNorth() {
        JPanel panel = new JPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        GridBagConstraints c = new GridBagConstraints();

        {
            c.fill = GridBagConstraints.NONE;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 0;
            c.insets = new Insets(15, 10, 0, 0);
            c.anchor = GridBagConstraints.LINE_START;
            JComponent jc = buildModeSelector();
            gridBagLayout.setConstraints(jc, c);
            panel.add(jc);
        }

        {
            c.fill = GridBagConstraints.NONE;
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 0;
            c.insets = new Insets(0, 20, 0, 0);
            c.anchor = GridBagConstraints.LINE_START;
            JComponent jc = buildLoadStatisticsPanel();
            gridBagLayout.setConstraints(jc, c);
            panel.add(jc);
        }

        {
            c.fill = GridBagConstraints.NONE;
            c.gridx = 0;
            c.gridy = 2;
            c.weightx = 0;
            c.insets = new Insets(0, 20, 0, 0);
            c.anchor = GridBagConstraints.LINE_START;
            JComponent jc = buildOutputDirectorySelector();
            gridBagLayout.setConstraints(jc, c);
            panel.add(jc);
            c.weightx = 0;
        }

        // ------------------------------------------------------------

        {
            c.fill = GridBagConstraints.NONE;
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 1;
            c.insets = new Insets(20, 20, 0, 0);
            c.anchor = GridBagConstraints.CENTER;
            c.gridheight = 1;
            c.gridwidth = 1;
            // JComponent jc = buildPacsSelector();
            JComponent jc = buildNorthEast();
            gridBagLayout.setConstraints(jc, c);
            panel.add(jc);
        }

        {
            c.fill = GridBagConstraints.NONE;
            c.gridx = 1;
            c.gridy = 1;
            c.weightx = 0;
            c.insets = new Insets(20, 20, 0, 0);
            c.anchor = GridBagConstraints.CENTER;
            JPanel anonSearchPanel = new JPanel();
            anonSearchPanel.add(buildAnonOptionsButtonPanel());
            anonSearchPanel.add(new JLabel("    "));
            anonSearchPanel.add(buildSearchSubdirsPanel());

            gridBagLayout.setConstraints(anonSearchPanel, c);
            panel.add(anonSearchPanel);
        }

        {
            c.fill = GridBagConstraints.NONE;
            c.gridx = 1;
            c.gridy = 2;
            c.weightx = 0;
            c.insets = new Insets(20, 20, 15, 0);
            c.anchor = GridBagConstraints.CENTER;
            JComponent jc = buildProcessedStatisticsPanel();
            gridBagLayout.setConstraints(jc, c);
            panel.add(jc);
        }

        return panel;
    }

    private JComponent buildDragAndDropTarget() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel dragLabel = new JLabel("<html><h1> <p> <br> <p>Drag DICOM files and folders here<p/><br/><p/><br/></h1></html>");
        dragLabel.setFont(FONT_HUGE_ITALICS);
        dragLabel.setAlignmentX((float) 0.5);
        dragLabel.setAlignmentY((float) 0.5);
        dragLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dragLabel.setVerticalAlignment(SwingConstants.CENTER);

        JPanel south = new JPanel();
        south.setLayout(new BorderLayout());

        /*
         * JLabel source = new JLabel(
         * "<html> &nbsp; &nbsp;  &nbsp; &nbsp; &nbsp; &nbsp; Jim Irrer &nbsp; &nbsp; &nbsp; &nbsp; irrer@umich.edu <br/><p/><br/> &nbsp;  &nbsp;  &nbsp; University of Michigan Radiation Oncology</html>"
         * );
         * source.setHorizontalAlignment(SwingConstants.CENTER);
         * source.setVerticalAlignment(SwingConstants.CENTER);
         */

        JTextArea source = new JTextArea("\n\n           Jim Irrer         irrer@umich.edu\n\n      University of Michigan Radiation Oncology");
        source.setFont(FONT_MEDIUM);
        source.setBackground(null);
        source.setEditable(false);
        // source.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel umichLogo = new JLabel(PreDefinedIcons.getUmichLogo());
        south.add(source, BorderLayout.CENTER);
        south.add(umichLogo, BorderLayout.WEST);

        JPanel outerSouth = new JPanel();
        outerSouth.setLayout(new FlowLayout());
        outerSouth.add(south);

        panel.add(dragLabel, BorderLayout.CENTER);
        panel.add(outerSouth, BorderLayout.SOUTH);

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

        patientListPanel.add(dragHereTarget = buildDragAndDropTarget());

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
            if (component instanceof Patient) patientList.add((Patient) component);
        }
        return patientList;
    }

    private Patient findPatient(String patientId) {
        for (Patient patient : getPatientList())
            if (patient.getPatientId().equals(patientId)) return patient;
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
                setColor((Container) component);
            }
            if (component instanceof JComponent) {
                ((JComponent) component).setForeground(COLOR_FONT);
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
            Util.exitFail();
        }
    }

    /**
     * Set the enabled status of container and all of its children.
     * 
     * @param container
     *            Top level container.
     * 
     * @param enable
     *            True to enable, false to disable (disabled == greyed out).
     */
    public static void setEnabledRecursively(Container container, boolean enable) {
        if (container != null) {
            Component[] components = container.getComponents();
            for (Component component : components) {
                component.setEnabled(enable);
                if (component instanceof Container) {
                    setEnabledRecursively((Container) component, enable);
                }
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
            frame.setTitle(ClientConfig.getInstance().getApplicationName());

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
                setPacsLabelSize();
            }
            setMode();
        }
        indicateThatStatisticsHaveChanged();
        cleanScreen();
    }

    private void resetOutputDirectory() {
        hasSpecifiedOutputDirectory = false;
        setAnonymizeDestinationText("");
    }

    private void setAnonymizeDestinationText(String text) {
        int max = 80;
        String t = text;
        anonymizeDestinationText.setToolTipText(text);
        if (text.length() > max) {
            t = text.substring(0, max) + "...";
        }
        anonymizeDestinationText.setText(t);
    }

    /**
     * Remove the given patient.
     * 
     * @param patient
     */
    public void clearPatient(Patient patient) {
        if (preview != null) preview.setVisible(false);
        Anonymize.clearPatientHistory(patient.getPatientId());
        // this is needed because it makes the patient disappear immediately, instead of waiting for the next redraw.
        // patient.setVisible(false);
        patientListPanel.remove(patient);
        patientListPanel.validate();
        if (patientListPanel.getComponentCount() == 0) resetOutputDirectory();
        setProcessedStatus();
        indicateThatStatisticsHaveChanged();
    }

    private void clearAll() {
        for (Patient patient : getPatientList())
            clearPatient(patient);
        Anonymize.clearHistory();
        messageTextArea.setText("");
        showMessageText.delete(0, showMessageText.length());
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        markScreenAsModified();
        Object source = ev.getSource();

        if (source.equals(exitButton)) {
            Util.exitSuccess();
        }

        if (source.equals(clearButton)) {
            clearAll();
        }

        if (source.equals(helpButton)) {
            new Help(ClientConfig.getInstance().getShowUploadCapability());
            // This is for helping to diagnose memory usage.
            Runtime.getRuntime().gc();
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
                int increment = source.equals(pacsNorth) ? -1 : 1;
                currentPacs = (currentPacs + pacsList.size() + increment) % pacsList.size();
                String pacs = pacsList.get(currentPacs).aeTitle;
                pacsLabel.setText(pacs);
                setProcessedStatus();
            }
        }

        if (source.equals(anonymizeOptionsButton)) {
            AnonymizeGUI.getInstance().getDialog().setVisible(!inCommandLineMode());
        }

        if (source.equals(anonymizeRadioButton) || source.equals(anonymizeThenLoadRadioButton) || source.equals(uploadRadioButton)
                || source.equals(anonymizeThenUploadRadioButton)) {
            setMode();
            setProcessedStatus();
        }

        if (source.equals(anonymizeDestinationBrowseButton)) {
            if (!hasSpecifiedOutputDirectory) directoryChooser.setSelectedFile(defaultOutputDirectory());
            switch (directoryChooser.showOpenDialog(getMainContainer())) {
            case JFileChooser.APPROVE_OPTION:
                setAnonymizeDestinationText(directoryChooser.getSelectedFile().getAbsolutePath());
                Log.get().info("Destination for anonymized file: " + anonymizeDestinationText.getText());
                hasSpecifiedOutputDirectory = true;
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
            }
        }

        if (source.equals(searchSubdirsCheckbox)) {
            searchSubdirs = searchSubdirsCheckbox.isSelected();
        }

        if (source.equals(outputOrgFlat)) {
            outputOrgMode = OutputFileOrganization.FLAT;
        }

        if (source.equals(outputOrgTree)) {
            outputOrgMode = OutputFileOrganization.TREE;
        }

        if (source.equals(outputOrgLocal)) {
            outputOrgMode = OutputFileOrganization.LOCAL;
        }

        markScreenAsModified();
    }

    public void updatePreviewIfAppropriate() {
        if (preview != null) {
            preview.updateHighlightedTextIfAppropriate();
        }
    }

    public PACS getCurrentPacs() {
        return PACSConfig.getInstance().getPacsList().get(currentPacs);
    }

    public boolean ensureAnonymizeDirectoryExists() {
        boolean ok = false;
        if (DicomClient.getInstance().getProcessingMode() != ProcessingMode.UPLOAD) {
            File newDir = getDestinationDirectory();
            if (newDir.isDirectory())
                ok = true;
            else {
                newDir.mkdirs();
                ok = newDir.isDirectory();
            }
            if (!ok) {
                String msg = "<html><p>Unable to create anonymization destination<p><br> &nbsp; &nbsp; &nbsp; " + newDir.getAbsolutePath() +
                        "<p><br><p>It is most likely that you do not have<br>permission to write to this directory.</html>";
                new Alert(msg, "Can Not Create Directory");
            }
        }
        else
            ok = true;
        return ok;
    }

    private LinkedBlockingQueue<Object> statisticsQueue = new LinkedBlockingQueue<Object>();

    public void indicateThatStatisticsHaveChanged() {
        statisticsQueue.add(new Object());
        markScreenAsModified();
    }

    private void repaintMain() {
        getMainContainer().paintAll(getMainContainer().getGraphics());
    }

    private LinkedBlockingQueue<Object> repaintQueue = new LinkedBlockingQueue<Object>();

    public void markScreenAsModified() {
        repaintQueue.add(new Object());
    }

    /**
     * Periodically refresh the main screen to fix display problems. This should
     * not really have to be done, but it seems to help on some displays.
     */
    private void cleanScreen() {
        class CleanScreen implements Runnable {
            public void run() {
                while (true) {
                    try {
                        Exec.sleep(2000);
                        if (!repaintQueue.isEmpty()) {
                            Exec.sleep(2000);
                            repaintQueue.clear();
                            repaintMain();
                        }
                    }
                    catch (Exception e) {
                        Exec.sleep(10 * 1000);
                    }
                }
            }
        }
        (new Thread(new CleanScreen())).start();
    }

    public void continuallyUpdateStatistics() {
        class UpdateStats implements Runnable {
            private void showStats() {
                loadedStatisticsLabel.setText(loadedStatistics());
                String processedText = "Files Anonymized: " + Series.totalFilesAnonymized +
                        "            Files Uploaded: " + uploadCount;
                processedStatisticsLabel.setText(processedText);

                Container container = loadedStatisticsLabel.getParent();
                Graphics graphics = container.getGraphics();
                container.paintAll(graphics);
            }

            public void run() {
                while (true) {
                    try {
                        if (statisticsQueue.isEmpty()) {
                            Exec.sleep(250);
                        }
                        if (!statisticsQueue.isEmpty()) {
                            statisticsQueue.clear();
                            showStats();
                        }
                    }
                    catch (Exception e) {
                        Log.get().warning("Unexpected error while updating statistics: " + Log.fmtEx(e));
                        Exec.sleep(10 * 1000);
                    }
                }
            }
        }
        (new Thread(new UpdateStats())).start();
    }

    private String loadedStatistics() {

        ArrayList<Patient> patientList = getPatientList();
        int studyCount = 0;
        int seriesCount = 0;
        int fileCount = 0;
        for (Patient p : patientList) {
            ArrayList<Study> studyList = p.getStudyList();
            studyCount += studyList.size();
            for (Study s : studyList) {
                ArrayList<Series> seriesList = s.seriesList();
                seriesCount += seriesList.size();
                for (Series series : seriesList) {
                    fileCount += series.getFileCount();
                }
            }
        }

        String stats = "Loaded:";
        stats += "    Files: " + fileCount;
        stats += "        Series: " + seriesCount;
        stats += "        Studies: " + studyCount;
        stats += "        Patients: " + patientList.size();
        return stats;
    }

    private void outOfMemory(String msg) {
        String fullMessage = "<html>" + msg + "<p/><br/>The program has run out of memory.  This is usually" +
                "<br/>caused by loading very large data sets.  Anything done" +
                "<br/>beyond this point may silently fail, so it" +
                "<br/>is recommended that you exit the application." +
                "<<p/><br/>It is also possible that some files already processed" +
                "<br/>may have errors, so you may have to redo them." +
                "<p/><br/>An alternative is to run on a 64 bit machine with" +
                "<br/>the 64 bit version of the code.</html>";
        Log.get().severe(fullMessage);
        DicomClient.getInstance().showMessage(fullMessage);
        new Alert(fullMessage, "Out of Memory");
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
        case ANONYMIZE_THEN_LOAD:
            processAllButton.setText("Anonymize then Load All");
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
    }

    public synchronized void incrementUploadCount(int size) {
        uploadCount += size;
        indicateThatStatisticsHaveChanged();
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
                all = ((Series) component).setProcessedStatus(mode) && all;
            }
            if (component instanceof Patient) {
                ((Patient) component).setMode(mode);
            }
            if (component instanceof Container) {
                all = setUploadStatusWorker((Container) component, all) && all;
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
        boolean enabled = uploadEnabled() || (getProcessingMode() == ProcessingMode.ANONYMIZE) || (getProcessingMode() == ProcessingMode.ANONYMIZE_THEN_LOAD);
        processAllButton.setEnabled(enabled);
        processAllButton.setToolTipText(enabled ? "" : "Select a PACS to enable");
        pacsLabel.setToolTipText(pacsLabel.isEnabled() ? "<html>Files will be uploaded<br>to this PACS</html>"
                : "<html>Select <b>Upload</b> or<br><b>Anonymize then Upload</b><br>to enable</html>");
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

    public void stateChanged(ChangeEvent ev) {
        setProcessedStatus();
    }

    /**
     * Get number of DICOM files specified by user.
     * 
     * @return
     */
    public int getFileCount() {
        return fileCount;
    }

    private AttributeList minimalAttributeList(AttributeList attributeList) {
        AttributeTag tagList[] = {
                TagFromName.AcquisitionDate,
                TagFromName.AcquisitionTime,
                TagFromName.ContentDate,
                TagFromName.ContentSequence,
                TagFromName.ContentTime,
                TagFromName.InstanceCreationDate,
                TagFromName.InstanceCreationTime,
                TagFromName.InstanceNumber,
                TagFromName.ImagePositionPatient,
                TagFromName.SliceLocation,
                TagFromName.ManufacturerModelName,
                TagFromName.MediaStorageSOPClassUID,
                TagFromName.Modality,
                TagFromName.PatientBirthDate,
                TagFromName.PatientID,
                TagFromName.PatientName,
                TagFromName.RTPlanDate,
                TagFromName.RTPlanTime,
                TagFromName.SeriesDate,
                TagFromName.SeriesDescription,
                TagFromName.SeriesNumber,
                TagFromName.SeriesTime,
                TagFromName.SeriesInstanceUID,
                TagFromName.SOPClassUID,
                TagFromName.SOPInstanceUID,
                TagFromName.StructureSetDate,
                TagFromName.StructureSetTime,
                TagFromName.StudyDate,
                TagFromName.StudyDescription,
                TagFromName.StudyID,
                TagFromName.StudyInstanceUID,
                TagFromName.StudyTime,
                TagFromName.TransferSyntaxUID
        };
        AttributeList al = new AttributeList();

        for (AttributeTag tag : tagList) {
            try {
                Attribute a = attributeList.get(tag);
                if (a != null) {
                    String[] valueList = a.getStringValues();
                    if (valueList != null) {
                        Attribute at2 = AttributeFactory.newAttribute(tag);
                        for (int v = 0; v < valueList.length; v++) {
                            at2.addValue(new String(valueList[v]));
                        }
                        al.put(at2);
                    }
                }
            }
            catch (Exception e) {
                Log.get().warning("Unable to create attribute with tag " + tag + " : " + e);
            }
        }
        return al;
    }

    /**
     * Read at a minimum the first portion of the given DICOM file. The
     * 'portion' is defined to be long enough to get the basic meta-data.
     * 
     * @param fileName
     * 
     * @return The contents of the file
     */
    private AttributeList readDicomFile(File file) {
        AttributeList attributeList = new AttributeList();
        if (inCommandLineMode()) {
            try {
                // this does not show any annoying messages in the log
                attributeList.read(file);
            }
            catch (Exception e) {
                // Exceptions do not matter because
                // 1: If reading a partial file, there will always be an exception
                // 2: The content is checked anyway
                Log.get().severe("Error reading DICOM file " + file.getAbsolutePath() + " : " + e);
            }
        }
        else {
            DicomClientReadStrategy dcrs = new DicomClientReadStrategy();
            try {
                // attributeList.read(file, DicomClientReadStrategy.dicomClientReadStrategy);
                attributeList.read(file, dcrs);
                attributeList = minimalAttributeList(attributeList);
            }
            catch (Exception e) {
                // Exceptions do not matter because
                // 1: If reading a partial file, there will always be an exception
                // 2: The content is checked anyway
                Log.get().severe("Error reading DICOM file " + file.getAbsolutePath() + " : " + e);
                if (dcrs.latest != null) {
                    attributeList = minimalAttributeList(dcrs.latest);
                }
            }
        }

        // needed? attributeList = ensureMinimumMetadata(file, attributeList);
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

    /**
     * If there is no SOPInstanceUID, then create one and add it.
     * 
     * @param file
     * @param attributeList
     */
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
     * Add the given file to the list of loaded files. If the file is not
     * a DICOM file or can not be read, then show a message and ignore it.
     * The file must be readable (read permission) and a valid DICOM file.
     * There are many checks that could be made to validate it as a DICOM
     * file, but the only ones enforced here are that it must specify a
     * patient ID and modality.
     * 
     * @param fileName
     *            Name of a DICOM file.
     * 
     * @param descend
     *            True if it should descend into subdirectories and process files there.
     */
    public synchronized void addDicomFile(File file, boolean descend) {
        try {
            fileCount++;
            setPreviewEnableable(false);
            if (file.isDirectory()) {
                if (descend) {
                    for (File child : file.listFiles()) {
                        addDicomFile(child, searchSubdirs);
                    }
                }
                else {
                    showMessage(file.getAbsolutePath() + " is a directory and is being ignored.");
                }
                return;
            }
            AttributeList attributeList = readDicomFile(file);

            if (attributeList.size() < MIN_ATTRIBUTE_COUNT) {
                if (Anonymize.isPreloadFile(file)) {
                    Anonymize.preloadUids(file);
                    return;
                }
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
                patientId = new String(patientId); // avoid memory leaks
            }

            Patient patient = findPatient(patientId);
            if (patient == null) {
                patient = new Patient(file, attributeList, makeNewPatientId());
                patientListPanel.add(patient);
                setColor(patientListPanel);
                // JScrollBar scrollBar = patientScrollPane.getVerticalScrollBar();
                // scrollBar.setValue(scrollBar.getMaximum());
            }
            else {
                patient.addStudy(file, attributeList);
            }

            if (dragHereTarget != null) {
                Container parent = dragHereTarget.getParent();
                parent.remove(dragHereTarget);
                dragHereTarget = null;
            }

            synchronized (this) {
                if (!hasSpecifiedOutputDirectory) {
                    // We have a valid DICOM file, so use its directory in determining where to put files.
                    File parent = (file.getParentFile() == null) ? new File(".") : file.getParentFile();
                    File anonymizedDirectory = new File(file.isDirectory() ? file : parent, "output");
                    hasSpecifiedOutputDirectory = true;
                    if (!inCommandLineMode()) {
                        directoryChooser.setSelectedFile(anonymizedDirectory);
                        setAnonymizeDestinationText(anonymizedDirectory.getAbsolutePath());
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

    public void filesDropped(File[] fileList) {
        class Add implements Runnable {
            File[] fileList = null;

            Add(File[] fileList) {
                this.fileList = fileList;
            }

            public void run() {
                for (File file : fileList) {
                    try {
                        addDicomFile(file, true);
                    }
                    catch (OutOfMemoryError t) {
                        String msg = "The program has run out of memory reading file " + file.getAbsolutePath();
                        Log.get().severe(msg);
                        showMessage(msg);
                        outOfMemory(msg);
                    }
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
                seriesList.add((Series) component);
            }
            if (component instanceof Container) {
                getAllSeries((Container) component, seriesList);
            }
        }
        return seriesList;
    }

    /**
     * Process all series in the given container to the currently
     * specified PACS.
     * 
     * @param container
     *            GUI component to be searched for series.
     */
    static private void processAll() {
        try {
            for (Series series : getAllSeries(null, null)) {
                series.processSeries();
            }
            getInstance().setProcessedStatus();
        }
        catch (OutOfMemoryError t) {
            DicomClient.getInstance().outOfMemory("");
        }
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
        if (anonymizeThenLoadRadioButton.isSelected())
            return ProcessingMode.ANONYMIZE_THEN_LOAD;
        if (uploadRadioButton.isSelected())
            return ProcessingMode.UPLOAD;
        if (anonymizeThenUploadRadioButton.isSelected())
            return ProcessingMode.ANONYMIZE_THEN_UPLOAD;
        throw new RuntimeException("DicomClient.getProcessingMode: Unexpected processing mode state.");
    }

    /**
     * Get the instance of this class.
     * 
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

    public static OutputFileOrganization getOutputOrg() {
        return outputOrgMode;
    }

    public static boolean searchSubdirectories() {
        return searchSubdirs;
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
    private static boolean testPrefix(String prefix, ArrayList<String> suffixList) {
        File dir = getInstance().getDestinationDirectory();
        for (int s = 0; s < suffixList.size(); s++) {
            File file = new File(dir, prefix + suffixList.get(s));
            if (file.exists()) return false;
        }
        return true;
    }

    /**
     * Get an available file prefix to be written to. No file should exist with
     * this prefix and any of the suffixes provided. The file prefix will also
     * represent the content of the attribute list. The point of this is to be
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

        name = Util.replaceInvalidFileNameCharacters(name.replace(' ', '_'), '_');

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

    /**
     * Get an available file prefix to be written to. No file should exist with
     * this prefix and any of the suffixes provided. The file prefix will also
     * represent the content of the attribute list. The point of this is to be
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
    public static File getAvailableFile(AttributeList attributeList, String suffix, File seriesOutDir, File inputFile) throws SecurityException {

        OutputOrganization org = new OutputOrganization(attributeList, suffix, seriesOutDir, inputFile);
        switch (outputOrgMode) {
        case TREE:
            return org.tree();
        case LOCAL:
            return org.local();
        default:
            return org.flat();
        }
    }

    private static void usage(String msg) {
        System.err.println(msg);
        String usage = "Usage:\n\n" +
                "    DICOMClient [ -h ] [ -c ] [ -P patient_id ] [ -o output_file ] [ -3 ] [ -z ] [ -g ] inFile1 inFile2 inDir1 inDir2 ...\n" +
                "        -h Show this help and then exit (without GUI)\n" +
                "        -c Run in command line mode (without GUI)\n" +
                "        -P Specify new patient ID for anonymization\n" +
                "        -o Specify output file for anonymization (single file only, command line only)\n" +
                "        -d Specify output directory for anonymization (can not be used with -o option)\n" +
                "        -a Set all dates and times to the same value, eg: 19670225.092251 -> 19560124.105959\n" +
                "           Alternately, use a comma-separated pair to pick a random value between them.\n" +
                "        -i Shift all dates and times by adding the given amount.  Amount may be pos or neg.\n" +
                "           eg: -128.050000 -> subtract 128 days and 5 hours from all dates and times.  Alternately, use a\n" +
                "           comma-separated pair to pick a random value between them.\n" +
                "        -y Truncate all dates to just the year, eg: 19670225 -> 19670101 . Leave the time intact.\n" +
                "        -s When reading files, keep recursively searching through sub-directories\n" +
                "        -F (Flat) The default.  Store created files into the same directory.  Use only one of -F, -T, or -L\n" +
                "        -T (Tree) Store created files in patient ID / series tree under specified directory\n" +
                "        -L (Local) Store created files in local directory as a child of their source directory.  Requires\n" +
                "           write access to source directories.\n" +
                "        -3 Restrict generated XML to 32 character tag names, as required by the SAS software package\n" +
                "        -t Show attribute tag details in text dump (effective in command line mode only)\n" +
                "        -l preload.xml Preload UIDs for anonymization.  This allows anonymizing to take place over multiple sessions.\n" +
                "        -z Replace each control character in generated XML files that describe DICOM attributes with a blank.  Required by SAS\n" +
                "        -g Perform aggressive anonymization - anonymize fields that are not marked for\n" +
                "           anonymization but contain strings found in fields that are marked for anonymization.\n";
        System.err.println(usage);
    }

    private static void fail(String msg) {
        usage(msg);
        Util.exitFail();
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

    private static String[] parseArguments(String[] args) {
        String[] fileList = new String[0];
        File preloadFile = null;
        try {
            for (int a = 0; a < args.length; a++) {
                if (args[a].equals("-P")) {
                    a++;
                    defaultPatientId = args[a];
                    continue;
                }

                if (args[a].equals("-o")) {
                    a++;
                    commandParameterOutputFile = new File(args[a]);
                    Log.get().info("Output file: " + commandParameterOutputFile.getAbsolutePath());
                    hasSpecifiedOutputDirectory = true;
                    continue;
                }

                if (args[a].equals("-d")) {
                    a++;
                    commandParameterOutputDirectory = new File(args[a]);
                    Log.get().info("Output directory: " + commandParameterOutputDirectory.getAbsolutePath());
                    hasSpecifiedOutputDirectory = true;
                    continue;
                }

                if (args[a].equals("-h")) {
                    usage("");
                    Util.exitSuccess();
                }

                if (args[a].equals("-c")) {
                    commandLineMode = true;
                    continue;
                }
                if (args[a].equals("-t")) {
                    showDetails = true;
                    continue;
                }

                if (args[a].equals("-3")) {
                    restrictXmlTagsToLength32 = true;
                    continue;
                }

                if (args[a].equals("-z")) {
                    replaceControlCharacters = true;
                    continue;
                }

                if (args[a].equals("-g")) {
                    aggressivelyAnonymize = true;
                    continue;
                }

                if (args[a].equals("-s")) {
                    searchSubdirs = true;
                    continue;
                }

                if (args[a].equals("-F")) {
                    outputOrgMode = OutputFileOrganization.FLAT;
                    continue;
                }

                if (args[a].equals("-T")) {
                    outputOrgMode = OutputFileOrganization.TREE;
                    continue;
                }

                if (args[a].equals("-L")) {
                    outputOrgMode = OutputFileOrganization.LOCAL;
                    continue;
                }

                if (args[a].equals("-l")) { // preload UIDs
                    a++;
                    preloadFile = new File(args[a]);
                    continue;
                }

                if (args[a].equals("-y")) {
                    if (AnonymizeDateTime.getMode() != DateMode.None) {
                        fail("Conflict with -y option.  Can only use one date anonymization mode of -y, -i, or -a.");
                    }
                    AnonymizeDateTime.setMode(DateMode.Year);
                    continue;
                }

                if (args[a].equals("-i")) { // date shift
                    if (AnonymizeDateTime.getMode() != DateMode.None) {
                        fail("Conflict with -i option.  Can only use one date anonymization mode of -y, -i, or -a.");
                    }
                    a++;
                    if (a >= args.length) fail("Missing value for -i (date shift) option.");
                    try {
                        String text = args[a].trim();

                        Long shift = null;
                        if (args[a].contains(",")) {
                            String[] textArray = args[a].split(",");
                            Long shift0 = AnonymizeDateTime.parseShift(textArray[0]);
                            Long shift1 = AnonymizeDateTime.parseShift(textArray[1]);
                            Long loMs = Math.min(shift0, shift1);
                            Long hiMs = Math.max(shift0, shift1);

                            Long rangeSec = (hiMs - loMs) / 1000;
                            Long sec = (Long) (Math.round(Util.random.nextDouble() * rangeSec));
                            shift = loMs + (sec * 1000);
                            Log.get().info("Randomly chosen shift for anonymization: " + AnonymizeDateTime.formatShiftValue(shift));
                        }
                        else {
                            shift = AnonymizeDateTime.parseShift(text);
                        }

                        if (shift == null) fail("Unable to parse -i shift value: " + text +
                                "\nShould be of the form d.HHMMSS, as in 46.152345 for 46 days, 15 hours, 23 minutes and 45 seconds (15:23:45)");
                        AnonymizeDateTime.setMode(DateMode.Shift);
                        AnonymizeDateTime.setShiftValue(shift);
                    }
                    catch (Exception e) {
                        fail("Unable to parse date shift -i value " + args[a] + " as days.HHMMSS ");
                    }
                    continue;
                }

                if (args[a].equals("-a")) { // date anonymization
                    final String example = "Format is yyyyMMDDD.HHmmss, as in 19560124.115959 for Jan 24, 1956, 11:59:59";
                    if (AnonymizeDateTime.getMode() != DateMode.None) {
                        fail("Conflict with -a option.  Can only use one date anonymization mode of -y, -i, or -a.");
                    }
                    a++;
                    if (a >= args.length) fail("Missing date value for -a (date anonymization) option.  " + example);
                    try {
                        Date anon = null;

                        // if two dates are specified, then pick a random one between them
                        if (args[a].contains(",")) {
                            String[] textArray = args[a].split(",");

                            Date date0 = AnonymizeDateTime.parseAnon(textArray[0]);
                            Date date1 = AnonymizeDateTime.parseAnon(textArray[1]);
                            Long loMs = Math.min(date0.getTime(), date1.getTime());
                            Long hiMs = Math.max(date0.getTime(), date1.getTime());
                            Long rangeSec = (hiMs - loMs) / 1000;
                            Long sec = (Long) (Math.round(Util.random.nextDouble() * rangeSec));
                            Long ms = loMs + (sec * 1000);
                            anon = new Date(ms);
                            Log.get().info("Randomly chosen date for anonymization: " + AnonymizeDateTime.dateTimeFormat.format(anon));
                        }
                        else {
                            anon = AnonymizeDateTime.parseAnon(args[a]);
                        }

                        if (anon == null) {
                            fail("Unable to parse -a (anonymize date) value " + args[a] + "\n" + example);
                        }

                        AnonymizeDateTime.setMode(DateMode.Anon);
                        AnonymizeDateTime.setAnonValue(anon);
                        Log.get().info("Using -a date-time of " + AnonymizeDateTime.dateTimeFormat.format(anon));
                    }
                    catch (Exception e) {
                        fail("Unable to parse date shift -a value " + args[a] + " as date. " + example);
                    }
                    continue;
                }

                if (args[a].startsWith("-")) {
                    fail("Invalid argument: " + args[a]);
                    Util.exitFail();
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

            if (preloadFile != null) { // Do this last because we need to know if we are in command line mode or not.
                Anonymize.preloadUids(preloadFile);
            }
        }
        catch (Exception e) {
            fail("Unable to parse command line arguments.");
        }
        if ((commandParameterOutputFile != null) && (commandParameterOutputDirectory != null)) {
            fail("Can not specify both -o and -d options.");
        }
        if ((commandParameterOutputFile != null) && (fileList.length != 1)) {
            fail("Can one specify -o option with exactly one input file.");
        }
        if ((commandParameterOutputFile != null) && (!inCommandLineMode())) {
            fail("Can one specify -o option in command line mode.");
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

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(ClientConfig.getInstance().getApplicationName() + " starting.  Jar file: " +
                new java.io.File(DicomClient.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName());

        // System.getProperties().list(System.out);
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

            // If in command line mode, then anonymize all files and exit
            // happily
            if (inCommandLineMode()) {
                for (String fileName : args) {
                    dicomClient.addDicomFile(new File(fileName), true);
                }
                Series.processOk = true;
                processAll();
                Util.exitSuccess();
            }
            else {
                // doGc();
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
            Util.exitFail();
        }
    }

    /**
     * Control whether it is ok to show the previewer.
     * 
     * @param previewEnableable
     *            Set to true to enable previewing.
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
     * Set the enabled state of the main dialog.
     * 
     * @param enabled
     *            True to enable, false to disable.
     */
    public void setEnabled(boolean enabled) {
        setEnabledRecursively(frame.getContentPane(), enabled);
    }

}
