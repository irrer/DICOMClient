package edu.umro.dicom.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.TagFromName;

import edu.umro.dicom.common.Anonymize;
import edu.umro.util.Log;



/**
 * AnonymizeGUI PHI in a DICOM file.
 * 
 * @author irrer
 *
 */
public class AnonymizeGUI extends JDialog implements ActionListener {


    /**
     * Represents one attribute to be anonymized.
     * 
     * @author irrer
     *
     */
    private class AnonymizeAttribute extends JPanel implements ActionListener, ItemListener {
        /** Default ID */
        private static final long serialVersionUID = 1L;

        /** Provides choices of attributes. */
        private JComboBox comboBox = null;

        /** Determines whether an attribute is active. */
        private JCheckBox checkBox = null;

        /** Value to use for anonymization. */
        private JTextField textField = null;

        /** Locks the checkBox from being changed by another thread. */
        private boolean locked = false;


        /**
         * Construct the interface for one attribute.
         * 
         */
        public AnonymizeAttribute() {
            comboBox = new JComboBox();
            comboBox.setMaximumRowCount(30);
            comboBox.addItemListener(this);
            checkBox = new JCheckBox();
            checkBox.setToolTipText("<html>Select for<br>anonymizing</html>");
            checkBox.addActionListener(this);
            textField = new JTextField(12);

            add(comboBox);
            add(checkBox);
            add(textField);

            setChoiceList();
            comboBox.setSelectedIndex(0);
        }


        /**
         * Choose the tag for the attribute.
         * 
         * @param tag Attribute type.
         * 
         */
        public void setTag(AttributeTag tag) {
            String name = dicomDictionary.getNameFromTag(tag);
            comboBox.setSelectedItem(name);
        }


        /**
         * Get the type of attribute to be anonymized.
         * 
         * @return The type of attribute to be anonymized.
         */
        public AttributeTag getTag() {
            return dicomDictionary.getTagFromName((String)(comboBox.getSelectedItem()));
        }


        /**
         * Set the list of choices of types of attributes.
         */
        public synchronized void setChoiceList() {
            locked = true;
            Object previous = comboBox.getSelectedItem();
            comboBox.removeAllItems();
            for (String name : tagListSorted) {
                comboBox.addItem(name);
            }
            comboBox.setSelectedItem(previous);
            locked = false;
        }


        /**
         * Make anonymization active for this attribute.
         * 
         * @param active True if attribute is to be anonymized.
         */
        public void setActive(boolean active) {
            checkBox.setSelected(active);
            comboBox.setEnabled(getActive());
            textField.setEnabled(getActive());
        }


        public boolean getActive() {
            return checkBox.isSelected();
        }


        public void setValue(String value) {
            textField.setText(value);
        }


        public String getValue() {
            return textField.getText();
        }


        @Override
        public void itemStateChanged(ItemEvent e) {
            if (!locked) {
                checkBox.setSelected(true);
            }
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            comboBox.setEnabled(getActive());
            textField.setEnabled(getActive());
        }

        @Override
        public String toString() {
            return comboBox.getSelectedItem() + "  " + (checkBox.isSelected() ? "active" : "inactive") + "  " + textField.getText();
        }

    }

    /** Default ID */
    private static final long serialVersionUID = 1L;

    /** Title of screen. */
    private static final String WINDOW_TITLE = "Anonymize Options";

    /** Preferred size of screen. */
    private static final Dimension PREFERRED_SIZE = new Dimension(640, 830);

    /** Scroll bar increment. */
    private static final int SCROLL_INCREMENT = 20;

    /** List of all tags in all series (superset). */
    private TreeSet<AttributeTag> tagList = null;

    /** Same as <code>tagList</code>, but Names of tags sorted alphabetically.
     * Keeping this list is just a small optimization so that it does not have
     * to be re-created every time it is needed.
     */
    private TreeSet<String> tagListSorted = new TreeSet<String>();

    /** Correlates tags to attributes. */
    private DicomDictionary dicomDictionary = null;

    /** Adds a new anonymize field to the dialog. */
    private JButton addButton = null;

    /** Closes the dialog. */
    private JButton closeButton = null;

    /** Holds the list of anonymizing fields. */
    private JPanel anonPanel = null;

    /** Scrolls list of attributes. */
    private JScrollPane scrollPane = null;

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource().equals(closeButton)) {
            setVisible(false);
        }
        if (ev.getSource().equals(addButton)) {
            GridLayout gridLayout = (GridLayout)anonPanel.getLayout();
            gridLayout.setRows(gridLayout.getRows()+1);
            addAttribute(null, null).setTag(TagFromName.AbortFlag);
        }
    }


    /**
     * Add a new attribute selector to the list.  If parameters are null, then add
     * a default attribute with an empty value.
     * 
     * @param attributeList List of attributes.
     * 
     * @param tag Item to add.
     * 
     * @return The new anonymize attribute.
     */
    private AnonymizeAttribute addAttribute(AttributeList attributeList, AttributeTag tag) {
        AnonymizeAttribute aa = new AnonymizeAttribute();
        aa.setActive(true);
        if (tag != null) {
            aa.setTag(tag);
            if (attributeList != null) {
                aa.setValue(attributeList.get(tag).getSingleStringValueOrEmptyString());
            }
        }
        anonPanel.add(aa);
        aa.invalidate();
        pack();
        if (scrollPane != null) {
            JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMaximum());
        }
        return aa;
    }


    /**
     * Construct the anonymization panel.
     * 
     * @return The anonymization panel.
     */
    @SuppressWarnings("unchecked")
    private JComponent constructAnonPanel() {
        anonPanel = new JPanel();
        AttributeList attributeList = ClientConfig.getInstance().getAnonymizingReplacementList();

        GridLayout gridLayout = new GridLayout(attributeList.size(), 1);
        gridLayout.setVgap(20);
        anonPanel.setLayout(gridLayout);

        updateTagList(attributeList);

        for (AttributeTag tag : (Set<AttributeTag>)attributeList.keySet()) {
            addAttribute(attributeList, tag);

        }

        anonPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        scrollPane = new JScrollPane(anonPanel);
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

        addButton = new JButton("Add");
        addButton.addActionListener(this);
        addButton.setToolTipText("Add a field");
        buttonPanel.add(addButton);

        closeButton = new JButton("Close");
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));

        return buttonPanel;
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
        panel.add(constructButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }


    /**
     * Get the list of all possible tags.
     * 
     * @return The list of all possible tags.
     */
    public TreeSet<AttributeTag> getTagList() {
        return this.tagList;
    }


    /**
     * Make sure that all tags in the given attribute list are in the list of tags.
     * 
     * @param attributeList List containing potentially new tags.
     */
    public void updateTagList(AttributeList attributeList) {
        int oldSize = tagList.size();

        for (Object oAttr : attributeList.values()) {
            Attribute attribute = (Attribute)oAttr;
            tagList.add(attribute.getTag());
            if (attribute instanceof SequenceAttribute) {
                Iterator<?> si = ((SequenceAttribute)attribute).iterator();
                while (si.hasNext()) {
                    SequenceItem item = (SequenceItem)si.next();
                    updateTagList(item.getAttributeList());
                }
            }
        }

        // If the tag list changed (can only increase), then update the
        // sorted list of tag names
        if (tagList.size() != oldSize) {
            for (AttributeTag tag : tagList) {
                String name = dicomDictionary.getNameFromTag(tag);
                if ((name != null) && (name.length() > 0) && (!tagListSorted.contains(name))) {
                    tagListSorted.add(name);
                }
            }

            for (Component aa : anonPanel.getComponents()) {
                ((AnonymizeAttribute)aa).setChoiceList();
            }
        }
    }


    /**
     * Build the GUI for anonymization options.
     * 
     */
    public AnonymizeGUI() {
        super(DicomClient.getInstance(), false);
        setTitle(WINDOW_TITLE);
        tagList = new TreeSet<AttributeTag>();
        dicomDictionary = new DicomDictionary();

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(new BorderLayout());

        panel.add(constructAnonPanel(), BorderLayout.CENTER);

        panel.add(constructSouthPanel(), BorderLayout.SOUTH);

        DicomClient.setColor(getContentPane());

        setPreferredSize(PREFERRED_SIZE);
        pack();
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
            AnonymizeAttribute aa = (AnonymizeAttribute)c;
            if (aa.getActive()) {
                try {
                    Attribute attribute = AttributeFactory.newAttribute(aa.getTag());
                    attribute.addValue(aa.getValue());
                    attributeList.put(attribute);
                }
                catch (DicomException e) {
                    Log.get().logrb(Level.SEVERE, Anonymize.class.getCanonicalName(),
                            "translateGuid", null, "Unexpected value for attribute.  Ignored. ", e);
                }
            }
        }

        return attributeList;
    }

}
