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
public class Profile {
    public Profile(){

    }

    public Profile(Long id, String name, String destinationAgencyIdentifier, String originatingAgencyIdentifier, String attentionIndicator,
                   String sourceAgency, boolean isReportExclude, String fullName, String email, String phoneNumber, String title,
                   String department, String address, UserSetting userSetting){
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
    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    private String destinationAgencyIdentifier;
    public String getDestinationAgencyIdentifier() { return destinationAgencyIdentifier; }
    public void setDestinationAgencyIdentifier(String destinationAgencyIdentifier) { this.destinationAgencyIdentifier = destinationAgencyIdentifier; }

    private String originatingAgencyIdentifier;
    public String getOriginatingAgencyIdentifier() { return originatingAgencyIdentifier; }
    public void setOriginatingAgencyIdentifier(String originatingAgencyIdentifier) {this.originatingAgencyIdentifier = originatingAgencyIdentifier; }

    private String attentionIndicator;
    public String getAttentionIndicator() { return attentionIndicator; }
    public void setAttentionIndicator(String attentionIndicator) { this.attentionIndicator = attentionIndicator; }

    private String sourceAgency;
    public String getSourceAgency() { return sourceAgency; }
    public void setSourceAgency(String sourceAgency) { this.sourceAgency = sourceAgency; }

    private boolean isReportExclude;
    public boolean getIsReportExclude() { return isReportExclude; }
    public void setIsReportExclude(boolean reportExclude) { isReportExclude = reportExclude; }

    private String fullName;
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    private String email;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    private String phoneNumber;
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    private String title;
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    private String department;
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    private String address;
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @OneToOne(cascade = CascadeType.ALL )
    private UserSetting userSetting;
    public UserSetting getUserSetting() { return this.userSetting; }
    public void setUserSetting(UserSetting userSetting) { this.userSetting = userSetting; }
}
