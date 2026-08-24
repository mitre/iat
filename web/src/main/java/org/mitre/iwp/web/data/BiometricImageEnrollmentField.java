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
public class BiometricImageEnrollmentField {

    private static final Logger log = LoggerFactory.getLogger(BiometricImageEnrollmentField.class);

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String biometricSetIdentifier;
    public String getBiometricSetIdentifier() { return biometricSetIdentifier; }

    private int imageType;
    public int getImageType() { return imageType; }

    private String subjectPose;
    public String getSubjectPose() { return subjectPose; }

    private String scarsMarksTattoos;
    public String getScarsMarksTattoos() { return scarsMarksTattoos; }

    public BiometricImageEnrollmentField() {
        // Empty constructor to satisfy JPA requirement
    }

    public String toString() {
        return String.format("%s#<id='%s', bsi='%s', imt='%d', pos='%s', smt='%s'>",
                this.getClass().getSimpleName(), id, biometricSetIdentifier, imageType, subjectPose, scarsMarksTattoos);
    }

    public static BiometricImageEnrollmentField buildFromEbtsOccurence(Occurrence occurrence) {
        BiometricImageEnrollmentField bief = new BiometricImageEnrollmentField();

        if (occurrence == null) {
            log.debug("BIE occurrence is null.");
        } else if (occurrence.getSubFields().size() != 4) {
            log.debug("BIE field contains an unexpected number of subfields ({}) and will not be parsed.", occurrence.getSubFields().size());
        } else {
            bief.biometricSetIdentifier = EbtsConversionHelper.getStringFromOccurrence(occurrence, 0);
            bief.imageType = EbtsConversionHelper.getIntFromOccurrence(occurrence, 1, Integer.MAX_VALUE);
            bief.subjectPose = EbtsConversionHelper.getStringFromOccurrence(occurrence, 2);
            bief.scarsMarksTattoos = EbtsConversionHelper.getStringFromOccurrence(occurrence, 3);
        }

        return bief;
    }

    public static Occurrence buildEbtsOccurrence(BiometricImageEnrollmentField bie) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subFields = new ArrayList<>();
        subFields.add(EbtsConversionHelper.getSubFieldFromString(bie.getBiometricSetIdentifier()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(bie.getImageType()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(bie.getSubjectPose()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(bie.getScarsMarksTattoos()));
        occurrence.setSubfields(subFields);
        return occurrence;
    }
}
