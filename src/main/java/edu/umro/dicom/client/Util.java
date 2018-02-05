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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DateAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.DicomOutputStream;
import com.pixelmed.dicom.PersonNameAttribute;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TimeAttribute;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.dicom.XMLRepresentationOfDicomObjectFactory;
import com.pixelmed.dicom.AttributeList.ReadTerminationStrategy;
import com.pixelmed.display.ConsumerFormatImageMaker;

import edu.umro.util.JarInfo;
import edu.umro.util.Log;
import edu.umro.util.OpSys;
import edu.umro.util.UMROException;
import edu.umro.util.Utility;
import edu.umro.util.XML;

/**
 * General purpose methods.
 * 
 * @author Jim Irrer irrer@umich.edu
 * 
 */
public class Util {

    /**
     * Number of bytes in a single buffer used for
     * transferring data to and from server.
     */
    private final static int TRANSFER_BUFFER_SIZE = 64 * 1024;

    /** The root UID which is used to prefix files constructed by the University of Michigan. */
    public static final String UMRO_ROOT_UID = "1.3.6.1.4.1.22361";

    /** The name of the System property to define to enable testing. */
    public static final String TESTING_PROPERTY = "TESTING";

    /** Default transfer syntax for serializing DICOM files. */
    public static final String DEFAULT_TRANSFER_SYNTAX = TransferSyntax.ImplicitVRLittleEndian;
    public static final String DEFAULT_STORAGE_SYNTAX = TransferSyntax.ExplicitVRLittleEndian;
    // public static final String DEFAULT_TRANSFER_SYNTAX = TransferSyntax.ExplicitVRLittleEndian;

    public static final Random random = new Random();

    /** For getting values from the MANIFEST.MF file in the jar. */
    private static JarInfo jarInfo = null;

    /**
     * The MAC address of this machine. This is used to make
     * the UID unique across machines.
     */
    private static long macAddress = 0;

    /** Flag to determine whether MAC address has been initialized. */
    private static boolean initialized = false;

    /** Root UID used as a prefix when building UIDs. */
    private static String rootUid = null;

    /** DICOM postal address. */
    public static final String UMRO_POSTAL_ADDRESS = "University of Michigan Health System, 1500 E. Medical Center Drive Ann Arbor, MI 48109";

    /** Suffixes used when writing files. */
    public static final String TEXT_SUFFIX = ".TXT";
    public static final String PNG_SUFFIX = ".PNG";
    public static final String XML_SUFFIX = ".XML";
    public static final String DICOM_SUFFIX = ".DCM";

    public static final String[] allSuffixes = { TEXT_SUFFIX, PNG_SUFFIX, XML_SUFFIX, DICOM_SUFFIX };

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private static boolean testing() {
        return System.getProperties().contains(TESTING_PROPERTY);
    }

    public static File removeSuffix(File file) {
        String name = file.getAbsolutePath();
        for (String s : allSuffixes) {
            if (name.toLowerCase().endsWith(s.toLowerCase())) {
                name = name.substring(0, name.length() - s.length());
                break;
            }
        }
        return new File(name);
    }

    public static File addSuffix(File file, String suffix) {
        return new File(removeSuffix(file).getAbsolutePath() + suffix);
    }

    /**
     * Get an attribute value, or null if anything goes wrong. Also, if there is a value,
     * replace all null characters with blanks, and trim whitespace.
     * 
     * @param attributeList
     *            Get it from this list.
     * 
     * @param tag
     *            The attribute to get.
     * 
     * @return The value of the attribute as a string, or null if either the
     *         value is not on the list or the attribute does not have a value.
     */
    public static String getAttributeValue(AttributeList attributeList, AttributeTag tag) {
        Attribute attribute = attributeList.get(tag);
        String value = (attribute == null) ? null : attribute.getSingleStringValueOrNull();
        if (value != null) {
            value = (new String(value)).replace('\0', ' ').trim(); // try to plug memory leak
            byte[] vr = attribute.getVR();

            Class<?> vrClass = AttributeFactory.getClassOfAttributeFromValueRepresentation(tag, vr, true, TRANSFER_BUFFER_SIZE, true, false);

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
                Date date = dateFormat.parse(text, parsePosition);
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
     * Initialize UID parameters.
     */
    private static void initialize() {
        if (!initialized) {
            initialized = true;
            if (testing()) {
                rootUid = "0.0.0.0.0.0.0.0.0.0";
                macAddress = 0;
            }
            else {
                try {
                    rootUid = ClientConfig.getInstance().getRootUid();
                }
                catch (Exception e) {
                    Log.get().severe("Unable to get root UID from configuration.  Instead using Univ Mich Hosp UID: " + UMRO_ROOT_UID);
                    rootUid = UMRO_ROOT_UID;
                }

                try {
                    macAddress = OpSys.getMACAddress();
                }
                catch (Exception e) {
                    macAddress = Long.parseLong(OpSys.getHostIPAddress().replace('.', 'x').replaceAll("x", ""));
                    // if localhost (127.0.0.1) is returned, then try something
                    // random instead. The risk is
                    // that the same 2^63 random number will be returned twice,
                    // but it is a low risk.
                    if (macAddress == 127001) {
                        macAddress = new Random().nextLong();
                    }
                }
                macAddress = Math.abs(macAddress);
            }
        }
    }

    /**
     * Generate a DICOM compliant UID using the UMRO root.
     * 
     * @return A DICOM compliant UID using the UMRO root.
     * @throws SocketException
     * @throws UnknownHostException
     */
    public static synchronized String getUID() {

        initialize();

        if (testing()) {
            macAddress++;
            String text = "000000000000000000000000" + macAddress;
            return rootUid + "." + text.substring(text.length() - 20);
        }
        // Use standard class to get unique values.
        String uidText = new UID().toString();

        StringTokenizer st = new StringTokenizer(uidText, ":");

        int unique = Math.abs(Integer.valueOf(st.nextToken(), 16).intValue());
        long time = Math.abs(Long.valueOf(st.nextToken(), 16).longValue());
        // why add 0x8000 ? because usually starts at -8000, which wastes 4
        // digits
        int count = Math.abs(Short.valueOf(st.nextToken(), 16).shortValue() + 0x8000);

        // concatenate values to make it into a DICOM UID.
        String uid = rootUid + macAddress + "." + unique + "." + time + "." + count;

        return uid;
    }

    /**
     * Get parameter value from jar of the given key. If there is
     * an error, return "unknown" instead.
     * 
     * @param key
     *            Tag for parameter.
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

    /**
     * Make a new copy of an attribute list, not sharing any data with the original.
     * 
     * @param source
     *            List to copy.
     * 
     * @return Copy of list.
     * 
     * @throws IOException
     * 
     * @throws DicomException
     */
    public static AttributeList cloneTopLevelAttributeList(AttributeList source) throws IOException, DicomException {
        AttributeList dest = new AttributeList();

        Attribute transferSyntaxAttr = source.get(TagFromName.TransferSyntaxUID);
        String transferSyntax = TransferSyntax.ExplicitVRLittleEndian; // DEFAULT_TRANSFER_SYNTAX;
        if (transferSyntaxAttr != null) transferSyntax = transferSyntaxAttr.getSingleStringValueOrDefault(DEFAULT_TRANSFER_SYNTAX);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DicomOutputStream dicomOutputStream = new DicomOutputStream(byteArrayOutputStream, transferSyntax, transferSyntax);
        source.write(dicomOutputStream);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        dest.read(new DicomInputStream(byteArrayInputStream));

        return dest;
    }

    /**
     * Make a new copy of an attribute list, not sharing any data with the original.
     * 
     * @param source
     *            List to copy.
     * 
     * @return Copy of list.
     * 
     * @throws IOException
     * 
     * @throws DicomException
     */

    public static AttributeList cloneAttributeList(AttributeList source) throws IOException, DicomException {
        AttributeList virtualList = new AttributeList();
        {
            Attribute sopInstanceUID = AttributeFactory.newAttribute(TagFromName.SOPInstanceUID);
            sopInstanceUID.addValue(Util.getUID());
            virtualList.put(sopInstanceUID);
        }

        // Make a top level sequence as a container. Any type of sequence will
        // do, this is relatively generic.
        SequenceAttribute contentSequence = new SequenceAttribute(TagFromName.ContentSequence);
        virtualList.put(contentSequence);

        contentSequence.addItem(source);

        AttributeList newAttributeList = cloneTopLevelAttributeList(virtualList);

        AttributeList dest = ((SequenceAttribute) newAttributeList.get(TagFromName.ContentSequence)).getItem(0).getAttributeList();
        return dest;
    }

    public static Attribute cloneAttribute(Attribute attribute) {
        AttributeList attributeList = new AttributeList();
        attributeList.put(attribute);
        try {
            return cloneAttributeList(attributeList).get(attribute.getTag());
        }
        catch (IOException e) {
            Log.get().severe("cloneAttribute Unexpected IOException: " + Log.fmtEx(e));
        }
        catch (DicomException e) {
            Log.get().severe("cloneAttribute Unexpected DicomException: " + Log.fmtEx(e));
        }
        return null;
    }

    /**
     * Determine if the given UID is syntactically valid, which
     * means that it must start and end with a digit and contain
     * only digits and periods (.) .
     * 
     * Example of valid UID: 98.09877.897.908.9
     * 
     * @param uid
     * @return
     */
    public static boolean isValidUid(String uid) {
        int len = uid.length();
        boolean ok = (uid.trim().length() > 0) &&
                uid.matches("[0-9\\.]*") &&
                uid.substring(0, 1).matches("[0-9]") &&
                uid.substring(len - 1, len).matches("[0-9]");
        return ok;

    }

    /**
     * Write the given attribute list to a PNG file. If the attribute list does
     * not describe an image file, then do nothing.
     * 
     * @param attributeList
     *            DICOM source.
     * @param pngFile
     *            PNG image file to create.
     */
    public static void writePngFile(AttributeList attributeList, File pngFile) throws DicomException, IOException {
        if (SOPClass.isImageStorage(Attribute.getSingleStringValueOrEmptyString(attributeList, TagFromName.SOPClassUID))) {
            pngFile.delete();
            BufferedImage image = ConsumerFormatImageMaker.makeEightBitImage(attributeList);
            ImageIO.write(image, "png", pngFile);
            Log.get().info("Wrote image file " + pngFile.getAbsolutePath());
        }
    }

    /**
     * Write the given attribute list to a text file as a user would see it in
     * the text previewer.
     * 
     * @param attributeList
     *            DICOM source.
     * 
     * @param textFile
     *            Text file to create.
     * 
     * @throws IOException
     * @throws UMROException
     */
    public static void writeTextFile(AttributeList attributeList, File textFile) throws IOException, UMROException {
        StringBuffer text = new StringBuffer();
        DicomClient.getInstance().getPreview().addTextAttributes(attributeList, text, 0, null);
        textFile.delete();
        textFile.createNewFile();
        Utility.writeFile(textFile, text.toString().getBytes());
        Log.get().info("Wrote text file " + textFile.getAbsolutePath());
    }

    /**
     * Write the given attribute list to a text file as a user would see it in the text previewer.
     * 
     * @param attributeList
     *            DICOM source.
     * 
     * @param xmlFile
     *            XML file to create.
     * 
     * @throws IOException
     * @throws UMROException
     * @throws ParserConfigurationException
     */
    public static void writeXmlFile(AttributeList attributeList, File xmlFile) throws IOException, UMROException, ParserConfigurationException {
        Document document = new XMLRepresentationOfDicomObjectFactory().getDocument(attributeList);
        if (DicomClient.getReplaceControlCharacters()) {
            XML.replaceControlCharacters(document, ' ');
        }
        String xmlText = XML.domToString(document);
        xmlFile.delete();
        Utility.writeFile(xmlFile, xmlText.getBytes());
        Log.get().info("Wrote xml file " + xmlFile.getAbsolutePath());
    }

    /**
     * Try to warn the user about using up memory.
     * 
     * @param file
     *            Incoming DICOM file.
     */
    private static void checkMemory(File file) {
        try {
            long fileLength = file.length();
            if (fileLength > Runtime.getRuntime().freeMemory()) {
                Runtime.getRuntime().gc(); // take a shot at freeing memory
            }
            if (fileLength > Runtime.getRuntime().freeMemory()) {
                DicomClient.getInstance().showMessage("Extremely large file " + file.getAbsolutePath() + " of size " + fileLength + " might need more memory than is available.");
            }
        }
        catch (Throwable t) {
            DicomClient.getInstance().showMessage("Problem reading file (partially read) " + file.getAbsolutePath() + " : " + t);
            Runtime.getRuntime().gc();
        }

    }

    /**
     * Read a DICOM file after checking that there is sufficient heap space to read it.
     * 
     * @param File
     *            to read
     * @return Parsed DICOM.
     * @throws IOException
     * @throws DicomException
     */
    public static AttributeList readDicomFile(File file) throws IOException, DicomException {

        class ReadTermStrat implements ReadTerminationStrategy {
            public AttributeList latest = null;

            @Override
            public boolean terminate(AttributeList attributeList, AttributeTag tag, long byteOffset) {
                latest = attributeList;
                return false;
            }
        }

        checkMemory(file);

        AttributeList attributeList = new AttributeList();

        ReadTermStrat rts = new ReadTermStrat();
        try {
            attributeList.read(file, rts);
        }
        catch (IOException e) {
            if (rts.latest != null) {
                DicomClient.getInstance().showMessage("Warning!  Problem reading DICOM file " + file.getAbsolutePath() + " : " + e.getMessage());
                return rts.latest;
            }
            else
                throw e;
        }
        catch (DicomException e) {
            if (rts.latest != null) {
                DicomClient.getInstance().showMessage("Warning!  DICOM file " + file.getAbsolutePath() + " has problems: " + e.getMessage());
                return rts.latest;
            }
            else
                throw e;
        }
        return attributeList;
    }

    /**
     * Exit with a status that indicates that everything is ok.
     */
    public static void exitSuccess() {
        System.exit(0);
    }

    /**
     * Exit with a status that indicates failure but does not indicate that
     * too much memory was requested of the JVM with the -Xmx option. The
     * exit status is not supposed to reflect a standard error.
     */
    public static void exitFail() {
        System.exit(7);
    }

    /**
     * Replace all characters in a name that are not supported by the Windows file system.
     * 
     * @param name:
     *            File name
     * 
     * @param replacement:
     *            Character to be used instead of original.
     *            This will work for *nix systems too. It will replace some characters that would be
     *            allowed in *nix, but using them is generally not a good idea anyway.
     */
    public static String replaceInvalidFileNameCharacters(String name, char replacement) {
        byte[] original = name.getBytes();
        byte rep = (byte) replacement;

        byte[] invalidChars = ("\\/:*\"<>").getBytes();

        byte[] newName = new String(name).getBytes();

        for (int c = 0; c < name.length(); c++) {
            byte orig = original[c];
            for (int i = 0; i < invalidChars.length; i++) {
                if (orig == invalidChars[i]) {
                    newName[c] = rep;
                }
            }
            if ((orig < 32) || (orig > 127)) {
                newName[c] = rep;
            }

        }
        return new String(newName);

    }

    /**
     * Determine if the attribute list is an image.
     * 
     * @param attributeList
     *            Used as input.
     * 
     * @return True if image.
     */
    public static boolean isImageStorage(AttributeList attributeList) {
        String sopClassUID = Attribute.getSingleStringValueOrEmptyString(attributeList, TagFromName.SOPClassUID);
        return SOPClass.isImageStorage(sopClassUID);
    }
}
