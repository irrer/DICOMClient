package edu.umro.dicom.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import com.pixelmed.dicom.AttributeTag;

public class TagChooser1 extends JComboBox<String> {

    /** Default */
    private static final long serialVersionUID = 1L;

    public TreeSet<String> nameList = new TreeSet<String>();

    private static class Doc extends PlainDocument implements DocumentListener {
        TagChooser1 tagChooser;
        ComboBoxModel<String> model;
        JTextComponent editor;

        private static final long serialVersionUID = 1L;

        Doc(final TagChooser1 comboBox) {
            this.tagChooser = comboBox;
            model = comboBox.getModel();
            editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
            comboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    textChange();
                }
            });

            this.addDocumentListener(this);

        }

        private void textChange() {
            try {
                String text = getText(0, getLength());
                System.out.println("text: " + text);
                String[] terms = text.toLowerCase().split(" ");
                tagChooser.removeAllItems();
                for (String name : tagChooser.nameList) {
                    boolean ok = true;
                    for (String t : terms) {
                        if (!name.toLowerCase().contains(t)) {
                            ok = false;
                            break;                       
                        }
                    }
                    if (ok) tagChooser.addItem(name);
                }
            }
            catch (Exception e) {
                System.out.println("bad: " + e);
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            textChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            textChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            textChange();
        }
    }

    private void setChoiceList() {

        for (String name : nameList) {
            addItem(name);
        }
    }

    public TagChooser1() {
        setEditable(true);
        CustomDictionary cd = CustomDictionary.getInstance();
        @SuppressWarnings("unchecked")
        Iterator<AttributeTag> ti = cd.getTagIterator();

        // Put all names in a tree set so they come out sorted alphabetically.
        while (ti.hasNext()) {
            String name = cd.getNameFromTag((AttributeTag) (ti.next()));
            nameList.add(name);
        }
        setChoiceList();
    }

    private static void createAndShowGUI() {
        // the combo box (add/modify items if you like to)
        TagChooser1 comboBox = new TagChooser1();
        // has to be editable
        comboBox.setEditable(true);
        // get the combo boxes editor component
        JTextComponent editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        // change the editor's document
        editor.setDocument(new TagChooser1.Doc(comboBox));

        // create and show a window containing the combo box
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(3);
        frame.getContentPane().add(comboBox);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

}
