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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.TagFromName;

import edu.umro.util.Log;

/**
 * A GUI widget that lets the user easily choose a DICOM attribute tag.
 * 
 * @author irrer
 *
 */
public class TagChooser extends JPanel implements DocumentListener, KeyListener, ActionListener {

    /** default */
    private static final long serialVersionUID = 1L;

    /** Maximum number of visible entries in the popup. */
    private static final int MAX_VISIBLE = 10;

    private JTextField searchField = null;
    private JComboBox<String> comboBox = null;
    private JCheckBox details = null;
    
    /** Attribute tags on this list are not selectable. */
    private AttributeList forbiddenList = null;
    
    private void update() {
        CustomDictionary customDictionary = CustomDictionary.getInstance();
        String text = searchField.getText();
        String[] terms = text.toLowerCase().split(" ");

        String currentName = getName(getSelectedName());
        comboBox.removeAllItems();

        TreeSet<String> nameList = new TreeSet<String>();

        @SuppressWarnings("unchecked")
        Iterator<AttributeTag> ti = customDictionary.getTagIterator();

        // Put all names in a tree set so they come out sorted
        // alphabetically.
        while (ti.hasNext()) {
            AttributeTag tag = ti.next();
            if ((forbiddenList == null) || (forbiddenList.get(tag) == null)) {
                String name = customDictionary.getNameFromTag((AttributeTag) tag);
                String vr = new String(customDictionary.getValueRepresentationFromTag(tag));
                String vm = customDictionary.getValueMultiplicity(tag).getName();
                String detailsText = String.format("%04x,%04x %2s %-3s", tag.getGroup(), tag.getElement(), vr, vm).toUpperCase();
                if (details.isSelected()) name = detailsText + " " + name;
                boolean ok = true;
                for (String t : terms) {
                    if (!name.toLowerCase().contains(t)) {
                        ok = false;
                        break;
                    }
                }
                if (ok) nameList.add(name);
            }
        }

        HashSet<String> alreadyOnList = new HashSet<String>();
        if (terms.length > 0) {
            for (String name : nameList) {
                if (name.toLowerCase().startsWith(terms[0])) {
                    comboBox.addItem(name);
                    alreadyOnList.add(name);
                }
            }
        }

        for (String name : nameList) {
            if (!alreadyOnList.contains(name)) comboBox.addItem(name);
        }

        int visSize = (comboBox.getItemCount() > MAX_VISIBLE) ? MAX_VISIBLE : comboBox.getItemCount();
        comboBox.setMaximumRowCount(visSize);
        
        if (currentName != null) {
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                String name = getName((String)comboBox.getItemAt(i));
                if (currentName.equals(name)) {
                    comboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    private void updateAndShowComboBoxPopup() {
        update();
        comboBox.setPopupVisible(true);
    }

    public void insertUpdate(DocumentEvent e) {
        updateAndShowComboBoxPopup();
    }

    public void removeUpdate(DocumentEvent e) {
        updateAndShowComboBoxPopup();
    }

    public void changedUpdate(DocumentEvent e) {
        updateAndShowComboBoxPopup();
    }

    
    private void buildSearchField() {
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(this);
        searchField.addKeyListener(this);
    }
    
    private void buildDetails() {
        details = new JCheckBox("Details");
        details.setToolTipText("<html>Show DICOM<br>attribute details</html>");
        details.setSelected(false);
        details.addActionListener(this);
    }
    
    public JComboBox<String> getComboBox() {
        return comboBox;
    }

    public TagChooser() {
        String toolTip = "<html>Enter name fragments separated<br>by blanks to search.</html>";
        JLabel label = new JLabel(" Search: ");
        label.setToolTipText(toolTip);
        buildSearchField();
        searchField.setToolTipText(toolTip);
        buildDetails();
        comboBox = new JComboBox<String>(new String[] { });

        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        GridBagConstraints c = new GridBagConstraints();
        
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        gridBagLayout.setConstraints(label, c);
        add(label);
        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        gridBagLayout.setConstraints(searchField, c);
        add(searchField);
        
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        gridBagLayout.setConstraints(details, c);
        add(details);
        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0;
        gridBagLayout.setConstraints(comboBox, c);
        add(comboBox);

        update();
        final int GAP = 20;
        this.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
    }

    public void keyTyped(KeyEvent e) {
    }
    
    
    /**
     * Move the selection by the given count.
     * 
     * @param incr Relative offset of new position.
     */
    private void incrementComboBoxSelection(int incr) {
        int index = comboBox.getSelectedIndex() + incr;
        int count = comboBox.getItemCount();
        if (index < 0) index = 0;
        if (index >= count) index = count-1;
        comboBox.setSelectedIndex(index);
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
        case KeyEvent.VK_UP:
            comboBox.setPopupVisible(true);
            incrementComboBoxSelection(-1);
            break;
        case KeyEvent.VK_DOWN:
            comboBox.setPopupVisible(true);
            incrementComboBoxSelection(1);
            break;
        case KeyEvent.VK_ENTER:
            comboBox.setPopupVisible(false);
            break;
        default:
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == details) {
            updateAndShowComboBoxPopup();
        }
    }
    
    public void setForbiddenList(AttributeList attributeList) {
        forbiddenList = attributeList;
        update();
    }
    
    private String getName(String name) {
        if (name != null) name = name.replaceAll(".* ", "");
        return name;
    }

    /**
     * Get the name of the selected attribute.
     * 
     * @return The name of the selected attribute.
     */
    public String getSelectedName() {
        return getName((String)comboBox.getSelectedItem());
    }

    public AttributeTag getSelectedTag() {
        AttributeTag tag = null;
        String text = getSelectedName();
        
        if (text != null) {
            tag = CustomDictionary.getInstance().getTagFromName(text);
        }
        return tag;
    }
    
    
    /**
     * For debug only.
     */
    private static void createAndShowGUI() {
        // create and show a window containing the combo box
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(3);
        final TagChooser tagChooser = new TagChooser();
        frame.getContentPane().add(tagChooser);
        frame.pack();
        frame.setVisible(true);

        if (System.out == null) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        while (true) {
                            Thread.sleep(4000);
                            Attribute a = AttributeFactory.newAttribute(TagFromName.PatientID);
                            AttributeList attributeList = new AttributeList();
                            attributeList.put(a);

                            Log.get().info("setting forbidden list...");
                            tagChooser.setForbiddenList(attributeList);
                        }
                    }
                    catch (Exception e) {
                        Log.get().severe("createAndShowGUI Exception: " + Log.fmtEx(e));
                    }
                }
            }).start();
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
