package edu.umro.dicom.client;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.SequenceAttribute;

class EditDelete extends Edit {
    @Override
    public void doEdit(AttributeList attributeList) {
        AttributeList al = attributeLocation.getAttributeList(attributeList);
        if (attributeLocation.attribute == null) {
            SequenceAttribute parent = attributeLocation.getParentAttribute();
            SequenceAttribute newParent = new SequenceAttribute(parent.getTag());
            for (int i = 0; i < parent.getNumberOfItems(); i++) {
                if (i != attributeLocation.getSequenceItemIndex()) {
                    newParent.addItem(parent.getItem(i));
                }
            }
            attributeLocation.getGrandParentAttributeList().put(newParent);
        }
        else {
            al.remove(attributeLocation.attribute.getTag());
        }
    }

    public EditDelete(AttributeLocation attributeLocation) {
        super(attributeLocation);
    }

    @Override
    public String description() {
        if (attributeLocation.getAttribute() == null) {
            return "Delete item " + (attributeLocation.getSequenceItemIndex() + 1);
        }
        else {
            return "Delete attribute " + CustomDictionary.getInstance().getNameFromTag(attributeLocation.attribute.getTag());
        }
    }
}