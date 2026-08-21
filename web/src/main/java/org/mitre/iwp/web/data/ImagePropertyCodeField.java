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
public class ImagePropertyCodeField {
    private static final Logger log = LoggerFactory.getLogger(ImagePropertyCodeField.class);

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private int horizontalOrientationCode;
    public int getHorizontalOrientationCode() { return this.horizontalOrientationCode; }

    private int verticalOrientationCode;
    public int getVerticalOrientationCode() { return this.verticalOrientationCode; }

    private int specificScanType;
    public int getSpecificScanType() { return this.specificScanType; }

    public ImagePropertyCodeField() {
        // Empty constructor to satisfy JPA requirement
    }

    public String toString() {
        return String.format("%s#<id='%s', iho='%d', ivo='%d', ist='%d'>",
                this.getClass().getSimpleName(), id, horizontalOrientationCode, verticalOrientationCode, specificScanType);
    }

    public static ImagePropertyCodeField buildFromEbtsField(org.mitre.jet.ebts.field.Field field) {
        ImagePropertyCodeField ipcf = new ImagePropertyCodeField();

        if (field == null) {
            log.debug("IPC field not provided");
        } else if (field.getOccurrences().size() != 1) {
            log.debug("IPC field contains an unexpected number of occurrences ({}) and will not be parsed.", field.getOccurrences().size());
        } else {
            Occurrence occurrence = field.getOccurrences().get(0);

            if (occurrence.getSubFields().size() != 3) {
                log.debug("IPC field contains an unexpected number of subfields ({}) and will not be parsed.", occurrence.getSubFields().size());
            } else {
                ipcf.horizontalOrientationCode = EbtsConversionHelper.getIntFromOccurrence(occurrence, 0, Integer.MAX_VALUE);
                ipcf.verticalOrientationCode = EbtsConversionHelper.getIntFromOccurrence(occurrence, 1, Integer.MAX_VALUE);
                ipcf.specificScanType = EbtsConversionHelper.getIntFromOccurrence(occurrence, 2, Integer.MAX_VALUE);
            }
        }

        return ipcf;
    }

    public static Occurrence buildEbtsOccurrence(ImagePropertyCodeField ipc) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subFields = new ArrayList<>();
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(ipc.getHorizontalOrientationCode()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(ipc.getVerticalOrientationCode()));
        subFields.add(EbtsConversionHelper.getSubFieldFromInt(ipc.getSpecificScanType()));
        occurrence.setSubfields(subFields);

        return occurrence;
    }
}
