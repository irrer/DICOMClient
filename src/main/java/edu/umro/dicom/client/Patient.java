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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.TagFromName;

import edu.umro.dicom.client.DicomClient.ProcessingMode;
import edu.umro.util.Log;

/**
 * Represent the data and GUI associated with a patient.
 * Patients are uniquely identified by their patient ID tag.
 * 
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
public class Patient extends JPanel implements Comparable<Patient>, DocumentListener, ActionListener, Runnable {

    /** Default ID. */
    private static final long serialVersionUID = 1L;

    /** Maximum allowed length of patient name */
    private static final int PATIENT_NAME_MAX_LEN = 64;

    /** Maximum allowed length of patient ID */
    private static final int PATIENT_ID_MAX_LEN = 64;

    /** Patient ID of this patient. */
    private String patientId = null;

    /** Name of this patient. */
    private String patientName = null;

    /** Birth date of this patient. */
    private String patientBirthDate = null;

    /** Summary information that helps identify a patient to the user. */
    private String patientSummary = null;

    /** Main border around patient object. */
    private Border border = BorderFactory.createLineBorder(new Color(180, 180, 180), 4);

    /** Panel containing anonymizing fields. */
    private JPanel anonymizePanel = null;

    /** Button that clears this patient from the list. */
    private JButton clearButton = null;

    /** Contains new patient ID for anonymizing. */
    private JTextField anonymizePatientIdTextField = null;

    /** Contains new patient ID for anonymizing. */
    private JTextField anonymizePatientNameTextField = null;

    /** Shows what the anonymizePatientNameTextField is for. */
    private JLabel anonymizePatientNameLabel = null;

    /** If selected, then patient name can be different from patient id. */
    private JCheckBox enableDifferentPatientName = null;

    /** Button that process all series for this patient. */
    private JButton processPatientButton = null; 

    private JComponent buildAnonymizingPatientId(String anonymousPatientId) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        anonymizePanel = new JPanel();

        anonymizePanel.add(new JLabel("New Patient ID: "));
        anonymizePatientIdTextField = new JTextField(16);
        anonymizePatientIdTextField.setText(anonymousPatientId);
        PlainDocument plainIdDocument = (PlainDocument)anonymizePatientIdTextField.getDocument();
        plainIdDocument.setDocumentFilter(new LimitedDocumentFilter(PATIENT_ID_MAX_LEN));
        anonymizePatientIdTextField.setToolTipText("<html>The ID to be used as<br>the anonymized patient ID</html>");
        anonymizePatientIdTextField.getDocument().addDocumentListener(this);
        anonymizePanel.add(anonymizePatientIdTextField);

        anonymizePanel.add(new JLabel("     "));

        anonymizePatientNameLabel = new JLabel("New Patient Name: ");
        anonymizePanel.add(anonymizePatientNameLabel);
        anonymizePatientNameTextField = new JTextField(16);
        anonymizePatientNameTextField.setText(anonymousPatientId);
        PlainDocument plainNameDocument = (PlainDocument)anonymizePatientNameTextField.getDocument();
        plainNameDocument.setDocumentFilter(new LimitedDocumentFilter(PATIENT_NAME_MAX_LEN));
        anonymizePatientNameTextField.setToolTipText("<html>The name to be used as<br>the anonymized patient name</html>");
        anonymizePatientNameTextField.getDocument().addDocumentListener(this);
        anonymizePanel.add(anonymizePatientNameTextField);

        enableDifferentPatientName = new JCheckBox();
        enableDifferentPatientName.setToolTipText("<html>Allow patient name to be<br>different from patient ID</html>");
        enableDifferentPatientName.addActionListener(this);
        anonymizePanel.add(enableDifferentPatientName);

        setDiffPatName();

        anonymizePanel.add(new JLabel("        "));
        processPatientButton = new JButton("Anonymize Patient");
        processPatientButton.setToolTipText("<html>Anonymize all series<br>for this patient</html>");
        processPatientButton.addActionListener(this);
        
        JPanel betweenPanel = new JPanel();
        betweenPanel.add(anonymizePanel);
        betweenPanel.add(processPatientButton);
        

        mainPanel.add(new JLabel(""), BorderLayout.CENTER);
        mainPanel.add(betweenPanel, BorderLayout.EAST);
        return mainPanel;
    }

    private JComponent buildPatientButtonPanel(String anonymousPatientId) {

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        clearButton = new JButton("Clear");
        clearButton.addActionListener(this);
        clearButton.setToolTipText("<html>Clear this patient<br>from display</html>");

        JPanel clearPanel = new JPanel();
        clearPanel.add(clearButton);

        panel.add(clearPanel, BorderLayout.WEST);
        panel.add(buildAnonymizingPatientId(anonymousPatientId));

        return panel;
    }

    /**
     * Construct a new patient with the given file.  If the file identifies a
     * study that is not already listed, then add it to the list.  If the study
     * is already listed, then pass it to the study object so that it can process
     * it on a series level.
     * 
     * @param fileName Name of DICOM file.
     * 
     * @param attributeList Representation of DICOM file.  The attribute list
     * could be derived from the file name, but it is more efficient to only
     * read and parse the file once.
     */
    public Patient(File file, AttributeList attributeList, String anonymousPatientId) {
        patientId        = Util.getAttributeValue(attributeList, TagFromName.PatientID);
        patientId        = (patientId == null) ? "No patient id" : patientId;
        patientName      = Util.getAttributeValue(attributeList, TagFromName.PatientName); 
        patientName      = (patientName == null) ? "No patient name" : patientName;
        patientBirthDate = Util.getAttributeValue(attributeList, TagFromName.PatientBirthDate); 

        patientSummary = "";

        if (patientId != null) {
            patientSummary += "ID: " + patientId;
        }

        if ((patientName != null) && (!patientName.equalsIgnoreCase(patientId))) {
            patientSummary += "    " + patientName;
        }

        if (patientBirthDate != null) {
            patientSummary += "     Birth: " + patientBirthDate; 
        }

        patientSummary = " " + patientSummary + " ";
        Log.get().info("Added patient");

        Border titledBorder = BorderFactory.createTitledBorder(border, patientSummary, TitledBorder.LEFT,
                TitledBorder.TOP , DicomClient.FONT_MEDIUM, DicomClient.COLOR_FONT);
        int gap = 30;
        Border emptyBorder = BorderFactory.createEmptyBorder(gap, gap, gap, gap);
        Border compositeBorder = BorderFactory.createCompoundBorder(emptyBorder, titledBorder);
        gap = 8;
        Border emptyBorder2 = BorderFactory.createEmptyBorder(gap, gap, gap, gap);
        Border compositeBorder2 = BorderFactory.createCompoundBorder(compositeBorder, emptyBorder2);
        setBorder(compositeBorder2);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(buildPatientButtonPanel(anonymousPatientId));

        Study study = new Study(file, attributeList);
        add(study);
    }
    
    
    /**
     * Set the mode (anonymize or upload) for this patient.
     */
    public void setMode(ProcessingMode mode) {
        anonymizePanel.setVisible(mode != ProcessingMode.UPLOAD);
        boolean uploadEnabled = DicomClient.getInstance().uploadEnabled();
        boolean enabled = false;
        String text = "?? Patient";
        switch (mode) {
        case ANONYMIZE:
            text = "Anonymize Patient";
            enabled = true;
            break;
        case ANONYMIZE_THEN_LOAD:
            text = "Anonymize then Load Patient";
            enabled = true;
            break;
        case UPLOAD:
            text = "Upload Patient";
            enabled = uploadEnabled;
            break;
        case ANONYMIZE_THEN_UPLOAD:
            text = "Anonymize then Upload Patient";
            enabled = uploadEnabled;
            break;
        }
        processPatientButton.setEnabled(enabled);
        processPatientButton.setText(text);
    }


    /**
     * Add a new study to the list of studies.
     * 
     * @param fileName DICOM file name.
     * 
     * @param attributeList Representation of DICOM file.
     */
    public void addStudy(File file, AttributeList attributeList) {
        String studyInstanceUid = Util.getAttributeValue(attributeList, TagFromName.StudyInstanceUID);
        studyInstanceUid = (studyInstanceUid == null) ? "" : studyInstanceUid;
        for (Component component : getComponents()) {
            if (component instanceof Study) {
                Study study = (Study)component;
                if (study.getStudyInstanceUID().equals(studyInstanceUid)) {
                    study.addInstance(file, attributeList);
                    return;
                }
            }
        }
        add(new Study(file, attributeList));
    }


    /**
     * Determine if two patients are the same or not based on patient ID.  Note that if the
     * IDs are slightly different (for example, one has an extra leading zero) then they will
     * be considered different.
     */
    public boolean equals(Object other) {
        return (other != null) && (other instanceof Patient) && patientId.equals(((Patient)other).patientId);
    }
    
    /**
     * Get the patient ID.
     * @return
     */
    public String getPatientId() {
        return patientId;
    }

    public int compareTo(Patient other) {
        return patientId.compareTo(other.patientId);
    }


    @Override
    public String toString() {
        return patientSummary;
    }


    /**
     * Upload all of the series in all of the studies in this patient.
     */
    public void uploadAll() {
        for (Component component : getComponents()) {
            if (component instanceof Study) {
                Study study = (Study)component;
                study.processAll();
            }
        }
    }


    /**
     * Reset all of the upload progress bars.
     */
    public void zeroAllProgressBars() {
        for (Component component : getComponents()) {
            if (component instanceof Study) {
                Study study = (Study)component;
                study.zeroAllProgressBars();
            }
        }
    }


    /**
     * Set the mode to anonymizing (true) or uploading (false).
     * 
     * @param anonymizing 
     * @return
     */
    public void setAnonymizingMode(boolean anonymizing) {
        anonymizePanel.setVisible(anonymizing);
    }


    public String getAnonymizePatientIdText() {
        return anonymizePatientIdTextField.getText();
    }


    public String getAnonymizePatientNameText() {
        return anonymizePatientNameTextField.getText();
    }


    public void setAnonymizePatientIdText(String id) {
        anonymizePatientIdTextField.setText(id);
    }


    public void setAnonymizePatienteNameText(String name) {
        anonymizePatientNameTextField.setText(name);
    }


    private void updateAnonymizePatientFields() {
        SwingUtilities.invokeLater(this);
    }


    /**
     * Set one of the special fields (anonymizing information is on
     * patient panel, patient id and name) values in the anonymizing
     * GUI so that they stay synchronized.
     * 
     * @param local
     * @param tag
     */
    /*
    private void setSpecialField(JTextField local, AttributeTag tag) {
        AnonymizeAttribute aa = AnonymizeGUI.getInstance().getAnonymizeAttribute(tag);
        if ((aa != null) && (!aa.getValue().equalsIgnoreCase(local.getText()))) {
            aa.setValue(local.getText());
        }
    }
    */


    public void run() {
        if ((!enableDifferentPatientName.isSelected()) && (!getAnonymizePatientIdText().equals(getAnonymizePatientNameText()))) {
            setAnonymizePatienteNameText(getAnonymizePatientIdText());
        }

        //setSpecialField(anonymizePatientIdTextField, TagFromName.PatientID);
        //setSpecialField(anonymizePatientNameTextField, TagFromName.PatientName);
    }


    public void insertUpdate(DocumentEvent e) {
        updateAnonymizePatientFields();
    }


    public void removeUpdate(DocumentEvent e) {
        updateAnonymizePatientFields();
    }


    public void changedUpdate(DocumentEvent e) {
        updateAnonymizePatientFields();
    }


    /**
     * Set the state corresponding to whether the user wants the
     * patient name to be different than the patient ID.
     */
    private void setDiffPatName() {
        anonymizePatientNameTextField.setEnabled(enableDifferentPatientName.isSelected());
        anonymizePatientNameLabel.    setEnabled(enableDifferentPatientName.isSelected());
    }


    private ArrayList<Series> getSeriesList(Container container, ArrayList<Series> seriesList) {
        if (container == null) container = this;
        if (seriesList == null) seriesList = new ArrayList<Series>();
        for (Component component : container.getComponents()) {
            if (component instanceof Series) {
                seriesList.add((Series)component);
            }
            if (component instanceof Container) {
                getSeriesList((Container)component, seriesList);
            }
        }
        return seriesList;
    }
    
    /**
     * Get a list of all studies.
     * 
     * @return List of studies.
     */
    public ArrayList<Study> getStudyList() {
        ArrayList<Study> list = new ArrayList<Study>();
        for (Component component : getComponents()) {
            if (component instanceof Study) {
                list.add((Study) component);
            }
        }
        return list;
    }


    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == enableDifferentPatientName) {
            setDiffPatName();
            updateAnonymizePatientFields();
        }

        if (e.getSource() == processPatientButton) {
            Log.get().info("Processing all series for patient");
            Series.processOk = true;
            for (Series series : getSeriesList(null, null)) series.processSeries();
            DicomClient.getInstance().setProcessedStatus();
        }

        if (e.getSource() == clearButton) {
            Log.get().info("Clearing all series for patient");
            DicomClient.getInstance().clearPatient(this);
        }
    }

}
