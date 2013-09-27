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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.DicomException;

import edu.umro.dicom.client.CustomDictionary.Multiplicity;
import edu.umro.util.Log;

public class UpdateGui extends JPanel implements ActionListener {

    /** default */
    private static final long serialVersionUID = 1L;

    private static final String CARD_SINGLE = "single";
    private static final String CARD_MULTIPLE = "multiple";
    
    /** Scroll bar increment. */
    private static final int SCROLL_INCREMENT = 12;

    private EditGui editGui;

    private JPanel cardPanel;
    private CardLayout cardLayout;

    private AttributeLocation attributeLocation = null;

    private JButton updateButton = null;
    private JButton cancelButton = null;

    private JLabel singleValueLabel;
    private JTextField singleValueText; // TODO remove

    private JLabel multipleValueLabel;
    private ArrayList<JTextField> multipleValueText;

    MultipleValueGui multipleValueGui;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(cancelButton)) editGui.setToMainMode();
        if ((e.getSource().equals(updateButton) || e.getSource().equals(singleValueText)) && modified()) {
            save();
        }
        else
            editGui.setToMainMode();

    }

    /**
     * Save the user's changes to the edit stack (not saved to disk).
     */
    private void save() {
        try {
            Attribute attribute = AttributeFactory.newAttribute(attributeLocation.getAttribute().getTag());
            if (singleMultiplicity())
                attribute.addValue(singleValueText.getText());
            else
                for (JTextField textField : multipleValueText)
                    attribute.addValue(textField.getText());
            editGui.addNewEdit(new EditUpdate(attributeLocation, attribute));
            attributeLocation = null;
        }
        catch (DicomException e) {
            new Alert("Unable to save update to " + getAttrName() + "\n\n" + e, "Failed to Save Update");
        }
    }

    private boolean singleMultiplicity() {
        return (attributeLocation == null) || (attributeLocation.getAttribute() == null)
                || (CustomDictionary.getInstance().getValueMultiplicity(attributeLocation.getAttribute().getTag()) == Multiplicity.M1);
    }

    private boolean modified() {
        if ((attributeLocation == null) || (attributeLocation.getAttribute() == null)) return false;
        if (singleMultiplicity()) {
            return !(attributeLocation.getAttribute().getSingleStringValueOrEmptyString().equals(singleValueText.getText()));
        }
        else {

            if (multipleValueText == null) return false;
            if (attributeLocation.getAttribute().getVL() != multipleValueText.size()) return false;
            try {
                String[] valueList = attributeLocation.getAttribute().getStringValues();
                for (int i = 0; i < multipleValueText.size(); i++) {
                    if (!multipleValueText.get(i).getText().equals(valueList[i])) return true;
                }
            }
            catch (DicomException e) {
            }
            return false;
        }
    }

    private String getAttrName() {
        return CustomDictionary.getInstance().getNameFromTag(attributeLocation.getAttribute().getTag());
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
        if (modified()) {
            String[] buttonNameList = { "Save", "Don't save" };
            Alert alert = new Alert("Update has not been saved", "Update Not Saved", buttonNameList, new Dimension(400, 300), true);
            switch (alert.selectedButton) {
            case 0:
                save();
                break;
            case 1:
                Log.get().info("Discarding update to " + getAttrName());
                break;
            }
        }
        this.attributeLocation = attributeLocation;

        Attribute attribute = attributeLocation.getAttribute();
        if (CustomDictionary.getInstance().getValueMultiplicity(attribute.getTag()) == Multiplicity.M1)
            setSingleMode(attributeLocation);
        else
            setMultipleMode(attributeLocation);
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

    private JPanel buildSinglePanel() {
        JPanel panel = new JPanel();

        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        layout.setHgap(30);
        layout.setVgap(30);
        panel.setLayout(layout);

        singleValueLabel = new JLabel();
        panel.add(singleValueLabel);

        singleValueText = new JTextField(20);
        panel.add(singleValueText);

        singleValueText.addActionListener(this);

        return panel;
    }
    
    private JPanel buildMultiValueGuiSpringPanel() {
        JPanel mvgPanel = new JPanel();
        SpringLayout springLayout = new SpringLayout();
        mvgPanel.setLayout(springLayout);
        mvgPanel.add(multipleValueGui = new MultipleValueGui(attributeLocation));
        int gap = 15;
        springLayout.putConstraint(SpringLayout.EAST, multipleValueGui, gap, SpringLayout.EAST, mvgPanel);
        springLayout.putConstraint(SpringLayout.WEST, multipleValueGui, gap, SpringLayout.WEST, mvgPanel);
        //springLayout.putConstraint(SpringLayout.NORTH, multipleValueGui, gap, SpringLayout.NORTH, mvgPanel);
        //springLayout.putConstraint(SpringLayout.SOUTH, multipleValueGui, -gap, SpringLayout.SOUTH, mvgPanel);
        
        return mvgPanel;
    }

    private JPanel buildMultiplePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        // panel.setLayout(new FlowLayout());

        multipleValueLabel = new JLabel("", JLabel.CENTER);
        JPanel mvlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mvlPanel.add(multipleValueLabel, BorderLayout.NORTH);
        // mvlPanel.add(multipleValueLabel);

        JPanel flowPanel = new JPanel();
        flowPanel.add(multipleValueGui = new MultipleValueGui(attributeLocation));
        
        JScrollPane scrollPane = new JScrollPane(flowPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
        // scrollPane.add(buildMultiValueGuiSpringPanel());

        // multipleValueGui.setPreferredSize(new Dimension(200, 200));

        panel.add(scrollPane, BorderLayout.CENTER);
        // panel.add(scrollPane);
        return panel;
    }

    private JPanel buildMainPanel() {
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        cardPanel.add(buildSinglePanel(), CARD_SINGLE);
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
