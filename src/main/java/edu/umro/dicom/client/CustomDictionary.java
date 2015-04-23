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

import java.lang.NumberFormatException;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.ValueRepresentation;
import com.pixelmed.dicom.AttributeTag;


/**
 * An extended DICOM Dictionary that allows the
 * inclusion of private tags and overriding of
 * the standard dictionary.
 *  
 * @author Jim Irrer  irrer@umich.edu 
 *
 */
public class CustomDictionary extends DicomDictionary {

    private volatile static CustomDictionary instance = null;

    private volatile static HashMap<AttributeTag, Multiplicity> valueMultiplicity = new HashMap<AttributeTag, CustomDictionary.Multiplicity>();
    
    /** List of extensions provided by this dictionary. */
    private volatile static ArrayList<PrivateTag> extensions = null;

    private void useShortenedAttributeNames() {
        extensions.add(new PrivateTag(TagFromName.MoveOriginatorApplicationEntityTitle, "MoveOriginatorApplEntityTitle"));
        extensions.add(new PrivateTag(TagFromName.SpecificCharacterSetOfFileSetDescriptorFile, "SpecifCharSetOfFileSetDescFile"));
        extensions.add(new PrivateTag(TagFromName.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity, "OffstOfFrstDerRecOfRootDerEnty"));
        extensions.add(new PrivateTag(TagFromName.OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity, "OffstOfLastDerRecOfRootDerEnty"));
        extensions.add(new PrivateTag(TagFromName.OffsetOfReferencedLowerLevelDirectoryEntity, "OffstOfRefdLowerLevelDerEnty"));
        extensions.add(new PrivateTag(TagFromName.ReferencedTransferSyntaxUIDInFile, "RefdTransferSyntaxUIDInFile"));
        extensions.add(new PrivateTag(TagFromName.ReferencedRelatedGeneralSOPClassUIDInFile, "RefdRelatedGenSOPClassUIDInFile"));
        extensions.add(new PrivateTag(TagFromName.ReferringPhysicianTelephoneNumbers, "ReferringPhysicianTelephoneNums"));
        extensions.add(new PrivateTag(TagFromName.ReferringPhysicianIdentificationSequence, "ReferringPhysicianIdentfSequence"));
        extensions.add(new PrivateTag(TagFromName.CodingSchemeIdentificationSequence, "CodingSchemeIdentfSequence"));
        extensions.add(new PrivateTag(TagFromName.CodingSchemeResponsibleOrganization, "CodingSchemeResponsibleOrg"));
        extensions.add(new PrivateTag(TagFromName.PhysiciansOfRecordIdentificationSequence, "PhysiciansOfRecordIdentfSequence"));
        extensions.add(new PrivateTag(TagFromName.PerformingPhysicianIdentificationSequence, "PerformingPhyscnIdentfSequence"));
        extensions.add(new PrivateTag(TagFromName.PhysiciansReadingStudyIdentificationSequence, "PhyscnsReadingStudyIdentfSeq"));
        extensions.add(new PrivateTag(TagFromName.ReferencedPerformedProcedureStepSequence, "ReferencedPerfProcStepSequence"));
        extensions.add(new PrivateTag(TagFromName.ReferencedStereometricInstanceSequence, "RefdStereometricInstSequence"));
        extensions.add(new PrivateTag(TagFromName.ReferencedRealWorldValueMappingInstanceSequence, "RefdRealWorldValueMappingInstSeq"));
        extensions.add(new PrivateTag(TagFromName.StudiesContainingOtherReferencedInstancesSequence, "StudiesContaingOtherRefdInstsSeq"));
        extensions.add(new PrivateTag(TagFromName.AnatomicStructureSpaceOrRegionSequence, "AnatmStructureSpaceOrRegnSeq"));
        extensions.add(new PrivateTag(TagFromName.PrimaryAnatomicStructureModifierSequence, "PrimaryAnatmStructureModifierSeq"));
        extensions.add(new PrivateTag(TagFromName.TransducerPositionModifierSequence, "TransducerPosnModifierSequence"));
        extensions.add(new PrivateTag(TagFromName.TransducerOrientationModifierSequence, "TransducerOrientationModifierSeq"));
        extensions.add(new PrivateTag(TagFromName.AnatomicStructureSpaceOrRegionCodeSequenceTrial, "AnatmStructSpacOrRegnCodSeqTrial"));
        extensions.add(new PrivateTag(TagFromName.AnatomicPortalOfEntranceCodeSequenceTrial, "AnatmPortlOfEntranceCodeSeqTrial"));
        extensions.add(new PrivateTag(TagFromName.AnatomicApproachDirectionCodeSequenceTrial, "AnatmApprchDirctnCodeSeqTrial"));
        extensions.add(new PrivateTag(TagFromName.AnatomicPerspectiveDescriptionTrial, "AnatmPerspectiveDescriptionTrial"));
        extensions.add(new PrivateTag(TagFromName.AnatomicPerspectiveCodeSequenceTrial, "AnatmPerspectiveCodeSeqTrial"));
        extensions.add(new PrivateTag(TagFromName.AnatomicLocationOfExaminingInstrumentDescriptionTrial, "AnatmLocatnOfExamInstrmtDescrTrl"));
        extensions.add(new PrivateTag(TagFromName.AnatomicLocationOfExaminingInstrumentCodeSequenceTrial, "AnatmLoctnOfExamInstrmtCodSeqTrl"));
        extensions.add(new PrivateTag(TagFromName.AnatomicStructureSpaceOrRegionModifierCodeSequenceTrial, "AnatmStrcSpcOrRegnModfrCodSeqTrl"));
        extensions.add(new PrivateTag(TagFromName.OnAxisBackgroundAnatomicStructureCodeSequenceTrial, "OnAxisBkgndAnatmStructCodeSeqTrl"));
        extensions.add(new PrivateTag(TagFromName.ReferencedPresentationStateSequence, "RefdPresentationStateSequence"));
        extensions.add(new PrivateTag(TagFromName.RecommendedDisplayFrameRateInFloat, "RecommendedDsplyFrameRateInFloat"));
        extensions.add(new PrivateTag(TagFromName.IssuerOfPatientIDQualifiersSequence, "IssuerOfPatntIDQualifiersSeq"));
        extensions.add(new PrivateTag(TagFromName.PatientPrimaryLanguageCodeSequence, "PatientPrimaryLangCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.PatientPrimaryLanguageModifierCodeSequence, "PatntPrimaryLangModifierCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.ClinicalTrialTimePointDescription, "ClinTrialTimePointDescription"));
        extensions.add(new PrivateTag(TagFromName.ClinicalTrialCoordinatingCenterName, "ClinTrialCoordinatingCntrName"));
        extensions.add(new PrivateTag(TagFromName.DeidentificationMethodCodeSequence, "DeidentificationMethodCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.ClinicalTrialProtocolEthicsCommitteeName, "ClinTrlProtclEthicsCommitteeName"));
        extensions.add(new PrivateTag(TagFromName.ClinicalTrialProtocolEthicsCommitteeApprovalNumber, "ClinTrialProtoEthicCmteApprvlNum"));
        extensions.add(new PrivateTag(TagFromName.ConsentForClinicalTrialUseSequence, "ConsentForClinTrialUseSequence"));
        extensions.add(new PrivateTag(TagFromName.IndicationPhysicalPropertySequence, "IndicationPhysicalPropertySeq"));
        extensions.add(new PrivateTag(TagFromName.CoordinateSystemTransformSequence, "CoordinateSystemTransformSeq"));
        extensions.add(new PrivateTag(TagFromName.CoordinateSystemTransformRotationAndScaleMatrix, "CoordSysTransfrmRtatnAndScalMtrx"));
        extensions.add(new PrivateTag(TagFromName.CoordinateSystemTransformTranslationMatrix, "CoordSysTransformTranslationMtrx"));
        extensions.add(new PrivateTag(TagFromName.FilterMaterialUsedInGainCalibration, "FilterMaterlUsedInGainCalib"));
        extensions.add(new PrivateTag(TagFromName.FilterThicknessUsedInGainCalibration, "FilterThickUsedInGainCalibration"));
        extensions.add(new PrivateTag(TagFromName.TransmitTransducerSettingsSequence, "TransmitTransducerSettingsSeq"));
        extensions.add(new PrivateTag(TagFromName.ReceiveTransducerSettingsSequence, "ReceiveTransducerSettingsSeq"));
        extensions.add(new PrivateTag(TagFromName.ContrastBolusAdministrationRouteSequence, "ContrastBolusAdmRouteSequence"));
        extensions.add(new PrivateTag(TagFromName.InterventionDrugInformationSequence, "InterventionDrugInfoSequence"));
        extensions.add(new PrivateTag(TagFromName.AcquisitionTerminationConditionData, "AcqTerminationConditionData"));
        extensions.add(new PrivateTag(TagFromName.SecondaryCaptureDeviceManufacturer, "SecondaryCaptureDevcManufacturer"));
        extensions.add(new PrivateTag(TagFromName.SecondaryCaptureDeviceManufacturerModelName, "SecndryCapturDevcManufacrMdlName"));
        extensions.add(new PrivateTag(TagFromName.SecondaryCaptureDeviceSoftwareVersions, "SecndryCaptureDevcSoftwrVersions"));
        extensions.add(new PrivateTag(TagFromName.HardcopyDeviceManufacturerModelName, "HardcopyDevcManufacrModelName"));
        extensions.add(new PrivateTag(TagFromName.ContrastBolusIngredientConcentration, "ContrastBolusIngredientConcntr"));
        extensions.add(new PrivateTag(TagFromName.RadiopharmaceuticalSpecificActivity, "RadpharmSpecificActivity"));
        extensions.add(new PrivateTag(TagFromName.EstimatedRadiographicMagnificationFactor, "EstmRadiographicMagnifcnFctr"));
        extensions.add(new PrivateTag(TagFromName.ImageAndFluoroscopyAreaDoseProduct, "ImageAndFluoroAreaDoseProduct"));
        extensions.add(new PrivateTag(TagFromName.AcquisitionDeviceProcessingDescription, "AcqDeviceProcessingDescription"));
        extensions.add(new PrivateTag(TagFromName.NumberOfTomosynthesisSourceImages, "NumberOfTomosynthesisSourceImgs"));
        extensions.add(new PrivateTag(TagFromName.PositionerSecondaryAngleIncrement, "PosnrSecondaryAngleIncrement"));
        extensions.add(new PrivateTag(TagFromName.ShutterPresentationColorCIELabValue, "ShutterPresentatonColorCIELabVal"));
        extensions.add(new PrivateTag(TagFromName.DigitizingDeviceTransportDirection, "DigitizingDevcTransportDirection"));
        extensions.add(new PrivateTag(TagFromName.ProjectionEponymousNameCodeSequence, "ProjectionEponymousNameCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.DopplerSampleVolumeXPositionRetired, "DoplSampleVolumeXPositionRetired"));
        extensions.add(new PrivateTag(TagFromName.DopplerSampleVolumeYPositionRetired, "DoplSampleVolumeYPositionRetired"));
        extensions.add(new PrivateTag(TagFromName.ExposuresOnDetectorSinceLastCalibration, "ExpossOnDetSinceLastCalibration"));
        extensions.add(new PrivateTag(TagFromName.ExposuresOnDetectorSinceManufactured, "ExposuresOnDetSinceManufactured"));
        extensions.add(new PrivateTag(TagFromName.DetectorActivationOffsetFromExposure, "DetActivationOffsetFromExposure"));
        extensions.add(new PrivateTag(TagFromName.PixelDataAreaRotationAngleRelativeToFOV, "PixDataAreaRotatnAngleRelToFOV"));
        extensions.add(new PrivateTag(TagFromName.MRAcquisitionFrequencyEncodingSteps, "MRAcqFrequencyEncodingSteps"));
        extensions.add(new PrivateTag(TagFromName.DiffusionGradientDirectionSequence, "DiffusionGradientDirctnSequence"));
        extensions.add(new PrivateTag(TagFromName.VelocityEncodingAcquisitionSequence, "VelocityEncodingAcqSequence"));
        extensions.add(new PrivateTag(TagFromName.ParallelReductionFactorInPlaneRetired, "ParallelReducFctrInPlaneRetired"));
        extensions.add(new PrivateTag(TagFromName.MRSpectroscopyFOVGeometrySequence, "MRSpectroscopyFOVGeometrySeq"));
        extensions.add(new PrivateTag(TagFromName.MRTimingAndRelatedParametersSequence, "MRTimingAndRelatedParmsSequence"));
        extensions.add(new PrivateTag(TagFromName.SpectroscopyAcquisitionDataColumns, "SpectroscopyAcqDataColumns"));
        extensions.add(new PrivateTag(TagFromName.ParallelReductionFactorOutOfPlane, "ParallelReductionFctrOutOfPlane"));
        extensions.add(new PrivateTag(TagFromName.SpectroscopyAcquisitionOutOfPlanePhaseSteps, "SpectroAcqOutOfPlanePhaseSteps"));
        extensions.add(new PrivateTag(TagFromName.ParallelReductionFactorSecondInPlane, "ParallelReducFctrSecondInPlane"));
        extensions.add(new PrivateTag(TagFromName.RespiratoryMotionCompensationTechnique, "ResptryMotionCompsatnTechnique"));
        extensions.add(new PrivateTag(TagFromName.ApplicableSafetyStandardDescription, "ApplicableSafetyStandrdDescr"));
        extensions.add(new PrivateTag(TagFromName.RespiratoryMotionCompensationTechniqueDescription, "ResptryMotionCompsatnTechnqDesc"));
        extensions.add(new PrivateTag(TagFromName.ChemicalShiftMinimumIntegrationLimitInHz, "ChemShiftMinimumIntregLimitInHz"));
        extensions.add(new PrivateTag(TagFromName.ChemicalShiftMaximumIntegrationLimitInHz, "ChemShiftMaximumIntregLimitInHz"));
        extensions.add(new PrivateTag(TagFromName.MRAcquisitionPhaseEncodingStepsInPlane, "MRAcqPhaseEncodingStepsInPlane"));
        extensions.add(new PrivateTag(TagFromName.MRAcquisitionPhaseEncodingStepsOutOfPlane, "MRAcqPhaseEncoStepsOutOfPlane"));
        extensions.add(new PrivateTag(TagFromName.SpectroscopyAcquisitionPhaseColumns, "SpectroscopyAcqPhaseColumns"));
        extensions.add(new PrivateTag(TagFromName.ChemicalShiftMinimumIntegrationLimitInppm, "ChemShiftMinimumIntregLimitInppm"));
        extensions.add(new PrivateTag(TagFromName.ChemicalShiftMaximumIntegrationLimitInppm, "ChemShiftMaximumIntregLimitInppm"));
        extensions.add(new PrivateTag(TagFromName.ReconstructionTargetCenterPatient, "ReconstructionTargetCntrPatient"));
        extensions.add(new PrivateTag(TagFromName.DistanceSourceToDataCollectionCenter, "DistSourceToDataCollectionCntr"));
        extensions.add(new PrivateTag(TagFromName.ContrastBolusIngredientCodeSequence, "ContrastBolusIngredCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.ContrastAdministrationProfileSequence, "ContrastAdmProfileSequence"));
        extensions.add(new PrivateTag(TagFromName.ProjectionPixelCalibrationSequence, "ProjectionPixCalibrationSequence"));
        extensions.add(new PrivateTag(TagFromName.XAXRFFrameCharacteristicsSequence, "XAXRFFrmCharacteristicsSequence"));
        extensions.add(new PrivateTag(TagFromName.DistanceReceptorPlaneToDetectorHousing, "DistReceptorPlaneToDetHousing"));
        extensions.add(new PrivateTag(TagFromName.ExposureControlSensingRegionsSequence, "ExposCntrlSensingRegionsSequence"));
        extensions.add(new PrivateTag(TagFromName.ExposureControlSensingRegionShape, "ExposureCntrlSensingRegionShape"));
        extensions.add(new PrivateTag(TagFromName.ExposureControlSensingRegionLeftVerticalEdge, "ExposCntrlSensngRegnLtVertEdge"));
        extensions.add(new PrivateTag(TagFromName.ExposureControlSensingRegionRightVerticalEdge, "ExposCntrlSensngRegnRtVertEdge"));
        extensions.add(new PrivateTag(TagFromName.ExposureControlSensingRegionUpperHorizontalEdge, "ExposCntrlSensngRegnUpprHorzEdge"));
        extensions.add(new PrivateTag(TagFromName.ExposureControlSensingRegionLowerHorizontalEdge, "ExposCntrlSensngRegnLowrHorzEdge"));
        extensions.add(new PrivateTag(TagFromName.CenterOfCircularExposureControlSensingRegion, "CntrOfCircExposCntrlSensngRegn"));
        extensions.add(new PrivateTag(TagFromName.RadiusOfCircularExposureControlSensingRegion, "RadiusOfCircExposCntrlSensngRegn"));
        extensions.add(new PrivateTag(TagFromName.VerticesOfThePolygonalExposureControlSensingRegion, "VertcesOfPolyExposCntrlSensngRgn"));
        extensions.add(new PrivateTag(TagFromName.PositionerIsocenterSecondaryAngle, "PositionerIsocntrSecondaryAngle"));
        extensions.add(new PrivateTag(TagFromName.PositionerIsocenterDetectorRotationAngle, "PosnrIsocntrDetRotationAngle"));
        extensions.add(new PrivateTag(TagFromName.CArmPositionerTabletopRelationship, "CArmPosnrTabletopRelationship"));
        extensions.add(new PrivateTag(TagFromName.IrradiationEventIdentificationSequence, "IrradiationEventIdentfSequence"));
        extensions.add(new PrivateTag(TagFromName.SecondaryPositionerScanStartAngle, "SecondaryPosnrScanStartAngle"));
        extensions.add(new PrivateTag(TagFromName.StartRelativeDensityDifferenceThreshold, "StrtRelativeDensityDiffThresh"));
        extensions.add(new PrivateTag(TagFromName.StartCardiacTriggerCountThreshold, "StrtCardiacTriggerCountThreshold"));
        extensions.add(new PrivateTag(TagFromName.StartRespiratoryTriggerCountThreshold, "StrtResptryTriggerCountThreshold"));
        extensions.add(new PrivateTag(TagFromName.TerminationRelativeDensityThreshold, "TermnatnRelativeDensityThreshold"));
        extensions.add(new PrivateTag(TagFromName.TerminationCardiacTriggerCountThreshold, "TermnatnCardiacTrigrCountThresh"));
        extensions.add(new PrivateTag(TagFromName.TerminationRespiratoryTriggerCountThreshold, "TermnatnResptryTrigrCountThresh"));
        extensions.add(new PrivateTag(TagFromName.PETFrameCorrectionFactorsSequence, "PETFrameCorrectionFctrsSequence"));
        extensions.add(new PrivateTag(TagFromName.NonUniformRadialSamplingCorrected, "NonUniformRadialSamplingCorr"));
        extensions.add(new PrivateTag(TagFromName.AttenuationCorrectionTemporalRelationship, "AttenuationCorrectonTemporalReln"));
        extensions.add(new PrivateTag(TagFromName.PatientPhysiologicalStateSequence, "PatntPhysiologicalStateSequence"));
        extensions.add(new PrivateTag(TagFromName.PatientPhysiologicalStateCodeSequence, "PatntPhysiologicalStateCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.TransducerScanPatternCodeSequence, "TransducerScanPatternCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.TransducerBeamSteeringCodeSequence, "TransducerBeamSteeringCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.TransducerApplicationCodeSequence, "TransducerApplCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.SynchronizationFrameOfReferenceUID, "SynchronizationFrameOfRefUID"));
        extensions.add(new PrivateTag(TagFromName.SOPInstanceUIDOfConcatenationSource, "SOPInstanceUIDOfConctSource"));
        extensions.add(new PrivateTag(TagFromName.OriginalImageIdentificationNomenclature, "OriginalImageIdentfNomenclature"));
        extensions.add(new PrivateTag(TagFromName.NominalCardiacTriggerTimePriorToRPeak, "NominCardiacTrigrTimPriorToRPeak"));
        extensions.add(new PrivateTag(TagFromName.ActualCardiacTriggerTimePriorToRPeak, "ActlCardiacTrigrTimPriorToRPeak"));
        extensions.add(new PrivateTag(TagFromName.NominalPercentageOfRespiratoryPhase, "NominPctOfRespiratoryPhase"));
        extensions.add(new PrivateTag(TagFromName.RespiratorySynchronizationSequence, "ResptrySynchronizationSequence"));
        extensions.add(new PrivateTag(TagFromName.NominalRespiratoryTriggerDelayTime, "NominRespiratoryTriggerDelayTime"));
        extensions.add(new PrivateTag(TagFromName.ActualRespiratoryTriggerDelayTime, "ActualResptryTriggerDelayTime"));
        extensions.add(new PrivateTag(TagFromName.PatientOrientationInFrameSequence, "PatientOrientationInFrmSequence"));
        extensions.add(new PrivateTag(TagFromName.ContributingSOPInstancesReferenceSequence, "ContributingSOPInstsRefSequence"));
        extensions.add(new PrivateTag(TagFromName.LightPathFilterPassThroughWavelength, "LightPathFilterPassThruWavelen"));
        extensions.add(new PrivateTag(TagFromName.ImagePathFilterPassThroughWavelength, "ImgPathFilterPassThruWavelength"));
        extensions.add(new PrivateTag(TagFromName.PatientEyeMovementCommandCodeSequence, "PatntEyeMovementCommandCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.AcquisitionDeviceTypeCodeSequence, "AcqDeviceTypeCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.LightPathFilterTypeStackCodeSequence, "LightPathFilterTypeStackCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.ImagePathFilterTypeStackCodeSequence, "ImgPathFilterTypeStackCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.RelativeImagePositionCodeSequence, "RelativeImgPositionCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.MydriaticAgentConcentrationUnitsSequence, "MydriaticAgentConcntrUnitsSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialMeasurementsRightEyeSequence, "OpthmlcAxlMeasrmntsRtEyeSequence"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialMeasurementsLeftEyeSequence, "OpthmlcAxlMeasrmntsLtEyeSequence"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialMeasurementsDeviceType, "OphthalmicAxlMeasrmntsDevcType"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthMeasurementsType, "OphthalmicAxlLenMeasurementsType"));
        extensions.add(new PrivateTag(TagFromName.SourceOfOphthalmicAxialLengthCodeSequence, "SourceOfOpthmlcAxlLenCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.RefractiveSurgeryTypeCodeSequence, "RefractSurgeryTypeCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicUltrasoundMethodCodeSequence, "OpthmlcUltrasoundMethodCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthMeasurementsSequence, "OpthmlcAxlLenMeasrmntsSequence"));
        extensions.add(new PrivateTag(TagFromName.KeratometryMeasurementTypeCodeSequence, "KeratmtryMeasrmntTypeCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.ReferencedOphthalmicAxialMeasurementsSequence, "RefdOpthmlcAxlMeasrmntsSequence"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthMeasurementsSegmentNameCodeSequence, "OpthmlcAxlLenMeasSegmntNamCodSeq"));
        extensions.add(new PrivateTag(TagFromName.RefractiveErrorBeforeRefractiveSurgeryCodeSequence, "RefractErrBefRefractSurgryCodSeq"));
        extensions.add(new PrivateTag(TagFromName.AnteriorChamberDepthDefinitionCodeSequence, "AnteriorChamberDepthDefCodeSeq"));
        //extensions.add(new PrivateTag(TagFromName.SourceofLensThicknessDataCodeSequence, "SourceofLensThicknessDataCodeSeq"));
        //extensions.add(new PrivateTag(TagFromName.SourceofAnteriorChamberDepthDataCodeSequence, "SrcOfAnterChambrDepthDataCodeSeq"));
        //extensions.add(new PrivateTag(TagFromName.SourceofRefractiveMeasurementsSequence, "SourceofRefractMeasrmntsSequence"));
        //extensions.add(new PrivateTag(TagFromName.SourceofRefractiveMeasurementsCodeSequence, "SourceofRefractMeasrmntsCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthMeasurementModified, "OphthalmicAxlLenMeasrmntModified"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthDataSourceCodeSequence, "OpthmlcAxlLenDataSourceCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthDataSourceDescription, "OpthmlcAxlLenDataSrcDescription"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthMeasurementsTotalLengthSequence, "OpthmlcAxlLenMeasrmntsTotLenSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthMeasurementsSegmentalLengthSequence, "OpthmlcAxlLenMeasSegmntlLenSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthMeasurementsLengthSummationSequence, "OpthmlcAxlLenMeasrmntsLenSummSeq"));
        extensions.add(new PrivateTag(TagFromName.UltrasoundOphthalmicAxialLengthMeasurementsSequence, "UltrasndOpthmlcAxlLenMesrmntsSeq"));
        extensions.add(new PrivateTag(TagFromName.OpticalOphthalmicAxialLengthMeasurementsSequence, "OpticalOpthmlcAxlLenMeasrmntsSeq"));
        extensions.add(new PrivateTag(TagFromName.UltrasoundSelectedOphthalmicAxialLengthSequence, "UltrasndSelectedOpthmlcAxlLenSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthSelectionMethodCodeSequence, "OpthmlcAxlLenSelcntMethodCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.OpticalSelectedOphthalmicAxialLengthSequence, "OpticalSelectedOpthmlcAxlLenSeq"));
        extensions.add(new PrivateTag(TagFromName.SelectedSegmentalOphthalmicAxialLengthSequence, "SelectedSegmntlOpthmlcAxlLenSeq"));
        extensions.add(new PrivateTag(TagFromName.SelectedTotalOphthalmicAxialLengthSequence, "SelectedTotalOpthmlcAxlLenSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthQualityMetricSequence, "OpthmlcAxlLenQualityMetricSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicAxialLengthQualityMetricTypeCodeSequence, "OpthmlcAxlLenQualMetricTypCodSeq"));
        extensions.add(new PrivateTag(TagFromName.IntraocularLensCalculationsRightEyeSequence, "IntrclLensCalculationsRtEyeSeq"));
        extensions.add(new PrivateTag(TagFromName.IntraocularLensCalculationsLeftEyeSequence, "IntrclLensCalculationsLtEyeSeq"));
        extensions.add(new PrivateTag(TagFromName.ReferencedOphthalmicAxialLengthMeasurementQCImageSequence, "RefdOpthmlcAxlLenMesrmntQCImgSeq"));
        //extensions.add(new PrivateTag(TagFromName.AcquisitonMethodAlgorithmSequence, "AcquisitonMethodAlgSequence"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicThicknessMapTypeCodeSequence, "OpthmlcThicknessMapTypeCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicThicknessMappingNormalsSequence, "OpthmlcThicknessMappingNormlsSeq"));
        extensions.add(new PrivateTag(TagFromName.RetinalThicknessDefinitionCodeSequence, "RetinalThickDefinitionCodeSeq"));
        //extensions.add(new PrivateTag(TagFromName.PixelValueMappingtoCodedConceptSequence, "PixValueMappingtoCodedConceptSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicThicknessMapQualityThresholdSequence, "OpthmlcThickMapQualityThreshSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicThicknessMapThresholdQualityRating, "OpthmlcThickMapThreshQualRating"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicThicknessMapQualityRatingSequence, "OpthmlcThickMapQualityRatingSeq"));
        extensions.add(new PrivateTag(TagFromName.BackgroundIlluminationColorCodeSequence, "BackgroundIllumColorCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.PatientNotProperlyFixatedQuantity, "PatntNotProperlyFixatedQuantity"));
        extensions.add(new PrivateTag(TagFromName.CommentsOnPatientPerformanceOfVisualField, "CommentsOnPatntPerfmncOfVisField"));
        extensions.add(new PrivateTag(TagFromName.GlobalDeviationProbabilityNormalsFlag, "GlobalDevnProbabilityNormalsFlag"));
        extensions.add(new PrivateTag(TagFromName.AgeCorrectedSensitivityDeviationAlgorithmSequence, "AgeCorrSenstvtyDevnAlgSequence"));
        extensions.add(new PrivateTag(TagFromName.GeneralizedDefectSensitivityDeviationAlgorithmSequence, "GenizedDefectSenstvtyDevnAlgSeq"));
        extensions.add(new PrivateTag(TagFromName.LocalDeviationProbabilityNormalsFlag, "LocalDevnProbabilityNormalsFlag"));
        extensions.add(new PrivateTag(TagFromName.ShortTermFluctuationProbabilityCalculated, "ShortTermFluctProbabilityCalc"));
        extensions.add(new PrivateTag(TagFromName.CorrectedLocalizedDeviationFromNormalCalculated, "CorrLocalizedDevnFromNormalCalc"));
        extensions.add(new PrivateTag(TagFromName.CorrectedLocalizedDeviationFromNormal, "CorrLocalizedDeviationFromNormal"));
        extensions.add(new PrivateTag(TagFromName.CorrectedLocalizedDeviationFromNormalProbabilityCalculated, "CorrLocalzdDevnFromNormlProbCalc"));
        extensions.add(new PrivateTag(TagFromName.CorrectedLocalizedDeviationFromNormalProbability, "CorrLocalzdDevnFromNormlProb"));
        extensions.add(new PrivateTag(TagFromName.GlobalDeviationProbabilitySequence, "GlobalDevnProbabilitySequence"));
        extensions.add(new PrivateTag(TagFromName.LocalizedDeviationProbabilitySequence, "LocalizedDevnProbabilitySequence"));
        extensions.add(new PrivateTag(TagFromName.AgeCorrectedSensitivityDeviationValue, "AgeCorrSensitivityDeviationValue"));
        extensions.add(new PrivateTag(TagFromName.VisualFieldTestPointNormalsSequence, "VisualFieldTestPointNormlsSeq"));
        extensions.add(new PrivateTag(TagFromName.AgeCorrectedSensitivityDeviationProbabilityValue, "AgeCorrSensitivityDevnProbValue"));
        extensions.add(new PrivateTag(TagFromName.GeneralizedDefectCorrectedSensitivityDeviationFlag, "GenizedDefectCorrSenstvtyDevnFlg"));
        extensions.add(new PrivateTag(TagFromName.GeneralizedDefectCorrectedSensitivityDeviationValue, "GenizedDefectCorrSenstvtyDevnVal"));
        extensions.add(new PrivateTag(TagFromName.GeneralizedDefectCorrectedSensitivityDeviationProbabilityValue, "GenDefctCorrSenstvtyDevnProbVal"));
        extensions.add(new PrivateTag(TagFromName.RefractiveParametersUsedOnPatientSequence, "RefractParmsUsedOnPatntSequence"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicPatientClinicalInformationLeftEyeSequence, "OpthmlcPatntClinInfoLtEyeSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicPatientClinicalInformationRightEyeSequence, "OpthmlcPatntClinInfoRtEyeSeq"));
        extensions.add(new PrivateTag(TagFromName.ScreeningBaselineMeasuredSequence, "ScreeningBaselineMeasuredSeq"));
        extensions.add(new PrivateTag(TagFromName.VisualFieldTestReliabilityGlobalIndexSequence, "VisFieldTestRelibltyGlobIndexSeq"));
        extensions.add(new PrivateTag(TagFromName.VisualFieldGlobalResultsIndexSequence, "VisualFieldGlobalResultsIndexSeq"));
        extensions.add(new PrivateTag(TagFromName.LongitudinalTemporalInformationModified, "LongitudinalTemporalInfoModified"));
        extensions.add(new PrivateTag(TagFromName.ReferencedColorPaletteInstanceUID, "ReferencedColorPaletteInstUID"));
        extensions.add(new PrivateTag(TagFromName.PixelSpacingCalibrationDescription, "PixSpacingCalibrationDescription"));
        extensions.add(new PrivateTag(TagFromName.RedPaletteColorLookupTableDescriptor, "RedPaletteColorLookupTableDesc"));
        extensions.add(new PrivateTag(TagFromName.GreenPaletteColorLookupTableDescriptor, "GreenPaletteColorLookupTableDesc"));
        extensions.add(new PrivateTag(TagFromName.BluePaletteColorLookupTableDescriptor, "BluePaletteColorLookupTableDesc"));
        extensions.add(new PrivateTag(TagFromName.AlphaPaletteColorLookupTableDescriptor, "AlphaPaletteColorLookupTableDesc"));
        extensions.add(new PrivateTag(TagFromName.LargeRedPaletteColorLookupTableDescriptor, "LgRedPaletteColorLookupTableDesc"));
        extensions.add(new PrivateTag(TagFromName.LargeGreenPaletteColorLookupTableDescriptor, "LgGreenPaletteColorLkupTableDesc"));
        extensions.add(new PrivateTag(TagFromName.LargeBluePaletteColorLookupTableDescriptor, "LgBluePaletteColorLkupTableDesc"));
        extensions.add(new PrivateTag(TagFromName.LargeRedPaletteColorLookupTableData, "LgRedPaletteColorLookupTableData"));
        extensions.add(new PrivateTag(TagFromName.LargeGreenPaletteColorLookupTableData, "LgGreenPaletteColorLkupTableData"));
        extensions.add(new PrivateTag(TagFromName.LargeBluePaletteColorLookupTableData, "LgBluePaletteColorLkupTableData"));
        extensions.add(new PrivateTag(TagFromName.SegmentedRedPaletteColorLookupTableData, "SegmntedRedPalttColorLkupTblData"));
        extensions.add(new PrivateTag(TagFromName.SegmentedGreenPaletteColorLookupTableData, "SegmntedGrnPalttColorLkupTblData"));
        extensions.add(new PrivateTag(TagFromName.SegmentedBluePaletteColorLookupTableData, "SegmntedBluePalttColrLkupTblData"));
        extensions.add(new PrivateTag(TagFromName.EnhancedPaletteColorLookupTableSequence, "EnhancedPalttColorLkupTableSeq"));
        extensions.add(new PrivateTag(TagFromName.PixelIntensityRelationshipLUTSequence, "PixIntensityRelnLUTSequence"));
        extensions.add(new PrivateTag(TagFromName.EquipmentCoordinateSystemIdentification, "EquipCoordinateSystemIdentf"));
        extensions.add(new PrivateTag(TagFromName.RequestingPhysicianIdentificationSequence, "RequestingPhyscnIdentfSequence"));
        extensions.add(new PrivateTag(TagFromName.ScheduledPatientInstitutionResidence, "ScheduledPatientInstituResidence"));
        extensions.add(new PrivateTag(TagFromName.PatientClinicalTrialParticipationSequence, "PatntClinTrialParticipationSeq"));
        extensions.add(new PrivateTag(TagFromName.ChannelSensitivityCorrectionFactor, "ChanSensitivityCorrectionFactor"));
        extensions.add(new PrivateTag(TagFromName.WaveformDisplayBackgroundCIELabValue, "WaveformDsplyBackgroundCIELabVal"));
        extensions.add(new PrivateTag(TagFromName.WaveformPresentationGroupSequence, "WaveformPresentationGroupSeq"));
        extensions.add(new PrivateTag(TagFromName.ChannelRecommendedDisplayCIELabValue, "ChanRecommendedDsplyCIELabValue"));
        extensions.add(new PrivateTag(TagFromName.MultiplexedAudioChannelsDescriptionCodeSequence, "MultplxAudioChansDescrCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.ScheduledProcedureStepDescription, "ScheduledProcStepDescription"));
        extensions.add(new PrivateTag(TagFromName.ScheduledPerformingPhysicianIdentificationSequence, "ScheldPerformingPhyscnIdentfSeq"));
        extensions.add(new PrivateTag(TagFromName.AssigningJurisdictionCodeSequence, "AssigningJurisdictionCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.AssigningAgencyOrDepartmentCodeSequence, "AssigningAgencyOrDeptCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.ReferencedNonImageCompositeSOPInstanceSequence, "RefdNonImgCompositeSOPInstSeq"));
        extensions.add(new PrivateTag(TagFromName.PerformedProcedureStepDescription, "PerfProcedureStepDescription"));
        extensions.add(new PrivateTag(TagFromName.PerformedProcedureTypeDescription, "PerfProcedureTypeDescription"));
        extensions.add(new PrivateTag(TagFromName.CommentsOnThePerformedProcedureStep, "CommentsOnThePerfProcedureStep"));
        extensions.add(new PrivateTag(TagFromName.PerformedProcedureStepDiscontinuationReasonCodeSequence, "PerfProcStepDisconReasonCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.BillingSuppliesAndDevicesSequence, "BillingSuppliesAndDevcsSequence"));
        extensions.add(new PrivateTag(TagFromName.CommentsOnTheScheduledProcedureStep, "CommentsOnTheScheduledProcStep"));
        extensions.add(new PrivateTag(TagFromName.IssuerOfTheContainerIdentifierSequence, "IssuerOfTheContainerIdentfrSeq"));
        extensions.add(new PrivateTag(TagFromName.AlternateContainerIdentifierSequence, "AlternateContainerIdentfrSeq"));
        extensions.add(new PrivateTag(TagFromName.IssuerOfTheSpecimenIdentifierSequence, "IssuerOfTheSpecimenIdentfrSeq"));
        extensions.add(new PrivateTag(TagFromName.SpecimenPreparationStepContentItemSequence, "SpecmnPresntnStepContentItemSeq"));
        extensions.add(new PrivateTag(TagFromName.SpecimenLocalizationContentItemSequence, "SpecmnLocalizationContentItemSeq"));
        extensions.add(new PrivateTag(TagFromName.ImageCenterPointCoordinatesSequence, "ImgCntrPointCoordinatesSequence"));
        extensions.add(new PrivateTag(TagFromName.ReasonForRequestedProcedureCodeSequence, "ReasonForReqdProcCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.NamesOfIntendedRecipientsOfResults, "NamesOfIntendedRecipsOfResults"));
        extensions.add(new PrivateTag(TagFromName.IntendedRecipientsOfResultsIdentificationSequence, "IntendedRecipsOfResultsIdentfSeq"));
        extensions.add(new PrivateTag(TagFromName.ReasonForPerformedProcedureCodeSequence, "ReasonForPerfProcCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.RequestedProcedureDescriptionTrial, "RequestedProcDescriptionTrial"));
        extensions.add(new PrivateTag(TagFromName.ReasonForTheImagingServiceRequest, "ReasonForTheImgingServiceRequest"));
        extensions.add(new PrivateTag(TagFromName.PlacerOrderNumberImagingServiceRequestRetired, "PlacerOrdNumImgingServcReqRetird"));
        extensions.add(new PrivateTag(TagFromName.FillerOrderNumberImagingServiceRequestRetired, "FillerOrdNumImgingServcReqRetird"));
        extensions.add(new PrivateTag(TagFromName.PlacerOrderNumberImagingServiceRequest, "PlacerOrderNumImgingServiceReq"));
        extensions.add(new PrivateTag(TagFromName.FillerOrderNumberImagingServiceRequest, "FillerOrderNumImgingServiceReq"));
        extensions.add(new PrivateTag(TagFromName.ConfidentialityConstraintOnPatientDataDescription, "ConfidConstraintOnPatntDataDescr"));
        extensions.add(new PrivateTag(TagFromName.GeneralPurposeScheduledProcedureStepStatus, "GenPurposeScheldProcStepStatus"));
        extensions.add(new PrivateTag(TagFromName.GeneralPurposePerformedProcedureStepStatus, "GenPurposePerfProcStepStatus"));
        extensions.add(new PrivateTag(TagFromName.GeneralPurposeScheduledProcedureStepPriority, "GenPurposeScheduledProcStepPrio"));
        extensions.add(new PrivateTag(TagFromName.ScheduledProcessingApplicationsCodeSequence, "ScheduledProcngApplsCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.ScheduledProcedureStepStartDateTime, "ScheduledProcStepStartDateTime"));
        extensions.add(new PrivateTag(TagFromName.PerformedProcessingApplicationsCodeSequence, "PerfProcessingApplsCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.ScheduledProcedureStepModificationDateTime, "ScheduledProcStepModfcnDateTime"));
        extensions.add(new PrivateTag(TagFromName.ResultingGeneralPurposePerformedProcedureStepsSequence, "ResltngGenPurposPerfProcStepsSeq"));
        extensions.add(new PrivateTag(TagFromName.ReferencedGeneralPurposeScheduledProcedureStepSequence, "RefdGenPurposeScheldProcStepSeq"));
        extensions.add(new PrivateTag(TagFromName.ReferencedGeneralPurposeScheduledProcedureStepTransactionUID, "RefdGenPurpSchdProcStpTranscnUID"));
        extensions.add(new PrivateTag(TagFromName.ScheduledStationClassCodeSequence, "ScheldStationClassCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.ScheduledStationGeographicLocationCodeSequence, "ScheldStatnGeographicLocCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.PerformedStationClassCodeSequence, "PerfStationClassCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.PerformedStationGeographicLocationCodeSequence, "PerfStatnGeographicLocCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.RequestedSubsequentWorkitemCodeSequence, "ReqdSubsequentWorkitemCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.PerformedProcedureStepStartDateTime, "PerfProcedureStepStartDateTime"));
        extensions.add(new PrivateTag(TagFromName.PerformedProcedureStepEndDateTime, "PerfProcedureStepEndDateTime"));
        extensions.add(new PrivateTag(TagFromName.ProcedureStepCancellationDateTime, "ProcStepCancellationDateTime"));
        extensions.add(new PrivateTag(TagFromName.ReferencedImageRealWorldValueMappingSequence, "RefdImgRealWorldValueMappingSeq"));
        extensions.add(new PrivateTag(TagFromName.FindingsSourceCategoryCodeSequenceTrial, "FindingsSrcCategoryCodeSeqTrial"));
        extensions.add(new PrivateTag(TagFromName.DocumentingOrganizationIdentifierCodeSequenceTrial, "DocingOrgIdentfrCodeSeqTrial"));
        extensions.add(new PrivateTag(TagFromName.MeasurementPrecisionDescriptionTrial, "MeasrmntPrecisionDescriptonTrial"));
        extensions.add(new PrivateTag(TagFromName.DocumentIdentifierCodeSequenceTrial, "DocIdentifierCodeSequenceTrial"));
        extensions.add(new PrivateTag(TagFromName.DocumentAuthorIdentifierCodeSequenceTrial, "DocAuthorIdentfrCodeSeqTrial"));
        extensions.add(new PrivateTag(TagFromName.DocumentingObserverIdentifierCodeSequenceTrial, "DocingObservrIdentfrCodeSeqTrial"));
        extensions.add(new PrivateTag(TagFromName.ProcedureIdentifierCodeSequenceTrial, "ProcIdentfrCodeSequenceTrial"));
        extensions.add(new PrivateTag(TagFromName.VerifyingObserverIdentificationCodeSequence, "VerifyingObserverIdentfCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.ObjectDirectoryBinaryIdentifierTrial, "ObjectDerBinaryIdentifierTrial"));
        extensions.add(new PrivateTag(TagFromName.DateOfDocumentOrVerbalTransactionTrial, "DateOfDocOrVerbalTransctnTrial"));
        extensions.add(new PrivateTag(TagFromName.TimeOfDocumentCreationOrVerbalTransactionTrial, "TimeOfDocCreatnOrVerblTranscnTrl"));
        extensions.add(new PrivateTag(TagFromName.ObservationCategoryCodeSequenceTrial, "ObsvnCategoryCodeSequenceTrial"));
        extensions.add(new PrivateTag(TagFromName.ReferencedObjectObservationClassTrial, "ReferencedObjectObsvnClassTrial"));
        extensions.add(new PrivateTag(TagFromName.NumericValueQualifierCodeSequence, "NumericValueQualifierCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.VerbalSourceIdentifierCodeSequenceTrial, "VerbalSourceIdentfrCodeSeqTrial"));
        extensions.add(new PrivateTag(TagFromName.CurrentRequestedProcedureEvidenceSequence, "CurrentReqdProcEvidenceSequence"));
        extensions.add(new PrivateTag(TagFromName.HL7StructuredDocumentReferenceSequence, "HL7StructuredDocRefSequence"));
        extensions.add(new PrivateTag(TagFromName.ObservationSubjectTypeCodeSequenceTrial, "ObsvnSubjectTypeCodeSeqTrial"));
        extensions.add(new PrivateTag(TagFromName.ObservationSubjectContextFlagTrial, "ObsvnSubjectContextFlagTrial"));
        extensions.add(new PrivateTag(TagFromName.RelationshipTypeCodeSequenceTrial, "RelnTypeCodeSequenceTrial"));
        extensions.add(new PrivateTag(TagFromName.SubstanceAdministrationParameterSequence, "SubstanceAdmParameterSequence"));
        extensions.add(new PrivateTag(TagFromName.UnspecifiedLateralityLensSequence, "UnspecifiedLatityLensSequence"));
        extensions.add(new PrivateTag(TagFromName.SubjectiveRefractionRightEyeSequence, "SubjectiveRefractionRtEyeSeq"));
        extensions.add(new PrivateTag(TagFromName.SubjectiveRefractionLeftEyeSequence, "SubjectiveRefractionLtEyeSeq"));
        extensions.add(new PrivateTag(TagFromName.ReferencedRefractiveMeasurementsSequence, "RefdRefractiveMeasrmntsSequence"));
        extensions.add(new PrivateTag(TagFromName.RecommendedAbsentPixelCIELabValue, "RecommendedAbsentPixCIELabValue"));
        extensions.add(new PrivateTag(TagFromName.ReferencedImageNavigationSequence, "ReferencedImgNavigationSequence"));
        extensions.add(new PrivateTag(TagFromName.BottomRightHandCornerOfLocalizerArea, "BottomRtHandCornerOfLocalizrArea"));
        extensions.add(new PrivateTag(TagFromName.OpticalPathIdentificationSequence, "OpticalPathIdentfSequence"));
        extensions.add(new PrivateTag(TagFromName.RowPositionInTotalImagePixelMatrix, "RowPositionInTotalImgPixelMatrix"));
        extensions.add(new PrivateTag(TagFromName.ColumnPositionInTotalImagePixelMatrix, "ColumnPositionInTotalImgPixMtrx"));
        extensions.add(new PrivateTag(TagFromName.ContainerComponentTypeCodeSequence, "ContainerComponentTypeCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.ContrastBolusIngredientPercentByVolume, "ContrastBolusIngredPctByVolume"));
        extensions.add(new PrivateTag(TagFromName.IntravascularOCTFrameTypeSequence, "IntravascularOCTFrmTypeSequence"));
        extensions.add(new PrivateTag(TagFromName.IntravascularFrameContentSequence, "IntravascularFrmContentSequence"));
        extensions.add(new PrivateTag(TagFromName.IntravascularLongitudinalDistance, "IntravascularLongitudinalDist"));
        extensions.add(new PrivateTag(TagFromName.IntravascularOCTFrameContentSequence, "IntravascularOCTFrmContentSeq"));
        extensions.add(new PrivateTag(TagFromName.RadiopharmaceuticalInformationSequence, "RadiopharmaceuticalInfoSequence"));
        extensions.add(new PrivateTag(TagFromName.PatientOrientationModifierCodeSequence, "PatntOrientationModifierCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.PatientGantryRelationshipCodeSequence, "PatntGantryRelnCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.SegmentedPropertyCategoryCodeSequence, "SegmntedPropertyCategoryCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.SegmentedPropertyTypeCodeSequence, "SegmntedPropertyTypeCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.DeformableRegistrationGridSequence, "DeformableRestrnGridSequence"));
        extensions.add(new PrivateTag(TagFromName.PreDeformationMatrixRegistrationSequence, "PreDeformationMtrxRestrnSequence"));
        extensions.add(new PrivateTag(TagFromName.PostDeformationMatrixRegistrationSequence, "PostDeformationMtrxRestrnSeq"));
        extensions.add(new PrivateTag(TagFromName.SegmentSurfaceGenerationAlgorithmIdentificationSequence, "SegmntSurfaceGenAlgIdentfSeq"));
        extensions.add(new PrivateTag(TagFromName.SegmentSurfaceSourceInstanceSequence, "SegmentSurfaceSourceInstSequence"));
        extensions.add(new PrivateTag(TagFromName.SurfaceProcessingAlgorithmIdentificationSequence, "SurfaceProcngAlgIdentfSequence"));
        extensions.add(new PrivateTag(TagFromName.DerivationImplantTemplateSequence, "DerivImplantTemplateSequence"));
        extensions.add(new PrivateTag(TagFromName.InformationFromManufacturerSequence, "InfoFromManufacturerSequence"));
        extensions.add(new PrivateTag(TagFromName.NotificationFromManufacturerSequence, "NotificationFromManufacrSequence"));
        extensions.add(new PrivateTag(TagFromName.ImplantRegulatoryDisapprovalCodeSequence, "ImplantRegulatoryDisappCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.ImplantTemplate3DModelSurfaceNumber, "ImplantTemplate3DMdlSurfaceNum"));
        extensions.add(new PrivateTag(TagFromName.MatingFeatureDegreeOfFreedomSequence, "MatingFeatureDegreeOfFreedomSeq"));
        extensions.add(new PrivateTag(TagFromName.TwoDMatingFeatureCoordinatesSequence, "TwoDMatingFeatureCoordinatesSeq"));
        extensions.add(new PrivateTag(TagFromName.PlanningLandmarkIdentificationCodeSequence, "PlanningLandmarkIdentfCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.BoundingBoxTextHorizontalJustification, "BoundingBoxTextHorzJustification"));
        extensions.add(new PrivateTag(TagFromName.DisplayedAreaTopLeftHandCornerTrial, "DsplyedAreaTopLtHandCornerTrial"));
        extensions.add(new PrivateTag(TagFromName.DisplayedAreaBottomRightHandCornerTrial, "DsplyedAreaBottomRtHandCornerTrl"));
        extensions.add(new PrivateTag(TagFromName.DisplayedAreaBottomRightHandCorner, "DsplyedAreaBottomRightHandCorner"));
        extensions.add(new PrivateTag(TagFromName.GraphicLayerRecommendedDisplayGrayscaleValue, "GraphicLayrRecmndDsplyGraysclVal"));
        extensions.add(new PrivateTag(TagFromName.GraphicLayerRecommendedDisplayRGBValue, "GraphicLayerRecmndedDsplyRGBVal"));
        extensions.add(new PrivateTag(TagFromName.ContentCreatorIdentificationCodeSequence, "ContentCreatorIdentfCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.AlternateContentDescriptionSequence, "AlternateContentDescriptionSeq"));
        extensions.add(new PrivateTag(TagFromName.PresentationPixelMagnificationRatio, "PresentationPixelMagnifcnRatio"));
        extensions.add(new PrivateTag(TagFromName.FrameOfReferenceTransformationMatrixType, "FrmOfRefTransformationMtrxType"));
        extensions.add(new PrivateTag(TagFromName.GraphicLayerRecommendedDisplayCIELabValue, "GraphicLayerRecmndDsplyCIELabVal"));
        extensions.add(new PrivateTag(TagFromName.ReferencedSpatialRegistrationSequence, "RefdSpatialRegistrationSequence"));
        extensions.add(new PrivateTag(TagFromName.HangingProtocolDefinitionSequence, "HangingProtocolDefinitionSeq"));
        extensions.add(new PrivateTag(TagFromName.HangingProtocolUserIdentificationCodeSequence, "HangingProtocolUserIdentfCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.SelectorSequencePointerPrivateCreator, "SelectorSeqPointerPrivateCreator"));
        extensions.add(new PrivateTag(TagFromName.DisplayEnvironmentSpatialPosition, "DsplyEnvironmentSpatialPosition"));
        extensions.add(new PrivateTag(TagFromName.DisplaySetPresentationGroupDescription, "DsplySetPresentationGroupDesc"));
        extensions.add(new PrivateTag(TagFromName.StructuredDisplayBackgroundCIELabValue, "StructdDsplyBackgroundCIELabVal"));
        extensions.add(new PrivateTag(TagFromName.StructuredDisplayImageBoxSequence, "StructuredDsplyImageBoxSequence"));
        extensions.add(new PrivateTag(TagFromName.ReformattingOperationInitialViewDirection, "ReformattingOperInitlViewDirctn"));
        extensions.add(new PrivateTag(TagFromName.PseudoColorPaletteInstanceReferenceSequence, "PseudoColorPalttInstRefSequence"));
        extensions.add(new PrivateTag(TagFromName.DisplaySetHorizontalJustification, "DsplySetHorizontalJustification"));
        extensions.add(new PrivateTag(TagFromName.ProcedureStepProgressInformationSequence, "ProcStepProgressInfoSequence"));
        extensions.add(new PrivateTag(TagFromName.ProcedureStepCommunicationsURISequence, "ProcStepCommunicationsURISeq"));
        extensions.add(new PrivateTag(TagFromName.ProcedureStepDiscontinuationReasonCodeSequence, "ProcStepDisconReasonCodeSequence"));
        extensions.add(new PrivateTag(TagFromName.TableTopLongitudinalAdjustedPosition, "TableTopLongitudinalAdjustedPosn"));
        extensions.add(new PrivateTag(TagFromName.DeliveryVerificationImageSequence, "DeliveryVerificationImgSequence"));
        extensions.add(new PrivateTag(TagFromName.GeneralMachineVerificationSequence, "GenMachineVerificationSequence"));
        extensions.add(new PrivateTag(TagFromName.ConventionalMachineVerificationSequence, "ConventionalMachineVerifSeq"));
        extensions.add(new PrivateTag(TagFromName.ConventionalControlPointVerificationSequence, "ConventionalCntrlPointVerifSeq"));
        extensions.add(new PrivateTag(TagFromName.IonControlPointVerificationSequence, "IonCntrlPointVerificationSeq"));
        extensions.add(new PrivateTag(TagFromName.AttributeOccurrencePrivateCreator, "AttrOccurrencePrivateCreator"));
        extensions.add(new PrivateTag(TagFromName.ScheduledProcessingParametersSequence, "ScheduledProcessingParmsSequence"));
        extensions.add(new PrivateTag(TagFromName.PerformedProcessingParametersSequence, "PerformedProcessingParmsSequence"));
        extensions.add(new PrivateTag(TagFromName.UnifiedProcedureStepPerformedProcedureSequence, "UnifiedProcStepPerfProcSequence"));
        extensions.add(new PrivateTag(TagFromName.ReplacedImplantAssemblyTemplateSequence, "ReplacedImplantAsmblyTemplateSeq"));
        extensions.add(new PrivateTag(TagFromName.OriginalImplantAssemblyTemplateSequence, "OriginalImplantAsmblyTemplateSeq"));
        extensions.add(new PrivateTag(TagFromName.DerivationImplantAssemblyTemplateSequence, "DerivImplantAsmblyTemplateSeq"));
        extensions.add(new PrivateTag(TagFromName.ImplantAssemblyTemplateTargetAnatomySequence, "ImplantAsmblyTmpltTargetAnatmSeq"));
        extensions.add(new PrivateTag(TagFromName.Component1ReferencedMatingFeatureSetID, "Component1RefdMatingFeatureSetID"));
        extensions.add(new PrivateTag(TagFromName.Component1ReferencedMatingFeatureID, "Component1RefdMatingFeatureID"));
        extensions.add(new PrivateTag(TagFromName.Component2ReferencedMatingFeatureSetID, "Component2RefdMatingFeatureSetID"));
        extensions.add(new PrivateTag(TagFromName.Component2ReferencedMatingFeatureID, "Component2RefdMatingFeatureID"));
        extensions.add(new PrivateTag(TagFromName.ReplacedImplantTemplateGroupSequence, "ReplacedImplantTemplateGroupSeq"));
        extensions.add(new PrivateTag(TagFromName.ImplantTemplateGroupTargetAnatomySequence, "ImplantTmpltGroupTargtAnatomySeq"));
        extensions.add(new PrivateTag(TagFromName.ImplantTemplateGroupMembersSequence, "ImplantTemplateGroupMembersSeq"));
        extensions.add(new PrivateTag(TagFromName.ThreeDImplantTemplateGroupMemberMatchingPoint, "ThreeDImplntTmpltGrpMembrMatchPt"));
        extensions.add(new PrivateTag(TagFromName.ThreeDImplantTemplateGroupMemberMatchingAxes, "ThreeDImplntTmpltGrpMemMatchAxes"));
        extensions.add(new PrivateTag(TagFromName.ImplantTemplateGroupMemberMatching2DCoordinatesSequence, "ImplntTmplGrpMembMatch2DCoordSeq"));
        extensions.add(new PrivateTag(TagFromName.TwoDImplantTemplateGroupMemberMatchingPoint, "TwoDImplantTmpltGrpMemberMatchPt"));
        extensions.add(new PrivateTag(TagFromName.TwoDImplantTemplateGroupMemberMatchingAxes, "TwoDImplntTmpltGrpMembrMatchAxes"));
        extensions.add(new PrivateTag(TagFromName.ImplantTemplateGroupVariationDimensionSequence, "ImplantTmpltGroupVartnDimSeq"));
        extensions.add(new PrivateTag(TagFromName.ImplantTemplateGroupVariationDimensionName, "ImplantTmpltGroupVartnDimName"));
        extensions.add(new PrivateTag(TagFromName.ImplantTemplateGroupVariationDimensionRankSequence, "ImplantTmpltGroupVartnDimRankSeq"));
        extensions.add(new PrivateTag(TagFromName.ReferencedImplantTemplateGroupMemberID, "RefdImplantTemplateGroupMemberID"));
        extensions.add(new PrivateTag(TagFromName.ImplantTemplateGroupVariationDimensionRank, "ImplantTmpltGroupVartnDimRank"));
        extensions.add(new PrivateTag(TagFromName.AuthorizationEquipmentCertificationNumber, "AuthorizationEquipCertifNumber"));
        extensions.add(new PrivateTag(TagFromName.DigitalSignaturePurposeCodeSequence, "DigitalSignaturePurposeCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.ReferencedDigitalSignatureSequence, "RefdDigitalSignatureSequence"));
        extensions.add(new PrivateTag(TagFromName.EncryptedContentTransferSyntaxUID, "EncryptContentTransferSyntaxUID"));
        extensions.add(new PrivateTag(TagFromName.ReasonForTheAttributeModification, "ReasonForTheAttrModification"));
        extensions.add(new PrivateTag(TagFromName.SupportedImageDisplayFormatsSequence, "SupportedImgDsplyFormatsSequence"));
        extensions.add(new PrivateTag(TagFromName.ConfigurationInformationDescription, "ConfigInformationDescription"));
        extensions.add(new PrivateTag(TagFromName.ReferencedBasicAnnotationBoxSequence, "RefdBasicAnnotationBoxSequence"));
        extensions.add(new PrivateTag(TagFromName.ReferencedImageOverlayBoxSequence, "ReferencedImgOverlayBoxSequence"));
        extensions.add(new PrivateTag(TagFromName.ReferencedImageBoxSequenceRetired, "ReferencedImgBoxSequenceRetired"));
        extensions.add(new PrivateTag(TagFromName.ReferencedPresentationLUTSequence, "RefdPresentationLUTSequence"));
        extensions.add(new PrivateTag(TagFromName.ReferencedPrintJobSequencePullStoredPrint, "RefdPrintJobSeqPullStoredPrint"));
        extensions.add(new PrivateTag(TagFromName.PrintManagementCapabilitiesSequence, "PrintManagementCapabilitiesSeq"));
        extensions.add(new PrivateTag(TagFromName.LabelUsingInformationExtractedFromInstances, "LabelUsingInfoExtrcFromInstances"));
        extensions.add(new PrivateTag(TagFromName.PreserveCompositeInstancesAfterMediaCreation, "PresrvCompstInstsAftrMediaCreatn"));
        extensions.add(new PrivateTag(TagFromName.TotalNumberOfPiecesOfMediaCreated, "TotalNumOfPiecesOfMediaCreated"));
        extensions.add(new PrivateTag(TagFromName.ReferencedFrameOfReferenceSequence, "ReferencedFrmOfReferenceSequence"));
        extensions.add(new PrivateTag(TagFromName.ROIElementalCompositionAtomicNumber, "ROIElemCompositionAtomicNumber"));
        extensions.add(new PrivateTag(TagFromName.ROIElementalCompositionAtomicMassFraction, "ROIElemCompositonAtomicMassFract"));
        extensions.add(new PrivateTag(TagFromName.FrameOfReferenceRelationshipSequence, "FrmOfRefRelationshipSequence"));
        extensions.add(new PrivateTag(TagFromName.FrameOfReferenceTransformationType, "FrmOfReferenceTransformationType"));
        extensions.add(new PrivateTag(TagFromName.FrameOfReferenceTransformationMatrix, "FrmOfReferenceTransformationMtrx"));
        extensions.add(new PrivateTag(TagFromName.FrameOfReferenceTransformationComment, "FrmOfRefTransformationComment"));
        extensions.add(new PrivateTag(TagFromName.ReferencedTreatmentRecordSequence, "ReferencedTreatmentRecSequence"));
        extensions.add(new PrivateTag(TagFromName.TreatmentSummaryCalculatedDoseReferenceSequence, "TreatmentSummaryCalcDoseRefSeq"));
        extensions.add(new PrivateTag(TagFromName.CalculatedDoseReferenceDescription, "CalcDoseReferenceDescription"));
        extensions.add(new PrivateTag(TagFromName.ReferencedMeasuredDoseReferenceSequence, "RefdMeasuredDoseRefSequence"));
        extensions.add(new PrivateTag(TagFromName.ReferencedMeasuredDoseReferenceNumber, "RefdMeasuredDoseReferenceNum"));
        extensions.add(new PrivateTag(TagFromName.ReferencedCalculatedDoseReferenceSequence, "RefdCalcDoseReferenceSequence"));
        extensions.add(new PrivateTag(TagFromName.ReferencedCalculatedDoseReferenceNumber, "ReferencedCalcDoseReferenceNum"));
        extensions.add(new PrivateTag(TagFromName.BeamLimitingDeviceLeafPairsSequence, "BeamLimitingDevcLeafPairsSeq"));
        extensions.add(new PrivateTag(TagFromName.TreatmentSummaryMeasuredDoseReferenceSequence, "TreatmentSumryMeasuredDoseRefSeq"));
        extensions.add(new PrivateTag(TagFromName.RecordedLateralSpreadingDeviceSequence, "RecordedLatSpreadingDevcSequence"));
        extensions.add(new PrivateTag(TagFromName.TreatmentSessionApplicationSetupSequence, "TreatmentSessionApplSetupSeq"));
        extensions.add(new PrivateTag(TagFromName.RecordedBrachyAccessoryDeviceSequence, "RecordedBrachyAccDeviceSequence"));
        extensions.add(new PrivateTag(TagFromName.ReferencedBrachyAccessoryDeviceNumber, "ReferencedBrachyAccDeviceNumber"));
        extensions.add(new PrivateTag(TagFromName.BrachyControlPointDeliveredSequence, "BrachyCntrlPointDeliveredSeq"));
        extensions.add(new PrivateTag(TagFromName.OrganAtRiskOverdoseVolumeFraction, "OrganAtRiskOverdoseVolumeFract"));
        extensions.add(new PrivateTag(TagFromName.BeamLimitingDeviceToleranceSequence, "BeamLimitingDevcToleranceSeq"));
        extensions.add(new PrivateTag(TagFromName.BeamLimitingDevicePositionTolerance, "BeamLimitingDevcPosnTolerance"));
        extensions.add(new PrivateTag(TagFromName.TableTopVerticalPositionTolerance, "TableTopVerticalPosnTolerance"));
        extensions.add(new PrivateTag(TagFromName.TableTopLongitudinalPositionTolerance, "TblTopLongitudinalPosnTolerance"));
        extensions.add(new PrivateTag(TagFromName.NumberOfFractionPatternDigitsPerDay, "NumberOfFractPatternDigitsPerDay"));
        extensions.add(new PrivateTag(TagFromName.BrachyApplicationSetupDoseSpecificationPoint, "BrachyApplSetupDoseSpecifnPoint"));
        extensions.add(new PrivateTag(TagFromName.SourceToBeamLimitingDeviceDistance, "SourceToBeamLimitingDevcDistance"));
        extensions.add(new PrivateTag(TagFromName.IsocenterToBeamLimitingDeviceDistance, "IsocenterToBeamLimitingDevcDist"));
        extensions.add(new PrivateTag(TagFromName.ImagingDeviceSpecificAcquisitionParameters, "ImagingDevcSpecificAcqParameters"));
        extensions.add(new PrivateTag(TagFromName.TotalWedgeTrayWaterEquivalentThickness, "TotalWedgeTrayWaterEquivThick"));
        extensions.add(new PrivateTag(TagFromName.TotalBlockTrayWaterEquivalentThickness, "TotalBlockTrayWaterEquivThick"));
        extensions.add(new PrivateTag(TagFromName.CumulativeDoseReferenceCoefficient, "CumulativeDoseRefCoefficient"));
        extensions.add(new PrivateTag(TagFromName.BeamLimitingDevicePositionSequence, "BeamLimitingDevcPositionSequence"));
        extensions.add(new PrivateTag(TagFromName.BeamLimitingDeviceRotationDirection, "BeamLimitingDevcRotationDirctn"));
        extensions.add(new PrivateTag(TagFromName.TableTopEccentricRotationDirection, "TableTopEccentricRotationDirctn"));
        extensions.add(new PrivateTag(TagFromName.TableTopVerticalSetupDisplacement, "TableTopVerticalSetupDispl"));
        extensions.add(new PrivateTag(TagFromName.TableTopLongitudinalSetupDisplacement, "TableTopLongitudinalSetupDispl"));
        extensions.add(new PrivateTag(TagFromName.SourceEncapsulationNominalThickness, "SourceEncapsNominalThickness"));
        extensions.add(new PrivateTag(TagFromName.SourceEncapsulationNominalTransmission, "SourceEncapsNominalTransmission"));
        extensions.add(new PrivateTag(TagFromName.BrachyAccessoryDeviceNominalThickness, "BrachyAccDeviceNominalThickness"));
        extensions.add(new PrivateTag(TagFromName.BrachyAccessoryDeviceNominalTransmission, "BrachyAccDevcNominalTransmission"));
        extensions.add(new PrivateTag(TagFromName.SourceApplicatorWallNominalThickness, "SrcApplicatorWallNominThickness"));
        extensions.add(new PrivateTag(TagFromName.SourceApplicatorWallNominalTransmission, "SrcApplicatorWallNominTransmssn"));
        extensions.add(new PrivateTag(TagFromName.TotalCompensatorTrayWaterEquivalentThickness, "TotalCompsatrTrayWaterEquivThick"));
        extensions.add(new PrivateTag(TagFromName.IsocenterToCompensatorTrayDistance, "IsocenterToCompsatrTrayDistance"));
        extensions.add(new PrivateTag(TagFromName.CompensatorRelativeStoppingPowerRatio, "CompsatrRelatvStoppingPowerRatio"));
        extensions.add(new PrivateTag(TagFromName.LateralSpreadingDeviceDescription, "LateralSpreadingDevcDescription"));
        extensions.add(new PrivateTag(TagFromName.LateralSpreadingDeviceWaterEquivalentThickness, "LatSpreadngDevcWaterEquivThick"));
        extensions.add(new PrivateTag(TagFromName.RangeShifterWaterEquivalentThickness, "RangeShifterWaterEquivThickness"));
        extensions.add(new PrivateTag(TagFromName.LateralSpreadingDeviceSettingsSequence, "LatSpreadingDevcSettingsSequence"));
        extensions.add(new PrivateTag(TagFromName.IsocenterToLateralSpreadingDeviceDistance, "IsocntrToLatSpreadingDevcDist"));
        extensions.add(new PrivateTag(TagFromName.RangeModulatorGatingStartWaterEquivalentThickness, "RangeModGatngStrtWaterEquivThick"));
        extensions.add(new PrivateTag(TagFromName.RangeModulatorGatingStopWaterEquivalentThickness, "RangeModGatngStopWaterEquivThick"));
        extensions.add(new PrivateTag(TagFromName.IsocenterToRangeModulatorDistance, "IsocenterToRangeModulatorDist"));
        extensions.add(new PrivateTag(TagFromName.SourceToApplicatorMountingPositionDistance, "SrcToApplicatorMountingPosnDist"));
        extensions.add(new PrivateTag(TagFromName.ReferencedBrachyApplicationSetupSequence, "RefdBrachyApplSetupSequence"));
        extensions.add(new PrivateTag(TagFromName.ReferencedBrachyApplicationSetupNumber, "ReferencedBrachyApplSetupNumber"));
        extensions.add(new PrivateTag(TagFromName.ReferencedVerificationImageSequence, "RefdVerificationImgSequence"));
        extensions.add(new PrivateTag(TagFromName.BrachyReferencedDoseReferenceSequence, "BrachyRefdDoseReferenceSequence"));
        extensions.add(new PrivateTag(TagFromName.ReferencedLateralSpreadingDeviceNumber, "ReferencedLatSpreadingDevcNumber"));
        extensions.add(new PrivateTag(TagFromName.InterpretationDiagnosisDescription, "InterpDiagnosisDescription"));
        extensions.add(new PrivateTag(TagFromName.InterpretationDiagnosisCodeSequence, "InterpDiagnosisCodeSequence"));
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
     * 
     * The leading M is to satisfy Java naming requirements and is otherwise
     * ignored.
     * 
     * @author irrer
     * 
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
                return Integer.parseInt(fields[1].substring(0, fields[1].length()-1));
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
            return this.name() + "  min:" + min + "  max:" + ((max == Integer.MAX_VALUE) ? "N" : (""+max)) + "  incr:" + incr;
        }
        
        public String getName() {
            return this.name().substring(1).replace('_', '-');
        }

    };


    /**
     * Initialize the value multiplicity table for the dictionary.  The value multiplicity determines how many
     * values a single attribute is allowed to have.
     * 
     * See the DICOM standard section 3.6 for details.
     */
    private void initValueMultiplicity() {
        valueMultiplicity.put(TagFromName.SpecificCharacterSet, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ImageType, Multiplicity.M2_N);
        valueMultiplicity.put(TagFromName.RelatedGeneralSOPClassUID, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.RetrieveAETitle, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FailedSOPInstanceUIDList, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ModalitiesInStudy, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SOPClassesInStudy, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ReferringPhysicianTelephoneNumbers, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PhysiciansOfRecord, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PerformingPhysicianName, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.NameOfPhysiciansReadingStudy, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.OperatorsName, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.AdmittingDiagnosesDescription, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SOPClassesSupported, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ReferencedFrameNumber, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SimpleFrameList, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CalculatedFrameList, Multiplicity.M3_3N);
        valueMultiplicity.put(TagFromName.TimeRange, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.EventElapsedTimes, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.EventTimerNames, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FrameType, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.OtherPatientIDs, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.OtherPatientNames, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.InsurancePlanIdentification, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.MedicalAlerts, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.Allergies, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PatientTelephoneNumbers, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DeidentificationMethod, Multiplicity.M1_N);
        //valueMultiplicity.put(TagFromName.CADFileFormat, Multiplicity.M1_N);
        //valueMultiplicity.put(TagFromName.ComponentReferenceSystem, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ComponentManufacturingProcedure, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ComponentManufacturer, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.MaterialThickness, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.MaterialPipeDiameter, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.MaterialIsolationDiameter, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.MaterialGrade, Multiplicity.M1_N);
        //valueMultiplicity.put(TagFromName.MaterialPropertiesFileID, Multiplicity.M1_N);
        //valueMultiplicity.put(TagFromName.MaterialPropertiesFileFormat, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.IndicationType, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TransformOrderOfAxes, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CoordinateSystemTransformRotationAndScaleMatrix, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CoordinateSystemTransformTranslationMatrix, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DACGainPoints, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DACTimePoints, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DACAmplitude, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CalibrationTime, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CalibrationDate, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ScanningSequence, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SequenceVariant, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ScanOptions, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.Radionuclide, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.EnergyWindowTotalWidth, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.EchoNumbers, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SecondaryCaptureDeviceSoftwareVersions, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.HardcopyDeviceSoftwareVersion, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SoftwareVersions, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ContrastFlowRate, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ContrastFlowDuration, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FrameTimeVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SynchronizationChannel, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.TableVerticalIncrement, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TableLateralIncrement, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TableLongitudinalIncrement, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.RadialPosition, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.RotationOffset, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FieldOfViewDimensions, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.TypeOfFilters, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ImagerPixelSpacing, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.Grid, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FocalDistance, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.XFocusCenter, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.YFocusCenter, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.FocalSpots, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DateOfLastCalibration, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TimeOfLastCalibration, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ConvolutionKernel, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.UpperLowerPixelValues, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.WholeBodyTechnique, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.AcquisitionMatrix, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.PositionerPrimaryAngleIncrement, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PositionerSecondaryAngleIncrement, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ShutterShape, Multiplicity.M1_3);
        valueMultiplicity.put(TagFromName.CenterOfCircularShutter, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.VerticesOfThePolygonalShutter, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.ShutterPresentationColorCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.CollimatorShape, Multiplicity.M1_3);
        valueMultiplicity.put(TagFromName.CenterOfCircularCollimator, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.VerticesOfThePolygonalCollimator, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.PageNumberVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FrameLabelVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FramePrimaryAngleVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FrameSecondaryAngleVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SliceLocationVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DisplayWindowLabelVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.NominalScannedPixelSpacing, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.LesionNumber, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.OutputPower, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TransducerData, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ImageTransformationMatrix, Multiplicity.M6);
        valueMultiplicity.put(TagFromName.ImageTranslationVector, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.TableOfXBreakPoints, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TableOfYBreakPoints, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TableOfPixelValues, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TableOfParameterValues, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.RWaveTimeVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DetectorBinning, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.DetectorElementPhysicalSize, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.DetectorElementSpacing, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.DetectorActiveDimensions, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.DetectorActiveOrigin, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.FieldOfViewOrigin, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.PixelDataAreaOriginRelativeToFOV, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.GridAspectRatio, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.FilterMaterial, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FilterThicknessMinimum, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FilterThicknessMaximum, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FilterBeamPathLengthMinimum, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FilterBeamPathLengthMaximum, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SpectralWidth, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.ChemicalShiftReference, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.DecoupledNucleus, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.DecouplingFrequency, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.DecouplingChemicalShiftReference, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.TimeDomainFiltering, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.NumberOfZeroFills, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.InversionTimes, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DiffusionGradientOrientation, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.VelocityEncodingDirection, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.TransmitterFrequency, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.ResonantNucleus, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.SlabOrientation, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.MidSlabPosition, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ASLSlabOrientation, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ASLMidSlabPosition, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.DataCollectionCenterPatient, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ReconstructionFieldOfView, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ReconstructionTargetCenterPatient, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ReconstructionPixelSpacing, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.CalciumScoringMassFactorDevice, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ObjectPixelSpacingInCenterOfBeam, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.IntensifierActiveDimensions, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.PhysicalDetectorSize, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.PositionOfIsocenterProjection, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.CenterOfCircularExposureControlSensingRegion, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.VerticesOfThePolygonalExposureControlSensingRegion, Multiplicity.M2_N);
        valueMultiplicity.put(TagFromName.FieldOfViewDimensionsInFloat, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.DepthsOfFocus, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PatientOrientation, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ImagePosition, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ImagePositionPatient, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ImageOrientation, Multiplicity.M6);
        valueMultiplicity.put(TagFromName.ImageOrientationPatient, Multiplicity.M6);
        valueMultiplicity.put(TagFromName.MaskingImage, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.Reference, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.OtherStudyNumbers, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.OriginalImageIdentification, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.OriginalImageIdentificationNomenclature, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DimensionIndexValues, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ImagePositionVolume, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ImageOrientationVolume, Multiplicity.M6);
        valueMultiplicity.put(TagFromName.ApexPosition, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.VolumeToTransducerMappingMatrix, Multiplicity.M16);
        valueMultiplicity.put(TagFromName.VolumeToTableMappingMatrix, Multiplicity.M16);
        valueMultiplicity.put(TagFromName.AcquisitionIndex, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.LightPathFilterPassBand, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ImagePathFilterPassBand, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ReferenceCoordinates, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.FrameIncrementPointer, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FrameDimensionPointer, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PixelSpacing, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ZoomFactor, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ZoomCenter, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.PixelAspectRatio, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ManipulatedImage, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CorrectedImage, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CompressionSequence, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CompressionStepPointers, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PerimeterTable, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PredictorConstants, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SequenceOfCompressedData, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DetailsOfCoefficients, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CoefficientCoding, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CoefficientCodingPointers, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DataBlockDescription, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DataBlock, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ZonalMapLocation, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CodeLabel, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CodeTableLocation, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ImageDataLocation, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.WindowCenter, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.WindowWidth, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.WindowCenterWidthExplanation, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.GrayLookupTableDescriptor, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.RedPaletteColorLookupTableDescriptor, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.GreenPaletteColorLookupTableDescriptor, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.BluePaletteColorLookupTableDescriptor, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.AlphaPaletteColorLookupTableDescriptor, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.LargeRedPaletteColorLookupTableDescriptor, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.LargeGreenPaletteColorLookupTableDescriptor, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.LargeBluePaletteColorLookupTableDescriptor, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.GrayLookupTableData, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.BlendingLookupTableDescriptor, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.LossyImageCompressionRatio, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.LossyImageCompressionMethod, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.LUTDescriptor, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.LUTData, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FrameNumbersOfInterest, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FrameOfInterestDescription, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.FrameOfInterestType, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.MaskPointers, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.RWavePointer, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ApplicableFrameRange, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.MaskFrameNumbers, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.MaskSubPixelShift, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ImageProcessingApplied, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.VerticesOfTheRegion, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.PixelShiftFrameRange, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.LUTFrameRange, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.ImageToEquipmentMappingMatrix, Multiplicity.M16);
        valueMultiplicity.put(TagFromName.ScheduledStudyLocationAETitle, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ChannelStatus, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.WaveformDisplayBackgroundCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ChannelRecommendedDisplayCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ScheduledStationAETitle, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ScheduledStationName, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ExposedArea, Multiplicity.M1_2);
        valueMultiplicity.put(TagFromName.NamesOfIntendedRecipientsOfResults, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PersonTelephoneNumbers, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.RealWorldValueLUTData, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.UrgencyOrPriorityAlertsTrial, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ReferencedWaveformChannels, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.ReportStatusIDTrial, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ReferencedSamplePositions, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ReferencedFrameNumbers, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ReferencedTimeOffsets, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ReferencedDateTime, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PixelCoordinatesSetTrial, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.NumericValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ReferencedContentItemIdentifier, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ListOfMIMETypes, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ProductName, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.VisualAcuityModifiers, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.RecommendedAbsentPixelCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ImageOrientationSlide, Multiplicity.M6);
        valueMultiplicity.put(TagFromName.TopLeftHandCornerOfLocalizerArea, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.BottomRightHandCornerOfLocalizerArea, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.EnergyWindowVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DetectorVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PhaseVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.RotationVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.RRIntervalVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TimeSlotVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SliceVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.AngularViewVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TimeSliceVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TriggerVector, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SeriesType, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.AxialMash, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.DetectorElementSize, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.SecondaryCountsType, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SecondaryCountsAccumulated, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CountsIncluded, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.HistogramData, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ReferencedSegmentNumber, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.RecommendedDisplayCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.GridDimensions, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.GridResolution, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.PointPositionAccuracy, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.PointsBoundingBoxCoordinates, Multiplicity.M6);
        valueMultiplicity.put(TagFromName.AxisOfRotation, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.CenterOfRotation, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.VectorAccuracy, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ViewOrientationModifier, Multiplicity.M9);
        valueMultiplicity.put(TagFromName.RecommendedRotationPoint, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.BoundingRectangle, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.ImplantTemplate3DModelSurfaceNumber, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TwoDMatingPoint, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.TwoDMatingAxes, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.ThreeDDegreeOfFreedomAxis, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.RangeOfFreedom, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ThreeDMatingPoint, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ThreeDMatingAxes, Multiplicity.M9);
        valueMultiplicity.put(TagFromName.TwoDDegreeOfFreedomAxis, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.TwoDPointCoordinates, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ThreeDPointCoordinates, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.TwoDLineCoordinates, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.ThreeDLineCoordinates, Multiplicity.M6);
        valueMultiplicity.put(TagFromName.TwoDPlaneIntersection, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.ThreeDPlaneOrigin, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ThreeDPlaneNormal, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.BoundingBoxTopLeftHandCorner, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.BoundingBoxBottomRightHandCorner, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.AnchorPoint, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.GraphicData, Multiplicity.M2_N);
        valueMultiplicity.put(TagFromName.DisplayedAreaTopLeftHandCornerTrial, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.DisplayedAreaBottomRightHandCornerTrial, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.DisplayedAreaTopLeftHandCorner, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.DisplayedAreaBottomRightHandCorner, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.GraphicLayerRecommendedDisplayRGBValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.PresentationPixelSpacing, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.PresentationPixelAspectRatio, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.TextColorCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ShadowColorCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.PatternOnColorCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.PatternOffColorCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.RotationPoint, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.GraphicLayerRecommendedDisplayCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.RelativeTime, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.AbstractPriorValue, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.SelectorSequencePointer, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorSequencePointerPrivateCreator, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorATValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorCSValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorISValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorLOValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorPNValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorSHValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorDSValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorFDValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorFLValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorULValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorUSValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorSLValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.SelectorSSValue, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DisplayEnvironmentSpatialPosition, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.DisplaySetScrollingGroup, Multiplicity.M2_N);
        valueMultiplicity.put(TagFromName.ReferenceDisplaySets, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.StructuredDisplayBackgroundCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.EmptyImageBoxCIELabValue, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.SynchronizedImageBoxList, Multiplicity.M2_N);
        valueMultiplicity.put(TagFromName.ThreeDRenderingType, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DisplaySetPatientOrientation, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.DoubleExposureFieldDeltaTrial, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.SelectorSequencePointerItems, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DoubleExposureFieldDelta, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.ThreeDImplantTemplateGroupMemberMatchingPoint, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ThreeDImplantTemplateGroupMemberMatchingAxes, Multiplicity.M9);
        valueMultiplicity.put(TagFromName.TwoDImplantTemplateGroupMemberMatchingPoint, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.TwoDImplantTemplateGroupMemberMatchingAxes, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.TopicKeywords, Multiplicity.M1_32);
        valueMultiplicity.put(TagFromName.DataElementsSigned, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.OtherMagnificationTypesAvailable, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.OtherSmoothingTypesAvailable, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.PrinterPixelSpacing, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ReferencedOverlayPlaneGroups, Multiplicity.M1_99);
        valueMultiplicity.put(TagFromName.FailureAttributes, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.XRayImageReceptorTranslation, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.RTImageOrientation, Multiplicity.M6);
        valueMultiplicity.put(TagFromName.ImagePlanePixelSpacing, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.RTImagePosition, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.DiaphragmPosition, Multiplicity.M4);
        valueMultiplicity.put(TagFromName.NormalizationPoint, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.GridFrameOffsetVector, Multiplicity.M2_N);
        valueMultiplicity.put(TagFromName.TissueHeterogeneityCorrection, Multiplicity.M1_3);
        valueMultiplicity.put(TagFromName.DVHNormalizationPoint, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.DVHData, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.ROIDisplayColor, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ContourOffsetVector, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.AttachedContours, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ContourData, Multiplicity.M3_3N);
        valueMultiplicity.put(TagFromName.FrameOfReferenceTransformationMatrix, Multiplicity.M16);
        valueMultiplicity.put(TagFromName.ScanSpotMetersetsDelivered, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TreatmentProtocols, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.TreatmentSites, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.DoseReferencePointCoordinates, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.BeamDoseSpecificationPoint, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.BrachyApplicationSetupDoseSpecificationPoint, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.LeafPositionBoundaries, Multiplicity.M3_N);
        valueMultiplicity.put(TagFromName.ImagingDeviceSpecificAcquisitionParameters, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CompensatorPixelSpacing, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.CompensatorPosition, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.CompensatorTransmissionData, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.CompensatorThicknessData, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.BlockData, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.LeafJawPositions, Multiplicity.M2_2N);
        valueMultiplicity.put(TagFromName.IsocenterPosition, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.SurfaceEntryPoint, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ControlPoint3DPosition, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.SourceToCompensatorDistance, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.IsocenterToCompensatorDistances, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.VirtualSourceAxisDistances, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ScanSpotPositionMap, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ScanSpotMetersetWeights, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.ScanningSpotSize, Multiplicity.M2);
        valueMultiplicity.put(TagFromName.ControlPointOrientation, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ThreatROIBase, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.ThreatROIExtents, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.CenterOfMass, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.CenterOfPTO, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.BoundingPolygon, Multiplicity.M6_N);
        valueMultiplicity.put(TagFromName.AbortReason, Multiplicity.M1_N);
        valueMultiplicity.put(TagFromName.OOISize, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.SourceOrientation, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.SourcePosition, Multiplicity.M3);
        valueMultiplicity.put(TagFromName.FileSetDescriptorFileID, Multiplicity.M1_8);
        valueMultiplicity.put(TagFromName.ReferencedFileID, Multiplicity.M1_8);
        valueMultiplicity.put(TagFromName.ReferencedRelatedGeneralSOPClassUIDInFile, Multiplicity.M1_N);

        int g;
        int e;

        for (e = 0x3100; e <= 0x31ff; e++) valueMultiplicity.put(new AttributeTag(0x0020, e), Multiplicity.M1_N);  // SourceImageIDs

        for (e = 0; e <= 0xfff; e++) {
            valueMultiplicity.put(new AttributeTag(0x1000, (e<<4) + 0), Multiplicity.M3);  // EscapeTriplet
            valueMultiplicity.put(new AttributeTag(0x1000, (e<<4) + 1), Multiplicity.M3);  // RunLengthTriplet
            valueMultiplicity.put(new AttributeTag(0x1000, (e<<4) + 3), Multiplicity.M3);  // HuffmanTableTriplet
            valueMultiplicity.put(new AttributeTag(0x1000, (e<<4) + 5), Multiplicity.M3);  // ShiftTableTriplet
        }
        for (e = 0x1010; e <= 0xffff; e++) valueMultiplicity.put(new AttributeTag(0x0020, e), Multiplicity.M1_N);  // ZonalMap

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
                TagFromName.VerticesOfThePolygonalCollimator,
                new AttributeTag(0x1000, (23<<4) + 1)
        };
        for (AttributeTag tag : tagList) {
            Multiplicity m = cd.getValueMultiplicity(tag);
            System.out.println("Tag: " + tag + "    Name: " + cd.getNameFromTag(tag) + "    Multiplicity: " + m);
        }

        System.out.println("varian tag: " + cd.getNameFromTag(new AttributeTag(0x3249, 0x0010)));
    }

}

