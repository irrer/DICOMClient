package edu.umro.dicom.client;

/*
 * Copyright 2017 Regents of the University of Michigan
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

import java.io.File;
import java.util.ArrayList;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.TagFromName;

public class OutputOrganization {

    private final char replc = '_';
    private final String fill = "" + replc;

    private final String patientIdText;
    private final String modalityText;
    private final String seriesNumberText;
    private final String instanceNumberText;

    private final ArrayList<String> suffixList;
    private final File seriesOutDir;
    private final File inputFile;
    private final File dir;

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

    private static boolean testFileUniqueness(File file, ArrayList<String> suffixList) {
        String fullName = file.getAbsolutePath();
        String prefix = fullName.substring(0, fullName.lastIndexOf('.'));
        for (int s = 0; s < suffixList.size(); s++) {
            File f = new File(prefix + suffixList.get(s));
            if (f.exists()) return false;
        }
        return true;
    }

    public OutputOrganization(AttributeList attributeList, ArrayList<String> suffixList_, File seriesOutDir_, File inputFile_) {
        patientIdText = Util.getAttributeValue(attributeList, TagFromName.PatientID);
        modalityText = Util.getAttributeValue(attributeList, TagFromName.Modality);
        seriesNumberText = Util.getAttributeValue(attributeList, TagFromName.SeriesNumber);
        String instNo = Util.getAttributeValue(attributeList, TagFromName.InstanceNumber);
        this.suffixList = suffixList_;
        this.seriesOutDir = seriesOutDir_;
        this.inputFile = inputFile_;

        dir = DicomClient.getInstance().getDestinationDirectory();

        if (instNo == null) {
            instNo = "";
        }

        while (instNo.length() < 4) {
            instNo = "0" + instNo;
        }

        instanceNumberText = replc + instNo;
    }

    public File flat() {
        String name = "";
        name += (patientIdText == null) ? "" : patientIdText;
        name += (modalityText == null) ? "" : ("_" + modalityText);
        name += (seriesNumberText == null) ? "" : ("_" + seriesNumberText);
        name += (instanceNumberText == null) ? "" : ("_" + instanceNumberText);

        name = Util.replaceInvalidFileNameCharacters(name.replace(' ', '_'), '_') + Util.DICOM_SUFFIX;

        File destDir = DicomClient.getInstance().getDestinationDirectory();
        System.out.println("destDir: " + destDir);  // TODO rm
        // try the prefix without an extra number to make it unique
        if (testPrefix(name, suffixList)) {
            File newFile = new File(destDir, name);
            newFile.getParentFile().mkdirs();
            return newFile;
        }

        // keep trying different unique numbers until one is found that is not
        // taken
        int count = 1;
        while (true) {
            String uniquifiedName = name + "_" + count;
            if (testPrefix(uniquifiedName, suffixList)) {
                File newFile = new File(destDir, uniquifiedName + Util.DICOM_SUFFIX);
                newFile.getParentFile().mkdirs();
                return newFile;
            }
            count++;
        }
    }

    public File tree() {

        String patId = (patientIdText == null) ? fill : patientIdText;
        File patientDir = new File(dir, Util.replaceInvalidFileNameCharacters(patId, replc));

        String modText = (modalityText == null) ? "" : modalityText + replc;

        String ser = (seriesNumberText == null) ? "" : seriesNumberText;

        String seriesDirName = patId + replc + modText + ser;
        if (seriesDirName.length() == 0) {
            seriesDirName = fill;
        }
        File seriesDir = new File(patientDir, seriesDirName);

        if (seriesOutDir == null) {
            int count = 1;
            // if a directory of this name has files in it, then make a different name.
            while (seriesDir.exists() && (seriesDir.list().length > 0)) {
                seriesDir = new File(patientDir, seriesDirName + replc + count);
                count++;
            }
        }

        String baseName = seriesDirName + instanceNumberText;

        File file = new File(seriesDir, baseName + Util.DICOM_SUFFIX);
        if (testFileUniqueness(file, suffixList)) {
            file.getParentFile().mkdirs();
            return file;
        }

        int count = 1;
        while (true) {
            file = new File(seriesDir, baseName + count + Util.DICOM_SUFFIX);
            if (testFileUniqueness(file, suffixList)) {
                file.getParentFile().mkdirs();
                return file;
            }
        }
    }

    public File local() {
        String name = "";
        name += (patientIdText == null) ? "" : patientIdText;
        name += (modalityText == null) ? "" : ("_" + modalityText);
        name += (seriesNumberText == null) ? "" : ("_" + seriesNumberText);
        name += (instanceNumberText == null) ? "" : ("_" + instanceNumberText);

        name = Util.replaceInvalidFileNameCharacters(name.replace(' ', '_'), '_') + Util.DICOM_SUFFIX;

        File destDir = new File(inputFile.getParent(), DicomClient.getInstance().getDestinationDirectory().getName());

        File newFile = new File(destDir, name);
        newFile.getParentFile().mkdirs();
        return newFile;
    }
}
