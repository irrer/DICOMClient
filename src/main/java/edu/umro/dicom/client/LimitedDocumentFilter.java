package edu.umro.dicom.client;

/*
 * Copyright 2012 Regents of the University of Michigan
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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;


/**
 * Limit the length of a document by over-riding the
 * insert method.
 * 
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
public class LimitedDocumentFilter extends DocumentFilter {

    /** Maximum allowed length of document. */
    private int maxLength = Integer.MAX_VALUE;

    public LimitedDocumentFilter(int maxLength) {
        super();
        this.maxLength = (maxLength > 0) ? maxLength : this.maxLength;
    }


    /**
     * If the resulting length of the insert will be less than or equal
     * to the allowed length, then allow it, otherwise ignore it.
     */
    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        int length = fb.getDocument().getLength();
        if ((string.length() + length) <= maxLength) {
            super.insertString(fb, offset, string, attr);
        }
    }


    /**
     * If the resulting length of the insert will be less than or equal
     * to the allowed length, then allow it, otherwise ignore it.
     */
    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if ((fb.getDocument().getLength() + text.length() - length) <= maxLength) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}
