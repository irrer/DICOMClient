package edu.umro.dicom.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.plaf.basic.BasicArrowButton;

import com.pixelmed.dicom.DicomException;


public class MultipleValueGui extends JPanel implements ActionListener {

    /** default */
    private static final long serialVersionUID = 1L;

    private static final int TEXT_FIELD_COLUMNS = 40;

    private class ValueGui extends JPanel {
        /** default */
        private static final long serialVersionUID = 1L;
        private JButton duplicateButton;
        private JButton deleteButton;
        private BasicArrowButton upButton;
        private BasicArrowButton downButton;
        private JTextField textField;

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
            
            textField = new JTextField(TEXT_FIELD_COLUMNS);
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



    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
    }
    
    public void setAttributeLocation(AttributeLocation attributeLocation) {
        try {
            removeAll();
            if ((attributeLocation == null) || (attributeLocation.getAttribute() == null) || (attributeLocation.getAttribute().getStringValues() == null)) {
                add(new ValueGui(this, ""));
            }
            else {
                for (String value : attributeLocation.getAttribute().getStringValues()) {
                    add(new ValueGui(this, value));
                }
            }

        }
        catch (DicomException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public MultipleValueGui(AttributeLocation attributeLocation) {
        if (this == null) {
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
