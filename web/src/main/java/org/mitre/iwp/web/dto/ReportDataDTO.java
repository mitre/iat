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

import org.mitre.iwp.web.model.*;

import java.util.ArrayList;
import java.util.List;

public class ReportDataDTO {
    public ReportDataDTO(){}

    public ReportDataDTO(Long id, String fileName, ProbeDTO probe, List<CandidateDTO> candidates, boolean includeExclude, ResponseType responseType, CaseInformationDTO caseInformation, List<String> results, List<String> invalidFields, List<String> errorMessages, UserAuthenticationDTO createdBy, UserAuthenticationDTO workedBy, Boolean isFinished, Boolean allowNullRequester) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ProbeDTO getProbe() {
        return probe;
    }

    public void setProbe(ProbeDTO probe) {
        this.probe = probe;
    }

    public List<CandidateDTO> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<CandidateDTO> candidates) {
        this.candidates = candidates;
    }

    public boolean isIncludeExclude() {
        return includeExclude;
    }

    public void setIncludeExclude(boolean includeExclude) {
        this.includeExclude = includeExclude;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public CaseInformationDTO getCaseInformation() {
        return caseInformation;
    }

    public void setCaseInformation(CaseInformationDTO caseInformation) {
        this.caseInformation = caseInformation;
    }

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }

    public List<String> getInvalidFields() {
        return invalidFields;
    }

    public void setInvalidFields(List<String> invalidFields) {
        this.invalidFields = invalidFields;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public UserAuthenticationDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserAuthenticationDTO createdBy) {
        this.createdBy = createdBy;
    }

    public UserAuthenticationDTO getWorkedBy() {
        return workedBy;
    }

    public void setWorkedBy(UserAuthenticationDTO workedBy) {
        this.workedBy = workedBy;
    }

    public Boolean getIsFinished() {
        return isFinished;
    }

    public void setIsFinished(Boolean isFinished) {
        this.isFinished = isFinished;
    }

    public Boolean getAllowNullRequester() {
        return allowNullRequester; }

    public void setAllowNullRequester(Boolean allowNullRequester) {
        this.allowNullRequester = allowNullRequester
;
    }

    Long id;
    String fileName;
    ProbeDTO probe = new ProbeDTO();
    List<CandidateDTO> candidates = new ArrayList<>();
    boolean includeExclude;
    ResponseType responseType;
    CaseInformationDTO caseInformation = new CaseInformationDTO();
    List<String> results = new ArrayList<>();
    List<String> invalidFields = new ArrayList<>();
    List<String> errorMessages = new ArrayList<>();
    UserAuthenticationDTO createdBy = new UserAuthenticationDTO();
    UserAuthenticationDTO workedBy = new UserAuthenticationDTO();
    Boolean isFinished = false;
    Boolean allowNullRequester;
}
