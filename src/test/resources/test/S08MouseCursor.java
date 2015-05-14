//Code from: http://www.orbital-computer.de/JComboBox/
/*
 Inside JComboBox: adding automatic completion

 Author: Thomas Bierhance
 thomas@orbital-computer.de
 */
/*
 Highlight complete text when the user selects an item via mouse

 When the user selects an item via mouse or using the cursor keys, the text is not highlighted. 
 When the user select an item directly, the insertString method is called once with the 
 complete string. Inside this method only completed text is highlighted which in this case is 
 none, as the call already contained the complete string.

 A solution is to highlight the complete text whenever an item gets selected and this 
 selection was not initiated by the autocompletion mechanism. This can be achieved using an 
 ActionListener...
 */
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class S08MouseCursor extends PlainDocument {
    private static final long serialVersionUID = 1L;
    JComboBox<String> comboBox;
    ComboBoxModel<String>  model;
    JTextComponent editor;
    // flag to indicate if setSelectedItem has been called
    // subsequent calls to remove/insertString should be ignored
    boolean selecting = false;

    public S08MouseCursor(final JComboBox<String>  comboBox) {
        this.comboBox = comboBox;
        model = comboBox.getModel();
        editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!selecting) highlightCompletedText(0);
            }
        });
    }

    public void remove(int offs, int len) throws BadLocationException {
        // return immediately when selecting an item
        if (selecting) return;
        super.remove(offs, len);
    }

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        // return immediately when selecting an item
        if (selecting) return;
        // insert the string into the document
        super.insertString(offs, str, a);
        // lookup and select a matching item
        Object item = lookupItem(getText(0, getLength()));
        if (item != null) {
            setSelectedItem(item);
        }
        else {
            // keep old item selected if there is no match
            item = comboBox.getSelectedItem();
            // imitate no insert (later on offs will be incremented by
            // str.length(): selection won't move forward)
            offs = offs - str.length();
            // provide feedback to the user that his input has been received but
            // can not be accepted
            comboBox.getToolkit().beep(); // when available use:
                                          // UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
        }

        setText(item.toString());

        // select the completed part
        highlightCompletedText(offs + str.length());
    }

    private void setText(String text) throws BadLocationException {
        // remove all text and insert the completed string
        super.remove(0, getLength());
        super.insertString(0, text, null);
    }

    private void highlightCompletedText(int start) {
        editor.setSelectionStart(start);
        editor.setSelectionEnd(getLength());
    }

    private void setSelectedItem(Object item) {
        selecting = true;
        model.setSelectedItem(item);
        selecting = false;
    }

    private Object lookupItem(String pattern) {
        Object selectedItem = model.getSelectedItem();
        // only search for a different item if the currently selected does not
        // match
        if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern)) {
            return selectedItem;
        }
        else {
            // iterate over all items
            for (int i = 0, n = model.getSize(); i < n; i++) {
                Object currentItem = model.getElementAt(i);
                // current item starts with the pattern?
                if (startsWithIgnoreCase(currentItem.toString(), pattern)) {
                    return currentItem;
                }
            }
        }
        // no item starts with the pattern => return null
        return null;
    }

    // checks if str1 starts with str2 - ignores case
    private boolean startsWithIgnoreCase(String str1, String str2) {
        return str1.toUpperCase().startsWith(str2.toUpperCase());
    }

    private static void createAndShowGUI() {
        // the combo box (add/modify items if you like to)
        @SuppressWarnings({ "unchecked", "rawtypes" })
        JComboBox<String> comboBox = (JComboBox<String>)(new JComboBox(new Object[] { "Ester", "Jordi", "Jordina", "Jorge", "Sergi" }));
        // has to be editable
        comboBox.setEditable(true);
        // get the combo boxes editor component
        JTextComponent editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        // change the editor's document
        editor.setDocument(new S08MouseCursor(comboBox));

        // create and show a window containing the combo box
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(3);
        frame.getContentPane().add(comboBox);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
