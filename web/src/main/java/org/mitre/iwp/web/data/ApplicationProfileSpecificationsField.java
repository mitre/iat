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
public class ApplicationProfileSpecificationsField {

    private static final Logger log = LoggerFactory.getLogger(ApplicationProfileSpecificationsField.class);

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String applicationProfileOrganization;
    public String getApplicationProfileOrganization() { return applicationProfileOrganization; }

    public void setApplicationProfileOrganization(String applicationProfileOrganization) {
        this.applicationProfileOrganization = applicationProfileOrganization;
    }

    private String applicationProfileName;
    public String getApplicationProfileName() { return applicationProfileName; }

    public void setApplicationProfileName(String applicationProfileName) {
        this.applicationProfileName = applicationProfileName;
    }

    private String applicationProfileVersionNumber;
    public String getApplicationProfileVersionNumber() { return applicationProfileVersionNumber; }

    public void setApplicationProfileVersionNumber(String applicationProfileVersionNumber) {
        this.applicationProfileVersionNumber = applicationProfileVersionNumber;
    }

    public ApplicationProfileSpecificationsField() {
        // Empty constructor to satisfy JPA requirement
     }

    public String toString() {
        return String.format("%s#<id='%s', apo='%s', apn='%s', apv='%s'>",
                this.getClass().getSimpleName(), id, applicationProfileOrganization, applicationProfileName, applicationProfileVersionNumber);
    }

    public static ApplicationProfileSpecificationsField buildFromEbtsField(org.mitre.jet.ebts.field.Field field) {
        ApplicationProfileSpecificationsField apsf = new ApplicationProfileSpecificationsField();

        if (field == null) {
            log.debug("APS field not provided");
        } else if (field.getOccurrences().size() != 1) {
            log.debug("APS field contains an unexpected number of occurrences ({}) and will not be parsed.", field.getOccurrences().size());
        } else {
            Occurrence occurrence = field.getOccurrences().get(0);

            if (occurrence.getSubFields().size() != 3) {
                log.debug("APS field contains an unexpected number of subfields ({}) and will not be parsed.", occurrence.getSubFields().size());
            } else {
                apsf.applicationProfileOrganization = EbtsConversionHelper.getStringFromOccurrence(occurrence, 0);
                apsf.applicationProfileName = EbtsConversionHelper.getStringFromOccurrence(occurrence, 1);
                apsf.applicationProfileVersionNumber = EbtsConversionHelper.getStringFromOccurrence(occurrence, 2);
            }
        }

        return apsf;
    }

    public static Occurrence buildEbtsOccurrence(ApplicationProfileSpecificationsField apsf) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subFields = new ArrayList<>();
        subFields.add(EbtsConversionHelper.getSubFieldFromString(apsf.getApplicationProfileOrganization()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(apsf.getApplicationProfileName()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(apsf.getApplicationProfileVersionNumber()));
        occurrence.setSubfields(subFields);
        return occurrence;
    }
}
