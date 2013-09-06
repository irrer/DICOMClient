package edu.umro.dicom.client;

import com.pixelmed.dicom.AttributeList;

class EditDelete extends Edit {
    @Override
    public void doEdit(AttributeList attributeList) {
        AttributeList al = attributeLocation.getAttributeList(attributeList);
        al.remove(attributeLocation.attribute.getTag());
        if ((al.size() == 0) && (al != attributeList)) {
            // TODO remove item
        }
    }

    public EditDelete(AttributeLocation attributeLocation) {
        super(attributeLocation);
    }

    @Override
    public String description() {
        return "Delete attribute " + CustomDictionary.getInstance().getNameFromTag(attributeLocation.attribute.getTag());
    }
}