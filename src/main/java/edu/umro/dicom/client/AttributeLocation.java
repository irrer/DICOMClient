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
        SequenceAttribute parent = null;
        int index = -1;

        public AttributeParent(SequenceAttribute parent, int index) {
            this.parent = parent;
            this.index = index;
        }
    }

    private Series series = null;

    private File file = null;

    public Attribute attribute = null;
    private int sequenceItemIndex = -1;
    private boolean located = false;
    private AttributeList attributeList = null;
    private int startOfText = -1;
    private int endOfText = -1;

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
        return located;
    }

    public int getStartOfText() {
        return startOfText;
    }

    public int getEndOfText() {
        return endOfText;
    }

    public int getSequenceItemIndex() {
        return sequenceItemIndex;
    }

    public void addParent(SequenceAttribute attribute, int index) {
        ancestry.add(new AttributeParent(attribute, index));
    }

    public void removeParent() {
        if (ancestry.size() > 0) ancestry.remove(ancestry.size() - 1);
    }
    
    public SequenceAttribute getParentAttribute() {
        return ancestry.get(ancestry.size()-1).parent;
    }
    
    public AttributeList getGrandParentAttributeList() {
        if (ancestry.size() > 1) {
            AttributeParent gp = ancestry.get(ancestry.size() - 2);
            int index = ancestry.get(ancestry.size() - 1).index;
            return gp.parent.getItem(index).getAttributeList();
        }
        else return attributeList;
    }

    public boolean setAttribute(int length, int sequenceItemIndex, Attribute attribute, int startOfText, int endOfText) {
        if ((!isLocated()) && (length > textPosition)) {
            this.attribute = attribute;
            this.sequenceItemIndex = sequenceItemIndex;
            this.startOfText = startOfText;
            this.endOfText = endOfText;
            located = true;
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
        text.append(indent);
        if (attribute == null) text.append("Index: " + (sequenceItemIndex + 1));
        else text.append(indent + CustomDictionary.getInstance().getNameFromTag(attribute.getTag()) + "\n");
        return text.toString();
    }
}
