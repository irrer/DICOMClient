package edu.umro.dicom.client;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

public class DirectoryChooser extends JDialog {   // TODO remove class?

    /** Default ID */
    private static final long serialVersionUID = 1L;

    /** Preferred size of directory chooser screen. */
    private static final Dimension PREFERRED_SIZE = new Dimension(640, 440);


    /** Controls selection of directory. */
    private JFileChooser directoryChooser = null;

    public DirectoryChooser(ActionListener actionListener) {
        super(DicomClient.getInstance(), true);

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
