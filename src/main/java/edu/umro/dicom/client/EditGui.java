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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.ValueRepresentation;

import edu.umro.dicom.client.CustomDictionary.Multiplicity;
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
    private static final String CARD_OPTION = "option";

    /** Ordered list of edits made by the user. */
    private LinkedList<Edit> editHistory = new LinkedList<Edit>();

    /** Ordered list of redo's available to the user. */
    private LinkedList<Edit> redoHistory = new LinkedList<Edit>();

    private class SavedFile {
        File file;
        String series;

        public SavedFile(File f, String s) {
            file = f;
            series = s;
        }
    }

    /**
     * List of edited DICOM files that have been saved. This list is kept so that
     * if a user saves the same file multiple times over the course of editing, a
     * new instance of the file will not be created every time.
     */
    private HashMap<File, SavedFile> savedFileList = new HashMap<File, SavedFile>();

    /** Reflects current state of EditGui. */
    private String currentCard;

    /** True indicates that there are unsaved modifications. */
    private boolean modified = false;

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
    private JButton optionButton = null;

    private JCheckBox saveToAllSlices = null;
    private JCheckBox overwrite = null;
    private JCheckBox saveAsDicom = null;
    private JCheckBox saveAsText = null;
    private JCheckBox saveAsPng = null;
    private JCheckBox saveAsXml = null;
    private JButton returnFromOptionsButton = null;

    private JButton createButton = null;
    private JButton updateButton = null;
    private JButton copyButton = null;
    private JButton deleteButton = null;

    private JButton createCancelButton = null;
    private JButton updateCancelButton = null;

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
        JButton[] list = { updateButton, createButton, copyButton, deleteButton };
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

        optionButton = new JButton("Options");
        optionButton.addActionListener(this);
        optionButton.setToolTipText("<html>Select Editing Options</html>");
        lowerPanel.add(optionButton);

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
    
    private JComponent buildOptionPanel() {
        JPanel checkBoxPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS);
        checkBoxPanel.setLayout(boxLayout);
        
        checkBoxPanel.add(new JLabel(" "));
        checkBoxPanel.add(new JLabel(" "));
        
        JLabel title = new JLabel("Editing Options");
        title.setFont(DicomClient.FONT_LARGE);
        checkBoxPanel.add(title);
        
        checkBoxPanel.add(new JLabel(" "));
        checkBoxPanel.add(new JLabel(" "));
        checkBoxPanel.add(new JLabel(" "));

        saveToAllSlices = new JCheckBox("Save to all slices in series");
        saveToAllSlices.setSelected(true);
        saveToAllSlices.addActionListener(this);
        saveToAllSlices.setToolTipText("<html>Save changes to all<br>slices in this series.<br>If not checked, only<br>change the current slice.</html>");
        checkBoxPanel.add(saveToAllSlices);
        checkBoxPanel.add(new JLabel(" "));

        overwrite = new JCheckBox("Over-write original file(s)");
        overwrite.setToolTipText("<html>If checked, save by over-writing the<br>original DICOM files.  If not checked,<br>only write to new files.</html>");
        overwrite.setSelected(false);
        checkBoxPanel.add(overwrite);
        checkBoxPanel.add(new JLabel(" "));

        saveAsDicom = new JCheckBox("Save as DICOM");
        saveAsDicom.setSelected(true);
        checkBoxPanel.add(saveAsDicom);
        checkBoxPanel.add(new JLabel(" "));

        saveAsText = new JCheckBox("Save as text");
        saveAsText.setSelected(false);
        checkBoxPanel.add(saveAsText);
        checkBoxPanel.add(new JLabel(" "));

        saveAsPng = new JCheckBox("Save as PNG image");
        saveAsPng.setSelected(false);
        checkBoxPanel.add(saveAsPng);
        checkBoxPanel.add(new JLabel(" "));

        saveAsXml = new JCheckBox("Save as XML");
        saveAsXml.setSelected(false);
        checkBoxPanel.add(saveAsXml);
        checkBoxPanel.add(new JLabel(" "));

        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        flowPanel.add(checkBoxPanel);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(flowPanel, BorderLayout.CENTER);
        
        returnFromOptionsButton = new JButton("Back to Main");
        returnFromOptionsButton.addActionListener(this);
        JPanel rfobPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        int gap = 20;
        Border borderLarge = BorderFactory.createEmptyBorder(gap, gap, gap, gap);
        rfobPanel.setBorder(borderLarge);
        rfobPanel.add(returnFromOptionsButton);
        
        mainPanel.add(rfobPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }

    private JComponent buildCenter() {
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        cardPanel.add(buildButtonPanel(), CARD_MAIN);
        
        updateGui = new UpdateGui(this);
        cardPanel.add(updateGui, CARD_UPDATE);
        
        createGui = new CreateGui(preview, this);
        cardPanel.add(createGui, CARD_CREATE);
        
        cardPanel.add(buildOptionPanel(), CARD_OPTION);
        
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
        dialog.setVisible(visible);        
        DicomClient.getInstance().setEnabled(!visible);
    }
    
    /**
     * Discard all editing history without saving.
     */
    public void reset() {
        modified = false;
        editHistory.clear();
        redoHistory.clear();
        resetDoButtons();
        attributeLocation = null;
        setCard(CARD_MAIN);
    }

    /**
     * Give the user a chance to save changes.
     * 
     * @return True if user wants to stop editing (either because they saved the
     * changes or they discarded the edits), false if they want to resume editing.
     */
    public boolean letUserSaveIfTheyWantTo() {
        if (!isModified()) return true;  // if no changes, then we're done
        
        // build a dialog to ask the user what they want to do
        String s = (editHistory.size() == 1) ? "" : "s";
        String msg = "You have made " + editHistory.size() + " change" + s + ".  Save your change" + s + "?";
        String title = editHistory.size() + " change" + s + " will be lost";
        String[] buttonNameList = { "Save", "Don't save", "Cancel" };
        Alert alert = new Alert(msg, title, buttonNameList, new Dimension(400, 300), true);
        
        switch (alert.selectedButton) {
        case 0:
            return save();
        case 1:
            Log.get().info("Discarding " + editHistory.size() + " edits.");
            return true;
        case 2:
            return false;
        default:  // should never happen because there is no such button
            return false;
        }
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
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
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
        setAttributeLocation(null);
    }
    
    /**
     * Get the most recently selected attribute location.
     * 
     * @return The most recently selected attribute location.
     */
    public AttributeLocation getAttributeLocation() {
        return attributeLocation;
    }
    
    private void setCard(String cardName) {
        if (cardName.equals(CARD_CREATE)) createGui.setAttributeLocation(attributeLocation);
        if (cardName.equals(CARD_UPDATE)) updateGui.setAttributeLocation(attributeLocation);
        cardLayout.show(cardPanel, cardName);
        currentCard = cardName;
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource().equals(createButton)) setCard(CARD_CREATE);
        if (ev.getSource().equals(updateButton)) {
            updateGui.setAttributeLocation(attributeLocation);
            setCard(CARD_UPDATE);
        }

        if (ev.getSource().equals(createCancelButton) || ev.getSource().equals(updateCancelButton)) setCard(CARD_MAIN);

        if (ev.getSource().equals(saveButton)) save();

        if (ev.getSource().equals(saveCloseButton)) {
            if (save()) {
                reset();
                setVisible(false);
            }
        }

        if (ev.getSource().equals(undoButton)) undo();

        if (ev.getSource().equals(redoButton)) redo();

        if (ev.getSource().equals(optionButton)) setCard(CARD_OPTION);
        
        if (ev.getSource().equals(returnFromOptionsButton)) setCard(CARD_MAIN);
        
        if (ev.getSource().equals(cancelButton)) {
            reset();
            setVisible(false);
        }

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
    
    private void undo() {
        if (!editHistory.isEmpty()) {
            modified = true;
            Edit e = editHistory.removeLast();
            redoHistory.push(e);
            resetDoButtons();
            preview.showDicom();
        }
    }
    
    private void redo() {
        if (!redoHistory.isEmpty()) {
            modified = true;
            Edit e = redoHistory.removeFirst();
            editHistory.add(e);
            resetDoButtons();
            preview.showDicom();
        }

    }
    
    private ArrayList<String> getFileSuffixList() {
        ArrayList<String> list = new ArrayList<String>();
        if (saveAsDicom.isSelected()) list.add(Util.DICOM_SUFFIX);
        if (saveAsText.isSelected()) list.add(Util.TEXT_SUFFIX);
        if (saveAsPng.isSelected()) list.add(Util.PNG_SUFFIX);
        if (saveAsXml.isSelected()) list.add(Util.XML_SUFFIX);
        return list;
    }

    
    /**
     * Check to see if no file exists in the user specified directory with the
     * given prefix and each of the given suffixes.
     * 
     * @param dir
     *            Directory to search.
     * 
     * @param prefix
     *            Base name of file(s).
     * 
     * @param suffixList
     *            Suffix for each file(s).
     * 
     * @return True if none of the files exist, and the prefix may be used to
     *         create new files without overwriting existing files.
     */
    private static boolean testPrefix(String prefix, ArrayList<String> suffixList) {
        File dir = DicomClient.getInstance().getDestinationDirectory();
        for (int s = 0; s < suffixList.size(); s++) {
            File file = new File(dir, prefix + suffixList.get(s));
            if (file.exists()) return false;
        }
        return true;
    }

    /**
     * Get an available file prefix to be written to. No file should exist with
     * this prefix and any of the suffixes provided. The file prefix will also
     * represent the content of the attribute list. The point of this is to be
     * able to create a set of files with the given prefix and suffixes without
     * overwriting any existing files.
     * 
     * @param attributeList
     *            Content that will be written.
     * 
     * @param suffixList
     *            List of suffixes needed. Suffixes are expected be provided
     *            with a leading '.' if desired by the caller.
     * @return A file prefix that, when appended with each of the prefixes, does
     *         not exist in the user specified directory.
     */
    public String getAvailableFilePrefix(AttributeList attributeList, ArrayList<String> suffixList) throws SecurityException {

        String patientIdText = Util.getAttributeValue(attributeList, TagFromName.PatientID);
        String modalityText = Util.getAttributeValue(attributeList, TagFromName.Modality);
        String seriesNumberText = Util.getAttributeValue(attributeList, TagFromName.SeriesNumber);
        String instanceNumberText = Util.getAttributeValue(attributeList, TagFromName.InstanceNumber);

        while ((instanceNumberText != null) && (instanceNumberText.length() < 4)) {
            instanceNumberText = "0" + instanceNumberText;
        }

        String name = "";
        name += (patientIdText == null) ? "" : patientIdText;
        name += (modalityText == null) ? "" : ("_" + modalityText);
        name += (seriesNumberText == null) ? "" : ("_" + seriesNumberText);
        name += (instanceNumberText == null) ? "" : ("_" + instanceNumberText);

        name = Util.replaceInvalidFileNameCharacters(name.replace(' ', '_'), '_');

        // try the prefix without an extra number to make it unique
        if (testPrefix(name, suffixList)) return name;

        // keep trying different unique numbers until one is found that is not
        // taken
        int count = 1;
        while (true) {
            String uniquifiedName = name + "_" + count;
            if (testPrefix(uniquifiedName, suffixList)) return uniquifiedName;
            count++;
        }

    }

    private String getSeriesUid(File file) {
        try {
            String seriesUid = Util.readDicomFile(file).get(TagFromName.SeriesInstanceUID).getSingleStringValueOrNull();
            return seriesUid;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Save a single file.
     * 
     * @param file The file to be saved.
     * 
     * @return Null on success, error message on failure.
     */
    private String saveOneFile(File file) {
        File destFile;
        AttributeList attributeList = new AttributeList();
        try {
            attributeList = Util.readDicomFile(file);
        }
        catch (DicomException e) {
            return "DICOM error while reading file " + file + " : " + e.toString();
        }
        catch (IOException e) {
            return "Unable to read file " + file + " : " + e.toString();
        }
        performEdits(attributeList);

        if (overwrite.isSelected()) {
            destFile = file;
        }
        else {
            if (savedFileList.containsKey(file)) {
                destFile = savedFileList.get(file).file;
            }
            else {
                if (System.out != null) {
                    File seriesDir = null;
                    if (!savedFileList.isEmpty()) {
                        String seriesUid = getSeriesUid(file);
                        if (seriesUid != null) {
                            for (SavedFile sf : savedFileList.values()) {
                                if ((sf.series != null) && (sf.series.equals(seriesUid))) {
                                    seriesDir = sf.file.getParentFile();
                                    break;
                                }
                            }
                        }
                    }
                    destFile = DicomClient.getAvailableFile(attributeList, Util.DICOM_SUFFIX, seriesDir, file);
                    savedFileList.put(file, new SavedFile(destFile, getSeriesUid(file)));
                }
                else { // TODO rm
                    String prefix = getAvailableFilePrefix(attributeList, getFileSuffixList());
                    destFile = new File(DicomClient.getInstance().getDestinationDirectory(), prefix + Util.DICOM_SUFFIX);
                    savedFileList.put(file, new SavedFile(destFile, getSeriesUid(file)));
                }
            }
        }
        String prefix = destFile.getName().replaceAll("\\.[^\\.]*$", "");

        destFile.getParentFile().mkdirs();
        
        if (saveAsDicom.isSelected()) {
            try {
                destFile.delete();
                
                FileMetaInformation.addFileMetaInformation(attributeList, Util.DEFAULT_TRANSFER_SYNTAX, ClientConfig.getInstance().getApplicationName());
                attributeList.write(destFile, Util.DEFAULT_TRANSFER_SYNTAX, true, true);
            }
            catch (IOException e) {
                return "Unable to write file " + destFile + " : " + e.toString();
            }
            catch (DicomException e) {
                return "DICOM error while writing file " + destFile + " : " + e.toString();
            }
        }
        
        if (saveAsText.isSelected()) {
            try {
                File textFile = new File(destFile.getParentFile(), prefix + Util.TEXT_SUFFIX);
                Util.writeTextFile(attributeList, textFile);
            }
            catch (Exception e) {
                return "Unable to save text version of " + destFile.getAbsolutePath() + " : " + e.toString();
            }
        }
        
        if (saveAsPng.isSelected()) {
            try {
                File pngFile = new File(destFile.getParentFile(), prefix + Util.PNG_SUFFIX);
                Util.writePngFile(attributeList, pngFile);
            }
            catch (Exception e) {
                return "Unable to save PNG image version of " + destFile.getAbsolutePath() + " : " + e.toString();
            }
        }
        
        if (saveAsXml.isSelected()) {
            try {
                File xmlFile = new File(destFile.getParentFile(), prefix + Util.XML_SUFFIX);
                Util.writeXmlFile(attributeList, xmlFile);
            }
            catch (Exception e) {
                return "Unable to save text version of " + destFile.getAbsolutePath() + " : " + e.toString();
            }
        }
        return null;   // success
    }
        
    
    /**
     * Save the current edits.  The edit history will remain unchanged whether or not
     * the save was successful.  A save can fail because of an I/O error such as not
     * having permission to write the file or disk full.
     * <p>
     * A save is performed whether or not there were edits made.  This is useful in
     * the case where the user wants to save the file in a different format without
     * making changes.
     * <p>
     * It is possible for a save to be partially successful, in the case where a
     * series with multiple slices is being saved and only some of them are saved before
     * one fails.
     * 
     * @return True if successful.
     */
    private boolean save() {
        Series series = preview.getPreviewedSeries();
        if (series != null) {
            if (saveToAllSlices.isSelected()) {
                for (File file : series.getFileList()) {
                    String msg = saveOneFile(file);
                    if (msg != null) {
                        Log.get().severe(msg);
                        new Alert(msg, "Error Saving Edited File");
                        return false;
                    }
                }
            }
            else {
                File file = (File)(series.getFileList().toArray()[preview.getCurrentSlice()-1]);
                saveOneFile(file);
            }
            modified = false;
            return true;
        }
        return false;
    }
    
    
    /**
     * Determine if the user has unsaved edits.
     * 
     * @return True if the user has unsaved edits.
     */
    public boolean isModified() {
        return modified;
    }
    
    private void copy() {
        if (attributeLocation != null) addNewEdit(new EditCopy(attributeLocation));
    }
    
    /**
     * Push a new edit action onto the <code>editHistory</code> stack.
     * 
     * @param edit
     */
    public void addNewEdit(Edit edit) {
        modified = true;
        editHistory.add(edit);
        redoHistory.clear();
        setCard(CARD_MAIN);
        setAttributeLocation(attributeLocation);
        resetDoButtons();
        preview.showDicom();
        setAttributeLocation(null);
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
    
    public static boolean isSingleMultiplicity(AttributeTag tag) {
        CustomDictionary cd = CustomDictionary.getInstance();
        return (cd.getValueMultiplicity(tag) == Multiplicity.M1) && (!ValueRepresentation.isSequenceVR(cd.getValueRepresentationFromTag(tag))); 
    }

    public static boolean isMultipleMultiplicity(AttributeTag tag) {
        return (!isSingleMultiplicity(tag)) && (!ValueRepresentation.isSequenceVR(CustomDictionary.getInstance().getValueRepresentationFromTag(tag)));
    }

    public static boolean isNoMultiplicity(AttributeTag tag) {
        return ValueRepresentation.isSequenceVR(CustomDictionary.getInstance().getValueRepresentationFromTag(tag));
    }

    public void setAttributeLocation(AttributeLocation attrLoc) {
        
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
        
        this.attributeLocation = attrLoc;

        deleteButton.setEnabled(attributeLocation != null);
        updateButton.setEnabled((attributeLocation != null) && (attributeLocation.attribute != null) && (!(attributeLocation.attribute instanceof SequenceAttribute)));
        copyButton.setEnabled((attributeLocation != null) && (attributeLocation.attribute == null));

        deleteButton.setToolTipText("<html>Select an entry in<br>the preview window</html>");
        updateButton.setToolTipText("<html>Select an entry in the<br>preview window that is<br>not a numbered <em>Item</em> or<br>Sequence attribute</html>");
        copyButton.setToolTipText("<html>Select a numbered <em>Item</em> under a<br>Sequence attribute to copy</html>");
        createButton.setToolTipText("<html>Create a new attribute</html>");

        if (attributeLocation != null) {
            String desc;
            if (attributeLocation.attribute == null) {
                int item = attributeLocation.getParentIndex() + 1;
                desc = "Item " + item + " of " + CustomDictionary.getInstance().getNameFromTag(attributeLocation.getParentTag());
            }
            else {
                desc = CustomDictionary.getName(attributeLocation);
            }

            deleteButton.setToolTipText("Delete " + desc);

            if ((attributeLocation.attribute != null) && (!ValueRepresentation.isSequenceVR(attributeLocation.attribute.getVR()))) updateButton.setToolTipText("Update " + desc);

            if (attributeLocation.attribute == null) copyButton.setToolTipText("Copy " + desc);
            
            if (currentCard.equals(CARD_UPDATE)) {
                if (UpdateGui.isUpdateable(attributeLocation))
                    updateGui.setAttributeLocation(attributeLocation);
                else
                    setCard(CARD_MAIN);
            }
            if (currentCard.equals(CARD_CREATE)) createGui.setAttributeLocation(attributeLocation);
        }
    }

    public void windowOpened(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {
        if (!isModified()) {
            setVisible(false);
            reset();
            return;
        }

        if (letUserSaveIfTheyWantTo()) {
            reset();
            setVisible(false);
        }
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

}
