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

import org.mitre.jet.ebts.records.GenericRecord;
import org.mitre.jet.ebts.records.LogicalRecord;

import jakarta.persistence.*;

@Entity
public class Type1 {
    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private int length;
    public int getLength() {
        return this.length;
    }

    private String version;
    public String getVersion() {
        return this.version;
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private FileContentField fileContent;
    public FileContentField getFileContent() { return this.fileContent; }

    private String typeOfTransaction;
    public String getTypeOfTransaction() { return this.typeOfTransaction; }

    private String date;
    public String getDate() { return this.date; }

    private String transactionPriority;
    public String getTransactionPriority() { return this.transactionPriority; }

    private String destinationAgencyIdentifier;
    public String getDestinationAgencyIdentifier() { return this.destinationAgencyIdentifier; }

    private String originatingAgencyIdentifier;
    public String getOriginatingAgencyIdentifier() { return this.originatingAgencyIdentifier; }

    private String transactionControlNumber;
    public String getTransactionControlNumber() { return this.transactionControlNumber; }

    private String transactionControlReference;
    public String getTransactionControlReference() { return this.transactionControlReference; }

    private String nativeScanningResolution;
    public String getNativeScanningResolution() { return this.nativeScanningResolution; }

    private String nominalResolution;
    public String getNominalResolution() { return this.nominalResolution; }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DomainNameField domainName;
    public DomainNameField getDomainName() { return this.domainName; }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private ApplicationProfileSpecificationsField applicationProfileSpecification;
    public ApplicationProfileSpecificationsField getApplicationProfileSpecification() { return this.applicationProfileSpecification; }


    public Type1() {
        // Empty constructor to satisfy JPA requirement
    }

    public String toString() {
        return String.format("%s#<id='%s'>",
                this.getClass().getSimpleName(),
                id);
    }

    public static Type1 buildFromEbtsRecord(LogicalRecord logicalRecord) {
        Type1 type1 = new Type1();
        type1.length = EbtsConversionHelper.getIntFromField(logicalRecord.getField(1), Integer.MAX_VALUE);
        type1.version = EbtsConversionHelper.getStringFromField(logicalRecord.getField(2));
        type1.fileContent = FileContentField.buildFromEbtsField(logicalRecord.getField(3));
        type1.typeOfTransaction = EbtsConversionHelper.getStringFromField(logicalRecord.getField(4));
        type1.date = EbtsConversionHelper.getStringFromField(logicalRecord.getField(5));
        type1.transactionPriority = EbtsConversionHelper.getStringFromField(logicalRecord.getField(6));
        type1.destinationAgencyIdentifier = EbtsConversionHelper.getStringFromField(logicalRecord.getField(7));
        type1.originatingAgencyIdentifier = EbtsConversionHelper.getStringFromField(logicalRecord.getField(8));
        type1.transactionControlNumber = EbtsConversionHelper.getStringFromField(logicalRecord.getField(9));
        type1.transactionControlReference = EbtsConversionHelper.getStringFromField(logicalRecord.getField(10));
        type1.nativeScanningResolution = EbtsConversionHelper.getStringFromField(logicalRecord.getField(11));
        type1.nominalResolution = EbtsConversionHelper.getStringFromField(logicalRecord.getField(12));
        type1.domainName = DomainNameField.buildFromEbtsField(logicalRecord.getField(13));
        type1.applicationProfileSpecification = ApplicationProfileSpecificationsField.buildFromEbtsField(logicalRecord.getField(16)); 
        return type1;
    }

    public static LogicalRecord buildEbtsLogicalRecord(Type1 t1) {
        LogicalRecord logicalRecord = new GenericRecord(1);

        logicalRecord.setField(4, EbtsConversionHelper.getFieldFromString(t1.getTypeOfTransaction()));
        logicalRecord.setField(5, EbtsConversionHelper.getFieldFromString(t1.getDate()));
        logicalRecord.setField(6, EbtsConversionHelper.getFieldFromString(t1.getTransactionPriority()));
        logicalRecord.setField(7, EbtsConversionHelper.getFieldFromString(t1.getDestinationAgencyIdentifier()));
        logicalRecord.setField(8, EbtsConversionHelper.getFieldFromString(t1.getOriginatingAgencyIdentifier()));
        logicalRecord.setField(9, EbtsConversionHelper.getFieldFromString(t1.getTransactionControlNumber()));
        logicalRecord.setField(10, EbtsConversionHelper.getFieldFromString(t1.getTransactionControlReference()));
        logicalRecord.setField(11, EbtsConversionHelper.getFieldFromString(t1.getNativeScanningResolution()));
        logicalRecord.setField(12, EbtsConversionHelper.getFieldFromString(t1.getNominalResolution()));

        // Create 'File Content Field' Occurrence - 1 Occurrence
        org.mitre.jet.ebts.field.Field tempField = EbtsConversionHelper.getEmptyField();
        tempField.getOccurrences().addAll(FileContentField.buildEbtsOccurrence(t1.getFileContent()));
        logicalRecord.setField(3, tempField);

        // Create 'Domain Name' Occurrence - 1 Occurrence
        tempField = EbtsConversionHelper.getEmptyField();
        tempField.getOccurrences().add(DomainNameField.buildEbtsOccurrence(t1.getDomainName()));
        logicalRecord.setField(13, tempField);

        // Create 'Application Profile Specifications' Occurrence - 1 Occurrence
        tempField = EbtsConversionHelper.getEmptyField();
        tempField.getOccurrences().add(ApplicationProfileSpecificationsField.buildEbtsOccurrence(t1.getApplicationProfileSpecification()));
        logicalRecord.setField(16, tempField);

        return logicalRecord;
    }
}
