package edu.umro.dicom.client;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.*;

import java.io.*;

public class DicomDump {

    CustomDictionary dict = CustomDictionary.getInstance();

    public class ReadStrategy implements AttributeList.ReadTerminationStrategy {

        byte[] binary;

        StringBuffer text;

        public ReadStrategy(byte[] bin, StringBuffer txt) {
            this.binary = bin;
            this.text = txt;
        }

        private long previousOffset = 0;


        private AttributeTag previousTag = null;

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

            buf.append(String.format("%08d", begin));
            buf.append(":");
            for (long i = begin; i < end; i++) {
                buf.append(String.format(" %02x", binary[(int) i]));
            }
            return buf.toString();
        }

        private String tagToText(AttributeTag tag) {
            String text = String.format("%04x", tag.getGroup()) + "," + String.format("%04x", tag.getElement());
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
                    StringBuffer txt = new StringBuffer();
                    for (String s : list) {
                        txt.append(s + "  ");
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

            text.append(tagToText(tag));
            text.append(" ");
            text.append(vrToText(tag));
            text.append(" ");
            text.append(attributeNameToText(tag));
            text.append(" : ");
            text.append(attributeValueToText(attribute));
            text.append(" -- ");
            text.append("\n");
            previousTag = tag;
            previousOffset = byteOffset;
        }

        AttributeList al;

        @Override
        public boolean terminate(AttributeList attributeList, AttributeTag tag, long byteOffset) {
            al = attributeList;

            if (previousTag == null) {
                text.append("DICOM Header:\n");
                text.append(bytesToText(0, byteOffset - 4));
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

    public String dump(File file) throws IOException {
        byte[] binary = FileUtilities.readAllBytes(new FileInputStream(file));

        DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(binary));

        AttributeList al = new AttributeList();

        StringBuffer text = new StringBuffer();
        ReadStrategy readStrategy = new ReadStrategy(binary, text);

        try {
            al.read(dis, readStrategy);
            readStrategy.doLastAttribute();
        } catch
        (DicomException ex) {
            text.append("\nDicomException: " + ex);
        }


        return text.toString();

    }

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
