package edu.umro.dicom.client;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;


/**
 * Limit the length of a document by over-riding the
 * insert method.
 * 
 * @author irrer
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
