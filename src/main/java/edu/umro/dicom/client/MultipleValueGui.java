package edu.umro.dicom.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.plaf.basic.BasicArrowButton;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;

import edu.umro.util.Log;

public class MultipleValueGui extends JPanel implements ActionListener {

    /** default */
    private static final long serialVersionUID = 1L;


    private class ValueGui extends JPanel {
        /** default */
        private static final long serialVersionUID = 1L;
        JButton duplicateButton;
        JButton deleteButton;
        BasicArrowButton upButton;
        BasicArrowButton downButton;
        JTextField textField;

        public ValueGui(MultipleValueGui parent, String value) {
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

            duplicateButton = new JButton("+");
            deleteButton = new JButton("-");
            upButton = new BasicArrowButton(BasicArrowButton.NORTH);
            downButton = new BasicArrowButton(BasicArrowButton.SOUTH);

            duplicateButton.addActionListener(parent);
            deleteButton.addActionListener(parent);
            upButton.addActionListener(parent);
            downButton.addActionListener(parent);

            duplicateButton.setToolTipText("Add New");
            deleteButton.setToolTipText("Remove");
            upButton.setToolTipText("Move Up");
            downButton.setToolTipText("Move Down");

            textField = new JTextField(UpdateGui.TEXT_FIELD_COLUMNS);
            textField.setText(value);

            buttonPanel.add(duplicateButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(upButton);
            buttonPanel.add(downButton);

            SpringLayout springLayout = new SpringLayout();
            setLayout(springLayout);

            add(buttonPanel, BorderLayout.WEST);
            add(textField, BorderLayout.CENTER);
            setPreferredSize(new Dimension(400, 30));

            springLayout.putConstraint(SpringLayout.WEST, textField, 10, SpringLayout.EAST, buttonPanel);
            springLayout.putConstraint(SpringLayout.WEST, this, 5, SpringLayout.WEST, buttonPanel);
            springLayout.putConstraint(SpringLayout.EAST, textField, -5, SpringLayout.EAST, this);
            springLayout.putConstraint(SpringLayout.NORTH, textField, 5, SpringLayout.NORTH, this);
            springLayout.putConstraint(SpringLayout.SOUTH, textField, -5, SpringLayout.SOUTH, this);
        }
    }

    private AttributeLocation attributeLocation = null;

    private void duplicate(ValueGui valueGui) {
        ValueGui newValuGui = new ValueGui(this, valueGui.textField.getText());
        add(newValuGui, indexOf(valueGui));
    }

    private void delete(ValueGui valueGui) {
        remove(valueGui);
    }

    private void up(ValueGui valueGui) {
        int i = indexOf(valueGui);
        remove(valueGui);
        add(valueGui, i - 1);
    }

    private void down(ValueGui valueGui) {
        int i = indexOf(valueGui);
        remove(valueGui);
        add(valueGui, i + 1);
    }

    private ValueGui getValueGui(Object src) {
        return (ValueGui) (((JComponent) src).getParent().getParent());
    }

    private int indexOf(Component component) {
        for (int i = 0; i < getComponentCount(); i++)
            if (getComponent(i) == component) return i;
        return -1;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Object src = e.getSource();
        for (Component c : getComponents()) {
            ValueGui vg = (ValueGui) c;
            if (src.equals(vg.duplicateButton)) duplicate(getValueGui(src));
            if (src.equals(vg.deleteButton)) delete(getValueGui(src));
            if (src.equals(vg.upButton)) up(getValueGui(src));
            if (src.equals(vg.downButton)) down(getValueGui(src));
        }
        setupEnabled();
    }

    public boolean isModified() {
        if ((attributeLocation != null) || (attributeLocation.getAttribute() != null)) {
            { // TODO remove
                boolean isUp = UpdateGui.isUpdateable(attributeLocation.getAttribute());
                System.out.println("MultipleValueGui.isModified attrLoc: " + attributeLocation + "  updateable: " + isUp);
                if (!isUp) {
                    System.out.println("MultipleValueGui.isModified BAAAAAD");
                }
            }
            try {
                String[] oldValueList = attributeLocation.getAttribute().getStringValues();
                if ((oldValueList == null) || (oldValueList.length != getComponentCount())) return true;
                for (int v = 0; v < oldValueList.length; v++) {
                    ValueGui vg = (ValueGui) (getComponent(v));
                    if (!(vg.textField.getText().equals(oldValueList[v]))) return true;
                }
            }
            catch (DicomException e) {
                String name = CustomDictionary.getName(attributeLocation.getAttribute());
                Log.get().warning("Unable to get DICOM values as strings from attribute " + name + " : " + e);
            }
        }
        return false;
    }

    private void setupEnabled() {
        for (Component c : getComponents()) {
            ValueGui vg = (ValueGui) c;
            vg.deleteButton.setEnabled(true);
            vg.upButton.setEnabled(true);
            vg.downButton.setEnabled(true);
        }
        ((ValueGui) (getComponent(0))).upButton.setEnabled(false);
        ((ValueGui) (getComponent(getComponentCount() - 1))).downButton.setEnabled(false);
        if (getComponentCount() == 1) ((ValueGui) (getComponent(0))).deleteButton.setEnabled(false);
        revalidate();
    }
    
    public Attribute getUpdatedAttribute(AttributeTag tag) throws DicomException {
        Attribute attribute = AttributeFactory.newAttribute(tag, CustomDictionary.getInstance().getValueRepresentationFromTag(tag));
        for (Component c : getComponents()) {
            String value = ((ValueGui)c).textField.getText();
            attribute.addValue(value);
        }
        return attribute;
    }
    
    public void reset() {
        while (getComponentCount() > 1)
            remove(1);
        ValueGui vg = (ValueGui) (getComponent(0));
        vg.textField.setText("");
    }
    
    public void setAttributeLocation(AttributeLocation attributeLocation) {
        System.out.println("MultipleValueGui.setAttributeLocation: " + attributeLocation);
        {   // TODO remove
            if (attributeLocation == null) {
                System.out.println("null Badness!!!!");
            }
            else {
                boolean isUp = UpdateGui.isUpdateable(attributeLocation.getAttribute());
                System.out.println("MultValGui.setAttributeLocation isUpdateable:  " + isUp);
                if (!isUp) {
                    System.out.println("Badness!!!!");
                }
            }
        }
        try {
            this.attributeLocation = attributeLocation;
            removeAll();
            if ((attributeLocation == null) || (attributeLocation.getAttribute() == null) || (attributeLocation.getAttribute().getStringValues() == null)) {
                add(new ValueGui(this, ""));
            }
            else {
                for (String value : attributeLocation.getAttribute().getStringValues()) {
                    add(new ValueGui(this, value));
                }
            }
            setupEnabled();
        }
        catch (DicomException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public MultipleValueGui(AttributeLocation attributeLocation) {
        if (this == null) {   // TODO remove
            GridLayout gridLayout = new GridLayout(0, 1);
            gridLayout.setVgap(10);
            setLayout(gridLayout);
        }
        else {
            BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
            setLayout(boxLayout);
        }
        setAttributeLocation(attributeLocation);
    }
}
