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
public class ContributorCaseIdentifierNumberField {

    private static final Logger log = LoggerFactory.getLogger(ContributorCaseIdentifierNumberField.class);

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String contributorCasePrefix;
    public String getContributorCasePrefix() {
        return this.contributorCasePrefix;
    }
    public void setContributorCasePrefix(String contributorCasePrefix) {
        this.contributorCasePrefix = contributorCasePrefix;
    }

    private String contributorCaseId;
    public String getContributorCaseId() {
        return this.contributorCaseId;
    }
    public void setContributorCaseId(String contributorCaseId) {
        this.contributorCaseId = contributorCaseId;
    }

    public ContributorCaseIdentifierNumberField() {
        // Empty constructor to satisfy JPA requirement
    }

    public String toString() {
        return String.format("%s#<id='%s', cin_pre='%s', cin_id='%s'>",
                this.getClass().getSimpleName(), id, contributorCasePrefix, contributorCaseId);
    }

    public static ContributorCaseIdentifierNumberField buildFromEbtsOccurence(Occurrence occurrence) {
        ContributorCaseIdentifierNumberField ccinf = new ContributorCaseIdentifierNumberField();

        if (occurrence == null) {
            log.debug("CIN occurrence is null.");
        } else if (occurrence.getSubFields().size() != 2) {
            log.debug("CIN field contains an unexpected number of subfields ({}) and will not be parsed.", occurrence.getSubFields().size());
        } else {
            ccinf.contributorCasePrefix = EbtsConversionHelper.getStringFromOccurrence(occurrence, 0);
            ccinf.contributorCaseId = EbtsConversionHelper.getStringFromOccurrence(occurrence, 1);
        }

        return ccinf;
    }

    public static Occurrence buildEbtsOccurrence(ContributorCaseIdentifierNumberField cin) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subFields = new ArrayList<>();
        subFields.add(EbtsConversionHelper.getSubFieldFromString(cin.getContributorCasePrefix()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(cin.getContributorCaseId()));
        occurrence.setSubfields(subFields);

        return occurrence;
    }
}