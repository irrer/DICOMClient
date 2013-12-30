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

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;

import edu.umro.util.Log;

public class EditCopy extends Edit {

    Attribute newAttribute = null;

    
    /**
     * Construct a copy the selected item and put it into the sequence.  This is
     * complicated by the fact that sequence items can not be inserted into an
     * existing list, which means that the list must be copied and as the copy is
     * being done, the new item inserted.  Also, when attributes are copied, they
     * must be cloned, otherwise subsequent modifications will be duplicated in
     * multiple items.
     */
    @Override
    public void doEdit(AttributeList attributeList) {
        try {
            SequenceAttribute parentSeqAttr = attributeLocation.getParentAttribute(attributeList);
            // only copy the minimum needed
            AttributeList src = new AttributeList();
            src.put(parentSeqAttr);
            int index = attributeLocation.getParentIndex();
            AttributeList copiedList = Util.cloneAttributeList(src);
            SequenceAttribute copiedSeqAttr = (SequenceAttribute)copiedList.get(parentSeqAttr.getTag());
            SequenceAttribute finalSeqAttr = new SequenceAttribute(parentSeqAttr.getTag());
            
            for (int i = 0; i < copiedSeqAttr.getNumberOfItems(); i++) {
                finalSeqAttr.addItem(copiedSeqAttr.getItem(i));
                if (i == index) {
                    SequenceItem si = copiedSeqAttr.getItem(i);
                    AttributeList al = Util.cloneAttributeList(si.getAttributeList());
                    finalSeqAttr.addItem(new SequenceItem(al));
                }
            }

            attributeLocation.getGrandParentAttributeList(attributeList).put(finalSeqAttr);
        }
        catch (Exception e) {
            Log.get().warning("Unable to copy attribute list: " + e);
        }
    }

    public EditCopy(AttributeLocation attributeLocation) {
        super(attributeLocation);
    }

    @Override
    public String description() {
        
        try {
            String name = CustomDictionary.getInstance().getNameFromTag(attributeLocation.getParentTag());
            int index = attributeLocation.getParentIndex();
            String desc = "Copy item " + (index+1) + " of " + name;
            return desc;
        }
        catch (Exception e) {
            return "Copied sequence";
        }
    }
}

