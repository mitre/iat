/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EbtsMessageRequestDTO {
    public EbtsMessageRequestDTO(){}

    public EbtsMessageRequestDTO(Long id, Map<String, String> mapData, ImageDataDTO imageData, String cinPrefix, String cinIdentifier, int numberOfCandidates, int caseExtension, String dateOfSubmission, String destinationAgencyIdentifier, String originatingAgencyIdentifier, String transactionControlNumber, String attentionIndicator, String sourceAgency, String irisCaptureDate, int rotationOfEye, int rotationUncertainty, List<String> errorMessages, List<String> invalidFields) {
        this.id = id;
        this.mapData = mapData;
        this.imageData = imageData;
        this.cinPrefix = cinPrefix;
        this.cinIdentifier = cinIdentifier;
        this.numberOfCandidates = numberOfCandidates;
        this.caseExtension = caseExtension;
        this.dateOfSubmission = dateOfSubmission;
        this.destinationAgencyIdentifier = destinationAgencyIdentifier;
        this.originatingAgencyIdentifier = originatingAgencyIdentifier;
        this.transactionControlNumber = transactionControlNumber;
        this.attentionIndicator = attentionIndicator;
        this.sourceAgency = sourceAgency;
        this.irisCaptureDate = irisCaptureDate;
        this.rotationOfEye = rotationOfEye;
        this.rotationUncertainty = rotationUncertainty;
        this.errorMessages = errorMessages;
        this.invalidFields = invalidFields;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, String> getMapData() {
        return mapData;
    }

    public void setMapData(Map<String, String> mapData) {
        this.mapData = mapData;
    }

    public ImageDataDTO getImageData() {
        return imageData;
    }

    public void setImageData(ImageDataDTO imageData) {
        this.imageData = imageData;
    }

    public String getCinPrefix() {
        return cinPrefix;
    }

    public void setCinPrefix(String cinPrefix) {
        this.cinPrefix = cinPrefix;
    }

    public String getCinIdentifier() {
        return cinIdentifier;
    }

    public void setCinIdentifier(String cinIdentifier) {
        this.cinIdentifier = cinIdentifier;
    }

    public int getNumberOfCandidates() {
        return numberOfCandidates;
    }

    public void setNumberOfCandidates(int numberOfCandidates) {
        this.numberOfCandidates = numberOfCandidates;
    }

    public int getCaseExtension() {
        return caseExtension;
    }

    public void setCaseExtension(int caseExtension) {
        this.caseExtension = caseExtension;
    }

    public String getDateOfSubmission() {
        return dateOfSubmission;
    }

    public void setDateOfSubmission(String dateOfSubmission) {
        this.dateOfSubmission = dateOfSubmission;
    }

    public String getDestinationAgencyIdentifier() {
        return destinationAgencyIdentifier;
    }

    public void setDestinationAgencyIdentifier(String destinationAgencyIdentifier) {
        this.destinationAgencyIdentifier = destinationAgencyIdentifier;
    }

    public String getOriginatingAgencyIdentifier() {
        return originatingAgencyIdentifier;
    }

    public void setOriginatingAgencyIdentifier(String originatingAgencyIdentifier) {
        this.originatingAgencyIdentifier = originatingAgencyIdentifier;
    }

    public String getTransactionControlNumber() {
        return transactionControlNumber;
    }

    public void setTransactionControlNumber(String transactionControlNumber) {
        this.transactionControlNumber = transactionControlNumber;
    }

    public String getAttentionIndicator() {
        return attentionIndicator;
    }

    public void setAttentionIndicator(String attentionIndicator) {
        this.attentionIndicator = attentionIndicator;
    }

    public String getSourceAgency() {
        return sourceAgency;
    }

    public void setSourceAgency(String sourceAgency) {
        this.sourceAgency = sourceAgency;
    }

    public String getIrisCaptureDate() {
        return irisCaptureDate;
    }

    public void setIrisCaptureDate(String irisCaptureDate) {
        this.irisCaptureDate = irisCaptureDate;
    }

    public int getRotationOfEye() {
        return rotationOfEye;
    }

    public void setRotationOfEye(int rotationOfEye) {
        this.rotationOfEye = rotationOfEye;
    }

    public int getRotationUncertainty() {
        return rotationUncertainty;
    }

    public void setRotationUncertainty(int rotationUncertainty) {
        this.rotationUncertainty = rotationUncertainty;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public List<String> getInvalidFields() {
        return invalidFields;
    }

    public void setInvalidFields(List<String> invalidFields) {
        this.invalidFields = invalidFields;
    }

    Long id;
    Map<String, String> mapData = new HashMap<>();
    ImageDataDTO imageData = new ImageDataDTO();
    String cinPrefix = ""; // 2.010a - cin_pre
    String cinIdentifier = ""; // 2.010b - cin_id
    int numberOfCandidates; // 2.079 - ncr
    int caseExtension; //2.011 - cix
    String dateOfSubmission = ""; // 1.005 - dat
    String destinationAgencyIdentifier = ""; // 1.007 - dai
    String originatingAgencyIdentifier; // 1.008 - ori
    String transactionControlNumber; // 1.009 - tcn
    String attentionIndicator; // 2.006 - atn
    String sourceAgency; // 17.004 - src
    String irisCaptureDate; // 17.005 - icd
    int rotationOfEye; //17.014 - rae
    int rotationUncertainty; // 17.015 - rau
    List<String> errorMessages = new ArrayList<>();
    List<String> invalidFields = new ArrayList<>();

}
