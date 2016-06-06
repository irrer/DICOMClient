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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import edu.umro.util.JarInfo;
import edu.umro.util.Log;
import edu.umro.util.Utility;
import edu.umro.util.XML;

/**
 * Get the PACS configuration information.
 * 
 * @author Jim Irrer irrer@umich.edu
 * 
 */
public class PACSConfig {

    /** Name of configuration file. */
    private static final String CONFIG_FILE_NAME = "PACSConfig.xml";

    private volatile static boolean configHasBeenTried = false;

    private Document jarConf() {
        try {
            JarInfo jarInfo = new JarInfo(this.getClass());
            File jarFile = new File(jarInfo.getFullJarFilePath());
            File dir = jarFile.getParentFile();
            File configFile = new File(dir, CONFIG_FILE_NAME);
            Document config = XML.parseToDocument(Utility.readFile(configFile));
            if (config != null) {
                Log.get().info("Using configuration file " + configFile.getAbsolutePath());
            }

            return config;
        }
        catch (Exception e) {
            return null;
        }
    }

    /** List of all possible configuration files. */
    private static final String[] CONFIG_FILE_LIST = {
            System.getProperty("pacsconfig"),
            CONFIG_FILE_NAME,
            "src\\main\\resources\\" + CONFIG_FILE_NAME
    };

    /** Instance of this object. */
    private volatile static PACSConfig pacsConfig = null;

    private PACS identity = null;

    final private ArrayList<PACS> pacsList;

    /**
     * Read in the configuration for the client from the configuration file. Try
     * all files on the list and use the first one that parses.
     */
    private void parseConfigFile() {
        Document config = null;
        for (String configFileName : CONFIG_FILE_LIST) {
            try {
                Log.get().info("Trying configuration file " + (new File(configFileName)).getAbsolutePath());
                config = XML.parseToDocument(Utility.readFile(new File(configFileName)));
                Log.get().info("was able to parse PACS config file " + configFileName);
            }
            catch (Exception e) {
                ;
            }

            if (config != null) {
                Log.get().info("Using configuration file " + (new File(configFileName)).getAbsolutePath());
                break;
            }
        }

        if (config == null) {
            config = jarConf();
        }

        if (config != null) {
            try {
                identity = new PACS(XML.getSingleNode(config, "/PacsConfiguration/Identity/PACS"));

                NodeList nodeList = XML.getMultipleNodes(config, "/PacsConfiguration/PacsList/PACS");
                for (int n = 0; n < nodeList.getLength(); n++) {
                    PACS pacs = new PACS(nodeList.item(n));
                    pacsList.add(pacs);
                }
                StringBuffer msg = new StringBuffer("List of known PACS");
                for (PACS pacs : pacsList) {
                    msg.append("\n    " + pacs);
                }
                msg.append("\n");
                Log.get().info(msg.toString());
            }
            catch (Exception e) {
                Log.get().warning("Unable to extract PACS list from file.  Will not be able to perform uploads.  Error: " + e);
            }
        }

        if (config == null) {
            String msg = "Unable to read and parse any PACS configuration file of: " + CONFIG_FILE_LIST;
            Log.get().severe(msg);
            // DicomClient.getInstance().showMessage(msg);
        }
    }

    /**
     * Construct a configuration object.
     */
    private PACSConfig() {
        pacsList = new ArrayList<PACS>();
        parseConfigFile();
    }

    /**
     * Get the information that indicates how this program identifies itself to other PACS devices
     * 
     * @return Identity of this DICOM program.
     */
    public PACS getIdentity() {
        return identity;
    }

    
    public String getMyDicomAETitle() {
        return identity.aeTitle;
    }


    /**
     * Get the list of PACS we know about.
     * 
     * @return The list of PACS we know about
     */
    public ArrayList<PACS> getPacsList() {
        return pacsList;
    }

    /**
     * Get the common instance of this configuration.
     * 
     * @return This configuration.
     */
    public static PACSConfig getInstance() {
        if (configHasBeenTried)
            return pacsConfig;
        else {
            configHasBeenTried = true;
            if (pacsConfig == null) {
                pacsConfig = new PACSConfig();
            }
            return pacsConfig;
        }
    }
}
