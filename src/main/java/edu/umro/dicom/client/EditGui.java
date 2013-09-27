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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.SequenceAttribute;

import edu.umro.util.Log;

/**
 * GUI that controls editing.
 * 
 * @author irrer
 *
 */
public class EditGui implements ActionListener {

    /** Preferred size of screen. */
    private static final Dimension PREFERRED_SIZE = new Dimension(600, 700);

    private static final String CARD_MAIN = "main";
    private static final String CARD_CREATE = "create";
    private static final String CARD_UPDATE = "update";

    /** Ordered list of edits made by the user. */
    private LinkedList<Edit> editHistory = new LinkedList<Edit>();

    /** Ordered list of undo's made by the user. */
    private LinkedList<Edit> undoHistory = new LinkedList<Edit>();

    private JDialog dialog = null;
    private Container mainContainer = null;
    private Preview preview = null;

    /** Panel that switches back and forth between image and text viewing modes. */
    private JPanel cardPanel = null;

    private JButton cancelButton = null;
    private JButton saveButton = null;
    private JButton undoButton = null;
    private JButton redoButton = null;
    private JCheckBox applyToAllSlices = null;

    private JButton createButton = null;
    private JButton updateButton = null;
    private JButton copyButton = null;
    private JButton deleteButton = null;

    private JButton createCancelButton = null;
    private JButton updateCancelButton = null;

    private JButton createApplyButton = null;
    private JButton updateApplyButton = null;

    private UpdateGui updateGui; 
    private CreateGui createGui; 
    
    private String currentCard;
            
    /** Last attribute to be selected for editing. */
    private AttributeLocation attributeLocation = null;

    /**
     * Layout that switches back and forth between image and text viewing modes.
     */
    private CardLayout cardLayout = null;

    private JComponent buildButtonPanelCenter() {
        JPanel boxPanel = new JPanel();
        BoxLayout layout = new BoxLayout(boxPanel, BoxLayout.Y_AXIS);
        // FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        boxPanel.setLayout(layout);

        createButton = new JButton("Create");

        updateButton = new JButton("Update");

        copyButton = new JButton("Copy");

        deleteButton = new JButton("Delete");

        int gapLarge = 30;
        int gapSmall = gapLarge / 2;
        Border borderLarge = BorderFactory.createEmptyBorder(gapLarge, gapLarge, gapLarge, gapLarge);
        Border borderSmall = BorderFactory.createEmptyBorder(gapSmall, gapSmall*2, gapSmall, gapSmall*2);
        JButton[] list = { createButton, updateButton, copyButton, deleteButton };
        for (JButton button : list) {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout(FlowLayout.CENTER));
            button.addActionListener(this);
            button.setBorder(borderSmall);
            button.setFont(DicomClient.FONT_LARGE);
            p.setBorder(borderLarge);
            p.add(button);
            boxPanel.add(p);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(boxPanel);
        panel.setBorder(borderLarge);
        return panel;
    }

    private JComponent buildButtonPanelSouth() {
        JPanel panel = new JPanel();
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        layout.setHgap(30);
        layout.setVgap(30);
        panel.setLayout(layout);

        applyToAllSlices = new JCheckBox("All");
        applyToAllSlices.addActionListener(this);
        applyToAllSlices.setToolTipText("<html>Apply changes to all<br>slices in this series.<br>If not checked, only<br>change the current slice.</html>");
        panel.add(applyToAllSlices);

        undoButton = new JButton("Undo");
        undoButton.addActionListener(this);
        panel.add(undoButton);

        redoButton = new JButton("Redo");
        redoButton.addActionListener(this);
        panel.add(redoButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setToolTipText("<html>Discard all edits without<br>saving anything.</html>");
        panel.add(cancelButton);

        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        saveButton.setToolTipText("<html>Write new version<br>of file(s).</html>");
        panel.add(saveButton);

        return panel;
    }

    private JComponent buildButtonPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(buildButtonPanelCenter(), BorderLayout.CENTER);
        panel.add(buildButtonPanelSouth(), BorderLayout.SOUTH);

        return panel;
    }

    private JComponent buildCreatePanel() {
        JPanel panel = new JPanel();
        return panel;
    }

    private JComponent buildCenter() {
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        cardPanel.add(buildButtonPanel(), CARD_MAIN);
        cardPanel.add(buildCreatePanel(), CARD_CREATE);
        updateGui = new UpdateGui(this);
        cardPanel.add(updateGui, CARD_UPDATE);
        setCard(CARD_MAIN);

        return cardPanel;
    }

    private JComponent buildSouth() {
        JPanel panel = new JPanel();

        return panel;
    }

    private void setTitle() {
        String title = "Editing " + preview.getPreviewedSeries().getDescription();
        dialog.setTitle(title);
    }

    public EditGui(Preview preview) {
        this.preview = preview;
        mainContainer = new Container();
        if (!DicomClient.inCommandLineMode()) {
            dialog = new JDialog(DicomClient.getInstance().getFrame(), false);
            setTitle();
        }
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(new JLabel("EditGui.EditGui :: "));
        panel.add(buildCenter(), BorderLayout.CENTER);
        panel.add(buildSouth(), BorderLayout.SOUTH);

        DicomClient.setColor(panel);

        if (DicomClient.inCommandLineMode()) {
            mainContainer.add(panel);
        }
        else {
            resetDoButtons();
            dialog.setPreferredSize(PREFERRED_SIZE);
            dialog.getContentPane().add(panel);
            dialog.pack();
            dialog.setVisible(true);
        }
        setAttributeLocation(null);
    }
    
    private void setCard(String cardName) {
        if (cardName.equals(CARD_CREATE)) createGui.setAttributeLocation(attributeLocation);
        if (cardName.equals(CARD_UPDATE)) updateGui.setAttributeLocation(attributeLocation);
        cardLayout.show(cardPanel, cardName);
        currentCard = cardName;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource().equals(createButton)) setCard(CARD_CREATE);
        if (ev.getSource().equals(updateButton)) {
            updateGui.setAttributeLocation(attributeLocation);
            setCard(CARD_UPDATE);
        }

        if (ev.getSource().equals(createCancelButton) || ev.getSource().equals(updateCancelButton)) setCard(CARD_MAIN);

        if (ev.getSource().equals(saveButton)) save();

        if (ev.getSource().equals(undoButton)) undo();

        if (ev.getSource().equals(redoButton)) redo();

        if (ev.getSource().equals(cancelButton)) cancel();

        if (ev.getSource().equals(copyButton)) copy();

        if (ev.getSource().equals(deleteButton)) delete();
    }
    
    private void resetDoButtons() {
        if (editHistory.isEmpty()) {
            undoButton.setEnabled(false);
            undoButton.setText("Undo");
        }
        else {
            undoButton.setEnabled(true);
            undoButton.setText("Undo " + editHistory.getLast().description());
        }
        
        if (undoHistory.isEmpty()) {
            redoButton.setEnabled(false);
            redoButton.setText("Redo");
        }
        else {
            redoButton.setEnabled(true);
            redoButton.setText("Redo " + undoHistory.getFirst().description());
        }
    }
    
    private void undo() {
        if (!editHistory.isEmpty()) {
            Edit e = editHistory.removeLast();
            undoHistory.push(e);
            resetDoButtons();
            preview.showDicom();
        }
    }
    
    private void redo() {
        if (!undoHistory.isEmpty()) {
            Edit e = undoHistory.removeFirst();
            editHistory.add(e);
            resetDoButtons();
            preview.showDicom();
        }

    }
    
    private void cancel() {
        if (editHistory.size() > 0) {
            String s = (editHistory.size() == 1) ? "" : "s"; 
            String msg = "You have made " + editHistory.size() + " change" + s + ".  Save your change" + s + "?";
            String title = editHistory.size() + " change" + s + " will be lost";
            String[] buttonNameList = { "Save", "Don't save", "Cancel" };
            Alert alert = new Alert(msg, title, buttonNameList, new Dimension(400, 300), true);
            switch (alert.selectedButton) {
            case 0:
                save();
                dialog.setVisible(false);
                break;
            case 1:
                Log.get().info("Discarding " + editHistory.size() + " edits.");
                preview.terminateEditing();
                dialog.setVisible(false);
                break;
            case 2:
                break;
            }
        }
        else {
            dialog.setVisible(false);
            preview.terminateEditing();
        }
    }
        
    private void save() {
        System.out.println("Save not yet supported");  // TODO
        dialog.setVisible(false);
        preview.terminateEditing();
    }

    private void copy() {
        System.out.println("Copy not yet implemented.");   // TODO
    }
    
    public void addNewEdit(Edit edit) {
        editHistory.add(edit);
        undoHistory.clear();
        setCard(CARD_MAIN);
        setAttributeLocation(null);
        resetDoButtons();
        preview.showDicom();
    }
    
    public void setToMainMode() {
        setCard(CARD_MAIN);
    }

    private void delete() {
        if (attributeLocation != null) addNewEdit(new EditDelete(attributeLocation));
    }

    public void performEdits(AttributeList attributeList) {
        for (Edit edit : editHistory) {
            edit.doEdit(attributeList);
        }
    }
    
    public void setAttributeLocation(AttributeLocation attributeLocation) {
        this.attributeLocation = attributeLocation;
        deleteButton.setEnabled(attributeLocation != null);
        updateButton.setEnabled((attributeLocation != null) && (attributeLocation.attribute != null) && (!(attributeLocation.attribute instanceof SequenceAttribute)));
        
        if (attributeLocation == null) {
            String tip = "<html>Select an entry in<br>the preview window</html>";
            deleteButton.setToolTipText(tip);
            updateButton.setToolTipText(tip);
        }
        else {
            String desc;
            if (attributeLocation.attribute == null) {
                int item = attributeLocation.getParentIndex() + 1;
                desc = "Item " + item + " of " + CustomDictionary.getInstance().getNameFromTag(attributeLocation.getParentTag());
            }
            else {
                desc = CustomDictionary.getInstance().getNameFromTag(attributeLocation.attribute.getTag());
            }
                        
            deleteButton.setToolTipText("Delete " + desc);
            updateButton.setToolTipText("Update " + desc);
            
            if (currentCard.equals(CARD_UPDATE)) updateGui.setAttributeLocation(attributeLocation);
            if (currentCard.equals(CARD_CREATE)) createGui.setAttributeLocation(attributeLocation);
        }
    }
    
}
