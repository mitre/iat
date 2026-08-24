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
import org.mitre.jet.ebts.field.SubField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CandidateInvesitgativeListField {

    private static final Logger log = LoggerFactory.getLogger(CandidateInvesitgativeListField.class);

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String subjectIdentifier;
    public String getSubjectIdentifier() {
        return this.subjectIdentifier;
    }

    private String masterName;
    public String getMasterName() {
        return this.masterName;
    }

    private String biometricSetIdentifier;
    public String getBiometricSetIdentifier() {
        return this.biometricSetIdentifier;
    }

    private int imageType;
    public int getImageType() {
        return this.imageType;
    }

    private int frictionRidgeGeneralizedPosition;
    public int getFrictionRidgeGeneralizedPosition() {
        return this.frictionRidgeGeneralizedPosition;
    }

    private String printPositionDescriptor;
    public String getPrintPositionDescriptor() {
        return this.printPositionDescriptor;
    }

    private int matchScore;
    public int getMatchScore() {
        return this.matchScore;
    }

    private int biometricImageAvailable;
    public int getBiometricImageAvailable() {
        return this.biometricImageAvailable;
    }

    private int nameOfDesignatedRepository;
    public int getNameOfDesignatedRepository() {
        return this.nameOfDesignatedRepository;
    }

    private String informationDesignationCharacter;
    public String getInformationDesignationCharacter() {
        return this.informationDesignationCharacter;
    }

    private String noteField;
    public String getNoteField() {
        return this.noteField;
    }

    private String subjectPose;
    public String getSubjectPose() {
        return this.subjectPose;
    }

    private String ncicSmtCode;
    public String getNcicSmtCode() {
        return ncicSmtCode;
    }

    private int eyeLabel;
    public int getEyeLabel() {
        return this.eyeLabel;
    }

    public CandidateInvesitgativeListField() {
        // Empty constructor to satisfy JPA requirement
    }

    public String toString() {
        return String.format("%s#<id='%s'>",
                this.getClass().getSimpleName(),
                id);
    }

    public static CandidateInvesitgativeListField buildFromEbtsOccurence(Occurrence occurrence) {
        CandidateInvesitgativeListField cilf = new CandidateInvesitgativeListField();

        if (occurrence == null) {
            log.debug("CNL occurrence is null.");
        } else if (occurrence.getSubFields().size() != 14) {
            log.debug("CNL field contains an unexpected number of subfields ({}) and will not be parsed.", occurrence.getSubFields().size());
        } else {
            cilf.subjectIdentifier = EbtsConversionHelper.getStringFromOccurrence(occurrence, 0);
            cilf.masterName = EbtsConversionHelper.getStringFromOccurrence(occurrence, 1);
            cilf.biometricSetIdentifier = EbtsConversionHelper.getStringFromOccurrence(occurrence, 2);
            cilf.imageType = EbtsConversionHelper.getIntFromOccurrence(occurrence, 3, Integer.MAX_VALUE);
            cilf.frictionRidgeGeneralizedPosition = EbtsConversionHelper.getIntFromOccurrence(occurrence, 4, Integer.MAX_VALUE);
            cilf.printPositionDescriptor = EbtsConversionHelper.getStringFromOccurrence(occurrence, 5);
            cilf.matchScore = EbtsConversionHelper.getIntFromOccurrence(occurrence, 6, Integer.MAX_VALUE);
            cilf.biometricImageAvailable = EbtsConversionHelper.getIntFromOccurrence(occurrence, 7, Integer.MAX_VALUE);
            cilf.nameOfDesignatedRepository = EbtsConversionHelper.getIntFromOccurrence(occurrence, 8, Integer.MAX_VALUE);
            cilf.informationDesignationCharacter = EbtsConversionHelper.getStringFromOccurrence(occurrence, 9);
            cilf.noteField = EbtsConversionHelper.getStringFromOccurrence(occurrence, 10);
            cilf.subjectPose = EbtsConversionHelper.getStringFromOccurrence(occurrence, 11);
            cilf.ncicSmtCode = EbtsConversionHelper.getStringFromOccurrence(occurrence, 12);
            cilf.eyeLabel = EbtsConversionHelper.getIntFromOccurrence(occurrence, 13, Integer.MAX_VALUE);
        }

        return cilf;
    }

    public static Occurrence buildEbtsOccurrence(CandidateInvesitgativeListField cil) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subFields = new ArrayList<>();
        subFields.add(EbtsConversionHelper.getSubFieldFromString(cil.getSubjectIdentifier()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(cil.getMasterName()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(cil.getBiometricSetIdentifier()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(cil.getImageType()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(cil.getFrictionRidgeGeneralizedPosition()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(cil.getPrintPositionDescriptor()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(cil.getMatchScore()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(cil.getBiometricImageAvailable()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(cil.getNameOfDesignatedRepository()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(cil.getInformationDesignationCharacter()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(cil.getNoteField()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(cil.getSubjectPose()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(cil.getNcicSmtCode()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(cil.getEyeLabel()));
        occurrence.setSubfields(subFields);

        return occurrence;
    }
}