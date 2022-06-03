package edu.umro.dicom.client;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.*;

import java.io.*;

/**
 * Dump DICOM files with the usual tag, VR, attribute name, attribute value data, but also
 * include the corresponding hex bytes and their offset within the file.
 * <p>
 * If there is a problem reading the file (e.g. bad DICOM), then the progress captured up
 * to that point is reported.
 * <p>
 * Note that Sequence attributes are not included because the implementation of
 * {@link com.pixelmed.dicom.AttributeList.ReadTerminationStrategy} does not support them.
 * Supporting them would either require changing the Pixelmed library or java byte code
 * injection.
 */
public class DicomDump {

    CustomDictionary dict = CustomDictionary.getInstance();

    /**
     * Used to capture read progress.  Always returns false.
     */
    public class ReadStrategy implements AttributeList.ReadTerminationStrategy {

        byte[] binary;

        StringBuffer text;

        public ReadStrategy(byte[] bin, StringBuffer txt) {
            this.binary = bin;
            this.text = txt;
        }

        private long previousOffset = 0;


        private AttributeTag previousTag = null;

        private String offsetToText(long offset) {
            return String.format("%08d", offset) + ": ";
        }

        /**
         * Convert the given segment of bytes to user-friendly hex.
         *
         * @param begin First byte.
         * @param end   Last byte.
         * @return Readable text.
         */
        private String bytesToText(long begin, long end) {
            StringBuilder buf = new StringBuilder();
            int max = 512;

            if ((end - begin) > max) {
                end = begin + max;
            }

            for (long i = begin; i < end; i++) {
                buf.append(String.format(" %02x", binary[(int) i]));
            }
            return buf.toString();
        }

        private String tagToText(AttributeTag tag) {
            @SuppressWarnings("UnnecessaryLocalVariable") String text = String.format("%04x", tag.getGroup()) + "," + String.format("%04x", tag.getElement());
            return text;
        }

        private String vrToText(AttributeTag tag) {
            byte[] vr = dict.getValueRepresentationFromTag(tag);

            if (vr == null) {
                return "UN";
            } else {
                return new String(vr);
            }
        }


        private String attributeNameToText(AttributeTag tag) {
            String format = "%-32s";
            String name = dict.getNameFromTag(tag);

            if (name == null) {
                return String.format(format, "unknown");
            } else {
                return String.format(format, name);
            }
        }

        private String attributeValueToText(Attribute attribute) {
            int maxLen = 24;
            String format = "%-" + maxLen + "s";

            String value = attribute.getSingleStringValueOrNull();

            try {
                if (attribute.getVL() > 1) {
                    String[] list = attribute.getStringValues();
                    StringBuilder txt = new StringBuilder();
                    for (String s : list) {
                        txt.append(s).append("  ");
                    }
                    value = txt.toString().trim();
                }
            } catch (Exception e) {
                // do not override other text value
            }

            byte[] vr = dict.getValueRepresentationFromTag(attribute.getTag());

            if ((vr != null) && ValueRepresentation.isOtherByteOrWordVR(vr)) {
                return String.format(format, "binary data");
            }

            if (value == null) {
                return String.format(format, "no value");
            } else {
                return String.format(format, value);
            }
        }

        private void appendAttribute(Attribute attribute, long byteOffset) {

            AttributeTag tag = attribute.getTag();

            text.append(offsetToText(previousOffset - 4));
            text.append(tagToText(tag));
            text.append(" ");
            text.append(vrToText(tag));
            text.append(" ");
            text.append(attributeNameToText(tag));
            text.append(" : ");
            text.append(attributeValueToText(attribute));
            text.append(" -- ");
            text.append(bytesToText(previousOffset - 4, byteOffset - 4));
            text.append("\n");
            previousTag = tag;
            previousOffset = byteOffset;
        }

        AttributeList al;

        /**
         * When done, the caller should call this to show the last attribute.
         * <p>
         * This could potentially be done with dispose() method, but it is not clear when it is called by the JVM.
         *
         * @param attributeList Attribute list constructed so far.
         * @param tag           Last tag.
         * @param byteOffset    Offset into file.
         * @return Always false (never intentionally terminate)
         */
        @Override
        public boolean terminate(AttributeList attributeList, AttributeTag tag, long byteOffset) {
            al = attributeList;

            if (previousTag == null) {
                text.append("DICOM Prelude:\n");
                text.append(offsetToText(0));
                text.append("--" + bytesToText(0, byteOffset - 4));
                text.append("\n");
            } else {
                appendAttribute(attributeList.get(previousTag), byteOffset);
            }

            previousTag = tag;
            previousOffset = byteOffset;

            return false; // never terminate
        }

        public void doLastAttribute() {
            appendAttribute(al.get(previousTag), binary.length);
        }
    }


    /**
     * Print contents of a single file to standard output.
     *
     * @param byteArray DICOM content to dump.
     * @return Text version of file with both formatted data and original data in hex.
     * @throws IOException On IO error (e.g. no file permission or premature end of file)
     */
    public String dump(byte[] byteArray) throws IOException {

        DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(byteArray));

        AttributeList al = new AttributeList();

        StringBuffer text = new StringBuffer();
        ReadStrategy readStrategy = new ReadStrategy(byteArray, text);

        try {
            al.read(dis, readStrategy);
            readStrategy.doLastAttribute();
        } catch (DicomException ex) {
            text.append("\nDicomException: ").append(ex);
        }

        return text.toString();
    }

    /**
     * Make a hex dump of an attribute list.
     *
     * @param attributeList Dump this.
     * @return Text version with byte offsets.
     * @throws DicomException If problem writing DICOM to byte array.
     * @throws IOException    If IO error.
     */
    public String dump(AttributeList attributeList) throws DicomException, IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String transferSyntaxUID = attributeList.get(TagFromName.TransferSyntaxUID).getSingleStringValueOrNull();

        attributeList.write(out, transferSyntaxUID, true, true, true, null);

        return dump(out.toByteArray());
    }

    /**
     * Print contents of a single file to standard output.
     *
     * @param file DICOM file to dump.
     * @return Text version of file with both formatted data and original data in hex.
     * @throws IOException On IO error (e.g. no file permission or premature end of file)
     */
    public String dump(File file) throws IOException {
        byte[] binary = FileUtilities.readAllBytes(new FileInputStream(file));
        return dump(binary);
    }

    /**
     * Dump DICOM files to standard output.
     *
     * @param args List of DICOM files.
     * @throws Exception Usually because when encountering non-DICOM file, read
     *                   permission error, or premature end of file.
     */
    public static void main(String[] args) throws Exception {
        System.err.println("Starting ...");
        DicomDump dicomDump = new DicomDump();

        for (String arg : args) {
            File file = new File(arg);
            if (file.isFile()) {
                System.err.println("dumping DICOM file: " + file.getAbsolutePath());
                String text = dicomDump.dump(file);
                System.out.println(text);
            } else {
                System.err.println("Ignoring file: " + file.getAbsolutePath());
            }
        }
        System.err.println("Done.");
    }
}
