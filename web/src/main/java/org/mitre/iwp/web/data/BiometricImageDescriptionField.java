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
public class BiometricImageDescriptionField {
    private static final Logger log = LoggerFactory.getLogger(BiometricImageDescriptionField.class);

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String subjectIdentifier;
    public String getSubjectIdentifier() { return subjectIdentifier; }

    private int imageType;
    public int getImageType() { return imageType; }

    private String biometricSetIdentifier;
    public String getBiometricSetIdentifier() { return biometricSetIdentifier; }

    private int fingerNumberRequested;
    public int getFingerNumberRequested() { return fingerNumberRequested; }

    private String printPositionDescriptors;
    public String getPrintPositionDescriptors() { return printPositionDescriptors; }

    private String subjectPose;
    public String getSubjectPose() { return subjectPose; }

    private String ncicSmtCode;
    public String getNcicSmtCode() { return ncicSmtCode; }

    private int eyeLabel;
    public int getEyeLabel() { return eyeLabel; }

    public BiometricImageDescriptionField() {
        // Empty constructor to satisfy JPA requirement 
    }

    public String toString() {
        return String.format("%s#<id='%s'>",
                this.getClass().getSimpleName(),
                id);
    }

    public static BiometricImageDescriptionField buildFromEbtsOccurence(Occurrence occurrence){
        BiometricImageDescriptionField bidf = new BiometricImageDescriptionField();

        if (occurrence == null) {
            log.debug("BID occurrence is null.");
        } else if (occurrence.getSubFields().size() != 8) {
            log.debug("BID field contains an unexpected number of subfields ({}) and will not be parsed.", occurrence.getSubFields().size());
        } else {
            bidf.subjectIdentifier = EbtsConversionHelper.getStringFromOccurrence(occurrence, 0);
            bidf.imageType = EbtsConversionHelper.getIntFromOccurrence(occurrence, 1, Integer.MAX_VALUE);
            bidf.biometricSetIdentifier = EbtsConversionHelper.getStringFromOccurrence(occurrence, 2);
            bidf.fingerNumberRequested = EbtsConversionHelper.getIntFromOccurrence(occurrence, 3, Integer.MAX_VALUE);
            bidf.printPositionDescriptors = EbtsConversionHelper.getStringFromOccurrence(occurrence, 4);
            bidf.subjectPose = EbtsConversionHelper.getStringFromOccurrence(occurrence, 5);
            bidf.ncicSmtCode = EbtsConversionHelper.getStringFromOccurrence(occurrence, 6);
            bidf.eyeLabel = EbtsConversionHelper.getIntFromOccurrence(occurrence, 7, Integer.MAX_VALUE);
        }

        return bidf;
    }

    public static Occurrence buildEbtsOccurrence(BiometricImageDescriptionField bid) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subFields = new ArrayList<>();
        subFields.add(EbtsConversionHelper.getSubFieldFromString(bid.getSubjectIdentifier()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(bid.getImageType()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(bid.getBiometricSetIdentifier()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(bid.getFingerNumberRequested()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(bid.getPrintPositionDescriptors()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(bid.getSubjectPose()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(bid.getNcicSmtCode()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(bid.getEyeLabel()));
        occurrence.setSubfields(subFields);
        return occurrence;
    }
}
