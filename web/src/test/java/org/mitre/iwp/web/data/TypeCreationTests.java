/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.data;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mitre.jet.ebts.Ebts;
import org.mitre.jet.ebts.field.Field;
import org.mitre.jet.ebts.field.Occurrence;
import org.mitre.jet.ebts.field.SubField;
import org.mitre.jet.ebts.records.GenericRecord;
import org.mitre.jet.ebts.records.LogicalRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TypeCreationTests {

    @Value("classpath:srb.eft")
    private Resource srbResponse;

    @Test
    public void TestType17Creation() {
        Ebts ebts = new Ebts();

        LogicalRecord record = new GenericRecord(17);

        // 17.002 - Image Designation Character
        String idc = "01";
        record.setField(2, createField(idc));

        // 17.003 - Eye Label
        String elr = "1";
        record.setField(3, createField(elr));

        // 17.004 - Source Agency
        String src = "SAGENCY37";
        record.setField(4, createField(src));

        // 17.005 - Iris Capture Date
        String icd = "17760704";
        record.setField(5, createField(icd));

        // 17.006 - Horizontal Line Length
        String hll = "99999";
        record.setField(6, createField(hll));

        // 17.007 - Vertical Line Length
        String vll = "9999";
        record.setField(7, createField(vll));

        // 17.008 - Scale Units
        String slc = "1";
        record.setField(8, createField(slc));

        // 17.009 - Transmitted Horizontal Pixel Scale
        String thps = "1000";
        record.setField(9, createField(thps));

        // 17.010 - Transmitted Vertical Pixel Scale
        String tvps = "1000";
        record.setField(10, createField(tvps));

        // 17.011 - Compression Algorithm
        String cga = "NONE";
        record.setField(11, createField(cga));

        // 17.012 - Bits Per Pixel
        String bpx = "8";
        record.setField(12, createField(bpx));

        // 17.013 - Color Space
        String csp = "GRAY";
        record.setField(13, createField(csp));

        // 17.014 - Rotation Angle of the Eye
        String rae = "4000";
        record.setField(14, createField(rae));

        // 17.015 - Rotation Uncertainty
        String rau = "FFFF";
        record.setField(15, createField(rau));

        // 17.016 - Image Property Code
        String iho = "1";
        String ivo = "1";
        String ist = "1";
        record.setField(16, createField(iho, ivo, ist));

        // 17.017 - Device Unique Identifier
        String dui = "ALSDFOISDFLKWNEINDSLSDFSD";
        record.setField(17, createField(dui));

        // 17.019 - Make Model Serial Number
        String mak = "Generic Make";
        String mod = "Generic Model";
        String ser = "Generic Serial";
        record.setField(19, createField(mak, mod, ser));

        // 17.020 - Eye Color
        String ecl = "XXX";
        record.setField(20, createField(ecl));

        // 17.021 - Comment
        String com = "Free text field with commenting information, for the sake of commenting";
        record.setField(21, createField(com));

        //17.025 - Effective Aquisition Spectrum
        String eas = "NIR";
        record.setField(25, createField(eas));

        // 17.028 - Damaged or Missing Eye
        String dme = "UC";
        record.setField(28, createField(dme));

        // 17.031 - Subject Acquisition Profile - Iris
        String iap = "40";
        record.setField(31, createField(iap));

        // 17.032 - Iris Storage Format
        String isf = "1";
        record.setField(32, createField(isf));

        ebts.addRecord(record);
        List<LogicalRecord> recordList = ebts.getRecordsByType(17);

        if(recordList.size() > 0) {

            LogicalRecord temp = recordList.get(0);

            Type17 t1 = Type17.buildFromEbtsRecord(temp);
            Assertions.assertEquals(idc, t1.getImageDesignationCharacter());
            Assertions.assertEquals(Integer.parseInt(elr), t1.getEyeLabel());
            Assertions.assertEquals(src, t1.getSourceAgency());
            Assertions.assertEquals(icd, t1.getIrisCaptureDate());
            Assertions.assertEquals(Integer.parseInt(hll), t1.getHorizontalLineLength());
            Assertions.assertEquals(Integer.parseInt(vll), t1.getVerticalLineLength());
            Assertions.assertEquals(Integer.parseInt(slc), t1.getScaleUnits());
            Assertions.assertEquals(Integer.parseInt(thps), t1.getTransmittedHorizontalPixelScale());
            Assertions.assertEquals(Integer.parseInt(tvps), t1.getTransmittedVerticalPixelScale());
            Assertions.assertEquals(cga, t1.getCompressionAlgorithm());
            Assertions.assertEquals(Integer.parseInt(bpx), t1.getBitsPerPixel());
            Assertions.assertEquals(csp, t1.getColorSpace());
            Assertions.assertEquals(Integer.parseInt(rae), t1.getRotationAngleOfEye());
            Assertions.assertEquals(rau, t1.getRotationUncertainty());
            Assertions.assertEquals(Integer.parseInt(iho), t1.getImagePropertyCodeField().getHorizontalOrientationCode());
            Assertions.assertEquals(Integer.parseInt(ivo), t1.getImagePropertyCodeField().getVerticalOrientationCode());
            Assertions.assertEquals(Integer.parseInt(ist), t1.getImagePropertyCodeField().getSpecificScanType());
            Assertions.assertEquals(dui, t1.getDeviceUniqueIdentifier());
            Assertions.assertEquals(mak, t1.getMakeModelSerialNumberField().getMake());
            Assertions.assertEquals(mod, t1.getMakeModelSerialNumberField().getModel());
            Assertions.assertEquals(ser, t1.getMakeModelSerialNumberField().getSerialNumber());
            Assertions.assertEquals(ecl, t1.getEyeColor());
            Assertions.assertEquals(com, t1.getComment());
            Assertions.assertEquals(eas, t1.getEffectiveAcquisitionSpectrum());
            Assertions.assertEquals(Integer.parseInt(iap), t1.getSubjectAcquisitionProfileIris());
            Assertions.assertEquals(Integer.parseInt(isf), t1.getIrisStorageFormat());
            Assertions.assertEquals(dme, t1.getDamagedOrMissingEye());

            /*
             ******************************
             *
             *
             * Begin test conversion From Type 2
             * to Logical Record
             *
             *
             *
             ******************************/

            LogicalRecord comparisonRecord = Type17.buildEbtsLogicalRecord(t1);

            Assertions.assertEquals(Integer.toString(t1.getEyeLabel()), comparisonRecord.getField(3).toString());
            Assertions.assertEquals(t1.getSourceAgency(), comparisonRecord.getField(4).toString());
            Assertions.assertEquals(t1.getIrisCaptureDate(), comparisonRecord.getField(5).toString());
            Assertions.assertEquals(Integer.toString(t1.getHorizontalLineLength()), comparisonRecord.getField(6).toString());
            Assertions.assertEquals(Integer.toString(t1.getVerticalLineLength()), comparisonRecord.getField(7).toString());
            Assertions.assertEquals(Integer.toString(t1.getScaleUnits()), comparisonRecord.getField(8).toString());
            Assertions.assertEquals(Integer.toString(t1.getTransmittedHorizontalPixelScale()), comparisonRecord.getField(9).toString());
            Assertions.assertEquals(Integer.toString(t1.getTransmittedVerticalPixelScale()), comparisonRecord.getField(10).toString());
            Assertions.assertEquals(t1.getCompressionAlgorithm(), comparisonRecord.getField(11).toString());
            Assertions.assertEquals(Integer.toString(t1.getBitsPerPixel()), comparisonRecord.getField(12).toString());
            Assertions.assertEquals(t1.getColorSpace(), comparisonRecord.getField(13).toString());
            Assertions.assertEquals(Integer.toString(t1.getRotationAngleOfEye()), comparisonRecord.getField(14).toString());
            Assertions.assertEquals(t1.getRotationUncertainty(), comparisonRecord.getField(15).toString());

            Field tempField = comparisonRecord.getField(16);
            Assertions.assertEquals(1, tempField.getOccurrences().size());
            Occurrence tempOcc = tempField.getOccurrences().get(0);
            Assertions.assertEquals(3, tempOcc.getSubFields().size());
            Assertions.assertEquals(Integer.toString(t1.getImagePropertyCodeField().getHorizontalOrientationCode()), tempOcc.getSubFields().get(0).toString());
            Assertions.assertEquals(Integer.toString(t1.getImagePropertyCodeField().getVerticalOrientationCode()), tempOcc.getSubFields().get(1).toString());
            Assertions.assertEquals(Integer.toString(t1.getImagePropertyCodeField().getSpecificScanType()), tempOcc.getSubFields().get(2).toString());

            Assertions.assertEquals(t1.getDeviceUniqueIdentifier(), comparisonRecord.getField(17).toString());

            tempField = comparisonRecord.getField(19);
            Assertions.assertEquals(1, tempField.getOccurrences().size());
            tempOcc = tempField.getOccurrences().get(0);
            Assertions.assertEquals(3, tempOcc.getSubFields().size());
            Assertions.assertEquals(t1.getMakeModelSerialNumberField().getMake(), tempOcc.getSubFields().get(0).toString());
            Assertions.assertEquals(t1.getMakeModelSerialNumberField().getModel(), tempOcc.getSubFields().get(1).toString());
            Assertions.assertEquals(t1.getMakeModelSerialNumberField().getSerialNumber(), tempOcc.getSubFields().get(2).toString());

            Assertions.assertEquals(t1.getEyeColor(), comparisonRecord.getField(20).toString());
            Assertions.assertEquals(t1.getComment(), comparisonRecord.getField(21).toString());
            Assertions.assertEquals(t1.getEffectiveAcquisitionSpectrum(), comparisonRecord.getField(25).toString());
            Assertions.assertEquals(Integer.toString(t1.getSubjectAcquisitionProfileIris()), comparisonRecord.getField(31).toString());
            Assertions.assertEquals(Integer.toString(t1.getIrisStorageFormat()), comparisonRecord.getField(32).toString());
        }
    }

    @Test
    public void TestType2Creation() {
        Ebts ebts = new Ebts();

        LogicalRecord record = new GenericRecord(2);

        // 2.002 - Image Designation Character
        String idc = "01";
        record.setField(2, createField(idc));

        // 2.006 - Attention Indicator
        String atn = "Test";
        record.setField(6, createField(atn));

        // 2.007 - Send Copy To
        String sco1 = "WVCOPY123";
        String sco2 = "WVCOPY1234";
        String sco3 = "WVCOPY12345";
        record.setField(7, createFieldWithMultipleOccurences(sco1, sco2, sco3));

        // 2.009 - Originating Agency Case Number
        String oca = "AGENCY123";
        record.setField(9, createField(oca));

        // 2.010 - Contributor Case Identifier Number
        String cinPre = "Contributor Case Id";
        String cinId = "10";
        List<Occurrence> occurenceList = new ArrayList<>();
        occurenceList.add(createOccurence(cinPre, cinId));
        occurenceList.add(createOccurence(cinPre, cinId));
        record.setField(10, createFieldFromOccurrences(occurenceList));

        // 2.011 - Contributor Case ID Ext
        String cix1 = "1";
        String cix2 = "2";
        String cix3 = "3";
        record.setField(11, createFieldWithMultipleOccurences(cix1, cix2, cix3));

        // 2.014 - Universal Control Number
        String ucn = "123456FH8";
        record.setField(14, createField(ucn));

        // 2.015 - State ID
        String sid1 = "WV12345678";
        String sid2 = "NC87654321";
        record.setField(15, createFieldWithMultipleOccurences(sid1, sid2));

        // 2.017 - Miscellaneious Identification Number
        String mnu1 = "MNU_VAL_1";
        String mnu2 = "MNU_VAL_2";
        record.setField(17, createFieldWithMultipleOccurences(mnu1, mnu2));

        // 2.018 - Name
        String nam = "Smith,John";
        record.setField(18, createField(nam));

        // 2.020 - Place of Birth
        String pob = "WV";
        record.setField(20, createField(pob));

        // 2.021 - Citizenship
        String ctz = "US";
        record.setField(21, createField(ctz));

        // 2.022 - Date of Birth
        String dob = "17760704";
        record.setField(22, createField(dob));

        // 2.023 - Age Range
        String agr = "2535";
        record.setField(23, createField(agr));

        // 2.024 - Sex
        String sex = "F";
        record.setField(24, createField(sex));

        // 2.025 - Race
        String rac = "U";
        record.setField(25, createField(rac));

        // 2.026 - Scars Marks and Tattoos
        String smt = "TAT ARM";
        record.setField(26, createField(smt));

        // 2.028 - Height Range
        String htr = "400711";
        record.setField(28, createField(htr));

        // 2.030 - Weight Range
        String wtr = "175185";
        record.setField(30, createField(wtr));

        // 2.031 - Eye Color
        String eye = "MAR";
        record.setField(31, createField(eye));

        // 2.032 - Hair Color
        String hai = "GRN";
        record.setField(32, createField(hai));

        // 2.036 - Photo Available
        String pht = "Y";
        record.setField(36, createField(pht));

        // 2.038 - Date Printed
        String dpr = "17760704";
        record.setField(38, createField(dpr));

        // 2.059 - Search Result Findings
        String srf = "Y";
        record.setField(59, createField(srf));

        // 2.060 - Status Error Message
        String msg1 = "Error Message Number 1";
        String msg2 = "Error Message, but not the same as message 1";
        record.setField(60, createFieldWithMultipleOccurences(msg1, msg2));

        // 2.062 - Image Type
        String imt1 = "image_11";
        String imt2 = "image_12";
        record.setField(62, createFieldWithMultipleOccurences(imt1, imt2));

        // 2.073 - Controlling Agency Identifier
        String cri1 = "AGENCY1";
        String cri2 = "AGENCY2";
        record.setField(73, createFieldWithMultipleOccurences(cri1, cri2));

        // 2.079 - Number of Candidates Returned
        String ncr = "7";
        record.setField(79, createField(ncr));

        // 2.088 - NoteField
        String not = "This is a note";
        record.setField(88, createField(not));

        // 2.096 - Request Photo Record
        String rpr = "Y";
        record.setField(96, createField(rpr));

        // 2.2010 - Number of Images Requested
        String nir = "37";
        record.setField(2010, createField(nir));

        // 2.2023 - Supplementary Identity Information
        String sii = "00 - Armed and Dangerous";
        record.setField(2023, createField(sii));

        // 2.2028 - Biometric Image Description
        String si = "123456HU8";
        String imt = "9";
        String bsi = "0123456789012345";
        String fnr = "32";
        String ppd = "23";
        String pos = "F";
        String elr = "3";
        occurenceList = new ArrayList<>();
        occurenceList.add(createOccurence(si, imt, bsi, fnr, ppd, pos, smt, elr));
        occurenceList.add(createOccurence(si, imt, bsi, fnr, ppd, pos, smt, elr));
        record.setField(2028, createFieldFromOccurrences(occurenceList));

        // 2.2029 - Biometric Set Identifier
        record.setField(2029, createField(bsi));

        // 2.2031 - Biometric Image Available
        String bia = "3";
        record.setField(2031, createField(bia));

        // 2.2033 - Candidate Invastigative List
        String fgp = "137";
        String msc = "1200";
        String ndr = "237";
        occurenceList = new ArrayList<>();
        occurenceList.add(createOccurence(si, nam, bsi, imt, fgp, ppd, msc, bia, ndr, idc, not, pos, smt, elr));
        occurenceList.add(createOccurence(si, nam, bsi, imt, fgp, ppd, msc, bia, ndr, idc, not, pos, smt, elr));
        occurenceList.add(createOccurence(si, nam, bsi, imt, fgp, ppd, msc, bia, ndr, idc, not, pos, smt, elr));
        record.setField(2033, createFieldFromOccurrences(occurenceList));

        // 2.2035 - Event Identifier
        String evi = "";
        record.setField(2035, createField(evi));

        // 2.2061 - Biometric Image Enrollment
        occurenceList = new ArrayList<>();
        occurenceList.add(createOccurence(bsi, imt, pos, smt));
        occurenceList.add(createOccurence(bsi, imt, pos, smt));
        record.setField(2061, createFieldFromOccurrences(occurenceList));

        // 2.2073 - Biometric Image List
        Timestamp bcd = Timestamp.valueOf("1776-07-04 01:02:03.123");
        occurenceList = new ArrayList<>();
        occurenceList.add(createOccurence(si, bsi, bcd.toString(), imt));
        occurenceList.add(createOccurence(si, bsi, bcd.toString(), imt));
        record.setField(2073, createFieldFromOccurrences(occurenceList));

        ebts.addRecord(record);
        List<LogicalRecord> recordList = ebts.getRecordsByType(2);

        if(recordList.size() > 0) {
            LogicalRecord temp = recordList.get(0);

            Type2 t2 = Type2.buildFromEbtsRecord(temp);

            Assertions.assertEquals(idc, t2.getInformationDesignationCharacter());
            Assertions.assertEquals(atn, t2.getAttentionIndicator());

            List<String> scoList = t2.getSendCopyTo();
            Assertions.assertTrue(scoList.contains(sco1));
            Assertions.assertTrue(scoList.contains(sco2));
            Assertions.assertTrue(scoList.contains(sco3));
            Assertions.assertEquals(3, scoList.size());

            Assertions.assertEquals(oca, t2.getOriginatingAgencyCaseNumber());

            List<ContributorCaseIdentifierNumberField> ccinList = t2.getContributorCaseIdentifierNumber();

            for(ContributorCaseIdentifierNumberField ccin : ccinList){
                Assertions.assertEquals(cinPre, ccin.getContributorCasePrefix());
                Assertions.assertEquals(cinId, ccin.getContributorCaseId());
            }
            Assertions.assertEquals(2, ccinList.size());

            List<Integer> cixList = t2.getContributorCaseIdExtension();
            Assertions.assertTrue(cixList.contains(Integer.parseInt(cix1)));
            Assertions.assertTrue(cixList.contains(Integer.parseInt(cix2)));
            Assertions.assertTrue(cixList.contains(Integer.parseInt(cix3)));
            Assertions.assertEquals(3, cixList.size());

            Assertions.assertEquals(ucn, t2.getUniversalControlNumber());

            List<String> sidList = t2.getStateIdentificationNumber();
            Assertions.assertTrue(sidList.contains(sid1));
            Assertions.assertTrue(sidList.contains(sid2));
            Assertions.assertEquals(2, sidList.size());

            List<String> mnuList = t2.getMiscellaneousIdentificationNumber();
            Assertions.assertTrue(mnuList.contains(mnu1));
            Assertions.assertTrue(mnuList.contains(mnu2));
            Assertions.assertEquals(2, mnuList.size());

            Assertions.assertEquals(nam, t2.getName());
            Assertions.assertEquals(pob, t2.getPlaceOfBirth());
            Assertions.assertEquals(ctz, t2.getCitizenship());
            Assertions.assertEquals(dob, t2.getDateOfBirth());
            Assertions.assertEquals(Integer.parseInt(agr), t2.getAgeRange());
            Assertions.assertEquals(sex, t2.getSex());
            Assertions.assertEquals(rac, t2.getRace());
            Assertions.assertEquals(smt, t2.getScarsMarksAndTattoos());
            Assertions.assertEquals(Integer.parseInt(htr), t2.getHeightRange());
            Assertions.assertEquals(Integer.parseInt(wtr), t2.getWeightRange());
            Assertions.assertEquals(eye, t2.getEyeColor());
            Assertions.assertEquals(hai, t2.getHairColor());
            Assertions.assertEquals(pht, t2.getPhotoAvailableIndicator());
            Assertions.assertEquals(dpr, t2.getDatePrinted());
            Assertions.assertEquals(srf, t2.getSearchResultFindings());

            List<String> errorMessageList = t2.getStatusErrorMessage();
            Assertions.assertTrue(errorMessageList.contains(msg1));
            Assertions.assertTrue(errorMessageList.contains(msg2));
            Assertions.assertEquals(2, errorMessageList.size());

            List<String> imageTypeList = t2.getImageType();
            Assertions.assertTrue(imageTypeList.contains(imt1));
            Assertions.assertTrue(imageTypeList.contains(imt2));
            Assertions.assertEquals(2, imageTypeList.size());


            List<String> controllingAgencyList = t2.getControllingAgencyIdentifier();
            Assertions.assertTrue(controllingAgencyList.contains(cri1));
            Assertions.assertTrue(controllingAgencyList.contains(cri2));
            Assertions.assertEquals(2, controllingAgencyList.size());

            Assertions.assertEquals(Integer.parseInt(ncr), t2.getNumberOfCandidatesReturned());
            Assertions.assertEquals(not, t2.getNoteField());
            Assertions.assertEquals(rpr, t2.getRequestPhotoRecord());
            Assertions.assertEquals(Integer.parseInt(nir), t2.getNumberOfImagesRequested());
            Assertions.assertEquals(sii, t2.getSupplementaryIdentityInformation());

            List<BiometricImageDescriptionField> bidList = t2.getBiometricImageDescription();
            for(BiometricImageDescriptionField bidField : bidList) {
                Assertions.assertEquals(si, bidField.getSubjectIdentifier());
                Assertions.assertEquals(Integer.parseInt(imt), bidField.getImageType());
                Assertions.assertEquals(bsi, bidField.getBiometricSetIdentifier());
                Assertions.assertEquals(Integer.parseInt(fnr), bidField.getFingerNumberRequested());
                Assertions.assertEquals(ppd, bidField.getPrintPositionDescriptors());
                Assertions.assertEquals(pos, bidField.getSubjectPose());
                Assertions.assertEquals(smt, bidField.getNcicSmtCode());
                Assertions.assertEquals(Integer.parseInt(elr), bidField.getEyeLabel());
            }

            Assertions.assertEquals(2, bidList.size());
            Assertions.assertEquals(bsi, t2.getBiometricSetIdentifier());
            Assertions.assertEquals(Integer.parseInt(bia), t2.getBiometricImageAvailable());

            List<CandidateInvesitgativeListField> ciList = t2.getCandidateInvesitgativeListField();
            for(CandidateInvesitgativeListField ciField : ciList) {
                Assertions.assertEquals(si, ciField.getSubjectIdentifier());
                Assertions.assertEquals(nam, ciField.getMasterName());
                Assertions.assertEquals(bsi, ciField.getBiometricSetIdentifier());
                Assertions.assertEquals(Integer.parseInt(imt), ciField.getImageType());
                Assertions.assertEquals(Integer.parseInt(fgp), ciField.getFrictionRidgeGeneralizedPosition());
                Assertions.assertEquals(ppd, ciField.getPrintPositionDescriptor());
                Assertions.assertEquals(Integer.parseInt(msc), ciField.getMatchScore());
                Assertions.assertEquals(Integer.parseInt(bia), ciField.getBiometricImageAvailable());
                Assertions.assertEquals(Integer.parseInt(ndr), ciField.getNameOfDesignatedRepository());
                Assertions.assertEquals(idc, ciField.getInformationDesignationCharacter());
                Assertions.assertEquals(not, ciField.getNoteField());
                Assertions.assertEquals(pos, ciField.getSubjectPose());
                Assertions.assertEquals(smt, ciField.getNcicSmtCode());
                Assertions.assertEquals(Integer.parseInt(elr), ciField.getEyeLabel());
            }

            Assertions.assertEquals(3, ciList.size());

            Assertions.assertEquals(evi, t2.getEventIdentifier());

            List<BiometricImageEnrollmentField> bieList = t2.getBiometricImageEnrollment();
            for(BiometricImageEnrollmentField bie : bieList)
            {
                Assertions.assertEquals(bsi, bie.getBiometricSetIdentifier());
                Assertions.assertEquals(Integer.parseInt(imt), bie.getImageType());
                Assertions.assertEquals(pos, bie.getSubjectPose());
                Assertions.assertEquals(smt, bie.getScarsMarksTattoos());
            }

            Assertions.assertEquals(2, bieList.size());

            List<BiometricImageListField> bilList = t2.getBiometricImageList();
            for(BiometricImageListField bil : bilList){
                Assertions.assertEquals(si, bil.getSubjectIdentifier());
                Assertions.assertEquals(bsi, bil.getBiometricSetIdentifier());
                Assertions.assertEquals(bcd, bil.getBiometricCaptureDate());
                Assertions.assertEquals(Integer.parseInt(imt), bil.getImageType());
            }

            Assertions.assertEquals(2, bilList.size());

            /*
             ******************************
             *
             * Begin test conversion From Type 2
             * to Logical Record
             *
             *
             *
             ******************************/

            LogicalRecord comparisonRecord = Type2.buildEbtsLogicalRecord(t2);
            Assertions.assertEquals(t2.getAttentionIndicator(), comparisonRecord.getField(6).toString());

            Field tempField = comparisonRecord.getField(7);
            Assertions.assertEquals(3, tempField.getOccurrences().size());
            for (int i = 0; i < 3; i++) {
                Assertions.assertEquals(t2.getSendCopyTo().get(i), tempField.getOccurrences().get(i).toString());
            }

            Assertions.assertEquals(t2.getOriginatingAgencyCaseNumber(), comparisonRecord.getField(9).toString());

            tempField = comparisonRecord.getField(10);
            Assertions.assertEquals(2, tempField.getOccurrences().size());
            Occurrence tempOccurrence = tempField.getOccurrences().get(0);
            Assertions.assertEquals(2, tempOccurrence.getSubFields().size());
            Assertions.assertEquals(t2.getContributorCaseIdentifierNumber().get(0).getContributorCasePrefix(), tempOccurrence.getSubFields().get(0).toString());
            Assertions.assertEquals(t2.getContributorCaseIdentifierNumber().get(0).getContributorCaseId(), tempOccurrence.getSubFields().get(1).toString());
            tempOccurrence = tempField.getOccurrences().get(1);
            Assertions.assertEquals(2, tempOccurrence.getSubFields().size());
            Assertions.assertEquals(t2.getContributorCaseIdentifierNumber().get(1).getContributorCasePrefix(), tempOccurrence.getSubFields().get(0).toString());
            Assertions.assertEquals(t2.getContributorCaseIdentifierNumber().get(1).getContributorCaseId(), tempOccurrence.getSubFields().get(1).toString());

            tempField = comparisonRecord.getField(11);
            Assertions.assertEquals(3, tempField.getOccurrences().size());
            for (int i = 0; i < 3; i++) {
                Assertions.assertEquals(Integer.toString(t2.getContributorCaseIdExtension().get(i)), tempField.getOccurrences().get(i).toString());
            }

            Assertions.assertEquals(t2.getUniversalControlNumber(), comparisonRecord.getField(14).toString());

            tempField = comparisonRecord.getField(15);
            Assertions.assertEquals(2, tempField.getOccurrences().size());
            Assertions.assertEquals(t2.getStateIdentificationNumber().get(0), tempField.getOccurrences().get(0).toString());
            Assertions.assertEquals(t2.getStateIdentificationNumber().get(1), tempField.getOccurrences().get(1).toString());

            tempField = comparisonRecord.getField(17);
            Assertions.assertEquals(2, tempField.getOccurrences().size());
            Assertions.assertEquals(t2.getMiscellaneousIdentificationNumber().get(0), tempField.getOccurrences().get(0).toString());
            Assertions.assertEquals(t2.getMiscellaneousIdentificationNumber().get(1), tempField.getOccurrences().get(1).toString());

            Assertions.assertEquals(t2.getName(), comparisonRecord.getField(18).toString());
            Assertions.assertEquals(t2.getPlaceOfBirth(), comparisonRecord.getField(20).toString());
            Assertions.assertEquals(t2.getCitizenship(), comparisonRecord.getField(21).toString());
            Assertions.assertEquals(t2.getDateOfBirth(), comparisonRecord.getField(22).toString());
            Assertions.assertEquals(Integer.toString(t2.getAgeRange()), comparisonRecord.getField(23).toString());
            Assertions.assertEquals(t2.getSex(), comparisonRecord.getField(24).toString());
            Assertions.assertEquals(t2.getRace(), comparisonRecord.getField(25).toString());
            Assertions.assertEquals(t2.getScarsMarksAndTattoos(), comparisonRecord.getField(26).toString());
            Assertions.assertEquals(Integer.toString(t2.getHeightRange()), comparisonRecord.getField(28).toString());
            Assertions.assertEquals(Integer.toString(t2.getWeightRange()), comparisonRecord.getField(30).toString());
            Assertions.assertEquals(t2.getEyeColor(), comparisonRecord.getField(31).toString());
            Assertions.assertEquals(t2.getHairColor(), comparisonRecord.getField(32).toString());
            Assertions.assertEquals(t2.getPhotoAvailableIndicator(), comparisonRecord.getField(36).toString());
            Assertions.assertEquals(t2.getDatePrinted(), comparisonRecord.getField(38).toString());
            Assertions.assertEquals(t2.getSearchResultFindings(), comparisonRecord.getField(59).toString());

            tempField = comparisonRecord.getField(60);
            Assertions.assertEquals(2, tempField.getOccurrences().size());
            Assertions.assertEquals(t2.getStatusErrorMessage().get(0), tempField.getOccurrences().get(0).toString());
            Assertions.assertEquals(t2.getStatusErrorMessage().get(1), tempField.getOccurrences().get(1).toString());

            tempField = comparisonRecord.getField(62);
            Assertions.assertEquals(2, tempField.getOccurrences().size());
            Assertions.assertEquals(t2.getImageType().get(0), tempField.getOccurrences().get(0).toString());
            Assertions.assertEquals(t2.getImageType().get(1), tempField.getOccurrences().get(1).toString());

            tempField = comparisonRecord.getField(73);
            Assertions.assertEquals(2, tempField.getOccurrences().size());
            Assertions.assertEquals(t2.getControllingAgencyIdentifier().get(0), tempField.getOccurrences().get(0).toString());
            Assertions.assertEquals(t2.getControllingAgencyIdentifier().get(1), tempField.getOccurrences().get(1).toString());

            Assertions.assertEquals(Integer.toString(t2.getNumberOfCandidatesReturned()), comparisonRecord.getField(79).toString());
            Assertions.assertEquals(t2.getNoteField(), comparisonRecord.getField(88).toString());
            Assertions.assertEquals(t2.getRequestPhotoRecord(), comparisonRecord.getField(96).toString());
            Assertions.assertEquals(Integer.toString(t2.getNumberOfImagesRequested()), comparisonRecord.getField(2010).toString());
            Assertions.assertEquals(t2.getSupplementaryIdentityInformation(), comparisonRecord.getField(2023).toString());

            tempField = comparisonRecord.getField(2028);
            Assertions.assertEquals(2, tempField.getOccurrences().size());

            for (int i = 0; i < 2; i++) {
                tempOccurrence = tempField.getOccurrences().get(i);
                Assertions.assertEquals(8, tempOccurrence.getSubFields().size());
                BiometricImageDescriptionField bid = t2.getBiometricImageDescription().get(i);
                Assertions.assertEquals(bid.getSubjectIdentifier(), tempOccurrence.getSubFields().get(0).toString());
                Assertions.assertEquals(Integer.toString(bid.getImageType()), tempOccurrence.getSubFields().get(1).toString());
                Assertions.assertEquals(bid.getBiometricSetIdentifier(), tempOccurrence.getSubFields().get(2).toString());
                Assertions.assertEquals(Integer.toString(bid.getFingerNumberRequested()), tempOccurrence.getSubFields().get(3).toString());
                Assertions.assertEquals(bid.getPrintPositionDescriptors(), tempOccurrence.getSubFields().get(4).toString());
                Assertions.assertEquals(bid.getSubjectPose(), tempOccurrence.getSubFields().get(5).toString());
                Assertions.assertEquals(bid.getNcicSmtCode(), tempOccurrence.getSubFields().get(6).toString());
                Assertions.assertEquals(Integer.toString(bid.getEyeLabel()), tempOccurrence.getSubFields().get(7).toString());
            }

            Assertions.assertEquals(t2.getBiometricSetIdentifier(), comparisonRecord.getField(2029).toString());
            Assertions.assertEquals(Integer.toString(t2.getBiometricImageAvailable()), comparisonRecord.getField(2031).toString());

            tempField = comparisonRecord.getField(2033);
            Assertions.assertEquals(3, tempField.getOccurrences().size());

            for (int i = 0; i < 3; i++) {
                tempOccurrence = tempField.getOccurrences().get(i);
                Assertions.assertEquals(14, tempOccurrence.getSubFields().size());
                CandidateInvesitgativeListField cil = t2.getCandidateInvesitgativeListField().get(i);
                Assertions.assertEquals(cil.getSubjectIdentifier(), tempOccurrence.getSubFields().get(0).toString());
                Assertions.assertEquals(cil.getMasterName(), tempOccurrence.getSubFields().get(1).toString());
                Assertions.assertEquals(cil.getBiometricSetIdentifier(), tempOccurrence.getSubFields().get(2).toString());
                Assertions.assertEquals(Integer.toString(cil.getImageType()), tempOccurrence.getSubFields().get(3).toString());
                Assertions.assertEquals(Integer.toString(cil.getFrictionRidgeGeneralizedPosition()), tempOccurrence.getSubFields().get(4).toString());
                Assertions.assertEquals(cil.getPrintPositionDescriptor(), tempOccurrence.getSubFields().get(5).toString());
                Assertions.assertEquals(Integer.toString(cil.getMatchScore()), tempOccurrence.getSubFields().get(6).toString());
                Assertions.assertEquals(Integer.toString(cil.getBiometricImageAvailable()), tempOccurrence.getSubFields().get(7).toString());
                Assertions.assertEquals(Integer.toString(cil.getNameOfDesignatedRepository()), tempOccurrence.getSubFields().get(8).toString());
                Assertions.assertEquals(cil.getInformationDesignationCharacter(), tempOccurrence.getSubFields().get(9).toString());
                Assertions.assertEquals(cil.getNoteField(), tempOccurrence.getSubFields().get(10).toString());
                Assertions.assertEquals(cil.getSubjectPose(), tempOccurrence.getSubFields().get(11).toString());
                Assertions.assertEquals(cil.getNcicSmtCode(), tempOccurrence.getSubFields().get(12).toString());
                Assertions.assertEquals(Integer.toString(cil.getEyeLabel()), tempOccurrence.getSubFields().get(13).toString());
            }

            Assertions.assertEquals(t2.getEventIdentifier(), comparisonRecord.getField(2035).toString());

            tempField = comparisonRecord.getField(2061);
            Assertions.assertEquals(2, tempField.getOccurrences().size());

            for (int i = 0; i < 2; i++) {
                tempOccurrence = tempField.getOccurrences().get(i);
                Assertions.assertEquals(4, tempOccurrence.getSubFields().size());
                BiometricImageEnrollmentField bie = t2.getBiometricImageEnrollment().get(i);
                Assertions.assertEquals(bie.getBiometricSetIdentifier(), tempOccurrence.getSubFields().get(0).toString());
                Assertions.assertEquals(Integer.toString(bie.getImageType()), tempOccurrence.getSubFields().get(1).toString());
                Assertions.assertEquals(bie.getSubjectPose(), tempOccurrence.getSubFields().get(2).toString());
                Assertions.assertEquals(bie.getScarsMarksTattoos(), tempOccurrence.getSubFields().get(3).toString());
            }

            tempField = comparisonRecord.getField(2073);
            Assertions.assertEquals(2, tempField.getOccurrences().size());

            for (int i = 0; i < 2; i++) {
                tempOccurrence = tempField.getOccurrences().get(i);
                Assertions.assertEquals(4, tempOccurrence.getSubFields().size());
                BiometricImageListField bil = t2.getBiometricImageList().get(i);
                Assertions.assertEquals(bil.getSubjectIdentifier(), tempOccurrence.getSubFields().get(0).toString());
                Assertions.assertEquals(bil.getBiometricSetIdentifier(), tempOccurrence.getSubFields().get(1).toString());
                Assertions.assertEquals(bil.getBiometricCaptureDate(), Timestamp.valueOf(tempOccurrence.getSubFields().get(2).toString()));
                Assertions.assertEquals(Integer.toString(bil.getImageType()), tempOccurrence.getSubFields().get(3).toString());
            }
        }
    }

    @Test
    public void TestType1Creation() {
        Ebts ebts = new Ebts();

        LogicalRecord record = new GenericRecord(1);

        // 1.002 - Version
        String ver = "0500";
        record.setField(2, createField(ver));

        // 1.003 - File Content
        String frc = "1";
        String crc = "01";
        String rec = "02";
        String idc = "00";
        List<Occurrence> occList = new ArrayList<>();
        occList.add(createOccurence(frc, crc));
        occList.add(createOccurence(rec, idc));
        record.setField(3, createFieldFromOccurrences(occList));

        // 1.004 - Type of Transaction
        String tot = "SRE";
        record.setField(4, createField(tot));

        // 1.005 - Date
        String dat = "17760704";
        record.setField(5, createField(dat));

        // 1.006 - Transaction Priority
        String pry = "1";
        record.setField(6, createField(pry));

        // 1.007 - Destination Agency ID
        String dai = "DESTAGC37";
        record.setField(7, createField(dai));

        // 1.008 - Originating Agency ID
        String ori = "WVUPUNT37";
        record.setField(8, createField(ori));

        // 1.009 - Transaction Control Number
        String tcn = "FAKE_TCN_FOR_TESTING_PURPOSES";
        record.setField(9, createField(tcn));

        // 1.010 - Transaction Control Reference
        String tcr = "NO_IDEA_WHAT_THIS_VALUE_SHOULD_BE";
        record.setField(10, createField(tcr));

        // 1.011 - Native Scanning Resolution
        String nsr = "00.00";
        record.setField(11, createField(nsr));

        // 1.012 - Nominal Resolution
        String ntr = "00.00";
        record.setField(12, createField(ntr));

        // 1.013 - Domain Name
        String dnm = "NORAM";
        String dvn = "EBTS 10.0";
        record.setField(13, createField(dnm, dvn));

        // 1.016 - Application Profile Specifications
        String apo = "APO";
        String apn = "APN";
        String apv = "1.0";
        record.setField(16, createField(apo, apn, apv));

        ebts.addRecord(record);
        List<LogicalRecord> recordList = ebts.getRecordsByType(1);

        if(recordList.size() > 0){

            LogicalRecord temp = recordList.get(0);

            Type1 t1 = Type1.buildFromEbtsRecord(temp);

            Assertions.assertEquals(ver, t1.getVersion());
            Assertions.assertEquals(tot, t1.getTypeOfTransaction());
            Assertions.assertEquals(Integer.parseInt(frc), t1.getFileContent().getFirstRecordCategoryCode());
            Assertions.assertEquals(crc, t1.getFileContent().getContentRecordCount());
            Assertions.assertEquals(rec, t1.getFileContent().getRecordCategoryCodes().get(0));
            Assertions.assertEquals(idc, t1.getFileContent().getInformationDesignationCharacters().get(0));
            Assertions.assertEquals(dat, t1.getDate());
            Assertions.assertEquals(pry, t1.getTransactionPriority());
            Assertions.assertEquals(dai, t1.getDestinationAgencyIdentifier());
            Assertions.assertEquals(ori, t1.getOriginatingAgencyIdentifier());
            Assertions.assertEquals(tcn, t1.getTransactionControlNumber());
            Assertions.assertEquals(tcr, t1.getTransactionControlReference());
            Assertions.assertEquals(nsr, t1.getNativeScanningResolution());
            Assertions.assertEquals(ntr, t1.getNominalResolution());
            Assertions.assertEquals(dnm, t1.getDomainName().getDomainName());
            Assertions.assertEquals(dvn, t1.getDomainName().getDomainVersionNumber());
            Assertions.assertEquals(apo, t1.getApplicationProfileSpecification().getApplicationProfileOrganization());
            Assertions.assertEquals(apn, t1.getApplicationProfileSpecification().getApplicationProfileName());
            Assertions.assertEquals(apv, t1.getApplicationProfileSpecification().getApplicationProfileVersionNumber());

            /*
             ******************************
             *
             *
             * Begin test conversion From Type 1
             * to Logical Record
             *
             *
             *
             ******************************/

            LogicalRecord comparisonRecord = Type1.buildEbtsLogicalRecord(t1);

            Field tempField = comparisonRecord.getField(3);
            Assertions.assertEquals(2, tempField.getOccurrences().size());
            Occurrence tempOcc = tempField.getOccurrences().get(0);
            Assertions.assertEquals(2, tempOcc.getSubFields().size());
            Assertions.assertEquals(Integer.toString(t1.getFileContent().getFirstRecordCategoryCode()), tempOcc.getSubFields().get(0).toString());
            Assertions.assertEquals(t1.getFileContent().getContentRecordCount(), tempOcc.getSubFields().get(1).toString());
            tempOcc = tempField.getOccurrences().get(1);
            Assertions.assertEquals(2, tempOcc.getSubFields().size());
            Assertions.assertEquals(t1.getFileContent().getRecordCategoryCodes().get(0), tempOcc.getSubFields().get(0).toString());
            Assertions.assertEquals(t1.getFileContent().getInformationDesignationCharacters().get(0), tempOcc.getSubFields().get(1).toString());

            Assertions.assertEquals(t1.getTypeOfTransaction(), comparisonRecord.getField(4).toString());
            Assertions.assertEquals(t1.getDate(), comparisonRecord.getField(5).toString());
            Assertions.assertEquals(t1.getTransactionPriority(), comparisonRecord.getField(6).toString());
            Assertions.assertEquals(t1.getDestinationAgencyIdentifier(), comparisonRecord.getField(7).toString());
            Assertions.assertEquals(t1.getOriginatingAgencyIdentifier(), comparisonRecord.getField(8).toString());
            Assertions.assertEquals(t1.getTransactionControlNumber(), comparisonRecord.getField(9).toString());
            Assertions.assertEquals(t1.getTransactionControlReference(), comparisonRecord.getField(10).toString());

            tempField = comparisonRecord.getField(13);
            Assertions.assertEquals(1, tempField.getOccurrences().size());
            tempOcc = tempField.getOccurrences().get(0);
            Assertions.assertEquals(2, tempOcc.getSubFields().size());
            Assertions.assertEquals(t1.getDomainName().getDomainName(), tempOcc.getSubFields().get(0).toString());
            Assertions.assertEquals(t1.getDomainName().getDomainVersionNumber(), tempOcc.getSubFields().get(1).toString());

            tempField = comparisonRecord.getField(16);
            Assertions.assertEquals(1, tempField.getOccurrences().size());
            tempOcc = tempField.getOccurrences().get(0);
            Assertions.assertEquals(3, tempOcc.getSubFields().size());
            Assertions.assertEquals(t1.getApplicationProfileSpecification().getApplicationProfileOrganization(), tempOcc.getSubFields().get(0).toString());
            Assertions.assertEquals(t1.getApplicationProfileSpecification().getApplicationProfileName(), tempOcc.getSubFields().get(1).toString());
            Assertions.assertEquals(t1.getApplicationProfileSpecification().getApplicationProfileVersionNumber(), tempOcc.getSubFields().get(2).toString());
        }

    }

    private Field createField(String... dataArgs){
        Field field = new Field();
        Occurrence occurence = new Occurrence();
        List<SubField> subList = new ArrayList<>();
        List<Occurrence> occList = new ArrayList<>();

        for(String data : dataArgs) {
            SubField subField = new SubField();
            subField.setData(data);
            subList.add(subField);
        }

        occurence.setSubfields(subList);
        occList.add(occurence);

        field.setOccurrences(occList);

        return field;
    }

    private Field createFieldFromOccurrences(List<Occurrence> occurenceList) {
        Field field = new Field();
        field.setOccurrences(occurenceList);
        return field;
    }

    private Field createFieldWithMultipleOccurences(String... occurenceArgs){
        Field field = new Field();
        List<Occurrence> occList = new ArrayList<>();

        for(String data : occurenceArgs) {
            Occurrence occurence = new Occurrence();
            SubField subField = new SubField();
            subField.setData(data);

            List<SubField> subList = new ArrayList<>();
            subList.add(subField);
            occurence.setSubfields(subList);
            occList.add(occurence);
        }

        field.setOccurrences(occList);
        return field;
    }

    private Occurrence createOccurence(String... occurrenceArgs){
        Occurrence occurrence = new Occurrence();
        List<SubField> subList = new ArrayList<>();

        for(String data : occurrenceArgs){
            SubField sf = new SubField();
            sf.setData(data);
            subList.add(sf);
        }
        occurrence.setSubfields(subList);
        return occurrence;
    }
}
