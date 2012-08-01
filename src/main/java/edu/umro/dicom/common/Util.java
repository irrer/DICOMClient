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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.w3c.dom.NodeList;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DateAttribute;
import com.pixelmed.dicom.PersonNameAttribute;
import com.pixelmed.dicom.TimeAttribute;

import edu.umro.dicom.common.TrustStore;
import edu.umro.util.JarInfo;
import edu.umro.util.Log;

/**
 * General purpose methods.
 * 
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
public class Util {

    static public final String MEDIA_TYPE_PARAMETER_NAME = "media_type";

    static public final String USER_ID_PARAMETER_NAME = "user_id";

    /** Number of bytes in a single buffer used for
     * transferring data to and from server. */
    private final static int TRANSFER_BUFFER_SIZE = 64 * 1024;

    /** The root UID which is used to prefix files constructed by the University of Michigan. */
    public static final String UMRO_ROOT_GUID = "1.3.6.1.4.1.22361";

    /** For getting values from the MANIFEST.MF file in the jar. */
    private static JarInfo jarInfo = null;

    /** The MAC address of this machine.  This is used to make
     * the GUID unique across machines.
     */
    private static long macAddress = 0;

    /** Flag to determine whether MAC address has been initialized. */
    private static boolean initialized = false;

    /** DICOM manufacturer name. */
    public static final String UMRO_MANUFACTURER = "Univ of Mich Radiation Oncology";

    /** DICOM postal address. */
    public static final String UMRO_POSTAL_ADDRESS = "University of Michigan Health System, 1500 E. Medical Center Drive Ann Arbor, MI 48109";

    public static String getHtmlHead(String title) {
        return 
        "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>\n" +
        "<html xmlns='http://www.w3.org/1999/xhtml'>\n" +
        "<head>\n" +
        "    <meta http-equiv='Content-Type' content='text/html; charset=iso-8859-1' />\n" +
        "    <title>" + title + "</title>\n" +
        "    <link rel='stylesheet' type='text/css' href='/example.css'/>\n" +
        "    <meta name='author' content='Joost de Valk, http://www.joostdevalk.nl/' />\n" +
        "    <link href='http://www.joostdevalk.nl/' rev='made' />\n" +
        "    <script type='text/javascript' src='/sortable_us.js'></script>\n" +
        "</head>\n" +
        "\n" +
        "<br><a href='/'>Home</a></br>\n" +
        "\n" +
        "<body>\n" +
        "\n";
    }   


    /**
     * Get the list of parameters and their values (foo=bar parts of URL).
     * Set all parameter names to lower case.
     * 
     * @param request Request from client.
     * 
     * @return list of parameters and their values.
     */
    public static HashMap<String, String> getParameterList(Request request) {
        HashMap<String, String> parameterList = new HashMap<String, String>();
        Form form = request.getResourceRef().getQueryAsForm();
        for (Parameter parameter : form) {
            String name = parameter.getName().toLowerCase();
            parameterList.put(name, parameter.getValue());
        }
        return parameterList;
    }



    /**
     * Copy the input stream to the output stream.
     *
     * @param in Data source.
     *
     * @param out Data sink.
     * 
     * @return Total length of transfer.
     *
     * @throws IOException
     */
    public static long inToOut(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];
        int length = 1;
        long totalLength = 0;
        while ((length = in.read(buffer)) >= 0) {
            totalLength += length;
            out.write(buffer, 0, length);
        }
        return totalLength;
    }


    /**
     * Get the list of names of the types of media that the client will accept.
     * Also, if a media type is found in the parameter list, then remove it to
     * indicate that the parameter has been recognized by it's name.  It
     * is up to the caller to determine whether or not the media type names are
     * valid and what to do with them.  If present, the media type specified in
     * the URL is listed first. 
     * 
     * @param request Request from client.
     * 
     * @param parameterList List of URL parameters.
     * 
     * @return 
     */
    public static ArrayList<String> getMediaTypeNameList(Request request, HashMap<String, String> parameterList) {
        ArrayList<String> mediaTypeList = new ArrayList<String>();
        String value = parameterList.get(MEDIA_TYPE_PARAMETER_NAME);
        if (value != null) {
            mediaTypeList.add(value);
            parameterList.remove(MEDIA_TYPE_PARAMETER_NAME);
        }

        for (Preference<MediaType> pmt : request.getClientInfo().getAcceptedMediaTypes()) {
            String name = pmt.getMetadata().getName();
            if (!mediaTypeList.contains(name)) {
                mediaTypeList.add(name);
            }
        }

        return mediaTypeList;
    }

    public static String getUserId(Request request, Response response, HashMap<String, String> parameterList) {
        String userId = parameterList.get(USER_ID_PARAMETER_NAME);
        if (userId != null) {
            parameterList.remove(USER_ID_PARAMETER_NAME);
        }
        return userId;
    }


    /**
     * Get an attribute value, or null if anything goes wrong.  Also, if there is a value,
     * replace all null characters with blanks, and trim whitespace.
     * 
     * @param attributeList Get it from this list.
     * 
     * @param tag The attribute to get.
     * 
     * @return The value of the attribute as a string, or null if either the
     * value is not on the list or the attribute does not have a value.
     */
    public static String getAttributeValue(AttributeList attributeList, AttributeTag tag) {
        Attribute attribute = attributeList.get(tag);
        String value = (attribute == null) ? null : attribute.getSingleStringValueOrNull();
        if (value != null) {
            value = value.replace('\0', ' ').trim();
            byte[] vr = attribute.getVR();

            Class<?> vrClass = AttributeFactory.getClassOfAttributeFromValueRepresentation(tag, vr, true, TRANSFER_BUFFER_SIZE, true);

            if (vrClass.equals(TimeAttribute.class)) {
                String text = value.replaceFirst("\\..*", "");
                Date date = null;
                try {
                    date = new Date(Long.parseLong(text) * 1000);
                }
                catch (Exception ex) {
                    // if there is a badly formatted time, then just return the unprocessed value.
                    return value;
                }
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                return timeFormat.format(date);
            }

            if (vrClass.equals(DateAttribute.class)) {
                String text = value.replaceFirst("\\..*", "");
                ParsePosition parsePosition = new ParsePosition(0);
                SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");
                Date date = dateParse.parse(text, parsePosition);
                if (date == null) {
                    // if there is a badly formatted date, then just return the value.
                    return value;
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd, yyyy");
                return dateFormat.format(date);

            }

            if (vrClass.equals(PersonNameAttribute.class)) {
                return value.replaceAll("\\^", " ").replaceAll("   *", " ");
            }
        }
        return value;
    }


    /**
     * Generate a DICOM compliant GUID using the UMRO root.
     *
     * @return A DICOM compliant GUID using the UMRO root.
     * @throws SocketException 
     * @throws UnknownHostException 
     */
    public static synchronized String getUID() throws UnknownHostException, SocketException {

        // Initialized MAC address if necessary.
        if (!initialized) {
            initialized = true;
            macAddress = UMROMACAddress.getMACAddress();
            macAddress = Math.abs(macAddress);
        }

        // Use standard class to get unique values.
        String guidText = new UID().toString();

        StringTokenizer st = new StringTokenizer(guidText, ":");

        int unique = Math.abs(Integer.valueOf(st.nextToken(), 16).intValue());
        long time = Math.abs(Long.valueOf(st.nextToken(), 16).longValue());
        // why add 0x8000 ? because usually starts at -8000, which wastes 4 digits
        int count = Math
        .abs(Short.valueOf(st.nextToken(), 16).shortValue() + 0x8000);

        // concatenate values to make it into a DICOM GUID.
        String guid = UMRO_ROOT_GUID + macAddress + "." + unique + "." + time
        + "." + count;

        return guid;
    }


    /**
     * Determine the trust store file to use and set it up.
     * First look at the javax.net.ssl.trustStore system property,
     * and if it is pointing to a readable file then use it.
     * If it is not set, then look through the javax.net.ssl.trustStore
     * list in the configuration file and use the first one that
     * points to a readable file.
     * 
     * @param nodeList List of nodes containing file names.
     * @return File to be used for javax.net.ssl.trustStore.  Null indicates failure.
     */
    public static synchronized TrustStore setupTrustStore(NodeList nodeList) {
        TrustStore trustStore = new TrustStore();

        for (int ts = 0; ts < nodeList.getLength() && (!trustStore.viable()); ts++) {
            trustStore = new TrustStore(nodeList.item(ts));
        }

        if (!trustStore.viable()) {
            Log.get().warning("Unable to find a javax.net.ssl.trustStore file");            
        }
        else {
            Log.get().info("Using file " + trustStore.getKeystoreFile().getAbsolutePath() + " for the javax.net.ssl.trustStore file.");
        }
        return trustStore;
    }


    /**
     * Get parameter value from jar of the given key.  If there is
     * an error, return "unknown" instead.
     * 
     * @param key Tag for parameter.
     * 
     * @return
     */
    private static String getJarInfo(String key) {
        if (jarInfo == null) {
            jarInfo = new JarInfo(Util.class);
        }
        return jarInfo.getMainManifestValue(key, "unknown");
    }


    /**
     * Get the vendor organization of this application.
     * 
     * @return Organization or "unknown".
     */
    public static String getImplementationVendor() {
        return getJarInfo("Implementation-Vendor");
    }


    /**
     * Get the version of this application.
     * 
     * @return Version or "unknown".
     */
    public static String getImplementationVersion() {
        return getJarInfo("Implementation-Version");
    }


    /**
     * Get the build date of this application.
     * 
     * @return Version or "unknown".
     */
    public static String getBuildDate() {
        return getJarInfo("BuildDate");
    }


    /**
     * Get the build date of this application.
     * 
     * @return Version or "unknown".
     */
    public static String getBuiltBy() {
        return getJarInfo("Built-By");
    }
}
