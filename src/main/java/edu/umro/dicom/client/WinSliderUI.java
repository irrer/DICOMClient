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

import javax.swing.JSlider;
import com.sun.java.swing.plaf.windows.WindowsSliderUI;

/**
 * Override the standard Windows slider by incrementing
 * a slider by one instead of roughly 10% of the range.
 * 
 * This give the user finer control over positioning sliders.
 * 
 * @author Jim Irrer  irrer@umich.edu 
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