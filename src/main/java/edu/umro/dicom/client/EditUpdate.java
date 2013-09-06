package edu.umro.dicom.client;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;

class EditUpdate extends Edit {

    Attribute newAttribute = null;

    @Override
    public void doEdit(AttributeList attributeList) {
    }

    public EditUpdate(AttributeLocation attributeLocation) {
        super(attributeLocation);
    }

    @Override
    public String description() {
        try {
            return "Change value of " + CustomDictionary.getInstance().getFullNameFromTag(attributeLocation.attribute.getTag()) + " to " + newAttribute.getStringValues();
        }
        catch (Exception e) {
            return "Changed value";
        }
    }
}