package edu.umro.dicom.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.ValueRepresentation;

import edu.umro.dicom.client.CustomDictionary.Multiplicity;
import edu.umro.util.Log;

public class UpdateGui extends JPanel implements ActionListener {

    /** default */
    private static final long serialVersionUID = 1L;

    private static final String CARD_SINGLE = "single";
    private static final String CARD_MULTIPLE = "multiple";

    /** Scroll bar increment. */
    private static final int SCROLL_INCREMENT = 12;

    public static final int TEXT_FIELD_COLUMNS = 40;

    private EditGui editGui;

    private JPanel cardPanel;
    private CardLayout cardLayout;

    private AttributeLocation attributeLocation = null;

    private JButton updateButton = null;
    private JButton cancelButton = null;

    private JLabel singleValueLabel;
    private JTextField singleValueText;

    private JLabel multipleValueLabel;

    MultipleValueGui multipleValueGui;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(cancelButton)) editGui.setToMainMode();
        if ((e.getSource().equals(updateButton) || e.getSource().equals(singleValueText)) && isModified()) {
            save();
        }
        else
            editGui.setToMainMode();
    }

    /**
     * Save the user's changes to the edit stack (not saved to disk).
     */
    public void save() {
        try {
            Attribute attribute = null;
        
            if (singleMultiplicity()) {
                attribute = AttributeFactory.newAttribute(attributeLocation.getAttribute().getTag());
                attribute.addValue(singleValueText.getText());
            }
            else attribute = multipleValueGui.getUpdatedAttribute();
            editGui.addNewEdit(new EditUpdate(attributeLocation, attribute));
            attributeLocation = null;
        }
        catch (DicomException e) {
            new Alert("Unable to save update to " + getAttrName() + "\n\n" + e, "Failed to Save Update");
        }
    }

    private boolean singleMultiplicity() {
        return (attributeLocation == null) || (attributeLocation.getAttribute() == null)
                || (CustomDictionary.getVM(attributeLocation) == Multiplicity.M1);
    }

    /**
     * Determine if attribute has been modified.
     * 
     * @return True if modified.
     */
    public boolean isModified() {
        if ((attributeLocation == null) || (attributeLocation.getAttribute() == null)) return false;

        if (singleMultiplicity()) {
            return !(attributeLocation.getAttribute().getSingleStringValueOrEmptyString().equals(singleValueText.getText()));
        }
        else {
            return multipleValueGui.isModified();
        }
    }

    private String getAttrName() {
        return CustomDictionary.getName(attributeLocation);
    }
    
    /**
     * Determine if the given attribute can be updated by this GUI.
     * 
     * @param attribute
     *            Attribute to be tested.
     * 
     * @return True if it can be edited.
     */
    public static boolean isUpdateable(Attribute attribute) {
        if (attribute == null) return false;
        byte[] vr = attribute.getVR();
        if (ValueRepresentation.isSequenceVR(vr)) return false;
        if (ValueRepresentation.isOtherByteOrWordVR(vr)) return false;
        if (ValueRepresentation.isOtherUnspecifiedVR(vr)) return false;
        if (ValueRepresentation.isUnknownVR(vr)) return false;
        return true;
    }
    
    public static boolean isUpdateable(AttributeLocation attributeLocation) {
        if (attributeLocation == null) return false;
        return isUpdateable(attributeLocation.getAttribute());
    }

    private void setSingleMode(AttributeLocation attributeLocation) {
        cardLayout.show(cardPanel, CARD_SINGLE);
        singleValueLabel.setText(getAttrName());

        singleValueText.setText(attributeLocation.getAttribute().getSingleStringValueOrEmptyString());
    }

    private void setMultipleMode(AttributeLocation attributeLocation) {
        multipleValueLabel.setText(getAttrName());
        cardLayout.show(cardPanel, CARD_SINGLE); // reliably forces screen
                                                 // redraw
        cardLayout.show(cardPanel, CARD_MULTIPLE);
        multipleValueGui.setAttributeLocation(attributeLocation);
    }
    
    public void setAttributeLocation(AttributeLocation attributeLocation) {
        System.out.println("UpdateGui.setAttributeLocation: " + attributeLocation);
        
        this.attributeLocation = isUpdateable(attributeLocation) ? attributeLocation : null;

        Attribute attribute = this.attributeLocation.getAttribute();
        if (isUpdateable(attribute)) {
            if (CustomDictionary.getVM(attribute) == Multiplicity.M1)
                setSingleMode(this.attributeLocation);
            else
                setMultipleMode(this.attributeLocation);
        }
        else {
            multipleValueGui.setAttributeLocation(null);
            editGui.setToMainMode();
        }
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
        updateButton = new JButton("Update");

        cancelButton.addActionListener(this);
        updateButton.addActionListener(this);

        cancelButton.setFont(DicomClient.FONT_MEDIUM);
        updateButton.setFont(DicomClient.FONT_MEDIUM);

        panel.add(cancelButton);
        panel.add(updateButton);

        return panel;
    }

    private JPanel wrapInCenteredFlow(JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(component);
        return panel;
    }

    private JPanel buildSingleValuePanel() {
        JPanel panel = new JPanel();

        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);

        singleValueLabel = new JLabel("", JLabel.CENTER);
        panel.add(wrapInCenteredFlow(singleValueLabel));

        singleValueText = new JTextField(TEXT_FIELD_COLUMNS);
        panel.add(wrapInCenteredFlow(singleValueText));

        singleValueText.addActionListener(this);

        return wrapInCenteredFlow(panel);
    }

    private JPanel buildMVLabelPanel() {
        multipleValueLabel = new JLabel("", JLabel.CENTER);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(multipleValueLabel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildMVGuiFlowPanel() {
        JPanel flowPanel = new JPanel();
        flowPanel.add(multipleValueGui = new MultipleValueGui(this, attributeLocation));
        return flowPanel;
    }

    private JPanel buildMVGuiBoxPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(multipleValueGui = new MultipleValueGui(this, attributeLocation));
        return panel;
    }

    private JPanel buildMultiValueGuiSpringPanel() {
        JPanel panel = new JPanel();
        SpringLayout springLayout = new SpringLayout();
        panel.setLayout(springLayout);
        panel.add(multipleValueGui = new MultipleValueGui(this, attributeLocation));
        int gap = 15;
        springLayout.putConstraint(SpringLayout.EAST, multipleValueGui, -gap, SpringLayout.EAST, panel);
        springLayout.putConstraint(SpringLayout.WEST, multipleValueGui, gap, SpringLayout.WEST, panel);
        springLayout.putConstraint(SpringLayout.NORTH, multipleValueGui, gap, SpringLayout.NORTH, panel);
        // springLayout.putConstraint(SpringLayout.SOUTH, multipleValueGui,
        // -gap, SpringLayout.SOUTH, mvgPanel);

        return panel;
    }

    private JPanel buildMVGuiBorderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(multipleValueGui = new MultipleValueGui(this, attributeLocation), BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane wrapInScrollPane(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
        return scrollPane;
    }

    private JPanel buildMultiplePanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());
        panel.add(buildMVLabelPanel(), BorderLayout.NORTH);
        panel.add(wrapInScrollPane(buildMVGuiFlowPanel()), BorderLayout.CENTER);
        // panel.add(wrapInScrollPane(buildMultiValueGuiSpringPanel()),
        // BorderLayout.CENTER);
        // panel.add(wrapInScrollPane(buildMVGuiBorderPanel()),
        // BorderLayout.CENTER);
        // panel.add(wrapInScrollPane(buildMVGuiBoxPanel()),
        // BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildMainPanel() {
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        cardPanel.add(buildSingleValuePanel(), CARD_SINGLE);
        cardPanel.add(buildMultiplePanel(), CARD_MULTIPLE);

        return cardPanel;
    }

    public UpdateGui(EditGui editGui) {
        this.editGui = editGui;
        setLayout(new BorderLayout());
        add(buildMainPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

}
