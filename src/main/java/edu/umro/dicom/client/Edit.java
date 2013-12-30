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
