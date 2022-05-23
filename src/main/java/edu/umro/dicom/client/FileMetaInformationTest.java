package edu.umro.dicom.client;

import com.pixelmed.dicom.*;

import java.io.*;

public class FileMetaInformationTest {

    private static byte[] writeToByteArray(AttributeList list, String sourceAETitle) throws DicomException, IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

        String transferSyntaxUIDText = TransferSyntax.ExplicitVRLittleEndian;
        UniqueIdentifierAttribute transferSyntaxUID = (UniqueIdentifierAttribute) list.get(TagFromName.TransferSyntaxUID);
        if (transferSyntaxUID != null)
            transferSyntaxUIDText = transferSyntaxUID.getSingleStringValueOrNull();

        FileMetaInformation.addFileMetaInformation(list, transferSyntaxUIDText, sourceAETitle);
        DicomOutputStream dicomOut = new DicomOutputStream(bytesOut, transferSyntaxUIDText, transferSyntaxUIDText);
        list.write(dicomOut);
        return bytesOut.toByteArray();
    }

    private static boolean readFromByteArray(byte[] bytes) throws DicomException, IOException {
        try {
            DicomInputStream dicomInputStream = new DicomInputStream(new ByteArrayInputStream(bytes));
            AttributeList list = new AttributeList();
            list.read(dicomInputStream);
            return true;
        } catch (DicomException ex) {
            System.out.println("Unable to read attribute list.");
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting...");
        File file = new File("D:\\tmp\\maggie\\UMDICOM\\CT_1.dcm");

        StringBuffer aeTitle = new StringBuffer();

        for (int i = 1; i < 10; i++) {
            AttributeList list = new AttributeList();
            list.read(file);
            System.out.println("\n----------------  aeTitle: " + aeTitle + "    len: " + aeTitle.length());
            byte[] bytes = writeToByteArray(list, aeTitle.toString());
            boolean success = readFromByteArray(bytes);

            if (success)
                System.out.println("Success ");
            else {
                System.out.println("Failure.");
            }

            aeTitle.append("" + i);
        }


        System.out.println("Done...");
    }
}
