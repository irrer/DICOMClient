package edu.umro.dicom.client;
//package com.pixelmed.dicom;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.*;

import java.io.*;

/**
 * Special note: This is a replacement for the standard com.pixelmed.dicom version that did not handle metadata that
 * contained the lesser used metadata tags, such as:
 * <p>
 * (0002,0100) PrivateInformationCreatorUID
 * (0002,0102) PrivateInformation
 * <p>
 * It also did not handle the case where
 * <p>
 * (0002,0016) SourceApplicationEntityTitle
 * *
 * was of length 0 (an empty string).
 *
 *
 * <p>A class to abstract the contents of a file meta information header as used for a
 * DICOM PS 3.10 file, with additional static methods to add to and extract from an
 * existing list of attributes.</p>
 *
 * @author dclunie
 * @author irrer
 * Updated to calculate FileMetaInformationGroupLength for all possible group 0x0002 items, and also to handle
 * SourceApplicationEntityTitle of length 0.
 */
public class FileMetaInformation {

    private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/FileMetaInformation.java,v 1.19 2020/01/01 15:48:09 dclunie Exp $";

    private AttributeList list;

    /**
     * <p>Construct an instance of the  file meta information from the specified parameters.</p>
     *
     * @param mediaStorageSOPClassUID      the SOP Class UID of the dataset to which the file meta information will be prepended
     * @param mediaStorageSOPInstanceUID   the SOP Instance UID of the dataset to which the file meta information will be prepended
     * @param transferSyntaxUID            the transfer syntax UID that will be used to write the dataset
     * @param sourceApplicationEntityTitle the source AE title of the dataset (may be null)
     * @throws DicomException if error in DICOM encoding
     */
    public FileMetaInformation(String mediaStorageSOPClassUID, String mediaStorageSOPInstanceUID, String transferSyntaxUID, String sourceApplicationEntityTitle) throws DicomException {
        list = new AttributeList();
        addFileMetaInformation(list, mediaStorageSOPClassUID, mediaStorageSOPInstanceUID, transferSyntaxUID, sourceApplicationEntityTitle);
    }

    /**
     * <p>Add the file meta information attributes to an existing list, using
     * only the parameters supplied.</p>
     *
     * <p>Note that the appropriate (mandatory) file meta information group length tag is also computed and added.</p>
     *
     * @param list                         the list to be extended with file meta information attributes
     * @param mediaStorageSOPClassUID      the SOP Class UID of the dataset to which the file meta information will be prepended
     * @param mediaStorageSOPInstanceUID   the SOP Instance UID of the dataset to which the file meta information will be prepended
     * @param transferSyntaxUID            the transfer syntax UID that will be used to write the dataset
     * @param sourceApplicationEntityTitle the source AE title of the dataset (may be null)
     * @throws DicomException if error in DICOM encoding
     */
    public static void addFileMetaInformation(AttributeList list,
                                              String mediaStorageSOPClassUID, String mediaStorageSOPInstanceUID, String transferSyntaxUID, String sourceApplicationEntityTitle) throws DicomException {

        {
            Attribute a = new OtherByteAttribute(TagFromName.FileMetaInformationVersion);
            byte[] b = new byte[2];
            b[0] = 0x00;
            b[1] = 0x01;
            a.setValues(b);
            list.put(a);
        }

        if (mediaStorageSOPClassUID == null || mediaStorageSOPClassUID.trim().length() == 0) {
            throw new DicomException("Cannot add FileMetaInformation without MediaStorageSOPClassUID value");
        } else {
            Attribute a = new UniqueIdentifierAttribute(TagFromName.MediaStorageSOPClassUID);
            a.addValue(mediaStorageSOPClassUID);
            list.put(a);
        }

        if (mediaStorageSOPInstanceUID == null || mediaStorageSOPInstanceUID.trim().length() == 0) {
            throw new DicomException("Cannot add FileMetaInformation without MediaStorageSOPInstanceUID value");
        } else {
            Attribute a = new UniqueIdentifierAttribute(TagFromName.MediaStorageSOPInstanceUID);
            a.addValue(mediaStorageSOPInstanceUID);
            list.put(a);
        }

        if (transferSyntaxUID == null || transferSyntaxUID.trim().length() == 0) {
            throw new DicomException("Cannot add FileMetaInformation without TransferSyntaxUID value");
        } else {
            Attribute a = new UniqueIdentifierAttribute(TagFromName.TransferSyntaxUID);
            a.addValue(transferSyntaxUID);
            list.put(a);
        }

        {
            Attribute a = new UniqueIdentifierAttribute(TagFromName.ImplementationClassUID);
            a.addValue(VersionAndConstants.implementationClassUID);
            list.put(a);
        }
        {
            Attribute a = new ShortStringAttribute(TagFromName.ImplementationVersionName);
            a.addValue(VersionAndConstants.implementationVersionName);
            list.put(a);
        }
        if (sourceApplicationEntityTitle != null) {
            Attribute a = new ApplicationEntityAttribute(TagFromName.SourceApplicationEntityTitle);
            a.addValue(sourceApplicationEntityTitle);
            list.put(a);
        }

        // Make an attribute list containing only metadata.
        AttributeList onlyMetaList = new AttributeList();
        for (AttributeTag t : list.keySet()) {
            if (t.getGroup() == 0x0002)
                onlyMetaList.put(list.get(t));
        }

        // Ensure that there is a group length element.
        Attribute groupLength = new UnsignedLongAttribute(TagFromName.FileMetaInformationGroupLength);
        groupLength.addValue(0L); // The value does matter because it will be overwritten.
        list.put(groupLength);

        // write the metadata to a byte array so its size can be determined.
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        try {
            DicomOutputStream dicomOut = new DicomOutputStream(bytesOut, transferSyntaxUID, transferSyntaxUID);
            onlyMetaList.write(dicomOut);
        } catch (IOException ex) {
            throw new DicomException("Unable to convert metadata to an output stream");
        }

        long standardHeaderLength = 128 + 12;  // 128 byte DICOM header plus FileMetaInformationGroupLength
        long measuredGl = bytesOut.size() - standardHeaderLength;

        // Set the group length to the proper value
        groupLength.removeValues();
        groupLength.addValue(measuredGl);
    }


    /**
     * <p>Add the file meta information attributes to an existing list, extracting
     * the known UIDs from that list, and adding the additional parameters supplied.</p>
     *
     * @param list                         the list to be extended with file meta information attributes
     * @param transferSyntaxUID            the transfer syntax UID that will be used to write this list
     * @param sourceApplicationEntityTitle the source AE title of the dataset in the list (may be null)
     * @throws DicomException if error in DICOM encoding
     */
    public static void addFileMetaInformation(AttributeList list, String transferSyntaxUID, String sourceApplicationEntityTitle) throws DicomException {
        String mediaStorageSOPClassUID = null;
        Attribute aSOPClassUID = list.get(TagFromName.SOPClassUID);
        if (aSOPClassUID != null) {
            mediaStorageSOPClassUID = aSOPClassUID.getSingleStringValueOrNull();
        }

        String mediaStorageSOPInstanceUID = null;
        Attribute aSOPInstanceUID = list.get(TagFromName.SOPInstanceUID);
        if (aSOPInstanceUID != null) {
            mediaStorageSOPInstanceUID = aSOPInstanceUID.getSingleStringValueOrNull();
        }
        if (mediaStorageSOPClassUID == null && mediaStorageSOPInstanceUID == null && list.get(TagFromName.DirectoryRecordSequence) != null) {
            // is a DICOMDIR, so use standard SOP Class and make up a UID
            mediaStorageSOPClassUID = SOPClass.MediaStorageDirectoryStorage;
            mediaStorageSOPInstanceUID = new UIDGenerator().getNewUID();
        }

        if (mediaStorageSOPClassUID == null) {
            throw new DicomException("Could not add File Meta Information - missing or empty SOPClassUID and not a DICOMDIR");
        }
        if (mediaStorageSOPInstanceUID == null) {
            throw new DicomException("Could not add File Meta Information - missing or empty SOPInstanceUID and not a DICOMDIR");
        }

        addFileMetaInformation(list, mediaStorageSOPClassUID, mediaStorageSOPInstanceUID, transferSyntaxUID, sourceApplicationEntityTitle);
    }

    /**
     * <p>Get the attribute list in this instance of the file meat information.</p>
     *
     * @return the attribute list
     */
    public AttributeList getAttributeList() {
        return list;
    }

    /**
     * <p>For testing.</p>
     *
     * <p>Generate a dummy file meta information header and test reading and writing it.</p>
     *
     * @param arg ignored
     */
    public static void main(String[] arg) {

        try {
            AttributeList list = new AttributeList();
            DicomDictionary dictionary = new DicomDictionary();

            {
                UniqueIdentifierAttribute ui = new UniqueIdentifierAttribute(dictionary.getTagFromName("PrivateInformationCreatorUID"));
                ui.addValue("1.2.3.5.7.11"); // randomly chosen UID for testing only.
                list.put(ui);

            }
            {
                OtherByteAttribute ob = new OtherByteAttribute(dictionary.getTagFromName("PrivateInformation"));
                ob.setValues("ABCDE".getBytes());
                list.put(ob);
            }

            FileMetaInformation.addFileMetaInformation(list, "1.2.3.44", "1.2", TransferSyntax.Default, "MYAE");

            // Un-comment the following line to show the problem that was fixed.
            // com.pixelmed.dicom.FileMetaInformation.addFileMetaInformation(list, "1.2.3.44", "1.2", TransferSyntax.Default, "MYAE");

            System.err.println("As constructed:");    // no need to use SLF4J since command line utility/test
            System.err.print(list);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            list.write(new DicomOutputStream(bout, TransferSyntax.ExplicitVRLittleEndian, null));
            byte[] b = bout.toByteArray();
            System.err.print(HexDump.dump(b));
            AttributeList rlist = new AttributeList();
            rlist.read(new DicomInputStream(new ByteArrayInputStream(b), TransferSyntax.ExplicitVRLittleEndian, true));
            System.err.println("As read:");
            System.err.print(rlist);
        } catch (Exception e) {
            e.printStackTrace(System.err);    // no need to use SLF4J since command line utility/test
            System.exit(0);
        }
    }
}
