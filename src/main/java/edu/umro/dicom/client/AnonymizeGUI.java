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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.ValueRepresentation;

import edu.umro.util.Log;

/**
 * AnonymizeGUI PHI in a DICOM file.
 * 
 * @author Jim Irrer irrer@umich.edu
 *
 */
public class AnonymizeGUI implements ActionListener, DocumentListener {

    private final static AttributeTag[] sopClassUidTagList = {
            TagFromName.AffectedSOPClassUID,
            TagFromName.RequestedSOPClassUID,
            TagFromName.MediaStorageSOPClassUID,
            TagFromName.ImplementationClassUID,
            TagFromName.ReferencedSOPClassUIDInFile,
            TagFromName.ReferencedRelatedGeneralSOPClassUIDInFile,
            TagFromName.SOPClassUID,
            TagFromName.RelatedGeneralSOPClassUID,
            TagFromName.OriginalSpecializedSOPClassUID,
            TagFromName.SOPClassesInStudy,
            TagFromName.ReferencedSOPClassUID,
            TagFromName.SOPClassesSupported
    };

    /**
     * Represents one attribute to be anonymized.
     * 
     * @author Jim Irrer irrer@umich.edu
     *
     */
    public class AnonymizeAttribute extends JPanel implements ActionListener, ItemListener, DocumentListener {
        /** Default ID */
        private static final long serialVersionUID = 1L;

        /** Provides choices of attributes. */
        private JTextField attrNameLabel = null;

        /** Determines whether an attribute is active. */
        private JCheckBox checkBox = null;

        /** Value to use for anonymization. */
        private JTextField textField = null;

        /** The tag associated with this attribute. */
        private AttributeTag tag = null;

        private boolean isEditable(AttributeTag tag) {

            byte[] vr = CustomDictionary.getInstance().getValueRepresentationFromTag(tag);
            if (ValueRepresentation.isUniqueIdentifierVR(vr)) {
                for (AttributeTag c : sopClassUidTagList) {
                    if ((tag.getGroup() == c.getGroup()) && (tag.getElement() == c.getElement())) {
                        return true;
                    }
                }
                return false;
            }

            return true;
        }

        /**
         * Construct the interface for one attribute.
         * 
         */
        public AnonymizeAttribute(AttributeTag tag) {
            this.tag = tag;
            attrNameLabel = new JTextField();
            attrNameLabel.setFont(DicomClient.FONT_MEDIUM);
            attrNameLabel.setEditable(false);
            attrNameLabel.setBorder(null);
            attrNameLabel.setHorizontalAlignment(JLabel.LEFT);
            attrNameLabel.setColumns(20);
            resetText(showDetails.isSelected());

            checkBox = new JCheckBox();
            checkBox.setToolTipText("<html>Select for<br>anonymizing</html>");
            textField = new JTextField(12);
            textField.getDocument().addDocumentListener(this);

            Attribute attr = defaultList.get(tag);
            if (attr != null) textField.setText(attr.getSingleStringValueOrEmptyString());
            setActive(attr != null);
            checkBox.addActionListener(this);
            checkBox.addItemListener(this);

            add(attrNameLabel);
            add(checkBox);
            add(textField);

            boolean isEditable = isEditable(tag);
            textField.setEnabled(isEditable && checkBox.isSelected());
            textField.setEditable(isEditable); // extra guard against editing

            if (!isEditable) textField.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        }

        public void resetText(boolean showDetail) {
            String text = CustomDictionary.getInstance().getNameFromTag(tag);
            if (showDetail) text = String.format("%04x,%04x  ", tag.getGroup(), tag.getElement()) + text;
            attrNameLabel.setText(text);
            attrNameLabel.setToolTipText(text);
            attrNameLabel.setCaretPosition(0);
        }

        /**
         * Get the type of attribute to be anonymized.
         * 
         * @return The type of attribute to be anonymized.
         */
        public AttributeTag getTag() {
            return tag;
        }

        /**
         * Make anonymization active for this attribute.
         * 
         * @param active
         *            True if attribute is to be anonymized.
         */
        public void setActive(boolean active) {
            checkBox.setSelected(active);
            if (isEditable(tag)) textField.setEnabled(active);
            attrNameLabel.setEnabled(active);
        }

        public boolean getActive() {
            return checkBox.isSelected();
        }

        public String getValue() {
            return textField.getText();
        }

        public void setValue(String value) {
            textField.setText(value);
        }

        public void itemStateChanged(ItemEvent e) {
        }

        public void actionPerformed(ActionEvent e) {
            if (isEditable(tag)) textField.setEnabled(getActive());
            attrNameLabel.setEnabled(getActive());
            DicomClient.getInstance().updatePreviewIfAppropriate();
        }

        @Override
        public String toString() {
            return attrNameLabel.getText() + "  " + (checkBox.isSelected() ? "active" : "inactive") + "  " + textField.getText();
        }

        public JTextField getTextField() {
            return attrNameLabel;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            DicomClient.getInstance().updatePreviewIfAppropriate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            DicomClient.getInstance().updatePreviewIfAppropriate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            DicomClient.getInstance().updatePreviewIfAppropriate();
        }
    }

    /**
     * The position of text that matches the search text.
     * 
     * @author Jim Irrer irrer@umich.edu
     *
     */
    class TextMatch {

        /** Line number within text. */
        public AnonymizeAttribute anonymizeAttribute = null;

        /** Character position within text. */
        public int position = -1;

        /** JTextArea highlighter. */
        public Object highlighter = null;

        public TextMatch(AnonymizeAttribute aa, int p) {
            anonymizeAttribute = aa;
            position = p;
        }
    }

    /**
     * Custom text highlighter.
     * 
     * @author Jim Irrer irrer@umich.edu
     *
     */
    class MatchHighlightPainterAnon extends DefaultHighlighter.DefaultHighlightPainter {
        /**
         * Construct with the given color.
         * 
         * @param color
         *            Text background color.
         */
        public MatchHighlightPainterAnon(Color color) {
            super(color);
        }
    }

    /** Color to indicate text that matches search pattern. */
    private static final Color TEXT_MATCH_COLOR = new Color(255, 255, 150);

    /** Color to indicate text that matches search pattern. */
    private static final Color TEXT_CURRENT_MATCH_COLOR = new Color(255, 150, 50);

    /** List of instances of text matching the searched text. */
    private ArrayList<TextMatch> matchList = new ArrayList<TextMatch>();

    /** Title of screen. */
    private static final String WINDOW_TITLE = "Anonymize Options";

    /** Preferred size of screen. */
    private static final Dimension PREFERRED_SIZE = new Dimension(1024, 830);

    /** Scroll bar increment. */
    private static final int SCROLL_INCREMENT = 20;

    /** List of all attributes in all series (superset) and their values for anonymization. */
    private AttributeList knownAttributes = new AttributeList();

    private JDialog dialog = null;

    /** Selects all checkboxes. */
    private JButton selectAllButton = null;

    /** Selects no checkboxes. */
    private JButton selectNoneButton = null;

    /** Selects no checkboxes. */
    private JButton selectDefaultsButton = null;

    /** Closes the dialog. */
    private JButton closeButton = null;

    /** Holds the list of anonymizing fields. */
    private JPanel anonPanel = null;

    /** Scrolls list of attributes. */
    private JScrollPane scrollPane = null;

    /** Default anonymization list. Defined in configuration. */
    private AttributeList defaultList = ClientConfig.getInstance().getAnonymizingReplacementList();

    /** If checked, show detailed tag information. */
    private JCheckBox showDetails = null;

    /** Field for entering a search string when in text mode. */
    private JTextField searchField = null;

    /** Takes you to the previous instance of the text search pattern. */
    private BasicArrowButton searchPrev = null;

    /** Takes you to the next instance of the text search pattern. */
    private BasicArrowButton searchNext = null;

    /** Label used to display the number of strings matching the search text. */
    private JLabel matchCountLabel = null;

    /** Index indicating which matched string is currently displayed via the scroll bar. */
    int matchIndex = 0;

    /** Singleton instance of this class. */
    private static AnonymizeGUI anonymizeGUI = null;

    public JDialog getDialog() {
        return dialog;
    }

    private void setAllCheckboxes(boolean state) {
        for (Component aa : anonPanel.getComponents()) {
            ((AnonymizeAttribute) aa).setActive(state);
        }
    }

    private void setDefaultCheckboxes() {
        for (Component aa : anonPanel.getComponents()) {
            AnonymizeAttribute anonAttr = (AnonymizeAttribute) aa;
            boolean state = defaultList.get(anonAttr.getTag()) != null;
            anonAttr.setActive(state);
        }
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource().equals(closeButton)) {
            dialog.setVisible(false);
        }
        if (ev.getSource().equals(selectAllButton)) {
            setAllCheckboxes(true);
        }
        if (ev.getSource().equals(selectNoneButton)) {
            setAllCheckboxes(false);
        }
        if (ev.getSource().equals(selectDefaultsButton)) {
            setDefaultCheckboxes();
        }
        if (ev.getSource().equals(searchNext)) {
            setCurrentlySelectedMatch(1);
        }
        if (ev.getSource().equals(searchPrev)) {
            setCurrentlySelectedMatch(-1);
        }
        if (ev.getSource().equals(searchField)) {
            setCurrentlySelectedMatch(1);
        }
        if (ev.getSource().equals(showDetails)) {
            redrawAnonList();
        }
        DicomClient.getInstance().updatePreviewIfAppropriate();
    }

    private void redrawAnonList() {
        boolean sd = showDetails.isSelected();
        for (Component component : anonPanel.getComponents()) {
            if (component instanceof AnonymizeAttribute) {
                ((AnonymizeAttribute) component).resetText(sd);
            }
        }
    }

    private void setMatch(int matchIndex, Color color) {
        if (matchIndex >= matchList.size()) matchIndex = matchList.size() - 1;
        if (matchIndex < 0) matchIndex = 0;
        TextMatch match = matchList.get(matchIndex);
        try {
            int length = searchField.getText().length();
            Highlighter highlighter = match.anonymizeAttribute.getTextField().getHighlighter();
            highlighter.removeAllHighlights();
            MatchHighlightPainterAnon matchHighlightPainter = new MatchHighlightPainterAnon(color);
            match.highlighter = highlighter.addHighlight(match.position, match.position + length, matchHighlightPainter);
        }
        catch (BadLocationException e) {
            ;
        }
    }

    /**
     * Set the currently selected text to the given matching entry.
     * 
     * @param index
     *            Entry on list that should be shown as currently selected.
     */
    private void setCurrentlySelectedMatch(int incr) {
        int size = matchList.size();
        if (size > 1) {
            int oldIndex = matchIndex;
            matchIndex = (matchIndex + incr + size) % size;
            if (oldIndex >= size) oldIndex = size - 1;
            if (oldIndex < 0) oldIndex = 0;
            setMatch(oldIndex, TEXT_MATCH_COLOR);
        }
        else
            matchIndex = 0;

        if (size > 0) {
            setMatch(matchIndex, TEXT_CURRENT_MATCH_COLOR);
            anonPanel.scrollRectToVisible(matchList.get(matchIndex).anonymizeAttribute.getBounds());
        }
        matchCountLabel.setText("  " + ((size > 0) ? matchIndex + 1 : 0) + " of " + size);
    }

    private void search() {
        try {
            String searchText = searchField.getText().toLowerCase();
            int searchTextLen = searchText.length();

            matchList = new ArrayList<TextMatch>();

            for (Component component : anonPanel.getComponents()) {
                if (component instanceof AnonymizeAttribute) {
                    AnonymizeAttribute aa = (AnonymizeAttribute) component;
                    JTextField textField = aa.getTextField();
                    String text = textField.getText().toLowerCase();
                    int posn = text.indexOf(searchText);
                    Highlighter highlighter = textField.getHighlighter();
                    highlighter.removeAllHighlights();
                    if (posn != -1) {
                        TextMatch textMatch = new TextMatch(aa, posn);
                        matchList.add(textMatch);
                        MatchHighlightPainterAnon matchHighlightPainter = new MatchHighlightPainterAnon(TEXT_MATCH_COLOR);
                        textMatch.highlighter = highlighter.addHighlight(posn, posn + searchTextLen, matchHighlightPainter);
                    }
                }
            }
            setCurrentlySelectedMatch(0);
        }
        catch (BadLocationException e) {
            ;
        }
    }

    public void insertUpdate(DocumentEvent e) {
        search();
    }

    public void removeUpdate(DocumentEvent e) {
        search();
    }

    public void changedUpdate(DocumentEvent e) {
        search();
    }

    /**
     * Add a new attribute selector to the list. If parameters are null, then add
     * 
     * @param attributeList
     *            List of attributes.
     * 
     * @param tag
     *            Item to add.
     * 
     * @return The new anonymize attribute.
     */
    private void addAttribute(AttributeTag tag) {
        if (isAnonymizable(tag)) {
            AnonymizeAttribute aa = new AnonymizeAttribute(tag);
            anonPanel.add(aa);
            aa.invalidate();
            if (!DicomClient.inCommandLineMode()) {
                dialog.pack();
            }
            if (scrollPane != null) {
                JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
            }
        }
    }

    /**
     * Construct the anonymization panel.
     * 
     * @return The anonymization panel.
     * @throws DicomException
     */
    private JComponent constructAnonPanel() throws DicomException {
        anonPanel = new JPanel();

        GridLayout gridLayout = new GridLayout(0, 2);
        gridLayout.setVgap(10);
        anonPanel.setLayout(gridLayout);

        int gap = 10;
        anonPanel.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));

        scrollPane = new JScrollPane(anonPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
        return scrollPane;
    }

    /**
     * Construct the panel that hold the directory chooser options.
     * 
     * @return Directory chooser panel.
     */
    private JPanel constructDirectoryChooserPanel() {
        JPanel panel = new JPanel();
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        layout.setHgap(3);
        panel.setLayout(layout);

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 30, 10));

        return panel;
    }

    private JComponent constructSearchPanel() {
        showDetails = new JCheckBox();
        showDetails.addActionListener(this);
        showDetails.setText("Details");
        showDetails.setToolTipText("<html>Show DICOM<br>attribute details</html>");

        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(DicomClient.FONT_MEDIUM);
        searchField = new JTextField(30);
        searchField.getDocument().addDocumentListener(this);
        searchField.addActionListener(this);

        searchPrev = new BasicArrowButton(BasicArrowButton.NORTH);
        searchPrev.addActionListener(this);
        searchPrev.setToolTipText("<html>Previous<br>match</html>");

        searchNext = new BasicArrowButton(BasicArrowButton.SOUTH);
        searchNext.addActionListener(this);
        searchNext.setToolTipText("<html>Next<br>match</html>");

        JPanel panel = new JPanel();
        panel.add(showDetails);
        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(searchPrev);
        panel.add(searchNext);

        matchCountLabel = new JLabel();
        matchCountLabel.setFont(DicomClient.FONT_MEDIUM);

        panel.add(matchCountLabel);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        return panel;
    }

    /**
     * Construct the panel that hold the buttons.
     * 
     * @return Button panel.
     */
    private JPanel constructButtonPanel() {
        JPanel buttonPanel = new JPanel();
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        layout.setHgap(30);
        buttonPanel.setLayout(layout);

        // buttonPanel.add(constructSearchPanel());
        // buttonPanel.add(new JLabel(" "));

        selectAllButton = new JButton("All");
        selectAllButton.addActionListener(this);
        selectAllButton.setToolTipText("Select all checkboxes");
        buttonPanel.add(selectAllButton);

        selectNoneButton = new JButton("None");
        selectNoneButton.addActionListener(this);
        selectNoneButton.setToolTipText("Un-Select all checkboxes");
        buttonPanel.add(selectNoneButton);

        selectDefaultsButton = new JButton("Defaults");
        selectDefaultsButton.addActionListener(this);
        selectDefaultsButton.setToolTipText("<html>Reset selections to<br>the defaults</html>");
        buttonPanel.add(selectDefaultsButton);

        closeButton = new JButton("Close");
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        return buttonPanel;
    }

    private boolean isAnonymizable(AttributeTag tag) {
        if (tag.getGroup() == 2) return false;
        byte[] vr = CustomDictionary.getInstance().getValueRepresentationFromTag(tag);
        if (vr == null) return false;
        if (ValueRepresentation.isOtherByteOrWordVR(vr)) return false;
        if (ValueRepresentation.isSequenceVR(vr)) return false;
        return true;
    }

    private JPanel constructNorthPanel() {
        JPanel panel = new JPanel();
        panel.add(AnonymizeDateTime.getInstance().getMainPanel());
        return panel;
    }

    /**
     * Construct panel that goes on the southern part of the window.
     * 
     * @return South (bottom) panel.
     */
    private JPanel constructSouthPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(constructDirectoryChooserPanel(), BorderLayout.NORTH);
        JPanel southSouth = new JPanel();
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        layout.setHgap(10);
        southSouth.setLayout(layout);
        southSouth.add(constructSearchPanel());
        southSouth.add(constructButtonPanel());

        JScrollPane scroll = new JScrollPane(southSouth);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        panel.add(scroll, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Make sure that all tags in the given attribute list are in the list of tags.
     * 
     * @param attributeList
     *            List containing potentially new tags.
     */
    @SuppressWarnings("unchecked")
    public synchronized void updateTagList(AttributeList attributeList) {
        AttributeList newAttributeList = new AttributeList();

        for (Object oAttr : attributeList.values()) {
            Attribute attribute = (Attribute) oAttr;
            AttributeTag tag = attribute.getTag();
            if ((knownAttributes.get(tag) == null) && (!tag.equals(TagFromName.PatientID) && (!tag.equals(TagFromName.PatientName)))) {
                try {
                    String value = "";
                    Attribute newAttr = AttributeFactory.newAttribute(new AttributeTag(tag.getGroup(), tag.getElement()));
                    try {
                        newAttr.addValue(value);
                    }
                    catch (Exception e) {
                    }
                    newAttributeList.put(newAttr);
                    knownAttributes.put(newAttr);
                    tag = new AttributeTag(tag.getGroup(), tag.getElement()); // avoid memory leaks
                    if (defaultList.get(tag) != null) {
                        value = defaultList.get(tag).getSingleStringValueOrEmptyString();
                    }
                }
                catch (DicomException e) {
                    Log.get().warning("Unexpected exception while adding new tag to anonymize list: " + e);
                }
            }
            if (attribute instanceof SequenceAttribute) {
                Iterator<?> si = ((SequenceAttribute) attribute).iterator();
                while (si.hasNext()) {
                    SequenceItem item = (SequenceItem) si.next();
                    updateTagList(item.getAttributeList());
                }
            }
        }

        // If there are new attributes, then update the
        // sorted list of tag names
        if (!newAttributeList.isEmpty()) {
            TreeSet<String> tagListSorted = new TreeSet<String>();

            for (Object attrO : newAttributeList.entrySet()) {
                Map.Entry<AttributeTag, Attribute> mapEntry = (Map.Entry<AttributeTag, Attribute>) attrO;
                Attribute attr = mapEntry.getValue();
                String name = CustomDictionary.getName(attr);
                if ((name != null) && (name.length() > 0) && (!tagListSorted.contains(name))) {
                    tagListSorted.add(name);
                }
            }

            for (AttributeTag tag : (Set<AttributeTag>) newAttributeList.keySet()) {
                addAttribute(tag);
            }
            search();
        }
    }

    private Container mainContainer = null;

    /**
     * Build the GUI for anonymization options.
     * 
     * @throws DicomException
     * 
     */
    private AnonymizeGUI() throws DicomException {

        if (DicomClient.inCommandLineMode()) {
            mainContainer = new Container();
            mainContainer.add(constructAnonPanel(), BorderLayout.CENTER);
            mainContainer.add(constructSouthPanel(), BorderLayout.SOUTH);

        }
        else {
            dialog = new JDialog(DicomClient.getInstance().getFrame(), false);
            dialog.setTitle(WINDOW_TITLE);

            JPanel panel = new JPanel();
            dialog.getContentPane().add(panel);
            panel.setLayout(new BorderLayout());

            panel.add(constructNorthPanel(), BorderLayout.NORTH);

            panel.add(constructAnonPanel(), BorderLayout.CENTER);

            panel.add(constructSouthPanel(), BorderLayout.SOUTH);

            DicomClient.setColor(dialog.getContentPane());

            dialog.setPreferredSize(PREFERRED_SIZE);
            dialog.pack();
        }
    }

    public static AnonymizeGUI getInstance() {
        if (anonymizeGUI == null)
            try {
            anonymizeGUI = new AnonymizeGUI();
            }
            catch (DicomException e) {
            Log.get().severe("Unable to construct anonymize GUI: " + Log.fmtEx(e));
            }
        return anonymizeGUI;
    }

    /**
     * Get the attribute list containing tags and values to be used
     * for anonymization.
     * 
     * @return List of attributes to replace.
     */
    public AttributeList getAttributeList() {
        AttributeList attributeList = new AttributeList();
        for (Component c : anonPanel.getComponents()) {
            AnonymizeAttribute aa = (AnonymizeAttribute) c;
            if (aa.getActive()) {
                try {
                    Attribute attribute = AttributeFactory.newAttribute(aa.getTag());
                    attribute.addValue(aa.getValue());
                    attributeList.put(attribute);
                }
                catch (DicomException e) {
                    Log.get().warning("Unexpected value for attribute.  Ignored. " + Log.fmtEx(e));
                }
            }
        }

        return attributeList;
    }

}
