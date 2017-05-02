package edu.umro.dicom.client;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.ValueRepresentation;

import edu.umro.util.JarInfo;
import edu.umro.util.Log;
import edu.umro.util.UMROException;
import edu.umro.util.Utility;
import edu.umro.util.XML;

/**
 * Get the configuration information.
 * 
 * @author Jim Irrer irrer@umich.edu
 * 
 */
public class ClientConfig {

    /** Name of configuration file. */
    private static final String CONFIG_FILE_NAME = "DicomClientConfig.xml";

    /** Default characters to check for disqualifying content as PHI. */
    private static final String DEFAULT_PHI_DISQUALIFYING_CHARACTERS = "0123456789$%";
    private HashSet<Character> phiDisqualifyingCharacters = null;

    /** List of all possible configuration files. */
    private static final String[] CONFIG_FILE_LIST = {
            System.getProperty("dicomclient.config"),
            CONFIG_FILE_NAME,
            "src\\main\\resources\\" + CONFIG_FILE_NAME
    };

    /** Instance of this object. */
    private volatile static ClientConfig clientConfig = null;

    private volatile static boolean configHasBeenTried = false;

    /** Configuration information from file. */
    private volatile Document config = null;

    /** List of private tags. */
    private ArrayList<PrivateTag> privateTagList = null;

    private AttributeList anonymizingReplacementList = null;

    private Document parseConfigFile(String configFileName) {
        try {
            Log.get().info("Trying configuration file " + configFileName);
            config = XML.parseToDocument(Utility.readFile(new File(configFileName)));
            Log.get().info("Using configuration file " + configFileName);
            return config;
        }
        catch (Exception e) {
            ;
        }
        return null;
    }

    /*
     * Try to get the configuration file by looking for it in
     * same directory as the jar file that contains this class.
     * 
     * @return Either configuration file or null on failure.
     */
    private Document getConfigFromJarPath() {
        File dir = null;
        try {
            JarInfo jarInfo = new JarInfo(this.getClass());
            File jarFile = new File(jarInfo.getFullJarFilePath());
            dir = jarFile.getParentFile();
            File configFile = new File(dir, CONFIG_FILE_NAME);
            return parseConfigFile(configFile.getAbsolutePath());
        }
        catch (Exception e) {
            String filePath = (dir == null) ? "" : ("'" + dir.getAbsolutePath() + "'");
            Log.get().warning("Could not get configuration file by looking in the same directory " + filePath + " as the jar file: " + e);
        }

        return null;
    }

    /**
     * Read in the configuration for the client from the configuration file. Try
     * all files on the list and use the first one that parses.
     */
    private void tryConfigDirs() {
        for (String configFileName : CONFIG_FILE_LIST) {
            config = parseConfigFile(configFileName);
            if (config != null) {
                break;
            }
        }

        if (config == null) config = getConfigFromJarPath();

        if (config == null) {
            String message = "Unable to read and parse any configuration file of: " + CONFIG_FILE_LIST;
            Log.get().severe(message);
            if (!DicomClient.inCommandLineMode()) {
                String msg = "<html>The application could not find a configuration file.<br>&nbsp<p>\n" +
                        "The configuration is an XML file and must be named <br>\n" + CONFIG_FILE_NAME + " ." +
                        "Without this file anonymization can not be performed.<br>&nbsp<p>\n" +
                        "The file should be in the same folder as the jar file." +
                        "</html>";
                new Alert(msg, "No Configuration File", null, null, true);
            }
        }
    }

    /**
     * Construct a configuration object.
     */
    private ClientConfig() {
        if (configHasBeenTried)
            return;
        else {
            configHasBeenTried = true;

            tryConfigDirs();
        }
    }

    /**
     * Get the flag indicating whether or not the upload capability should be shown. If there is a problem,
     * return true.
     * 
     * @return True if all help, false if only anonymize help.
     */
    public boolean getShowUploadCapability() {
        try {
            String text = XML.getValue(config, "/DicomClientConfig/ShowUploadHelp/text()");
            return text.equalsIgnoreCase("true") || text.equalsIgnoreCase("yes");
        }
        catch (UMROException e) {

        }
        Log.get().severe("getShowUploadHelp: Unable to read configuration file " + CONFIG_FILE_NAME);
        return true;
    }

    /**
     * Get the template that controls how new patient IDs are generated for anonymization.
     * 
     * @return Template that controls how new patient IDs are generated for anonymization.
     */
    public String getAnonPatientIdTemplate() {
        try {
            return XML.getValue(config, "/DicomClientConfig/AnonPatientIdTemplate/text()");
        }
        catch (UMROException e) {

        }
        Log.get().severe("getAnonPatientIdTemplate: Unable to read configuration file " + CONFIG_FILE_NAME);
        return null;
    }

    private HashSet<Character> getPhiDisqualifyingCharacters() {
        if (phiDisqualifyingCharacters == null) {
            String pdcText = DEFAULT_PHI_DISQUALIFYING_CHARACTERS;
            try {
                String text = XML.getValue(config, "/DicomClientConfig/PhiDisqualifyingCharacters/text()");
                pdcText = text;
            }
            catch (Exception e) {
                pdcText = DEFAULT_PHI_DISQUALIFYING_CHARACTERS;
            }
            phiDisqualifyingCharacters = new HashSet<Character>();
            for (int i = 0; i < pdcText.length(); i++)
                phiDisqualifyingCharacters.add(pdcText.charAt(i));
        }
        return phiDisqualifyingCharacters;
    }

    /**
     * Return TRUE if this value might be PHI. Values are not considered PHI if they
     * contain special characters. For example, TEST5 would not be PHI because it
     * contains the digit 5.
     * 
     * @param text
     * @return
     */
    private boolean mightBePHI(String text) {
        HashSet<Character> phiDisq = getPhiDisqualifyingCharacters();
        for (int i = 0; i < text.length(); i++) {
            if (phiDisq.contains(text.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Get the values that should be replaced for aggressive patient anonymization.
     * 
     * @return List of values (in lower case) and their replacement values.
     */
    public HashMap<String, String> getAggressiveAnonymization(AttributeList attributeList, DicomDictionary dictionary) {
        HashMap<String, String> replaceList = new HashMap<String, String>();
        if (!DicomClient.getAggressivelyAnonymize()) return replaceList;
        getReservedWordList();
        try {
            NodeList nodeList = XML.getMultipleNodes(config, "/DicomClientConfig/AggressiveAnonymization");
            for (int n = 0; n < nodeList.getLength(); n++) {
                Node node = nodeList.item(n);
                String replacement = XML.getAttributeValue(node, "replacement");
                replacement = (replacement == null) ? "" : replacement;
                String tagName = XML.getValue(node, "text()");
                AttributeTag tag = dictionary.getTagFromName(tagName);
                if (tag == null) {
                    throw new RuntimeException("Unknown DICOM attribute " + tagName + " in AggressiveAnonymization list.");
                }
                Attribute attribute = attributeList.get(tag);
                if (attribute != null) {
                    String[] origValueList = attribute.getOriginalStringValues();
                    if ((origValueList != null) && (origValueList.length > 0)) {
                        for (String value : origValueList) {
                            if (mightBePHI(value)) {
                                String[] tokenList = value.toLowerCase().split("[^a-z0-9]");
                                for (String token : tokenList) {
                                    if ((token.length() > 1) && (!reservedWordList.contains(token))) {
                                        replaceList.put(token, replacement);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (UMROException e) {
            Log.get().severe("UMROException getAggressiveAnonymization: " + Log.fmtEx(e));
        }
        catch (DicomException e) {
            Log.get().severe("DicomException getAggressiveAnonymization: " + Log.fmtEx(e));
        }

        return replaceList;
    }

    private HashSet<String> reservedWordList = null;

    private HashSet<String> getReservedWordList() {
        if (reservedWordList == null) {
            reservedWordList = new HashSet<String>();
            String text = null;
            try {
                text = XML.getValue(config, "/DicomClientConfig/ReservedWordList/text()");
                String[] list = text.toLowerCase().replace('\r', ' ').replace('\n', ' ').split(" ");
                for (String word : list) {
                    reservedWordList.add(word);
                }
                DicomDictionary dictionary = new DicomDictionary();
                @SuppressWarnings("rawtypes")
                Iterator i = dictionary.getTagIterator();
                while (i.hasNext()) {
                    AttributeTag tag = (AttributeTag) i.next();
                    String fullName = dictionary.getFullNameFromTag(tag);
                    String[] wordList = fullName.toLowerCase().split(" ");
                    for (String word : wordList) {
                        reservedWordList.add(word);
                    }
                }
            }
            catch (UMROException e) {
            }
        }
        return reservedWordList;
    }

    /**
     * Get the template that controls how new patient IDs are generated for anonymization.
     * 
     * @return Template that controls how new patient IDs are generated for anonymization.
     * 
     * @throws UMROException
     */
    public String getRootUid() throws UMROException {
        try {
            return XML.getValue(config, "/DicomClientConfig/RootUid/text()");
        }
        catch (Exception e) {
            // Try old name just in case the configuration file uses the old name
            return XML.getValue(config, "/DicomClientConfig/RootGuid/text()");
        }
    }

    /**
     * Get the application name. Also is used for SourceApplicationEntityTitle when generating DICOM files.
     * 
     * @return The application Name.
     */
    public String getApplicationName() {
        String value = null;
        try {
            value = XML.getValue(config, "/DicomClientConfig/ApplicationName/text()");
        }
        catch (Exception e) {
            // Use standard name if custom name not specified.
            value = null;
        }
        return (value == null) ? "DICOM+" : value;
    }

    /**
     * Only let user control certain types of attributes. It does not make
     * sense to manually control values of UIDs, and sequence attributes do
     * not have values.
     * 
     * @param tag
     *            Tag to check.
     * 
     * @return
     */
    private boolean canControlAnonymizing(AttributeTag tag) {
        return (!ValueRepresentation.isSequenceVR(CustomDictionary.getInstance().getValueRepresentationFromTag(tag)));
    }

    /**
     * Get the list of attributes to anonymize and their values. All UIDs are
     * anonymized using a manufactured UID.
     * 
     * @return
     */
    public AttributeList getAnonymizingReplacementList() {
        if (anonymizingReplacementList == null) {
            anonymizingReplacementList = new AttributeList();
            try {
                NodeList nodeList = XML.getMultipleNodes(config, "/DicomClientConfig/AnonymizeDefaultList/*");
                for (int ad = 0; ad < nodeList.getLength(); ad++) {
                    Node node = nodeList.item(ad);
                    String tagText = XML.getValue(node, "@Name");
                    if (tagText != null) {
                        try {
                            AttributeTag tag = CustomDictionary.getInstance().getTagFromName(tagText);
                            if (tag != null) {
                                String value = XML.getValue(node, "text()");
                                value = (value == null) ? "" : value;
                                Attribute attribute = AttributeFactory.newAttribute(tag);
                                if (canControlAnonymizing(tag)) {
                                    attribute.addValue(value);
                                    anonymizingReplacementList.put(attribute);
                                }
                            }
                        }
                        catch (Exception e) {
                            Log.get().warning("Unable to parse list of default attributes to anonymize.  User will have to supply them manually.");
                        }
                    }
                }
            }
            catch (UMROException e) {
                Log.get().warning("Unable to parse list of default attributes to anonymize.  User will have to supply them manually.");
            }
        }
        return anonymizingReplacementList;
    }

    public synchronized ArrayList<PrivateTag> getPrivateTagList() {
        if (privateTagList == null) {
            privateTagList = new ArrayList<PrivateTag>();
            try {
                NodeList nodeList = XML.getMultipleNodes(config, "/DicomClientConfig/PrivateTagList/*");
                for (int n = 0; n < nodeList.getLength(); n++) {
                    Node node = nodeList.item(n);
                    String group = XML.getValue(node, "@group");
                    if (group.contains(":")) {
                        int element = Integer.parseInt(XML.getValue(node, "@element").toUpperCase(), 16);
                        byte[] valueRepresentation = XML.getValue(node, "@vr").getBytes();
                        String name = node.getNodeName();
                        String fullName = XML.getValue(node, "@fullName");
                        String[] parts = group.split(":");
                        int firstGroup = Integer.parseInt(parts[0].toUpperCase(), 16);
                        int lastGroup = Integer.parseInt(parts[1].toUpperCase(), 16);
                        int incr = (parts.length < 2) ? 1 : Integer.parseInt(parts[2].toUpperCase(), 16);
                        for (int g = firstGroup; g <= lastGroup; g += incr) {
                            String gHex = String.format("%04x", g);
                            privateTagList.add(new PrivateTag(g, element, valueRepresentation, name + gHex, fullName + " " + gHex));
                        }
                    }
                    else {
                        privateTagList.add(new PrivateTag(node));
                    }
                }
            }
            catch (UMROException e) {
                Log.get().warning("Problem interpreting custom tags: " + e);
            }
            catch (DicomException e) {
                Log.get().warning("DICOM Problem interpreting custom tags: " + e);
            }
        }
        return privateTagList;
    }

    public static ClientConfig getInstance() {
        if (clientConfig == null) {
            clientConfig = new ClientConfig();
        }
        return clientConfig;
    }
}
