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
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.TagFromName;

import edu.umro.util.Log;

/**
 * Contain the information pertaining to a given study and display it.
 * Each study contains one or more series.  Studies are uniquely identified by
 * their SOP instance UID.
 * 
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
public class Study extends JPanel {

    /** Default ID. */
    private static final long serialVersionUID = 1L;

    private String studyInstanceUid = null;
    private String studyId = null;
    private String studyDate = null;
    private String studyTime = null;
    private String studyDescription = null;

    private String studySummary = null;

    private JPanel seriesListPanel = null;

    @Override
    public boolean equals(Object other) {
        return (other instanceof Study) && (studyInstanceUid.equals(((Study)other).studyInstanceUid));
    }


    public Study(File file, AttributeList attributeList) {

        studyInstanceUid = Util.getAttributeValue(attributeList, TagFromName.StudyInstanceUID);
        studyInstanceUid = (studyInstanceUid == null) ? "" : studyInstanceUid;
        studyId          = Util.getAttributeValue(attributeList, TagFromName.StudyID);
        studyDate        = Util.getAttributeValue(attributeList, TagFromName.StudyDate);
        studyTime        = Util.getAttributeValue(attributeList, TagFromName.StudyTime);
        studyDescription = Util.getAttributeValue(attributeList, TagFromName.StudyDescription);

        studySummary = "Study: ";

        studySummary += (studyDescription == null) ? "" : " " + studyDescription;
        studySummary += (studyId == null) ? "" : "   " + studyId;
        studySummary += (studyDate == null) ? "" : "   " + studyDate;
        studySummary += (studyTime == null) ? "" : "  " + studyTime;

        Log.get().info("Added study");

        setLayout(new BorderLayout());
        JLabel summaryLabel = new JLabel(studySummary);
        summaryLabel.setFont(DicomClient.FONT_MEDIUM);

        add(summaryLabel, BorderLayout.NORTH);

        seriesListPanel = new JPanel();
        BoxLayout seriesListLayout = new BoxLayout(seriesListPanel, BoxLayout.Y_AXIS);
        seriesListPanel.setLayout(seriesListLayout);

        seriesListPanel.add(new Series(file, attributeList));
        add(seriesListPanel, BorderLayout.CENTER);

        int gap = 8;
        Border emptyBorder = BorderFactory.createEmptyBorder(gap, gap, gap, gap);
        setBorder(emptyBorder);
    }

    
    
    
    private ArrayList<Series> findMatchingSeriesUID(AttributeList attributeList) {
        Attribute attr = attributeList.get(TagFromName.SeriesInstanceUID);
        
        ArrayList<Series> list = new ArrayList<Series>();
        String seriesInstanceUID = (attr == null) ? null : attr.getSingleStringValueOrEmptyString();
        for (Component component : seriesListPanel.getComponents()) {
            if (component instanceof Series) {
                Series series = (Series) component;
                if (series.getSeriesInstanceUID().equals(seriesInstanceUID)) {
                    list.add(series);
                }
            }
        }
        return list;
    }
    

    /**
     * Add an instance (slice of a series)
     * 
     * @param file The DICOM file.
     * 
     * @param attributeList The first parameters of the DICOM file.
     */
    public void addInstance(File file, AttributeList attributeList) {
        String seriesInstanceUid = Util.getAttributeValue(attributeList, TagFromName.SeriesInstanceUID);
        seriesInstanceUid = (seriesInstanceUid == null) ? "" : seriesInstanceUid;
        ArrayList<Series> existingSeriesList = findMatchingSeriesUID(attributeList);

        String sopInstanceUID = new String(attributeList.get(TagFromName.SOPInstanceUID).getSingleStringValueOrEmptyString());
        for (Series existingSeries : existingSeriesList) {
            if (existingSeries.getDirectory().equals(file.getParentFile())) {
                if (existingSeries.containsFile(file)) {
                    DicomClient.getInstance().showMessage("File " + file.getAbsolutePath() + " has already been loaded and is being ignored.");
                    return;
                }
                if (existingSeries.containsSOPInstanceUID(sopInstanceUID)) {
                    DicomClient.getInstance().showMessage(
                            "File " + file.getAbsolutePath() + " has the same SOPInstanceUID as a file " +
                                    "already in the same directory is being ignored.  If you want load " +
                                    "two or more files with the same SOPInstanceUID, they must be in different directories (folders).");
                    return;
                }
                else {
                    existingSeries.addFile(file, attributeList);
                    return;
                }
            }
        }

        // If we got here, it means that a new series should be created. This
        // can either be because it is a totally new series or
        // has the same series UID as an existing series but comes from a
        // different directory.
        seriesListPanel.add(new Series(file, attributeList));
    }


    public String getStudyInstanceUID() {
        return studyInstanceUid;
    }


    @Override
    public String toString() {
        return studySummary;
    }


    public void processAll() {
        for (Component component : seriesListPanel.getComponents()) {
            if (component instanceof Series) {
                Series series = (Series)component;
                series.processSeries();
            }
        }
    }

    public void zeroAllProgressBars() {
        for (Component component : seriesListPanel.getComponents()) {
            if (component instanceof Series) {
                Series series = (Series)component;
                series.zeroProgressBar();
            }
        }
    }
    
    /**
     * Get a list of all series in this study.
     * 
     * @return List of series in this study.
     */
    public ArrayList<Series> seriesList() {
        ArrayList<Series> list = new ArrayList<Series>();
        for (Component component : seriesListPanel.getComponents()) {
            if (component instanceof Series) {
                list.add((Series)component);
            }
        }
        return list;
    }

}
