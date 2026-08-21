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

import org.mitre.iwp.web.model.EbtsMessageRequest;

public class CreateIrisSearchRequestDTO {
    public CreateIrisSearchRequestDTO(){}

    public CreateIrisSearchRequestDTO(Long id, EbtsMessageRequest message, UserAuthenticationDTO user, Long creationDateEpoch, Boolean isFinished, String filePath) {
        this.id = id;
        this.message = message;
        this.user = user;
        this.creationDateEpoch = creationDateEpoch;
        this.isFinished = isFinished;
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EbtsMessageRequest getMessage() {
        return message;
    }

    public void setMessage(EbtsMessageRequest message) {
        this.message = message;
    }

    public UserAuthenticationDTO getUser() {
        return user;
    }

    public void setUser(UserAuthenticationDTO user) {
        this.user = user;
    }

    public Long getCreationDateEpoch() {
        return creationDateEpoch;
    }

    public void setCreationDateEpoch(Long creationDateEpoch) {
        this.creationDateEpoch = creationDateEpoch;
    }

    public Boolean getIsFinished() {
        return isFinished;
    }

    public void setIsFinished(Boolean finished) {
        isFinished = finished;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    Long id;
    EbtsMessageRequest message;
    UserAuthenticationDTO user = new UserAuthenticationDTO();
    Long creationDateEpoch = 0L;
    boolean isFinished = false;
    String filePath;
}
