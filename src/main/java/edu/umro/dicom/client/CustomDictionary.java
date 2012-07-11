package edu.umro.dicom.client;

import java.lang.NumberFormatException;

import java.util.Date;
import java.util.ArrayList;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.ValueRepresentation;
import com.pixelmed.dicom.AttributeTag;


public class CustomDictionary extends DicomDictionary {

    private static CustomDictionary instance = null;

    /** List of extensions provided by this dictionary. */
    ArrayList<PrivateTag> extensions = null;


    /**
     * Format an integer as a hex number with leading zeroes
     * padded to the indicated length.
     *
     * @param i Integer to format.
     *
     * @param len Number of resulting digits.
     *
     * @return Formatted integer.
     */
    private String intToHex(int i, int len) {
        String text = Integer.toHexString(i);
        if (text.length() > len) {
            throw new NumberFormatException("Unable to fit integer value of '" + i +
                    "' into " + len + " hex digits.  Result is: " + text);
        }
        while (text.length() < len) {
            text = "0" + text;
        }
        return text;
    }


    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked") protected void createTagList() {
        init();
        super.createTagList();
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            tagList.add(privateTag.getAttributeTag());
        }
    }




    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked") protected void createValueRepresentationsByTag() {
        init();
        super.createValueRepresentationsByTag();
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            valueRepresentationsByTag.put(privateTag.getAttributeTag(), privateTag.getValueRepresentation());
        }
    }




    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked") protected void createInformationEntityByTag() {
        init();
        super.createInformationEntityByTag();
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            informationEntityByTag.put(privateTag.getAttributeTag(), privateTag.getInformationEntity());
        }
    }




    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked") protected void createNameByTag() {
        init();
        super.createNameByTag();
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            nameByTag.put(privateTag.getAttributeTag(), privateTag.getName());
        }
    }




    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked") protected void createTagByName() {
        init();
        super.createTagByName();
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            tagByName.put(privateTag.getName(), privateTag.getAttributeTag());
        }
    }




    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked") protected void createFullNameByTag() {
        init();
        super.createFullNameByTag();
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            fullNameByTag.put(privateTag.getName(), privateTag.getFullName());
        }
    }


    private synchronized void init() {
        // only do this once.
        if (extensions == null) {
            extensions = new ArrayList<PrivateTag>();

            ClientConfig.getInstance().getPrivateTagList();
        }
    }


    /**
     * Initialize the list of extensions.
     */
    protected void generateCustomAttributes() {
        extensions = new ArrayList<PrivateTag>();

        extensions.add(new PrivateTag(0x3249, 0x0010, ValueRepresentation.LO, "VarianCreator3249"            , "Varian Creator 3249"             ));
        extensions.add(new PrivateTag(0x3249, 0x1000, ValueRepresentation.DS, "MaximumTreatmentTime"         , "Maximum Treatment Time"          ));

        extensions.add(new PrivateTag(0x3267, 0x0010, ValueRepresentation.LO, "VarianCreator3267"            , "Varian Creator 3267"             ));
        extensions.add(new PrivateTag(0x3267, 0x1000, ValueRepresentation.SH, "ReferencedPatientVolumeID"    , "Referenced Patient Volume ID"    ));

        extensions.add(new PrivateTag(0x3253, 0x0010, ValueRepresentation.LO, "VarianCreator3253"            , "Varian Creator 3253"             ));
        extensions.add(new PrivateTag(0x3253, 0x1000, ValueRepresentation.OB, "VarianExtendedInterfaceData"  , "Varian Extended Interface Data"  ));
        extensions.add(new PrivateTag(0x3253, 0x1001, ValueRepresentation.IS, "VarianExtendedInterfaceLength", "Varian Extended Interface Length"));
        extensions.add(new PrivateTag(0x3253, 0x1002, ValueRepresentation.LO, "VarianExtendedInterfaceFormat", "Varian Extended Interface Format"));



        // newer Varian extensions
        extensions.add(new PrivateTag(0x3243, 0x1009, ValueRepresentation.SH, "BeamSecondaryName"                          , "BeamSecondaryName"                          ));

        extensions.add(new PrivateTag(0x3249, 0x1010, ValueRepresentation.UI, "ReferencedPrimaryDoseReferenceUID"          , "ReferencedPrimaryDoseReferenceUID"          ));

        extensions.add(new PrivateTag(0x3285, 0x0010, ValueRepresentation.LO, "VarianCreator3285"                          , "VarianCreator3285"                          ));
        extensions.add(new PrivateTag(0x3285, 0x1000, ValueRepresentation.SQ, "PrimaryFluenceModeSequence"                 , "PrimaryFluenceModeSequence"                 ));
        extensions.add(new PrivateTag(0x3285, 0x1001, ValueRepresentation.CS, "FluenceMode"                                , "FluenceMode"                                ));
        extensions.add(new PrivateTag(0x3285, 0x1002, ValueRepresentation.SH, "FluenceMode"                                , "FluenceMode"                                ));

        extensions.add(new PrivateTag(0x3287, 0x0010, ValueRepresentation.LO, "VarianCreator3287"                          , "VarianCreator3287"                          ));
        extensions.add(new PrivateTag(0x3287, 0x1000, ValueRepresentation.SQ, "PlanIntegritySequence"                      , "PlanIntegritySequence"                      ));
        extensions.add(new PrivateTag(0x3287, 0x1001, ValueRepresentation.LO, "PlanIntegrityHash"                          , "PlanIntegrityHash"                          ));
        extensions.add(new PrivateTag(0x3287, 0x1002, ValueRepresentation.SH, "PlanIntegrityHashVersion"                   , "PlanIntegrityHashVersion"                   ));

        extensions.add(new PrivateTag(0x3273, 0x0010, ValueRepresentation.LO, "VarianCreator3273"                          , "VarianCreator3273"                          ));
        extensions.add(new PrivateTag(0x3273, 0x1000, ValueRepresentation.DS, "RTImageIsocenterPosition"                   , "RTImageIsocenterPosition"                   ));
        extensions.add(new PrivateTag(0x3273, 0x1001, ValueRepresentation.CS, "RTImagePatientPosition"                     , "RTImagePatientPosition"                     ));

        extensions.add(new PrivateTag(0x3263, 0x0010, ValueRepresentation.LO, "VarianCreator3263"                          , "VarianCreator3263"                          ));
        extensions.add(new PrivateTag(0x3263, 0x1001, ValueRepresentation.SQ, "ReferencedStructureSetRelationshipSequence" , "ReferencedStructureSetRelationshipSequence" ));
        extensions.add(new PrivateTag(0x3263, 0x1002, ValueRepresentation.CS, "StructureSetRelationship"                   , "StructureSetRelationship"                   ));



        //<VarianCreator3243 element="1009" group="3243" vr="LO"></VarianCreator3243>
        //extensions.add(new PrivateTag(0x3243, 0x1009, ValueRepresentation.LO, "VarianCreator3243"            , "Varian Creator 3243"             ));

        extensions.add(new PrivateTag(0x3243, 0x0010, ValueRepresentation.LO, "VarianCreator3243"               , "Varian Creator 3243"                  ));
        extensions.add(new PrivateTag(0x3243, 0x1009, ValueRepresentation.SH, "BeamSecondaryName"               , "Beam Secondary Name"                  ));
        extensions.add(new PrivateTag(0x3291, 0x0010, ValueRepresentation.LO, "VarianCreator3291"               , "Varian Creator 3291"                  ));
        extensions.add(new PrivateTag(0x3291, 0x1000, ValueRepresentation.DS, "SourceToGeneralAccessoryDistance", "Source To General Accessory Distance "));

        for (int a = 0x5000; a <= 0x50fe; a += 2) {
            extensions.add(new PrivateTag(a, 0x0005, ValueRepresentation.US, "CurveDimensions"         + intToHex(a,4), "Curve Dimensions "         + intToHex(a,4)));
            extensions.add(new PrivateTag(a, 0x0010, ValueRepresentation.US, "NumberOfPoints"          + intToHex(a,4), "Number Of Points "         + intToHex(a,4)));
            extensions.add(new PrivateTag(a, 0x0020, ValueRepresentation.CS, "TypeOfData"              + intToHex(a,4), "Type Of Data "             + intToHex(a,4)));
            extensions.add(new PrivateTag(a, 0x0022, ValueRepresentation.LO, "CurveDescription"        + intToHex(a,4), "Curve Description "        + intToHex(a,4)));
            extensions.add(new PrivateTag(a, 0x0030, ValueRepresentation.SH, "AxisUnits"               + intToHex(a,4), "Axis Units "               + intToHex(a,4)));
            extensions.add(new PrivateTag(a, 0x0040, ValueRepresentation.SH, "AxisLabels"              + intToHex(a,4), "Axis Labels "              + intToHex(a,4)));
            extensions.add(new PrivateTag(a, 0x0103, ValueRepresentation.US, "DataValueRepresentation" + intToHex(a,4), "Data Value Representation "+ intToHex(a,4)));
            extensions.add(new PrivateTag(a, 0x2500, ValueRepresentation.LO, "CurveLabel"              + intToHex(a,4), "Curve Label "              + intToHex(a,4)));
            extensions.add(new PrivateTag(a, 0x3000, ValueRepresentation.OX, "CurveData"               + intToHex(a,4), "Curve Data "               + intToHex(a,4)));
        }

        {
            for (PrivateTag pt : extensions) {
                System.out.println(pt.toXml());
            }
        }

    }


    /**
     * Default constructor.
     */
    private CustomDictionary() {
        init();
    }


    public synchronized static CustomDictionary getInstance() {
        if (instance == null) {
            instance = new CustomDictionary();
        }
        return instance;
    }


    /**
     * Format an XML version of this dictionary.
     *
     * @param out Print it to here.
     */
    @Override
    public String toString() {

        StringBuffer text = new StringBuffer("");
        Date now = new Date();

        text.append("<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n");

        text.append("<CustomDictionary>\n");
        text.append("<CustomDictionaryGenerationTimeAndDate>" +
                now +
        "</CustomDictionaryGenerationTimeAndDate>\n");
        text.append("<CustomDictionaryVersionTimeStamp>" +
                now.getTime() +
        "</CustomDictionaryVersionTimeStamp>\n");
        for (Object oTag : tagList) {
            AttributeTag tag = (AttributeTag)(oTag);
            // out.write(getNameFromTag(tag) + " : " + tag);
            // String tagName = getNameFromTag(tag);
            String subText = "<" + getNameFromTag(tag);

            subText +=
                " element='" + intToHex(tag.getElement(), 4) + "'" +
                " group='" + intToHex(tag.getGroup(), 4) + "'" +
                " vr='" + ValueRepresentation.getAsString(getValueRepresentationFromTag(tag)) + "'" +
                "></" + getNameFromTag(tag) + ">\n";
            text.append(subText);
        }
        text.append("</CustomDictionary>\n");
        return text.toString().replaceAll("\n", System.getProperty("line.separator"));
    }

    private static void testVrs() {

        byte[][] vrList = {
                ValueRepresentation.AE,
                ValueRepresentation.AS,
                ValueRepresentation.AT,
                ValueRepresentation.CS,
                ValueRepresentation.DA,
                ValueRepresentation.DS,
                ValueRepresentation.DT,
                ValueRepresentation.FL,
                ValueRepresentation.FD,
                ValueRepresentation.IS,
                ValueRepresentation.LO,
                ValueRepresentation.LT,
                ValueRepresentation.OB,
                ValueRepresentation.OF,
                ValueRepresentation.OW,
                ValueRepresentation.OX,
                ValueRepresentation.PN,
                ValueRepresentation.SH,
                ValueRepresentation.SL,
                // ValueRepresentation.SQ,
                ValueRepresentation.SS,
                ValueRepresentation.ST,
                ValueRepresentation.TM,
                ValueRepresentation.UI,
                ValueRepresentation.UL,
                ValueRepresentation.UN,
                ValueRepresentation.US,
                ValueRepresentation.UT,
                ValueRepresentation.XS,
                ValueRepresentation.XO
        };

        final String VALUE = "12";
        for (byte[] vr : vrList) {
            try {
                Attribute at = AttributeFactory.newAttribute(new AttributeTag("0x5062,0x0005"), vr);
                at.setValue(VALUE);
                at.addValue(VALUE);
                String[] textList = at.getStringValues();
                boolean ok = textList.length == 2;
                if (!ok) {
                    System.out.println("wrong number of values for " + ValueRepresentation.getAsString(vr) + "    Expected: 2    got: " + textList.length);
                }
                for (String text : textList) {
                    if (!text.trim().equals(VALUE)) {
                        ok = false;
                        System.out.println("value wrong for vr: " + ValueRepresentation.getAsString(vr) + "    Expected: " + VALUE + "    got: " + text);
                        break;
                    }
                }
                if (ok) {
                    System.out.println("success for vr: " + ValueRepresentation.getAsString(vr));
                }
            }
            catch (Exception e) {
                System.out.println("Failed for vr: " + ValueRepresentation.getAsString(vr));
                try {
                    byte[] data = { 1, 2 };
                    Attribute bt = AttributeFactory.newAttribute(new AttributeTag("0x5062,0x0005"), vr);
                    bt.setValues(data);
                    byte[] dataOut = bt.getByteValues();
                    for (int d = 0; d < data.length; d++) {
                        if (data[d] != dataOut[d]) {
                            System.out.println("Failed binary data match for vr: " + ValueRepresentation.getAsString(vr));
                            throw new RuntimeException("Failed binary data match for vr: " + ValueRepresentation.getAsString(vr));
                        }
                    }
                    System.out.println("binary success for vr: " + ValueRepresentation.getAsString(vr));
                }
                catch (Exception e2) {
                    System.out.println("binary failed for vr: " + ValueRepresentation.getAsString(vr));
                }

            }
        }
        System.out.println("Done");
        System.exit(0);
    }

    public static void main(String[] args) {
        testVrs();
        new CustomDictionary();
    }

}

