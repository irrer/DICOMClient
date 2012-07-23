package edu.umro.dicom.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.umro.util.Utility;


/**
 * Show a dialog that gives the user basic instructions on
 * how to use the application.  An HTML based JLabel is used
 * to display the text.
 * 
 * @author irrer
 *
 */
public class Alert extends JDialog implements ActionListener {

    /** Default id. */
    private static final long serialVersionUID = 1L;

    /** Preferred size of screen. */
    private static final Dimension PREFERRED_SIZE = new Dimension(440, 300);

    /** List of buttons */
    private JButton[] buttonList = null;
    
    /** Default list of buttons to use if no list is given. */
    private static String[] DEFAULT_BUTTON_NAME_LIST = { "Close" };

    /** The index in the array of buttons of the button that was clicked. */
    int selectedButton = -1;

    protected JPanel cont = null;  // TODO remove

    /**
     * Build the center of the dialog.
     * 
     * @return The component that shows the center of the GUI.
     */
    private JComponent buildCenter(String msg) {
        JPanel panel = new JPanel();
        cont = panel;
        JScrollPane scrollPane = new JScrollPane(new JLabel(msg));
        int gap = 20;
        scrollPane.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));

        panel.add(scrollPane);

        return scrollPane;
    }


    /**
     * Build the southern (lower) part of the GUI.
     * 
     * @param buttonNameList Names of buttons to appear at the bottom of the window.
     * 
     * @return Component for the southern part of the GUI.
     */
    private JComponent buildSouth(String[] buttonNameList) {
        JPanel panel = new JPanel();
        int gap = 20;
        panel.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));

        buttonList = new JButton[buttonNameList.length];
        int b = 0;
        for (String buttonName : buttonNameList) {
            buttonList[b] = new JButton(buttonName);
            buttonList[b].addActionListener(this);
            panel.add(buttonList[b]);
            b++;
        }

        return panel;
    }


    @Override
    public void actionPerformed(ActionEvent ev) {
        Object source = ev.getSource();
        int b = 0;
        for (JButton button : buttonList) {
            if (source == button) {
                selectedButton = b;
                setVisible(false);
            }
            b++;
        }
    }


    /**
     * Return the index of the button that the user clicked.
     *
     * @return Index of button in original list of buttons.
     */
    public int getSelectedButtonIndex() {
        return selectedButton;
    }


    /**
     * Build the GUI and display it.
     */
    public Alert(String msg, String title, String[] buttonNameList, Dimension preferredSize) {
        super(DicomClient.getInstance().getFrame(), true);
        
        buttonNameList = (buttonNameList == null) ? DEFAULT_BUTTON_NAME_LIST : buttonNameList;
        
        setTitle(title);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(buildCenter(msg), BorderLayout.CENTER);
        panel.add(buildSouth(buttonNameList), BorderLayout.SOUTH);

        DicomClient.setColor(panel);

        setPreferredSize(preferredSize);
        getContentPane().add(panel);
        pack();
        setVisible(!DicomClient.inCommandLineMode());
    }


    /**
     * Show a message window that the user is required to respond to with
     * the single default button.
     * 
     * @param msg Message to show.
     * 
     * @param title Window title.
     */
    public Alert(String msg, String title) {
        this(msg, title, DEFAULT_BUTTON_NAME_LIST, PREFERRED_SIZE);
    }


    /**
     * Show a message window that the user is required to respond to.
     * 
     * @param msg Message to show.
     * 
     * @param title Window title.
     * 
     * @param buttonName Single button to be shown.  If null, use the default button.
     */
    public Alert(String msg, String title, String buttonName) {
        this(msg, title, (buttonName == null) ? DEFAULT_BUTTON_NAME_LIST : new String[] { buttonName }, PREFERRED_SIZE);
    }


    /**
     * Generate the help text from also source file.  This could also
     * be done with an editor.  This would only be done when developing,
     * and, when the help text has changed.
     * 
     * @param args List of file names.  If empty, use a default file.
     * 
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if ((args.length) == 0) {
            args = new String[] { "src/main/resources/DICOMClientHelp.html" };
        }
        for (String fileName : args) {
            System.out.println("\n\n\n");
            String text = Utility.readFile(new File(fileName));
            String[] lineList = text.split("\r*\n");
            for (String line : lineList) {
                System.out.println("\"" + line + "<br>\\n\" +");
            }
            System.out.println("\"\";");
        }
    }

}
