package edu.umro.dicom.client;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;

class EditUpdate extends Edit {

    private Attribute newAttribute = null;

    @Override
    public void doEdit(AttributeList attributeList) {
        AttributeList al = attributeLocation.getAttributeList(attributeList);
        al.put(newAttribute);
    }

    public EditUpdate(AttributeLocation attributeLocation, Attribute attribute) {
        super(attributeLocation);
        newAttribute = attribute;
    }

    @Override
    public String description() {
        try {
            StringBuffer text = new StringBuffer();
            for (String value : newAttribute.getStringValues()) text.append((text.length() == 0) ? value : (" \\ " + value));
            return "Update " + CustomDictionary.getInstance().getNameFromTag(attributeLocation.attribute.getTag()) + " to " + text.toString();
        }
        catch (Exception e) {
            return "Changed value";
        }
    }
}