/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.model;

import org.apache.commons.lang3.StringUtils;
import jakarta.persistence.*;
import java.util.*;

@Entity
public class EbtsMessageRequest {
    public EbtsMessageRequest(){

    }

    public EbtsMessageRequest(Long id, Map<String, String> mapData, ImageData imageData, String cinPrefix, String cinIdentifier, int numberOfCandidates, int caseExtension, String dateOfSubmission, String destinationAgencyIdentifier, String originatingAgencyIdentifier, String transactionControlNumber, String attentionIndicator, String sourceAgency, String irisCaptureDate, int rotationOfEye, int rotationUncertainty, List<String> errorMessages, List<String> invalidFields) {
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

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public boolean isValid() {
        invalidFields.clear();
        checkField(dateOfSubmission, "DAT");
        checkField(destinationAgencyIdentifier, "DAI");
        checkField(originatingAgencyIdentifier, "ORI");
        checkField(transactionControlNumber, "TCN");
        checkField(attentionIndicator, "ATN");
        checkField(cinPrefix, "CIN_PRE");
        checkField(cinIdentifier, "CIN_ID");
        if(numberOfCandidates < 20 || numberOfCandidates > 50){
            invalidFields.add("NCR");
        }
        checkField(sourceAgency, "SRC");
        checkField(irisCaptureDate, "ICD");

        if(this.imageData != null){
            checkField(imageData.getImageData(), "DATA");

            if(null == this.imageData.getEyeLabel())
                invalidFields.add("ELR");
        }
        return invalidFields.isEmpty();
    }

    public String getErrors() {
        if(invalidFields.isEmpty())
            return "";

        return String.format("Missing mandatory fields '%s'", String.join("', '", invalidFields));
    }

    public void checkField(String value, String mnemonic){
        if(StringUtils.isEmpty(value)){
            invalidFields.add(mnemonic);
        }
    }

    public void checkField(byte[] value, String mnemonic){
        if(value.length == 0){
            invalidFields.add(mnemonic);
        }
    }

    @ElementCollection
    private Map<String, String> mapData = new HashMap<>();
    public Map<String, String> getMapData() { return mapData; }
    public void setMapData(HashMap<String, String> mapData) { this.mapData = mapData; }

    @OneToOne(cascade = CascadeType.ALL)
    private ImageData imageData;
    public ImageData getImageData() { return this.imageData; }
    public void setImageData(ImageData imageData) {
        this.imageData = imageData;
    }

    private String cinPrefix = ""; // 2.010a - cin_pre
    public String getCinPrefix() { return cinPrefix; }
    public void setCinPrefix(String cinPrefix) { this.cinPrefix = cinPrefix; }

    private String cinIdentifier = ""; // 2.010b - cin_id
    public String getCinIdentifier() { return cinIdentifier; }
    public void setCinIdentifier(String cinIdentifier) { this.cinIdentifier = cinIdentifier; }

    private int numberOfCandidates; // 2.079 - ncr
    public int getNumberOfCandidates() { return numberOfCandidates; }
    public void setNumberOfCandidates(int numberOfCandidates) { this.numberOfCandidates = numberOfCandidates; }

    private int caseExtension; //2.011 - cix
    public int getCaseExtension() { return caseExtension; }
    public void setCaseExtension(int caseExtension) { this.caseExtension = caseExtension; }

    private String dateOfSubmission = ""; // 1.005 - dat
    public String getDateOfSubmission() { return dateOfSubmission; }
    public void setDateOfSubmission(String dateOfSubmission) { this.dateOfSubmission = dateOfSubmission; }

    private String destinationAgencyIdentifier = ""; // 1.007 - dai
    public String getDestinationAgencyIdentifier() { return destinationAgencyIdentifier; }
    public void setDestinationAgencyIdentifier(String destinationAgencyIdentifier) { this.destinationAgencyIdentifier = destinationAgencyIdentifier; }

    private String originatingAgencyIdentifier; // 1.008 - ori
    public String getOriginatingAgencyIdentifier() { return originatingAgencyIdentifier; }
    public void setOriginatingAgencyIdentifier(String originatingAgencyIdentifier) { this.originatingAgencyIdentifier = originatingAgencyIdentifier; }

    private String transactionControlNumber; // 1.009 - tcn
    public String getTransactionControlNumber() { return transactionControlNumber; }
    public void setTransactionControlNumber(String transactionControlNumber) { this.transactionControlNumber = transactionControlNumber; }

    private String attentionIndicator; // 2.006 - atn
    public String getAttentionIndicator() { return attentionIndicator; }
    public void setAttentionIndicator(String attentionIndicator) { this.attentionIndicator = attentionIndicator; }

    private String sourceAgency; // 17.004 - src
    public String getSourceAgency() { return sourceAgency; }
    public void setSourceAgency(String sourceAgency) { this.sourceAgency = sourceAgency; }

    private String irisCaptureDate; // 17.005 - icd
    public String getIrisCaptureDate() { return irisCaptureDate; }
    public void setIrisCaptureDate(String irisCaptureDate) { this.irisCaptureDate = irisCaptureDate; }

    private int rotationOfEye; //17.014 - rae
    public int getRotationOfEye() { return rotationOfEye; }
    public void setRotationOfEye(int rotationOfEye) { this.rotationOfEye = rotationOfEye; }

    private int rotationUncertainty; // 17.015 - rau
    public int getRotationUncertainty() { return rotationUncertainty; }
    public void setRotationUncertainty(int rotationUncertainty) { this.rotationUncertainty = rotationUncertainty; }

    @ElementCollection
    private List<String> errorMessages = new ArrayList<>();
    public List<String> getErrorMessages() { return errorMessages; }
    public void addErrorMessages(String errorMessage) { this.errorMessages.add(errorMessage); }

    @ElementCollection
    private List<String> invalidFields = new ArrayList<>();
    public String getInvalidFieldsString() { return String.join(", ", invalidFields); }
    public List<String> getInvalidFields() { return this.invalidFields; }

    private int tempEbtsImageId = -1;
    public int getTempEbtsImageId() {
        return this.tempEbtsImageId;
    }
    public void setTempEbtsImageId(int imageDataId){
        this.tempEbtsImageId = imageDataId;
    }
}
