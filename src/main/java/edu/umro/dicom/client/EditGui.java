package edu.umro.dicom.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;

public class EditGui implements ActionListener {

    /** Preferred size of screen. */
    private static final Dimension PREFERRED_SIZE = new Dimension(600, 700);

    private static final String CARD_BUTTON = "card";
    private static final String CARD_CREATE = "create";
    private static final String CARD_UPDATE = "update";

    private LinkedList<Edit> editHistory = new LinkedList<Edit>();

    private JDialog dialog = null;
    private Container mainContainer = null;
    private Preview preview = null;

    /** Panel that switches back and forth between image and text viewing modes. */
    private JPanel cardPanel = null;

    private JButton cancelButton = null;
    private JButton saveButton = null;
    private JButton undoButton = null;
    private JButton redoButton = null;
    private JCheckBox applyToAllSlices = null;

    private JButton createButton = null;
    private JButton updateButton = null;
    private JButton deleteButton = null;

    private JButton createCancelButton = null;
    private JButton updateCancelButton = null;

    private JButton createApplyButton = null;
    private JButton updateApplyButton = null;

    /** Last attribute to be selected for editing. */
    private AttributeLocation attributeLocation = null;

    /**
     * Layout that switches back and forth between image and text viewing modes.
     */
    private CardLayout cardLayout = null;

    private JComponent buildButtonPanelCenter() {
        JPanel boxPanel = new JPanel();
        BoxLayout layout = new BoxLayout(boxPanel, BoxLayout.Y_AXIS);
        // FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        boxPanel.setLayout(layout);

        createButton = new JButton("Create");

        updateButton = new JButton("Update");

        deleteButton = new JButton("Delete");

        int gapLarge = 40;
        int gapSmall = 20;
        Border borderLarge = BorderFactory.createEmptyBorder(gapLarge, gapLarge, gapLarge, gapLarge);
        Border borderSmall = BorderFactory.createEmptyBorder(gapSmall, gapSmall*2, gapSmall, gapSmall*2);
        JButton[] list = { createButton, updateButton, deleteButton };
        for (JButton button : list) {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout(FlowLayout.CENTER));
            button.addActionListener(this);
            button.setBorder(borderSmall);
            button.setFont(DicomClient.FONT_LARGE);
            p.setBorder(borderLarge);
            p.add(button);
            boxPanel.add(p);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(boxPanel);
        panel.setBorder(borderLarge);
        return panel;
    }

    private JComponent buildButtonPanelSouth() {
        JPanel panel = new JPanel();
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        layout.setHgap(30);
        layout.setVgap(30);
        panel.setLayout(layout);

        applyToAllSlices = new JCheckBox("All");
        applyToAllSlices.addActionListener(this);
        applyToAllSlices.setToolTipText("<html>Apply changes to all<br>slices in this series.<br>If not checked, only<br>change the current slice.</html>");
        panel.add(applyToAllSlices);

        undoButton = new JButton("Undo");
        undoButton.addActionListener(this);
        panel.add(undoButton);

        redoButton = new JButton("Redo");
        redoButton.addActionListener(this);
        panel.add(redoButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setToolTipText("<html>Discard all edits without<br>saving anything.</html>");
        panel.add(cancelButton);

        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        saveButton.setToolTipText("<html>Write new version<br>of file(s).</html>");
        panel.add(saveButton);

        return panel;
    }

    private JComponent buildButtonPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(buildButtonPanelCenter(), BorderLayout.CENTER);
        panel.add(buildButtonPanelSouth(), BorderLayout.SOUTH);

        return panel;
    }

    private JComponent buildCreatePanel() {
        JPanel panel = new JPanel();
        return panel;
    }

    private JComponent buildUpdatePanel() {
        JPanel panel = new JPanel();
        return panel;
    }

    private JComponent buildCenter() {
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        cardPanel.add(buildButtonPanel(), CARD_BUTTON);
        cardPanel.add(buildCreatePanel(), CARD_CREATE);
        cardPanel.add(buildUpdatePanel(), CARD_UPDATE);

        return cardPanel;
    }

    private JComponent buildSouth() {
        JPanel panel = new JPanel();

        return panel;
    }

    private void setTitle() {
        String title = "Editing " + preview.getPreviewedSeries().getDescription();
        dialog.setTitle(title);
    }

    public EditGui(Preview preview) {
        this.preview = preview;
        mainContainer = new Container();
        if (!DicomClient.inCommandLineMode()) {
            dialog = new JDialog(DicomClient.getInstance().getFrame(), false);
            setTitle();
        }
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(buildCenter(), BorderLayout.CENTER);
        panel.add(buildSouth(), BorderLayout.SOUTH);

        DicomClient.setColor(panel);

        if (DicomClient.inCommandLineMode()) {
            mainContainer.add(panel);
        }
        else {
            dialog.setPreferredSize(PREFERRED_SIZE);
            dialog.getContentPane().add(panel);
            dialog.pack();
            dialog.setVisible(true);
        }
        setAttributeLocation(null);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource().equals(createButton)) cardLayout.show(cardPanel, CARD_CREATE);
        if (ev.getSource().equals(updateButton)) cardLayout.show(cardPanel, CARD_UPDATE);

        if (ev.getSource().equals(createCancelButton) || ev.getSource().equals(updateCancelButton)) cardLayout.show(cardPanel, CARD_BUTTON);

        if (ev.getSource().equals(deleteButton)) delete();
    }

    private void delete() {
        if (attributeLocation != null) editHistory.add(new EditDelete(attributeLocation));
        cardLayout.show(cardPanel, CARD_BUTTON);
        setAttributeLocation(null);
        preview.showDicom();
    }

    public void performEdits(AttributeList attributeList) {
        for (Edit edit : editHistory) {
            edit.doEdit(attributeList);
        }
    }

    public void setAttributeLocation(AttributeLocation attributeLocation) {
        this.attributeLocation = attributeLocation;
        deleteButton.setEnabled(attributeLocation != null);
        updateButton.setEnabled(attributeLocation != null);
    }

}
