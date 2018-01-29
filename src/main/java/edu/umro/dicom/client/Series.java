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
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.xml.parsers.ParserConfigurationException;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.SOPClassDescriptions;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.dicom.TagFromName;
import edu.umro.dicom.client.DicomClient.ProcessingMode;
import edu.umro.dicom.client.test.AutoTest;
import edu.umro.util.Log;
import edu.umro.util.UMROException;

/**
 * Represent a DICOM series.
 * 
 * @author Jim Irrer irrer@umich.edu
 * 
 */

public class Series extends JPanel implements ActionListener, Runnable {

    /** Default ID. */
    private static final long serialVersionUID = 1L;

    /** Card layout identifier for preview slider mode. */
    static private final String CARD_SLIDER = "Slider";

    /** Card layout identifier for upload progress mode. */
    static private final String CARD_PROGRESS = "Progress";

    /** Tool tip for Upload All button. */
    private static final String UPLOAD_BUTTON_TOOLTIP_TEXT_ENABLED = "<html>Upload this series<br>to the selected<br>PACS server.</html>";

    /** Tool tip for Upload All button. */
    private static final String UPLOAD_BUTTON_TOOLTIP_TEXT_DISABLED = "<html>Select a PACS server<br>to enable uploading.</html>";

    /** Tool tip for Upload All button. */
    private static final String ANONYMIZE_BUTTON_TOOLTIP = "<html>Anonymize this series,<br>overwriting files.</html>";

    /** DICOM value for Patient ID for the series. */
    private String patientID = null;

    /** DICOM value for Patient name for the series. */
    private String patientName = null;

    /** Description of series including key metadata values. */
    private String seriesSummary = null;

    /** True if this series has been anonymized. */
    private boolean isAnonymized = false;

    /** True if this series has been anonymized and then uploaded. */
    private boolean isAnonymizedThenUploaded = false;

    /** DICOM value for series number for the series. */
    private String seriesNumber = null;
    /** DICOM value for modality for the series. */
    private String modality = null;
    /** DICOM value for series description for the series. */
    private String seriesDescription = null;

    private static volatile Semaphore processLock = new Semaphore(1);

    /**
     * The next two sets of DICOM values are dates and times. Frequently, a
     * DICOM file is generated with one or another date or time field filled in,
     * but others left vacant. To get some date and time to show to the user,
     * several are examined to see if they are present in the hope that at least
     * one of them is. This situation is not ideal (all DICOM files should
     * always have a minimal set of values), but vendors will do what they will.
     */

    /** DICOM value for series date for the series. */
    private String seriesDate = null;
    /** DICOM value for content date for the series. */
    private String contentDate = null;
    /** DICOM value for acquisition date for the series. */
    private String acquisitionDate = null;
    /** DICOM value for instance creation date for the series. */
    private String instanceCreationDate = null;
    /** DICOM value for RT plan date for the series. */
    private String rtPlanDate = null;
    /** DICOM value for structure set date for the series. */
    private String structureSetDate = null;

    /** DICOM value for series time for the series. */
    private String seriesTime = null;
    /** DICOM value for content time for the series. */
    private String contentTime = null;
    /** DICOM value for acquisition time for the series. */
    private String acquisitionTime = null;
    /** DICOM value for instance creation time for the series. */
    private String instanceCreationTime = null;
    /** DICOM value for RT plan time for the series. */
    private String rtPlanTime = null;
    /** DICOM value for structure set time for the series. */
    private String structureSetTime = null;

    /** DICOM value for structure set time for the series. */
    private String seriesInstanceUID = null;

    /**
     * Short human readable description of series constructed from its DICOM
     * values.
     */
    private JLabel summaryLabel = null;

    /** Button that user uses to initiate uploads and anonymization. */
    private JButton uploadAnonymizeButton = null;

    /**
     * Icon associated with upload button to indicate whether or not the series
     * has been uploaded, or anonymized, or both.
     */
    private JLabel doneIcon = null;

    /** Shows preview. */
    private JButton previewButton = null;

    /** Shows progressBar of upload. */
    private JProgressBar progressBar = null;

    /** Panel that shows the progress bar or preview slider. */
    private JPanel previewProgressPanel = null;

    /** Layout that shows the progress bar or preview slider. */
    private CardLayout previewProgressLayout = null;

    public static int totalFilesAnonymized = 0;

    private class InstanceList {
        private class Instance implements Comparable<Instance> {
            public String sopInstanceUID;
            public File file;
            AttributeList attributeList;

            private int compareString(Instance other, AttributeTag tag) {
                Attribute thisAttr = attributeList.get(tag);
                Attribute otherAttr = other.attributeList.get(tag);

                if ((thisAttr == null) && (otherAttr == null)) return 0;
                if (thisAttr == null) return -1;
                if (otherAttr == null) return 1;

                String thisVal = thisAttr.getSingleStringValueOrNull();
                String otherVal = otherAttr.getSingleStringValueOrNull();
                if ((thisVal == null) && (otherVal == null)) return 0;
                if (thisVal == null) return -1;
                if (otherVal == null) return 1;

                return thisVal.compareTo(otherVal);
            }

            private int compareDouble(Instance other, AttributeTag tag, int index) {
                Attribute thisAttr = attributeList.get(tag);
                Attribute otherAttr = other.attributeList.get(tag);

                if ((thisAttr == null) && (otherAttr == null)) return 0;
                if (thisAttr == null) return -1;
                if (otherAttr == null) return 1;

                try {
                    double[] thisVal = thisAttr.getDoubleValues();
                    double[] otherVal = otherAttr.getDoubleValues();
                    if ((thisVal == null) && (otherVal == null)) return 0;
                    if (thisVal == null) return -1;
                    if (otherVal == null) return 1;

                    if ((thisVal.length <= index) && (otherVal.length <= index)) return 0;
                    if (thisVal.length <= index) return -1;
                    if (otherVal.length <= index) return 1;

                    double tv = thisVal[index];
                    double ov = otherVal[index];
                    if (tv < ov) return -1;
                    if (tv > ov) return 1;
                    return 0;
                    // return Double.compare(tv, ov);
                }
                catch (Exception e) {
                    return 0;
                }
            }

            public Instance(String sopInstanceUID, File file, AttributeList attributeList) {
                this.sopInstanceUID = sopInstanceUID;
                this.file = file;
                this.attributeList = attributeList;
            }

            public int compareTo(Instance o) {
                int c = 0;

                c = compareDouble(o, TagFromName.SliceLocation, 0);
                if (c != 0) return c;

                c = compareDouble(o, TagFromName.ImagePositionPatient, 0);
                if (c != 0) return c;

                c = compareDouble(o, TagFromName.ImagePositionPatient, 1);
                if (c != 0) return c;

                c = compareDouble(o, TagFromName.ImagePositionPatient, 2);
                if (c != 0) return c;

                c = compareDouble(o, TagFromName.InstanceNumber, 0);
                if (c != 0) return c;

                c = compareString(o, TagFromName.InstanceCreationDate);
                if (c != 0) return c;

                c = compareString(o, TagFromName.InstanceCreationTime);
                if (c != 0) return c;

                c = compareString(o, TagFromName.InstanceCreationTime);
                if (c != 0) return c;

                c = compareString(o, TagFromName.AcquisitionDate);
                if (c != 0) return c;

                c = compareString(o, TagFromName.AcquisitionTime);
                if (c != 0) return c;

                c = compareString(o, TagFromName.ContentDate);
                if (c != 0) return c;

                c = compareString(o, TagFromName.ContentTime);
                if (c != 0) return c;

                c = compareString(o, TagFromName.RTPlanDate);
                if (c != 0) return c;

                c = compareString(o, TagFromName.RTPlanTime);
                if (c != 0) return c;

                c = compareString(o, TagFromName.StructureSetDate);
                if (c != 0) return c;

                c = compareString(o, TagFromName.StructureSetTime);
                if (c != 0) return c;

                return compareString(o, TagFromName.SOPInstanceUID);
            }
        }

        private TreeSet<Instance> instList = new TreeSet<Instance>();

        private HashSet<String> sopList = new HashSet<String>();
        private HashSet<File> fileList = new HashSet<File>();

        private Semaphore lock = new Semaphore(1);

        private void acquire() {
            try {
                if (!lock.tryAcquire(10, TimeUnit.SECONDS)) {
                    DicomClient.getInstance().showMessage("Failed to acquire internal data lock");
                }
            }
            catch (Exception e) {
            }
        }

        private void release() {
            lock.release();
        }

        /**
         * Get the sortedList of file names in ascending order by instance
         * number.
         * 
         * @return List of file names.
         */
        public ArrayList<File> values() {
            try {
                acquire();
                ArrayList<File> list = new ArrayList<File>();
                for (Instance inst : instList)
                    list.add(inst.file);
                return list;
            }
            finally {
                release();
            }
        }

        public TreeSet<Instance> getList() {
            return instList;
        }

        public int size() {

            try {
                acquire();

                return instList.size();
            }
            finally {
                release();
            }
        }

        public File getFirstFile() {
            return instList.first().file;
        }

        public File getFile(int i) {
            try {
                acquire();
                return ((Instance) (instList.toArray()[i])).file;
            }
            finally {
                release();
            }
        }

        private String put(File file, AttributeList attributeList) {
            try {
                acquire();

                if (fileList.contains(file)) {
                    return "The file " + file + " has already been loaded.";
                }

                String sopInstanceUID = attributeList.get(TagFromName.SOPInstanceUID).getSingleStringValueOrEmptyString();
                if (sopList.contains(sopInstanceUID)) {
                    File oldFile = null;
                    for (Instance instance : instList) {
                        if (instance.sopInstanceUID == sopInstanceUID) {
                            oldFile = instance.file;
                            break;
                        }
                    }
                    return "The SOP Instance UID " + sopInstanceUID + " was already loaded with from file " + oldFile + ", so ignoring file " + file.getAbsolutePath();
                }

                instList.add(new Instance(sopInstanceUID, file, attributeList));
                sopList.add(sopInstanceUID);
                fileList.add(file);

                return null;
            }
            finally {
                release();
            }
        }
    }

    /** List of file names for this series. */
    private InstanceList instanceList = new InstanceList();

    /** List of PACS AE titles to which this series has been uploaded. */
    private HashSet<String> aeTitleUploadList = new HashSet<String>();

    /**
     * Indicate whether or not the file is in this series.
     * 
     * @param file
     *            File for which to search.
     * 
     * @return True if file is in list.
     */
    public boolean containsFile(File file) {
        String path = file.getAbsolutePath();
        for (File f : instanceList.fileList)
            if (f.getAbsolutePath().equals(path)) return true;
        return false;
    }

    /**
     * Indicate whether or not the file is already in this series.
     * 
     * @param sopInstanceUID
     *            SOP instance UID to search for
     * 
     * @return True if instance is in list.
     */
    public boolean containsSOPInstanceUID(String sopInstanceUID) {
        return instanceList.sopList.contains(sopInstanceUID);
    }

    /**
     * Find the first non-null value in a sortedList.
     * 
     * @param textList
     *            List of values.
     * 
     * @return the first non-null value, or null if all are null.
     */
    private String firstNonNull(String[] textList) {
        for (String text : textList) {
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    /**
     * Reset the summary label based on the current state of the series.
     */
    private void resetSummary() {
        String date = firstNonNull(new String[] { seriesDate, contentDate, acquisitionDate, instanceCreationDate, rtPlanDate, structureSetDate });
        String time = firstNonNull(new String[] { seriesTime, contentTime, acquisitionTime, instanceCreationTime, rtPlanTime, structureSetTime });

        String seriesSummary = "         ";
        seriesSummary += (seriesNumber == null) ? "" : " " + seriesNumber;
        seriesSummary += (modality == null) ? " No modality" : " " + modality;
        seriesSummary += (seriesDescription == null) ? "" : " " + seriesDescription;
        seriesSummary += (date == null) ? "" : " " + date;
        seriesSummary += (time == null) ? "" : " " + time;

        seriesSummary += "    Files: " + instanceList.size();

        seriesSummary += "   ";
        summaryLabel.setText(seriesSummary);
    }

    private JComponent buildPreviewButton() {
        previewButton = new JButton("Preview");
        previewButton.setToolTipText("<html>Preview file</html>");
        previewButton.addActionListener(this);
        JPanel previewButtonPanel = new JPanel();
        previewButtonPanel.setLayout(new BorderLayout());
        previewButtonPanel.add(previewButton, BorderLayout.CENTER);
        previewButtonPanel.add(new JLabel(), BorderLayout.EAST);
        return previewButtonPanel;
    }

    private JComponent buildUploadAnonymizeButton() {
        uploadAnonymizeButton = new JButton("Upload");
        uploadAnonymizeButton.addActionListener(this);
        uploadAnonymizeButton.setToolTipText("<html>Upload this series to<br>the given PACS server.</html>");
        doneIcon = new JLabel(PreDefinedIcons.getEmpty());
        JPanel uploadButtonPanel = new JPanel();
        uploadButtonPanel.add(uploadAnonymizeButton);
        uploadButtonPanel.add(doneIcon);
        uploadButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        return uploadButtonPanel;
    }

    private JComponent buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.add(buildPreviewButton());
        panel.add(buildUploadAnonymizeButton());
        return panel;
    }

    private JComponent buildProgressBar() {
        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(1);
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);

        return progressPanel;
    }

    private JComponent buildEast() {
        previewProgressPanel = new JPanel();
        previewProgressLayout = new CardLayout();
        previewProgressPanel.setLayout(previewProgressLayout);
        previewProgressPanel.add(buildButtonPanel(), CARD_SLIDER);
        previewProgressPanel.add(buildProgressBar(), CARD_PROGRESS);
        previewProgressLayout.show(previewProgressPanel, CARD_SLIDER);
        return previewProgressPanel;
    }

    private JComponent buildSummaryLabel() {
        summaryLabel = new JLabel();
        summaryLabel.setFont(DicomClient.FONT_MEDIUM);
        return summaryLabel;
    }

    private void buildGui() {
        setLayout(new BorderLayout());

        add(buildSummaryLabel(), BorderLayout.CENTER);
        add(buildEast(), BorderLayout.EAST);

        int gap = 8;
        setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, 0));
    }

    /**
     * Build a GUI component that displays and controls the uploading of a
     * single series.
     * 
     * @param fileName
     *            Name of DICOM file from which the series originated.
     * 
     * @param attributeList
     *            Parsed version of DICOM file contents.
     */
    public Series(File file, AttributeList attributeList) {

        // For the first file of every series, read the entire attribute
        // list and then update the anonymizing list. This ensures that
        // the anonymizing list contains a superset of all attributes used
        // by the DICOM files that have been loaded. The only problem that
        // would arise is if a subsequent file in a series contained a
        // new type of DICOM attribute that was not in the first file of
        // the series (which is unlikely), and the user wanted to specify
        // custom anonymization for that type (again, unlikely). The user
        // could get around this by previewing slice in the series, which
        // will read each file in it's entirety and update the anonymizing
        // list. Correcting this error would make the initial loading of
        // DICOM files much slower (~ 10x), and so would annoy users more than
        // make them happy. Possibly in the future a background thread will
        // be started to read each file in its entirety while the user is
        // staring at the screen, but there's not a big benefit to it.
        try {
            AttributeList al = Util.readDicomFile(file);

            if (!DicomClient.hasValidSOPInstanceUID(al)) {
                // make a new attribute to avoid memory leaks
                Attribute at = AttributeFactory.newAttribute(TagFromName.SOPInstanceUID);
                String uid = Util.getAttributeValue(attributeList, TagFromName.SOPInstanceUID);
                at.addValue(uid);
                al.put(at);
            }

            attributeList = al;
        }
        catch (Exception e) {
            Log.get().warning("Error reading DICOM file " + file.getAbsolutePath() + " .  The file may not be processed properly. :  " + e);
        }

        AnonymizeGUI.getInstance().updateTagList(attributeList);

        patientID = Util.getAttributeValue(attributeList, TagFromName.PatientID);
        patientID = (patientID == null) ? "No patient id" : patientID;
        patientName = Util.getAttributeValue(attributeList, TagFromName.PatientName);
        patientName = (patientName == null) ? "No patient name" : patientName;

        modality = Util.getAttributeValue(attributeList, TagFromName.Modality);
        if (modality == null) {
            modality = SOPClassDescriptions.getAbbreviationFromUID(Util.getAttributeValue(attributeList, TagFromName.MediaStorageSOPClassUID));
        }
        modality = (modality == null) ? "No modality" : modality;
        seriesDescription = Util.getAttributeValue(attributeList, TagFromName.SeriesDescription);

        // try lots of ways to get the date, because some machines fill in some
        // dates and others fill in others.
        seriesDate = Util.getAttributeValue(attributeList, TagFromName.SeriesDate);
        contentDate = Util.getAttributeValue(attributeList, TagFromName.ContentDate);
        acquisitionDate = Util.getAttributeValue(attributeList, TagFromName.AcquisitionDate);
        instanceCreationDate = Util.getAttributeValue(attributeList, TagFromName.InstanceCreationDate);
        rtPlanDate = Util.getAttributeValue(attributeList, TagFromName.RTPlanDate);
        structureSetDate = Util.getAttributeValue(attributeList, TagFromName.StructureSetDate);

        // try lots of times also.
        seriesTime = Util.getAttributeValue(attributeList, TagFromName.SeriesTime);
        contentTime = Util.getAttributeValue(attributeList, TagFromName.ContentTime);
        acquisitionTime = Util.getAttributeValue(attributeList, TagFromName.AcquisitionTime);
        instanceCreationTime = Util.getAttributeValue(attributeList, TagFromName.InstanceCreationTime);
        rtPlanTime = Util.getAttributeValue(attributeList, TagFromName.RTPlanTime);
        structureSetTime = Util.getAttributeValue(attributeList, TagFromName.StructureSetTime);

        instanceCreationDate = Util.getAttributeValue(attributeList, TagFromName.InstanceCreationDate);
        instanceCreationTime = Util.getAttributeValue(attributeList, TagFromName.InstanceCreationTime);
        seriesInstanceUID = Util.getAttributeValue(attributeList, TagFromName.SeriesInstanceUID);
        seriesInstanceUID = (seriesInstanceUID == null) ? "" : seriesInstanceUID;

        buildGui();
        resetSummary();
        addFile(file, attributeList);
        seriesSummary = getSeriesSummary();
        Log.get().info("Added series " + seriesSummary);
    }

    /**
     * Get the currently selected (by the user) AE title.
     * 
     * @return The currently selected (by the user) AE title
     */
    private String getSelectedAeTitle() {
        return DicomClient.getInstance().getSelectedAeTitle();
    }

    /**
     * Set the progress bar to zero.
     */
    public void zeroProgressBar() {
        setProgress(0);
    }

    /**
     * Set the progress bar to the given value.
     * 
     * @param value
     *            Value to set the progress bar to.
     */
    private void setProgress(int value) {
        previewProgressLayout.show(previewProgressPanel, CARD_PROGRESS);
        progressBar.setValue(value);
        Graphics graphics = previewProgressPanel.getGraphics();
        previewProgressPanel.paintAll(graphics);
    }

    private boolean isBeingPreviewed() {
        Preview preview = DicomClient.getInstance().getPreview();
        return (preview.getPreviewedSeries() == this) && (preview.isVisible());
    }

    /**
     * Set the icon to reflect whether this series has been anonymized or
     * uploaded to the currently selected PACS.
     * 
     * @return True if it has been uploaded, false if not.
     */
    public boolean setProcessedStatus(ProcessingMode mode) {
        Color foregroundColor = isBeingPreviewed() ? Color.GREEN : DicomClient.COLOR_FONT;
        previewButton.setForeground(foregroundColor);

        switch (mode) {
        case ANONYMIZE:
        case ANONYMIZE_THEN_LOAD: {
            doneIcon.setIcon(isAnonymized ? PreDefinedIcons.getOk() : PreDefinedIcons.getEmpty());
            uploadAnonymizeButton.setToolTipText(ANONYMIZE_BUTTON_TOOLTIP);
            uploadAnonymizeButton.setText("Anonymize");
            uploadAnonymizeButton.setEnabled(true);
            return isAnonymized;
        }

        case UPLOAD: {
            boolean isUploaded = aeTitleUploadList.contains(DicomClient.getInstance().getSelectedAeTitle());
            Icon icon = isUploaded ? PreDefinedIcons.getOk() : PreDefinedIcons.getEmpty();
            doneIcon.setIcon(icon);
            boolean enabled = DicomClient.getInstance().uploadEnabled();
            uploadAnonymizeButton.setEnabled(enabled);
            uploadAnonymizeButton.setToolTipText(enabled ? UPLOAD_BUTTON_TOOLTIP_TEXT_ENABLED : UPLOAD_BUTTON_TOOLTIP_TEXT_DISABLED);
            uploadAnonymizeButton.setText("Upload");
            return isUploaded;
        }

        case ANONYMIZE_THEN_UPLOAD: {
            Icon icon = isAnonymizedThenUploaded ? PreDefinedIcons.getOk() : PreDefinedIcons.getEmpty();
            doneIcon.setIcon(icon);
            boolean enabled = DicomClient.getInstance().uploadEnabled();
            uploadAnonymizeButton.setEnabled(enabled);
            uploadAnonymizeButton.setToolTipText(enabled ? UPLOAD_BUTTON_TOOLTIP_TEXT_ENABLED : UPLOAD_BUTTON_TOOLTIP_TEXT_DISABLED);
            uploadAnonymizeButton.setText("Anonymize then Upload");
            return isAnonymizedThenUploaded;
        }
        }
        return false;
    }

    /**
     * Upload the given list of attribute lists to the current PACS.
     * 
     * @param description
     *            Description of this series.
     * 
     * @param attrListList
     *            Content to upload.
     * 
     * @return True on success.
     */
    private void pushDicom(SetOfDicomFiles setOfDicomFiles) {
        try {
            processLock.acquireUninterruptibly();
            PACS pacs = DicomClient.getInstance().getCurrentPacs();
            DicomPush dicomPush = new DicomPush(pacs, setOfDicomFiles, null);
            String pushError = dicomPush.push();
            if (pushError != null) {
                processOk = false;
                String message = "Problem uploading files for " + seriesSummary + " : " + pushError;
                Log.get().warning(message);
                DicomClient.getInstance().showMessage(message);
                new Alert(message, "Upload Error");
            }
            return;
        }
        catch (Exception e) {
            String message = "Problem uploading series " + seriesSummary + " . Remaining uploads for this series are being aborted.  Exception: " + e;
            DicomClient.getInstance().showMessage(message);
        }
        finally {
            processLock.release();
        }
        processOk = false;
    }

    public void run() {
        processOk = true;
        switch (DicomClient.getInstance().getProcessingMode()) {
        case ANONYMIZE:
            anonymizeSeries();
            break;
        case ANONYMIZE_THEN_LOAD:
            anonymizeSeries();
            break;
        case UPLOAD:
            uploadSeries();
            break;
        case ANONYMIZE_THEN_UPLOAD:
            Log.get().info("Anonymizing and uploading series " + summaryLabel.getText() + " : " + seriesInstanceUID);
            ArrayList<File> filesCreated = anonymizeSeries();
            uploadFiles(filesCreated);
            isAnonymizedThenUploaded = true;
            break;
        }
        DicomClient.getInstance().markScreenAsModified();
        DicomClient.getInstance().setProcessedStatus();
    }

    /**
     * Perform either anonymization or upload on this series depending on the
     * mode.
     */
    public void processSeries() {
        if (!processOk) return;
        if (DicomClient.inCommandLineMode()) {
            run();
        }
        else {
            Thread thread = new Thread(this);
            thread.start();
        }
        DicomClient.getInstance().markScreenAsModified();
    }

    /**
     * Get the patient object containing this series.
     * 
     * @return The patient object containing this series.
     */
    private Patient getPatient() {
        Component pat = this;
        while (!(pat instanceof Patient)) {
            pat = pat.getParent();
        }
        return (Patient) pat;
    }

    /**
     * Get the sortedList of anonymization values for this series.
     * 
     * @return The sortedList of anonymization values for this series.
     * 
     * @throws DicomException
     *             On invalid PatientID or PatientName
     */
    public synchronized AttributeList getAnonymizingReplacementList() throws DicomException {
        AttributeList replacementAttributeList = AnonymizeGUI.getInstance().getAttributeList();

        Attribute patientId = AttributeFactory.newAttribute(TagFromName.PatientID);
        patientId.addValue(getPatient().getAnonymizePatientIdText());
        replacementAttributeList.put(patientId);

        Attribute patientName = AttributeFactory.newAttribute(TagFromName.PatientName);
        patientName.addValue(getPatient().getAnonymizePatientNameText());
        replacementAttributeList.put(patientName);

        return replacementAttributeList;
    }

    /**
     * If in command line mode, save the anonymized DICOM as text and, if
     * possible, as an image.
     * 
     * @param attributeList
     *            Anonymized DICOM.
     * 
     * @param file
     *            File where anonymized DICOM is to be written.
     */
    private void saveTextAndXmlAndImageFiles(AttributeList attributeList, File file) {
        String fileName = file.getName();
        String textFileName = null;
        String imageFileName = null;
        String xmlFileName = null;
        File dir = (file.getParentFile() == null) ? new File(".") : file.getParentFile();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            textFileName = fileName + Util.TEXT_SUFFIX;
            imageFileName = fileName + Util.PNG_SUFFIX;
            xmlFileName = fileName + Util.XML_SUFFIX;
        }
        else {
            String baseName = fileName.substring(0, dotIndex);
            textFileName = baseName + Util.TEXT_SUFFIX;
            imageFileName = baseName + Util.PNG_SUFFIX;
            xmlFileName = baseName + Util.XML_SUFFIX;
        }

        File textFile = new File(dir, textFileName);
        Log.get().info("Writing text file: " + textFile.getAbsolutePath());
        try {
            Util.writeTextFile(attributeList, textFile);
        }
        catch (UMROException e) {
            System.err.println("Unable to write anonymized text file: " + Log.fmtEx(e));
        }
        catch (IOException e) {
            System.err.println("Unable to create anonymized text file " + textFile.getAbsolutePath() + " : " + Log.fmtEx(e));
        }

        File imageFile = new File(dir, imageFileName);
        try {
            Log.get().info("Writing PNG file: " + imageFile.getAbsolutePath());
            Util.writePngFile(attributeList, imageFile);
        }
        catch (Exception e) {
            Log.get().warning("Unable to write image file as part of anonymization for file " + imageFile.getAbsolutePath() + " : " + Log.fmtEx(e));
        }

        File xmlFile = new File(dir, xmlFileName);
        try {
            Log.get().info("Writing XML file: " + xmlFile.getAbsolutePath());
            Util.writeXmlFile(attributeList, xmlFile);
        }
        catch (ParserConfigurationException e) {
            System.err.println("Unable to parse anonymized DICOM file " + xmlFile.getAbsolutePath() + " as XML: " + Log.fmtEx(e));
        }
        catch (Exception e) {
            System.err.println("Unable to write anonymized " + xmlFile.getAbsolutePath() + " XML file: " + Log.fmtEx(e));
        }
    }

    /**
     * AnonymizeGUI this series and write the results to new files.
     */
    private synchronized ArrayList<File> anonymizeSeries() {
        ArrayList<File> filesCreated = new ArrayList<File>();
        try {
            processLock.acquireUninterruptibly();
            if (!processOk) return filesCreated;
            File newFile = null;
            int count = 0;
            int tries = 0;
            try {
                previewProgressLayout.show(previewProgressPanel, CARD_PROGRESS);
                zeroProgressBar();
                File seriesOutDir = null;
                for (InstanceList.Instance instance : instanceList.getList()) {
                    tries++;
                    AttributeList attributeList = Util.readDicomFile(instance.file);
                    if (!DicomClient.hasValidSOPInstanceUID(attributeList)) {
                        attributeList.put(instance.attributeList.get(TagFromName.SOPInstanceUID));
                    }

                    // ensure that all types of attributes that will be
                    // encountered have already been initialized for
                    // anonymization
                    AnonymizeGUI.getInstance().updateTagList(attributeList);

                    // do the actual anonymization
                    Anonymize.anonymize(attributeList, getAnonymizingReplacementList());

                    // Indicate that the file was touched by this application. Also a subtle way to advertise. :)
                    FileMetaInformation.addFileMetaInformation(attributeList, Util.DEFAULT_TRANSFER_SYNTAX, ClientConfig.getInstance().getApplicationName());

                    if (DicomClient.inCommandLineMode()) {
                        if (DicomClient.getInstance().getCommandParameterOutputFile() != null) {
                            newFile = DicomClient.getInstance().getCommandParameterOutputFile();
                        }
                        else {
                            newFile = DicomClient.getAvailableFile(attributeList, Util.DICOM_SUFFIX, seriesOutDir, instance.file);
                            seriesOutDir = newFile.getParentFile();
                        }
                        newFile.getParentFile().mkdirs();
                        attributeList.write(newFile, Util.DEFAULT_TRANSFER_SYNTAX, true, true);
                        saveTextAndXmlAndImageFiles(attributeList, newFile);
                    }
                    else {
                        ArrayList<String> suffixList = new ArrayList<String>();
                        suffixList.add(Util.DICOM_SUFFIX);
                        newFile = DicomClient.getAvailableFile(attributeList, Util.DICOM_SUFFIX, seriesOutDir, instance.file);
                        seriesOutDir = newFile.getParentFile();
                        newFile.getParentFile().mkdirs();
                        attributeList.write(newFile, Util.DEFAULT_TRANSFER_SYNTAX, true, true);
                        // load the anonymized file automatically
                        if (DicomClient.getInstance().getProcessingMode() == ProcessingMode.ANONYMIZE_THEN_LOAD) {
                            DicomClient.getInstance().addDicomFile(newFile, false);
                        }
                        filesCreated.add(newFile);
                    }

                    totalFilesAnonymized++;
                    DicomClient.getInstance().indicateThatStatisticsHaveChanged();

                    count++;
                    setProgress(count);
                    Log.get().info("Anonymized to file: " + newFile.getAbsolutePath());
                    DicomClient.getInstance().markScreenAsModified();
                }
            }
            catch (DicomException e) {
                String msg = "DICOM error - unable to anonymize series : " + e;
                DicomClient.getInstance().showMessage(msg);
                Log.get().severe(msg);
                processOk = false;
                if (DicomClient.inCommandLineMode()) {
                    System.err.println(msg);
                    Util.exitFail();
                }
            }
            catch (IOException e) {
                processOk = false;
                String msg = "File error - unable to anonymize series : " + e;
                DicomClient.getInstance().showMessage(msg);
                Log.get().severe(msg);
                // If there was an IO exception, then
                if (tries == 1) { // only tell user once per series
                    String ioMsg = "<html>Unable to write file\n\n    " + newFile.getAbsolutePath()
                            + "\n<p><br><p>It is possible that you do not have permission to write to this directory." + "\n<p><br><p>Technical details:<br>" + e + "\n</html>";
                    new Alert(ioMsg, "Unable to write file");
                }
                if (DicomClient.inCommandLineMode()) {
                    System.err.println(msg);
                    Util.exitFail();
                }
            }
            finally {
                previewProgressLayout.show(previewProgressPanel, CARD_SLIDER);
            }
            if (count == instanceList.size()) {
                isAnonymized = true;
                setProcessedStatus(DicomClient.getInstance().getProcessingMode());
            }
            else {
                String msg = "Unable to anonymize series " + toString() + " .  Only " + count + " slices of " + instanceList.size() + " were completed.";
                DicomClient.getInstance().showMessage(msg);
            }
        }
        finally {
            processLock.release();
        }
        return filesCreated;
    }

    private String getSeriesSummary() {
        seriesSummary = "";
        seriesSummary += (patientID == null) ? "" : " " + patientID;
        seriesSummary += (patientName == null) ? "" : " " + patientName;
        seriesSummary += (seriesNumber == null) ? "" : " " + seriesNumber;
        seriesSummary += (modality == null) ? "" : " " + modality;
        seriesSummary += (seriesDescription == null) ? "" : " " + seriesDescription;
        return seriesSummary;
    }

    private void uploadFiles(ArrayList<File> fileList) {
        int fileIndex = 0;

        /*
         * AttributeList[] tempList = new AttributeList[fileList.size()];
         * while ((fileIndex < fileList.size()) && (processOk)) {
         * int f = 0;
         * while ((fileIndex < fileList.size()) && processOk && (f < UPLOAD_BUFFER_SIZE)) {
         * File file = fileList.get(fileIndex);
         * AttributeList attributeList = new CustomAttributeList();
         * try {
         * attributeList = Util.readDicomFile(file);
         * }
         * catch (Exception e) {
         * String msg = seriesSummary + " Unable to read DICOM file " + file.getAbsolutePath() + " : " + e;
         * Log.get().warning(msg);
         * DicomClient.getInstance().showMessage(msg);
         * new Alert(msg, "Upload Failure");
         * processOk = false;
         * return;
         * }
         * tempList[f] = attributeList;
         * f++;
         * fileIndex++;
         * }
         * AttributeList[] attrListList = null;
         * if (tempList.length == f)
         * attrListList = tempList;
         * else {
         * attrListList = new AttributeList[f];
         * for (int ff = 0; ff < f; ff++)
         * attrListList[ff] = tempList[ff];
         * }
         */

        String failText = "";
        int failCount = 0;
        SetOfDicomFiles setOfDicomFiles = new SetOfDicomFiles();
        for (File file : fileList) {
            try {
                setOfDicomFiles.add(file);
            }
            catch (IOException e) {
                failCount++;
                String message = "Unable to read file " + file.getAbsolutePath() + " : " + e + "\n";
                failText += message;
                Log.get().warning(message);
            }
        }
        if (failCount > 0) {
            String msg = "While uploading, " + failCount + " files out of " + fileList.size() +
                    " failed to upload.  Details below.\n\n" + failText;
            new Alert(msg, "Upload Failure");
        }

        if (!setOfDicomFiles.isEmpty()) {
            pushDicom(setOfDicomFiles);
            if (processOk) {
                Log.get().info("Uploaded file to PACS " + getSelectedAeTitle());
                setProgress(fileIndex);
                DicomClient.getInstance().incrementUploadCount(setOfDicomFiles.size());
            }
        }

        if (processOk) {
            aeTitleUploadList.add(getSelectedAeTitle());
            DicomClient.getInstance().setProcessedStatus();
        }
        previewProgressLayout.show(previewProgressPanel, CARD_SLIDER);
    }

    /**
     * Upload the entire series to the currently selected PACS. If any of the
     * files in the series have a problem uploading, then fail with a message to
     * the user. If the entire upload succeeds, then mark the series as
     * uploaded.
     */
    private void uploadSeries() {
        if (!processOk) return;
        String aeTitle = getSelectedAeTitle();
        zeroProgressBar();
        if (aeTitle == null) {
            String message = "No valid PACS selected.";
            Log.get().warning(message);
            DicomClient.getInstance().showMessage(message);
            return;
        }
        try {
            Log.get().info("Starting upload of " + seriesSummary);

            ArrayList<File> fileList = new ArrayList<File>();
            for (int f = 0; f < instanceList.size(); f++) {
                fileList.add(instanceList.getFile(f));
                DicomClient.getInstance().markScreenAsModified();
            }

            uploadFiles(fileList);
        }
        catch (Exception e) {
            Log.get().severe("Unexpected error in edu.umro.dicom.client.Series.uploadSeries: " + Log.fmtEx(e));
        }
        finally {
            previewProgressLayout.show(previewProgressPanel, CARD_SLIDER);
            DicomClient.getInstance().markScreenAsModified();
        }
    }

    /**
     * Add the given file to this series.
     * 
     * @param fileName
     *            The file to add.
     * 
     * @param attributeList
     *            Parsed version of the DICOM file contents.
     */
    public void addFile(File file, AttributeList attributeList) {
        AnonymizeGUI.getInstance().updateTagList(attributeList);
        String msg = instanceList.put(file, attributeList);
        progressBar.setMaximum(instanceList.size());
        if (msg == null) {
            resetSummary();
        }
        else {
            DicomClient.getInstance().showMessage(msg);
        }
        if (DicomClient.getInstance().getPreview().getPreviewedSeries() == this) {
            int sliceNumber = instanceList.size() / 2;
            sliceNumber = (sliceNumber < 1) ? 1 : sliceNumber;
            showPreview(sliceNumber);
        }
        DicomClient.getInstance().indicateThatStatisticsHaveChanged();
    }

    /**
     * Get the series instance UID.
     * 
     * @return The series instance UID.
     */
    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    /**
     * Determine if this series is equal to another. Two series are considered
     * equal if they have the same series instance UID.
     */
    public boolean equals(Object other) {
        return (other instanceof Series) && seriesInstanceUID.equals(((Series) other).seriesInstanceUID);
    }

    public static volatile boolean processOk = true;

    public void actionPerformed(ActionEvent ev) {
        processOk = true;
        if (ev.getSource() == uploadAnonymizeButton) {
            if (DicomClient.getInstance().getProcessingMode() != ProcessingMode.UPLOAD) {
                if (DicomClient.getInstance().ensureAnonymizeDirectoryExists()) {
                    processSeries();
                }
            }
            else {
                processSeries();
            }
        }

        if (ev.getSource() == previewButton) {
            int slice = instanceList.size() / 2;
            slice = (slice < 1) ? 1 : slice;
            showPreview(slice);
        }
        DicomClient.getInstance().markScreenAsModified();
    }

    @Override
    public String toString() {
        return summaryLabel.getText();
    }

    /**
     * Get the title to be displayed on the preview window.
     * 
     * @param sliceNumber
     *            Current slice being displayed.
     * 
     * @return Orienting description of preview.
     */
    public String getPreviewTitle(int sliceNumber) {
        return getDescription() + "   " + sliceNumber + " / " + instanceList.size();
    }

    /**
     * Get a description of this series.
     * 
     * @return A description of this series.
     */
    public String getDescription() {
        StringBuffer title = new StringBuffer();

        title.append((patientID == null) ? "" : "  " + patientID);
        title.append((patientName == null) ? "" : "  " + patientName);
        title.append((seriesNumber == null) ? "" : "  " + seriesNumber);
        title.append((modality == null) ? "" : "  " + modality);
        title.append((seriesDescription == null) ? "" : "  " + seriesDescription);
        return title.toString();
    }

    /**
     * Show the current slice in the previewer.
     */
    public synchronized void showPreview(int sliceNumber) {
        Preview preview = DicomClient.getInstance().getPreview();
        File file = instanceList.values().get(sliceNumber - 1);
        preview.showDicom(this, getPreviewTitle(sliceNumber), sliceNumber, getFileList().size(), file);
    }

    /**
     * Get the sortedList of file names.
     * 
     * @return List of file names.
     */
    public Collection<File> getFileList() {
        return instanceList.values();
    }

    public int getFileCount() {
        return instanceList.size();
    }

    /**
     * Get the directory where series is stored.
     * 
     * @return Directory where series is stored.
     */
    public File getDirectory() {
        return instanceList.getFirstFile().getParentFile();
    }

    /**
     * Support for testing.
     */
    @AutoTest
    public void doClickPreviewButtonTest() {
        previewButton.doClick();
    }
}
