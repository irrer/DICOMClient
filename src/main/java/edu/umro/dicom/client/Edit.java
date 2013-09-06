package edu.umro.dicom.client;

import com.pixelmed.dicom.AttributeList;

public abstract class Edit {

    AttributeLocation attributeLocation = null;

    public abstract void doEdit(AttributeList attributeList);

    public abstract String description();

    public Edit(AttributeLocation attributeLocation) {
        this.attributeLocation = attributeLocation;
    }
}
