package edu.umro.dicom.client;

import javax.swing.JSlider;
import com.sun.java.swing.plaf.windows.WindowsSliderUI;

/**
 * Override the standard Windows slider by incrementing
 * a slider by one instead of roughly 10% of the range.
 * 
 * This give the user finer control over positioning sliders.
 * 
 * @author irrer
 *
 */
@SuppressWarnings("restriction")
public class WinSliderUI extends WindowsSliderUI {

    /**
     * Use the same constructor.
     * 
     * @param jSlider Slider.
     */
    public WinSliderUI(JSlider jSlider) {
        super(jSlider);
    }

    /**
     * Increment the slider by 1 when the user clicks on
     * either side of the marker.
     */
    public void scrollByBlock(int direction)    {
        synchronized(slider) {

            int oldValue = slider.getValue();
            int blockIncrement = 1;
            int delta = blockIncrement * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);
            slider.setValue(oldValue + delta);          
        }
    }
}