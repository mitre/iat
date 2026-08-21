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

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;

@Entity
public class FileContentField {
    private static final Logger log = LoggerFactory.getLogger(FileContentField.class);

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private int firstRecordCategoryCode;
    public int getFirstRecordCategoryCode() { return this.firstRecordCategoryCode; }
    public void setFirstRecordCategoryCode(int firstRecordCategoryCode) { this.firstRecordCategoryCode = firstRecordCategoryCode; }

    private String contentRecordCount;
    public String getContentRecordCount() {
        return this.contentRecordCount;
    }
    public void setContentRecordCount(String contentRecordCount) { this.contentRecordCount = contentRecordCount; }

    @ElementCollection
    private List<String> recordCategoryCodes = new ArrayList<>();
    public List<String> getRecordCategoryCodes() {
        return this.recordCategoryCodes;
    }
    public void setRecordCategoryCodes(List<String> recordCategoryCodes) {
        this.recordCategoryCodes = recordCategoryCodes;
    }

    @ElementCollection
    private List<String> informationDesignationCharacters = new ArrayList<>();
    public List<String> getInformationDesignationCharacters() {
        return this.informationDesignationCharacters;
    }
    public void setInformationDesignationCharacters(List<String> informationDesignationCharacters) {
        this.informationDesignationCharacters = informationDesignationCharacters;
    }

    public FileContentField() {
        // Empty constructor to satisfy JPA requirement
    }

    public String toString() {
        return String.format("%s#<id='%s', frc='%d', crc='%s', rec_size='%s', idc_size='%s'>",
                this.getClass().getSimpleName(), id, firstRecordCategoryCode, contentRecordCount, recordCategoryCodes.size(), informationDesignationCharacters.size());
    }

    public static FileContentField buildFromEbtsField(org.mitre.jet.ebts.field.Field field) {
        FileContentField cnt = new FileContentField();

        if (field == null) {
            log.debug("CNT field not provided");
        } else {
            for (int i = 0; i < field.getOccurrences().size(); i++) {
                Occurrence occurrence = field.getOccurrences().get(i);

                if (occurrence.getSubFields().size() != 2) {
                    log.debug("CNT occurrence contains an unexpected number of subfields ({}) and will not be parsed", occurrence.getSubFields().size());
                } else {
                    if (i == 0) {
                        cnt.firstRecordCategoryCode = EbtsConversionHelper.getIntFromOccurrence(occurrence, 0, Integer.MAX_VALUE);
                        cnt.contentRecordCount = EbtsConversionHelper.getStringFromOccurrence(occurrence, 1);
                    } else {
                        cnt.recordCategoryCodes.add(EbtsConversionHelper.getStringFromOccurrence(occurrence, 0));
                        cnt.informationDesignationCharacters.add(EbtsConversionHelper.getStringFromOccurrence(occurrence, 1));
                    }
                }
            }
        }

        return cnt;
    }

    public static List<Occurrence> buildEbtsOccurrence(FileContentField fcf) {
        List<Occurrence> occList = new ArrayList<>();
        Occurrence occurrence = new Occurrence();

        List<SubField> subFields = new ArrayList<>();
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(fcf.getFirstRecordCategoryCode()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(fcf.getContentRecordCount()));
        occurrence.setSubfields(subFields);
        occList.add(occurrence);
        occurrence = new Occurrence();
        subFields = new ArrayList<>();
        for (int i = 0; i < fcf.getRecordCategoryCodes().size(); i++) { // getRecordCategoryCodes and getInformationDesignationCharacters 'should' always be equal?
            subFields.add(EbtsConversionHelper.getSubFieldFromString(fcf.getRecordCategoryCodes().get(i)));
            subFields.add(EbtsConversionHelper.getSubFieldFromString(fcf.getInformationDesignationCharacters().get(i)));
        }

        occurrence.setSubfields(subFields);
        occList.add(occurrence);
        return occList;
    }
}
