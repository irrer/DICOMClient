package edu.umro.dicom.client;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;

public class EditCreate extends Edit {

    Attribute newAttribute = null;

    @Override
    public void doEdit(AttributeList attributeList) {
        AttributeList al = attributeLocation.getAttributeList(attributeList);
        al.put(newAttribute);
    }

    public EditCreate(AttributeLocation attributeLocation, Attribute attribute) {
        super(attributeLocation);
        newAttribute = attribute;
    }

    @Override
    public String description() {
        
        try {
            StringBuffer text = new StringBuffer();
            for (String value : newAttribute.getStringValues()) text.append((text.length() == 0) ? value : (" \\ " + value));
            String desc = "Create " + CustomDictionary.getName(attributeLocation) + " of " + text.toString();
            if (desc.length() > MAX_DESCRIPTION_LENGTH) desc = desc.substring(0, MAX_DESCRIPTION_LENGTH) + " ...";
            return desc;
        }
        catch (Exception e) {
            return "Created value";
        }
    }
}

