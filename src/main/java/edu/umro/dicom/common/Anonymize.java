package edu.umro.dicom.common;

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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.UnknownAttribute;
import com.pixelmed.dicom.ValueRepresentation;

import edu.umro.dicom.client.ClientConfig;
import edu.umro.dicom.client.CustomDictionary;
import edu.umro.dicom.client.Util;
import edu.umro.util.Log;
import edu.umro.util.UMROGUID;


/**
 * Represent a patient ID - GUID combination
 * for anonymization.  The pairs are used to
 * facilitate the proper re-using of GUIDs
 * necessary to consistent anonymization of
 * GUIDs.
 * 
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
class Guid {

    /** Non-anonymized patient ID. */
    private String patientId;

    /** Anonymized GUID. */
    private String guid;

    public Guid(String patientId, String guid) {
        this.patientId = patientId;
        this.guid = guid;
    }

    @Override
    public boolean equals(Object obj) {
        Guid other = (Guid)obj;
        return patientId.equals(other.patientId) && guid.equals(other.guid);
    }

    @Override
    public int hashCode() {
        return patientId.hashCode() ^ guid.hashCode();
    }
}


public class Anonymize {


    private static HashSet<String> patientList = new HashSet<String>();

    private static String rootGuid = UMROGUID.UMRO_ROOT_GUID;

    private static HashMap<Guid, String> guidHistory = new HashMap<Guid, String>();

    /** Template to be used to generate anonymous patient IDs.  Use a default ID. */
    private static String template = "$######";


    /**
     * Set the template.
     * 
     * @param template Template to be used.
     */
    public static synchronized void setTemplate(String template) {
        if ((template != null) && (template.length() > 0)) {
            Anonymize.template = template;
        }
    }


    /**
     * Set the root GUID.
     * 
     * @param rootGuid The new root GUID.
     */
    public static synchronized void setRootGuid(String rootGuid) {
        if ((rootGuid != null) && (rootGuid.length() > 0)) {
            Anonymize.rootGuid = rootGuid;
        }
    }


    private static String genId() {
        StringBuffer patientId = new StringBuffer();
        char[] tmpl = template.toCharArray();
        int t = 0;
        Random random = new Random();
        while (t < tmpl.length) {
            switch (tmpl[t]) {
                case '*' :
                    int r = random.nextInt(36);
                    if (r < 10) {
                        patientId.append((char)('0' + r));
                    }
                    else {
                        patientId.append((char)('A' + (r-10)));
                    }
                    break;
                case '?' :
                    patientId.append((char)('A' + random.nextInt(26)));
                    break;
                case '#' :
                    patientId.append((char)('0' + random.nextInt(10)));
                    break;
                case '%' :
                    t++;
                    if (t <= (tmpl.length-1)) {
                        patientId.append(tmpl[t]);
                    }
                    break;
                default :
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


    /**
     * Translate the given GUID into an anonymized one.  If the
     * same GUID is passed in, the same anonymized GUID will be
     * returned.
     * 
     * @param anonimizedPatientId anonymized (target) patient ID.
     * 
     * @param oldGuid Non-anonymized GUID.
     * 
     * @return Anonymized GUID that is being used instead
     * of the non-anonymized GUID.
     */
    private static synchronized String translateGuid(String anonimizedPatientId, String oldGuid) {

        String newGuid = guidHistory.get(new Guid(anonimizedPatientId, oldGuid));
        if (newGuid == null) {
            try {
                newGuid = UMROGUID.getUID();
                // This should always be true
                if (newGuid.startsWith(UMROGUID.UMRO_ROOT_GUID)) {
                    newGuid = rootGuid  + newGuid.substring(UMROGUID.UMRO_ROOT_GUID.length());
                }

                guidHistory.put(new Guid(anonimizedPatientId, oldGuid), newGuid);
            } catch (UnknownHostException e) {
                Log.get().logrb(Level.SEVERE, Anonymize.class.getCanonicalName(),
                        "translateGuid", null, "UnknownHostException Unable to generate new GUID", e);
            }
        }
        return newGuid;
    }


    private static void anonymizeNonSequenceAttribute(String patientId, Attribute attribute, Attribute replacement) {
        if (replacement != null) {
            String replacementValue = replacement.getSingleStringValueOrEmptyString();

            if (ValueRepresentation.isUniqueIdentifierVR(attribute.getVR())) {
                String oldGuid = attribute.getSingleStringValueOrNull();
                if (oldGuid != null) {
                    String newGuid = Util.isValidUid(replacementValue) ? replacementValue : translateGuid(patientId, oldGuid);
                    try {
                        attribute.setValue(newGuid);
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


    @SuppressWarnings("unchecked")
    private static TreeMap<AttributeTag, Attribute> getAttributeListValues(AttributeList attributeList) {
        return (TreeMap<AttributeTag, Attribute>)attributeList;
    }


    private static void aggressivelyAnonymize(Attribute attribute, HashMap<String,String> aggressiveReplaceList) {
        try {
            ArrayList<String> newValueList = new ArrayList<String>();
            String[] originalValueList = null;
            try { originalValueList = attribute.getStringValues(); }
            catch (Exception e) { originalValueList = null; }

            if (originalValueList != null) {
                int changeCount = 0;
                for (String originalValue : originalValueList) {
                    String newValue = originalValue;
                    boolean changed = false;
                    for (String aggressiveValue : aggressiveReplaceList.keySet()) {
                        // Do not allow too many replacements.  There is the possibility of recursion.
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
                            } catch (Exception e) {
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


    /**
     * Perform anonymization recursively to accommodate sequence attributes.
     * 
     * @param patientId New patient ID.
     * 
     * @param attributeList Anonymize (modify) this.
     * 
     * @param replacementAttributeList Reference this for what is to be anonymized.
     */
    private static void anonymize(String patientId, AttributeList attributeList, AttributeList replacementAttributeList, HashMap<String,String> aggressiveReplaceList) {
        for (Attribute attribute : getAttributeListValues(attributeList).values()) {
            AttributeTag tag = attribute.getTag();
            if (attribute instanceof SequenceAttribute) {
                Iterator<?> si = ((SequenceAttribute)attribute).iterator();
                while (si.hasNext()) {
                    SequenceItem item = (SequenceItem)si.next();
                    anonymize(patientId, item.getAttributeList(), replacementAttributeList, aggressiveReplaceList);
                }
            }
            else {
                Attribute replacement = replacementAttributeList.get(tag);
                if (replacement != null) {
                    anonymizeNonSequenceAttribute(patientId, attribute, replacement);
                }
                aggressivelyAnonymize(attribute, aggressiveReplaceList);
            }
        }
    }


    /**
     * Anonymize the given DICOM object.  Values are replaced with corresponding values
     * in the replacement list.  All UIDs are replaced with newly constructed ones, and
     * are kept consistent, corresponding to the new PatientID.
     * 
     * The PatientID is required to be anonymized.  If the PatientID is not given in
     * the replacement list, then a new unique patient ID will be constructed and put
     * into the target attribute list.
     * 
     * @param attributeList Target object to be anonymized.
     * 
     * @param replacementAttributeList List of values to be written into the attributeList.
     */
    public static synchronized void anonymize(AttributeList attributeList, AttributeList replacementAttributeList) {
        HashMap<String, String> aggressiveReplaceList = ClientConfig.getInstance().getAggressiveAnonymization(attributeList, CustomDictionary.getInstance());
        anonymize(establishNewPatientId(replacementAttributeList), attributeList, replacementAttributeList, aggressiveReplaceList);

        // TODO temporary hack for fixing special case of damaged DICOM files
        if (System.out == null) {
            try {
                Attribute sopCls = attributeList.get(TagFromName.SOPClassUID);
                if ((sopCls != null) && (sopCls.getSingleStringValueOrEmptyString().equals(SOPClass.RTImageStorage))) {
                    {
                        Attribute patPos = attributeList.get(TagFromName.PatientPosition);
                        if (patPos == null) {
                            patPos = AttributeFactory.newAttribute(TagFromName.PatientPosition);
                            patPos.addValue("HFS");
                            attributeList.put(patPos);
                        }
                    }
                    {
                        Attribute crDate = attributeList.get(TagFromName.CreationDate);
                        if (crDate == null) {
                            crDate = AttributeFactory.newAttribute(TagFromName.CreationDate);
                            crDate.addValue("20131022");
                            attributeList.put(crDate);
                        }
                    }
                }
                {
                    Attribute birthDate = attributeList.get(TagFromName.PatientBirthDate);
                    if (birthDate == null) {
                        birthDate = AttributeFactory.newAttribute(TagFromName.PatientBirthDate);
                        birthDate.addValue("18000101");
                        attributeList.put(sopCls);
                    }
                }
                if (System.out == null) {
                    String siUid = System.getenv("SOPInstanceUID");
                    if ((siUid != null) && (siUid.length() > 4)) {
                        Attribute siuAttr = AttributeFactory.newAttribute(TagFromName.SOPInstanceUID);
                        siuAttr.addValue(siUid);
                        attributeList.put(sopCls);
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Badness: " + e);
            }
        }

    }

    
    /**
     * For testing only.
     * 
     * @param args Ignored.
     * 
     * @throws Exception Should never happen.
     */
    public static void main(String[] args) throws Exception {

        String[] uidList = { "2.318828.3", " 123", "", "123", "123...a45.6", ".32362.38.6.8468.7.3.2.4252", "1.2.3." };
        for (String uid : uidList) {
            System.out.println("'" + uid +  "' :" + Util.isValidUid(uid));
        }

        HashMap<String,String> aggressiveReplaceList = new HashMap<String, String>();
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
