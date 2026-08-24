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
public class DomainNameField {

    private static final Logger log = LoggerFactory.getLogger(DomainNameField.class);

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String domainName;
    public String getDomainName() { return this.domainName; }
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    private String domainVersionNumber;
    public String getDomainVersionNumber() { return this.domainVersionNumber; }
    public void setDomainVersionNumber(String domainVersionNumber) {
        this.domainVersionNumber = domainVersionNumber;
    }

    public DomainNameField() {
        // Empty constructor to satisfy JPA requirement
    }

    public String toString() {
        return String.format("%s#<id='%s', dnm='%s', dvn='%s'>",
                this.getClass().getSimpleName(), id, domainName, domainVersionNumber);
    }

    public static DomainNameField buildFromEbtsField(org.mitre.jet.ebts.field.Field field) {
        DomainNameField dnf = new DomainNameField();

        if (field == null) {
            log.debug("DOM field not provided");
        } else if (field.getOccurrences().size() != 1) {
            log.debug("DOM field contains an unexpected number of occurrences ({}) and will not be parsed.", field.getOccurrences().size());
        } else {
            Occurrence occurrence = field.getOccurrences().get(0);

            if (occurrence.getSubFields().size() != 2) {
                log.debug("DOM field contains an unexpected number of subfields ({}) and will not be parsed.", occurrence.getSubFields().size());
            } else {
                dnf.domainName = EbtsConversionHelper.getStringFromOccurrence(occurrence, 0);
                dnf.domainVersionNumber = EbtsConversionHelper.getStringFromOccurrence(occurrence, 1);
            }
        }

        return dnf;
    }

    public static Occurrence buildEbtsOccurrence(DomainNameField dnf) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subFields = new ArrayList<>();
        subFields.add(EbtsConversionHelper.getSubFieldFromString(dnf.getDomainName()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(dnf.getDomainVersionNumber()));
        occurrence.setSubfields(subFields);

        return occurrence;
    }
}
