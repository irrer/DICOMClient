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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.AttributeTagAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.OtherByteAttribute;
import com.pixelmed.dicom.OtherFloatAttribute;
import com.pixelmed.dicom.OtherWordAttribute;
import com.pixelmed.dicom.SOPClassDescriptions;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.dicom.ValueRepresentation;
import com.pixelmed.display.ConsumerFormatImageMaker;

import edu.umro.dicom.client.DicomClient.ProcessingMode;
import edu.umro.dicom.client.test.AutoTest;
import edu.umro.util.Log;

/**
 * Preview a DICOM file as either text or image, though only image type files
 * can be display as an image, and the previewer will automatically switch to
 * text mode for them.
 * 
 * @author Jim Irrer irrer@umich.edu
 * 
 */
public class Preview implements ActionListener, ChangeListener, DocumentListener, KeyListener, MouseListener, WindowListener {

    /** Maximum line length for attributes of uncertain qualities. */
    private static final int MAX_LINE_LENGTH = 2 * 1000;

    /**
     * When there are multiple values to be displayed for a single attribute,
     * they are separated by this string followed by a blank.
     */
    private static final String VALUE_SEPARATOR = " \\ ";

    /**
     * List of value representations that can be displayed as strings in the
     * text version of the preview.
     */
    private static final byte[][] TEXTUAL_VR = { ValueRepresentation.AE, ValueRepresentation.AS, ValueRepresentation.CS, ValueRepresentation.DA, ValueRepresentation.DS,
            ValueRepresentation.DT, ValueRepresentation.FL, ValueRepresentation.FD, ValueRepresentation.IS, ValueRepresentation.LO, ValueRepresentation.LT, ValueRepresentation.PN,
            ValueRepresentation.SH, ValueRepresentation.SL, ValueRepresentation.SS, ValueRepresentation.ST, ValueRepresentation.TM, ValueRepresentation.UI, ValueRepresentation.UL,
            ValueRepresentation.US, ValueRepresentation.UT, ValueRepresentation.XS, ValueRepresentation.XO };

    /** A quickly searchable list of value representations. */
    public static HashSet<String> vrSet = new HashSet<String>();
    {
        for (byte[] vr : TEXTUAL_VR) {
            vrSet.add(new String(vr));
        }
    }

    /**
     * List of value representations that may contain characters (such as null)
     * that are invalid for XML.
     */
    private static final byte[][] STRING_VR = { ValueRepresentation.AS, ValueRepresentation.CS, ValueRepresentation.DS, ValueRepresentation.IS, ValueRepresentation.LO,
            ValueRepresentation.LT, ValueRepresentation.OF, ValueRepresentation.OW, ValueRepresentation.SH, ValueRepresentation.ST, ValueRepresentation.UT };

    /** A quickly searchable list of value representations. */
    private static HashSet<String> stringSet = new HashSet<String>();
    {
        for (byte[] vr : STRING_VR) {
            stringSet.add(new String(vr));
        }
    }

    /** The possible viewing modes with different highlighting */
    public enum ViewMode {

        /** No highlighting */
        PLAIN("No Highlighting", "<html>Show original values<br/>with no highlighting.</html>"),

        /** Show fields to be anonymized */
        ORIGINAL("Not Anonymized", "<html>Show original values,<br/>highlighting those that will<br/>changed when anonymized.</html>"),

        /** Show what it will look like when it is anonymized. */
        ANONYMIZED("Anonymized", "<html>Show how values<br/>will be anonymized.</html>");

        /** Name displayed to the user. */
        String displayName;

        /** Tool tip to give user more information. */
        String toolTip;

        private ViewMode(String dn, String tt) {
            displayName = dn;
            toolTip = tt;
        }
    };

    /** Preferred size of screen. */
    private static final Dimension PREFERRED_SIZE = new Dimension(610, 830);

    /** Color to indicate text that matches search pattern. */
    private static final Color TEXT_MATCH_COLOR = new Color(255, 255, 150);

    /** Color to indicate text being edited. */
    private static final Color TEXT_EDIT_COLOR = new Color(210, 210, 210);

    /** Color to indicate text that matches search pattern. */
    private static final Color TEXT_CURRENT_MATCH_COLOR = new Color(255, 150, 50);

    /** Color to indicate text that has been anonymized. */
    private static final Color TEXT_ANONYMIZED_COLOR = new Color(220, 255, 230);

    /** Color to indicate text that matches search pattern. */
    private static final Color TEXT_NOT_ANONYMIZED_COLOR = new Color(255, 220, 230);

    /** Values for setting up the contrast slider. */
    private static final int CONTRAST_MIN_VALUE = 0;
    private static final int CONTRAST_MAX_VALUE = 100;
    private static final int CONTRAST_INITIAL_VALUE = 10;

    /** Values for setting up the brightness slider. */
    private static final int BRIGHTNESS_MIN_VALUE = -255;
    private static final int BRIGHTNESS_INITIAL_VALUE = 0;
    private static final int BRIGHTNESS_MAX_VALUE = 255;

    /** Values for setting up the brightness slider. */
    private static final int ZOOM_MIN_VALUE = -12;
    private static final int ZOOM_INITIAL_VALUE = 0;
    private static final int ZOOM_MAX_VALUE = 32;

    /** Prefix used in the title bar of the window. */
    private static final String TITLE_PREFIX = "DICOM Preview";

    /** Identifier used in the card layout to identify text viewing mode. */
    private static final String TEXT_VIEW = "Text";

    /** Identifier used in the card layout to identify image viewing mode. */
    private static final String IMAGE_VIEW = "Image";

    private JDialog dialog = null;
    private Container mainContainer = null;

    /** The series currently being displayed. */
    private Series series = null;

    /** Panel that switches back and forth between image and text viewing modes. */
    private JPanel cardPanel = null;

    /** Layout that switches back and forth between image and text viewing modes. */
    private CardLayout cardLayout = null;

    /** Label at top of screen that shows the current file name. */
    private JTextField fileNameLabel = null;

    /** Contains the text describing the series. */
    private JTextArea textPreview = null;

    /** If checked, show detailed tag information. */
    private JCheckBox showDetails = null;

    /** Contains the image describing the series. */
    private ScalableJLabel imagePreview = null;

    /** Button to close the window. The window is only made non-visible. */
    private JButton closeButton = null;

    /** Scroll pane containing DICOM text. */
    private JScrollPane scrollPaneText = null;

    /** Contains the scrolled image. */
    private JScrollPane scrollPaneImage = null;

    /** Slider to control the image contrast. */
    private JSlider contrastSlider = null;

    /** Slider to control the image brightness. */
    private JSlider brightnessSlider = null;

    /** Slider to control the image zoom. */
    private JSlider zoomSlider = null;

    /** Slider to select the slice. */
    private JSlider sliceSlider = null;

    /** Radio button to switch to text mode. */
    private JRadioButton textRadioButton = null;

    /** Radio button to switch to image mode. */
    private JRadioButton imageRadioButton = null;

    /** Arrow button to change how content is viewed. */
    private BasicArrowButton viewingModeArrowButton = null;

    /** Indicates the current viewing mode. */
    private JLabel viewingModeLabel = null;

    /** Field for entering a search string when in text mode. */
    private JTextField searchField = null;

    /** Current viewing mode. */
    private ViewMode currentViewMode = ViewMode.PLAIN;

    /** Takes you to the previous instance of the text search pattern. */
    private BasicArrowButton searchPrev = null;

    /** Takes you to the next instance of the text search pattern. */
    private BasicArrowButton searchNext = null;

    /** Button to reset the contrast and brightness controls. */
    private JButton resetButton = null;

    /** Button to edit. */
    private JButton editButton = null;

    /** For editing files */
    private EditGui editGui = null;

    /** Label used to display the number of strings matching the search text. */
    private JLabel matchCountLabel = null;

    /**
     * Index indicating which matched string is currently displayed via the
     * scroll bar.
     */
    private int matchIndex = 0;

    /** List of instances of text matching the searched text. */
    private ArrayList<TextMatch> matchList = new ArrayList<TextMatch>();

    /** Representation of currently selected series for viewing. */
    private AttributeList attributeList = null;

    /**
     * Previous value of contrast slider. This is a small optimization to avoid
     * redraws if the same value is given consecutively.
     */
    private int prevContrastValue = Integer.MAX_VALUE;

    /**
     * Previous value of brightness slider. This is a small optimization to
     * avoid redraws if the same value is given consecutively.
     */
    private int prevBrightnessValue = Integer.MAX_VALUE;

    /**
     * Previous value of zoom slider. This is a small optimization to avoid
     * redraws if the same value is given consecutively.
     */
    private int prevZoomValue = Integer.MAX_VALUE;

    /** True if the GUI components need packing. Packing happens only once. */
    private static boolean needsPacking = true;

    /**
     * Lock to prevent events from firing another showDicom operation while
     * already doing a showDicom.
     */
    private Semaphore showDicomInProgress = new Semaphore(1);

    /**
     * Custom text highlighter.
     * 
     * @author Jim Irrer irrer@umich.edu
     * 
     */
    class MatchHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        /**
         * Construct with the given color.
         * 
         * @param color
         *            Text background color.
         */
        public MatchHighlightPainter(Color color) {
            super(color);
        }
    }

    /**
     * The position of text that matches the search text.
     * 
     * @author Jim Irrer irrer@umich.edu
     * 
     */
    class TextMatch {

        /** Character position within text. */
        public int position = -1;

        /** JTextArea highlighter. */
        public Object textHighlighter = null;

        public TextMatch(int p) {
            position = p;
        }
    }

    /**
     * Build the northern (upper) portion of the GUI.
     * 
     * @return Component containing norther part of GUI.
     */
    private JComponent buildNorth() {
        JPanel panel = new JPanel();
        fileNameLabel = new JTextField();
        fileNameLabel.setFont(DicomClient.FONT_TINY);
        fileNameLabel.setEditable(false);
        fileNameLabel.setBorder(null);
        fileNameLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(fileNameLabel);
        return panel;
    }

    private JComponent buildViewingMode() {
        viewingModeArrowButton = new BasicArrowButton(BasicArrowButton.SOUTH);
        viewingModeArrowButton.addActionListener(this);
        viewingModeArrowButton.setToolTipText("<html>Select how content<br>is viewed</html>");

        viewingModeLabel = new JLabel(currentViewMode.displayName);
        viewingModeLabel.setToolTipText(currentViewMode.toolTip);
        viewingModeLabel.setFont(DicomClient.FONT_MEDIUM);
        JPanel viewingPanel = new JPanel(new BorderLayout());
        viewingPanel.add(viewingModeArrowButton, BorderLayout.WEST);
        viewingPanel.add(new JLabel("  "), BorderLayout.CENTER);
        viewingPanel.add(viewingModeLabel, BorderLayout.EAST);
        viewingModeLabel.addMouseListener(this);

        if (!DicomClient.inCommandLineMode()) {
            int maxWidth = 0;
            Graphics graphics = DicomClient.getInstance().getMainContainer().getGraphics();
            FontMetrics metrics = graphics.getFontMetrics(DicomClient.FONT_MEDIUM);
            for (ViewMode vm : ViewMode.values()) {
                int width = metrics.stringWidth(vm.displayName);
                if (width > maxWidth) maxWidth = width;
            }
            viewingModeLabel.setPreferredSize(new Dimension(maxWidth + 15, metrics.getHeight()));
        }

        return viewingPanel;
    }

    private JComponent buildTextScrollArea() {
        textPreview = new JTextArea();
        textPreview.setFont(DicomClient.FONT_MEDIUM);
        textPreview.setEditable(false);
        textPreview.setCaret(new DefaultCaret());
        textPreview.addKeyListener(this);
        textPreview.addMouseListener(this);
        int gap = 10;
        textPreview.setBorder(BorderFactory.createEmptyBorder(gap / 2, gap, gap / 2, gap));
        scrollPaneText = new JScrollPane(textPreview);
        scrollPaneText.setBorder(BorderFactory.createEmptyBorder());
        return scrollPaneText;
    }

    private JComponent buildShowDetails() {
        showDetails = new JCheckBox();
        showDetails.addActionListener(this);
        showDetails.setText("Details");
        showDetails.setFont(DicomClient.FONT_MEDIUM);
        showDetails.setToolTipText("<html>Show DICOM<br>attribute details</html>");
        return showDetails;
    }

    private JComponent buildSearch() {
        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(DicomClient.FONT_MEDIUM);
        searchField = new JTextField(10);
        searchField.getDocument().addDocumentListener(this);
        searchField.addActionListener(this);

        searchPrev = new BasicArrowButton(BasicArrowButton.NORTH);
        searchPrev.addActionListener(this);
        searchPrev.setToolTipText("<html>Previous<br>match</html>");

        searchNext = new BasicArrowButton(BasicArrowButton.SOUTH);
        searchNext.addActionListener(this);
        searchNext.setToolTipText("<html>Next<br>match</html>");

        matchCountLabel = new JLabel();
        matchCountLabel.setFont(DicomClient.FONT_MEDIUM);

        JPanel panel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        gridBagLayout.setConstraints(searchLabel, c);
        panel.add(searchLabel);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        gridBagLayout.setConstraints(searchField, c);
        panel.add(searchField);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0;
        gridBagLayout.setConstraints(searchPrev, c);
        panel.add(searchPrev);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0;
        gridBagLayout.setConstraints(searchNext, c);
        panel.add(searchNext);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        c.gridy = 0;
        c.weightx = 0;
        gridBagLayout.setConstraints(matchCountLabel, c);
        panel.add(matchCountLabel);

        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 20));

        return panel;
    }

    private JComponent buildEditButton() {
        JPanel panel = new JPanel();
        editButton = new JButton("Edit...");
        editButton.addActionListener(this);
        editButton.setToolTipText("<html>Edit a slice<br>or series</html>");
        panel.add(editButton);
        return panel;
    }

    /**
     * Build the text previewer GUI and add it to the card panel.
     */
    private void buildTextPreview() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel westPanel = new JPanel();
        westPanel.add(buildViewingMode());
        westPanel.add(buildShowDetails());

        JPanel subPanel = new JPanel(new BorderLayout());
        subPanel.add(westPanel, BorderLayout.WEST);
        subPanel.add(buildSearch(), BorderLayout.CENTER);
        subPanel.add(buildEditButton(), BorderLayout.EAST);

        int gap = 20;
        subPanel.setBorder(BorderFactory.createEmptyBorder(0, gap, 0, gap));

        panel.add(buildTextScrollArea(), BorderLayout.CENTER);
        panel.add(subPanel, BorderLayout.SOUTH);

        cardPanel.add(panel, TEXT_VIEW);
    }

    /**
     * Build the image previewer portion of the GUI and add it to the card
     * panel.
     */
    private void buildImagePreview() {
        int gap = 10;
        contrastSlider = new JSlider(CONTRAST_MIN_VALUE, CONTRAST_MAX_VALUE, CONTRAST_INITIAL_VALUE);
        contrastSlider.setSnapToTicks(false);
        contrastSlider.setMajorTickSpacing(10);
        contrastSlider.setMinorTickSpacing(1);
        contrastSlider.setPaintTicks(true);
        contrastSlider.addChangeListener(this);
        if (UIManager.getLookAndFeel().getID().equalsIgnoreCase("Windows")) {
            SliderUI sliderUI = new WinSliderUI(contrastSlider);
            contrastSlider.setUI(sliderUI);
        }
        JPanel contrastPanel = new JPanel();
        contrastPanel.setLayout(new BorderLayout());
        contrastPanel.add(new JLabel("    Contrast"), BorderLayout.NORTH);
        contrastPanel.add(contrastSlider, BorderLayout.CENTER);
        contrastPanel.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));

        brightnessSlider = new JSlider(BRIGHTNESS_MIN_VALUE, BRIGHTNESS_MAX_VALUE, BRIGHTNESS_INITIAL_VALUE);
        brightnessSlider.setSnapToTicks(false);
        brightnessSlider.setMajorTickSpacing(32);
        brightnessSlider.setMinorTickSpacing(8);
        brightnessSlider.setPaintTicks(true);
        brightnessSlider.addChangeListener(this);
        if (UIManager.getLookAndFeel().getID().equalsIgnoreCase("Windows")) {
            SliderUI sliderUI = new WinSliderUI(brightnessSlider);
            brightnessSlider.setUI(sliderUI);
        }
        JPanel brightnessPanel = new JPanel();
        brightnessPanel.setLayout(new BorderLayout());
        brightnessPanel.add(new JLabel("    Brightness"), BorderLayout.NORTH);
        brightnessPanel.add(brightnessSlider, BorderLayout.CENTER);
        brightnessPanel.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));

        zoomSlider = new JSlider(ZOOM_MIN_VALUE, ZOOM_MAX_VALUE, ZOOM_INITIAL_VALUE);
        zoomSlider.setSnapToTicks(true);
        zoomSlider.setMajorTickSpacing(4);
        zoomSlider.setMinorTickSpacing(1);
        zoomSlider.setPaintTicks(true);
        zoomSlider.addChangeListener(this);
        if (UIManager.getLookAndFeel().getID().equalsIgnoreCase("Windows")) {
            SliderUI sliderUI = new WinSliderUI(zoomSlider);
            zoomSlider.setUI(sliderUI);
        }
        JPanel zoomPanel = new JPanel();
        zoomPanel.setLayout(new BorderLayout());
        zoomPanel.add(new JLabel("    Zoom"), BorderLayout.NORTH);
        zoomPanel.add(zoomSlider, BorderLayout.CENTER);
        zoomPanel.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));

        resetButton = new JButton("Reset");
        resetButton.setToolTipText("<html>Reset the Contrast, Brightness,<br>and Zoom controls to their<br>original neutral settings.</html>");
        resetButton.addActionListener(this);
        resetButton.setMinimumSize(new Dimension(30, 24));
        // resetButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10,
        // 30));
        JPanel resetPanel = new JPanel();
        resetPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        resetPanel.add(resetButton);

        JPanel gadgetPanel = new JPanel();

        gadgetPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; // components grow in both dimensions
        c.insets = new Insets(5, 5, 5, 5); // 5-pixel margins on all sides
        c.gridheight = 1;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = c.weighty = 1.0;
        gadgetPanel.add(contrastPanel, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        c.weightx = c.weighty = 1.0;
        gadgetPanel.add(brightnessPanel, c);

        c.gridx = 3;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = c.weighty = 0.0;
        gadgetPanel.add(zoomPanel, c);

        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = c.weighty = 0.0;
        gadgetPanel.add(resetPanel, c);

        imagePreview = new ScalableJLabel();
        imagePreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        imagePreview.setAlignmentY(Component.CENTER_ALIGNMENT);
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        imagePanel.add(imagePreview);

        scrollPaneImage = new JScrollPane(imagePanel);
        scrollPaneImage.setBorder(BorderFactory.createEmptyBorder());
        JPanel outerImagePanel = new JPanel();
        outerImagePanel.setLayout(new BorderLayout());

        outerImagePanel.add(scrollPaneImage, BorderLayout.CENTER);
        outerImagePanel.add(gadgetPanel, BorderLayout.SOUTH);

        cardPanel.add(outerImagePanel, IMAGE_VIEW);
    }

    /**
     * Build the center portion of the GUI.
     * 
     * @return The center portion of the GUI.
     */
    private JComponent buildCenter() {
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        buildTextPreview();
        buildImagePreview();

        return cardPanel;
    }

    private JComponent buildSliceSliderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(new JLabel("Slice: "), BorderLayout.WEST);

        sliceSlider = new JSlider(1, 10, 5);
        sliceSlider.setSnapToTicks(true);
        sliceSlider.setMajorTickSpacing(10);
        sliceSlider.setMinorTickSpacing(1);
        sliceSlider.setPaintTicks(true);
        sliceSlider.addChangeListener(this);
        if (UIManager.getLookAndFeel().getID().equalsIgnoreCase("Windows")) {
            SliderUI sliderUI = new WinSliderUI(sliceSlider);
            sliceSlider.setUI(sliderUI);
        }

        panel.add(sliceSlider, BorderLayout.CENTER);

        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 5));

        return panel;
    }

    /**
     * Build the southern (lower) portion of the GUI.
     * 
     * @return The southern (lower) portion of the GUI.
     */
    private JComponent buildSouth() {
        JPanel panel = new JPanel();
        int gap = 20;
        panel.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));

        closeButton = new JButton("Close");
        closeButton.addActionListener(this);
        panel.add(closeButton);

        panel.add(new JLabel("        "));

        ButtonGroup buttonGroup = new ButtonGroup();

        imageRadioButton = new JRadioButton(IMAGE_VIEW);
        imageRadioButton.setFont(DicomClient.FONT_MEDIUM);
        imageRadioButton.addActionListener(this);
        imageRadioButton.setToolTipText("<html>Format the DICOM content<br>as an image.  If it does<br>not contain an image,<br>then display it as text.</html>");
        buttonGroup.add(imageRadioButton);
        panel.add(imageRadioButton);
        imageRadioButton.setSelected(true);

        textRadioButton = new JRadioButton(TEXT_VIEW);
        textRadioButton.setFont(DicomClient.FONT_MEDIUM);
        textRadioButton.addActionListener(this);
        textRadioButton.setToolTipText("<html>Format the DICOM content as text.</html>");
        buttonGroup.add(textRadioButton);
        panel.add(textRadioButton);
        textRadioButton.setSelected(false);

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());

        outerPanel.add(buildSliceSliderPanel(), BorderLayout.CENTER);
        outerPanel.add(panel, BorderLayout.EAST);

        return outerPanel;
    }

    /**
     * Change to the next viewing mode.
     */
    private void incrementViewingMode() {
        String currentName = viewingModeLabel.getText();
        int numModes = ViewMode.values().length;
        for (int vm = 0; vm < numModes; vm++) {
            if (ViewMode.values()[vm].displayName.equals(currentName)) {
                currentViewMode = ViewMode.values()[(vm + 1) % numModes];
                viewingModeLabel.setText(currentViewMode.displayName);
                viewingModeLabel.setToolTipText(currentViewMode.toolTip);
            }
        }
    }

    /**
     * Set the currently selected text to the given matching entry.
     * 
     * @param index
     *            Entry on list that should be shown as currently selected.
     */
    private void setCurrentlySelectedMatch(int oldIndex, int newIndex) {
        if (matchList.size() > 0) {
            try {
                Highlighter highlighter = textPreview.getHighlighter();
                int length = searchField.getText().length();

                if ((oldIndex >= 0) && (oldIndex < matchList.size())) {
                    TextMatch currentTextMatch = matchList.get(oldIndex);
                    highlighter.removeHighlight(currentTextMatch.textHighlighter);
                    MatchHighlightPainter matchHighlightPainter = new MatchHighlightPainter(TEXT_MATCH_COLOR);
                    currentTextMatch.textHighlighter = highlighter.addHighlight(currentTextMatch.position, currentTextMatch.position + length, matchHighlightPainter);
                }
                if ((newIndex >= 0) && (newIndex < matchList.size())) {
                    TextMatch textMatch = matchList.get(newIndex);
                    highlighter.removeHighlight(textMatch.textHighlighter);
                    MatchHighlightPainter matchHighlightPainter = new MatchHighlightPainter(TEXT_CURRENT_MATCH_COLOR);
                    textMatch.textHighlighter = highlighter.addHighlight(textMatch.position, textMatch.position + length, matchHighlightPainter);
                    textPreview.setCaretPosition(textMatch.position);
                }
            }
            catch (BadLocationException e) {
                ;
            }
            matchCountLabel.setText("  " + (matchIndex + 1) + " of " + matchList.size());
        }
        else {
            matchCountLabel.setText("  0 of 0");
        }
    }

    private void changeSearchMatch(int diff) {
        if (matchList.size() >= 1) {
            matchIndex = (matchIndex + diff + matchList.size()) % matchList.size();
            textPreview.setCaretPosition(matchList.get(matchIndex).position);
            showDicom();
        }
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource().equals(closeButton)) {
            close();
        }

        if (ev.getSource().equals(imageRadioButton) || ev.getSource().equals(textRadioButton)) {
            showDicom();
        }

        if (ev.getSource().equals(viewingModeArrowButton)) {
            incrementViewingMode();
            showDicom();
        }

        if (ev.getSource().equals(resetButton)) {
            contrastSlider.setValue(CONTRAST_INITIAL_VALUE);
            brightnessSlider.setValue(BRIGHTNESS_INITIAL_VALUE);
            zoomSlider.setValue(ZOOM_INITIAL_VALUE);
        }

        if (ev.getSource().equals(searchField)) {
            changeSearchMatch(1);
        }

        if (ev.getSource().equals(searchPrev)) {
            changeSearchMatch(-1);
        }

        if (ev.getSource().equals(searchNext)) {
            changeSearchMatch(1);
        }

        if (ev.getSource().equals(showDetails)) {
            showText(null);
        }

        if (ev.getSource().equals(editButton)) {
            if (editGui == null) editGui = new EditGui(this);

            // Set viewing mode to PLAIN
            currentViewMode = ViewMode.PLAIN;
            viewingModeLabel.setText(currentViewMode.displayName);
            viewingModeLabel.setToolTipText(currentViewMode.toolTip);

            editGui.setVisible(true);
            showText(null);
            DicomClient.getInstance().setEnabled(false);
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        char key = e.getKeyChar();

        // go to next search field
        if (key == '\n') {
            changeSearchMatch(1);
        }

        // use the selected text as search text
        if (key == 6) {
            String match = textPreview.getSelectedText();
            if ((match != null) && (match.length() > 0)) {
                searchField.setText(match);
            }
            searchField.grabFocus();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        if ((editGui != null) && (editGui.isVisible() && (e.getSource() == textPreview))) {
            AttributeLocation attributeLocation = new AttributeLocation(textPreview.getCaretPosition());
            showText(attributeLocation);
            editGui.setAttributeLocation(attributeLocation);
        }
        if (e.getSource() == viewingModeLabel) {
            viewingModeArrowButton.doClick();
        }

    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void stateChanged(ChangeEvent ev) {
        if (ev.getSource().equals(sliceSlider)) {
            if (series != null) {
                series.showPreview(sliceSlider.getValue());
            }
        }
        else {
            if ((contrastSlider.getValue() != prevContrastValue) || (brightnessSlider.getValue() != prevBrightnessValue) || (zoomSlider.getValue() != prevZoomValue)) {

                if (zoomSlider.getValue() != prevZoomValue) {
                    scrollPaneImage.getVerticalScrollBar().setValue(0);
                    scrollPaneImage.getHorizontalScrollBar().setValue(0);
                }

                prevContrastValue = contrastSlider.getValue();
                prevBrightnessValue = brightnessSlider.getValue();
                prevZoomValue = zoomSlider.getValue();
                showImage();
            }
        }
    }

    public void insertUpdate(DocumentEvent ev) {
        showText(null);
    }

    public void removeUpdate(DocumentEvent ev) {
        showText(null);
    }

    public void changedUpdate(DocumentEvent ev) {
        showText(null);
    }

    /**
     * Set the series to be previewed.
     * 
     * @param series
     *            Series to be previewed.
     */
    public void setSeries(Series series) {
        sliceSlider.setMaximum(series.getFileList().size());
        DicomClient.getInstance().setProcessedStatus();
    }

    /**
     * Get the current attribute list.
     * 
     * @return The current attribute list.
     */
    public AttributeList getAttributeList() {
        return attributeList;
    }

    /**
     * Get the current slice number.
     * 
     * @return The current slice number.
     */
    public int getCurrentSlice() {
        return sliceSlider.getValue();
    }

    /**
     * Get the currently previewed series.
     * 
     * @return
     */
    public Series getPreviewedSeries() {
        if (DicomClient.inCommandLineMode()) return null;
        if (!dialog.isShowing()) return null;
        return series;
    }

    /**
     * Display the current DICOM file as an image to the user.
     */
    private void showImage() {
        try {
            if (Util.isImageStorage(attributeList)) {
                BufferedImage image = ConsumerFormatImageMaker.makeEightBitImage(attributeList);

                float contrast = (float) contrastSlider.getValue() / (float) 10.0;
                float offset = (float) brightnessSlider.getValue();
                RescaleOp rescale = new RescaleOp(contrast, offset, null);
                BufferedImage transformedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
                transformedImage = rescale.filter(image, transformedImage);

                float zoomValue = zoomSlider.getValue();
                float zoomFactor = 1f;
                if (zoomValue != 0) {
                    if (zoomValue > 0) {
                        zoomFactor = 1f + (zoomValue / 4f);
                    }
                    else {
                        zoomFactor = 4f / ((-zoomValue) + 4f);
                    }
                }
                imagePreview.setScaleFactor(zoomFactor);

                imagePreview.setImage(transformedImage);
                ImageIcon imageIcon = new ImageIcon(transformedImage);
                cardLayout.show(cardPanel, IMAGE_VIEW);
                imagePreview.setIcon(imageIcon);
                Log.get().info("Previewed in image mode: " + dialog.getTitle() + "  contrast: " + contrast + "  brightness: " + offset + "  zoom factor: " + zoomFactor);
                // Force redrawing
                imagePreview.update(imagePreview.getGraphics());
                dialog.update(dialog.getGraphics());
            }
            else {
                showText(null);
            }
        }
        catch (Exception ex) {
            showText(null);
            return;
        }
    }

    /**
     * Determine the prefix for the given indent level.
     * 
     * @param indentLevel
     *            Degree of indentation.
     * 
     * @return String to shift text to the right.
     */
    private String indent(int indentLevel) {
        final String INDENT_VAL = "    ";
        StringBuffer text = new StringBuffer();
        for (int i = 0; i < indentLevel; i++) {
            text.append(INDENT_VAL);
        }
        return text.toString();
    }

    /**
     * Add a single line of text to the text view. If the text matches the
     * current search text, then add its location to the list of matches.
     * 
     * @param text
     *            Text so far.
     * 
     * @param line
     *            New line to add.
     * 
     * @param searchText
     *            Search text - null if none.
     * 
     * @param indentLevel
     *            Degree of indentation.
     */
    private void addLine(StringBuffer text, String line, String searchText, int indentLevel, AttributeLocation attributeLocation, int sequenceItemIndex, Attribute attribute) {
        int textStart = text.length();
        text.append(indent(indentLevel) + line + "\n");
        int textEnd = text.length();
        if (attributeLocation != null) attributeLocation.setAttribute(text.length(), 0, attribute, textStart, textEnd);
    }

    private String addDetails(AttributeTag tag, byte[] vr, String line) {
        if (showDetails.isSelected() || (DicomClient.inCommandLineMode() && DicomClient.showDetails)) {
            String element = Integer.toHexString(tag.getElement()).toUpperCase();
            while (element.length() < 4) {
                element = "0" + element;
            }
            String group = Integer.toHexString(tag.getGroup()).toUpperCase();
            while (group.length() < 4) {
                group = "0" + group;
            }
            if (vr == null) {
                vr = new byte[] { '?', '?' };
            }
            String vmName = CustomDictionary.getInstance().getValueMultiplicity(tag).getName();
            String prefix = group + "," + element + " " + (char) vr[0] + (char) vr[1] + " " + vmName + "  ";
            line = prefix + line;
        }
        return line;
    }

    /**
     * Show a byte value as humanly readable as possible. If it is a displayable
     * ASCII character, then show that, otherwise show the hex value (as in
     * 0xfe).
     * 
     * @param i
     * 
     * @return
     */
    private static String byteToHuman(int i) {
        i = i & 255;
        return ((i >= 32) && (i <= 126)) ? ("" + (char) i) : ("0x" + Integer.toHexString(i & 255));
    }

    /*
     * public static AttributeList replaceNullsWithBlanks(AttributeList attributeList) throws IOException,
     * DicomException {
     * 
     * attributeList = Util.cloneAttributeList(attributeList);
     * 
     * Iterator<?> i = attributeList.values().iterator();
     * while (i.hasNext()) {
     * Attribute attribute = (Attribute) i.next();
     * if (attribute instanceof SequenceAttribute) {
     * Iterator<?> si = ((SequenceAttribute) attribute).iterator();
     * while (si.hasNext()) {
     * SequenceItem item = (SequenceItem) si.next();
     * replaceNullsWithBlanks(item.getAttributeList());
     * }
     * }
     * else {
     * AttributeTag tag = attribute.getTag();
     * byte[] vr = CustomDictionary.getInstance().getValueRepresentationFromTag(tag);
     * if ((vr != null) && stringSet.contains(new String(vr))) {
     * try {
     * String[] valueList = attribute.getStringValues();
     * if ((valueList != null) && (valueList.length > 0)) {
     * boolean same = true;
     * for (int v = 0; v < valueList.length; v++) {
     * if (valueList[v].indexOf('\0') != -1) {
     * same = false;
     * valueList[v] = valueList[v].replace('\0', ' ');
     * }
     * }
     * if (!same) {
     * attribute.removeValues();
     * for (String value : valueList) {
     * attribute.addValue(value);
     * }
     * }
     * }
     * }
     * catch (DicomException e) {
     * ;
     * }
     * }
     * }
     * }
     * return attributeList;
     * }
     */

    /**
     * Convert a single non-sequence attribute to a human readable text format.
     * 
     * @param attribute
     *            Attribute to format.
     * 
     * @return String version of attribute.
     */
    private String getAttributeAsText(Attribute attribute) {
        AttributeTag tag = attribute.getTag();
        StringBuffer line = new StringBuffer();
        boolean ok = false;
        byte[] vr = CustomDictionary.getInstance().getValueRepresentationFromTag(tag);
        if (vr == null) {
            vr = attribute.getVR();
        }
        if (vr != null) {
            try {
                if ((!ok) && (vrSet.contains(new String(vr)))) {
                    String[] valueList = attribute.getStringValues();

                    if ((valueList != null) && (valueList.length > 0)) {
                        boolean first = true;
                        for (String value : valueList) {
                            if (first)
                                first = false;
                            else
                                line.append(VALUE_SEPARATOR);
                            line.append(" " + value.replace('\n', ' '));
                            if (line.length() > MAX_LINE_LENGTH) {
                                break;
                            }
                            if (ValueRepresentation.isUniqueIdentifierVR(vr)) {
                                String classDesc = SOPClassDescriptions.getDescriptionFromUID(value);
                                if (classDesc.length() > 0) {
                                    line.append(" (" + classDesc + ")");
                                }
                                TransferSyntax transferSyntax = new TransferSyntax(value);
                                if (transferSyntax.isRecognized()) {
                                    line.append(" (" + transferSyntax.getDescription() + ")");
                                }
                            }
                        }
                    }
                    ok = true;
                }
                else {
                    if ((!ok) && (ValueRepresentation.isAttributeTagVR(vr))) {
                        AttributeTag[] atList = ((AttributeTagAttribute) attribute).getAttributeTagValues();
                        for (AttributeTag t : atList) {
                            line.append("  " + t);
                            if (CustomDictionary.getInstance().getNameFromTag(t) == null) {
                                line.append(":<unknown>");
                            }
                            else {
                                line.append(":" + CustomDictionary.getInstance().getNameFromTag(t));
                            }
                            if (line.length() > MAX_LINE_LENGTH) {
                                break;
                            }
                        }
                        ok = true;
                    }

                    if ((!ok) && ((ValueRepresentation.isOtherByteVR(vr)) || (attribute instanceof OtherByteAttribute))) {
                        byte[] outData = ((OtherByteAttribute) attribute).getByteValues();

                        for (int b = 0; b < outData.length; b++) {
                            line.append(" " + byteToHuman(outData[b]));
                            if (line.length() > MAX_LINE_LENGTH) {
                                break;
                            }
                        }
                        ok = true;
                    }

                    if ((!ok) && ((ValueRepresentation.isOtherFloatVR(vr) || (attribute instanceof OtherFloatAttribute)))) {
                        float[] floatValues = ((OtherFloatAttribute) attribute).getFloatValues();
                        boolean first = true;
                        for (float f : floatValues) {
                            if (first)
                                first = false;
                            else
                                line.append(VALUE_SEPARATOR);
                            line.append(" " + f);
                            if (line.length() > MAX_LINE_LENGTH) {
                                break;
                            }
                        }
                        ok = true;
                    }

                    if ((!ok) && ((attribute instanceof OtherWordAttribute) || (ValueRepresentation.isOtherWordVR(vr)))) {
                        short[] outData = ((OtherWordAttribute) attribute).getShortValues();

                        for (int b = 0; b < outData.length; b++) {
                            line.append(" " + byteToHuman((outData[b] & 0xffff) >> 8) + " " + byteToHuman(outData[b] & 0xff));
                            if (line.length() > MAX_LINE_LENGTH) {
                                break;
                            }
                        }
                        ok = true;
                    }
                }
            }
            catch (Exception e) {
                line.append(" Error interpreting field: " + attribute.toString().replace('\n', ' '));
            }
        }

        if (!ok) {
            line = new StringBuffer(" " + attribute.toString().replace('\n', ' '));
        }

        if (line.length() > MAX_LINE_LENGTH) {
            line = new StringBuffer(line.substring(0, MAX_LINE_LENGTH) + " ... (truncated)");
        }

        String tagName = CustomDictionary.getInstance().getNameFromTag(tag);
        if (tagName == null) {
            tagName = "<unknown>";
        }

        line = new StringBuffer(line.toString().replace('\0', ' '));

        return addDetails(tag, vr, tagName + " :" + line.toString());
    }

    /**
     * Add the list of attributes to the given text. This method is called
     * recursively to support DICOM attributes that are defined as a tree.
     * 
     * @param attributeList
     *            List of attributes to add.
     * 
     * @param text
     *            Existing text to append to.
     * 
     * @param dicomDictionary
     *            DICOM dictionary to use.
     * 
     * @param indentLevel
     *            Indicates the depth of recursion and drives the amount of
     *            whitespace prepended to each line.
     */
    public void addTextAttributes(AttributeList attributeList, StringBuffer text, int indentLevel, AttributeLocation attributeLocation) {
        String searchText = searchField.getText().toLowerCase();

        Iterator<?> i = attributeList.values().iterator();
        while (i.hasNext()) {
            Attribute attribute = (Attribute) i.next();
            if (attribute instanceof SequenceAttribute) {
                AttributeTag tag = attribute.getTag();
                String line = CustomDictionary.getInstance().getNameFromTag(tag) + " : ";
                line = addDetails(tag, CustomDictionary.getInstance().getValueRepresentationFromTag(tag), line);
                addLine(text, line, searchText, indentLevel, attributeLocation, 0, attribute);
                Iterator<?> si = ((SequenceAttribute) attribute).iterator();
                int itemNumber = 1;
                while (si.hasNext()) {
                    if ((attributeLocation != null) && (!attributeLocation.isLocated())) attributeLocation.addParent((SequenceAttribute) attribute, itemNumber - 1);
                    SequenceItem item = (SequenceItem) si.next();
                    addLine(text, "Item: " + itemNumber + " / " + ((SequenceAttribute) attribute).getNumberOfItems(), searchText, indentLevel + 1, attributeLocation,
                            itemNumber - 1, null);
                    addTextAttributes(item.getAttributeList(), text, indentLevel + 2, attributeLocation);
                    if ((attributeLocation != null) && (!attributeLocation.isLocated())) attributeLocation.removeParent();
                    itemNumber++;
                }
            }
            else {
                addLine(text, getAttributeAsText(attribute), searchText, indentLevel, attributeLocation, 0, attribute);
            }
        }
    }

    public void selectForEdit(AttributeLocation attributeLocation) {
        showText(attributeLocation);
    }

    /**
     * Perform edits on attribute list if there are any edits.
     * 
     * @param attributeList
     *            Source list.
     * 
     * @return Modified list.
     */
    private AttributeList performEdits(AttributeList attributeList) {
        AttributeList atList = attributeList;
        if ((editGui != null) && (editGui.isVisible())) {
            try {
                atList = Util.cloneTopLevelAttributeList(attributeList);
                editGui.performEdits(atList);
            }
            catch (Exception e) {
                atList = attributeList;
            }
        }

        return atList;
    }

    /**
     * Highlight the differences between two versions of text, the original text and the anonymized text.
     * 
     * @param displayBuffer
     *            Text to be displayed.
     * 
     * @param referenceBuffer
     *            Alternate text.
     * 
     * @param highlightColor
     *            Color to use on lines that are different.
     */
    private void highlightDiffs(StringBuffer displayBuffer, StringBuffer referenceBuffer, Color highlightColor) {
        String[] displayLines = displayBuffer.toString().split("\n");
        String[] referenceLines = referenceBuffer.toString().split("\n");
        Highlighter highlighter = textPreview.getHighlighter();

        int index = 0;
        int len = (displayLines.length < referenceLines.length) ? displayLines.length : referenceLines.length;
        for (int l = 0; l < len; l++) {
            String disp = displayLines[l];
            String ref = referenceLines[l];

            if (!disp.equals(ref)) {
                MatchHighlightPainter painter = new MatchHighlightPainter(highlightColor);
                int start = 0;
                while ((disp.charAt(start) == ' ') && (start < disp.length()))
                    start++;
                try {
                    highlighter.addHighlight(index + start, index + disp.length(), painter);
                }
                catch (BadLocationException e) {
                    ;
                }
            }
            index += (disp.length() + 1);
        }
    }

    private void highlightSearchText() {

        // Find list of matches
        String searchText = searchField.getText().toLowerCase();
        int length = searchText.length();

        int oldMatchListSize = matchList.size();
        int oldMatchIndex = matchIndex;
        matchIndex = 0;
        matchList = new ArrayList<TextMatch>();

        if (length > 0) {
            String[] lineList = textPreview.getText().toLowerCase().split("\n");
            int index = 0;

            for (String line : lineList) {
                int position = 0;
                while ((position = line.indexOf(searchText, position)) >= 0) {
                    matchList.add(new TextMatch(index + position));
                    position += length;
                }
                index += (line.length() + 1);
            }

            Highlighter highlighter = textPreview.getHighlighter();

            // highlight all instances of the search text
            if (matchList.size() > 0) {
                MatchHighlightPainter matchHighlightPainter = new MatchHighlightPainter(TEXT_MATCH_COLOR);
                try {
                    for (TextMatch textMatch : matchList) {
                        textMatch.textHighlighter = highlighter.addHighlight(textMatch.position, textMatch.position + length, matchHighlightPainter);
                    }
                }
                catch (BadLocationException e) {
                }

                if (oldMatchListSize == matchList.size()) {
                    matchIndex = oldMatchIndex;
                }
                else {
                    matchIndex = 0;
                }
                setCurrentlySelectedMatch(oldMatchIndex, matchIndex);
            }
        }
    }

    /**
     * Display the current DICOM file as text to the user.
     */
    private void showText(AttributeLocation attributeLocation) {
        if (attributeList == null) return;
        AttributeList attrList = null;
        try {
            attrList = Util.cloneTopLevelAttributeList(attributeList);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        matchCountLabel.setText("  0 of 0");
        int scrollPosition = scrollPaneText.getVerticalScrollBar().getValue();
        int caretPosition = textPreview.getCaretPosition();
        Highlighter highlighter = textPreview.getHighlighter();
        highlighter.removeAllHighlights();

        AttributeList editedAttributeList = performEdits(attrList);
        StringBuffer origText = new StringBuffer();
        addTextAttributes(editedAttributeList, origText, 0, attributeLocation);

        StringBuffer anonText = new StringBuffer();
        try {
            if ((editGui != null) && (editGui.isVisible())) {
                addTextAttributes(editedAttributeList, anonText, 0, attributeLocation);
                textPreview.setText(origText.toString());
            }
            else {
                Anonymize.anonymize(editedAttributeList, series.getAnonymizingReplacementList());
                addTextAttributes(editedAttributeList, anonText, 0, attributeLocation);
                if (currentViewMode == ViewMode.ANONYMIZED) {
                    textPreview.setText(anonText.toString());
                }
                else {
                    textPreview.setText(origText.toString());
                }
            }
        }
        catch (DicomException e) {
            textPreview.setText(origText.toString());
        }

        highlightSearchText();

        // highlight the attribute being edited
        if (attributeLocation != null) {
            MatchHighlightPainter matchHighlightPainter = new MatchHighlightPainter(TEXT_EDIT_COLOR);
            try {
                highlighter.addHighlight(attributeLocation.getStartOfText(), attributeLocation.getEndOfText(), matchHighlightPainter);
            }
            catch (BadLocationException e) {
            }
        }
        if (attributeLocation != null) Log.get().info("Selected for edit:\n" + attributeLocation.toString() + "\n");

        switch (currentViewMode) {
        case PLAIN:
            break;

        case ORIGINAL:
            highlightDiffs(origText, anonText, TEXT_NOT_ANONYMIZED_COLOR);
            break;
        case ANONYMIZED:

            highlightDiffs(anonText, origText, TEXT_ANONYMIZED_COLOR);
            break;
        }

        if ((scrollPosition >= scrollPaneText.getVerticalScrollBar().getMinimum()) && (scrollPosition <= scrollPaneText.getVerticalScrollBar().getMaximum())) {
            scrollPaneText.getVerticalScrollBar().setValue(scrollPosition);
            if ((caretPosition < textPreview.getText().length())) {
                textPreview.setCaretPosition(caretPosition);
            }
        }
        else {
            scrollPaneText.getVerticalScrollBar().setValue(0);
            textPreview.setCaretPosition(0);
        }

        cardLayout.show(cardPanel, TEXT_VIEW);
    }

    /**
     * Only become visible if the previewer is enabled, otherwise (true) leave
     * it the way it is or (false) make is not visible.
     */
    public void setVisible(boolean visible) {
        if (visible) {
            if (DicomClient.getInstance().isPreviewEnableable()) {
                dialog.setVisible(!DicomClient.inCommandLineMode());
            }
        }
        else {
            dialog.setVisible(false);
        }
        DicomClient.getInstance().setProcessedStatus();
    }

    public boolean isVisible() {
        return dialog.isVisible();
    }

    /**
     * Show the current DICOM file to the user in either image or text mode,
     * which ever was requested. If image mode is requested but is not possible,
     * then automatically switch to text mode.
     */
    public void showDicom() {
        if (imageRadioButton.isSelected()) {
            showImage();
        }
        else {
            showText(null);
        }
        if (needsPacking) {
            dialog.pack();
            needsPacking = false;
        }
        if (!isVisible()) {
            setVisible(true);
        }
    }

    /**
     * This supports the user updating the date shift from the main
     * screen and immediately seeing the result in the preview.
     */
    public void updateHighlightedTextIfAppropriate() {
        if (attributeList != null) {
            if ((textRadioButton.isSelected()) || (!Util.isImageStorage(attributeList))) {
                if (!viewingModeLabel.getText().equalsIgnoreCase(ViewMode.PLAIN.displayName)) {
                    showDicom();
                }
            }
        }
    }

    /**
     * Select the given DICOM file and show it to the user.
     * 
     * @param title
     *            Title for window.
     * 
     * @param file
     *            Name of DICOM file.
     */
    public void showDicom(Series series, String title, int sliceNumber, int maxSlice, File file) {
        if (!DicomClient.inCommandLineMode()) {
            if (showDicomInProgress.tryAcquire()) {
                try {
                    this.series = series;
                    sliceSlider.setMaximum(maxSlice);
                    DicomClient.getInstance().setProcessedStatus();

                    fileNameLabel.setText(file.getAbsolutePath());
                    dialog.setTitle(TITLE_PREFIX + "  " + title);
                    try {
                        attributeList = Util.readDicomFile(file);
                        AnonymizeGUI.getInstance().updateTagList(attributeList);
                        sliceSlider.setValue(sliceNumber);
                        showDicom();
                    }
                    catch (DicomException ex) {
                        DicomClient.getInstance().showMessage("Unable to interpret file " + file + " as DICOM: " + ex.getMessage());
                    }
                    catch (IOException ex) {
                        DicomClient.getInstance().showMessage("Unable to read file " + file + " : " + ex.getMessage());
                    }
                    DicomClient.getInstance().setProcessedStatus();
                }
                finally {
                    showDicomInProgress.release();
                }
            }
        }
    }

    /**
     * Build the GUI for previewing DICOM files.
     */
    public Preview() {
        mainContainer = new Container();
        if (!DicomClient.inCommandLineMode()) {
            dialog = new JDialog(DicomClient.getInstance().getFrame(), false);
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            dialog.addWindowListener(this);
            dialog.setTitle(TITLE_PREFIX);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(buildNorth(), BorderLayout.NORTH);
        panel.add(buildCenter(), BorderLayout.CENTER);
        panel.add(buildSouth(), BorderLayout.SOUTH);

        DicomClient.setColor(panel);

        if (DicomClient.inCommandLineMode()) {
            mainContainer.add(panel);
        }
        else {
            dialog.setPreferredSize(PREFERRED_SIZE);
            dialog.getContentPane().add(panel);
            dialog.pack();
        }
    }

    /**
     * Handle the user's request to close the dialog.
     */
    private void close() {
        ProcessingMode mode = DicomClient.getInstance().getProcessingMode();
        if (editGui == null) {
            dialog.setVisible(false);
            if (series != null) series.setProcessedStatus(mode);
        }
        else {
            if (!editGui.isModified()) {
                editGui.setVisible(false);
                editGui.reset();
                dialog.setVisible(false);
            }
            else {

                if (editGui.letUserSaveIfTheyWantTo()) {
                    editGui.reset();
                    dialog.setVisible(false);
                    editGui.setVisible(false);
                }
            }
        }
        if (series != null) series.setProcessedStatus(mode);
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        close();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    @AutoTest
    public void doClickTextPreview() {
        textRadioButton.doClick();
    }

    @AutoTest
    public String getTextTest() {
        return textPreview.getText();
    }

}
