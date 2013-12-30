package edu.umro.dicom.client;

/*
 * Copyright 2013 Regents of the University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;

import edu.umro.util.Log;

public class AttributeLocation {

    private class AttributeParent {
        AttributeTag parent = null;
        int index = -1;

        public AttributeParent(AttributeTag parent, int index) {
            this.parent = parent;
            this.index = index;
        }
    }

    public Attribute attribute = null;
    private int sequenceItemIndex = -1;
    private boolean located = false;
    private int startOfText = -1;
    private int endOfText = -1;

    private int textPosition = Integer.MAX_VALUE;

    private ArrayList<AttributeParent> ancestry = new ArrayList<AttributeParent>();

    public AttributeLocation(int textPosition) {
        this.textPosition = textPosition;
    }

    public AttributeLocation(AttributeList attributeList) {
        textPosition = 0;
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
        ancestry.add(new AttributeParent(attribute.getTag(), index));
    }

    public void removeParent() {
        if (ancestry.size() > 0) ancestry.remove(ancestry.size() - 1);
    }

    public SequenceAttribute getParentAttribute(AttributeList attributeList) {
        SequenceAttribute sa = null;
        for (AttributeParent p : ancestry) {
            sa = (SequenceAttribute) attributeList.get(p.parent);
            attributeList = sa.getItem(p.index).getAttributeList();
        }
        return sa;
    }

    public AttributeTag getParentTag() {
        return ancestry.get(ancestry.size() - 1).parent;
    }

    public int getParentIndex() {
        return ancestry.get(ancestry.size() - 1).index;
    }

    public AttributeList getGrandParentAttributeList(AttributeList attributeList) {
        switch (ancestry.size()) {
        case 0: {
            String msg = "Should not be getting grandparent of attribute at top level.";
            Log.get().severe(msg);
            throw new RuntimeException(msg);
        }

        case 1:
            return attributeList;

        default:
            SequenceAttribute sa = null;
            AttributeList grandParent = null;
            for (AttributeParent p : ancestry) {
                grandParent = attributeList;
                sa = (SequenceAttribute) attributeList.get(p.parent);
                attributeList = sa.getItem(p.index).getAttributeList();
            }
            return grandParent;
        }
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
            SequenceAttribute seqAt = (SequenceAttribute) (al.get(ap.parent));
            SequenceItem si = seqAt.getItem(ap.index);
            al = si.getAttributeList();
        }
        return al;
    }

    public String toString() {
        StringBuffer text = new StringBuffer();

        String indent = "";
        for (AttributeParent ap : ancestry) {
            text.append(indent + CustomDictionary.getInstance().getNameFromTag(ap.parent) + " : " + ap.index + "\n");
            indent += "  ";
        }
        text.append(indent);
        if (attribute == null)
            text.append("Index: " + (sequenceItemIndex + 1));
        else
            text.append(indent + CustomDictionary.getName(attribute) + "\n");
        return text.toString();
    }
}
