package edu.umro.dicom.client;

import java.io.File;
import java.io.FileInputStream;

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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DateTimeAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.UnknownAttribute;
import com.pixelmed.dicom.ValueRepresentation;

import edu.umro.util.Log;
import edu.umro.util.Utility;
import edu.umro.util.XML;

/**
 * Represent a patient ID - UID combination
 * for anonymization. The pairs are used to
 * facilitate the proper re-using of UIDs
 * necessary to consistent anonymization of
 * UIDs.
 * 
 * @author Jim Irrer irrer@umich.edu
 *
 */
class Uid {

    /** Anonymized patient ID. */
    private String anonymizedPatientId;

    /** Anonymized UID. */
    private String uid;

    /** Original patient ID, or null if none. */
    private String originalPatientId;

    public String getOriginalPatientId() {
        return originalPatientId;
    }

    public Uid(String anonymizedPatientId, String uid, String originalPatientId) {
        this.anonymizedPatientId = anonymizedPatientId;
        this.uid = uid;
        this.originalPatientId = originalPatientId;
    }

    @Override
    public boolean equals(Object obj) {
        Uid other = (Uid) obj;
        return anonymizedPatientId.equals(other.anonymizedPatientId) && uid.equals(other.uid);
    }

    @Override
    public int hashCode() {
        return anonymizedPatientId.hashCode() ^ uid.hashCode();
    }
}

public class Anonymize {

    private static HashSet<String> patientList = new HashSet<String>();

    private static HashMap<Uid, String> uidHistory = new HashMap<Uid, String>();

    /** Template to be used to generate anonymous patient IDs. Use a default ID unless it is overridden. */
    private static String template = "$######";

    /**
     * Set the template.
     * 
     * @param template
     *            Template to be used.
     */
    public static synchronized void setTemplate(String template) {
        if ((template != null) && (template.length() > 0)) {
            Anonymize.template = template;
        }
    }

    /**
     * Remove all anonymizing history.
     */
    public static void clearHistory() {
        uidHistory.clear();
    }

    /**
     * Remove all anonymizing history for the given patient. The patient ID
     * given is for the patient's ID prior to anonymization. Upper and lower
     * case is ignored.
     * 
     * @param patientId
     *            Clear for this patient id.
     * 
     */
    public static void clearPatientHistory(String patientId) {
        ArrayList<Uid> removeList = new ArrayList<Uid>();
        for (Uid uid : uidHistory.keySet())
            if (uid.getOriginalPatientId().equalsIgnoreCase(patientId)) removeList.add(uid);
        for (Uid uid : removeList)
            uidHistory.remove(uid);
    }

    private static String genId() {
        StringBuffer patientId = new StringBuffer();
        char[] tmpl = template.toCharArray();
        int t = 0;
        while (t < tmpl.length) {
            switch (tmpl[t]) {
            case '*':
                int r = Util.random.nextInt(36);
                if (r < 10) {
                    patientId.append((char) ('0' + r));
                }
                else {
                    patientId.append((char) ('A' + (r - 10)));
                }
                break;
            case '?':
                patientId.append((char) ('A' + Util.random.nextInt(26)));
                break;
            case '#':
                patientId.append((char) ('0' + Util.random.nextInt(10)));
                break;
            case '%':
                t++;
                if (t <= (tmpl.length - 1)) {
                    patientId.append(tmpl[t]);
                }
                break;
            default:
                patientId.append(tmpl[t]);
                break;
            }
            t++;
        }
        return patientId.toString();
    }

    public synchronized static String makeUniquePatientId() {
        final int MAX_TRIES = 100;
        String patientId = null;
        int tries = 0;
        while ((patientId == null) && (++tries < MAX_TRIES)) {
            String patId = genId();
            if (!patientList.contains(patId)) {
                patientList.add(patId);
                patientId = patId;
                return patientId;
            }
        }
        throw new RuntimeException("Unable to generate unique patient ID with template: " + template + "  Last Id generated: " + patientId);
    }

    private static String establishNewPatientId(AttributeList attributeList) {
        Attribute patientAttribute = attributeList.get(TagFromName.PatientID);
        String patientId = null;
        if (patientAttribute != null) {
            patientId = patientAttribute.getSingleStringValueOrNull();
        }
        return (patientId == null) ? makeUniquePatientId() : patientId;
    }

    public static boolean isPreloadFile(File file) {
        if (file.getName().toLowerCase().endsWith(".xml")) {
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[256];
                fis.read(buffer);
                fis.close();
                return (new String(buffer)).contains("<AnonymizePreload");
            }
            catch (Exception e) {
            }
        }
        return false;
    }

    public static void preloadUids(File file) {
        try {
            Document doc = XML.parseToDocument(Utility.readFile(file));
            NodeList patientList = XML.getMultipleNodes(doc, "*/PatientID");
            int uidCount = 0;
            for (int p = 0; p < patientList.getLength(); p++) {
                Node patNode = patientList.item(p);
                String origPat = XML.getValue(patNode, "@orig");
                String anonPat = XML.getValue(patNode, "@anon");
                NodeList uidList = XML.getMultipleNodes(patNode, "UID");
                for (int u = 0; u < uidList.getLength(); u++) {
                    Node uidNode = uidList.item(u);
                    String origUid = XML.getValue(uidNode, "@orig");
                    String anonUid = XML.getValue(uidNode, "@anon");
                    uidHistory.put(new Uid(anonPat, origUid, origPat), anonUid);
                    // System.out.println("preload added origPat: " + origPat + " anonPat: " + anonPat + " origUid: " +
                    // origUid + " anonUid: " + anonUid);
                    uidCount++;
                }
            }
            Log.get().info("Preloaded " + patientList.getLength() + " patient IDs and " + uidCount + " UIDs from file " + file.getAbsolutePath());
        }
        catch (Exception e) {
            Log.get().severe("\n\nUnable to preload UIDS: " + e.getMessage() + "\n\n");
        }
    }

    /**
     * Translate the given UID into an anonymized one. If the
     * same UID is passed in, the same anonymized UID will be
     * returned.
     * 
     * @param anonymizedPatientId
     *            anonymized (target) patient ID.
     * 
     * @param oldUid
     *            Non-anonymized UID.
     * 
     * @return Anonymized UID that is being used instead
     *         of the non-anonymized UID.
     */
    private static synchronized String translateUid(String anonymizedPatientId, String oldUid, String originalPatientId) {
        String newUid = uidHistory.get(new Uid(anonymizedPatientId, oldUid, originalPatientId));
        if (newUid == null) {
            newUid = Util.getUID();
            uidHistory.put(new Uid(anonymizedPatientId, oldUid, originalPatientId), newUid);
        }
        return newUid;
    }

    private static void anonymizeNonSequenceAttribute(String anonymizedPatientId, Attribute attribute, Attribute replacement, String originalPatientId) {
        if (replacement != null) {
            String replacementValue = replacement.getSingleStringValueOrEmptyString();

            if (ValueRepresentation.isUniqueIdentifierVR(attribute.getVR())) {
                String oldUid = attribute.getSingleStringValueOrNull();
                if (oldUid != null) {
                    String newUid = Util.isValidUid(replacementValue) ? replacementValue : translateUid(anonymizedPatientId, oldUid, originalPatientId);
                    try {
                        attribute.setValue(newUid);
                    }
                    catch (DicomException e) {
                        ;
                    }
                }
            }

            else {
                try {
                    attribute.setValue(replacementValue);
                }
                catch (DicomException e) {
                    // If there is a problem, then just make the attribute empty
                    try {
                        attribute.removeValues();
                    }
                    catch (DicomException e1) {
                        ;
                    }
                }
            }
        }
    }

    private static TreeMap<AttributeTag, Attribute> getAttributeListValues(AttributeList attributeList) {
        return (TreeMap<AttributeTag, Attribute>) attributeList;
    }

    private static void aggressivelyAnonymize(Attribute attribute, HashMap<String, String> aggressiveReplaceList) {
        try {
            ArrayList<String> newValueList = new ArrayList<String>();
            String[] originalValueList = null;
            try {
                originalValueList = attribute.getStringValues();
            }
            catch (Exception e) {
                originalValueList = null;
            }

            if (originalValueList != null) {
                int changeCount = 0;
                for (String originalValue : originalValueList) {
                    String newValue = originalValue;
                    boolean changed = false;
                    for (String aggressiveValue : aggressiveReplaceList.keySet()) {
                        // Do not allow too many replacements. There is the possibility of recursion.
                        int count = 0;
                        int start;
                        int finish = 0;
                        while (((start = newValue.substring(finish).toLowerCase().indexOf(aggressiveValue)) != -1) && (count < 100)) {
                            start += finish;
                            finish = start + aggressiveValue.length();
                            newValue = newValue.substring(0, start) + aggressiveReplaceList.get(aggressiveValue) + newValue.substring(finish);
                            changed = true;
                            count++;
                        }
                    }
                    if (changed) {
                        newValueList.add(newValue);
                        changeCount++;
                    }
                    else {
                        newValueList.add(originalValue);
                    }
                }
                if (changeCount != 0) {
                    if (attribute instanceof UnknownAttribute) {
                        if (newValueList.size() > 0) {
                            Field origValues = UnknownAttribute.class.getDeclaredField("originalLittleEndianByteValues");
                            origValues.setAccessible(true);
                            origValues.set(attribute, newValueList.get(0).getBytes());
                        }
                    }
                    else {
                        attribute.removeValues();
                        for (String value : newValueList) {
                            try {
                                attribute.addValue(value);
                            }
                            catch (Exception e) {
                                attribute.setValues(value.getBytes());
                                String name = CustomDictionary.getName(attribute);
                                if (name == null) name = attribute.toString();
                                Log.get().info("Value for attribute " + name + " removed entirely due to aggressive anonymization.");
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            Log.get().severe("Unexpected exception (ignored) for attribute " + attribute + " in Anonymize.aggressivelyAnonymize: " + Log.fmtEx(e));
        }
    }

    private static boolean isDateOrDateTime(byte[] vr) {
        return ValueRepresentation.isDateVR(vr) || ValueRepresentation.isDateTimeVR(vr);
    }

    private static String truncToYear(String text) {
        byte[] bytes = text.getBytes();
        if (bytes.length > 4) bytes[4] = '0';
        if (bytes.length > 5) bytes[5] = '1';
        if (bytes.length > 6) bytes[6] = '0';
        if (bytes.length > 7) bytes[7] = '1';
        return new String(bytes);
    }

    /**
     * Truncate all dates to the year, changing the month and day to 1.
     * 
     * @param attribute
     */
    private static void truncateYear(Attribute attribute) {
        try {
            String[] originalValueList = attribute.getStringValues();
            attribute.removeValues();
            for (String s : originalValueList) {
                attribute.addValue(truncToYear(s));
            }
        }
        catch (Exception e) {

        }
    }

    private static HashMap<AttributeTag, AttributeTag> dateTimePairs = null;

    private static void initDateTimePairs() {
        dateTimePairs = new HashMap<AttributeTag, AttributeTag>();
        CustomDictionary dict = CustomDictionary.getInstance();
        Iterator<?> iter = dict.getTagIterator();
        while (iter.hasNext()) {
            AttributeTag dateTag = (AttributeTag) iter.next();
            if (ValueRepresentation.isDateVR(dict.getValueRepresentationFromTag(dateTag))) {
                String timeName = dict.getNameFromTag(dateTag).replace("Date", "Time");
                AttributeTag timeTag = dict.getTagFromName(timeName);
                if ((timeTag != null) && (ValueRepresentation.isTimeVR(dict.getValueRepresentationFromTag(timeTag)))) {
                    dateTimePairs.put(dateTag, timeTag);
                }
            }
        }
    }

    private static AttributeTag getDateMate(AttributeTag dateTag) {

        if (dateTimePairs == null) {
            initDateTimePairs();
        }

        return dateTimePairs.get(dateTag);
    }

    private static String getSubSecText(String sec) {
        String[] parts = sec.split("\\.");
        if (parts.length > 1)
            return "." + parts[1];
        else
            return "";
    }

    /**
     * Find date-time pairs within an attribute list and shift them. These have to be done as
     * pairs because the DICOM 142 supplement states that they should be done together.
     * 
     * @param attributeList
     * @return
     */
    private static AttributeList shiftAllDateTimePairAttributes(AttributeList attributeList) {

        AttributeList done = new AttributeList();

        Long shiftValue = AnonymizeDateTime.getShiftValue();

        for (Attribute attribute : attributeList.values()) {
            try {
                Attribute dateAttr = attribute;
                AttributeTag dateTag = attribute.getTag();
                AttributeTag timeTag = getDateMate(dateTag);
                if (timeTag != null) {
                    Attribute timeAttr = attributeList.get(timeTag);
                    if (timeAttr != null) {
                        try {
                            String[] originalDateValueList = dateAttr.getStringValues();
                            String[] originalTimeValueList = timeAttr.getStringValues();
                            dateAttr.removeValues();
                            timeAttr.removeValues();
                            int numPairs = Math.min(originalDateValueList.length, originalTimeValueList.length);

                            for (int pair = 0; pair < numPairs; pair++) {
                                try {
                                    Date oldDate = AnonymizeDateTime.parseAnon(originalDateValueList[pair] + "." + originalTimeValueList[pair]);
                                    Date newDate = new Date(oldDate.getTime() + shiftValue);
                                    String fullText = AnonymizeDateTime.dateTimeFormat.format(newDate);
                                    String dateText = fullText.substring(0, 8);
                                    String subSec = getSubSecText(originalTimeValueList[pair]);
                                    String timeText = fullText.substring(9) + subSec;

                                    dateAttr.addValue(dateText);
                                    timeAttr.addValue(timeText);
                                }
                                catch (Exception e) {
                                }
                            }

                            // If there were more dates than pairs, shift them and add them to the list. This would be
                            // sooooo weird.
                            for (int pair = numPairs; pair < originalDateValueList.length; pair++) {
                                Date oldDate = AnonymizeDateTime.parseAnon(originalDateValueList[pair]);
                                Date newDate = new Date(oldDate.getTime() + shiftValue);
                                String fullText = AnonymizeDateTime.dateTimeFormat.format(newDate);
                                String dateText = fullText.substring(0, 8);
                                dateAttr.addValue(dateText);
                            }

                            // If there were more times than pairs, shift them and add them to the list. This also would
                            // be sooooo weird.
                            for (int pair = numPairs; pair < originalTimeValueList.length; pair++) {
                                Date oldDate = AnonymizeDateTime.parseAnon("19700101." + originalTimeValueList[pair]);
                                Date newDate = new Date(oldDate.getTime() + shiftValue);
                                String fullText = AnonymizeDateTime.dateTimeFormat.format(newDate);
                                String timeText = fullText.substring(9) + getSubSecText(originalTimeValueList[pair]);
                                timeAttr.addValue(timeText);
                            }

                            done.put(dateAttr);
                            done.put(timeAttr);
                        }
                        catch (Exception e) {
                        }
                    }
                }

            }
            catch (Exception e) {
                ;
            }

        }

        return done;
    }

    /**
     * Shift dates that were not paired with a time.
     * 
     * @param attributeList
     * @param done
     */
    private static void shiftAllDateAttributes(AttributeList attributeList, AttributeList done) {
        Long shiftValue = AnonymizeDateTime.getShiftValue();

        for (Attribute dateAttr : attributeList.values()) {
            try {
                if (ValueRepresentation.isDateVR(dateAttr.getVR()) && (done.get(dateAttr.getTag()) == null)) {
                    String[] originalValueList = dateAttr.getStringValues();
                    dateAttr.removeValues();
                    for (String oldDateText : originalValueList) {
                        Date oldDate = AnonymizeDateTime.dateTimeFormat.parse(oldDateText);
                        Date newDate = new Date(oldDate.getTime() + shiftValue);
                        String newDateText = AnonymizeDateTime.dateTimeFormat.format(newDate).substring(0, 8);
                        if (!oldDateText.equalsIgnoreCase(newDateText)) {
                            dateAttr.addValue(newDateText);
                        }
                    }
                }
            }
            catch (Exception e) {
                ;
            }
        }
    }

    private static void shiftAllTimeAttributes(AttributeList attributeList, AttributeList done) {
        Long shiftValue = AnonymizeDateTime.getShiftValue();

        for (Attribute timeAttr : attributeList.values()) {
            try {
                if (ValueRepresentation.isTimeVR(timeAttr.getVR()) && (done.get(timeAttr.getTag()) == null)) {
                    String[] originalValueList = timeAttr.getStringValues();
                    timeAttr.removeValues();
                    for (String oldTimeText : originalValueList) {
                        Date oldTime = AnonymizeDateTime.dateTimeFormat.parse(oldTimeText);
                        Date newTime = new Date(oldTime.getTime() + shiftValue);
                        String newTimeText = AnonymizeDateTime.dateTimeFormat.format(newTime).substring(9) + getSubSecText(oldTimeText);
                        timeAttr.addValue(newTimeText);
                    }
                }
            }
            catch (Exception e) {
                ;
            }
        }
    }

    private static void replaceAllValues(Attribute attribute, String value) {
        try {
            String[] originalValueList = attribute.getStringValues();
            attribute.removeValues();
            for (int i = 0; i < originalValueList.length; i++) {
                attribute.addValue(value);
            }
        }
        catch (Exception e) {

        }
    }

    private static void dateTimeAnon(AttributeList attributeList) {
        String dateTimeText = AnonymizeDateTime.dateTimeFormat.format(AnonymizeDateTime.getAnonValue());
        String dateText = dateTimeText.substring(0, 8);
        String timeText = dateTimeText.substring(9, 15);
        for (Attribute at : attributeList.values()) {
            byte[] vr = at.getVR();
            if (ValueRepresentation.isDateVR(vr)) {
                replaceAllValues(at, dateText);
            }
            if (ValueRepresentation.isTimeVR(vr)) {
                replaceAllValues(at, timeText);
            }
            if (ValueRepresentation.isDateTimeVR(vr)) {
                replaceAllValues(at, dateTimeText);
            }
        }
    }

    private static void dateTimeYearTruncate(AttributeList attributeList) {
        for (Attribute attribute : attributeList.values()) {
            if (isDateOrDateTime(attribute.getVR())) {
                truncateYear(attribute);
            }
        }
    }

    /**
     * Shift all attributes with value representation DT (datetime).
     * 
     * @param attributeList
     */
    private static void shiftAllDateTimeAttributes(AttributeList attributeList) {
        for (Attribute attribute : attributeList.values()) {
            try {
                if (ValueRepresentation.isDateTimeVR(attribute.getVR())) {
                    String[] originalValueList = attribute.getStringValues();
                    attribute.removeValues();
                    for (String s : originalValueList) {
                        Date orig = DateTimeAttribute.getDateFromFormattedString(s);
                        Date shifted = new Date(orig.getTime() + AnonymizeDateTime.getShiftValue());
                        attribute.addValue(DateTimeAttribute.getFormattedStringDefaultTimeZone(shifted));
                    }
                }
            }
            catch (Exception e) {
            }

        }
    }

    private static void dateTimeShift(AttributeList attributeList) {
        if (AnonymizeDateTime.getShiftValue() != 0) {
            shiftAllDateTimeAttributes(attributeList);
            AttributeList done = shiftAllDateTimePairAttributes(attributeList);
            shiftAllDateAttributes(attributeList, done);
            shiftAllTimeAttributes(attributeList, done);
        }
    }

    private static void anonymizeDatesAndTimes(AttributeList attributeList) {

        switch (AnonymizeDateTime.getMode()) {
        case Anon: {
            dateTimeAnon(attributeList);
            break;
        }

        case Year: {
            dateTimeYearTruncate(attributeList);
            break;
        }

        case Shift: {
            dateTimeShift(attributeList);
            break;
        }

        default:
        }
    }

    /**
     * Perform anonymization recursively to accommodate sequence attributes.
     * 
     * @param patientId
     *            New patient ID.
     * 
     * @param attributeList
     *            Anonymize (modify) this.
     * 
     * @param replacementAttributeList
     *            Reference this for what is to be anonymized.
     */
    private static void anonymize(String anonymizedPatientId, AttributeList attributeList, AttributeList replacementAttributeList,
            HashMap<String, String> aggressiveReplaceList,
            String originalPatientId) {
        anonymizeDatesAndTimes(attributeList);
        for (Attribute attribute : getAttributeListValues(attributeList).values()) {
            AttributeTag tag = attribute.getTag();
            if (attribute instanceof SequenceAttribute) {
                Iterator<?> si = ((SequenceAttribute) attribute).iterator();
                while (si.hasNext()) {
                    SequenceItem item = (SequenceItem) si.next();
                    anonymize(anonymizedPatientId, item.getAttributeList(), replacementAttributeList, aggressiveReplaceList, originalPatientId);
                }
            }
            else {
                Attribute replacement = replacementAttributeList.get(tag);
                if (replacement != null) {
                    anonymizeNonSequenceAttribute(anonymizedPatientId, attribute, replacement, originalPatientId);
                }
                aggressivelyAnonymize(attribute, aggressiveReplaceList);
            }
        }
    }

    /**
     * Anonymize the given DICOM object. Values are replaced with corresponding values
     * in the replacement list. All UIDs are replaced with newly constructed ones, and
     * are kept consistent, corresponding to the new PatientID.
     * 
     * The PatientID is required to be anonymized. If the PatientID is not given in
     * the replacement list, then a new unique patient ID will be constructed and put
     * into the target attribute list.
     * 
     * @param attributeList
     *            Target object to be anonymized.
     * 
     * @param replacementAttributeList
     *            List of values to be written into the attributeList.
     */
    public static synchronized void anonymize(AttributeList attributeList, AttributeList replacementAttributeList) {
        HashMap<String, String> aggressiveReplaceList = ClientConfig.getInstance().getAggressiveAnonymization(attributeList, CustomDictionary.getInstance());
        String originalPatientId = (attributeList.get(TagFromName.PatientID) == null) ? null : attributeList.get(TagFromName.PatientID).getSingleStringValueOrNull();
        String anonymizedPatientId = establishNewPatientId(replacementAttributeList);
        anonymize(anonymizedPatientId, attributeList, replacementAttributeList, aggressiveReplaceList, originalPatientId);
    }

    /**
     * For testing only.
     * 
     * @param args
     *            Ignored.
     * 
     * @throws Exception
     *             Should never happen.
     */
    public static void main(String[] args) throws Exception {

        if (true) {
            File file = new File("D:\\tmp\\dcmexp\\dicomclient-1.0.45\\preload.xml");
            preloadUids(file);
            Thread.sleep(5000);
            Util.exitSuccess();
        }

        String[] uidList = { "2.318828.3", " 123", "", "123", "123...a45.6", ".32362.38.6.8468.7.3.2.4252", "1.2.3." };
        for (String uid : uidList) {
            System.out.println("'" + uid + "' :" + Util.isValidUid(uid));
        }

        HashMap<String, String> aggressiveReplaceList = new HashMap<String, String>();
        aggressiveReplaceList.put("ril", "-----");
        aggressiveReplaceList.put("anc", "--");
        aggressiveReplaceList.put("big", "---");
        aggressiveReplaceList.put("18000101", "77777777");
        aggressiveReplaceList.put("orig", "origin");

        Attribute attribute = AttributeFactory.newAttribute(TagFromName.ManufacturerModelName);
        String origValue = "Brilliance Big Bore orig orig ";
        attribute.addValue(origValue);
        aggressivelyAnonymize(attribute, aggressiveReplaceList);
        System.out.println(origValue + " --> " + attribute);

        attribute = AttributeFactory.newAttribute(TagFromName.PatientBirthDate);
        origValue = "18000101";
        attribute.addValue(origValue);
        aggressivelyAnonymize(attribute, aggressiveReplaceList);
        System.out.println(origValue + " --> " + attribute);

    }
}
