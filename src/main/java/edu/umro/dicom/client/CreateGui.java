package edu.umro.dicom.client;

/*
 * Copyright 2013 Regents of the University of Michigan
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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.ValueRepresentation;

public class CreateGui extends JPanel implements ActionListener {

    /** default */
    private static final long serialVersionUID = 1L;
    
    private static final String CARD_NONE = "none";
    private static final String CARD_SINGLE = "single";
    private static final String CARD_MULTIPLE = "multiple";

    /** Scroll bar increment. */
    private static final int SCROLL_INCREMENT = 12;

    public static final int TEXT_FIELD_COLUMNS = 40;

    private EditGui editGui;

    private JPanel cardPanel;
    private CardLayout cardLayout;

    private AttributeLocation attributeLocation = null;

    private JButton createButton = null;
    private JButton cancelButton = null;

    private JTextField singleValueText;
    
    private TagChooser tagChooser = null;

    private MultipleValueGui multipleValueGui;

    private String prevAttributeName = null;
    
    private Preview preview = null;
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(cancelButton)) editGui.setToMainMode();
        if (e.getSource().equals(createButton) || e.getSource().equals(singleValueText)) {
            save();
            editGui.setToMainMode();
        }

        if ((tagChooser != null) && (e.getSource().equals(tagChooser.getComboBox()))) {
            String currItem = tagChooser.getSelectedName();
            if (currItem != null) {
                if (!currItem.equals(prevAttributeName)) {
                    if ((prevAttributeName == null) || (!currItem.equals(prevAttributeName))) {
                        setAttributeTag(tagChooser.getSelectedTag());
                        prevAttributeName = currItem;
                    }
                }
            }
        }
    }

    /**
     * Save the user's changes to the edit stack (not saved to disk).
     */
    public void save() {
        try {
            AttributeTag tag = tagChooser.getSelectedTag();
            byte[] vr = CustomDictionary.getInstance().getValueRepresentationFromTag(tag);
            Attribute attribute = AttributeFactory.newAttribute(tag, vr);

            if (EditGui.isSingleMultiplicity(tag)) {
                attribute.addValue(singleValueText.getText());
            }

            if (EditGui.isMultipleMultiplicity(tag)) {
                attribute = multipleValueGui.getUpdatedAttribute(tag);
            }

            if (EditGui.isNoMultiplicity(tag)) {
                ;
            }

            if (ValueRepresentation.isSequenceVR(vr)) {
                SequenceAttribute sequenceAttribute = (SequenceAttribute) attribute;
                sequenceAttribute.addItem(new AttributeList());
            }

            if (attributeLocation == null) attributeLocation = editGui.getAttributeLocation();

            if (attributeLocation == null) attributeLocation = new AttributeLocation(0);

            editGui.addNewEdit(new EditCreate(attributeLocation, attribute));
            preview.selectForEdit(attributeLocation);
        }
        catch (DicomException e) {
            new Alert("Unable to save create of " + tagChooser.getSelectedName() + "\n\n" + e, "Failed to Save Create");
        }
    }

    /**
     * Determine if attribute has been modified.
     * 
     * @return True if modified.
     */
    public boolean isModified() {

        AttributeTag tag = tagChooser.getSelectedTag();

        if (EditGui.isSingleMultiplicity(tag)) return (singleValueText.getText().trim().length() != 0);

        if (EditGui.isMultipleMultiplicity(tag)) return multipleValueGui.isModified();

        return false;
    }

    /**
     * Determine if the given attribute can be updated by this GUI.
     * 
     * @param attribute
     *            Attribute to be tested.
     * 
     * @return True if it can be edited.
     */
    public static boolean isCreateable(Attribute attribute) {
        if (attribute == null) return false;
        byte[] vr = attribute.getVR();
        return Preview.vrSet.contains(vr) || ValueRepresentation.isSequenceVR(vr);
    }
    
    public static boolean isCreateable(AttributeLocation attributeLocation) {
        if (attributeLocation == null) return false;
        return isCreateable(attributeLocation.getAttribute());
    }

    private void setAttributeTag(AttributeTag tag) {
        // Even if not single mode, this is required for other card 'show' calls.  It should not be, but it is.
        cardLayout.show(cardPanel, CARD_SINGLE);
        singleValueText.setText("");
        multipleValueGui.reset();
        if (EditGui.isMultipleMultiplicity(tag)) cardLayout.show(cardPanel, CARD_MULTIPLE);
        else {
            if (EditGui.isNoMultiplicity(tag)) cardLayout.show(cardPanel, CARD_NONE);
        }
    }
    
    public void setAttributeLocation(AttributeLocation attributeLocation) {
        this.attributeLocation = attributeLocation;
    }

    /**
     * Build panel that contains the main buttons.
     * 
     * @return Panel that contains the main buttons.
     */
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
        layout.setHgap(30);
        layout.setVgap(30);
        panel.setLayout(layout);

        cancelButton = new JButton("Cancel");
        createButton = new JButton("Create");

        cancelButton.addActionListener(this);
        createButton.addActionListener(this);

        cancelButton.setFont(DicomClient.FONT_MEDIUM);
        createButton.setFont(DicomClient.FONT_MEDIUM);

        panel.add(cancelButton);
        panel.add(createButton);

        return panel;
    }

    private JPanel wrapInCenteredFlow(JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(component);
        return panel;
    }

    private JPanel buildNoValuePanel() {
        JPanel panel = new JPanel();
        return wrapInCenteredFlow(panel);
    }

    private JPanel buildSingleValuePanel() {
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);

        singleValueText = new JTextField(TEXT_FIELD_COLUMNS);
        panel.add(wrapInCenteredFlow(singleValueText));

        singleValueText.addActionListener(this);

        return wrapInCenteredFlow(panel);
    }

    private JScrollPane wrapInScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
        return scrollPane;
    }

    private JComponent buildMultiplePanel() {
        multipleValueGui = new MultipleValueGui(null);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(multipleValueGui, BorderLayout.NORTH);
        panel.add(new JLabel(), BorderLayout.CENTER);
        return wrapInScrollPane(panel);
    }

    private JPanel buildMainPanel() {
        JPanel panel = null;
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        cardPanel.add(buildSingleValuePanel(), CARD_SINGLE);
        cardPanel.add(buildNoValuePanel(), CARD_NONE);
        cardPanel.add(buildMultiplePanel(), CARD_MULTIPLE);
        tagChooser = new TagChooser();
        tagChooser.getComboBox().addActionListener(this);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(tagChooser, BorderLayout.NORTH);
        panel.add(cardPanel, BorderLayout.CENTER);

        return panel;
    }

    public CreateGui(Preview preview, EditGui editGui) {
        this.preview = preview;
        this.editGui = editGui;
        setLayout(new BorderLayout());
        add(buildMainPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

}
