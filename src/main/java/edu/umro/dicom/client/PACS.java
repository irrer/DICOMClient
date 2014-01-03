package edu.umro.dicom.client;

import org.w3c.dom.Node;

import edu.umro.util.XML;

/*
 * Copyright 2013 Regents of the University of Michigan
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


/**
 * Represent a DICOM PACS
 * 
 * @author irrer
 *
 */
public class PACS {

    public static enum Compression {
        UN,
        J1,
        J2,
        J3,
        J4,
        J5,
        J6,
        N1,
        N2,
        N3,
        N4;

        /**
         * Given the text representation of a compression, return the
         * corresponding compression.  This is case insensitive and
         * trimmed of whitespace.
         * 
         * @param text Text version of compression.
         * 
         * @return Matching compression, or null if not valid.
         */
        public static Compression textToCompression(String text) {
            for (Compression c : Compression.values()) {
                if (text.trim().equalsIgnoreCase(c.toString())) {
                    return c;
                }
            }
            return null;
        }
    };

    /** AE Title */
    public final String aeTitle;

    /** Host name or IP address. */
    public final String host;

    /** Port number. */
    public final int    port;

    /** Type of compression. */
    public final Compression compression;

    /**
     * Construct a new PACS
     * 
     * @param aeTitle
     * @param host
     * @param port
     * @param compression
     */
    public PACS (String aeTitle, String host, int port, Compression compression) {
        this.aeTitle = aeTitle;
        this.host = host;
        this.port = port;
        this.compression = compression;
    }

    /**
     * Construct a new PACS defaulting to no compression
     * 
     * @param aeTitle
     * @param host
     * @param port
     */
    public PACS (String aeTitle, String host, int port) {
        this.aeTitle = aeTitle;
        this.host = host;
        this.port = port;
        this.compression = Compression.UN;
    }
    
    /**
     * Construct a PACS from an XML node.
     * 
     * @param node Specifies PACS.
     */
    public PACS(Node node) {
        aeTitle = XML.getAttributeValue(node, "AETitle");
        host = XML.getAttributeValue(node, "Host");
        String portText = XML.getAttributeValue(node, "Port");
        port = Integer.parseInt(portText);
        this.compression = Compression.UN;
    }

    @Override
    public String toString() {
        return aeTitle + " " + host + ":" + port + (compression == null ? "" : (" compr:" + compression));
    }
}
