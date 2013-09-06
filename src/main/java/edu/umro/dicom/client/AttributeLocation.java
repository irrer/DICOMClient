package edu.umro.dicom.client;

import java.io.File;
import java.util.ArrayList;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;

import edu.umro.util.Log;

public class AttributeLocation {

    private class AttributeParent {
        Attribute parent = null;
        int index = -1;

        public AttributeParent(Attribute parent, int index) {
            this.parent = parent;
            this.index = index;
        }
    }

    private Series series = null;

    private File file = null;

    public Attribute attribute = null;
    private AttributeList attributeList = null;

    private int textPosition = Integer.MAX_VALUE;

    private ArrayList<AttributeParent> ancestry = new ArrayList<AttributeParent>();

    public AttributeLocation(Series series, File file, AttributeList attributeList, int textPosition) {
        this.series = series;
        this.file = file;
        this.attributeList = attributeList;
        this.textPosition = textPosition;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public boolean isLocated() {
        return this.attribute != null;
    }

    public void addParent(Attribute attribute, int index) {
        ancestry.add(new AttributeParent(attribute, index));
    }

    public void removeParent() {
        if (ancestry.size() > 0) ancestry.remove(ancestry.size() - 1);
    }

    public boolean setAttribute(int length, Attribute attribute) {
        if ((!isLocated()) && (length > textPosition)) {
            this.attribute = attribute;
        }
        return isLocated();
    }
    
    public AttributeList getAttributeList(AttributeList main) {
        AttributeList al = main;
        for (AttributeParent ap : ancestry) {
            SequenceAttribute seqAt = (SequenceAttribute)(al.get(ap.parent.getTag()));
            SequenceItem si = seqAt.getItem(ap.index);
            al = si.getAttributeList();
        }
        return al;
    }

    public String toString() {
        StringBuffer text = new StringBuffer();

        String indent = "";
        for (AttributeParent ap : ancestry) {
            text.append(indent + CustomDictionary.getInstance().getNameFromTag(ap.parent.getTag()) + " : " + ap.index + "\n");
            indent += "  ";
        }
        text.append(indent + CustomDictionary.getInstance().getNameFromTag(attribute.getTag()) + "\n");
        return text.toString();
    }
}
