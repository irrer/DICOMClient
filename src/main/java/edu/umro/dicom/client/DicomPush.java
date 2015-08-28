package edu.umro.dicom.client;

/*
 * Copyright 2015 Regents of the University of Michigan
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

import com.pixelmed.network.MultipleInstanceTransferStatusHandler;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.network.StorageSOPClassSCU;
import edu.umro.util.Log;

public class DicomPush extends MultipleInstanceTransferStatusHandler {

    private static final int COMPRESSION_LEVEL = 0;
    private static final int DEBUG_LEVEL = 0;

    private PACS pacs = null;
    private SetOfDicomFiles setOfDicomFiles = null;

    int remaining = 0;
    int completed = 0;
    int failed = 0;
    int warning = 0;
    String sopInstanceUID = null;
    MultipleInstanceTransferStatusHandler multipleInstanceTransferStatusHandler = null;

    @Override
    public void updateStatus(int nRemaining, int nCompleted, int nFailed, int nWarning, String nSopInstanceUID) {
        remaining = nRemaining;
        completed = nCompleted;
        failed = nFailed;
        warning = nWarning;
        sopInstanceUID = nSopInstanceUID;
        Log.get().info("DICOM Push remaining: " + remaining + "  completed: " + completed + "  failed: " + failed + "  warning: " + warning);
        if (multipleInstanceTransferStatusHandler != null) multipleInstanceTransferStatusHandler.updateStatus(nRemaining, nCompleted, nFailed, nWarning, nSopInstanceUID);
    }

    public DicomPush(PACS pacs, SetOfDicomFiles setOfDicomFiles, MultipleInstanceTransferStatusHandler multipleInstanceTransferStatusHandler) {
        this.pacs = pacs;
        this.setOfDicomFiles = setOfDicomFiles;
        this.multipleInstanceTransferStatusHandler = multipleInstanceTransferStatusHandler;
    }

    public String push() {

        new StorageSOPClassSCU(
                pacs.host,
                pacs.port,
                pacs.aeTitle,
                PACSConfig.getInstance().getMyDicomAETitle(),
                setOfDicomFiles,
                COMPRESSION_LEVEL,
                this,
                DEBUG_LEVEL);

        String error = null;

        if ((completed != setOfDicomFiles.size()) || (failed != 0) || (remaining != 0) || (warning != 0)) {
            error = "Transfer of files to PACS completely or partially failed." +
                    "\nDetails: PACS: " + pacs +
                    "\n   remaining: " + remaining + "    completed: " + completed + "    failed: " + failed + "    warning: " + warning;
        }

        if (error == null) {
            Log.get().info("Succeeded in DICOM transfer of " + setOfDicomFiles.size() +
                    " files from " + PACSConfig.getInstance().getMyDicomAETitle() + " to " + pacs.toString());
        }
        else
            Log.get().warning(error);
        return error;
    }

}
