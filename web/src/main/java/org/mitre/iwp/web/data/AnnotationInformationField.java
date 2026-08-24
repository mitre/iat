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
public class AnnotationInformationField {

    private static final Logger log = LoggerFactory.getLogger(AnnotationInformationField.class);

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private Timestamp greenwichMeanTime;
    public Timestamp getGreenwichMeanTime() { return this.greenwichMeanTime; }

    private String processingAlgorithmNameVersion;
    public String getProcessingAlgorithmNameVersion() { return this.processingAlgorithmNameVersion; }

    private String algorithmOwner;
    public String getAlgorithmOwner() { return this.algorithmOwner; }

    private String processDescription;
    public String getProcessDescription() { return this.processDescription; }

    public AnnotationInformationField() { }
    public AnnotationInformationField(long id){
        this.id = id;
    }

    public String toString() {
        return String.format("%s#<id='%s', gmt='%s', nav='%s', own='%s', pro='%s'>",
                this.getClass().getSimpleName(), id,
                this.greenwichMeanTime,
                this.processingAlgorithmNameVersion,
                this.algorithmOwner,
                this.processDescription);
    }

    public static AnnotationInformationField buildFromEbtsOccurence(Occurrence occurrence) {
        AnnotationInformationField aif = new AnnotationInformationField();

        if (occurrence == null) {
            log.debug("BIL occurrence is null.");
        } else if (occurrence.getSubFields().size() != 4) {
            log.debug("ANN field contains an unexpected number of subfields ({}) and will not be parsed.", occurrence.getSubFields().size());
        } else {
            try {
                aif.greenwichMeanTime = Timestamp.valueOf(EbtsConversionHelper.getStringFromOccurrence(occurrence, 0));
            } catch(Exception e) { //this generic but you can control another types of exception
                log.error("Error parsing Greenwich Mean Time '{}' to Timestamp", EbtsConversionHelper.getStringFromOccurrence(occurrence, 0));
            }
            aif.processingAlgorithmNameVersion = EbtsConversionHelper.getStringFromOccurrence(occurrence, 1);
            aif.algorithmOwner = EbtsConversionHelper.getStringFromOccurrence(occurrence, 2);
            aif.processDescription = EbtsConversionHelper.getStringFromOccurrence(occurrence, 3);
        }

        return aif;
    }

    public static Occurrence buildEbtsOccurrence(AnnotationInformationField apsf) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subFields = new ArrayList<>();
        subFields.add(EbtsConversionHelper.getSubFieldFromString(apsf.getGreenwichMeanTime().toString()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(apsf.getProcessingAlgorithmNameVersion()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(apsf.getAlgorithmOwner()));
        subFields.add(EbtsConversionHelper.getSubFieldFromString(apsf.getProcessDescription()));
        occurrence.setSubfields(subFields);
        return occurrence;
    }
}
