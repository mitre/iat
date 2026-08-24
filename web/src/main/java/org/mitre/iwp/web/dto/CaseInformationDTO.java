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

import org.mitre.iwp.web.model.Person;

public class CaseInformationDTO {
    public CaseInformationDTO(){}

    public CaseInformationDTO(Long id, Long comparisonDate, Long receivedDate, Person requestedBy) {
        this.id = id;
        this.comparisonDate = comparisonDate;
        this.receivedDate = receivedDate;
        this.requestedBy = requestedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getComparisonDate() {
        return comparisonDate;
    }

    public void setComparisonDate(Long comparisonDate) {
        this.comparisonDate = comparisonDate;
    }

    public Long getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Long receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Person getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Person requestedBy) {
        this.requestedBy = requestedBy;
    }

    Long id;
    Long comparisonDate;
    Long receivedDate;
    Person requestedBy;

}
