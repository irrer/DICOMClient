package edu.umro.dicom.client;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomDictionary;

public class CustomAttributeList extends AttributeList {
    /** Default ID */
    private static final long serialVersionUID = 1L;

    public static void setDictionary(DicomDictionary dicomDictionary) {
        CustomAttributeList.dictionary = dicomDictionary;
    }

}
