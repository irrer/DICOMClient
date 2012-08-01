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

import java.io.File;

import org.w3c.dom.Node;

import edu.umro.util.UMROException;
import edu.umro.util.XML;

/**
 * Represent an SSL certificate java trust store.
 * 
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
public class TrustStore {

    /** File containing keys */
    private File keystoreFile = null;

    /** Password for key. */
    private String keyPassword = null;

    /** Password for file. */
    private String storepass = null;

    public String getKeyPassword() {
        return keyPassword;
    }

    public String getStorepass() {
        return storepass;
    }

    public File getKeystoreFile() {
        return keystoreFile;
    }


    /**
     * Construct from system properties.
     */
    public TrustStore() {
        String fileName = System.getProperty("javax.net.ssl.trustStore");
        keystoreFile = (fileName == null) ? null : new File(fileName);
        keyPassword = System.getProperty("javax.net.ssl.keyStorePassword");
        storepass = System.getProperty("javax.net.ssl.trustStorePassword");
    }


    /**
     * Construct from a DOM node.
     * 
     * @param node Contains configuration information.
     */
    public TrustStore(Node node) {
        String fileName = node.getNodeValue();
        if (fileName != null) {
            keystoreFile = new File(fileName);
        }

        try {
            storepass = XML.getValue(node, "../@storepass");
        }
        catch (UMROException e) {
            storepass = null;
        }

        try {
            keyPassword = XML.getValue(node, "../@keyPassword");
        }
        catch (UMROException e) {
            storepass = null;
        }
    }


    /**
     * Construct from parameters.
     * 
     * @param keystoreFile File containing key
     * 
     * @param keyPassword Password for key
     * 
     * @param storepass Password for file
     */
    public TrustStore(File keystoreFile, String keyPassword, String storepass) {
        this.keystoreFile = keystoreFile;
        this.keyPassword = keyPassword;
        this.storepass = storepass;
    }


    /**
     * Determine if this object is viable to use, requiring the file to
     * be readable.
     * 
     * @return True if viable.
     */
    public boolean viable() {
        return (keystoreFile != null) && keystoreFile.canRead();
    }


    @Override
    public String toString() {
        return
        "File: " + (keystoreFile == null ? "null" : keystoreFile.getAbsoluteFile()) +
        "    keyPassword: " + keyPassword + 
        "    storepass: " + storepass;
    }
}
