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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.TagFromName;
import edu.umro.dicom.common.Util;
import edu.umro.util.OpSys;

/**
 * Construct a Key Object DICOM object from a DICOM series.
 * 
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
public class KeyObject extends AttributeList {

    /** Default ID. */
    private static final long serialVersionUID = 1L;

    /** Description of source series. */
    private String seriesSummary = null;

    /** Files containing series slices. */
    private Collection<File> fileList = null;

    /** Current date and time used for time stamping DICOM file. */
    private Date now = new Date();

    /** One slice of the source series. */
    private AttributeList sampleInstance = null;

    /** Name of this program. */
    private static final String PROGRAM_NAME = "DICOM Upload";

    /** Specific definition of what this Key Object represents. */
    public static final String SPECIFIC_MEANING = "Instances in series to support verification that it is complete";

    /** The code for the specific purpose of this Key Object file.  This is composed of
     * the last dot delimited set of digits for the University of Michigan's unique UID
     * prefix followed by a simple counter to make instances unique. */
    public static final String CODE_VALUE = "22361" + ".0001";


    /**
     * Construct an attribute using the value from the source series.  If not
     * available, use a value from one of the other attributes (listed).  If not
     * available, use the default value.
     * 
     * @param tag Type of attribute to create.
     * 
     * @param defalt Default value if all else fails.
     * 
     * @param secondary List of values to use if given tag does not exist in source series.
     *  
     * @return Newly constructed attribute.
     * 
     * @throws DicomException
     */
    private Attribute constructAttribute(AttributeTag tag, String defalt, AttributeTag ... secondary) throws DicomException {
        Attribute s = sampleInstance.get(tag);
        if ((s != null) && (s.getSingleStringValueOrNull() != null)) {
            return s;
        }
        for (AttributeTag t : secondary) {
            s = sampleInstance.get(t);
            if ((s != null) && (s.getSingleStringValueOrNull() != null)) {
                Attribute a = AttributeFactory.newAttribute(tag);
                a.addValue(s.getSingleStringValueOrNull());
                return a;
            }
        }
        Attribute a = AttributeFactory.newAttribute(tag);
        a.addValue(defalt);
        return a;
    }


    /**
     * Construct an attribute with a string value and add it to the list.
     * 
     * @param attributeList Add it to this list.
     * 
     * @param tag Tag of new attribute.
     * 
     * @param value Value of new attribute.
     * 
     * @throws DicomException
     */
    private Attribute addAttr(AttributeList attributeList, AttributeTag tag, String value) throws DicomException {
        Attribute a = AttributeFactory.newAttribute(tag);
        a.addValue(value);
        attributeList.put(a);
        return a;
    }


    /**
     * Construct an attribute with a string value and add it to the series list.
     * 
     * @param attributeList Add it to this list.
     * 
     * @param tag Tag of new attribute.
     * 
     * @param value Value of new attribute.
     * 
     * @throws DicomException
     */
    private Attribute addAttr(AttributeTag tag, String value) throws DicomException {
        return addAttr(this, tag, value);
    }


    private String dicomDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return simpleDateFormat.format(date);
    }


    private String dicomTime(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmss");
        return simpleDateFormat.format(date);
    }


    private String dicomDate() {
        return dicomDate(now);
    }


    private String dicomTime() {
        return dicomTime(now);
    }


    /**
     * Get the series description and number and form descriptive
     * text.  If they do not exist, then do not use them.
     * 
     * @return Best possible description of series.
     */
    private String getSeriesDescription() {
        String seriesDescription = "";
        Attribute sd = get(TagFromName.SeriesDescription);
        if (sd != null) {
            seriesDescription += sd.getSingleStringValueOrEmptyString().trim();
        }
        Attribute sn = get(TagFromName.SeriesNumber);
        if (sd != null) {
            String seriesNumber = sn.getSingleStringValueOrEmptyString().trim();
            if (seriesNumber.length() > 0) {
                seriesDescription += "  Series number: " + seriesNumber; 
            }
        }

        if (seriesDescription.length() == 0) {
            seriesDescription = "<no series description or number>";
        }
        return "Manifest of " + seriesDescription + "  Files: " + fileList.size();
    }


    /**
     * Execute 'laundry list' of operations to add all the tags required to
     * construct a KO (Key Object) DICOM object.
     * 
     * @throws DicomException
     * @throws IOException 
     */
    private void addRequiredTags() throws DicomException, IOException {

        String sopInstanceUID = Util.getUID();
        String seriesInstanceUID = Util.getUID();

        addAttr(TagFromName.MediaStorageSOPClassUID, SOPClass.KeyObjectSelectionDocumentStorage);
        addAttr(TagFromName.MediaStorageSOPInstanceUID, sopInstanceUID);
        addAttr(TagFromName.TransferSyntaxUID, Util.DEFAULT_STORAGE_SYNTAX);
        addAttr(TagFromName.ImplementationClassUID, Util.UMRO_ROOT_GUID);
        addAttr(TagFromName.SpecificCharacterSet, "ISO_IR 100");
        addAttr(TagFromName.SOPClassUID, SOPClass.KeyObjectSelectionDocumentStorage);
        addAttr(TagFromName.SOPInstanceUID, sopInstanceUID);
        put(constructAttribute(TagFromName.StudyDate, dicomDate()));
        addAttr(TagFromName.SeriesDate, dicomDate());
        put(constructAttribute(TagFromName.ContentDate, dicomDate(), TagFromName.StudyDate, TagFromName.SeriesDate, TagFromName.InstanceCreationDate ));
        put(constructAttribute(TagFromName.StudyTime, dicomTime()));
        addAttr(TagFromName.SeriesTime, dicomTime());
        put(constructAttribute(TagFromName.ContentTime, dicomTime(), TagFromName.StudyTime, TagFromName.SeriesTime, TagFromName.InstanceCreationTime));
        put(constructAttribute(TagFromName.SeriesDescription, getSeriesDescription()));
        addAttr(TagFromName.AccessionNumber, "");
        addAttr(TagFromName.Modality, "KO");
        addAttr(TagFromName.Manufacturer, Util.getImplementationVendor());
        addAttr(TagFromName.InstitutionName, Util.getImplementationVendor());
        addAttr(TagFromName.InstitutionAddress, Util.UMRO_POSTAL_ADDRESS);
        addAttr(TagFromName.OperatorsName, "USER^ " + System.getProperty("user.name").toUpperCase());
        addAttr(TagFromName.ReferringPhysicianName, "");
        addAttr(TagFromName.StationName, (OpSys.getHostName() != null) ? OpSys.getHostName() : OpSys.getHostIPAddress());
        put(constructAttribute(TagFromName.StudyDescription, ""));
        put(constructAttribute(TagFromName.NameOfPhysiciansReadingStudy, ""));
        addAttr(TagFromName.ManufacturerModelName, PROGRAM_NAME);
        put(AttributeFactory.newAttribute(TagFromName.ReferencedPerformedProcedureStepSequence));
        put(constructAttribute(TagFromName.PatientName, ""));
        put(constructAttribute(TagFromName.PatientID, ""));
        put(constructAttribute(TagFromName.PatientBirthDate, ""));
        put(constructAttribute(TagFromName.PatientSex, ""));
        put(constructAttribute(TagFromName.AdditionalPatientHistory, ""));
        addAttr(TagFromName.DeviceSerialNumber, "Not Applicable");
        addAttr(TagFromName.SoftwareVersions, Util.getImplementationVersion());
        put(constructAttribute(TagFromName.StudyInstanceUID, null));
        addAttr(TagFromName.SeriesInstanceUID, seriesInstanceUID);
        put(constructAttribute(TagFromName.StudyID, ""));
        addAttr(TagFromName.SeriesNumber, "0");
        addAttr(TagFromName.InstanceNumber, "0");
        addAttr(TagFromName.ValueType, "CONTAINER");

        { // ConceptNameCodeSequence
            AttributeList aList = new AttributeList();
            addAttr(aList, TagFromName.CodeValue, CODE_VALUE);
            addAttr(aList, TagFromName.CodingSchemeDesignator, "DCM");
            addAttr(aList, TagFromName.CodeMeaning, SPECIFIC_MEANING);

            SequenceAttribute a = (SequenceAttribute)AttributeFactory.newAttribute(TagFromName.ConceptNameCodeSequence);
            a.addItem(aList);
            put(a);
        }

        {
            addAttr(TagFromName.ContinuityOfContent, "SEPARATE");
            // 'SEPARATE' is assumed because although the children of this container are
            // logically linked in a continuous flow, the flow is not textual.
        }

        { // CurrentRequestedProcedureEvidenceSequence
            SequenceAttribute curReqProcEvidSeqAttr = (SequenceAttribute)AttributeFactory.newAttribute(TagFromName.CurrentRequestedProcedureEvidenceSequence);
            put(curReqProcEvidSeqAttr);
            AttributeList curReqProcEvidSeqList = new AttributeList();
            curReqProcEvidSeqAttr.addItem(curReqProcEvidSeqList);

            SequenceAttribute refSerSeqAttr = (SequenceAttribute)AttributeFactory.newAttribute(TagFromName.ReferencedSeriesSequence);
            curReqProcEvidSeqList.put(refSerSeqAttr);
            AttributeList refSerSeqList = new AttributeList();
            refSerSeqAttr.addItem(refSerSeqList);

            SequenceAttribute refSopSeqAttr = (SequenceAttribute)AttributeFactory.newAttribute(TagFromName.ReferencedSOPSequence);
            refSerSeqList.put(refSopSeqAttr);
            for (File file : fileList) {
                AttributeList seriesList = new AttributeList();
                seriesList.read(file);

                AttributeList childList = new AttributeList();
                refSopSeqAttr.addItem(childList);
                {
                    Attribute a = AttributeFactory.newAttribute(TagFromName.ReferencedSOPClassUID);
                    String value = seriesList.get(TagFromName.SOPClassUID).getSingleStringValueOrNull();
                    if (value == null) {
                        throw new NullPointerException("Mis-formed series.  Series " + seriesSummary + " is required to contain a SOP Class UID but it does not.");
                    }
                    a.addValue(value);
                    childList.put(a);
                }

                {
                    Attribute a = AttributeFactory.newAttribute(TagFromName.ReferencedSOPInstanceUID);
                    String value = seriesList.get(TagFromName.SOPInstanceUID).getSingleStringValueOrNull();
                    if (value == null) {
                        throw new NullPointerException("Mis-formed series.  Series " + seriesSummary + " is required to contain a SOP Instance UID but it does not.");
                    }
                    a.addValue(value);
                    childList.put(a);
                }
            }

            addAttr(refSerSeqList, TagFromName.SeriesInstanceUID, sampleInstance.get(TagFromName.SeriesInstanceUID).getSingleStringValueOrNull());

            addAttr(curReqProcEvidSeqList, TagFromName.StudyInstanceUID, sampleInstance.get(TagFromName.StudyInstanceUID).getSingleStringValueOrNull());
        }



        { // ContentTemplateSequence
            AttributeList aList = new AttributeList();

            addAttr(aList, TagFromName.MappingResource, "DCMR");
            addAttr(aList, TagFromName.TemplateIdentifier, "2010");

            SequenceAttribute a = (SequenceAttribute)AttributeFactory.newAttribute(TagFromName.ContentTemplateSequence);
            a.addItem(aList);
            put(a);
        }


        { // ContentSequence (what we're really here for)
            SequenceAttribute contentSeqAttr = (SequenceAttribute)AttributeFactory.newAttribute(TagFromName.ContentSequence);
            put(contentSeqAttr);

            for (File file : fileList) {
                AttributeList refList = new AttributeList();
                contentSeqAttr.addItem(refList);
                addAttr(refList, TagFromName.RelationshipType, "CONTAINS");
                addAttr(refList, TagFromName.ValueType, "IMAGE");

                SequenceAttribute refSopSeq = (SequenceAttribute)AttributeFactory.newAttribute(TagFromName.ReferencedSOPSequence);
                refList.put(refSopSeq);

                AttributeList seriesList = new AttributeList();
                seriesList.read(file);

                AttributeList refSopList = new AttributeList();
                refSopSeq.addItem(refSopList);

                {
                    Attribute a = AttributeFactory.newAttribute(TagFromName.ReferencedSOPClassUID);
                    String value = seriesList.get(TagFromName.SOPClassUID).getSingleStringValueOrNull();
                    if (value == null) {
                        throw new NullPointerException("Mis-formed series.  Series " + seriesSummary + " is required to contain a SOP Class UID but it does not.");
                    }
                    a.addValue(value);
                    refSopList.put(a);
                }

                {
                    Attribute a = AttributeFactory.newAttribute(TagFromName.ReferencedSOPInstanceUID);
                    String value = seriesList.get(TagFromName.SOPInstanceUID).getSingleStringValueOrNull();
                    if (value == null) {
                        throw new NullPointerException("Mis-formed series.  Series " + seriesSummary + " is required to contain a SOP Instance UID but it does not.");
                    }
                    a.addValue(value);
                    refSopList.put(a);
                }
            }
        }
    }


    /**
     * Construct a key object DICOM modality that contains a list of the object in the fileNameList.
     *   
     * @param seriesSummary Description of source series.
     * 
     * @param fileNameList Names of files containing series.  Must be from the same series.
     * 
     * @throws DicomException
     * @throws IOException
     */
    public KeyObject(String seriesDescription, Collection<File> fileList) throws DicomException, IOException {
        this.seriesSummary = seriesDescription;
        this.fileList = fileList;
        sampleInstance = new AttributeList();
        sampleInstance.read((String)(fileList.toArray()[0]));
        addRequiredTags();
        FileMetaInformation.addFileMetaInformation(this, Util.DEFAULT_STORAGE_SYNTAX, PROGRAM_NAME);
    }
}
