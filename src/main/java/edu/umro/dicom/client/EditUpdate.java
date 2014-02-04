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

class EditUpdate extends Edit {

    private Attribute newAttribute = null;

    @Override
    public void doEdit(AttributeList attributeList) {
        AttributeList al = attributeLocation.getAttributeList(attributeList);
        al.put(Util.cloneAttribute(newAttribute));
    }

    public EditUpdate(AttributeLocation attributeLocation, Attribute attribute) {
        super(attributeLocation);
        newAttribute = Util.cloneAttribute(attribute);
    }

    @Override
    public String description() {
        try {
            StringBuffer text = new StringBuffer();
            for (String value : newAttribute.getStringValues()) text.append((text.length() == 0) ? value : (" \\ " + value));
            String desc = "Update " + CustomDictionary.getName(attributeLocation) + " to " + text.toString();
            if (desc.length() > MAX_DESCRIPTION_LENGTH) desc = desc.substring(0, MAX_DESCRIPTION_LENGTH) + " ...";
            return desc;
        }
        catch (Exception e) {
            return "Updated value";
        }
    }
}
