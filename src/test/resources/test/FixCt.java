import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.TagFromName;

import edu.umro.dicom.client.Util;
import edu.umro.util.Utility;

public class FixCt {

    class CT {
        File file;
        AttributeList al;

        CT(File file, AttributeList al) {
            this.file = file;
            this.al = al;
        }

        public String toString() {
            return file.getAbsolutePath(); // al.get(TagFromName.SOP)
        }
    }

    private static final String UM_TREATMENT_MACHINE_NAME = "UM-EX1";
    // private static final String UM_TREATMENT_MACHINE_NAME = "Eclipse CAP";

    private ArrayList<File> rtstructFileList = new ArrayList<File>();

    private ArrayList<CT> ctList = new ArrayList<CT>();

    private void readFiles(File dir) throws IOException, DicomException {
        for (File file : dir.listFiles()) {
            AttributeList al = new AttributeList();
            al.read(file);
            String modality = al.get(TagFromName.SOPClassUID).getSingleStringValueOrNull();
            if (modality.equals(SOPClass.CTImageStorage)) ctList.add(new CT(file, al));
            if (modality.equals(SOPClass.RTStructureSetStorage)) rtstructFileList.add(file);
            // System.out.println(modality + " : " + al.get(TagFromName.SOPInstanceUID).getSingleStringValueOrNull());
        }
    }

    private SequenceAttribute getSubSequence(SequenceAttribute parent, AttributeTag tag) {
        Attribute a = parent.getItem(0).getAttributeList().get(tag);
        return (SequenceAttribute) a;
    }

    private SequenceAttribute getContourImageSequence(AttributeList rtstruct) {
        SequenceAttribute ReferencedFrameOfReferenceSequence = (SequenceAttribute) rtstruct.get(TagFromName.ReferencedFrameOfReferenceSequence);

        SequenceAttribute RTReferencedStudySequence = getSubSequence(ReferencedFrameOfReferenceSequence, TagFromName.RTReferencedStudySequence);

        SequenceAttribute RTReferencedSeriesSequence = getSubSequence(RTReferencedStudySequence, TagFromName.RTReferencedSeriesSequence);

        SequenceAttribute ContourImageSequence = getSubSequence(RTReferencedSeriesSequence, TagFromName.ContourImageSequence);
        return ContourImageSequence;
    }

    private ArrayList<String> getCtUids() throws IOException, DicomException {
        ArrayList<String> ctUidList = new ArrayList<String>();

        AttributeList rtstruct = new AttributeList();
        rtstruct.read(rtstructFileList.get(0));
        /*
         * SequenceAttribute ReferencedFrameOfReferenceSequence = (SequenceAttribute)
         * rtstruct.get(TagFromName.ReferencedFrameOfReferenceSequence);
         * 
         * SequenceAttribute RTReferencedStudySequence = getSubSequence(ReferencedFrameOfReferenceSequence,
         * TagFromName.RTReferencedStudySequence);
         * 
         * SequenceAttribute RTReferencedSeriesSequence = getSubSequence(RTReferencedStudySequence,
         * TagFromName.RTReferencedSeriesSequence);
         * 
         * SequenceAttribute ContourImageSequence = getSubSequence(RTReferencedSeriesSequence,
         * TagFromName.ContourImageSequence);
         */

        SequenceAttribute ContourImageSequence = getContourImageSequence(rtstruct);

        for (int c = 0; c < ContourImageSequence.getNumberOfItems(); c++) {
            String ctUid = ContourImageSequence.getItem(c).getAttributeList().get(TagFromName.ReferencedSOPInstanceUID).getSingleStringValueOrNull();
            ctUidList.add(ctUid);
        }

        return ctUidList;
    }

    private Attribute generateAttribute(AttributeTag tag, String value) throws DicomException {
        Attribute a = AttributeFactory.newAttribute(tag);
        a.addValue(value);
        return a;
    }

    private AttributeList generateDemographics(File directory) throws DicomException {
        AttributeList al = new AttributeList();
        String name = directory.getName();
        al.put(generateAttribute(TagFromName.PatientBirthDate, "18000101"));
        al.put(generateAttribute(TagFromName.PatientID, "H" + name));
        al.put(generateAttribute(TagFromName.PatientName, "H" + name));
        return al;
    }

    private File makeDestDir(File sourceDir) {
        File destDir = new File(sourceDir.getParentFile(), sourceDir.getName() + "_fixed");
        Utility.deleteFileTree(destDir);
        destDir.mkdirs();
        return destDir;
    }

    private void writeFile(File destFile, AttributeList source, AttributeList demographics) throws IOException, DicomException {
        Set<Map.Entry<AttributeTag, Attribute>> set = (Set<Map.Entry<AttributeTag, Attribute>>) (demographics.entrySet());
        for (Entry<AttributeTag, Attribute> e : set) {
            source.put(e.getValue());
        }
        source.write(destFile, Util.DEFAULT_TRANSFER_SYNTAX, true, true);
    }

    private void fixSop(File file, AttributeList al, ArrayList<String> ctUidList) throws DicomException {
        String absFilePath = file.getAbsolutePath();
        for (int c = 0; c < ctList.size(); c++) {
            CT ct = ctList.get(c);
            if (ct.file.getAbsolutePath().equals(absFilePath)) {
                {
                    Attribute inst = AttributeFactory.newAttribute(TagFromName.SOPInstanceUID);
                    if ((c < 0) || (c >= ctUidList.size())) {
                        System.out.println("File " + file.getAbsolutePath() + " Index out of bounds for ctUidList: " + c + "    ctUidList size: " + ctUidList.size());
                    }
                    else {
                        inst.addValue(ctUidList.get(c));
                        al.put(inst);
                    }
                }

                {
                    Attribute media = AttributeFactory.newAttribute(TagFromName.MediaStorageSOPInstanceUID);
                    media.addValue(ctUidList.get(c));
                    al.put(media);
                }

                {
                    Attribute RescaleIntercept = AttributeFactory.newAttribute(TagFromName.RescaleIntercept);
                    RescaleIntercept.addValue("-1024");
                    al.put(RescaleIntercept);
                }

                {
                    Attribute RescaleSlope = AttributeFactory.newAttribute(TagFromName.RescaleSlope);
                    RescaleSlope.addValue("1");
                    al.put(RescaleSlope);
                }

                return;
            }
        }
        throw new RuntimeException("Could not find file in list: " + absFilePath);
    }

    /**
     * Find all occurrences of the given tag and change the value to
     * the given value.
     * 
     * @param al
     * @throws DicomException
     */
    private void fixAttribute(AttributeList al, AttributeTag tag, String value) throws DicomException {
        Attribute attr = al.get(tag);
        if (attr != null) {
            attr = AttributeFactory.newAttribute(tag);
            attr.addValue(value);
            al.put(attr);
        }
        Set<Map.Entry<AttributeTag, Attribute>> set = (Set<Map.Entry<AttributeTag, Attribute>>) (al.entrySet());

        for (Entry<AttributeTag, Attribute> entry : set) {
            if (entry.getValue() instanceof SequenceAttribute) {
                SequenceAttribute seq = (SequenceAttribute) entry.getValue();
                for (int l = 0; l < seq.getNumberOfItems(); l++) {
                    fixAttribute(seq.getItem(l).getAttributeList(), tag, value);
                }
            }
        }
    }

    /**
     * Find all occurrences of the TreatmentMachineName tag and change the name to
     * the UM machine name.
     * 
     * @param al
     * @throws DicomException
     */
    private void fixMachineName(AttributeList al) throws DicomException {
        fixAttribute(al, TagFromName.TreatmentMachineName, UM_TREATMENT_MACHINE_NAME);
    }

    private void hackFractionGroupSequence(AttributeList al) throws DicomException {
        SequenceAttribute FractionGroupSequence = (SequenceAttribute) (al.get(TagFromName.FractionGroupSequence));
        int numItems = FractionGroupSequence.getNumberOfItems();
        // If only 1 fraction group, then do nothing. Otherwise, remove all but
        // first fraction group. This is necessary because Aria only supports one
        // fraction group.
        if (numItems > 1) {
            SequenceAttribute fgs = new SequenceAttribute(TagFromName.FractionGroupSequence);
            fgs.addItem(FractionGroupSequence.getItem(0));
            al.put(fgs);
            FractionGroupSequence = fgs;
        }
    }

    private void writeAllFiles(File directory, ArrayList<String> ctUidList) throws IOException, DicomException {
        AttributeList basicDemographics = generateDemographics(directory);
        File destDir = makeDestDir(directory);
        for (File file : directory.listFiles()) {
            AttributeList al = new AttributeList();
            al.read(file);
            String modality = al.get(TagFromName.SOPClassUID).getSingleStringValueOrNull();
            if (modality.equals(SOPClass.CTImageStorage)) fixSop(file, al, ctUidList);
            if (modality.equals(SOPClass.RTPlanStorage)) {
                fixMachineName(al);
                fixAttribute(al, TagFromName.NominalBeamEnergy, "6");
                fixAttribute(al, TagFromName.DoseRateSet, "600");
                hackFractionGroupSequence(al);
            }
            if (modality.equals(SOPClass.RTDoseStorage)) {
                fixMachineName(al);
                File rtDoseDir = new File(destDir, "rtdose");
                rtDoseDir.mkdirs();
                writeFile(new File(rtDoseDir, file.getName()), al, basicDemographics);
            }
            else {
                writeFile(new File(destDir, file.getName()), al, basicDemographics);
            }
        }
    }

    private void process(File directory) throws IOException, DicomException {
        readFiles(directory);

        ArrayList<String> ctUidList = getCtUids();

        /*
         * while (ctList.size() > ctUidList.size()) {
         * System.out.println("Removing entry from ctList");
         * ctList.remove(ctList.size()-1);
         * }
         */
        System.out.println("Directory: " + directory.getName());
        System.out.println("   rtstructFileList: " + rtstructFileList.size());
        System.out.println("   ctUidList:        " + ctUidList.size());
        System.out.println("   ctList:           " + ctList.size());
        writeAllFiles(directory, ctUidList);

        System.out.println("    Number of CTs: " + ctUidList.size());
    }

    private FixCt(File directory) throws IOException, DicomException {
        System.out.println("Processing directory: " + directory.getAbsolutePath());
        process(directory);
    }

    public static void main(String[] args) {
        try {
            for (String dirName : args) {
                if (args == null) {
                    for (File file : new File(dirName).listFiles()) {
                        System.out.println("    " + file.getName());
                    }
                    System.exit(99);
                }

                new FixCt(new File(dirName));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
