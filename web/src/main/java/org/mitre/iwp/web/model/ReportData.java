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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ReportData {
    private static final Logger log = LoggerFactory.getLogger(ReportData.class);

    public ReportData(){

    }

    public ReportData(Long id,
                      String fileName,
                      Probe probe,
                      List<Candidate> candidates,
                      boolean includeExclude,
                      ResponseType responseType,
                      CaseInformation caseInformation,
                      List<String> results,
                      List<String> invalidFields,
                      List<String> errorMessages,
                      UserAuthentication createdBy,
                      UserAuthentication workedBy,
                      Boolean isFinished,
                      Boolean allowNullRequester) {
        this.id = id;
        this.fileName = fileName;
        this.probe = probe;
        this.candidates = candidates;
        this.includeExclude = includeExclude;
        this.responseType = responseType;
        this.caseInformation = caseInformation;
        this.results = results;
        this.invalidFields = invalidFields;
        this.errorMessages = errorMessages;
        this.createdBy = createdBy;
        this.workedBy = workedBy;
        this.isFinished = isFinished;
        this.allowNullRequester = allowNullRequester;
    }

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public boolean isValid() {
        if(probe == null) {
            invalidFields.add("Missing Probe Field");
        } else {
            if (probe.getImageList().isEmpty()) {
                invalidFields.add("Missing Probe Image Field");
            } else {

                for (ImageData data : probe.getImageList()) {
                    if (data.getId() == null) {
                        invalidFields.add("Missing Probe Image Id");
                    }
                }
            }
        }

        if(candidates == null) {
            invalidFields.add("Missing Candidate Field");
        } else {
            if (this.candidates.isEmpty()) {
                invalidFields.add("Missing Candidate List");
            }
        }

        return invalidFields.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLineString = System.getProperty("line.separator");

        result.append(this.getClass().getName());
        result.append(" Object {");
        result.append(newLineString);

        //determine fields declared in this class only (no fields of superclass)
        java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for (java.lang.reflect.Field field : fields) {
            result.append("  ");
            try {
                result.append(field.getName());
                result.append(": ");
                //requires access to private field:
                result.append(field.get(this));
            }
            catch (IllegalAccessException ex) {
                log.error(ex.getMessage(), ex);
            }
            result.append(newLineString);
        }
        result.append("}");

        return result.toString();
    }

    private String fileName;
    public String getFileName() { return this.fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Probe probe = new Probe();
    public Probe getProbe() { return this.probe; }
    public void setProbe(Probe probe) { this.probe = probe; }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Candidate> candidates = new ArrayList<>();
    public List<Candidate> getCandidates() { return this.candidates; }
    public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }

    private boolean includeExclude = false;
    public boolean getIncludeExclude() { return this.includeExclude; }
    public void setIncludeExclude(boolean includeExclude) { this.includeExclude = includeExclude; }

    @Enumerated(value = EnumType.STRING)
    private ResponseType responseType = ResponseType.Unknown;
    public ResponseType getResponseType() { return this.responseType; }
    public void setResponseType(ResponseType rType) { this.responseType = rType; }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private CaseInformation caseInformation = new CaseInformation();
    public CaseInformation getCaseInformation() { return caseInformation; }
    public void setCaseInformation(CaseInformation caseInformation) { this.caseInformation = caseInformation; }

    @ElementCollection
    private List<String> results = new ArrayList<>();
    public List<String> getResults() { return this.results; }
    public void setResults(List<String> results) { this.results = results; }

    @ElementCollection
    private List<String> invalidFields = new ArrayList<>();
    public List<String> getInvalidFields() { return this.invalidFields; }
    public void setInvalidFields(List<String> invFields) { this.invalidFields = invFields; }

    @ElementCollection
    private List<String> errorMessages = new ArrayList<>();
    public List<String> getErrorMessages() { return errorMessages; }
    public void addErrorMessages(String errorMessage) { this.errorMessages.add(errorMessage); }

    @ManyToOne
    private UserAuthentication createdBy;

    public UserAuthentication getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserAuthentication createdBy) {
        this.createdBy = createdBy;
    }

    @ManyToOne
    private UserAuthentication workedBy;

    public UserAuthentication getWorkedBy() {
        return workedBy;
    }

    public void setWorkedBy(UserAuthentication workedBy) {
        this.workedBy = workedBy;
    }

    public Long getUserId() {
        return createdBy != null ? createdBy.getId() : null;
    }


    private Boolean isFinished = false;
    public void setIsFinished(Boolean isFinished) { this.isFinished = isFinished; }
    public Boolean getIsFinished() { return this.isFinished; }
    

    private Boolean allowNullRequester;
    public void setAllowNullRequester(Boolean allowNullRequester) {this.allowNullRequester = allowNullRequester;}
    public Boolean getAllowNullRequester() { return this.allowNullRequester; }
}
