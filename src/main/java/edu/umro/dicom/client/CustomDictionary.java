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
        extensions.add(new PrivateTag(TagFromName.SourceofLensThicknessDataCodeSequence, "SourceofLensThicknessDataCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.SourceofAnteriorChamberDepthDataCodeSequence, "SrcOfAnterChambrDepthDataCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.SourceofRefractiveMeasurementsSequence, "SourceofRefractMeasrmntsSequence"));
        extensions.add(new PrivateTag(TagFromName.SourceofRefractiveMeasurementsCodeSequence, "SourceofRefractMeasrmntsCodeSeq"));
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
        extensions.add(new PrivateTag(TagFromName.AcquisitonMethodAlgorithmSequence, "AcquisitonMethodAlgSequence"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicThicknessMapTypeCodeSequence, "OpthmlcThicknessMapTypeCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.OphthalmicThicknessMappingNormalsSequence, "OpthmlcThicknessMappingNormlsSeq"));
        extensions.add(new PrivateTag(TagFromName.RetinalThicknessDefinitionCodeSequence, "RetinalThickDefinitionCodeSeq"));
        extensions.add(new PrivateTag(TagFromName.PixelValueMappingtoCodedConceptSequence, "PixValueMappingtoCodedConceptSeq"));
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
        extensions.add(new PrivateTag(TagFromName.ThreatDetectionAlgorithmandVersion, "ThreatDetectionAlgandVersion"));

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
            if (ClientConfig.getInstance().restrictXmlTagsToLength32()) {
                useShortenedAttributeNames();
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


    public static void main(String[] args) {
        CustomDictionary cd = new CustomDictionary();
        System.out.println("varian tag: " + cd.getNameFromTag(new AttributeTag(0x3249, 0x0010)));
    }

}

