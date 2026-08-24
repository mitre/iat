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

import jakarta.persistence.*;

@Entity
public class CreateIrisSearchRequest {
  public CreateIrisSearchRequest(){

  }

  public CreateIrisSearchRequest(Long id, EbtsMessageRequest message, UserAuthentication user, Long creationDateEpoch, Boolean isFinished, String filePath) {
    this.id = id;
    this.message = message;
    this.user = user;
    this.creationDateEpoch = creationDateEpoch;
    this.isFinished = isFinished;
    this.filePath = filePath;
  }

  @Id
  @GeneratedValue
  private Long id;
  public Long getId() {
    return this.id;
  }
  public void setId(Long id) {
    this.id = id;
  }

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private EbtsMessageRequest message;
  public void setMessage(EbtsMessageRequest message) {
    this.message = message;
  }
  public EbtsMessageRequest getMessage() {
    return this.message;
  }

  @ManyToOne
  private UserAuthentication user;
  public UserAuthentication getUser() {
    return this.user;
  }
  public void setUser(UserAuthentication user) {
    this.user = user;
  }

  private Long creationDateEpoch = 0L;
  public void setCreationDateEpoch(Long creationDateEpoch) {
    this.creationDateEpoch = creationDateEpoch;
  }
  public Long getCreationDateEpoch() {
    return this.creationDateEpoch;
  }

  private Boolean isFinished = false;
  public void setIsFinished(Boolean isFinished) {
    this.isFinished = isFinished;
  }
  public Boolean getIsFinished() {
    return this.isFinished;
  }

  private String filePath;
  public String getFilePath() { return this.filePath; }
  public void setFilePath(String filePath) { this.filePath = filePath; }
}
