package edu.umro.dicom.client;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.ValueRepresentation;

public class EditCreate extends Edit {

    Attribute newAttribute = null;

    private boolean isSeq() {
        return ValueRepresentation.isSequenceVR(CustomDictionary.getInstance().getValueRepresentationFromTag(newAttribute.getTag()));
    }

    @Override
    public void doEdit(AttributeList attributeList) {
        AttributeList al = attributeLocation.getAttributeList(attributeList);
        if (ValueRepresentation.isSequenceVR(CustomDictionary.getInstance().getValueRepresentationFromTag(newAttribute.getTag()))) {
            ((SequenceAttribute)newAttribute).getItem(0).getAttributeList().clear();
        }
        al.put(newAttribute);
    }

    public EditCreate(AttributeLocation attributeLocation, Attribute attribute) {
        super(attributeLocation);
        newAttribute = attribute;
    }

    @Override
    public String description() {

        try {
            if (isSeq()) {
                String desc = "Create " + CustomDictionary.getName(newAttribute);
                return desc;
            }
            else {
                StringBuffer text = new StringBuffer();
                for (String value : newAttribute.getStringValues())
                    text.append((text.length() == 0) ? value : (" \\ " + value));
                String desc = "Create " + CustomDictionary.getName(newAttribute) + " of " + text.toString();
                if (desc.length() > MAX_DESCRIPTION_LENGTH) desc = desc.substring(0, MAX_DESCRIPTION_LENGTH) + " ...";
                return desc;
            }
        }
        catch (Exception e) {
            return "Created value";
        }
    }
}
