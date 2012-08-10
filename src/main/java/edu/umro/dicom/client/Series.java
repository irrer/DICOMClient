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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomOutputStream;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.SOPClassDescriptions;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.display.ConsumerFormatImageMaker;

import edu.umro.dicom.common.Anonymize;
import edu.umro.dicom.common.Util;
import edu.umro.util.Log;
import edu.umro.util.UMROException;
import edu.umro.util.Utility;

/**
 * Represent a DICOM series.
 * 
 * @author Jim Irrer  irrer@umich.edu
 *
 */

public class Series extends JPanel implements ActionListener {

    /** Default ID. */
    private static final long serialVersionUID = 1L;

    /** Suffix for creating DICOM files. */
    private static final String DICOM_SUFFIX = ".DCM";

    /** Card layout identifier for preview slider mode. */
    static private final String CARD_SLIDER = "Slider";

    /** Card layout identifier for upload progress mode. */
    static private final String CARD_PROGRESS = "Progress";

    /** Tool tip for Upload All button. */
    private static final String UPLOAD_BUTTON_TOOLTIP_TEXT_ENABLED = "<html>Upload this series<br>to the selected<br>PACS server.</html>";

    /** Tool tip for Upload All button. */
    private static final String UPLOAD_BUTTON_TOOLTIP_TEXT_DISABLED = "<html>Log in and Select a PACS<br>server to enable uploading.</html>";

    /** Tool tip for Upload All button. */
    private static final String ANONYMIZE_BUTTON_TOOLTIP = "<html>AnonymizeGUI this series,<br>overwriting files.</html>";

    /** DICOM value for Patient ID for the series. */
    private String patientID = null;

    /** DICOM value for Patient name for the series. */
    private String patientName = null;

    /** True if this series has been anonymized. */
    private boolean isAnonymized = false;


    /** DICOM value for series number for the series. */
    private String seriesNumber = null;
    /** DICOM value for modality for the series. */
    private String modality = null;
    /** DICOM value for series description for the series. */
    private String seriesDescription = null;


    /** The next two sets of DICOM values are dates and times.  Frequently,
     * a DICOM file is generated with one or another date or time field
     * filled in, but others left vacant.  To get some date and time to
     * show to the user, several are examined to see if they are present
     * in the hope that at least one of them is.  This situation is not
     * ideal (all DICOM files should always have a minimal set of values),
     * but vendors will do what they will.
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

    /** Short human readable description of series constructed from its DICOM values. */
    private JLabel summaryLabel = null;

    /** Button that user uses to initiate uploads and anonymization. */
    private JButton uploadAnonymizeButton = null;

    /** Icon associated with upload button to indicate whether or not the series has been uploaded. */ 
    private JLabel uploadButtonIcon = null;

    /** Shows preview. */
    private JButton previewButton = null;

    /** Shows progressBar of upload. */
    private JProgressBar progressBar = null;

    /** Panel that shows the progress bar or preview slider. */
    private JPanel previewProgressPanel = null;

    /** Layout that shows the progress bar or preview slider. */
    private CardLayout previewProgressLayout = null;

    private class InstanceList {
        private class Instance implements Comparable<Instance> {
            public final int instanceNumber;
            public String sopInstanceUID;
            public String fileName;

            public Instance(int instanceNumber, String sopInstanceUID, String fileName) {
                this.instanceNumber = instanceNumber;
                this.sopInstanceUID = sopInstanceUID;
                this.fileName = fileName;
            }

            @Override
            public int compareTo(Instance o) {
                return this.instanceNumber - o.instanceNumber;
            }
        }

        private ArrayList<Instance> instList = new ArrayList<Instance>();

        private HashSet<String> sopList = new HashSet<String>();
        private HashSet<String> fileList = new HashSet<String>();
        private ArrayList<String> sortedList = null;

        
        /**
         * Get the sortedList of file names in ascending order by instance number.
         * 
         * @return List of file names.
         */
        public ArrayList<String> values() {
            if (sortedList == null) {
                Collections.sort(instList);
                sortedList = new ArrayList<String>();
                for (Instance inst : instList) {
                    sortedList.add(inst.fileName);
                }
            }
            return sortedList;
        }
        
        
        public int size() {
            return instList.size();
        }


        public String put(String fileName, AttributeList attributeList) {

            if (fileList.contains(fileName)) {
                return "The file " + fileName + " has already been loaded.";
            }

            String sopInstanceUID = attributeList.get(TagFromName.SOPInstanceUID).getSingleStringValueOrEmptyString();
            if (sopList.contains(sopInstanceUID)) {
                String oldFile = "";
                for (Instance instance : instList) {
                    if (instance.sopInstanceUID == sopInstanceUID) {
                        oldFile = instance.fileName;
                        break;
                    }
                }
                return "The SOP Instance UID " + sopInstanceUID + " was already loaded with from file " + oldFile + ", so ignoring file " + fileName;
            }

            Attribute instanceNumberAttr = attributeList.get(TagFromName.InstanceNumber);
            int instanceNumber = 0;
            if (instanceNumberAttr != null) {
                instanceNumber = instanceNumberAttr.getSingleIntegerValueOrDefault(0);
            }

            instList.add(new Instance(instanceNumber, sopInstanceUID, fileName));
            sopList.add(sopInstanceUID);
            fileList.add(fileName);
            sortedList = null;

            return null;
        }
    }

    /** List of file names for this series. */
    private InstanceList instanceList = new InstanceList();

    /** List of PACS AE titles to which this series has been uploaded. */
    private HashSet<String> aeTitleUploadList = new HashSet<String>();


    /**
     * Find the first non-null value in a sortedList.
     * 
     * @param textList List of values.
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

        String seriesSummary = "        Series: ";
        seriesSummary += (seriesNumber == null     ) ? "" : " " + seriesNumber;
        seriesSummary += (modality == null         ) ? " No modality" : " " + modality;
        seriesSummary += (seriesDescription == null) ? "" : " " + seriesDescription;
        seriesSummary += (date == null             ) ? "" : " " + date;
        seriesSummary += (time == null             ) ? "" : " " + time;

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
        uploadButtonIcon = new JLabel(PreDefinedIcons.getEmpty());
        JPanel uploadButtonPanel = new JPanel();
        uploadButtonPanel.add(uploadAnonymizeButton);
        uploadButtonPanel.add(uploadButtonIcon);
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
     * Build a GUI component that displays and controls the uploading of a single series.
     * 
     * @param fileName Name of DICOM file from which the series originated.
     * 
     * @param attributeList Parsed version of DICOM file contents.
     */
    public Series(String fileName, AttributeList attributeList) {

        patientID             = Util.getAttributeValue(attributeList, TagFromName.PatientID);
        patientID             = (patientID == null) ? "No patient id" : patientID;
        patientName           = Util.getAttributeValue(attributeList, TagFromName.PatientName);
        patientName           = (patientName == null) ? "No patient name" : patientName;

        modality              = Util.getAttributeValue(attributeList, TagFromName.Modality);
        if (modality == null) {
            modality = SOPClassDescriptions.getAbbreviationFromUID(Util.getAttributeValue(attributeList, TagFromName.MediaStorageSOPClassUID));
        }
        modality              = (modality == null) ? "No modality" : modality;
        seriesDescription     = Util.getAttributeValue(attributeList, TagFromName.SeriesDescription);

        // try lots of ways to get the date, because some machines fill in some dates and others fill in others.
        seriesDate            = Util.getAttributeValue(attributeList, TagFromName.SeriesDate);
        contentDate           = Util.getAttributeValue(attributeList, TagFromName.ContentDate);
        acquisitionDate       = Util.getAttributeValue(attributeList, TagFromName.AcquisitionDate);
        instanceCreationDate  = Util.getAttributeValue(attributeList, TagFromName.InstanceCreationDate);
        rtPlanDate            = Util.getAttributeValue(attributeList, TagFromName.RTPlanDate);
        structureSetDate      = Util.getAttributeValue(attributeList, TagFromName.StructureSetDate);

        // try lots of times also.
        seriesTime            = Util.getAttributeValue(attributeList, TagFromName.SeriesTime);
        contentTime           = Util.getAttributeValue(attributeList, TagFromName.ContentTime);
        acquisitionTime       = Util.getAttributeValue(attributeList, TagFromName.AcquisitionTime);
        instanceCreationTime  = Util.getAttributeValue(attributeList, TagFromName.InstanceCreationTime);
        rtPlanTime            = Util.getAttributeValue(attributeList, TagFromName.RTPlanTime);
        structureSetTime      = Util.getAttributeValue(attributeList, TagFromName.StructureSetTime);

        instanceCreationDate  = Util.getAttributeValue(attributeList, TagFromName.InstanceCreationDate);
        instanceCreationTime  = Util.getAttributeValue(attributeList, TagFromName.InstanceCreationTime);
        seriesInstanceUID     = Util.getAttributeValue(attributeList, TagFromName.SeriesInstanceUID);
        seriesInstanceUID = (seriesInstanceUID == null) ? "" : seriesInstanceUID;

        buildGui();
        resetSummary();
        addFile(fileName, attributeList);
        Log.get().info("Added series: " + summaryLabel.getText());
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
     * @param value Value to set the progress bar to.
     */
    private void setProgress(int value) {
        previewProgressLayout.show(previewProgressPanel, CARD_PROGRESS);
        progressBar.setValue(value);
        Graphics graphics = previewProgressPanel.getGraphics();
        previewProgressPanel.paintAll(graphics);
    }


    /**
     * Set the icon to reflect whether this series has been anonymized or uploaded to
     * the currently selected PACS.
     * 
     * @return True if it has been uploaded, false if not.
     */
    public boolean setProcessedStatus() {
        if (DicomClient.getInstance().getAnonymizeMode()) {
            uploadButtonIcon.setIcon(isAnonymized ? PreDefinedIcons.getOk() : PreDefinedIcons.getEmpty());
            uploadAnonymizeButton.setToolTipText(ANONYMIZE_BUTTON_TOOLTIP);
            uploadAnonymizeButton.setText("Anonymize");
            uploadAnonymizeButton.setEnabled(true);
            return false;
        }
        else {
            String aeTitle = DicomClient.getInstance().getSelectedAeTitle();
            boolean isUploaded = aeTitleUploadList.contains(aeTitle);
            Icon icon = isUploaded ? PreDefinedIcons.getOk() : PreDefinedIcons.getEmpty();
            uploadButtonIcon.setIcon(icon);
            boolean enabled = DicomClient.getInstance().uploadEnabled();
            uploadAnonymizeButton.setEnabled(enabled);
            uploadAnonymizeButton.setToolTipText(enabled ? UPLOAD_BUTTON_TOOLTIP_TEXT_ENABLED : UPLOAD_BUTTON_TOOLTIP_TEXT_DISABLED);
            uploadAnonymizeButton.setText("Upload");
            return isUploaded;
        }
    }


    /**
     * Upload the given stream to the current PACS.
     * 
     * @param urlText DICOM service URL.
     * 
     * @param seriesSummary Description of this series.
     * 
     * @param inputStream Stream containing content to upload.
     * 
     * @return True on success.
     */
    private static boolean uploadStream(String urlText, String seriesSummary, InputStream inputStream) {
        boolean ok = false;
        Response response = null;
        try {
            Client client = new Client(Protocol.HTTPS);
            Request request = new Request(Method.PUT, new Reference(urlText));
            DicomClient.getInstance().setChallengeResponse(request);
            Representation representation = new InputRepresentation(inputStream, MediaType.APPLICATION_OCTET_STREAM);
            request.setEntity(representation);
            request.setMethod(Method.PUT);
            response = client.handle(request);
            representation.release();
            Status status = response.getStatus();

            if (status.equals(Status.SUCCESS_OK)) {
                ok = true;
            }
            else {
                String message =
                    "Problem uploading series " + seriesSummary + ".  Remaining uploads for this series are being aborted.\n" +
                    "    Server status: " + status + " : " + response.getEntityAsText();
                DicomClient.getInstance().showMessage(message);
            }
        }
        catch (ResourceException ex) {
            String message =
                "Problem uploading series " + seriesSummary + " to " + urlText + " .  Remaining uploads for this series are being aborted. : " + ex;
            if (response != null) {
                message += "\n    Server status: " + response.getStatus() + " : " + response.getEntityAsText();
            }
            DicomClient.getInstance().showMessage(message);
            Log.get().severe(message);
        }
        catch (Exception e) {
            String message =
                "Unexpected problem while uploading series " + seriesSummary + " to " + urlText + ".  Remaining uploads for this series are being aborted." + e;
            if (response != null) {
                message += "\n    Server status: " + response.getStatus() + " : " + response.getEntityAsText();
            }
            DicomClient.getInstance().showMessage(message);
            Log.get().severe(message);
        }
        return ok;
    }


    /**
     * Perform either anonymization or upload on this series depending on the mode.
     */
    public void processSeries() {
        if (DicomClient.getInstance().getAnonymizeMode()) {
            anonymizeSeries();
        }
        else {
            uploadSeries();
        }
        DicomClient.getInstance().setProcessedStatus();
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
        return (Patient)pat;
    }


    /**
     * Get the sortedList of anonymization values for this series.
     * 
     * @return The sortedList of anonymization values for this series.
     * 
     * @throws DicomException On invalid PatientID or PatientName
     */
    private synchronized AttributeList getAnonymizingReplacementList() throws DicomException {
        AttributeList replacementAttributeList = DicomClient.getInstance().getAnonymizeGui().getAttributeList();

        Attribute patientId = AttributeFactory.newAttribute(TagFromName.PatientID);
        patientId.addValue(getPatient().getAnonymizePatientIdText());
        replacementAttributeList.put(patientId);

        Attribute patientName = AttributeFactory.newAttribute(TagFromName.PatientName);
        patientName.addValue(getPatient().getAnonymizePatientNameText());
        replacementAttributeList.put(patientName);

        return replacementAttributeList;
    }


    /**
     * Get an anonymized name for the file.
     * 
     * @param attributeList
     * 
     * @return
     */
    private File getNewFileName(File dir, AttributeList attributeList) {

        if ((DicomClient.inCommandLineMode()) && (DicomClient.getOutputFile() != null)) {
            if (DicomClient.getInstance().getFileCount() > 1) {
                dir = DicomClient.getOutputFile();
            }
            else {
                return DicomClient.getOutputFile();
            }
        }

        String patientIdText      = Util.getAttributeValue(attributeList, TagFromName.PatientID);
        String modalityText       = Util.getAttributeValue(attributeList, TagFromName.Modality);
        String seriesNumberText   = Util.getAttributeValue(attributeList, TagFromName.SeriesNumber);
        String instanceNumberText = Util.getAttributeValue(attributeList, TagFromName.InstanceNumber);

        while ((instanceNumberText != null) && (instanceNumberText.length() < 4)) {
            instanceNumberText = "0" + instanceNumberText;
        }

        String name = "";
        name += (patientIdText      == null) ? "" : patientIdText;
        name += (modalityText       == null) ? "" : ("_" + modalityText);
        name += (seriesNumberText   == null) ? "" : ("_" + seriesNumberText);
        name += (instanceNumberText == null) ? "" : ("_" + instanceNumberText);

        name = name.replace(' ', '_');

        File file = new File(dir, name + DICOM_SUFFIX);
        int count = 1;
        while (file.exists()) {
            file = new File(dir, name + "_" + count + DICOM_SUFFIX);
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }
        return file;
    }


    /**
     * If in command line mode, save the anonymized DICOM as text and, if
     * possible, as an image.
     * 
     * @param attributeList Anonymized DICOM.
     * 
     * @param file File where anonymized DICOM is to be written.
     */
    private void saveTextAndImageFiles(AttributeList attributeList, File file) {
        if (DicomClient.inCommandLineMode()) {
            StringBuffer text = new StringBuffer();
            DicomClient.getInstance().getPreview().addTextAttributes(attributeList, text, 0);
            String fileName = file.getName();
            String textFileName = null;
            String imageFileName = null;
            File dir = (file.getParentFile() == null) ? new File(".") : file.getParentFile();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex == -1) {
                textFileName = fileName + ".TXT";
                imageFileName = fileName + ".JPG";
            }
            else {
                String baseName = fileName.substring(0, dotIndex);
                textFileName = baseName + ".TXT";
                imageFileName = baseName + ".PNG";
            }

            File textFile = new File(dir, textFileName);
            try {
                textFile.delete();
                textFile.createNewFile();
                Utility.writeFile(textFile, text.toString().getBytes());
            }
            catch (UMROException e) {
                System.err.println("Unable to write anonymized text file: " + e);
                System.exit(1);
            }
            catch (IOException e) {
                System.err.println("Unable to create anonymized text file " + textFile.getAbsolutePath() + " : " + e);
                System.exit(1);
            }

            try {
                BufferedImage image = ConsumerFormatImageMaker.makeEightBitImage(attributeList, 0);
                File imageFile = new File(dir, imageFileName);
                try {
                    ImageIO.write(image, "png", imageFile);
                }
                catch (IOException e) {
                    Log.get().warning("Unable to write image file as part of anonymization for file " + imageFile.getAbsolutePath());
                }
            }
            catch (DicomException e) {
                // Ignore exception because this was not an image file.
            }
        }
    }


    /**
     * AnonymizeGUI this series and write the results to new files.
     */
    private void anonymizeSeries() {
        int count = 0;
        try {
            for (String fileName : instanceList.values()) {
                AttributeList attributeList = new AttributeList();
                attributeList.read(fileName);

                //DicomClient.getInstance().getAnonymize().anonymizeAttributeList(attributeList);
                Anonymize.anonymize(attributeList, getAnonymizingReplacementList());
                FileMetaInformation.addFileMetaInformation(attributeList, TransferSyntax.ExplicitVRLittleEndian, "DICOMService");

                // set up to put file in new directory
                File newDir = DicomClient.getInstance().getDestination();
                File newFile = getNewFileName(newDir, attributeList);
                File parent = (newFile.getParentFile() == null) ? new File(".") : newFile.getParentFile();
                parent.mkdirs();
                attributeList.write(newFile, TransferSyntax.ExplicitVRLittleEndian, true, true);
                saveTextAndImageFiles(attributeList, newFile);
                count++;
                Log.get().info("Anonymized to file: " + newFile.getAbsolutePath());
            }
            isAnonymized = true;
            setProcessedStatus();
        }
        catch (DicomException e) {
            String msg = "DICOM error - unable to anonymize series " + toString() + " : " + e;
            Log.get().severe(msg);
            if (DicomClient.inCommandLineMode()) {
                System.err.println(msg);
                System.exit(1);
            }
        }
        catch (IOException e) {
            String msg = "File error - unable to anonymize series " + toString() + " : " + e;
            Log.get().severe(msg);
            if (DicomClient.inCommandLineMode()) {
                System.err.println(msg);
                System.exit(1);
            }
        }
        if (count != instanceList.size()) {
            DicomClient.getInstance().showMessage("Unable to anonymize series " + toString() + " .  Only " + count + " slices of " + instanceList.size() + " were completed.");
        }
    }


    /**
     * Upload the entire series to the currently selected PACS.  If any of the files
     * in the series have a problem uploading, then fail with a message to the user.
     * If the entire upload succeeds, then mark the series as uploaded.
     */
    private void uploadSeries() {
        String aeTitle = getSelectedAeTitle();
        String urlText = ClientConfig.getInstance().getServerBaseUrl();
        zeroProgressBar();
        int fileIndex = 0;
        if (aeTitle == null) {
            String message = "No valid PACS selected.";
            Log.get().warning(message);
            DicomClient.getInstance().showMessage(message);
            return;
        }
        if (urlText != null) {
            try {
                urlText += "/dicom/put?pacs=" + aeTitle + "&user_id=" + DicomClient.getInstance().getLoginName();
                Log.get().info("Starting upload of " + instanceList.size() + " file(s) from series " + summaryLabel.getText() + " to PACS " + aeTitle + " using base url: " + urlText);
                String seriesSummary = "";
                seriesSummary += (patientID         == null) ? "" : " " + patientID;
                seriesSummary += (patientName       == null) ? "" : " " + patientName;
                seriesSummary += (seriesNumber      == null) ? "" : " " + seriesNumber;
                seriesSummary += (modality          == null) ? "" : " " + modality;
                seriesSummary += (seriesDescription == null) ? "" : " " + seriesDescription;

                try {
                    KeyObject keyObject = new KeyObject(seriesSummary, instanceList.values());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    DicomOutputStream dicomOutputStream = new DicomOutputStream(byteArrayOutputStream, TransferSyntax.ExplicitVRLittleEndian, TransferSyntax.ExplicitVRLittleEndian);
                    keyObject.write(dicomOutputStream);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    if (!uploadStream(urlText, seriesSummary, byteArrayInputStream)) {
                        String msg = "Could not upload Key Object file for series " + seriesSummary;
                        Log.get().info(msg);
                        DicomClient.getInstance().showMessage(msg);
                        return;
                    }
                }
                catch (Exception e) {
                    Log.get().info("Upload failed.  Could not generate Key Object File for series " + seriesSummary + " : " + e);
                    return;
                }

                for (String fileName : instanceList.values()) {
                    InputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(new File(fileName));
                    }
                    catch (FileNotFoundException e) {
                        Log.get().severe("Unable to upload file " + fileName + " . Was it deleted, renamed, or moved?   Error: " + e);
                    }

                    if (uploadStream(urlText, seriesSummary, inputStream)) {
                        fileIndex++;
                        Log.get().info("Uploaded file " + fileName + " from series " + seriesSummary + " to PACS " + aeTitle);
                        setProgress(fileIndex);
                    }
                    else {
                        return;
                    }
                }
                aeTitleUploadList.add(aeTitle);
                DicomClient.getInstance().setProcessedStatus();
            }
            finally {
                previewProgressLayout.show(previewProgressPanel, CARD_SLIDER);
            }
        }
        Log.get().info("Uploaded " + fileIndex + " of " + instanceList.size() + " files.");
    }


    /**
     * Add the given file to this series.
     * 
     * @param fileName The file to add.
     * 
     * @param attributeList Parsed version of the DICOM file contents.
     */
    public void addFile(String fileName, AttributeList attributeList) {
        String msg = instanceList.put(fileName, attributeList);
        if (msg == null) {
            resetSummary();
        }
        else {
            DicomClient.getInstance().showMessage(msg);
        }
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
     * Determine if this series is equal to another.  Two series are
     * considered equal if they have the same series instance UID.
     */
    public boolean equals(Object other) {
        return (other instanceof Series) && seriesInstanceUID.equals(((Series)other).seriesInstanceUID);
    }


    @Override
    public void actionPerformed(ActionEvent ev) {
        Profile.profile();
        if (ev.getSource() == uploadAnonymizeButton) {
            processSeries();
        }

        if (ev.getSource() == previewButton) {
            int slice = instanceList.size() / 2;
            slice = (slice < 1) ? 1 : slice;
            showPreview(slice);
        }
        Profile.profile();
    }


    @Override
    public String toString() {
        return summaryLabel.getText();
    }

    private volatile boolean inPreview = false;

    /**
     * Show the current slice in the previewer.
     */
    public synchronized void showPreview(int value) {
        if (!inPreview) {
            inPreview = true;
            try {
                Profile.profile();
                Preview preview = DicomClient.getInstance().getPreview();
                Profile.profile();
                preview.setSeries(this);
                Profile.profile();
                
                String fileName = instanceList.values().get(value-1);
                StringBuffer title = new StringBuffer();

                title.append((patientID         == null) ? "" : "  " + patientID);
                title.append((patientName       == null) ? "" : "  " + patientName);
                title.append((seriesNumber      == null) ? "" : "  " + seriesNumber);
                title.append((modality          == null) ? "" : "  " + modality);
                title.append((seriesDescription == null) ? "" : "  " + seriesDescription);
                title.append("   " + value + " / " + instanceList.size());

                Profile.profile();
                preview.showDicom(title.toString(), fileName);
                Profile.profile();
            }
            finally {
                inPreview = false;
            }
        }
    }


    /**
     * Get the sortedList of file names.
     * 
     * @return List of file names.
     */
    public Collection<String> getFileNameList() {
        return instanceList.values();
    }

}
