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

import com.pixelmed.dicom.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;


/**
 * An extended DICOM Dictionary that allows the
 * inclusion of private tags and overriding of
 * the standard dictionary.
 *
 * @author Jim Irrer  irrer@umich.edu
 */
public class CustomDictionary extends DicomDictionary {

    private volatile static CustomDictionary instance = null;

    private volatile static HashMap<AttributeTag, Multiplicity> valueMultiplicity = new HashMap<AttributeTag, CustomDictionary.Multiplicity>();

    private static DicomDictionary dict = null;


    /**
     * List of extensions provided by this dictionary.
     */
    private volatile static ArrayList<PrivateTag> extensions = null;

    private void useShortenedAttributeNames() {
        extensions.add(new PrivateTag(dict.getTagFromName("MoveOriginatorApplicationEntityTitle"), "MoveOriginatorApplEntityTitle"));
        extensions.add(new PrivateTag(dict.getTagFromName("SpecificCharacterSetOfFileSetDescriptorFile"), "SpecifCharSetOfFileSetDescFile"));
        extensions.add(new PrivateTag(dict.getTagFromName("OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity"), "OffstOfFrstDerRecOfRootDerEnty"));
        extensions.add(new PrivateTag(dict.getTagFromName("OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity"), "OffstOfLastDerRecOfRootDerEnty"));
        extensions.add(new PrivateTag(dict.getTagFromName("OffsetOfReferencedLowerLevelDirectoryEntity"), "OffstOfRefdLowerLevelDerEnty"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedTransferSyntaxUIDInFile"), "RefdTransferSyntaxUIDInFile"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedRelatedGeneralSOPClassUIDInFile"), "RefdRelatedGenSOPClassUIDInFile"));
        //extensions.add(new PrivateTag(dict.getTagFromName("ReferringPhysicianTelephoneNumbers"), "ReferringPhysicianTelephoneNums"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferringPhysicianIdentificationSequence"), "ReferringPhysicianIdentfSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("CodingSchemeIdentificationSequence"), "CodingSchemeIdentfSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("CodingSchemeResponsibleOrganization"), "CodingSchemeResponsibleOrg"));
        extensions.add(new PrivateTag(dict.getTagFromName("PhysiciansOfRecordIdentificationSequence"), "PhysiciansOfRecordIdentfSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("PerformingPhysicianIdentificationSequence"), "PerformingPhyscnIdentfSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("PhysiciansReadingStudyIdentificationSequence"), "PhyscnsReadingStudyIdentfSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedPerformedProcedureStepSequence"), "ReferencedPerfProcStepSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedStereometricInstanceSequence"), "RefdStereometricInstSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedRealWorldValueMappingInstanceSequence"), "RefdRealWorldValueMappingInstSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("StudiesContainingOtherReferencedInstancesSequence"), "StudiesContaingOtherRefdInstsSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("AnatomicStructureSpaceOrRegionSequence"), "AnatmStructureSpaceOrRegnSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("PrimaryAnatomicStructureModifierSequence"), "PrimaryAnatmStructureModifierSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("TransducerPositionModifierSequence"), "TransducerPosnModifierSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("TransducerOrientationModifierSequence"), "TransducerOrientationModifierSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("AnatomicStructureSpaceOrRegionCodeSequenceTrial"), "AnatmStructSpacOrRegnCodSeqTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("AnatomicPortalOfEntranceCodeSequenceTrial"), "AnatmPortlOfEntranceCodeSeqTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("AnatomicApproachDirectionCodeSequenceTrial"), "AnatmApprchDirctnCodeSeqTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("AnatomicPerspectiveDescriptionTrial"), "AnatmPerspectiveDescriptionTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("AnatomicPerspectiveCodeSequenceTrial"), "AnatmPerspectiveCodeSeqTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("AnatomicLocationOfExaminingInstrumentDescriptionTrial"), "AnatmLocatnOfExamInstrmtDescrTrl"));
        extensions.add(new PrivateTag(dict.getTagFromName("AnatomicLocationOfExaminingInstrumentCodeSequenceTrial"), "AnatmLoctnOfExamInstrmtCodSeqTrl"));
        extensions.add(new PrivateTag(dict.getTagFromName("AnatomicStructureSpaceOrRegionModifierCodeSequenceTrial"), "AnatmStrcSpcOrRegnModfrCodSeqTrl"));
        extensions.add(new PrivateTag(dict.getTagFromName("OnAxisBackgroundAnatomicStructureCodeSequenceTrial"), "OnAxisBkgndAnatmStructCodeSeqTrl"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedPresentationStateSequence"), "RefdPresentationStateSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("RecommendedDisplayFrameRateInFloat"), "RecommendedDsplyFrameRateInFloat"));
        extensions.add(new PrivateTag(dict.getTagFromName("IssuerOfPatientIDQualifiersSequence"), "IssuerOfPatntIDQualifiersSeq"));
        //extensions.add(new PrivateTag(dict.getTagFromName("PatientPrimaryLanguageCodeSequence"), "PatientPrimaryLangCodeSequence"));
        //extensions.add(new PrivateTag(dict.getTagFromName("PatientPrimaryLanguageModifierCodeSequence"), "PatntPrimaryLangModifierCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ClinicalTrialTimePointDescription"), "ClinTrialTimePointDescription"));
        extensions.add(new PrivateTag(dict.getTagFromName("ClinicalTrialCoordinatingCenterName"), "ClinTrialCoordinatingCntrName"));
        extensions.add(new PrivateTag(dict.getTagFromName("DeidentificationMethodCodeSequence"), "DeidentificationMethodCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ClinicalTrialProtocolEthicsCommitteeName"), "ClinTrlProtclEthicsCommitteeName"));
        extensions.add(new PrivateTag(dict.getTagFromName("ClinicalTrialProtocolEthicsCommitteeApprovalNumber"), "ClinTrialProtoEthicCmteApprvlNum"));
        extensions.add(new PrivateTag(dict.getTagFromName("ConsentForClinicalTrialUseSequence"), "ConsentForClinTrialUseSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("IndicationPhysicalPropertySequence"), "IndicationPhysicalPropertySeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("CoordinateSystemTransformSequence"), "CoordinateSystemTransformSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("CoordinateSystemTransformRotationAndScaleMatrix"), "CoordSysTransfrmRtatnAndScalMtrx"));
        extensions.add(new PrivateTag(dict.getTagFromName("CoordinateSystemTransformTranslationMatrix"), "CoordSysTransformTranslationMtrx"));
        extensions.add(new PrivateTag(dict.getTagFromName("FilterMaterialUsedInGainCalibration"), "FilterMaterlUsedInGainCalib"));
        extensions.add(new PrivateTag(dict.getTagFromName("FilterThicknessUsedInGainCalibration"), "FilterThickUsedInGainCalibration"));
        extensions.add(new PrivateTag(dict.getTagFromName("TransmitTransducerSettingsSequence"), "TransmitTransducerSettingsSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReceiveTransducerSettingsSequence"), "ReceiveTransducerSettingsSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ContrastBolusAdministrationRouteSequence"), "ContrastBolusAdmRouteSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("InterventionDrugInformationSequence"), "InterventionDrugInfoSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("AcquisitionTerminationConditionData"), "AcqTerminationConditionData"));
        extensions.add(new PrivateTag(dict.getTagFromName("SecondaryCaptureDeviceManufacturer"), "SecondaryCaptureDevcManufacturer"));
        extensions.add(new PrivateTag(dict.getTagFromName("SecondaryCaptureDeviceManufacturerModelName"), "SecndryCapturDevcManufacrMdlName"));
        extensions.add(new PrivateTag(dict.getTagFromName("SecondaryCaptureDeviceSoftwareVersions"), "SecndryCaptureDevcSoftwrVersions"));
        extensions.add(new PrivateTag(dict.getTagFromName("HardcopyDeviceManufacturerModelName"), "HardcopyDevcManufacrModelName"));
        extensions.add(new PrivateTag(dict.getTagFromName("ContrastBolusIngredientConcentration"), "ContrastBolusIngredientConcntr"));
        extensions.add(new PrivateTag(dict.getTagFromName("RadiopharmaceuticalSpecificActivity"), "RadpharmSpecificActivity"));
        extensions.add(new PrivateTag(dict.getTagFromName("EstimatedRadiographicMagnificationFactor"), "EstmRadiographicMagnifcnFctr"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImageAndFluoroscopyAreaDoseProduct"), "ImageAndFluoroAreaDoseProduct"));
        extensions.add(new PrivateTag(dict.getTagFromName("AcquisitionDeviceProcessingDescription"), "AcqDeviceProcessingDescription"));
        extensions.add(new PrivateTag(dict.getTagFromName("NumberOfTomosynthesisSourceImages"), "NumberOfTomosynthesisSourceImgs"));
        extensions.add(new PrivateTag(dict.getTagFromName("PositionerSecondaryAngleIncrement"), "PosnrSecondaryAngleIncrement"));
        extensions.add(new PrivateTag(dict.getTagFromName("ShutterPresentationColorCIELabValue"), "ShutterPresentatonColorCIELabVal"));
        extensions.add(new PrivateTag(dict.getTagFromName("DigitizingDeviceTransportDirection"), "DigitizingDevcTransportDirection"));
        extensions.add(new PrivateTag(dict.getTagFromName("ProjectionEponymousNameCodeSequence"), "ProjectionEponymousNameCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("DopplerSampleVolumeXPositionRetired"), "DoplSampleVolumeXPositionRetired"));
        extensions.add(new PrivateTag(dict.getTagFromName("DopplerSampleVolumeYPositionRetired"), "DoplSampleVolumeYPositionRetired"));
        extensions.add(new PrivateTag(dict.getTagFromName("ExposuresOnDetectorSinceLastCalibration"), "ExpossOnDetSinceLastCalibration"));
        extensions.add(new PrivateTag(dict.getTagFromName("ExposuresOnDetectorSinceManufactured"), "ExposuresOnDetSinceManufactured"));
        extensions.add(new PrivateTag(dict.getTagFromName("DetectorActivationOffsetFromExposure"), "DetActivationOffsetFromExposure"));
        extensions.add(new PrivateTag(dict.getTagFromName("PixelDataAreaRotationAngleRelativeToFOV"), "PixDataAreaRotatnAngleRelToFOV"));
        extensions.add(new PrivateTag(dict.getTagFromName("MRAcquisitionFrequencyEncodingSteps"), "MRAcqFrequencyEncodingSteps"));
        extensions.add(new PrivateTag(dict.getTagFromName("DiffusionGradientDirectionSequence"), "DiffusionGradientDirctnSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("VelocityEncodingAcquisitionSequence"), "VelocityEncodingAcqSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ParallelReductionFactorInPlaneRetired"), "ParallelReducFctrInPlaneRetired"));
        extensions.add(new PrivateTag(dict.getTagFromName("MRSpectroscopyFOVGeometrySequence"), "MRSpectroscopyFOVGeometrySeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("MRTimingAndRelatedParametersSequence"), "MRTimingAndRelatedParmsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("SpectroscopyAcquisitionDataColumns"), "SpectroscopyAcqDataColumns"));
        extensions.add(new PrivateTag(dict.getTagFromName("ParallelReductionFactorOutOfPlane"), "ParallelReductionFctrOutOfPlane"));
        extensions.add(new PrivateTag(dict.getTagFromName("SpectroscopyAcquisitionOutOfPlanePhaseSteps"), "SpectroAcqOutOfPlanePhaseSteps"));
        extensions.add(new PrivateTag(dict.getTagFromName("ParallelReductionFactorSecondInPlane"), "ParallelReducFctrSecondInPlane"));
        extensions.add(new PrivateTag(dict.getTagFromName("RespiratoryMotionCompensationTechnique"), "ResptryMotionCompsatnTechnique"));
        extensions.add(new PrivateTag(dict.getTagFromName("ApplicableSafetyStandardDescription"), "ApplicableSafetyStandrdDescr"));
        extensions.add(new PrivateTag(dict.getTagFromName("RespiratoryMotionCompensationTechniqueDescription"), "ResptryMotionCompsatnTechnqDesc"));
        extensions.add(new PrivateTag(dict.getTagFromName("ChemicalShiftMinimumIntegrationLimitInHz"), "ChemShiftMinimumIntregLimitInHz"));
        extensions.add(new PrivateTag(dict.getTagFromName("ChemicalShiftMaximumIntegrationLimitInHz"), "ChemShiftMaximumIntregLimitInHz"));
        extensions.add(new PrivateTag(dict.getTagFromName("MRAcquisitionPhaseEncodingStepsInPlane"), "MRAcqPhaseEncodingStepsInPlane"));
        extensions.add(new PrivateTag(dict.getTagFromName("MRAcquisitionPhaseEncodingStepsOutOfPlane"), "MRAcqPhaseEncoStepsOutOfPlane"));
        extensions.add(new PrivateTag(dict.getTagFromName("SpectroscopyAcquisitionPhaseColumns"), "SpectroscopyAcqPhaseColumns"));
        extensions.add(new PrivateTag(dict.getTagFromName("ChemicalShiftMinimumIntegrationLimitInppm"), "ChemShiftMinimumIntregLimitInppm"));
        extensions.add(new PrivateTag(dict.getTagFromName("ChemicalShiftMaximumIntegrationLimitInppm"), "ChemShiftMaximumIntregLimitInppm"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReconstructionTargetCenterPatient"), "ReconstructionTargetCntrPatient"));
        extensions.add(new PrivateTag(dict.getTagFromName("DistanceSourceToDataCollectionCenter"), "DistSourceToDataCollectionCntr"));
        extensions.add(new PrivateTag(dict.getTagFromName("ContrastBolusIngredientCodeSequence"), "ContrastBolusIngredCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ContrastAdministrationProfileSequence"), "ContrastAdmProfileSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ProjectionPixelCalibrationSequence"), "ProjectionPixCalibrationSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("XAXRFFrameCharacteristicsSequence"), "XAXRFFrmCharacteristicsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("DistanceReceptorPlaneToDetectorHousing"), "DistReceptorPlaneToDetHousing"));
        extensions.add(new PrivateTag(dict.getTagFromName("ExposureControlSensingRegionsSequence"), "ExposCntrlSensingRegionsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ExposureControlSensingRegionShape"), "ExposureCntrlSensingRegionShape"));
        extensions.add(new PrivateTag(dict.getTagFromName("ExposureControlSensingRegionLeftVerticalEdge"), "ExposCntrlSensngRegnLtVertEdge"));
        extensions.add(new PrivateTag(dict.getTagFromName("ExposureControlSensingRegionRightVerticalEdge"), "ExposCntrlSensngRegnRtVertEdge"));
        extensions.add(new PrivateTag(dict.getTagFromName("ExposureControlSensingRegionUpperHorizontalEdge"), "ExposCntrlSensngRegnUpprHorzEdge"));
        extensions.add(new PrivateTag(dict.getTagFromName("ExposureControlSensingRegionLowerHorizontalEdge"), "ExposCntrlSensngRegnLowrHorzEdge"));
        extensions.add(new PrivateTag(dict.getTagFromName("CenterOfCircularExposureControlSensingRegion"), "CntrOfCircExposCntrlSensngRegn"));
        extensions.add(new PrivateTag(dict.getTagFromName("RadiusOfCircularExposureControlSensingRegion"), "RadiusOfCircExposCntrlSensngRegn"));
        extensions.add(new PrivateTag(dict.getTagFromName("VerticesOfThePolygonalExposureControlSensingRegion"), "VertcesOfPolyExposCntrlSensngRgn"));
        extensions.add(new PrivateTag(dict.getTagFromName("PositionerIsocenterSecondaryAngle"), "PositionerIsocntrSecondaryAngle"));
        extensions.add(new PrivateTag(dict.getTagFromName("PositionerIsocenterDetectorRotationAngle"), "PosnrIsocntrDetRotationAngle"));
        extensions.add(new PrivateTag(dict.getTagFromName("CArmPositionerTabletopRelationship"), "CArmPosnrTabletopRelationship"));
        extensions.add(new PrivateTag(dict.getTagFromName("IrradiationEventIdentificationSequence"), "IrradiationEventIdentfSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("SecondaryPositionerScanStartAngle"), "SecondaryPosnrScanStartAngle"));
        extensions.add(new PrivateTag(dict.getTagFromName("StartRelativeDensityDifferenceThreshold"), "StrtRelativeDensityDiffThresh"));
        extensions.add(new PrivateTag(dict.getTagFromName("StartCardiacTriggerCountThreshold"), "StrtCardiacTriggerCountThreshold"));
        extensions.add(new PrivateTag(dict.getTagFromName("StartRespiratoryTriggerCountThreshold"), "StrtResptryTriggerCountThreshold"));
        extensions.add(new PrivateTag(dict.getTagFromName("TerminationRelativeDensityThreshold"), "TermnatnRelativeDensityThreshold"));
        extensions.add(new PrivateTag(dict.getTagFromName("TerminationCardiacTriggerCountThreshold"), "TermnatnCardiacTrigrCountThresh"));
        extensions.add(new PrivateTag(dict.getTagFromName("TerminationRespiratoryTriggerCountThreshold"), "TermnatnResptryTrigrCountThresh"));
        extensions.add(new PrivateTag(dict.getTagFromName("PETFrameCorrectionFactorsSequence"), "PETFrameCorrectionFctrsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("NonUniformRadialSamplingCorrected"), "NonUniformRadialSamplingCorr"));
        extensions.add(new PrivateTag(dict.getTagFromName("AttenuationCorrectionTemporalRelationship"), "AttenuationCorrectonTemporalReln"));
        extensions.add(new PrivateTag(dict.getTagFromName("PatientPhysiologicalStateSequence"), "PatntPhysiologicalStateSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("PatientPhysiologicalStateCodeSequence"), "PatntPhysiologicalStateCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("TransducerScanPatternCodeSequence"), "TransducerScanPatternCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("TransducerBeamSteeringCodeSequence"), "TransducerBeamSteeringCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("TransducerApplicationCodeSequence"), "TransducerApplCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("SynchronizationFrameOfReferenceUID"), "SynchronizationFrameOfRefUID"));
        extensions.add(new PrivateTag(dict.getTagFromName("SOPInstanceUIDOfConcatenationSource"), "SOPInstanceUIDOfConctSource"));
        extensions.add(new PrivateTag(dict.getTagFromName("OriginalImageIdentificationNomenclature"), "OriginalImageIdentfNomenclature"));
        extensions.add(new PrivateTag(dict.getTagFromName("NominalCardiacTriggerTimePriorToRPeak"), "NominCardiacTrigrTimPriorToRPeak"));
        extensions.add(new PrivateTag(dict.getTagFromName("ActualCardiacTriggerTimePriorToRPeak"), "ActlCardiacTrigrTimPriorToRPeak"));
        extensions.add(new PrivateTag(dict.getTagFromName("NominalPercentageOfRespiratoryPhase"), "NominPctOfRespiratoryPhase"));
        extensions.add(new PrivateTag(dict.getTagFromName("RespiratorySynchronizationSequence"), "ResptrySynchronizationSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("NominalRespiratoryTriggerDelayTime"), "NominRespiratoryTriggerDelayTime"));
        extensions.add(new PrivateTag(dict.getTagFromName("ActualRespiratoryTriggerDelayTime"), "ActualResptryTriggerDelayTime"));
        extensions.add(new PrivateTag(dict.getTagFromName("PatientOrientationInFrameSequence"), "PatientOrientationInFrmSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ContributingSOPInstancesReferenceSequence"), "ContributingSOPInstsRefSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("LightPathFilterPassThroughWavelength"), "LightPathFilterPassThruWavelen"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImagePathFilterPassThroughWavelength"), "ImgPathFilterPassThruWavelength"));
        extensions.add(new PrivateTag(dict.getTagFromName("PatientEyeMovementCommandCodeSequence"), "PatntEyeMovementCommandCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("AcquisitionDeviceTypeCodeSequence"), "AcqDeviceTypeCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("LightPathFilterTypeStackCodeSequence"), "LightPathFilterTypeStackCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImagePathFilterTypeStackCodeSequence"), "ImgPathFilterTypeStackCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("RelativeImagePositionCodeSequence"), "RelativeImgPositionCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("MydriaticAgentConcentrationUnitsSequence"), "MydriaticAgentConcntrUnitsSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialMeasurementsRightEyeSequence"), "OpthmlcAxlMeasrmntsRtEyeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialMeasurementsLeftEyeSequence"), "OpthmlcAxlMeasrmntsLtEyeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialMeasurementsDeviceType"), "OphthalmicAxlMeasrmntsDevcType"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthMeasurementsType"), "OphthalmicAxlLenMeasurementsType"));
        extensions.add(new PrivateTag(dict.getTagFromName("SourceOfOphthalmicAxialLengthCodeSequence"), "SourceOfOpthmlcAxlLenCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("RefractiveSurgeryTypeCodeSequence"), "RefractSurgeryTypeCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicUltrasoundMethodCodeSequence"), "OpthmlcUltrasoundMethodCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthMeasurementsSequence"), "OpthmlcAxlLenMeasrmntsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("KeratometryMeasurementTypeCodeSequence"), "KeratmtryMeasrmntTypeCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedOphthalmicAxialMeasurementsSequence"), "RefdOpthmlcAxlMeasrmntsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthMeasurementsSegmentNameCodeSequence"), "OpthmlcAxlLenMeasSegmntNamCodSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("RefractiveErrorBeforeRefractiveSurgeryCodeSequence"), "RefractErrBefRefractSurgryCodSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("AnteriorChamberDepthDefinitionCodeSequence"), "AnteriorChamberDepthDefCodeSeq"));
        //extensions.add(new PrivateTag(dict.getTagFromName("SourceofLensThicknessDataCodeSequence"), "SourceofLensThicknessDataCodeSeq"));
        //extensions.add(new PrivateTag(dict.getTagFromName("SourceofAnteriorChamberDepthDataCodeSequence"), "SrcOfAnterChambrDepthDataCodeSeq"));
        //extensions.add(new PrivateTag(dict.getTagFromName("SourceofRefractiveMeasurementsSequence"), "SourceofRefractMeasrmntsSequence"));
        //extensions.add(new PrivateTag(dict.getTagFromName("SourceofRefractiveMeasurementsCodeSequence"), "SourceofRefractMeasrmntsCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthMeasurementModified"), "OphthalmicAxlLenMeasrmntModified"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthDataSourceCodeSequence"), "OpthmlcAxlLenDataSourceCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthDataSourceDescription"), "OpthmlcAxlLenDataSrcDescription"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthMeasurementsTotalLengthSequence"), "OpthmlcAxlLenMeasrmntsTotLenSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthMeasurementsSegmentalLengthSequence"), "OpthmlcAxlLenMeasSegmntlLenSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthMeasurementsLengthSummationSequence"), "OpthmlcAxlLenMeasrmntsLenSummSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("UltrasoundOphthalmicAxialLengthMeasurementsSequence"), "UltrasndOpthmlcAxlLenMesrmntsSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OpticalOphthalmicAxialLengthMeasurementsSequence"), "OpticalOpthmlcAxlLenMeasrmntsSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("UltrasoundSelectedOphthalmicAxialLengthSequence"), "UltrasndSelectedOpthmlcAxlLenSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthSelectionMethodCodeSequence"), "OpthmlcAxlLenSelcntMethodCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OpticalSelectedOphthalmicAxialLengthSequence"), "OpticalSelectedOpthmlcAxlLenSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("SelectedSegmentalOphthalmicAxialLengthSequence"), "SelectedSegmntlOpthmlcAxlLenSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("SelectedTotalOphthalmicAxialLengthSequence"), "SelectedTotalOpthmlcAxlLenSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthQualityMetricSequence"), "OpthmlcAxlLenQualityMetricSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicAxialLengthQualityMetricTypeCodeSequence"), "OpthmlcAxlLenQualMetricTypCodSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("IntraocularLensCalculationsRightEyeSequence"), "IntrclLensCalculationsRtEyeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("IntraocularLensCalculationsLeftEyeSequence"), "IntrclLensCalculationsLtEyeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedOphthalmicAxialLengthMeasurementQCImageSequence"), "RefdOpthmlcAxlLenMesrmntQCImgSeq"));
        //extensions.add(new PrivateTag(dict.getTagFromName("AcquisitonMethodAlgorithmSequence"), "AcquisitonMethodAlgSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicThicknessMapTypeCodeSequence"), "OpthmlcThicknessMapTypeCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicThicknessMappingNormalsSequence"), "OpthmlcThicknessMappingNormlsSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("RetinalThicknessDefinitionCodeSequence"), "RetinalThickDefinitionCodeSeq"));
        //extensions.add(new PrivateTag(dict.getTagFromName("PixelValueMappingtoCodedConceptSequence"), "PixValueMappingtoCodedConceptSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicThicknessMapQualityThresholdSequence"), "OpthmlcThickMapQualityThreshSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicThicknessMapThresholdQualityRating"), "OpthmlcThickMapThreshQualRating"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicThicknessMapQualityRatingSequence"), "OpthmlcThickMapQualityRatingSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("BackgroundIlluminationColorCodeSequence"), "BackgroundIllumColorCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("PatientNotProperlyFixatedQuantity"), "PatntNotProperlyFixatedQuantity"));
        extensions.add(new PrivateTag(dict.getTagFromName("CommentsOnPatientPerformanceOfVisualField"), "CommentsOnPatntPerfmncOfVisField"));
        extensions.add(new PrivateTag(dict.getTagFromName("GlobalDeviationProbabilityNormalsFlag"), "GlobalDevnProbabilityNormalsFlag"));
        extensions.add(new PrivateTag(dict.getTagFromName("AgeCorrectedSensitivityDeviationAlgorithmSequence"), "AgeCorrSenstvtyDevnAlgSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("GeneralizedDefectSensitivityDeviationAlgorithmSequence"), "GenizedDefectSenstvtyDevnAlgSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("LocalDeviationProbabilityNormalsFlag"), "LocalDevnProbabilityNormalsFlag"));
        extensions.add(new PrivateTag(dict.getTagFromName("ShortTermFluctuationProbabilityCalculated"), "ShortTermFluctProbabilityCalc"));
        extensions.add(new PrivateTag(dict.getTagFromName("CorrectedLocalizedDeviationFromNormalCalculated"), "CorrLocalizedDevnFromNormalCalc"));
        extensions.add(new PrivateTag(dict.getTagFromName("CorrectedLocalizedDeviationFromNormal"), "CorrLocalizedDeviationFromNormal"));
        extensions.add(new PrivateTag(dict.getTagFromName("CorrectedLocalizedDeviationFromNormalProbabilityCalculated"), "CorrLocalzdDevnFromNormlProbCalc"));
        extensions.add(new PrivateTag(dict.getTagFromName("CorrectedLocalizedDeviationFromNormalProbability"), "CorrLocalzdDevnFromNormlProb"));
        extensions.add(new PrivateTag(dict.getTagFromName("GlobalDeviationProbabilitySequence"), "GlobalDevnProbabilitySequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("LocalizedDeviationProbabilitySequence"), "LocalizedDevnProbabilitySequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("AgeCorrectedSensitivityDeviationValue"), "AgeCorrSensitivityDeviationValue"));
        extensions.add(new PrivateTag(dict.getTagFromName("VisualFieldTestPointNormalsSequence"), "VisualFieldTestPointNormlsSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("AgeCorrectedSensitivityDeviationProbabilityValue"), "AgeCorrSensitivityDevnProbValue"));
        extensions.add(new PrivateTag(dict.getTagFromName("GeneralizedDefectCorrectedSensitivityDeviationFlag"), "GenizedDefectCorrSenstvtyDevnFlg"));
        extensions.add(new PrivateTag(dict.getTagFromName("GeneralizedDefectCorrectedSensitivityDeviationValue"), "GenizedDefectCorrSenstvtyDevnVal"));
        extensions.add(new PrivateTag(dict.getTagFromName("GeneralizedDefectCorrectedSensitivityDeviationProbabilityValue"), "GenDefctCorrSenstvtyDevnProbVal"));
        extensions.add(new PrivateTag(dict.getTagFromName("RefractiveParametersUsedOnPatientSequence"), "RefractParmsUsedOnPatntSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicPatientClinicalInformationLeftEyeSequence"), "OpthmlcPatntClinInfoLtEyeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OphthalmicPatientClinicalInformationRightEyeSequence"), "OpthmlcPatntClinInfoRtEyeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ScreeningBaselineMeasuredSequence"), "ScreeningBaselineMeasuredSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("VisualFieldTestReliabilityGlobalIndexSequence"), "VisFieldTestRelibltyGlobIndexSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("VisualFieldGlobalResultsIndexSequence"), "VisualFieldGlobalResultsIndexSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("LongitudinalTemporalInformationModified"), "LongitudinalTemporalInfoModified"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedColorPaletteInstanceUID"), "ReferencedColorPaletteInstUID"));
        extensions.add(new PrivateTag(dict.getTagFromName("PixelSpacingCalibrationDescription"), "PixSpacingCalibrationDescription"));
        extensions.add(new PrivateTag(dict.getTagFromName("RedPaletteColorLookupTableDescriptor"), "RedPaletteColorLookupTableDesc"));
        extensions.add(new PrivateTag(dict.getTagFromName("GreenPaletteColorLookupTableDescriptor"), "GreenPaletteColorLookupTableDesc"));
        extensions.add(new PrivateTag(dict.getTagFromName("BluePaletteColorLookupTableDescriptor"), "BluePaletteColorLookupTableDesc"));
        extensions.add(new PrivateTag(dict.getTagFromName("AlphaPaletteColorLookupTableDescriptor"), "AlphaPaletteColorLookupTableDesc"));
        extensions.add(new PrivateTag(dict.getTagFromName("LargeRedPaletteColorLookupTableDescriptor"), "LgRedPaletteColorLookupTableDesc"));
        extensions.add(new PrivateTag(dict.getTagFromName("LargeGreenPaletteColorLookupTableDescriptor"), "LgGreenPaletteColorLkupTableDesc"));
        extensions.add(new PrivateTag(dict.getTagFromName("LargeBluePaletteColorLookupTableDescriptor"), "LgBluePaletteColorLkupTableDesc"));
        extensions.add(new PrivateTag(dict.getTagFromName("LargeRedPaletteColorLookupTableData"), "LgRedPaletteColorLookupTableData"));
        extensions.add(new PrivateTag(dict.getTagFromName("LargeGreenPaletteColorLookupTableData"), "LgGreenPaletteColorLkupTableData"));
        extensions.add(new PrivateTag(dict.getTagFromName("LargeBluePaletteColorLookupTableData"), "LgBluePaletteColorLkupTableData"));
        extensions.add(new PrivateTag(dict.getTagFromName("SegmentedRedPaletteColorLookupTableData"), "SegmntedRedPalttColorLkupTblData"));
        extensions.add(new PrivateTag(dict.getTagFromName("SegmentedGreenPaletteColorLookupTableData"), "SegmntedGrnPalttColorLkupTblData"));
        extensions.add(new PrivateTag(dict.getTagFromName("SegmentedBluePaletteColorLookupTableData"), "SegmntedBluePalttColrLkupTblData"));
        extensions.add(new PrivateTag(dict.getTagFromName("EnhancedPaletteColorLookupTableSequence"), "EnhancedPalttColorLkupTableSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("PixelIntensityRelationshipLUTSequence"), "PixIntensityRelnLUTSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("EquipmentCoordinateSystemIdentification"), "EquipCoordinateSystemIdentf"));
        //extensions.add(new PrivateTag(dict.getTagFromName("RequestingPhysicianIdentificationSequence"), "RequestingPhyscnIdentfSequence"));
        //extensions.add(new PrivateTag(dict.getTagFromName("ScheduledPatientInstitutionResidence"), "ScheduledPatientInstituResidence"));
        extensions.add(new PrivateTag(dict.getTagFromName("PatientClinicalTrialParticipationSequence"), "PatntClinTrialParticipationSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ChannelSensitivityCorrectionFactor"), "ChanSensitivityCorrectionFactor"));
        extensions.add(new PrivateTag(dict.getTagFromName("WaveformDisplayBackgroundCIELabValue"), "WaveformDsplyBackgroundCIELabVal"));
        extensions.add(new PrivateTag(dict.getTagFromName("WaveformPresentationGroupSequence"), "WaveformPresentationGroupSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ChannelRecommendedDisplayCIELabValue"), "ChanRecommendedDsplyCIELabValue"));
        extensions.add(new PrivateTag(dict.getTagFromName("MultiplexedAudioChannelsDescriptionCodeSequence"), "MultplxAudioChansDescrCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ScheduledProcedureStepDescription"), "ScheduledProcStepDescription"));
        //extensions.add(new PrivateTag(dict.getTagFromName("ScheduledPerformingPhysicianIdentificationSequence"), "ScheldPerformingPhyscnIdentfSeq"));
        //extensions.add(new PrivateTag(dict.getTagFromName("AssigningJurisdictionCodeSequence"), "AssigningJurisdictionCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("AssigningAgencyOrDepartmentCodeSequence"), "AssigningAgencyOrDeptCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedNonImageCompositeSOPInstanceSequence"), "RefdNonImgCompositeSOPInstSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("PerformedProcedureStepDescription"), "PerfProcedureStepDescription"));
        extensions.add(new PrivateTag(dict.getTagFromName("PerformedProcedureTypeDescription"), "PerfProcedureTypeDescription"));
        extensions.add(new PrivateTag(dict.getTagFromName("CommentsOnThePerformedProcedureStep"), "CommentsOnThePerfProcedureStep"));
        extensions.add(new PrivateTag(dict.getTagFromName("PerformedProcedureStepDiscontinuationReasonCodeSequence"), "PerfProcStepDisconReasonCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("BillingSuppliesAndDevicesSequence"), "BillingSuppliesAndDevcsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("CommentsOnTheScheduledProcedureStep"), "CommentsOnTheScheduledProcStep"));
        extensions.add(new PrivateTag(dict.getTagFromName("IssuerOfTheContainerIdentifierSequence"), "IssuerOfTheContainerIdentfrSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("AlternateContainerIdentifierSequence"), "AlternateContainerIdentfrSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("IssuerOfTheSpecimenIdentifierSequence"), "IssuerOfTheSpecimenIdentfrSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("SpecimenPreparationStepContentItemSequence"), "SpecmnPresntnStepContentItemSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("SpecimenLocalizationContentItemSequence"), "SpecmnLocalizationContentItemSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImageCenterPointCoordinatesSequence"), "ImgCntrPointCoordinatesSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReasonForRequestedProcedureCodeSequence"), "ReasonForReqdProcCodeSequence"));
        //extensions.add(new PrivateTag(dict.getTagFromName("NamesOfIntendedRecipientsOfResults"), "NamesOfIntendedRecipsOfResults"));
        extensions.add(new PrivateTag(dict.getTagFromName("IntendedRecipientsOfResultsIdentificationSequence"), "IntendedRecipsOfResultsIdentfSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReasonForPerformedProcedureCodeSequence"), "ReasonForPerfProcCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("RequestedProcedureDescriptionTrial"), "RequestedProcDescriptionTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReasonForTheImagingServiceRequest"), "ReasonForTheImgingServiceRequest"));
        extensions.add(new PrivateTag(dict.getTagFromName("PlacerOrderNumberImagingServiceRequestRetired"), "PlacerOrdNumImgingServcReqRetird"));
        extensions.add(new PrivateTag(dict.getTagFromName("FillerOrderNumberImagingServiceRequestRetired"), "FillerOrdNumImgingServcReqRetird"));
        extensions.add(new PrivateTag(dict.getTagFromName("PlacerOrderNumberImagingServiceRequest"), "PlacerOrderNumImgingServiceReq"));
        extensions.add(new PrivateTag(dict.getTagFromName("FillerOrderNumberImagingServiceRequest"), "FillerOrderNumImgingServiceReq"));
        //extensions.add(new PrivateTag(dict.getTagFromName("ConfidentialityConstraintOnPatientDataDescription"), "ConfidConstraintOnPatntDataDescr"));
        extensions.add(new PrivateTag(dict.getTagFromName("GeneralPurposeScheduledProcedureStepStatus"), "GenPurposeScheldProcStepStatus"));
        extensions.add(new PrivateTag(dict.getTagFromName("GeneralPurposePerformedProcedureStepStatus"), "GenPurposePerfProcStepStatus"));
        extensions.add(new PrivateTag(dict.getTagFromName("GeneralPurposeScheduledProcedureStepPriority"), "GenPurposeScheduledProcStepPrio"));
        extensions.add(new PrivateTag(dict.getTagFromName("ScheduledProcessingApplicationsCodeSequence"), "ScheduledProcngApplsCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ScheduledProcedureStepStartDateTime"), "ScheduledProcStepStartDateTime"));
        extensions.add(new PrivateTag(dict.getTagFromName("PerformedProcessingApplicationsCodeSequence"), "PerfProcessingApplsCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ScheduledProcedureStepModificationDateTime"), "ScheduledProcStepModfcnDateTime"));
        extensions.add(new PrivateTag(dict.getTagFromName("ResultingGeneralPurposePerformedProcedureStepsSequence"), "ResltngGenPurposPerfProcStepsSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedGeneralPurposeScheduledProcedureStepSequence"), "RefdGenPurposeScheldProcStepSeq"));
        //extensions.add(new PrivateTag(dict.getTagFromName("ReferencedGeneralPurposeScheduledProcedureStepTransactionUID"), "RefdGenPurpSchdProcStpTranscnUID"));
        extensions.add(new PrivateTag(dict.getTagFromName("ScheduledStationClassCodeSequence"), "ScheldStationClassCodeSequence"));
        //extensions.add(new PrivateTag(dict.getTagFromName("ScheduledStationGeographicLocationCodeSequence"), "ScheldStatnGeographicLocCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("PerformedStationClassCodeSequence"), "PerfStationClassCodeSequence"));
        //extensions.add(new PrivateTag(dict.getTagFromName("PerformedStationGeographicLocationCodeSequence"), "PerfStatnGeographicLocCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("RequestedSubsequentWorkitemCodeSequence"), "ReqdSubsequentWorkitemCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("PerformedProcedureStepStartDateTime"), "PerfProcedureStepStartDateTime"));
        extensions.add(new PrivateTag(dict.getTagFromName("PerformedProcedureStepEndDateTime"), "PerfProcedureStepEndDateTime"));
        extensions.add(new PrivateTag(dict.getTagFromName("ProcedureStepCancellationDateTime"), "ProcStepCancellationDateTime"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedImageRealWorldValueMappingSequence"), "RefdImgRealWorldValueMappingSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("FindingsSourceCategoryCodeSequenceTrial"), "FindingsSrcCategoryCodeSeqTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("DocumentingOrganizationIdentifierCodeSequenceTrial"), "DocingOrgIdentfrCodeSeqTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("MeasurementPrecisionDescriptionTrial"), "MeasrmntPrecisionDescriptonTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("DocumentIdentifierCodeSequenceTrial"), "DocIdentifierCodeSequenceTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("DocumentAuthorIdentifierCodeSequenceTrial"), "DocAuthorIdentfrCodeSeqTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("DocumentingObserverIdentifierCodeSequenceTrial"), "DocingObservrIdentfrCodeSeqTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("ProcedureIdentifierCodeSequenceTrial"), "ProcIdentfrCodeSequenceTrial"));
        //extensions.add(new PrivateTag(dict.getTagFromName("VerifyingObserverIdentificationCodeSequence"), "VerifyingObserverIdentfCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ObjectDirectoryBinaryIdentifierTrial"), "ObjectDerBinaryIdentifierTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("DateOfDocumentOrVerbalTransactionTrial"), "DateOfDocOrVerbalTransctnTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("TimeOfDocumentCreationOrVerbalTransactionTrial"), "TimeOfDocCreatnOrVerblTranscnTrl"));
        extensions.add(new PrivateTag(dict.getTagFromName("ObservationCategoryCodeSequenceTrial"), "ObsvnCategoryCodeSequenceTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedObjectObservationClassTrial"), "ReferencedObjectObsvnClassTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("NumericValueQualifierCodeSequence"), "NumericValueQualifierCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("VerbalSourceIdentifierCodeSequenceTrial"), "VerbalSourceIdentfrCodeSeqTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("CurrentRequestedProcedureEvidenceSequence"), "CurrentReqdProcEvidenceSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("HL7StructuredDocumentReferenceSequence"), "HL7StructuredDocRefSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ObservationSubjectTypeCodeSequenceTrial"), "ObsvnSubjectTypeCodeSeqTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("ObservationSubjectContextFlagTrial"), "ObsvnSubjectContextFlagTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("RelationshipTypeCodeSequenceTrial"), "RelnTypeCodeSequenceTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("SubstanceAdministrationParameterSequence"), "SubstanceAdmParameterSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("UnspecifiedLateralityLensSequence"), "UnspecifiedLatityLensSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("SubjectiveRefractionRightEyeSequence"), "SubjectiveRefractionRtEyeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("SubjectiveRefractionLeftEyeSequence"), "SubjectiveRefractionLtEyeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedRefractiveMeasurementsSequence"), "RefdRefractiveMeasrmntsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("RecommendedAbsentPixelCIELabValue"), "RecommendedAbsentPixCIELabValue"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedImageNavigationSequence"), "ReferencedImgNavigationSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("BottomRightHandCornerOfLocalizerArea"), "BottomRtHandCornerOfLocalizrArea"));
        extensions.add(new PrivateTag(dict.getTagFromName("OpticalPathIdentificationSequence"), "OpticalPathIdentfSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("RowPositionInTotalImagePixelMatrix"), "RowPositionInTotalImgPixelMatrix"));
        extensions.add(new PrivateTag(dict.getTagFromName("ColumnPositionInTotalImagePixelMatrix"), "ColumnPositionInTotalImgPixMtrx"));
        extensions.add(new PrivateTag(dict.getTagFromName("ContainerComponentTypeCodeSequence"), "ContainerComponentTypeCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ContrastBolusIngredientPercentByVolume"), "ContrastBolusIngredPctByVolume"));
        extensions.add(new PrivateTag(dict.getTagFromName("IntravascularOCTFrameTypeSequence"), "IntravascularOCTFrmTypeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("IntravascularFrameContentSequence"), "IntravascularFrmContentSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("IntravascularLongitudinalDistance"), "IntravascularLongitudinalDist"));
        extensions.add(new PrivateTag(dict.getTagFromName("IntravascularOCTFrameContentSequence"), "IntravascularOCTFrmContentSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("RadiopharmaceuticalInformationSequence"), "RadiopharmaceuticalInfoSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("PatientOrientationModifierCodeSequence"), "PatntOrientationModifierCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("PatientGantryRelationshipCodeSequence"), "PatntGantryRelnCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("SegmentedPropertyCategoryCodeSequence"), "SegmntedPropertyCategoryCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("SegmentedPropertyTypeCodeSequence"), "SegmntedPropertyTypeCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("DeformableRegistrationGridSequence"), "DeformableRestrnGridSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("PreDeformationMatrixRegistrationSequence"), "PreDeformationMtrxRestrnSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("PostDeformationMatrixRegistrationSequence"), "PostDeformationMtrxRestrnSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("SegmentSurfaceGenerationAlgorithmIdentificationSequence"), "SegmntSurfaceGenAlgIdentfSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("SegmentSurfaceSourceInstanceSequence"), "SegmentSurfaceSourceInstSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("SurfaceProcessingAlgorithmIdentificationSequence"), "SurfaceProcngAlgIdentfSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("DerivationImplantTemplateSequence"), "DerivImplantTemplateSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("InformationFromManufacturerSequence"), "InfoFromManufacturerSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("NotificationFromManufacturerSequence"), "NotificationFromManufacrSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImplantRegulatoryDisapprovalCodeSequence"), "ImplantRegulatoryDisappCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImplantTemplate3DModelSurfaceNumber"), "ImplantTemplate3DMdlSurfaceNum"));
        extensions.add(new PrivateTag(dict.getTagFromName("MatingFeatureDegreeOfFreedomSequence"), "MatingFeatureDegreeOfFreedomSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("TwoDMatingFeatureCoordinatesSequence"), "TwoDMatingFeatureCoordinatesSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("PlanningLandmarkIdentificationCodeSequence"), "PlanningLandmarkIdentfCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("BoundingBoxTextHorizontalJustification"), "BoundingBoxTextHorzJustification"));
        extensions.add(new PrivateTag(dict.getTagFromName("DisplayedAreaTopLeftHandCornerTrial"), "DsplyedAreaTopLtHandCornerTrial"));
        extensions.add(new PrivateTag(dict.getTagFromName("DisplayedAreaBottomRightHandCornerTrial"), "DsplyedAreaBottomRtHandCornerTrl"));
        extensions.add(new PrivateTag(dict.getTagFromName("DisplayedAreaBottomRightHandCorner"), "DsplyedAreaBottomRightHandCorner"));
        extensions.add(new PrivateTag(dict.getTagFromName("GraphicLayerRecommendedDisplayGrayscaleValue"), "GraphicLayrRecmndDsplyGraysclVal"));
        extensions.add(new PrivateTag(dict.getTagFromName("GraphicLayerRecommendedDisplayRGBValue"), "GraphicLayerRecmndedDsplyRGBVal"));
        extensions.add(new PrivateTag(dict.getTagFromName("ContentCreatorIdentificationCodeSequence"), "ContentCreatorIdentfCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("AlternateContentDescriptionSequence"), "AlternateContentDescriptionSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("PresentationPixelMagnificationRatio"), "PresentationPixelMagnifcnRatio"));
        extensions.add(new PrivateTag(dict.getTagFromName("FrameOfReferenceTransformationMatrixType"), "FrmOfRefTransformationMtrxType"));
        extensions.add(new PrivateTag(dict.getTagFromName("GraphicLayerRecommendedDisplayCIELabValue"), "GraphicLayerRecmndDsplyCIELabVal"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedSpatialRegistrationSequence"), "RefdSpatialRegistrationSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("HangingProtocolDefinitionSequence"), "HangingProtocolDefinitionSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("HangingProtocolUserIdentificationCodeSequence"), "HangingProtocolUserIdentfCodeSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("SelectorSequencePointerPrivateCreator"), "SelectorSeqPointerPrivateCreator"));
        extensions.add(new PrivateTag(dict.getTagFromName("DisplayEnvironmentSpatialPosition"), "DsplyEnvironmentSpatialPosition"));
        extensions.add(new PrivateTag(dict.getTagFromName("DisplaySetPresentationGroupDescription"), "DsplySetPresentationGroupDesc"));
        extensions.add(new PrivateTag(dict.getTagFromName("StructuredDisplayBackgroundCIELabValue"), "StructdDsplyBackgroundCIELabVal"));
        extensions.add(new PrivateTag(dict.getTagFromName("StructuredDisplayImageBoxSequence"), "StructuredDsplyImageBoxSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReformattingOperationInitialViewDirection"), "ReformattingOperInitlViewDirctn"));
        extensions.add(new PrivateTag(dict.getTagFromName("PseudoColorPaletteInstanceReferenceSequence"), "PseudoColorPalttInstRefSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("DisplaySetHorizontalJustification"), "DsplySetHorizontalJustification"));
        extensions.add(new PrivateTag(dict.getTagFromName("ProcedureStepProgressInformationSequence"), "ProcStepProgressInfoSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ProcedureStepCommunicationsURISequence"), "ProcStepCommunicationsURISeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ProcedureStepDiscontinuationReasonCodeSequence"), "ProcStepDisconReasonCodeSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("TableTopLongitudinalAdjustedPosition"), "TableTopLongitudinalAdjustedPosn"));
        extensions.add(new PrivateTag(dict.getTagFromName("DeliveryVerificationImageSequence"), "DeliveryVerificationImgSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("GeneralMachineVerificationSequence"), "GenMachineVerificationSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ConventionalMachineVerificationSequence"), "ConventionalMachineVerifSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ConventionalControlPointVerificationSequence"), "ConventionalCntrlPointVerifSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("IonControlPointVerificationSequence"), "IonCntrlPointVerificationSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("AttributeOccurrencePrivateCreator"), "AttrOccurrencePrivateCreator"));
        extensions.add(new PrivateTag(dict.getTagFromName("ScheduledProcessingParametersSequence"), "ScheduledProcessingParmsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("PerformedProcessingParametersSequence"), "PerformedProcessingParmsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("UnifiedProcedureStepPerformedProcedureSequence"), "UnifiedProcStepPerfProcSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReplacedImplantAssemblyTemplateSequence"), "ReplacedImplantAsmblyTemplateSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OriginalImplantAssemblyTemplateSequence"), "OriginalImplantAsmblyTemplateSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("DerivationImplantAssemblyTemplateSequence"), "DerivImplantAsmblyTemplateSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImplantAssemblyTemplateTargetAnatomySequence"), "ImplantAsmblyTmpltTargetAnatmSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("Component1ReferencedMatingFeatureSetID"), "Component1RefdMatingFeatureSetID"));
        extensions.add(new PrivateTag(dict.getTagFromName("Component1ReferencedMatingFeatureID"), "Component1RefdMatingFeatureID"));
        extensions.add(new PrivateTag(dict.getTagFromName("Component2ReferencedMatingFeatureSetID"), "Component2RefdMatingFeatureSetID"));
        extensions.add(new PrivateTag(dict.getTagFromName("Component2ReferencedMatingFeatureID"), "Component2RefdMatingFeatureID"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReplacedImplantTemplateGroupSequence"), "ReplacedImplantTemplateGroupSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImplantTemplateGroupTargetAnatomySequence"), "ImplantTmpltGroupTargtAnatomySeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImplantTemplateGroupMembersSequence"), "ImplantTemplateGroupMembersSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ThreeDImplantTemplateGroupMemberMatchingPoint"), "ThreeDImplntTmpltGrpMembrMatchPt"));
        extensions.add(new PrivateTag(dict.getTagFromName("ThreeDImplantTemplateGroupMemberMatchingAxes"), "ThreeDImplntTmpltGrpMemMatchAxes"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImplantTemplateGroupMemberMatching2DCoordinatesSequence"), "ImplntTmplGrpMembMatch2DCoordSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("TwoDImplantTemplateGroupMemberMatchingPoint"), "TwoDImplantTmpltGrpMemberMatchPt"));
        extensions.add(new PrivateTag(dict.getTagFromName("TwoDImplantTemplateGroupMemberMatchingAxes"), "TwoDImplntTmpltGrpMembrMatchAxes"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImplantTemplateGroupVariationDimensionSequence"), "ImplantTmpltGroupVartnDimSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImplantTemplateGroupVariationDimensionName"), "ImplantTmpltGroupVartnDimName"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImplantTemplateGroupVariationDimensionRankSequence"), "ImplantTmpltGroupVartnDimRankSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedImplantTemplateGroupMemberID"), "RefdImplantTemplateGroupMemberID"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImplantTemplateGroupVariationDimensionRank"), "ImplantTmpltGroupVartnDimRank"));
        extensions.add(new PrivateTag(dict.getTagFromName("AuthorizationEquipmentCertificationNumber"), "AuthorizationEquipCertifNumber"));
        extensions.add(new PrivateTag(dict.getTagFromName("DigitalSignaturePurposeCodeSequence"), "DigitalSignaturePurposeCodeSeq"));
        //extensions.add(new PrivateTag(dict.getTagFromName("ReferencedDigitalSignatureSequence"), "RefdDigitalSignatureSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("EncryptedContentTransferSyntaxUID"), "EncryptContentTransferSyntaxUID"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReasonForTheAttributeModification"), "ReasonForTheAttrModification"));
        extensions.add(new PrivateTag(dict.getTagFromName("SupportedImageDisplayFormatsSequence"), "SupportedImgDsplyFormatsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ConfigurationInformationDescription"), "ConfigInformationDescription"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedBasicAnnotationBoxSequence"), "RefdBasicAnnotationBoxSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedImageOverlayBoxSequence"), "ReferencedImgOverlayBoxSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedImageBoxSequenceRetired"), "ReferencedImgBoxSequenceRetired"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedPresentationLUTSequence"), "RefdPresentationLUTSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedPrintJobSequencePullStoredPrint"), "RefdPrintJobSeqPullStoredPrint"));
        extensions.add(new PrivateTag(dict.getTagFromName("PrintManagementCapabilitiesSequence"), "PrintManagementCapabilitiesSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("LabelUsingInformationExtractedFromInstances"), "LabelUsingInfoExtrcFromInstances"));
        extensions.add(new PrivateTag(dict.getTagFromName("PreserveCompositeInstancesAfterMediaCreation"), "PresrvCompstInstsAftrMediaCreatn"));
        extensions.add(new PrivateTag(dict.getTagFromName("TotalNumberOfPiecesOfMediaCreated"), "TotalNumOfPiecesOfMediaCreated"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedFrameOfReferenceSequence"), "ReferencedFrmOfReferenceSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ROIElementalCompositionAtomicNumber"), "ROIElemCompositionAtomicNumber"));
        extensions.add(new PrivateTag(dict.getTagFromName("ROIElementalCompositionAtomicMassFraction"), "ROIElemCompositonAtomicMassFract"));
        extensions.add(new PrivateTag(dict.getTagFromName("FrameOfReferenceRelationshipSequence"), "FrmOfRefRelationshipSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("FrameOfReferenceTransformationType"), "FrmOfReferenceTransformationType"));
        extensions.add(new PrivateTag(dict.getTagFromName("FrameOfReferenceTransformationMatrix"), "FrmOfReferenceTransformationMtrx"));
        extensions.add(new PrivateTag(dict.getTagFromName("FrameOfReferenceTransformationComment"), "FrmOfRefTransformationComment"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedTreatmentRecordSequence"), "ReferencedTreatmentRecSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("TreatmentSummaryCalculatedDoseReferenceSequence"), "TreatmentSummaryCalcDoseRefSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("CalculatedDoseReferenceDescription"), "CalcDoseReferenceDescription"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedMeasuredDoseReferenceSequence"), "RefdMeasuredDoseRefSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedMeasuredDoseReferenceNumber"), "RefdMeasuredDoseReferenceNum"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedCalculatedDoseReferenceSequence"), "RefdCalcDoseReferenceSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedCalculatedDoseReferenceNumber"), "ReferencedCalcDoseReferenceNum"));
        extensions.add(new PrivateTag(dict.getTagFromName("BeamLimitingDeviceLeafPairsSequence"), "BeamLimitingDevcLeafPairsSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("TreatmentSummaryMeasuredDoseReferenceSequence"), "TreatmentSumryMeasuredDoseRefSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("RecordedLateralSpreadingDeviceSequence"), "RecordedLatSpreadingDevcSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("TreatmentSessionApplicationSetupSequence"), "TreatmentSessionApplSetupSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("RecordedBrachyAccessoryDeviceSequence"), "RecordedBrachyAccDeviceSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedBrachyAccessoryDeviceNumber"), "ReferencedBrachyAccDeviceNumber"));
        extensions.add(new PrivateTag(dict.getTagFromName("BrachyControlPointDeliveredSequence"), "BrachyCntrlPointDeliveredSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("OrganAtRiskOverdoseVolumeFraction"), "OrganAtRiskOverdoseVolumeFract"));
        extensions.add(new PrivateTag(dict.getTagFromName("BeamLimitingDeviceToleranceSequence"), "BeamLimitingDevcToleranceSeq"));
        extensions.add(new PrivateTag(dict.getTagFromName("BeamLimitingDevicePositionTolerance"), "BeamLimitingDevcPosnTolerance"));
        extensions.add(new PrivateTag(dict.getTagFromName("TableTopVerticalPositionTolerance"), "TableTopVerticalPosnTolerance"));
        extensions.add(new PrivateTag(dict.getTagFromName("TableTopLongitudinalPositionTolerance"), "TblTopLongitudinalPosnTolerance"));
        extensions.add(new PrivateTag(dict.getTagFromName("NumberOfFractionPatternDigitsPerDay"), "NumberOfFractPatternDigitsPerDay"));
        extensions.add(new PrivateTag(dict.getTagFromName("BrachyApplicationSetupDoseSpecificationPoint"), "BrachyApplSetupDoseSpecifnPoint"));
        extensions.add(new PrivateTag(dict.getTagFromName("SourceToBeamLimitingDeviceDistance"), "SourceToBeamLimitingDevcDistance"));
        extensions.add(new PrivateTag(dict.getTagFromName("IsocenterToBeamLimitingDeviceDistance"), "IsocenterToBeamLimitingDevcDist"));
        extensions.add(new PrivateTag(dict.getTagFromName("ImagingDeviceSpecificAcquisitionParameters"), "ImagingDevcSpecificAcqParameters"));
        extensions.add(new PrivateTag(dict.getTagFromName("TotalWedgeTrayWaterEquivalentThickness"), "TotalWedgeTrayWaterEquivThick"));
        extensions.add(new PrivateTag(dict.getTagFromName("TotalBlockTrayWaterEquivalentThickness"), "TotalBlockTrayWaterEquivThick"));
        extensions.add(new PrivateTag(dict.getTagFromName("CumulativeDoseReferenceCoefficient"), "CumulativeDoseRefCoefficient"));
        extensions.add(new PrivateTag(dict.getTagFromName("BeamLimitingDevicePositionSequence"), "BeamLimitingDevcPositionSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("BeamLimitingDeviceRotationDirection"), "BeamLimitingDevcRotationDirctn"));
        extensions.add(new PrivateTag(dict.getTagFromName("TableTopEccentricRotationDirection"), "TableTopEccentricRotationDirctn"));
        extensions.add(new PrivateTag(dict.getTagFromName("TableTopVerticalSetupDisplacement"), "TableTopVerticalSetupDispl"));
        extensions.add(new PrivateTag(dict.getTagFromName("TableTopLongitudinalSetupDisplacement"), "TableTopLongitudinalSetupDispl"));
        extensions.add(new PrivateTag(dict.getTagFromName("SourceEncapsulationNominalThickness"), "SourceEncapsNominalThickness"));
        extensions.add(new PrivateTag(dict.getTagFromName("SourceEncapsulationNominalTransmission"), "SourceEncapsNominalTransmission"));
        extensions.add(new PrivateTag(dict.getTagFromName("BrachyAccessoryDeviceNominalThickness"), "BrachyAccDeviceNominalThickness"));
        extensions.add(new PrivateTag(dict.getTagFromName("BrachyAccessoryDeviceNominalTransmission"), "BrachyAccDevcNominalTransmission"));
        extensions.add(new PrivateTag(dict.getTagFromName("SourceApplicatorWallNominalThickness"), "SrcApplicatorWallNominThickness"));
        extensions.add(new PrivateTag(dict.getTagFromName("SourceApplicatorWallNominalTransmission"), "SrcApplicatorWallNominTransmssn"));
        extensions.add(new PrivateTag(dict.getTagFromName("TotalCompensatorTrayWaterEquivalentThickness"), "TotalCompsatrTrayWaterEquivThick"));
        extensions.add(new PrivateTag(dict.getTagFromName("IsocenterToCompensatorTrayDistance"), "IsocenterToCompsatrTrayDistance"));
        extensions.add(new PrivateTag(dict.getTagFromName("CompensatorRelativeStoppingPowerRatio"), "CompsatrRelatvStoppingPowerRatio"));
        extensions.add(new PrivateTag(dict.getTagFromName("LateralSpreadingDeviceDescription"), "LateralSpreadingDevcDescription"));
        extensions.add(new PrivateTag(dict.getTagFromName("LateralSpreadingDeviceWaterEquivalentThickness"), "LatSpreadngDevcWaterEquivThick"));
        extensions.add(new PrivateTag(dict.getTagFromName("RangeShifterWaterEquivalentThickness"), "RangeShifterWaterEquivThickness"));
        extensions.add(new PrivateTag(dict.getTagFromName("LateralSpreadingDeviceSettingsSequence"), "LatSpreadingDevcSettingsSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("IsocenterToLateralSpreadingDeviceDistance"), "IsocntrToLatSpreadingDevcDist"));
        extensions.add(new PrivateTag(dict.getTagFromName("RangeModulatorGatingStartWaterEquivalentThickness"), "RangeModGatngStrtWaterEquivThick"));
        extensions.add(new PrivateTag(dict.getTagFromName("RangeModulatorGatingStopWaterEquivalentThickness"), "RangeModGatngStopWaterEquivThick"));
        extensions.add(new PrivateTag(dict.getTagFromName("IsocenterToRangeModulatorDistance"), "IsocenterToRangeModulatorDist"));
        extensions.add(new PrivateTag(dict.getTagFromName("SourceToApplicatorMountingPositionDistance"), "SrcToApplicatorMountingPosnDist"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedBrachyApplicationSetupSequence"), "RefdBrachyApplSetupSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedBrachyApplicationSetupNumber"), "ReferencedBrachyApplSetupNumber"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedVerificationImageSequence"), "RefdVerificationImgSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("BrachyReferencedDoseReferenceSequence"), "BrachyRefdDoseReferenceSequence"));
        extensions.add(new PrivateTag(dict.getTagFromName("ReferencedLateralSpreadingDeviceNumber"), "ReferencedLatSpreadingDevcNumber"));
        //extensions.add(new PrivateTag(dict.getTagFromName("InterpretationDiagnosisDescription"), "InterpDiagnosisDescription"));
        extensions.add(new PrivateTag(dict.getTagFromName("InterpretationDiagnosisCodeSequence"), "InterpDiagnosisCodeSequence"));
    }


    /**
     * The following enum is used to enforce DICOM attribute value multiplicity.
     * The names are encoded to reflect how many values may be specified for an
     * attribute. The first number is the minimum number of values, the second
     * number is the maximum number of values, and the third number is the
     * increment to be used in changing the number of values. For example the
     * name M_2_8_2 would indicate that the attribute must have at least two
     * values, a maximum of 8 values, and when values are added, they must be
     * done so two at a time.
     * <p>
     * The leading M is to satisfy Java naming requirements and is otherwise
     * ignored.
     *
     * @author irrer
     */
    public enum Multiplicity {
        M0,
        M1,
        M1_2,
        M1_3,
        M1_32,
        M16,
        M1_8,
        M1_99,
        M1_N,
        M2,
        M2_2N,
        M2_N,
        M3,
        M3_3N,
        M3_N,
        M4,
        M6,
        M6_N,
        M9;

        public final int min;
        public final int max;
        public final int incr;

        private int getMax(int min, String[] fields) {
            if (fields.length == 1) {
                return min;
            }
            if ((fields.length == 2) && (fields[1].indexOf("N") == -1)) {
                return Integer.parseInt(fields[1]);
            }
            return Integer.MAX_VALUE;
        }

        private int getIncr(int min, int max, String[] fields) {
            if (min == max) return 0;
            if ((fields.length == 2) && (fields[1].indexOf("N") != -1) && (fields[1].length() > 1)) {
                return Integer.parseInt(fields[1].substring(0, fields[1].length() - 1));
            }
            return 1;
        }

        private Multiplicity() {
            String[] fields = name().substring(1).split("_");  // Ignore the M and split
            min = Integer.parseInt(fields[0]);   // first value is always minimum
            max = getMax(min, fields);
            incr = getIncr(min, max, fields);
        }

        @Override
        public String toString() {
            return this.name() + "  min:" + min + "  max:" + ((max == Integer.MAX_VALUE) ? "N" : ("" + max)) + "  incr:" + incr;
        }

        public String getName() {
            return this.name().substring(1).replace('_', '-');
        }

    }

    ;


    /**
     * Initialize the value multiplicity table for the dictionary.  The value multiplicity determines how many
     * values a single attribute is allowed to have.
     * <p>
     * See the DICOM standard section 3.6 for details.
     */
    private void initValueMultiplicity() {
        valueMultiplicity.put(dict.getTagFromName("CommandGroupLength"), Multiplicity.M1);
        valueMultiplicity.put(dict.getTagFromName("AffectedSOPClassUID"), Multiplicity.M1);
        valueMultiplicity.put(dict.getTagFromName("SpecificCharacterSet"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ImageType"), Multiplicity.M2_N);
        valueMultiplicity.put(dict.getTagFromName("RelatedGeneralSOPClassUID"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("RetrieveAETitle"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FailedSOPInstanceUIDList"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ModalitiesInStudy"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SOPClassesInStudy"), Multiplicity.M1_N);
        //valueMultiplicity.put(dict.getTagFromName("ReferringPhysicianTelephoneNumbers"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("PhysiciansOfRecord"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("PerformingPhysicianName"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("NameOfPhysiciansReadingStudy"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("OperatorsName"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("AdmittingDiagnosesDescription"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SOPClassesSupported"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ReferencedFrameNumber"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SimpleFrameList"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CalculatedFrameList"), Multiplicity.M3_3N);
        valueMultiplicity.put(dict.getTagFromName("TimeRange"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("EventElapsedTimes"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("EventTimerNames"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FrameType"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("OtherPatientIDs"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("OtherPatientNames"), Multiplicity.M1_N);
        //valueMultiplicity.put(dict.getTagFromName("InsurancePlanIdentification"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("MedicalAlerts"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("Allergies"), Multiplicity.M1_N);
        //valueMultiplicity.put(dict.getTagFromName("PatientTelephoneNumbers"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DeidentificationMethod"), Multiplicity.M1_N);
        //valueMultiplicity.put(dict.getTagFromName("CADFileFormat"), Multiplicity.M1_N);
        //valueMultiplicity.put(dict.getTagFromName("ComponentReferenceSystem"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ComponentManufacturingProcedure"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ComponentManufacturer"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("MaterialThickness"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("MaterialPipeDiameter"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("MaterialIsolationDiameter"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("MaterialGrade"), Multiplicity.M1_N);
        //valueMultiplicity.put(dict.getTagFromName("MaterialPropertiesFileID"), Multiplicity.M1_N);
        //valueMultiplicity.put(dict.getTagFromName("MaterialPropertiesFileFormat"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("IndicationType"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TransformOrderOfAxes"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CoordinateSystemTransformRotationAndScaleMatrix"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CoordinateSystemTransformTranslationMatrix"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DACGainPoints"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DACTimePoints"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DACAmplitude"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CalibrationTime"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CalibrationDate"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ScanningSequence"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SequenceVariant"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ScanOptions"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("Radionuclide"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("EnergyWindowTotalWidth"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("EchoNumbers"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SecondaryCaptureDeviceSoftwareVersions"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("HardcopyDeviceSoftwareVersion"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SoftwareVersions"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ContrastFlowRate"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ContrastFlowDuration"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FrameTimeVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SynchronizationChannel"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("TableVerticalIncrement"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TableLateralIncrement"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TableLongitudinalIncrement"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("RadialPosition"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("RotationOffset"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FieldOfViewDimensions"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("TypeOfFilters"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ImagerPixelSpacing"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("Grid"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FocalDistance"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("XFocusCenter"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("YFocusCenter"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("FocalSpots"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DateOfLastCalibration"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TimeOfLastCalibration"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ConvolutionKernel"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("UpperLowerPixelValues"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("WholeBodyTechnique"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("AcquisitionMatrix"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("PositionerPrimaryAngleIncrement"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("PositionerSecondaryAngleIncrement"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ShutterShape"), Multiplicity.M1_3);
        valueMultiplicity.put(dict.getTagFromName("CenterOfCircularShutter"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("VerticesOfThePolygonalShutter"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("ShutterPresentationColorCIELabValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("CollimatorShape"), Multiplicity.M1_3);
        valueMultiplicity.put(dict.getTagFromName("CenterOfCircularCollimator"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("VerticesOfThePolygonalCollimator"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("PageNumberVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FrameLabelVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FramePrimaryAngleVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FrameSecondaryAngleVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SliceLocationVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DisplayWindowLabelVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("NominalScannedPixelSpacing"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("LesionNumber"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("OutputPower"), Multiplicity.M1_N);
        //valueMultiplicity.put(dict.getTagFromName("TransducerData"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ImageTransformationMatrix"), Multiplicity.M6);
        valueMultiplicity.put(dict.getTagFromName("ImageTranslationVector"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("TableOfXBreakPoints"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TableOfYBreakPoints"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TableOfPixelValues"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TableOfParameterValues"), Multiplicity.M1_N);
        //valueMultiplicity.put(dict.getTagFromName("RWaveTimeVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DetectorBinning"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("DetectorElementPhysicalSize"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("DetectorElementSpacing"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("DetectorActiveDimensions"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("DetectorActiveOrigin"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("FieldOfViewOrigin"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("PixelDataAreaOriginRelativeToFOV"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("GridAspectRatio"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("FilterMaterial"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FilterThicknessMinimum"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FilterThicknessMaximum"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FilterBeamPathLengthMinimum"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FilterBeamPathLengthMaximum"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SpectralWidth"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("ChemicalShiftReference"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("DecoupledNucleus"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("DecouplingFrequency"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("DecouplingChemicalShiftReference"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("TimeDomainFiltering"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("NumberOfZeroFills"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("InversionTimes"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DiffusionGradientOrientation"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("VelocityEncodingDirection"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("TransmitterFrequency"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("ResonantNucleus"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("SlabOrientation"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("MidSlabPosition"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ASLSlabOrientation"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ASLMidSlabPosition"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("DataCollectionCenterPatient"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ReconstructionFieldOfView"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ReconstructionTargetCenterPatient"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ReconstructionPixelSpacing"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("CalciumScoringMassFactorDevice"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ObjectPixelSpacingInCenterOfBeam"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("IntensifierActiveDimensions"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("PhysicalDetectorSize"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("PositionOfIsocenterProjection"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("CenterOfCircularExposureControlSensingRegion"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("VerticesOfThePolygonalExposureControlSensingRegion"), Multiplicity.M2_N);
        valueMultiplicity.put(dict.getTagFromName("FieldOfViewDimensionsInFloat"), Multiplicity.M1_2);
        valueMultiplicity.put(dict.getTagFromName("DepthsOfFocus"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("PatientOrientation"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ImagePosition"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ImagePositionPatient"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ImageOrientation"), Multiplicity.M6);
        valueMultiplicity.put(dict.getTagFromName("ImageOrientationPatient"), Multiplicity.M6);
        valueMultiplicity.put(dict.getTagFromName("MaskingImage"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("Reference"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("OtherStudyNumbers"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("OriginalImageIdentification"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("OriginalImageIdentificationNomenclature"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DimensionIndexValues"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ImagePositionVolume"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ImageOrientationVolume"), Multiplicity.M6);
        valueMultiplicity.put(dict.getTagFromName("ApexPosition"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("VolumeToTransducerMappingMatrix"), Multiplicity.M16);
        valueMultiplicity.put(dict.getTagFromName("VolumeToTableMappingMatrix"), Multiplicity.M16);
        valueMultiplicity.put(dict.getTagFromName("AcquisitionIndex"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("LightPathFilterPassBand"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ImagePathFilterPassBand"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ReferenceCoordinates"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("FrameIncrementPointer"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FrameDimensionPointer"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("PixelSpacing"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ZoomFactor"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ZoomCenter"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("PixelAspectRatio"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ManipulatedImage"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CorrectedImage"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CompressionSequence"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CompressionStepPointers"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("PerimeterTable"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("PredictorConstants"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SequenceOfCompressedData"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DetailsOfCoefficients"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CoefficientCoding"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CoefficientCodingPointers"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DataBlockDescription"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DataBlock"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ZonalMapLocation"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CodeLabel"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CodeTableLocation"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ImageDataLocation"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("WindowCenter"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("WindowWidth"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("WindowCenterWidthExplanation"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("GrayLookupTableDescriptor"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("RedPaletteColorLookupTableDescriptor"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("GreenPaletteColorLookupTableDescriptor"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("BluePaletteColorLookupTableDescriptor"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("AlphaPaletteColorLookupTableDescriptor"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("LargeRedPaletteColorLookupTableDescriptor"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("LargeGreenPaletteColorLookupTableDescriptor"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("LargeBluePaletteColorLookupTableDescriptor"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("GrayLookupTableData"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("BlendingLookupTableDescriptor"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("LossyImageCompressionRatio"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("LossyImageCompressionMethod"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("LUTDescriptor"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("LUTData"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FrameNumbersOfInterest"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FrameOfInterestDescription"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("FrameOfInterestType"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("MaskPointers"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("RWavePointer"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ApplicableFrameRange"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("MaskFrameNumbers"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("MaskSubPixelShift"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ImageProcessingApplied"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("VerticesOfTheRegion"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("PixelShiftFrameRange"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("LUTFrameRange"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("ImageToEquipmentMappingMatrix"), Multiplicity.M16);
        //valueMultiplicity.put(dict.getTagFromName("ScheduledStudyLocationAETitle"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ChannelStatus"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("WaveformDisplayBackgroundCIELabValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ChannelRecommendedDisplayCIELabValue"), Multiplicity.M3);
        //valueMultiplicity.put(dict.getTagFromName("ScheduledStationAETitle"), Multiplicity.M1_N);
        //valueMultiplicity.put(dict.getTagFromName("ScheduledStationName"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ExposedArea"), Multiplicity.M1_2);
        //valueMultiplicity.put(dict.getTagFromName("NamesOfIntendedRecipientsOfResults"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("PersonTelephoneNumbers"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("RealWorldValueLUTData"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("UrgencyOrPriorityAlertsTrial"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ReferencedWaveformChannels"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("ReportStatusIDTrial"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ReferencedSamplePositions"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ReferencedFrameNumbers"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ReferencedTimeOffsets"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ReferencedDateTime"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("PixelCoordinatesSetTrial"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("NumericValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ReferencedContentItemIdentifier"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ListOfMIMETypes"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ProductName"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("VisualAcuityModifiers"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("RecommendedAbsentPixelCIELabValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ImageOrientationSlide"), Multiplicity.M6);
        valueMultiplicity.put(dict.getTagFromName("TopLeftHandCornerOfLocalizerArea"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("BottomRightHandCornerOfLocalizerArea"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("EnergyWindowVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DetectorVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("PhaseVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("RotationVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("RRIntervalVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TimeSlotVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SliceVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("AngularViewVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TimeSliceVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TriggerVector"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SeriesType"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("AxialMash"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("DetectorElementSize"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("SecondaryCountsType"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SecondaryCountsAccumulated"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CountsIncluded"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("HistogramData"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ReferencedSegmentNumber"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("RecommendedDisplayCIELabValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("GridDimensions"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("GridResolution"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("PointPositionAccuracy"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("PointsBoundingBoxCoordinates"), Multiplicity.M6);
        valueMultiplicity.put(dict.getTagFromName("AxisOfRotation"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("CenterOfRotation"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("VectorAccuracy"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ViewOrientationModifier"), Multiplicity.M9);
        valueMultiplicity.put(dict.getTagFromName("RecommendedRotationPoint"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("BoundingRectangle"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("ImplantTemplate3DModelSurfaceNumber"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TwoDMatingPoint"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("TwoDMatingAxes"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("ThreeDDegreeOfFreedomAxis"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("RangeOfFreedom"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ThreeDMatingPoint"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ThreeDMatingAxes"), Multiplicity.M9);
        valueMultiplicity.put(dict.getTagFromName("TwoDDegreeOfFreedomAxis"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("TwoDPointCoordinates"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ThreeDPointCoordinates"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("TwoDLineCoordinates"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("ThreeDLineCoordinates"), Multiplicity.M6);
        valueMultiplicity.put(dict.getTagFromName("TwoDPlaneIntersection"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("ThreeDPlaneOrigin"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ThreeDPlaneNormal"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("BoundingBoxTopLeftHandCorner"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("BoundingBoxBottomRightHandCorner"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("AnchorPoint"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("GraphicData"), Multiplicity.M2_N);
        valueMultiplicity.put(dict.getTagFromName("DisplayedAreaTopLeftHandCornerTrial"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("DisplayedAreaBottomRightHandCornerTrial"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("DisplayedAreaTopLeftHandCorner"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("DisplayedAreaBottomRightHandCorner"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("GraphicLayerRecommendedDisplayRGBValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("PresentationPixelSpacing"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("PresentationPixelAspectRatio"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("TextColorCIELabValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ShadowColorCIELabValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("PatternOnColorCIELabValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("PatternOffColorCIELabValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("RotationPoint"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("GraphicLayerRecommendedDisplayCIELabValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("RelativeTime"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("AbstractPriorValue"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("SelectorSequencePointer"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorSequencePointerPrivateCreator"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorATValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorCSValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorISValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorLOValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorPNValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorSHValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorDSValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorFDValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorFLValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorULValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorUSValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorSLValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("SelectorSSValue"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DisplayEnvironmentSpatialPosition"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("DisplaySetScrollingGroup"), Multiplicity.M2_N);
        valueMultiplicity.put(dict.getTagFromName("ReferenceDisplaySets"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("StructuredDisplayBackgroundCIELabValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("EmptyImageBoxCIELabValue"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("SynchronizedImageBoxList"), Multiplicity.M2_N);
        valueMultiplicity.put(dict.getTagFromName("ThreeDRenderingType"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DisplaySetPatientOrientation"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("DoubleExposureFieldDeltaTrial"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("SelectorSequencePointerItems"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DoubleExposureFieldDelta"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("ThreeDImplantTemplateGroupMemberMatchingPoint"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ThreeDImplantTemplateGroupMemberMatchingAxes"), Multiplicity.M9);
        valueMultiplicity.put(dict.getTagFromName("TwoDImplantTemplateGroupMemberMatchingPoint"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("TwoDImplantTemplateGroupMemberMatchingAxes"), Multiplicity.M4);
        //valueMultiplicity.put(dict.getTagFromName("TopicKeywords"), Multiplicity.M1_32);
        valueMultiplicity.put(dict.getTagFromName("DataElementsSigned"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("OtherMagnificationTypesAvailable"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("OtherSmoothingTypesAvailable"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("PrinterPixelSpacing"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ReferencedOverlayPlaneGroups"), Multiplicity.M1_99);
        valueMultiplicity.put(dict.getTagFromName("FailureAttributes"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("XRayImageReceptorTranslation"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("RTImageOrientation"), Multiplicity.M6);
        valueMultiplicity.put(dict.getTagFromName("ImagePlanePixelSpacing"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("RTImagePosition"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("DiaphragmPosition"), Multiplicity.M4);
        valueMultiplicity.put(dict.getTagFromName("NormalizationPoint"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("GridFrameOffsetVector"), Multiplicity.M2_N);
        valueMultiplicity.put(dict.getTagFromName("TissueHeterogeneityCorrection"), Multiplicity.M1_3);
        valueMultiplicity.put(dict.getTagFromName("DVHNormalizationPoint"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("DVHData"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("ROIDisplayColor"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ContourOffsetVector"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("AttachedContours"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ContourData"), Multiplicity.M3_3N);
        valueMultiplicity.put(dict.getTagFromName("FrameOfReferenceTransformationMatrix"), Multiplicity.M16);
        valueMultiplicity.put(dict.getTagFromName("ScanSpotMetersetsDelivered"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TreatmentProtocols"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("TreatmentSites"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("DoseReferencePointCoordinates"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("BeamDoseSpecificationPoint"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("BrachyApplicationSetupDoseSpecificationPoint"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("LeafPositionBoundaries"), Multiplicity.M3_N);
        valueMultiplicity.put(dict.getTagFromName("ImagingDeviceSpecificAcquisitionParameters"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CompensatorPixelSpacing"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("CompensatorPosition"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("CompensatorTransmissionData"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("CompensatorThicknessData"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("BlockData"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("LeafJawPositions"), Multiplicity.M2_2N);
        valueMultiplicity.put(dict.getTagFromName("IsocenterPosition"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("SurfaceEntryPoint"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ControlPoint3DPosition"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("SourceToCompensatorDistance"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("IsocenterToCompensatorDistances"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("VirtualSourceAxisDistances"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ScanSpotPositionMap"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ScanSpotMetersetWeights"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("ScanningSpotSize"), Multiplicity.M2);
        valueMultiplicity.put(dict.getTagFromName("ControlPointOrientation"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ThreatROIBase"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("ThreatROIExtents"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("CenterOfMass"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("CenterOfPTO"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("BoundingPolygon"), Multiplicity.M6_N);
        valueMultiplicity.put(dict.getTagFromName("AbortReason"), Multiplicity.M1_N);
        valueMultiplicity.put(dict.getTagFromName("OOISize"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("SourceOrientation"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("SourcePosition"), Multiplicity.M3);
        valueMultiplicity.put(dict.getTagFromName("FileSetDescriptorFileID"), Multiplicity.M1_8);
        valueMultiplicity.put(dict.getTagFromName("ReferencedFileID"), Multiplicity.M1_8);
        valueMultiplicity.put(dict.getTagFromName("ReferencedRelatedGeneralSOPClassUIDInFile"), Multiplicity.M1_N);


        int g;
        int e;

        for (e = 0x3100; e <= 0x31ff; e++)
            valueMultiplicity.put(new AttributeTag(0x0020, e), Multiplicity.M1_N);  // SourceImageIDs

        for (e = 0; e <= 0xfff; e++) {
            valueMultiplicity.put(new AttributeTag(0x1000, (e << 4) + 0), Multiplicity.M3);  // EscapeTriplet
            valueMultiplicity.put(new AttributeTag(0x1000, (e << 4) + 1), Multiplicity.M3);  // RunLengthTriplet
            valueMultiplicity.put(new AttributeTag(0x1000, (e << 4) + 3), Multiplicity.M3);  // HuffmanTableTriplet
            valueMultiplicity.put(new AttributeTag(0x1000, (e << 4) + 5), Multiplicity.M3);  // ShiftTableTriplet
        }
        for (e = 0x1010; e <= 0xffff; e++)
            valueMultiplicity.put(new AttributeTag(0x0020, e), Multiplicity.M1_N);  // ZonalMap

        for (g = 5000; g <= 0x50ff; g++) {
            valueMultiplicity.put(new AttributeTag(g, 0x0030), Multiplicity.M1_N);    // AxisUnits,
            valueMultiplicity.put(new AttributeTag(g, 0x0040), Multiplicity.M1_N);    // AxisLabels,
            valueMultiplicity.put(new AttributeTag(g, 0x0104), Multiplicity.M1_N);    // MinimumCoordinateValue,
            valueMultiplicity.put(new AttributeTag(g, 0x0105), Multiplicity.M1_N);    // MaximumCoordinateValue,
            valueMultiplicity.put(new AttributeTag(g, 0x0106), Multiplicity.M1_N);    // CurveRange,
            valueMultiplicity.put(new AttributeTag(g, 0x0110), Multiplicity.M1_N);    // CurveDataDescriptor,
            valueMultiplicity.put(new AttributeTag(g, 0x0112), Multiplicity.M1_N);    // CoordinateStartValue,
            valueMultiplicity.put(new AttributeTag(g, 0x0114), Multiplicity.M1_N);    // CoordinateStepValue,
        }

        for (g = 5000; g <= 0x50ff; g++) {
            valueMultiplicity.put(new AttributeTag(g, 0x0050), Multiplicity.M2);     // OverlayOrigin        
            valueMultiplicity.put(new AttributeTag(g, 0x0066), Multiplicity.M1_N);   // OverlayCompressionStepPointers
            valueMultiplicity.put(new AttributeTag(g, 0x0800), Multiplicity.M1_N);   // OverlayCodeLabel
            valueMultiplicity.put(new AttributeTag(g, 0x0803), Multiplicity.M1_N);   // OverlayCodeTableLocation
            valueMultiplicity.put(new AttributeTag(g, 0x1200), Multiplicity.M1_N);   // OverlaysGray
            valueMultiplicity.put(new AttributeTag(g, 0x1201), Multiplicity.M1_N);   // OverlaysRed
            valueMultiplicity.put(new AttributeTag(g, 0x1202), Multiplicity.M1_N);   // OverlaysGreen
            valueMultiplicity.put(new AttributeTag(g, 0x1203), Multiplicity.M1_N);   // OverlaysBlue
        }

    }

    /**
     * Format an integer as a hex number with leading zeroes
     * padded to the indicated length.
     *
     * @param i   Integer to format.
     * @param len Number of resulting digits.
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

    class OtherDict extends DicomDictionary {
        public TreeSet OtagList = tagList;
        public HashMap OvalueRepresentationsByTag = valueRepresentationsByTag;
        public HashMap OinformationEntityByTag = informationEntityByTag;
        public HashMap OnameByTag = nameByTag;
        public HashMap OtagByName = tagByName;
        public HashMap OfullNameByTag = fullNameByTag;

    }

    private OtherDict otherDict = null;

    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked")
    protected void createTagList() {
        init();
        // super.createTagList();
        tagList = otherDict.OtagList;
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            tagList.add(privateTag.getAttributeTag());
        }
    }


    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked")
    protected void createValueRepresentationsByTag() {
        init();
        // super.createValueRepresentationsByTag();
        valueRepresentationsByTag = otherDict.OvalueRepresentationsByTag;
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            valueRepresentationsByTag.put(privateTag.getAttributeTag(), privateTag.getValueRepresentation());
        }
    }


    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked")
    protected void createInformationEntityByTag() {
        init();
        // super.createInformationEntityByTag();
        informationEntityByTag = otherDict.OinformationEntityByTag;
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            informationEntityByTag.put(privateTag.getAttributeTag(), privateTag.getInformationEntity());
        }
    }


    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked")
    protected void createNameByTag() {
        init();
        // super.createNameByTag();
        nameByTag = otherDict.OnameByTag;
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            nameByTag.put(privateTag.getAttributeTag(), privateTag.getName());
        }
    }


    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked")
    protected void createTagByName() {
        init();
        // super.createTagByName();
        tagByName = otherDict.OtagByName;
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            tagByName.put(privateTag.getName(), privateTag.getAttributeTag());
        }
    }


    /**
     * Override super's method to add extensions.
     */
    @SuppressWarnings("unchecked")
    protected void createFullNameByTag() {
        init();
        // super.createFullNameByTag();
        fullNameByTag = otherDict.OfullNameByTag;
        for (int e = 0; e < extensions.size(); e++) {
            PrivateTag privateTag = extensions.get(e);
            fullNameByTag.put(privateTag.getName(), privateTag.getFullName());
        }
    }


    private synchronized void init() {
        dict = DicomDictionary.StandardDictionary;
        // only do this once.
        if (extensions == null) {
            otherDict = new OtherDict();
            extensions = ClientConfig.getInstance().getPrivateTagList();
            if (DicomClient.getRestrictXmlTagsToLength32()) {
                useShortenedAttributeNames();
            }
            initValueMultiplicity();
        }
    }


    /**
     * Default constructor.
     */
    private CustomDictionary() {
        super();
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
            AttributeTag tag = (AttributeTag) (oTag);
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

    public Multiplicity getValueMultiplicity(AttributeTag tag) {
        Multiplicity m = valueMultiplicity.get(tag);
        if (m != null) return m;
        if ((tag == null) || (getValueRepresentationFromTag(tag) == null)) return Multiplicity.M0;
        if (ValueRepresentation.isSequenceVR(getValueRepresentationFromTag(tag))) return Multiplicity.M0;
        return Multiplicity.M1;
    }

    public static Multiplicity getVM(Attribute attribute) {
        return getInstance().getValueMultiplicity(attribute.getTag());
    }

    public static Multiplicity getVM(AttributeLocation attributeLocation) {
        return getInstance().getValueMultiplicity(attributeLocation.getAttribute().getTag());
    }

    public static String getName(Attribute attribute) {
        return getInstance().getNameFromTag(attribute.getTag());
    }

    public static String getName(AttributeLocation attributeLocation) {
        return getInstance().getNameFromTag(attributeLocation.getAttribute().getTag());
    }

    public static void main(String[] args) {
        CustomDictionary cd = new CustomDictionary();

        AttributeTag[] tagList = {
                TagFromName.PatientID,
                dict.getTagFromName("VerticesOfThePolygonalCollimator"),
                new AttributeTag(0x1000, (23 << 4) + 1)
        };
        for (AttributeTag tag : tagList) {
            Multiplicity m = cd.getValueMultiplicity(tag);
            System.out.println("Tag: " + tag + "    Name: " + cd.getNameFromTag(tag) + "    Multiplicity: " + m);
        }

        System.out.println("varian tag: " + cd.getNameFromTag(new AttributeTag(0x3249, 0x0010)));
    }

}

