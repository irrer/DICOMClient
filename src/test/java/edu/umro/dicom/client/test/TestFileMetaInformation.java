package edu.umro.dicom.client.test;

import com.pixelmed.dicom.*;
import edu.umro.dicom.client.*;

import java.io.*;

public class TestFileMetaInformation {

    public static void main(String[] args) throws Exception {

        final boolean makeFile = false;

        final String suffix = "123456789";
        File file = new File("src\\test\\resources\\dicom\\99999999\\99999999_CT_2_0001.DCM");
        // file = null; // Comment this line out to test with more realistic data.

        UniqueIdentifierAttribute sopInstanceUID = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);
        sopInstanceUID.addValue("1.2.3.4.5");

        String[] transferSyntaxList = {
                TransferSyntax.ImplicitVRLittleEndian,
                TransferSyntax.ExplicitVRLittleEndian,
                TransferSyntax.ExplicitVRBigEndian
        };

        File outDir = new File("target\\testMeta");
        outDir.mkdirs();

        File dicomFile = null;

        String dashedLine = "----------------------------------------------------------------";
        int testCount = 0;
        int failCount = 0;

        for (int t = 0; t < transferSyntaxList.length; t++) {
            for (int s = 0; s < 5; s++) {
                for (int c = 0; c < 5; c++) {
                    for (int i = 0; i < 5; i++) {
                        testCount++;
                        String transferSyntaxUID = transferSyntaxList[t];
                        String sourceApplicationEntityTitle = "MyApp" + suffix.substring(0, s);
                        String mediaStorageSOPClassUID = "1.2.3.4.5.6" + suffix.substring(0, c);
                        String mediaStorageSOPInstanceUID = "1.3.6.1.4.1.22361.48658618118952.752827438.1376575785570.9" + suffix.substring(0, i);

                        String summary = "t: " + t + "  s: " + s + "  c: " + c + "  i: " + i;

                        String fileName = "_" +
                                t + "-" +
                                s + "-" +
                                c + "-" +
                                i;

                        // ----------------------------------------------------------------------------------------

                        AttributeList list = new AttributeList();
                        list.put(sopInstanceUID);
                        if (file != null) list.read(file);

                        Attribute groupLength = new UnsignedLongAttribute(TagFromName.FileMetaInformationGroupLength);
                        groupLength.addValue(0L); // The value does not matter because it will be overwritten.
                        list.put(groupLength);

                        com.pixelmed.dicom.FileMetaInformation.addFileMetaInformation(
                                list,
                                mediaStorageSOPClassUID,
                                mediaStorageSOPInstanceUID,
                                transferSyntaxUID,
                                sourceApplicationEntityTitle
                        );

                        if (makeFile) {
                            dicomFile = new File(outDir, fileName + "std.dcm");
                            dicomFile.delete();
                            list.write(dicomFile, TransferSyntax.ImplicitVRLittleEndian, true, true);
                            System.out.println("Wrote file: " + file.getAbsolutePath());
                        }

                        long groupLengthStandard = list.get(TagFromName.FileMetaInformationGroupLength).getLongValues()[0];

                        String dumpStandard = new DicomDump().dump(list);

                        // ----------------------------------------------------------------------------------------

                        list = new AttributeList();
                        list.put(sopInstanceUID);
                        if (file != null) list.read(file);

                        groupLength = new UnsignedLongAttribute(TagFromName.FileMetaInformationGroupLength);
                        groupLength.addValue(0L); // The value does not matter because it will be overwritten.
                        list.put(groupLength);
                        FileMetaInfo2.addFileMetaInfo2(
                                list,
                                mediaStorageSOPClassUID,
                                mediaStorageSOPInstanceUID,
                                transferSyntaxUID,
                                sourceApplicationEntityTitle
                        );


                        if (makeFile) {
                            dicomFile = new File(outDir, fileName + "irr.dcm");
                            dicomFile.delete();
                            list.write(dicomFile, TransferSyntax.ImplicitVRLittleEndian, true, true);
                            System.out.println("Wrote file: " + file.getAbsolutePath());
                        }

                        String dumpIrrer = new DicomDump().dump(list);
                        long groupLengthIrrer = list.get(TagFromName.FileMetaInformationGroupLength).getLongValues()[0];

                        boolean pass = dumpStandard.equals(dumpIrrer);
                        System.out.println("\n" + dashedLine);
                        System.out.println(summary + "  standard: " + groupLengthStandard +
                                "    irrer: " + groupLengthIrrer +
                                "    diff: " + (groupLengthStandard - groupLengthIrrer) +
                                "    same length: " + (groupLengthStandard == groupLengthIrrer) +
                                "    same content: " + pass);
                        System.out.println("---- Standard -----\n" + dumpStandard);
                        System.out.println("---- Irrer -----\n" + dumpIrrer);
                        if (!pass) {
                            failCount++;
                            System.out.println("TEST FAILED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            // System.exit(1);
                        }
                    }
                }
            }
        }
        System.out.println(dashedLine);
        System.out.println("Number of tests: " + testCount + "    Number of failures: " + failCount);
        System.out.println(dashedLine);
    }
}
