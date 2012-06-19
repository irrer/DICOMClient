package edu.umro.dicom.client;

import java.awt.Dimension;

import edu.umro.dicom.common.Util;

/**
 * Show a dialog that gives the user basic instructions on
 * how to use the application.  An HTML based JLabel is used
 * to display the text.
 * 
 * @author irrer
 *
 */
public class Help extends Alert {

    /** Default id. */
    private static final long serialVersionUID = 1L;

    /** Default id. */
    private static final String WINDOW_TITLE = "DICOM Utility Help";

    /** Preferred size of screen. */
    private static final Dimension PREFERRED_SIZE = new Dimension(440, 550);

    /** Describes how to use this utility. */
    private static final String ABOUT_TEXT =
        "<html>\n" +
        "<h2>DICOM Anonymize and Upload</h2>\n" +
        "<br>Build Date: " + Util.getBuildDate() + "&nbsp;&nbsp;&nbsp; Version: " + Util.getImplementationVersion() + "<br>\n" +
        "<br>\n" +
        "University of Michigan Department of Radiation Oncology<br>\n" +
        "<br>\n" +
        "Written by Jim Irrer: irrer@umich.edu<br>\n" +
        "</html>\n";


    /** Help text. */
    private static final String HELP_TEXT =
        "<html>\n" +
        "<!--\n" +
        "Help text built into application.\n" +
        "This text is written in a subset of HTML that is \n" +
        "understood by the Java Swing JLabel class.  It\n" +
        "is edited processed by running the <code>Help</code> class\n" +
        "and then the output pasted into the Help class, and then\n" +
        "the code is compiled.\n" +
        "-->\n" +
        "\n" +
        "<h2>DICOM Utility</h2>\n" +
        "<br>\n" +
        "<h3>Introduction</h3>\n" +
        "<br>\n" +
        "The primary purpose of the DICOM Anonymize/Upload tool is<br>\n" +
        "to anonymize DICOM files locally or cop from local storage<br>\n" +
        "on the desktop and put them into the PACS servers.  The tool<br>\n" +
        "also has a simple previewer, which can be used to view DICOM<br>\n" +
        "files even if the user has no intention of anonymizing or<br>\n" +
        "uploading them.<br>\n" +
        "<br>\n" +
        "DICOM files can reside on any storage that the local system<br>\n" +
        "recognizes, including hard disk, CD, DVD, USB, or thumb drives.<br>\n" +
        "<br>\n" +
        "\n" +
        "<h3>Starting the Program</h3>\n" +
        "The program may be started by either:\n" +
        "<br>\n" +
        "<ul>\n" +
        "  <li>dragging a DICOM file or directory onto the program icon</li>\n" +
        "  <li>double clicking the icon</li>\n" +
        "</ul>\n" +
        "<br>\n" +
        "<br>\n" +
        "The program has two modes (Anonymize and Upload) indicated by radio<br>\n" +
        "buttons near the top of the screen.  Choose the one that is appropriate<br>\n" +
        "for your needs.  If you only wish to view (preview) files, then either<br>\n" +
        "mode will suffice.\n" +
        "<br>\n" +
        "When a file is dragged to the icon, all DICOM files in the<br>\n" +
        "containing directory are loaded into the program.<br>\n" +
        "<br>\n" +
        "When a directory is dragged to the icon, all DICOM files within<br>\n" +
        "that directory are loaded into the program.<br>\n" +
        "<br>\n" +
        "Double clicking the icon will also start the program, but with no<br>\n" +
        "files loaded.<br>\n" +
        "<br>\n" +
        "After the program has been started, the user may drag additional<br>\n" +
        "files into it and they will be added to the list.<br>\n" +
        "<br>\n" +
        "Files are listed in a hierarchy, which has three levels:<br>\n" +
        "<ul>\n" +
        "  <li>Patient</li>\n" +
        "  <ul>\n" +
        "    <li>Study</li>\n" +
        "    <ul>\n" +
        "      <li>Series</li>\n" +
        "    </ul>\n" +
        "  </ul>\n" +
        "</ul>\n" +
        "with descriptive information for each.  Individual files for each<br>\n" +
        "series are not listed, but the number of files in each is shown.<br>\n" +
        "<br>\n" +
        "For each series, there is a <b>Preview</b> and an <b>Anonymize</b> or an<br>\n" +
        "<b>Upload</b> button.  The <b>Preview</b> is used to preview the series<br>\n" +
        "before anonymizing or uploading.\n" +
        "<br>\n" +
        "Any files that are not valid DICOM files, or, the<br>\n" +
        "program lacks the necessary permissions to read them, will be<br>\n" +
        "noted in the message field (lower left corner) and then ignored.<br>\n" +
        "\n" +
        "\n" +
        "<br>\n" +
        "<h3>Anonymizing Files</h3>\n" +
        "<br>\n" +
        "Anonymizing creates copies of existing DICOM files with<br>\n" +
        "selected fields anonymized.  New files are created, as<br>\n" +
        "opposed to over-writing files, and the new files are named<br>\n" +
        "with the new patient ID, modality, series number, and<br>\n" +
        "instance (slice) number.  If necessary, an additional number<br>\n" +
        "is added.  An example file name is:<br>\n" +
        "<br>\n" +
        "&nbsp; &nbsp; &nbsp; &nbsp; ANON3968_CT_2_0021.DCM<br>\n" +
        "<br>\n" +
        "To anonymize a series, select the <b>Anonymize Mode</b><br>\n" +
        "radio button near the top center of the screen, and then<br>\n" +
        "click the <b>Anonymize</b> button next to the desired<br>\n" +
        "series.  The user may perform anonymization on all series<br>\n" +
        "under a given patient by clicking the <b>Anonymize<br>\n" +
        "Patient</b> button, or may anonymize all series under all<br> \n" +
        "patients by clicking the <b>Anonymize All</b> button near the<br> \n" +
        "bottom right-hand corner of the window.<br>\n" +
        "<br>\n" +
        "After an anonymization is complete, a green indicator will<br>\n" +
        "be shown as confirmation.<br>\n" +
        "<br>\n" +
        "When Anonymize mode is selected with the radio button,<br>\n" +
        "the destination and <b>Anonymize Options</b> button will<br>\n" +
        "appear underneath, and all the <b>Upload</b> buttons will<br>\n" +
        "change to <b>Anonymize</b>.  Each patient will show<br>\n" +
        "fields indicating the new patient id and name.<br>\n" +
        "<br>\n" +
        "By default, the destination is set to be a sub-directory<br>\n" +
        "named <b>anonymize</b> underneath the first directory of<br>\n" +
        "files loaded.  This may be changed by entering a new<br> \n" +
        "value or use the <b>Browse...</b> button to select a<br>\n" +
        "directory.  If the directory chosen does not exist,<br>\n" +
        "it will be created.<br>\n" +
        "<br>\n" +
        "Clicking the <b>Anonymize Options</b> will display the<br> \n" +
        "<b>Anonymize Options</b> window, which allows the user to<br>\n" +
        "customize the anonymization.<br>\n" +
        "<br>\n" +
        "For each patient, a default patient ID is randomly chosen.<br>\n" +
        "This may be changed if desired.  By default, the patient<br>\n" +
        "name will be kept the same as the patient ID.  If the user<br>\n" +
        "wants the patient name to be different from the patient ID,<br> \n" +
        "then click the checkbox to the right of the <b>New Patient<br>\n" +
        "Name</b> field and set the name as desired.<br>\n" +
        "<br>\n" +
        "By default, the list of fields is set up to reflect those<br>\n" +
        "recommended by the DICOM standard.  The standard indicates<br>\n" +
        "that this list may in some cases be insufficient.  For<br> \n" +
        "greater control over anonymization, use the <b>Anonymize<br>\n" +
        "Options...</b> button to display the the <b>Anonymize<br>\n" +
        "Options</b> window.  This window shows the fields that<br>\n" +
        "will be anonymized and the new values to be used, except<br>\n" +
        "for UID values which due to their complexity are handled<br>\n" +
        "automatically.  In general, users will not need to modify<br>\n" +
        "this list of values, but if so, then:<br>\n" +
        "<br>\n" +
        "<ul><br>\n" +
        "  <li>New fields may be added with the <b>Add</b> button<br>\n" +
        "        at the bottom of the window.<br>\n" +
        "  <li>Fields may be de-activated by un-checking their<br>\n" +
        "        corresponding checkboxes.<br>\n" +
        "  <li>Alternate values for fields may be chosen with<br>\n" +
        "        the text boxes.<br>\n" +
        "</ul><br>\n" +
        "<br>\n" +
        "Note that existing files are never over-written, so<br>\n" +
        "performing anonymization twice will create two sets of<br>\n" +
        "files, even using the same source files.<br>\n" +
        "<br>\n" +
        "New UIDs (such as SOPInstanceUID, SeriesInstanceUID, and<br> \n" +
        "StudyInstanceUID) are automatically generated and all old<br> \n" +
        "UIDs are discarded.  It is not possible to override this<br>\n" +
        "feature.<br>\n" +
        "<br>\n" +
        "When anonymizing multiple series that reference each other<br>\n" +
        "via their UIDs, it is important to anonymize them all in the<br> \n" +
        "same session (invocation of the tool) so that the UIDs in<br>\n" +
        "the anonymized files will be consistent across the different<br>\n" +
        "series.<br>\n" +
        "<br>\n" +
        "Anonymized files may be re-loaded into the program to<br>\n" +
        "inspect the changes with the previewer.<br>\n" +
        "<br>\n" +
        "It is not necessary to log in to anonymize files.<br>\n" +
        "<br>\n" +
        "\n" +
        "\n" +
        "\n" +
        "\n" +
        "<h3>To Upload DICOM Files</h3>\n" +
        "<br>\n" +
        "Before uploading files, for security purposes, you must log in.<br>\n" +
        "Use your ARIA user account name and password.  After logging in,<br>\n" +
        "the login area will be replaced by a PACS selector.  Select the<br>\n" +
        "desired destination PACS  by using the spinner (up and down arrows)<br>\n" +
        "near the top of the screen until the the desired PACS is shown.<br>\n" +
        "<br>\n" +
        "To upload all of the series for all patients, use the <b>Upload All</b><br>\n" +
        "button.<br>\n" +
        "<br>\n" +
        "While a series is being uploaded, its buttons are replaced<br>\n" +
        "with a progress bar.  The display reverts back to the buttons<br>\n" +
        "when the upload is complete.<br>\n" +
        "<br>\n" +
        "When an upload of a complete series is successful, a green circle<br>\n" +
        "with a white plus sign is shown next to its upload button.  If all<br>\n" +
        "series have been uploaded, then an additional green icon is shown<br>\n" +
        "next to the <b>Upload All</b> button.  These indicators are specific to<br>\n" +
        "the PACS that files have been uploaded to.  In other words, if a<br>\n" +
        "series is indicated to have been uploaded, it is only for the PACS<br>\n" +
        "shown.  Switching to a different PACS will change the indicator<br>\n" +
        "to reflect the upload status for that PACS.  The upload state is<br>\n" +
        "reset when the program is exited, so if the program is restarted<br>\n" +
        "with the same DICOM files, they will be shown as not having been<br>\n" +
        "uploaded.<br>\n" +
        "<br>\n" +
        "<h3>Previewing Files</h3>\n" +
        "<br>\n" +
        "To preview DICOM files, drag the slider for the corresponding<br>\n" +
        "series and the preview window will be shown.  The preview window<br>\n" +
        "can show files as either images or text.  There is a pair of<br>\n" +
        "radio buttons at the bottom to select the mode.<br>\n" +
        "<br>\n" +
        "The title of the preview window shows basic information that<br>\n" +
        "identifies the series, and near the top of the window it<br>\n" +
        "displays the file from which the data originated.<br>\n" +
        "<br>\n" +
        "In image mode, the three sliders can be used to control the<br>\n" +
        "contrast, brightness and size of the image.  Between the sliders<br>\n" +
        "is a <b>Reset</b> button that resets the sliders to their<br>\n" +
        "initial position, which shows the image in an un-altered<br>\n" +
        "state.<br>\n" +
        "<br>\n" +
        "In text mode, there is search field where the user can type<br>\n" +
        "a string of text to search for.  Searches are case-insensitive,<br>\n" +
        "and the currently selected matching string and the total<br>\n" +
        "number of matching strings are indicated as <b>current of<br>\n" +
        "total</b>.\n" +
        "<br>\n" +
        "Searches are literal (as opposed to wild card or<br>\n" +
        "regular expressions), do not span lines, and can be used to<br>\n" +
        "search the names of fields as well their as values.  The<br>\n" +
        "down and up arrows next to the search field can be used to<br>\n" +
        "navigate to the previous and next matches.<br>\n" +
        "<br>\n" +
        "The user can also select text and use conventional copy and<br>\n" +
        "paste to copy it to another application.<br>\n" +
        "<br>\n" +
        "The text can not be modified in place.<br>\n" +
        "<br>\n" +
        "In text mode there are also two shortcuts.  After clicking<br>\n" +
        "in the text area:<br>\n" +
        "<br>\n" +
        "<ul>\n" +
        "<li>Enter will position to the next matching text string.</li>\n" +
        "<li>Selecting text and then typing Ctrl-F will search for that text.</li>\n" +
        "</ul>\n" +
        "<br>\n" +
        "Pressing <b>Enter</b> from the search field will scroll the<br>\n" +
        "text window to show the next match.  After the last match is<br>\n" +
        "scrolled to, the window will scroll back to the first match.<br>\n" +
        "<br>\n" +
        "<h3>Additional Notes</h3>\n" +
        "<br>\n" +
        "In general, uploading the same series multiple times has no real<br>\n" +
        "effect, as the copy on the PACS is over written with the new copy. <br>\n" +
        "<br>\n" +
        "Once a file has been uploaded it can not be un-uploaded.<br>\n" +
        "<br>\n" +
        "This program does not modify the files on local storage.<br>\n" +
        "<br>\n" +
        "</html>\n" +
        "";


    public Help() {
        super(HELP_TEXT, WINDOW_TITLE, new String[] { "About", "Close" }, PREFERRED_SIZE);
        if (this == null) {  // TODO remove - for development only
            for (int i = 0; i < 4; i++) {
                cont.invalidate();
                cont.repaint();
                cont.setPreferredSize(PREFERRED_SIZE);
                cont.setSize(PREFERRED_SIZE);
                setPreferredSize(PREFERRED_SIZE);
                setSize(PREFERRED_SIZE);
                pack();
                invalidate();
                repaint();
                setResizable(true);
            }
        }
        switch (getSelectedButtonIndex()) {
        case 0:
            new Alert(ABOUT_TEXT, "About DICOM Utility");
            break;
        default:
            break;
        }

    }
}
