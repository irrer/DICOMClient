package edu.umro.dicom.client;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;

public class EditCreate extends Edit {

    Attribute newAttribute = null;

    @Override
    public void doEdit(AttributeList attributeList) {
    }

    public EditCreate(AttributeLocation attributeLocation) {
        super(attributeLocation);
    }

    @Override
    public String description() {
        return "Created attribute " + CustomDictionary.getInstance().getNameFromTag(newAttribute.getTag());
    }
}

