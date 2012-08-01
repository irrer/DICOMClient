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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;

/**
 * Support a scalable label.
 * 
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
public class ScalableJLabel extends JLabel {
    /** Default id */
    private static final long serialVersionUID = 1L;

    /** Amount to scale by. */
    private float scaleFactor;

    /** Image to be scaled */
    private BufferedImage bufferedImage;

    /** Construct with default values. */
    public ScalableJLabel() {
        this.scaleFactor = 1;
        this.setSize(0, 0);
    }

    /**
     * Set the image.
     * 
     * @param image Image to use
     */
    public void setImage(BufferedImage image) {
        this.bufferedImage = image;
        this.setSize(image.getWidth(), image.getHeight());
    }

    /**
     * Set the scale factor.
     * 
     * @param scaleFactor Amount to shrink or grow.
     */
    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        if (this.bufferedImage != null) {
            int width = Math.round((bufferedImage.getWidth() * scaleFactor));
            int height = Math.round(bufferedImage.getHeight() * scaleFactor);
            this.setPreferredSize(new Dimension(width, height));
            this.revalidate();
            ((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            // Tried both
            //     RenderingHints.VALUE_INTERPOLATION_BILINEAR 
            //     RenderingHints.VALUE_INTERPOLATION_BICUBIC
            // and they were really slow.
            ((Graphics2D)graphics).drawImage(bufferedImage, 0, 0, width, height, null);
        }
    }

}
