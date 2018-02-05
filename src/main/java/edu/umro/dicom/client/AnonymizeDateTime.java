package edu.umro.dicom.client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

public class AnonymizeDateTime implements ActionListener, DocumentListener, MouseListener {

    public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
    public static final SimpleDateFormat dateTimeHumanFormat = new SimpleDateFormat("d MMM yyyy  HH:mm:ss");
    static {
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        dateTimeHumanFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    private static final Long msPerSecond = new Long(1000);
    private static final Long msPerMinute = msPerSecond * 60;
    private static final Long msPerHour = msPerMinute * 60;
    private static final long msPerDay = 24 * msPerHour;

    /** Maximum date-time allowed as text. */
    public static final String maxDateText = "99991231.235959";
    /** Minimum date-time allowed as text. */
    public static final String minDateText = "00010101.000000";

    /**
     * Parse the given text with the given format and trust that nothing will go wrong.
     * 
     * @param format
     * @param text
     * @return
     */
    private static Date trustedParse(SimpleDateFormat format, String text) {
        Date date = null;
        try {
            date = format.parse(text);
        }
        catch (Exception e) {
            Log.get().severe("trustedParse failed to parse " + text);
        }
        return date;
    }

    /** Maximum date-time allowed. */
    public static final Date maxDate = trustedParse(dateTimeFormat, maxDateText);
    /** Minimum date-time allowed. */
    public static final Date minDate = trustedParse(dateTimeFormat, minDateText);

    private ButtonGroup buttonGroup = new ButtonGroup();

    private JTextField anonTextField = null;
    private JTextField shiftTextField = null;
    private JLabel anonLabel = null;
    private JLabel shiftLabel = null;

    enum DateMode {
        None("None", "Leave dates and times as they are except for those individually tagged for anonymization."), //
        Year("Year", "Remove the month and day from each date, leaving only the year.  Do not change times."), //
        Anon("Anonymize", "Set dates and time to a specific value.  Example: 19560124.095400 for Jan 24, 1956 09:54:00."), //
        Shift("Shift", "Shift dates forward (positive) or back (negative) by the given number of days and time.");//

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

    private static String formatAnonValueToHuman(Date anonVal) {
        return dateTimeHumanFormat.format(anonVal);
    }

    private JPanel buildAnon() {
        JPanel panel = DateMode.Anon.panel;
        anonTextField = new JTextField(13);
        anonTextField.setText(dateTimeFormat.format(anonValue));
        panel.add(anonTextField);
        anonLabel = new JLabel(formatAnonValueToHuman(anonValue));
        panel.add(anonLabel);
        anonTextField.getDocument().addDocumentListener(this);
        anonTextField.addMouseListener(this);
        return panel;
    }

    public static String formatShiftValue(Long shftVal) {
        long sv = (shftVal >= 0) ? shftVal : -shftVal;

        Long days = sv / msPerDay;
        sv = sv - (days * msPerDay);

        Long hours = sv / msPerHour;
        sv = sv - (hours * msPerHour);

        Long minutes = sv / msPerMinute;
        sv = sv - (minutes * msPerMinute);

        Long seconds = sv / msPerSecond;

        String text = String.format("%d.%02d%02d%02d", days, hours, minutes, seconds);
        text = (shftVal >= 0) ? text : "-" + text;
        return text;
    }

    private static String formatShiftValueToHuman() {
        long sv = (shiftValue >= 0) ? shiftValue : -shiftValue;
        String[] daysTime = formatShiftValue(sv).split("\\.");

        String text = daysTime[0] + " days " + daysTime[1].substring(0, 2) + ":" + daysTime[1].substring(2, 4) + ":" + daysTime[1].substring(4, 6);

        text = (shiftValue >= 0) ? text : "-" + text;
        return text;
    }

    private JPanel buildShift() {
        JPanel panel = DateMode.Shift.panel;
        shiftTextField = new JTextField(13);
        shiftTextField.setText(formatShiftValue(shiftValue));
        shiftTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(shiftTextField);
        shiftLabel = new JLabel(formatShiftValueToHuman());
        panel.add(shiftLabel);
        shiftTextField.getDocument().addDocumentListener(this);
        shiftTextField.addMouseListener(this);
        return panel;
    }

    private void initDateMode(DateMode dm) {
        dm.radioButton = new JRadioButton(dm.display.toString());
        dm.panel = new JPanel();
        dm.panel.setToolTipText(dm.toolTip);
        dm.radioButton.setToolTipText(dm.toolTip);
        dm.panel.add(dm.radioButton);
        dm.radioButton.addActionListener(this);
        buttonGroup.add(dm.radioButton);
        dm.radioButton.setSelected(false);
    }

    private JPanel buildGUI() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Date Mode: "));

        DateMode[] valueList = DateMode.values();

        for (DateMode dm : valueList) {
            initDateMode(dm);
        }

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

        dateMode.radioButton.setSelected(true);

        panel.setToolTipText("Type of anonymization for dates and times.");
        return panel;
    }

    private JPanel mainPanel = null;

    public JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = buildGUI();
        }
        return mainPanel;
    }

    private static DateMode dateMode = DateMode.None;

    @Override
    public void actionPerformed(ActionEvent e) {
        for (DateMode m : DateMode.values()) {
            if (m.radioButton.isSelected()) {
                dateMode = m;
            }
        }

        DicomClient.getInstance().updatePreviewIfAppropriate();
    }

    static Long parseTime(String text) throws ParseException {
        text = (text + "000000").substring(0, 6);
        // If the time format is not valid, then throw an exception.

        Long hour = Long.parseLong(text.substring(0, 2));
        Long minute = Long.parseLong(text.substring(2, 4));
        Long second = Long.parseLong(text.substring(4, 6));

        if ((hour < 0) || (hour > 23) || (minute < 0) || (minute > 59) || (second < 0) || (second > 59)) throw new ParseException("Invalid Time", 0);

        long time = (hour * msPerHour) + (minute * msPerMinute) + (second * msPerSecond);
        return time;
    }

    /**
     * Check to see if the shift text is valid. If valid, return the ms to shift. Leading and trailing whitespace is
     * ignored.
     * 
     * @param text
     *            Text to convert.
     * 
     * @return Long (if valid), null if not.
     */
    public static Long parseShift(String text) {
        try {
            String[] parts = text.trim().split("\\.");
            switch (parts.length) {
            case 1: {
                return Long.parseLong(parts[0]) * msPerDay;
            }
            case 2: {
                boolean negative = parts[0].startsWith("-");
                if (negative) {
                    parts[0] = parts[0].substring(1);
                }

                parts[1] = (parts[1] + "000000").substring(0, 6);
                long shift = (Long.parseLong(parts[0]) * msPerDay) + parseTime(parts[1]);

                if (negative) {
                    shift = -shift;
                }

                return new Long(shift);
            }
            default: // text must be either NNN or NNN.NNN
                return null;
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    private void updateShiftText() {
        DateMode.Shift.radioButton.setSelected(true);
        String text = shiftTextField.getText().trim();
        Long newShiftValue = parseShift(text);

        boolean ok = (newShiftValue != null) && (text.equalsIgnoreCase(formatShiftValue(newShiftValue)));

        Color color = (ok) ? Color.WHITE : Color.YELLOW;
        newShiftValue = (ok) ? newShiftValue : new Long(0);
        shiftTextField.setBackground(color);

        if (newShiftValue != shiftValue) {
            shiftValue = newShiftValue;
            shiftLabel.setText(formatShiftValueToHuman());
            DicomClient.getInstance().updatePreviewIfAppropriate();
        }
    }

    /**
     * Parse the given text that represents a date-time string. If a date-only is given, then the time portion is
     * assumed to be zero. If date is out of bounds (must be from year 0001 to 9999) then return the closest bound.
     * 
     * <p/>
     * Part of the checking is to 'round trip' the value by converting it back to text and require that
     * it matches the original.
     * 
     * @param dateTimeText
     *            Date and time as text, as in 19562401.153021
     * 
     * @return Date if valid, null if not.
     */
    public static Date parseAnon(String dateTimeText) {
        try {
            // try to put the value into the standard format
            String text = dateTimeText.trim();
            if (!text.contains(".")) text = text + ".000000";
            text = (text + "000000").substring(0, 15);

            Date date = dateTimeFormat.parse(text);

            // Convert date back to text and compare to be sure it is correct. This
            // fixes problems like saying February 31st.
            if ((date == null) || (!text.equalsIgnoreCase(dateTimeFormat.format(date)))) {
                return null;
            }
            else {
                if (date.getTime() > maxDate.getTime()) date = maxDate;
                if (date.getTime() < minDate.getTime()) date = minDate;
                return date;
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    private void updateAnonText() {
        try {
            DateMode.Anon.radioButton.setSelected(true);

            Date date = parseAnon(anonTextField.getText());

            if (date == null) {
                anonTextField.setBackground(Color.YELLOW);
                initAnonValue();
            }
            else {
                anonValue = date;
                anonTextField.setBackground(Color.WHITE);
            }
        }
        catch (Exception e) {
            anonTextField.setBackground(Color.YELLOW);
            initAnonValue();
        }

        anonLabel.setText(formatAnonValueToHuman(anonValue));
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

    public static DateMode getMode() {
        return dateMode;
    }

    public static void setMode(DateMode dm) {
        dateMode = dm;
    }

    private static Date anonValue = null;

    public static Date getAnonValue() {
        return anonValue;
    }

    public static void setAnonValue(Date anon) {
        anonValue = anon;
    }

    /**
     * The dates and times to shift dates that are not otherwise anonymized.
     */
    private static Long shiftValue = new Long(0);

    public static Long getShiftValue() {
        return shiftValue;
    }

    public static void setShiftValue(Long sv) {
        shiftValue = sv;
    }

    private static void initAnonValue() {
        try {
            anonValue = Util.dateFormat.parse("18000101");
        }
        catch (Exception e) {
            Log.get().severe("Unexpected date parse problem: " + Log.fmtEx(e));
        }
    }

    static {
        initAnonValue();
    }

    private AnonymizeDateTime() {
    }

    private static AnonymizeDateTime instance = null;

    public static AnonymizeDateTime getInstance() {
        if (instance == null) {
            instance = new AnonymizeDateTime();
        }
        return instance;
    }

}
