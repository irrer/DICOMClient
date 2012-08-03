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
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

/**
 * A slightly customized version of a file chooser.
 * 
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
public class DirectoryChooser extends JDialog {   // TODO remove class?

    /** Default ID */
    private static final long serialVersionUID = 1L;

    /** Preferred size of directory chooser screen. */
    private static final Dimension PREFERRED_SIZE = new Dimension(640, 440);


    /** Controls selection of directory. */
    private JFileChooser directoryChooser = null;

    public DirectoryChooser(ActionListener actionListener) {
        super(DicomClient.getInstance().getFrame(), true);

        directoryChooser = new JFileChooser();

        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        directoryChooser.addActionListener(actionListener);

        getContentPane().add(directoryChooser);

        setPreferredSize(PREFERRED_SIZE);
        pack();
    }


    public JFileChooser getFileChooser() {
        return directoryChooser;
    }


    public void setDefaultDirectory(File file) {
        if (directoryChooser.getSelectedFile() == null) {
            directoryChooser.setSelectedFile(file);
        }
    }
}
