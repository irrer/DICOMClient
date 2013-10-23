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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

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
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SequenceAttribute;

import edu.umro.dicom.common.Util;
import edu.umro.util.Log;

/**
 * GUI that controls editing.
 * 
 * @author irrer
 *
 */
public class EditGui implements ActionListener, WindowListener {

    /** Preferred size of screen. */
    private static final Dimension PREFERRED_SIZE = new Dimension(600, 700);

    private static final String CARD_MAIN = "main";
    private static final String CARD_CREATE = "create";
    private static final String CARD_UPDATE = "update";

    /** Ordered list of edits made by the user. */
    private LinkedList<Edit> editHistory = new LinkedList<Edit>();

    /** Ordered list of redo's available to the user. */
    private LinkedList<Edit> redoHistory = new LinkedList<Edit>();
    
    /** List of edited DICOM files that have been saved.  This list is kept so that
     * if a user saves the same file multiple times over the course of editing, a
     * new instance of the file will not be created every time. */
    private HashMap<String, File> savedFileList = new HashMap<String, File>();
    
    /** True if DICOM series has been modified since last save. */
    private boolean modified = false;

    /** Reflects current state of EditGui. */
    private String currentCard;
            
    /** Last attribute to be selected for editing. */
    private AttributeLocation attributeLocation = null;
    
    private JDialog dialog = null;
    private Container mainContainer = null;
    private Preview preview = null;

    /** Panel that switches back and forth between image and text viewing modes. */
    private JPanel cardPanel = null;

    private JButton cancelButton = null;
    private JButton saveButton = null;
    private JButton saveCloseButton = null;
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
    
    /**
     * Layout that switches back and forth between image and text viewing modes.
     */
    private CardLayout cardLayout = null;

    private JComponent buildButtonPanelCenter() {
        JPanel innerPanel = new JPanel();
        GridLayout layout = new GridLayout(2, 2);
        innerPanel.setLayout(layout);

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
            innerPanel.add(p);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(innerPanel);
        panel.setBorder(borderLarge);
        return panel;
    }

    private JComponent buildButtonPanelSouth() {
        JPanel lowerPanel = new JPanel();
        FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
        layout.setHgap(30);
        layout.setVgap(30);
        lowerPanel.setLayout(layout);

        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        applyToAllSlices = new JCheckBox("All");
        applyToAllSlices.addActionListener(this);
        applyToAllSlices.setToolTipText("<html>Apply changes to all<br>slices in this series.<br>If not checked, only<br>change the current slice.</html>");
        lowerPanel.add(applyToAllSlices);

        undoButton = new JButton("Undo");
        undoButton.addActionListener(this);
        JPanel undoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        undoPanel.add(undoButton);
        panel.add(undoPanel);
        
        panel.add(new JLabel(" "));  // spacer

        redoButton = new JButton("Redo");
        redoButton.addActionListener(this);
        JPanel redoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        redoPanel.add(redoButton);
        panel.add(redoPanel);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setToolTipText("<html>Discard all edits without<br>saving anything.</html>");
        lowerPanel.add(cancelButton);

        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        saveButton.setToolTipText("<html>Save to<br>file(s).</html>");
        lowerPanel.add(saveButton);

        saveCloseButton = new JButton("Save+Close");
        saveCloseButton.addActionListener(this);
        saveCloseButton.setToolTipText("<html>Save to file(s) and<br>close window.</html>");
        lowerPanel.add(saveCloseButton);

        panel.add(lowerPanel);
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
        Series series = preview.getPreviewedSeries();
        String title = (series == null) ? "Editor" :  "Editing " + preview.getPreviewedSeries().getDescription();
        dialog.setTitle(title);
    }
    
    public void setVisible(boolean visible) {
        if (!visible) {
            cancel();
            editHistory = new LinkedList<Edit>();
            redoHistory = new LinkedList<Edit>();
            savedFileList.clear();
            setModified(false);
            attributeLocation = null;
            setCard(CARD_MAIN);
            resetDoButtons();
        }

        dialog.setVisible(visible);
        DicomClient.getInstance().setEnabled(!visible);
    }

    public boolean isVisible() {
        return dialog.isVisible();
    }

    public EditGui(Preview preview) {
        this.preview = preview;
        mainContainer = new Container();
        if (!DicomClient.inCommandLineMode()) {
            dialog = new JDialog(DicomClient.getInstance().getFrame(), false);
            dialog.addWindowListener(this);
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
        }
        setModified(false);
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

        if (ev.getSource().equals(saveCloseButton)) saveAndClose();

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
        
        if (redoHistory.isEmpty()) {
            redoButton.setEnabled(false);
            redoButton.setText("Redo");
        }
        else {
            redoButton.setEnabled(true);
            redoButton.setText("Redo " + redoHistory.getFirst().description());
        }
    }
    
    private void setModified(boolean modified) {
        this.modified = modified;
        saveButton.setEnabled(modified);
        saveCloseButton.setEnabled(modified);
    }
    
    private void undo() {
        if (!editHistory.isEmpty()) {
            setModified(true);
            Edit e = editHistory.removeLast();
            redoHistory.push(e);
            resetDoButtons();
            preview.showDicom();
        }
    }
    
    private void redo() {
        if (!redoHistory.isEmpty()) {
            setModified(true);
            Edit e = redoHistory.removeFirst();
            editHistory.add(e);
            resetDoButtons();
            preview.showDicom();
        }

    }
    
    /**
     * Determine if there are one or more unsaved edits.
     * 
     * @return True if there are unsaved edits.
     */
    public boolean getModified() {
        return modified;
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
                saveAndClose();
                break;
            case 1:
                Log.get().info("Discarding " + editHistory.size() + " edits.");
                setVisible(false);
                preview.showDicom();
                break;
            case 2:
                break;
            }
        }
        else {
            setVisible(false);
            preview.showDicom();
        }
    }
    
    private String saveOneFile(String fileName) {
        File destFile;
        AttributeList attributeList = new AttributeList();
        try {
            attributeList.read(fileName);
        }
        catch (DicomException e) {
            return "DICOM error while reading file " + fileName + " : " + e.toString();
        }
        catch (IOException e) {
            return "Unable to read file " + fileName + " : " + e.toString();
        }
        performEdits(attributeList);
        if (savedFileList.containsKey(fileName)) {
            destFile = savedFileList.get(fileName);
        }
        else {
            destFile = Series.getNewFile(attributeList);
            savedFileList.put(fileName, destFile);
        }
        destFile.delete();
        try {
            attributeList.write(destFile, Util.DEFAULT_TRANSFER_SYNTAX, true, true);
            return null;
        }
        catch (IOException e) {
            return "Unable to write file " + destFile + " : " + e.toString();
        }
        catch (DicomException e) {
            return "DICOM error while writing file " + destFile + " : " + e.toString();
        }
    }
        
    private boolean save() {
        Series series = preview.getPreviewedSeries();
        if (series != null) {
            if (applyToAllSlices.isSelected()) {
                for (String fileName : series.getFileNameList()) {
                    String msg = saveOneFile(fileName);
                    if (msg != null) {
                        Log.get().severe(msg);
                        new Alert(msg, "Error Saving Edited File");
                        return false;
                    }
                }
            }
            else {
                String fileName = (String)(series.getFileNameList().toArray()[preview.getCurrentSlice()-1]);
                saveOneFile(fileName);
            }
            setModified(false);
            return true;
        }
        return false;
    }
    
    /**
     * Save the DICOM file(s), close the dialog, and discard edit history.  If the save is not successful, then
     * leave the dialog open and keep the edit history.
     * 
     */
    private void saveAndClose() {
        if (save()) {
            redoHistory.clear();
            editHistory.clear();
            setVisible(false);
        }
    }

    private void copy() {
        System.out.println("Copy not yet implemented.");   // TODO
    }
    
    /**
     * Push a new edit action onto the <code>editHistory</code> stack.
     * 
     * @param edit
     */
    public void addNewEdit(Edit edit) {
        setModified(true);
        editHistory.add(edit);
        redoHistory.clear();
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
    
    private boolean editInProgress() {
        if (attributeLocation != null) {
            if (currentCard.equals(CARD_UPDATE) && updateGui.isModified()) {
                return true;
            }
            if (currentCard.equals(CARD_CREATE) && createGui.isModified()) {
                return true;
            }
        }
        return false;
    }
    
    public void setAttributeLocation(AttributeLocation attributeLocation) {
        
        if (editInProgress()) {
            String modeName = currentCard.equals(CARD_UPDATE) ? "Update" : "Create";
            String[] buttonNameList = { "Resume", "Save", "Discard" };
            String msg = "<html>" + modeName + " has not been saved<br><p><br>" +
                    "&nbsp; &nbsp; &nbsp; &nbsp; <b>Resume</b> : Resume current editing<br><p><br>" +
                    "&nbsp; &nbsp; &nbsp; &nbsp; <b>Save</b> : Save change and continue<br><p><br>" +
                    "&nbsp; &nbsp; &nbsp; &nbsp; <b>Discard</b> : Discard change and continue</html>";
                    
            Alert alert = new Alert(msg, modeName + " Not Saved", buttonNameList, new Dimension(400, 300), true);
            switch (alert.selectedButton) {
            case 0: // Resume
                return;
            case 1: // Save and continue
                if (currentCard.equals(CARD_UPDATE)) updateGui.save();
                if (currentCard.equals(CARD_CREATE)) createGui.save();
                return;
            case 2: // discard
                Log.get().info("Discarding change to " + CustomDictionary.getName(this.attributeLocation.getAttribute()));
                break;
            }
        }
        
        this.attributeLocation = attributeLocation;
        
        deleteButton.setEnabled(attributeLocation != null);
        updateButton.setEnabled((attributeLocation != null) && (attributeLocation.attribute != null) && (!(attributeLocation.attribute instanceof SequenceAttribute)));
        copyButton.setEnabled((attributeLocation != null) && (attributeLocation.attribute == null));
        
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
                desc = CustomDictionary.getName(attributeLocation);
            }
                        
            deleteButton.setToolTipText("Delete " + desc);
            updateButton.setToolTipText("Update " + desc);
            
            if (currentCard.equals(CARD_UPDATE)) {
                if (UpdateGui.isUpdateable(attributeLocation)) updateGui.setAttributeLocation(attributeLocation);
                else setCard(CARD_MAIN);
            }
            if (currentCard.equals(CARD_CREATE)) createGui.setAttributeLocation(attributeLocation);
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        cancel();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        System.out.println("windowClosed"); // TODO remove
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        System.out.println("windowDeactivated"); // TODO remove
    }
    
}
