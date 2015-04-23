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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

    private Attribute attribute = null;

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
    
    /**
     * This is called if the original value is null, meaning that the value does
     * not exist.  If the value is an empty string then it does exist.  In this
     * case the value is considered modified by the user if they have increased
     * the number of fields or have added text to any of the fields.  If there is
     * only one field with no text, then the value is considered modified.  This
     * is done because the field is generated when the interface is shown, even
     * though the user did not do anything.  This represents how DICOM should work,
     * that there are no null fields.
     * 
     * The possible down side is that the user wants to replace a null field with
     * an empty string, in which case the user has to explicitly click the Update
     * button.
     * 
     * @return
     */
    private boolean nullValueModified() {
        if (getComponentCount() == 1) {
            ValueGui vg = (ValueGui)getComponent(0);
            String value = vg.textField.getText();
            if (value.length() == 0) return false;
        }
        return true;
    }

    public boolean isModified() {
        if (attribute != null) {
            try {
                String[] oldValueList = attribute.getStringValues();
                
                if (oldValueList == null) return nullValueModified();
                
                if ((oldValueList.length != getComponentCount())) return true;
                for (int v = 0; v < oldValueList.length; v++) {
                    ValueGui vg = (ValueGui) (getComponent(v));
                    if (!(vg.textField.getText().equals(oldValueList[v]))) return true;
                }
            }
            catch (DicomException e) {
                String name = CustomDictionary.getName(attribute);
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
    
    public void setAttribute(Attribute attribute) {
        try {
            this.attribute = attribute;
            removeAll();
            if ((attribute == null) || (attribute.getStringValues() == null)) {
                add(new ValueGui(this, ""));
            }
            else {
                for (String value : attribute.getStringValues()) {
                    add(new ValueGui(this, value));
                }
            }
            setupEnabled();
        }
        catch (DicomException e) {
            Log.get().severe("Unexpected error in MultipleValueGui.setAttribute: " + Log.fmtEx(e));
        }
    }

    public MultipleValueGui(Attribute attribute) {
        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(boxLayout);
        setAttribute(attribute);
    }
}
