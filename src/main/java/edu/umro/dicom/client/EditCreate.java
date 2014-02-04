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
        al.put(Util.cloneAttribute(newAttribute));
    }

    public EditCreate(AttributeLocation attributeLocation, Attribute attribute) {
        super(attributeLocation);
        newAttribute = Util.cloneAttribute(attribute);

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
