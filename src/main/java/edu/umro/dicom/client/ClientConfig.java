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
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.ValueRepresentation;

import edu.umro.dicom.common.Util;
import edu.umro.util.Log;
import edu.umro.util.UMROException;
import edu.umro.util.Utility;
import edu.umro.util.XML;

/**
 * Get the configuration information.
 * 
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
public class ClientConfig {

    /** Name of configuration file. */
    private static final String CONFIG_FILE_NAME = "DicomClientConfig.xml";

    /** List of all possible configuration files. */
    private static final String[] CONFIG_FILE_LIST = {
        System.getProperty("dicomclient.config"),
        CONFIG_FILE_NAME,
        "src\\main\\resources\\" + CONFIG_FILE_NAME
    };

    /** Instance of this object. */
    private volatile static ClientConfig clientConfig = null;

    /** Configuration information from file. */
    private volatile Document config = null;


    /**
     * Read in the configuration for the client from the configuration file.  Try
     * all files on the list and use the first one that parses.
     */
    private void parseConfigFile() {
        for (String configFileName : CONFIG_FILE_LIST) {
            try {
                config = XML.parseToDocument(Utility.readFile(new File(configFileName)));
            }
            catch (Exception e) {
                Log.get().warning("Unable to parse file " + configFileName + " : " + e);
            }
            if (config == null) {
                Log.get().warning("Unable to read and parse configuration file: " + configFileName);
            }
            else {
                Log.get().info("Using configuration file " + configFileName);
                break;
            }
        }
        if (config == null) {
            Log.get().severe("Unable to read and parse any configuration file of: " + CONFIG_FILE_LIST);
        }
    }


    /**
     * Construct a configuration object.
     */
    public ClientConfig() {
        parseConfigFile();
    }


    /**
     * Get the base URL for the DICOM Service with any terminating /'s removed
     *  
     * @return Base URL for DICOM service, or null if not initialized.
     */
    public String getServerBaseUrl() {
        if (config != null) {
            try {
                return XML.getValue(config, "/DicomClientConfig/DicomServiceUrl/text()");
            }
            catch (UMROException e) {
                Log.get().logrb(Level.SEVERE, this.getClass().getCanonicalName(), "AriaVerifier", null,
                        "Failed to get DICOM service URL from configuration file " + CONFIG_FILE_NAME, e);
            }
        }
        Log.get().severe("ClientConfig.getServerBaseUrl: Unable to read configuration file " + CONFIG_FILE_NAME);
        return null;
    }


    /**
     * Get the flag indicating whether or not the upload capability should be shown.  If there is a problem,
     * return true.
     *  
     * @return True if all help, false if only anonymize help.
     */
    public boolean getShowUploadCapability() {
        if (config != null) {
            try {
                String text = XML.getValue(config, "/DicomClientConfig/ShowUploadHelp/text()");
                return text.equalsIgnoreCase("true") || text.equalsIgnoreCase("yes");
            }
            catch (UMROException e) {

            }
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
        if (config != null) {
            try {
                return XML.getValue(config, "/DicomClientConfig/AnonPatientIdTemplate/text()");
            }
            catch (UMROException e) {

            }
        }
        Log.get().severe("getAnonPatientIdTemplate: Unable to read configuration file " + CONFIG_FILE_NAME);
        return null;
    }


    /**
     * Get the template that controls how new patient IDs are generated for anonymization.
     *  
     * @return Template that controls how new patient IDs are generated for anonymization.
     */
    public String getRootGuid() {
        if (config != null) {
            try {
                return XML.getValue(config, "/DicomClientConfig/RootGuid/text()");
            }
            catch (UMROException e) {

            }
        }
        Log.get().severe("getRootGuid: Unable to read configuration file " + CONFIG_FILE_NAME);
        return null;
    }


    /**
     * Get flag indicating whether or not profiling is to be done.  If there is a problem
     * getting the flag, then assume false.
     *  
     * @return Flag indicating whether profiling should be done.
     */
    public boolean getProfileFlag() {
        if (config != null) {
            try {
                String text = XML.getValue(config, "/DicomClientConfig/Profile/text()").trim();
                boolean enabled =
                    text.equalsIgnoreCase("true") || 
                    text.equalsIgnoreCase("yes") || 
                    text.equalsIgnoreCase("y") || 
                    text.equalsIgnoreCase("1") || 
                    text.equalsIgnoreCase("t");
                return enabled;
            }
            catch (UMROException e) {
                Log.get().info("getProfileFlag: Unable to get flag: " + e);

            }
        }
        Log.get().info("getProfileFlag: Unable to read configuration file " + CONFIG_FILE_NAME);
        return false;
    }


    /**
     * Only let user control certain types of attributes.  It does not make
     * sense to manually control values of UIDs, and sequence attributes do
     * not have values.
     * 
     * @param tag Tag to check.
     * 
     * @return
     */
    private boolean canControlAnonymizing(AttributeTag tag) {
        return (!ValueRepresentation.isSequenceVR(CustomDictionary.getInstance().getValueRepresentationFromTag(tag)));
    }


    /**
     * Get the list of attributes to anonymize and their values.  All UIDs are
     * anonymized by default.
     * 
     * @return
     */
    public AttributeList getAnonymizingReplacementList() {
        AttributeList attributeList = new AttributeList();
        try {
            NodeList nodeList = XML.getMultipleNodes(config, "/DicomClientConfig/AnonymizeDefaultList/*");
            for (int ad = 0; ad < nodeList.getLength(); ad++) {
                Node node = nodeList.item(ad);
                String tagText = XML.getValue(node, "@Name");
                if (tagText != null) {
                    AttributeTag tag = CustomDictionary.getInstance().getTagFromName(tagText);
                    String value = XML.getValue(node, "text()");
                    value = (value == null) ? "" : value;
                    Attribute attribute = AttributeFactory.newAttribute(tag);
                    if (canControlAnonymizing(tag)) {
                        attribute.addValue(value);
                        attributeList.put(attribute);
                    }
                }
            }
        }
        catch (UMROException e) {
            Log.get().warning("Unable to parse list of default attributes to anonymize.  User will have to supply them manually.");
        }
        catch (DicomException e) {
            Log.get().warning(this.getClass().getName() + ".getAnonymizingReplacementList : Failed to construct DICOM Attribute: " + e);
        }
        return attributeList;
    }


    /**
     * Determine the trust store file to use and set it up.
     * First look at the javax.net.ssl.trustStore system property,
     * and if it is pointing to a readable file then use it.
     * If it is not set, then look through the javax.net.ssl.trustStore
     * list in the configuration file and use the first one that
     * points to a readable file.
     * 
     * @return The file to be used.
     */
    public File setupTrustStore() {
        NodeList nodeList = null;    
        try {
            nodeList = XML.getMultipleNodes(config, "/DicomClientConfig/javax.net.ssl.trustStore/text()");
        }
        catch (UMROException e) {
            Log.get().warning("Unable to parse list of javax.net.ssl.trustStore.  You will not be able to communicate with the DICOM service.  Details: " + e);
        }
        File file = null;
        try {
            file = Util.setupTrustStore(nodeList).getKeystoreFile();
        }
        catch (Exception e) {
            file = null;
        }
        if (file == null) {
            Log.get().warning("Unable to read trustStore file.  You will not be able to communicate with the DICOM service.");
        }
        else {
            System.setProperty("javax.net.ssl.trustStore", file.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        }
        return file;
    }


    public ArrayList<PrivateTag> getPrivateTagList() {
        ArrayList<PrivateTag> tagList = new ArrayList<PrivateTag>();
        try {
            NodeList nodeList = XML.getMultipleNodes(config, "/DicomClientConfig/PrivateTagList/*");
            for (int n = 0; n < nodeList.getLength(); n++) {
                tagList.add(new PrivateTag(nodeList.item(n)));
            }
        }
        catch (UMROException e) {
            Log.get().warning("Problem interpreting custom tags: " + e);
        }
        catch (DicomException e) {
            Log.get().warning("DICOM Problem interpreting custom tags: " + e);
        }
        return tagList;
    }





    public static ClientConfig getInstance() {
        if (clientConfig == null) {
            clientConfig = new ClientConfig();
        }
        return clientConfig;
    }
}
