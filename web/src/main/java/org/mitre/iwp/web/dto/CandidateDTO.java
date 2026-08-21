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

public class CandidateDTO {
    public CandidateDTO(){}

    public CandidateDTO(Long id, String score, String subjectIdentifier, List<ImageData> imageList, String adjudicationResults, String note) {
        this.id = id;
        this.score = score;
        this.subjectIdentifier = subjectIdentifier;
        this.imageList = imageList;
        this.adjudicationResults = adjudicationResults;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getSubjectIdentifier() {
        return subjectIdentifier;
    }

    public void setSubjectIdentifier(String subjectIdentifier) {
        this.subjectIdentifier = subjectIdentifier;
    }

    public List<ImageData> getImageList() {
        return imageList;
    }

    public void setImageList(List<ImageData> imageList) {
        this.imageList = imageList;
    }

    public String getAdjudicationResults() {
        return adjudicationResults;
    }

    public void setAdjudicationResults(String adjudicationResults) {
        this.adjudicationResults = adjudicationResults;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    Long id;
    String score;
    String subjectIdentifier;
    List<ImageData> imageList = new ArrayList<>();
    String adjudicationResults;
    String note;
}
