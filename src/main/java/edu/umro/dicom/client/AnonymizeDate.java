package edu.umro.dicom.client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

import javax.swing.BorderFactory;

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

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.umro.util.Log;

/**
 * Handle anonymization and GUI for dates.
 * 
 * @author Jim Irrer irrer@umich.edu
 *
 */

public class AnonymizeDate implements ActionListener, DocumentListener, MouseListener {

    private ButtonGroup buttonGroup = new ButtonGroup();

    private JTextField anonTextField = null;
    private JTextField shiftTextField = null;

    enum DateMode {
        None("None", "Leave dates as they are except for those individually tagged for anonymization."), //
        Year("Year", "Remove the month and day from each date, leaving only the year."), //
        Anon("Anonymize", "Set dates to a specific value.  Example: 19560124 for Jan 24, 1956."), //
        Shift("Shift", "Shift dates forward (positive) or back (negative) by the given number of days.");//

        /** Name displayed to the user. */
        final String display;

        /** Tool tip to give user more information. */
        final String toolTip;

        JRadioButton radioButton;

        JPanel panel;

        DateMode(String disp, String tooltp) {
            this.display = disp;
            this.toolTip = tooltp;
        }
    }

    private JPanel buildAnon() {
        JPanel panel = DateMode.Anon.panel;
        anonTextField = new JTextField(6);
        anonTextField.setText("18000101");
        anonTextField.getDocument().addDocumentListener(this);
        anonTextField.addMouseListener(this);
        panel.add(anonTextField);
        return panel;
    }

    private JPanel buildShift() {
        JPanel panel = DateMode.Shift.panel;
        shiftTextField = new JTextField(6);
        shiftTextField.setText("0");
        shiftTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        shiftTextField.getDocument().addDocumentListener(this);
        shiftTextField.addMouseListener(this);
        panel.add(shiftTextField);
        return panel;
    }

    private void initDateMode(DateMode dm) {
        System.out.println("dm.display: " + dm.display); // TODO rm
        dm.radioButton = new JRadioButton(dm.display.toString());
        dm.panel = new JPanel();
        dm.panel.setToolTipText(dm.toolTip);
        dm.radioButton.setToolTipText(dm.toolTip);
        dm.panel.add(dm.radioButton);
        dm.radioButton.addActionListener(this);
        buttonGroup.add(dm.radioButton);
    }

    private JPanel buildGUI() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Date Mode: "));

        DateMode[] valueList = DateMode.values();

        for (DateMode dm : valueList) {
            initDateMode(dm);
        }

        setMode(DateMode.None);

        int space = 20;
        DateMode.None.panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, space));
        DateMode.Year.panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, space));
        DateMode.Anon.panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, space));

        panel.add(DateMode.None.panel);
        panel.add(DateMode.Year.panel);
        panel.add(buildAnon());
        panel.add(buildShift());

        shiftTextField.setToolTipText(DateMode.Shift.toolTip);
        anonTextField.setToolTipText(DateMode.Anon.toolTip);

        DateMode.None.radioButton.setSelected(true);

        panel.setToolTipText("Type of anonymization for dates.");
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return panel;
    }

    private JPanel mainPanel = null;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private DateMode dateMode = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        for (DateMode m : DateMode.values()) {
            if (m.radioButton.isSelected()) {
                dateMode = m;
            }
        }

        DicomClient.getInstance().updatePreviewIfAppropriate();
    }

    private Date anonValue = null;

    public Date getAnonValue() {
        return anonValue;
    }

    public void setAnonValue(Date anon) {
        anonValue = anon;
        DicomClient.getInstance().updatePreviewIfAppropriate();
    }

    /**
     * The number of days to shift dates that are not otherwise anonymized.
     */
    private Integer shiftValue = 0;

    public int getShiftValue() {
        if (shiftValue == null) {
            return 0;
        }
        else {
            return shiftValue;
        }
    }

    public void setShiftValue(Integer dsv) {
        shiftValue = dsv;
        DicomClient.getInstance().updatePreviewIfAppropriate();
    }

    private Integer getDateShiftValueFromGUI() {
        Integer shift = null;
        try {
            shift = Integer.parseInt(shiftTextField.getText().trim());
            shiftTextField.setBackground(Color.WHITE);
        }
        catch (Exception e) {
            shift = null;
        }
        return shift;
    }

    private void updateShiftText() {
        Color color = Color.WHITE;
        Integer newShiftValue = getDateShiftValueFromGUI();
        if (newShiftValue == null) {
            color = Color.YELLOW;
            newShiftValue = 0;
        }

        shiftTextField.setBackground(color);

        if (newShiftValue != shiftValue) {
            shiftValue = newShiftValue;
            DicomClient.getInstance().updatePreviewIfAppropriate();
        }
    }

    private void updateAnonText() {
        Date date = null;
        try {
            date = Util.dateFormat.parse(anonTextField.getText());
            if ((anonValue == null) || ((anonValue.getTime() != date.getTime()))) {
                anonValue = date;
                anonTextField.setBackground(Color.WHITE);
                DicomClient.getInstance().updatePreviewIfAppropriate();
            }
        }
        catch (Exception e) {
            anonTextField.setBackground(Color.YELLOW);
            anonValue = null;
        }

        DicomClient.getInstance().updatePreviewIfAppropriate();

    }

    private void textChanged(DocumentEvent e) {
        if (e.getDocument().equals(anonTextField.getDocument())) {
            updateAnonText();
        }

        if (e.getDocument().equals(shiftTextField.getDocument())) {
            updateShiftText();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        textChanged(e);
    }

    private void selectByMouse(MouseEvent e) {
        if (e.getSource() == shiftTextField) {
            DateMode.Shift.radioButton.setSelected(true);
            dateMode = DateMode.Shift;
            DicomClient.getInstance().updatePreviewIfAppropriate();
        }
        if (e.getSource() == anonTextField) {
            DateMode.Anon.radioButton.setSelected(true);
            dateMode = DateMode.Anon;
            DicomClient.getInstance().updatePreviewIfAppropriate();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        selectByMouse(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        selectByMouse(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        selectByMouse(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        ;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        ;
    }

    public DateMode getMode() {
        return dateMode;
    }

    public void setMode(DateMode dm) {
        dateMode = dm;
        DicomClient.getInstance().updatePreviewIfAppropriate();
    }

    private void initAnonValue() {
        try {
            anonValue = Util.dateFormat.parse("18000101");
        }
        catch (Exception e) {
            Log.get().severe("Unexpected date parse problem: " + Log.fmtEx(e));
        }
    }

    private AnonymizeDate() {
        mainPanel = buildGUI();
        initAnonValue();
    }

    private static AnonymizeDate instance = null;

    public static AnonymizeDate getInstance() {
        if (instance == null) {
            instance = new AnonymizeDate();
        }
        return instance;
    }

}
