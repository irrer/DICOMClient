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

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.SequenceAttribute;

class EditDelete extends Edit {
    @Override
    public void doEdit(AttributeList attributeList) {
        AttributeList al = attributeLocation.getAttributeList(attributeList);
        if (attributeLocation.attribute == null) {
            SequenceAttribute parent = attributeLocation.getParentAttribute(attributeList);
            int parentIndex = attributeLocation.getParentIndex();
            SequenceAttribute newParent = new SequenceAttribute(parent.getTag());
            for (int i = 0; i < parent.getNumberOfItems(); i++) {
                if (i != parentIndex) {
                    newParent.addItem(parent.getItem(i));
                }
            }
            attributeLocation.getGrandParentAttributeList(attributeList).put(newParent);
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
            return "Delete " + CustomDictionary.getName(attributeLocation);
        }
    }
}