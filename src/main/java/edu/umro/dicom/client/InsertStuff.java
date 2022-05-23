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
 * {@link AttributeList.ReadTerminationStrategy} does not support them.
 * Supporting them would either require changing the Pixelmed library or java byte code
 * injection.
 */
public class InsertStuff {

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
            @SuppressWarnings("UnnecessaryLocalVariable")
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

    /**
     * Print contents of a single file to standard output.
     *
     * @param file DICOM file to dump.
     * @return Text version of file with both formatted data and original data in hex.
     * @throws IOException On IO error (e.g. no file permission or premature end of file)
     */
    public String dump(File file) throws IOException {
        byte[] binary = FileUtilities.readAllBytes(new FileInputStream(file));

        DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(binary));

        AttributeList al = new AttributeList();

        StringBuffer text = new StringBuffer();
        ReadStrategy readStrategy = new ReadStrategy(binary, text);

        try {
            al.read(dis);
            //readStrategy.doLastAttribute();

            // ------------------------------------------------------

            if (false) {
                AttributeTag picTag = new AttributeTag(0x0002, 0x0100);
                UniqueIdentifierAttribute picAttr = new UniqueIdentifierAttribute(picTag);
                picAttr.addValue("1.2.826.0.1.3680043.1.2.100.8.40.1101.0");
                al.put(picAttr);
            }

            // ------------------------------------------------------

            if (false) {
                AttributeTag piTag = new AttributeTag(0x0002, 0x0102);
                OtherByteAttribute piAttr = new OtherByteAttribute(piTag);
                piAttr.addValue("DicomObjects.NET");
                al.put(piAttr);
            }

            // ------------------------------------------------------

            if (false) {
                UnsignedLongAttribute fileMetaLen = (UnsignedLongAttribute) al.get(new AttributeTag(0002, 0000));
                fileMetaLen.removeValues();
                fileMetaLen.addValue(282);
            }

            // ------------------------------------------------------
            // write the DICOM
            File outDcm = new File(file.getParentFile(), file.getName() + "SaveIt.dcm");
            outDcm.delete();

            Attribute transferSyntaxAttribute = al.get(TagFromName.TransferSyntaxUID);
            String transferSyntax = TransferSyntax.ImplicitVRLittleEndian;
            if ((transferSyntaxAttribute != null) && (transferSyntaxAttribute.getSingleStringValueOrNull() != null)) {
                transferSyntax = transferSyntaxAttribute.getSingleStringValueOrNull();
            }

            // ------------------------------------------------------

            al.remove(TagFromName.FileMetaInformationGroupLength);
            FileMetaInformation.addFileMetaInformation(al, transferSyntax, "123456789x123456");

            // ------------------------------------------------------

            {
                UnsignedLongAttribute fileMetaLen = (UnsignedLongAttribute) al.get(new AttributeTag(0002, 0000));
                fileMetaLen.removeValues();
                fileMetaLen.addValue(292);
            }

            // ------------------------------------------------------

            al.write(outDcm, transferSyntax, true, true);
            System.out.println("Wrote to " + outDcm.getAbsolutePath());


        } catch
        (DicomException ex) {
            text.append("\nDicomException: ").append(ex);
        }


        return text.toString();

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
        InsertStuff dicomDump = new InsertStuff();

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
