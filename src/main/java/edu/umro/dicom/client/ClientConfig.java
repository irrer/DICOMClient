package edu.umro.dicom.client;

import java.io.File;
import java.util.logging.Level;

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

import edu.umro.dicom.common.Util;
import edu.umro.util.Log;
import edu.umro.util.UMROException;
import edu.umro.util.Utility;
import edu.umro.util.XML;

/**
 * Get the configuration information.
 * 
 * @author irrer
 *
 */
public class ClientConfig {

    /** Name of configuration file. */
    private static final String CONFIG_FILE_NAME = "DicomClientConfig.xml";

    /** List of all possible configuration files. */
    private static final String[] CONFIG_FILE_LIST = {
        CONFIG_FILE_NAME,
        "src\\main\\resources\\" + CONFIG_FILE_NAME
    };

    /** For convenience and optimization by re-use. */
    private static final DicomDictionary DICOM_DICTIONARY = new DicomDictionary();

    /** Instance of this object. */
    private static ClientConfig clientConfig = null;

    /** Configuration information from file. */
    private Document config = null;


    /**
     * Read in the configuration for the client from the configuration file.  Try
     * all files on the list and use the first one that parses.
     */
    private void parseConfigFile() {
        for (String configFileName : CONFIG_FILE_LIST) {
            try {
                config = XML.parseToDocument(Utility.readFile(new File(configFileName)));;
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
     * Only let user control certain types of attributes.  It does not make
     * sense to manually control values of UIDs, and sequence attributes do
     * not have values.
     * 
     * @param tag Tag to check.
     * 
     * @return
     */
    private boolean canControlAnonymizing(AttributeTag tag) {
        return (!ValueRepresentation.isSequenceVR(DICOM_DICTIONARY.getValueRepresentationFromTag(tag)));
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
                    AttributeTag tag = DICOM_DICTIONARY.getTagFromName(tagText);
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
            Log.get().warning("Unable to parse list of javax.net.ssl.trustStore.  You will not be able to communicate with the DICOM service.");
        }
        File file = Util.setupTrustStore(nodeList).getKeystoreFile();
        if (file != null) {
            System.setProperty("javax.net.ssl.trustStore", file.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        }
        return file;
    }


    public static ClientConfig getInstance() {
        if (clientConfig == null) {
            clientConfig = new ClientConfig();
        }
        return clientConfig;
    }
}
