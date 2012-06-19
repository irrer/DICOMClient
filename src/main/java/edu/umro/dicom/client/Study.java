package edu.umro.dicom.client;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.TagFromName;

import edu.umro.dicom.common.Util;
import edu.umro.util.Log;

/**
 * Contain the information pertaining to a given study and display it.
 * Each study contains one or more series.  Studies are uniquely identified by
 * their SOP instance UID.
 * 
 * @author irrer
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


    public Study(String fileName, AttributeList attributeList) {

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

        Log.get().info("Added study: " + studySummary);

        setLayout(new BorderLayout());
        JLabel summaryLabel = new JLabel(studySummary);
        summaryLabel.setFont(DicomClient.FONT_MEDIUM);

        add(summaryLabel, BorderLayout.NORTH);

        seriesListPanel = new JPanel();
        BoxLayout seriesListLayout = new BoxLayout(seriesListPanel, BoxLayout.Y_AXIS);
        seriesListPanel.setLayout(seriesListLayout);

        seriesListPanel.add(new Series(fileName, attributeList));
        add(seriesListPanel, BorderLayout.CENTER);

        int gap = 8;
        Border emptyBorder = BorderFactory.createEmptyBorder(gap, gap, gap, gap);
        setBorder(emptyBorder);
    }


    public void addSeries(String fileName, AttributeList attributeList) {
        String seriesInstanceUid = Util.getAttributeValue(attributeList, TagFromName.SeriesInstanceUID);
        seriesInstanceUid = (seriesInstanceUid == null) ? "" : seriesInstanceUid;
        for (Component component : seriesListPanel.getComponents()) {
            if (component instanceof Series) {
                Series series = (Series)component;
                if (series.getSeriesInstanceUID().equals(seriesInstanceUid)) {
                    series.addFile(fileName, attributeList);
                    return;
                }
            }
        }
        seriesListPanel.add(new Series(fileName, attributeList));
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

}
