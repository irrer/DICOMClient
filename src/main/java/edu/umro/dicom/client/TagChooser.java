package edu.umro.dicom.client;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import javax.naming.directory.SearchControls;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import com.pixelmed.dicom.AttributeTag;

public class TagChooser extends JPanel implements DocumentListener, KeyListener {

    /** default */
    private static final long serialVersionUID = 1L;

    private JTextField searchField = null;
    JComboBox<String> comboBox = new JComboBox<String>(new String[] { "Ester", "James", "Jordi", "Jordina", "Jim", "Jorge", "Sergi" });
    public TreeSet<String> nameList = null;

    private void update() {
        String text = searchField.getText();
        System.out.println("text: " + text);
        String[] terms = text.toLowerCase().split(" ");

        comboBox.removeAllItems();

        ArrayList<String> nl = new ArrayList<String>(1000);

        for (String name : nameList) {
            boolean ok = true;
            for (String t : terms) {
                if (!name.toLowerCase().contains(t)) {
                    ok = false;
                    break;
                }
            }
            if (ok) nl.add(name);
        }

        HashSet<String> alreadyOnList = new HashSet<String>();
        if (terms.length > 0) {
            for (String name : nl) {
                if (name.toLowerCase().startsWith(terms[0])) {
                    comboBox.addItem(name);
                    alreadyOnList.add(name);
                }
            }
        }

        for (String name : nl) {
            if (!alreadyOnList.contains(name)) comboBox.addItem(name);
        }
        comboBox.setPopupVisible(true);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        update();
    }

    private void initNameList() {
        nameList = new TreeSet<String>();
        CustomDictionary cd = CustomDictionary.getInstance();
        @SuppressWarnings("unchecked")
        Iterator<AttributeTag> ti = cd.getTagIterator();

        // Put all names in a tree set so they come out sorted alphabetically.
        while (ti.hasNext()) {
            String name = cd.getNameFromTag((AttributeTag) (ti.next()));
            nameList.add(name);
        }

    }

    public TagChooser() {
        FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER);
        setLayout(flowLayout);
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(this);
        searchField.addKeyListener(this);
        add(searchField);
        add(comboBox);
        initNameList();

        for (String name : nameList)
            comboBox.addItem(name);
    }

    private static void createAndShowGUI() {

        // create and show a window containing the combo box
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(3);
        frame.getContentPane().add(new TagChooser());
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

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("keyTyped");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("keyPressed: " + (e.getKeyCode()));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println("keyReleased");
    }

}
