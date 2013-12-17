package edu.umro.dicom.client;

import java.util.HashSet;

import com.pixelmed.network.MultipleInstanceTransferStatusHandler;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.network.StorageSOPClassSCU;

import edu.umro.util.Log;

public class DicomPush extends MultipleInstanceTransferStatusHandler {

    private static final String ORIGINATOR_PACS_AETITLE = "DICOMClient";
    private static final int COMPRESSION_LEVEL = 0;
    private static final int MOVE_ORIGINATOR_ID = -1;
    private static final int DEBUG_LEVEL = 0;

    private PACS pacs = null;
    private AttributeList[] attrListList = null;

    int remaining = 0;
    int completed = 0;
    int failed = 0;
    int warning = 0;
    String sopInstanceUID = null;

    @Override
    public void updateStatus(int nRemaining, int nCompleted, int nFailed, int nWarning, String nSopInstanceUID) {
        remaining = nRemaining;
        completed = nCompleted;
        failed = nFailed;
        warning = nWarning;
        sopInstanceUID = nSopInstanceUID;
        Log.get().info("DICOM Push remaining: " + remaining + "  completed: " + completed + "  failed: " + failed + "  warning: " + warning);
    }


    public DicomPush(PACS pacs, AttributeList[] attrListList) {
        this.pacs = pacs;
        this.attrListList = attrListList;
    }


    public String push() {

        Attribute sopClassUID = attrListList[0].get(TagFromName.SOPClassUID);
        if (sopClassUID == null) throw new RuntimeException("DICOM file does not have a SOPSClassUID.");

        HashSet<String> setOfSOPClassUIDs = new HashSet<String>();
        setOfSOPClassUIDs.add(sopClassUID.getSingleStringValueOrEmptyString());


        new StorageSOPClassSCU(pacs.host, pacs.port, pacs.aeTitle, ORIGINATOR_PACS_AETITLE,
                setOfSOPClassUIDs, attrListList,
                COMPRESSION_LEVEL, this,
                ORIGINATOR_PACS_AETITLE, MOVE_ORIGINATOR_ID, DEBUG_LEVEL);

        String error = null;

        if ((completed != attrListList.length) || (failed != 0) || (remaining != 0) || (warning != 0)) {
            error = "Transfer of files to PACS completely or partially failed." +
            "\nDetails: PACS: " + pacs +
            "\n   remaining: " + remaining + "    completed: " + completed + "    failed: " + failed + "    warning: " + warning;
        }

        if (error == null) {
            Log.get().info("Succeeded in DICOM transfer of " + attrListList.length +
                " files from " + ORIGINATOR_PACS_AETITLE + " to " + pacs.toString());
            }
        else Log.get().warning(error);
        return error;
    }

}
