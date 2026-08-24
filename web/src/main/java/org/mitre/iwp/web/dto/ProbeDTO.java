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

import org.mitre.iwp.web.model.ImageData;

import java.util.ArrayList;
import java.util.List;

public class ProbeDTO {
    public ProbeDTO(){}

    public ProbeDTO(Long id, String transactionControlReference, String transactionControlNumber, List<ImageData> imageList, String name, String subjectIdentifier) {
        this.id = id;
        this.transactionControlReference = transactionControlReference;
        this.transactionControlNumber = transactionControlNumber;
        this.imageList = imageList;
        this.name = name;
        this.subjectIdentifier = subjectIdentifier;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionControlReference() {
        return transactionControlReference;
    }

    public void setTransactionControlReference(String transactionControlReference) {
        this.transactionControlReference = transactionControlReference;
    }

    public String getTransactionControlNumber() {
        return transactionControlNumber;
    }

    public void setTransactionControlNumber(String transactionControlNumber) {
        this.transactionControlNumber = transactionControlNumber;
    }

    public List<ImageData> getImageList() {
        return imageList;
    }

    public void setImageList(List<ImageData> imageList) {
        this.imageList = imageList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubjectIdentifier() {
        return subjectIdentifier;
    }

    public void setSubjectIdentifier(String subjectIdentifier) {
        this.subjectIdentifier = subjectIdentifier;
    }

    Long id;
    String transactionControlReference;
    String transactionControlNumber;
    List<ImageData> imageList = new ArrayList<>();
    String name;
    String subjectIdentifier;

}
