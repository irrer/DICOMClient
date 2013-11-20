package edu.umro.dicom.client;

import com.pixelmed.dicom.AttributeList;

/**
 * An edit operation.
 * 
 * @author irrer
 *
 */

public abstract class Edit {
    protected static final int MAX_DESCRIPTION_LENGTH = 80;

    AttributeLocation attributeLocation = null;

    public abstract void doEdit(AttributeList attributeList);

    public abstract String description();

    public Edit(AttributeLocation attributeLocation) {
        this.attributeLocation = attributeLocation;
    }
}
