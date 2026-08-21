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

public class ProfileDTO {
    public ProfileDTO(){}

    public ProfileDTO(Long id, String name, String destinationAgencyIdentifier, String originatingAgencyIdentifier, String attentionIndicator,
                      String sourceAgency, boolean isReportExclude, String fullName, String email, String phoneNumber, String title,
                      String department, String address, UserSettingDTO userSetting){
        this.id = id;
        this.name = name;
        this.destinationAgencyIdentifier = destinationAgencyIdentifier;
        this.originatingAgencyIdentifier = originatingAgencyIdentifier;
        this.attentionIndicator = attentionIndicator;
        this.sourceAgency = sourceAgency;
        this.isReportExclude = isReportExclude;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.title = title;
        this.department = department;
        this.address = address;
        this.userSetting = userSetting;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean getIsReportExclude() {
        return isReportExclude;
    }

    public void setReportExclude(boolean reportExclude) {
        isReportExclude = reportExclude;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UserSettingDTO getUserSetting() {
        return userSetting;
    }

    public void setUserSetting(UserSettingDTO userSetting) {
        this.userSetting = userSetting;
    }

    Long id;
    String name;
    String destinationAgencyIdentifier;
    String originatingAgencyIdentifier;
    String attentionIndicator;
    String sourceAgency;
    boolean isReportExclude;
    String fullName;
    String email;
    String phoneNumber;
    String title;
    String department;
    String address;
    UserSettingDTO userSetting;
}
