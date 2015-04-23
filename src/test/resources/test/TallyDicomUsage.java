
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;


public class TallyDicomUsage {

    private final static String DLA = "=>";
    private final static String DLB = "<=";
    private static final int DICOM_METADATA_LENGTH = 4 * 1024;


    private static FileOutputStream out = null;

    private static DicomDictionary dicomDictionary = new DicomDictionary();


    private static String getVal(AttributeList al, AttributeTag tag) {
        Attribute at = al.get(tag);
        String value = "none";
        if (at != null) value = at.getSingleStringValueOrDefault(value);
        String name = dicomDictionary.getNameFromTag(tag);
        return name + DLA + value + DLB + " ";
    }

    /**
     * Read at a minimum the first portion of the given DICOM file.  The
     * 'portion' is defined to be long enough to get the basic meta-data.
     * 
     * @param fileName
     * 
     * @return The contents of the file
     * @throws IOException 
     * @throws DicomException 
     */
    private static AttributeList readDicomFile(File file) {
        try {
            AttributeList attributeList = new AttributeList(); 
            {
                Attribute a = AttributeFactory.newAttribute(TagFromName.PatientID);
                a.addValue(100);
                a.addValue(200);
                attributeList.put(a);
            }

            // The following is faster than <code>attributeList.read(fileName);</code>, as it only reads the first part of every DICOM file, but
            // it also produces a lot of error messages because of the 'ragged end' of each file.
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[DICOM_METADATA_LENGTH];
            DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(buffer, 0, fis.read(buffer)));
            attributeList.read(dis);
            fis.close();
            return attributeList;
        }
        catch  (Exception e) {;}
        
        return null;
    }

    private static void tallyDicom(File file) {
        try {
            AttributeList al = readDicomFile(file);
            if (al == null) return;

            String text = 
                    getVal(al, TagFromName.InstanceCreationDate) +
                    getVal(al, TagFromName.SeriesDate) +
                    getVal(al, TagFromName.StudyDate) +
                    getVal(al, TagFromName.Manufacturer) +
                    getVal(al, TagFromName.SoftwareVersions) +
                    getVal(al, TagFromName.Modality) +
                    getVal(al, TagFromName.ManufacturerModelName) +
                    getVal(al, TagFromName.InstitutionName) +
                    " FileName" + DLA + file.getAbsolutePath() + DLB +
                    " FileLength" + DLA + file.length() + DLB + "\n";

            out.write(text.getBytes());
        }
        catch (Exception e){;}
    }

    private static void count(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File f : children) {
                count(f);
            }
        }
        else {
            tallyDicom(file);
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        try {
            String logFileName = "D:\\tmp\\tally" + ((new Date()).toString().replaceAll("[^a-zA-Z0-9]", "_") ) + ".txt";
            File logFile = new File(logFileName);
            logFile.delete();
            logFile.createNewFile();
            out = new FileOutputStream(logFile);
            for (String a : args) count(new File(a));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            out.close();
        }
    }

}
