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

import org.mitre.jet.ebts.field.Occurrence;
import org.mitre.jet.ebts.records.GenericRecord;
import org.mitre.jet.ebts.records.LogicalRecord;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Type2 {
    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private int length;
    public int getLength() {
        return this.length;
    }
    public void setLength(int length) {
        this.length = length;
    }

    private String informationDesignationCharacter;
    public String getInformationDesignationCharacter() {
        return this.informationDesignationCharacter;
    }

    private String attentionIndicator;
    public String getAttentionIndicator() {
        return this.attentionIndicator;
    }
    public void setAttentionIndicator(String attentionIndicator) {
        this.attentionIndicator = attentionIndicator;
    }

    @ElementCollection
    private List<String> sendCopyTo = new ArrayList<>();
    public List<String> getSendCopyTo() {
        return this.sendCopyTo;
    }

    private String originatingAgencyCaseNumber;
    public String getOriginatingAgencyCaseNumber() {
        return this.originatingAgencyCaseNumber;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContributorCaseIdentifierNumberField> contributorCaseIdentifierNumber = new ArrayList<>();
    public List<ContributorCaseIdentifierNumberField> getContributorCaseIdentifierNumber() {
        return contributorCaseIdentifierNumber;
    }

    @ElementCollection
    private List<Integer> contributorCaseIdExtension = new ArrayList<>();
    public List<Integer> getContributorCaseIdExtension() {
        return this.contributorCaseIdExtension;
    }

    private String universalControlNumber;
    public String getUniversalControlNumber() {
        return this.universalControlNumber;
    }

    @ElementCollection
    private List<String> stateIdentificationNumber = new ArrayList<>();
    public List<String> getStateIdentificationNumber() {
        return this.stateIdentificationNumber;
    }

    @ElementCollection
    private List<String> miscellaneousIdentificationNumber = new ArrayList<>();
    public List<String> getMiscellaneousIdentificationNumber() {
        return this.miscellaneousIdentificationNumber;
    }

    private String name;
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    private String placeOfBirth;
    public String getPlaceOfBirth() { return this.placeOfBirth; }

    private String citizenship;
    public String getCitizenship() { return this.citizenship; }

    private String dateOfBirth;
    public String getDateOfBirth() { return this.dateOfBirth; }

    private int ageRange;
    public int getAgeRange() { return this.ageRange; }

    private String sex;
    public String getSex() { return this.sex; }

    private String race;
    public String getRace() { return this.race; }

    private String scarsMarksAndTattoos;
    public String getScarsMarksAndTattoos() {
        return this.scarsMarksAndTattoos;
    }

    private int heightRange;
    public int getHeightRange() {
        return this.heightRange;
    }

    private int weightRange;
    public int getWeightRange() {
        return this.weightRange;
    }

    private String eyeColor;
    public String getEyeColor() {
        return this.eyeColor;
    }

    private String hairColor;
    public String getHairColor() {
        return this.hairColor;
    }

    private String photoAvailableIndicator;
    public String getPhotoAvailableIndicator() {
        return this.photoAvailableIndicator;
    }

    private String datePrinted;
    public String getDatePrinted() {
        return this.datePrinted;
    }

    private String searchResultFindings;
    public String getSearchResultFindings() {
        return this.searchResultFindings;
    }

    @ElementCollection
    private List<String> statusErrorMessage = new ArrayList<>();
    public List<String> getStatusErrorMessage() {
        return this.statusErrorMessage;
    }

    @ElementCollection
    private List<String> imageType = new ArrayList<>();
    public List<String> getImageType() {
        return this.imageType;
    }

    @ElementCollection
    private List<String> controllingAgencyIdentifier = new ArrayList<>();
    public List<String> getControllingAgencyIdentifier() {
        return this.controllingAgencyIdentifier;
    }

    private int numberOfCandidatesReturned;
    public int getNumberOfCandidatesReturned() {
        return this.numberOfCandidatesReturned;
    }

    private String noteField;
    public String getNoteField() {
        return this.noteField;
    }

    private String requestPhotoRecord;
    public String getRequestPhotoRecord() {
        return this.requestPhotoRecord;
    }

    private int numberOfImagesRequested;
    public int getNumberOfImagesRequested() {
        return this.numberOfImagesRequested;
    }

    private String supplementaryIdentityInformation;
    public String getSupplementaryIdentityInformation() {
        return this.supplementaryIdentityInformation;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BiometricImageDescriptionField> biometricImageDescription = new ArrayList<>();
    public List<BiometricImageDescriptionField> getBiometricImageDescription() {
        return this.biometricImageDescription;
    }

    private String biometricSetIdentifier;
    public String getBiometricSetIdentifier() {
        return this.biometricSetIdentifier;
    }

    private int biometricImageAvailable;
    public int getBiometricImageAvailable() {
        return this.biometricImageAvailable;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CandidateInvesitgativeListField> candidateInvesitgativeListField = new ArrayList<>();
    public List<CandidateInvesitgativeListField> getCandidateInvesitgativeListField() {
        return this.candidateInvesitgativeListField;
    }

    private String eventIdentifier;
    public String getEventIdentifier() {
        return this.eventIdentifier;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BiometricImageEnrollmentField> biometricImageEnrollment = new ArrayList<>();
    public List<BiometricImageEnrollmentField> getBiometricImageEnrollment() {
        return this.biometricImageEnrollment;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BiometricImageListField> biometricImageList = new ArrayList<>();
    public List<BiometricImageListField> getBiometricImageList() {
        return this.biometricImageList;
    }

    public Type2() {
        // Empty constructor to satisfy JPA requirement
    }

    public String toString() {
        return String.format("%s#<id='%s'>", this.getClass().getSimpleName(), id);
    }

    public static Type2 buildFromEbtsRecord(LogicalRecord logicalRecord) {
        Type2 t2 = new Type2();

        t2.length = EbtsConversionHelper.getIntFromField(logicalRecord.getField(1), Integer.MAX_VALUE); // len
        t2.informationDesignationCharacter = EbtsConversionHelper.getStringFromField(logicalRecord.getField(2)); // idc
        t2.attentionIndicator = EbtsConversionHelper.getStringFromField(logicalRecord.getField(6)); // atn
        t2.originatingAgencyCaseNumber = EbtsConversionHelper.getStringFromField(logicalRecord.getField(9)); // oca
        t2.universalControlNumber = EbtsConversionHelper.getStringFromField(logicalRecord.getField(14)); // ucn
        t2.name = EbtsConversionHelper.getStringFromField(logicalRecord.getField(18)); // nam
        t2.placeOfBirth = EbtsConversionHelper.getStringFromField(logicalRecord.getField(20)); // pob
        t2.citizenship = EbtsConversionHelper.getStringFromField(logicalRecord.getField(21)); // ctz
        t2.dateOfBirth = EbtsConversionHelper.getStringFromField(logicalRecord.getField(22)); // dob
        t2.ageRange = EbtsConversionHelper.getIntFromField(logicalRecord.getField(23), Integer.MAX_VALUE); // agr
        t2.sex = EbtsConversionHelper.getStringFromField(logicalRecord.getField(24)); // sex
        t2.race = EbtsConversionHelper.getStringFromField(logicalRecord.getField(25)); // rac
        t2.scarsMarksAndTattoos = EbtsConversionHelper.getStringFromField(logicalRecord.getField(26)); // smt
        t2.heightRange = EbtsConversionHelper.getIntFromField(logicalRecord.getField(28), Integer.MAX_VALUE); // htr
        t2.weightRange = EbtsConversionHelper.getIntFromField(logicalRecord.getField(30), Integer.MAX_VALUE); // wtr
        t2.eyeColor = EbtsConversionHelper.getStringFromField(logicalRecord.getField(31)); // eye
        t2.hairColor = EbtsConversionHelper.getStringFromField(logicalRecord.getField(32)); // hai
        t2.photoAvailableIndicator = EbtsConversionHelper.getStringFromField(logicalRecord.getField(36)); // pht
        t2.datePrinted = EbtsConversionHelper.getStringFromField(logicalRecord.getField(38)); // dpr
        t2.searchResultFindings = EbtsConversionHelper.getStringFromField(logicalRecord.getField(59)); // srf
        t2.numberOfCandidatesReturned = EbtsConversionHelper.getIntFromField(logicalRecord.getField(79), Integer.MAX_VALUE); // ncr
        t2.noteField = EbtsConversionHelper.getStringFromField(logicalRecord.getField(88)); // not
        t2.requestPhotoRecord = EbtsConversionHelper.getStringFromField(logicalRecord.getField(96)); // rpr
        t2.numberOfImagesRequested = EbtsConversionHelper.getIntFromField(logicalRecord.getField(2010), Integer.MAX_VALUE); // nir
        t2.supplementaryIdentityInformation = EbtsConversionHelper.getStringFromField(logicalRecord.getField(2023)); // sii
        t2.biometricSetIdentifier = EbtsConversionHelper.getStringFromField(logicalRecord.getField(2029)); // bsi
        t2.biometricImageAvailable = EbtsConversionHelper.getIntFromField(logicalRecord.getField(2031), Integer.MAX_VALUE); // bia
        t2.eventIdentifier = EbtsConversionHelper.getStringFromField(logicalRecord.getField(2035)); // evi

        org.mitre.jet.ebts.field.Field field = logicalRecord.getField(7); // sco
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.sendCopyTo.add(EbtsConversionHelper.getStringFromOccurrence(occurrence, 0));
            }
        }

        field = logicalRecord.getField(10); // cin
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.contributorCaseIdentifierNumber.add(ContributorCaseIdentifierNumberField.buildFromEbtsOccurence(occurrence));
            }
        }

        field = logicalRecord.getField(11); // cix
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.contributorCaseIdExtension.add(EbtsConversionHelper.getIntFromOccurrence(occurrence, 0, Integer.MAX_VALUE));
            }
        }

        field = logicalRecord.getField(15); // sid
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.stateIdentificationNumber.add(EbtsConversionHelper.getStringFromOccurrence(occurrence, 0));
            }
        }

        field = logicalRecord.getField(17); // mnu
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.miscellaneousIdentificationNumber.add(EbtsConversionHelper.getStringFromOccurrence(occurrence, 0));
            }
        }

        field = logicalRecord.getField(60); // msg
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.statusErrorMessage.add(EbtsConversionHelper.getStringFromOccurrence(occurrence, 0));
            }
        }

        field = logicalRecord.getField(62); // imt
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.imageType.add(EbtsConversionHelper.getStringFromOccurrence(occurrence, 0));
            }
        }

        field = logicalRecord.getField(73); // cri
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.controllingAgencyIdentifier.add(EbtsConversionHelper.getStringFromOccurrence(occurrence, 0));
            }
        }

        field = logicalRecord.getField(2028); // bid
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.biometricImageDescription.add(BiometricImageDescriptionField.buildFromEbtsOccurence(occurrence));
            }
        }

        field = logicalRecord.getField(2033); // cnl
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.candidateInvesitgativeListField.add(CandidateInvesitgativeListField.buildFromEbtsOccurence(occurrence));
            }
        }

        field = logicalRecord.getField(2061); // bie
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.biometricImageEnrollment.add(BiometricImageEnrollmentField.buildFromEbtsOccurence(occurrence));
            }
        }

        field = logicalRecord.getField(2073); // bil
        if (field != null) {
            for (Occurrence occurrence : field.getOccurrences()) {
                t2.biometricImageList.add(BiometricImageListField.buildFromEbtsOccurence(occurrence));
            }
        }

        return t2;
    }

    public static LogicalRecord buildEbtsLogicalRecord(Type2 t2) {
        LogicalRecord logicalRecord = new GenericRecord(2);

        logicalRecord.setField(6, EbtsConversionHelper.getFieldFromString(t2.getAttentionIndicator()));
        logicalRecord.setField(9, EbtsConversionHelper.getFieldFromString(t2.getOriginatingAgencyCaseNumber()));
        logicalRecord.setField(14, EbtsConversionHelper.getFieldFromString(t2.getUniversalControlNumber()));
        logicalRecord.setField(18, EbtsConversionHelper.getFieldFromString(t2.getName()));
        logicalRecord.setField(20, EbtsConversionHelper.getFieldFromString(t2.getPlaceOfBirth()));
        logicalRecord.setField(21, EbtsConversionHelper.getFieldFromString(t2.getCitizenship()));
        logicalRecord.setField(22, EbtsConversionHelper.getFieldFromString(t2.getDateOfBirth()));
        logicalRecord.setField(23, EbtsConversionHelper.getFieldFromInt(t2.getAgeRange()));
        logicalRecord.setField(24, EbtsConversionHelper.getFieldFromString(t2.getSex()));
        logicalRecord.setField(25, EbtsConversionHelper.getFieldFromString(t2.getRace()));
        logicalRecord.setField(26, EbtsConversionHelper.getFieldFromString(t2.getScarsMarksAndTattoos()));
        logicalRecord.setField(28, EbtsConversionHelper.getFieldFromInt(t2.getHeightRange()));
        logicalRecord.setField(30, EbtsConversionHelper.getFieldFromInt(t2.getWeightRange()));
        logicalRecord.setField(31, EbtsConversionHelper.getFieldFromString(t2.getEyeColor()));
        logicalRecord.setField(32, EbtsConversionHelper.getFieldFromString(t2.getHairColor()));
        logicalRecord.setField(36, EbtsConversionHelper.getFieldFromString(t2.getPhotoAvailableIndicator()));
        logicalRecord.setField(38, EbtsConversionHelper.getFieldFromString(t2.getDatePrinted()));
        logicalRecord.setField(59, EbtsConversionHelper.getFieldFromString(t2.getSearchResultFindings()));
        logicalRecord.setField(79, EbtsConversionHelper.getFieldFromInt(t2.getNumberOfCandidatesReturned()));
        logicalRecord.setField(88, EbtsConversionHelper.getFieldFromString(t2.getNoteField()));
        logicalRecord.setField(96, EbtsConversionHelper.getFieldFromString(t2.getRequestPhotoRecord()));
        logicalRecord.setField(2010, EbtsConversionHelper.getFieldFromInt(t2.getNumberOfImagesRequested()));
        logicalRecord.setField(2023, EbtsConversionHelper.getFieldFromString(t2.getSupplementaryIdentityInformation()));
        logicalRecord.setField(2029, EbtsConversionHelper.getFieldFromString(t2.getBiometricSetIdentifier()));
        logicalRecord.setField(2031, EbtsConversionHelper.getFieldFromInt(t2.getBiometricImageAvailable()));
        logicalRecord.setField(2035, EbtsConversionHelper.getFieldFromString(t2.getEventIdentifier()));

        // Create 'Send Copy To' Occurrences - 1 Occurrence per String
        org.mitre.jet.ebts.field.Field tempField = EbtsConversionHelper.getEmptyField();
        for (String sendCopy : t2.getSendCopyTo()) {
            tempField.getOccurrences().add(EbtsConversionHelper.getOccurrenceFromString(sendCopy));
        }
        logicalRecord.setField(7, tempField);

        // Create 'Contributor Class Identifier Number' Occurrences - 1 Occurrence per ContributorCaseIdentifierNumberField
        tempField = EbtsConversionHelper.getEmptyField();
        for (ContributorCaseIdentifierNumberField ccin : t2.getContributorCaseIdentifierNumber()) {
            tempField.getOccurrences().add(ContributorCaseIdentifierNumberField.buildEbtsOccurrence(ccin));
        }
        logicalRecord.setField(10, tempField);

        // Create 'Contributor Case ID Extension' Occurrences - 1 Occurrence per Integer
        tempField = EbtsConversionHelper.getEmptyField();
        for (Integer cix : t2.getContributorCaseIdExtension()) {
            tempField.getOccurrences().add(EbtsConversionHelper.getOccurrenceFromInt(cix));
        }
        logicalRecord.setField(11, tempField);

        // Create 'State Identificatino Number' Occurrences - 1 Occurrence per String
        tempField = EbtsConversionHelper.getEmptyField();
        for (String sid : t2.getStateIdentificationNumber()) {
            tempField.getOccurrences().add(EbtsConversionHelper.getOccurrenceFromString(sid));
        }
        logicalRecord.setField(15, tempField);

        // Create 'Miscellaneous Identification Number' Occurrences - 1 Occurrence per String
        tempField = EbtsConversionHelper.getEmptyField();
        for (String mnu : t2.getMiscellaneousIdentificationNumber()) {
            tempField.getOccurrences().add(EbtsConversionHelper.getOccurrenceFromString(mnu));
        }
        logicalRecord.setField(17, tempField);

        // Create 'Status Error Message' Occurrences- 1 Occurrence per String
        tempField = EbtsConversionHelper.getEmptyField();
        for (String msg : t2.getStatusErrorMessage()) {
            tempField.getOccurrences().add(EbtsConversionHelper.getOccurrenceFromString(msg));
        }
        logicalRecord.setField(60, tempField);

        // Create 'Image Type' Occurrences - 1 Occurrence per String
        tempField = EbtsConversionHelper.getEmptyField();
        for (String imt : t2.getImageType()) {
            tempField.getOccurrences().add(EbtsConversionHelper.getOccurrenceFromString(imt));
        }
        logicalRecord.setField(62, tempField);

        // Create 'Controlling Agency Identifier' Occurrences - 1 Occurrence per String
        tempField = EbtsConversionHelper.getEmptyField();
        for (String cri : t2.getControllingAgencyIdentifier()) {
            tempField.getOccurrences().add(EbtsConversionHelper.getOccurrenceFromString(cri));
        }
        logicalRecord.setField(73, tempField);

        // Create 'Biometric Image Description' Occurrences - 1 Occurrence per BiometricImageDescriptionField object
        tempField = EbtsConversionHelper.getEmptyField();
        for (BiometricImageDescriptionField bid : t2.getBiometricImageDescription()) {
            tempField.getOccurrences().add(BiometricImageDescriptionField.buildEbtsOccurrence(bid));
        }
        logicalRecord.setField(2028, tempField);

        // Create 'Candidate Investigative List' Occurrences - 1 Occurrence per CandidateInvesitgativeListField object
        tempField = EbtsConversionHelper.getEmptyField();
        for (CandidateInvesitgativeListField cil : t2.getCandidateInvesitgativeListField()) {
            tempField.getOccurrences().add(CandidateInvesitgativeListField.buildEbtsOccurrence(cil));
        }
        logicalRecord.setField(2033, tempField);

        // Create 'Biometric Image Enrollment' Occurrences - 1 Occurrence per BiometricImageEnrollmentField object
        tempField = EbtsConversionHelper.getEmptyField();
        for (BiometricImageEnrollmentField bie : t2.getBiometricImageEnrollment()) {
            tempField.getOccurrences().add(BiometricImageEnrollmentField.buildEbtsOccurrence(bie));
        }
        logicalRecord.setField(2061, tempField);

        // Create 'Biometric Image List' Occurrences - 1 Occurrence per BiometricImageListField object
        tempField = EbtsConversionHelper.getEmptyField();
        for (BiometricImageListField bil : t2.getBiometricImageList()) {
            tempField.getOccurrences().add(BiometricImageListField.buildEbtsOccurrence(bil));
        }
        logicalRecord.setField(2073, tempField);

        return logicalRecord;
    }
}
