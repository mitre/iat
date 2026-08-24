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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BiometricImageListField {

    private static final Logger log = LoggerFactory.getLogger(BiometricImageListField.class);

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String subjectIdentifier;
    public String getSubjectIdentifier() { return this.subjectIdentifier; }

    private String biometricSetIdentifier;
    public String getBiometricSetIdentifier() { return this.biometricSetIdentifier; }

    private Timestamp biometricCaptureDate;
    public Timestamp getBiometricCaptureDate() { return this.biometricCaptureDate; }

    private int imageType;
    public int getImageType() { return this.imageType; }

    public BiometricImageListField() {
        // Empty constructor to satisfy JPA requirement
    }

    public String toString() {
        return String.format("%s#<id='%s', si='%s', bsi='%s', bcd='%s', imt='%d'>",
                this.getClass().getSimpleName(), id, subjectIdentifier, biometricSetIdentifier, biometricCaptureDate, imageType);
    }

    public static BiometricImageListField buildFromEbtsOccurence(Occurrence occurrence) {
        BiometricImageListField bilf = new BiometricImageListField();

        if (occurrence == null) {
            log.debug("BIL occurrence is null.");
        } else if (occurrence.getSubFields().size() != 4) {
            log.debug("BIL field contains an unexpected number of subfields ({}) and will not be parsed.", occurrence.getSubFields().size());

        } else {
            bilf.subjectIdentifier = EbtsConversionHelper.getStringFromOccurrence(occurrence, 0);
            bilf.biometricSetIdentifier = EbtsConversionHelper.getStringFromOccurrence(occurrence, 1);
            try {
                bilf.biometricCaptureDate = Timestamp.valueOf(EbtsConversionHelper.getStringFromOccurrence(occurrence, 2));
            } catch(Exception e) { //this generic but you can control another types of exception
                log.error("Error parsing Biometric Capture Date '{}' to Timestamp", EbtsConversionHelper.getStringFromOccurrence(occurrence, 2));
            }

            bilf.imageType = EbtsConversionHelper.getIntFromOccurrence(occurrence, 3, Integer.MAX_VALUE);
        }

        return bilf;
    }

    public static Occurrence buildEbtsOccurrence(BiometricImageListField bil) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subFields = new ArrayList<>();
        subFields.add(EbtsConversionHelper.getSubFieldFromString(bil.getSubjectIdentifier()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(bil.getBiometricSetIdentifier()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(bil.getBiometricCaptureDate().toString()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(bil.getImageType()));
        occurrence.setSubfields(subFields);

        return occurrence;
    }
}
