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
public class MakeModelSerialNumberField {
    private static final Logger log = LoggerFactory.getLogger(MakeModelSerialNumberField.class);

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String make;
    public String getMake() { return make; }

    private String model;
    public String getModel() { return model; }

    private String serialNumber;
    public String getSerialNumber() { return serialNumber; }

    public MakeModelSerialNumberField() {
        // Empty constructor to satisfy JPA requirement
    }

    public String toString() {
        return String.format("%s#<id='%s', mak='%s', mod='%s', ser='%s'>",
                this.getClass().getSimpleName(), id, make, model, serialNumber);
    }

    public static MakeModelSerialNumberField buildFromEbtsField(org.mitre.jet.ebts.field.Field field) {
        MakeModelSerialNumberField mmsnf = new MakeModelSerialNumberField();

        if (field == null) {
            log.debug("MMS field not provided");
        } else if (field.getOccurrences().size() != 1) {
            log.debug("MMS field contains an unexpected number of occurrences ({}) and will not be parsed.", field.getOccurrences().size());
        } else {
            Occurrence occurrence = field.getOccurrences().get(0);

            if (occurrence.getSubFields().size() != 3) {
                log.debug("MMS field contains an unexpected number of subfields ({}) and will not be parsed.", occurrence.getSubFields().size());
            } else {
                mmsnf.make = EbtsConversionHelper.getStringFromOccurrence(occurrence, 0);
                mmsnf.model = EbtsConversionHelper.getStringFromOccurrence(occurrence, 1);
                mmsnf.serialNumber = EbtsConversionHelper.getStringFromOccurrence(occurrence, 2);
            }
        }

        return mmsnf;
    }

    public static Occurrence buildEbtsOccurrence(MakeModelSerialNumberField mms) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subFields = new ArrayList<>();
        subFields.add(EbtsConversionHelper.getSubFieldFromString(mms.getMake()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(mms.getModel()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(mms.getSerialNumber()));
        occurrence.setSubfields(subFields);

        return occurrence;
    }
}
