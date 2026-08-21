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
public class Candidate {
    private static final Logger log = LoggerFactory.getLogger(Candidate.class);

    public Candidate(){

    }

    public Candidate(Long id, String score, String subjectIdentifier, List<ImageData> imageList, String adjudicationResults, String note) {
        this.id = id;
        this.score = score;
        this.subjectIdentifier = subjectIdentifier;
        this.imageList = imageList;
        this.adjudicationResults = adjudicationResults;
        this.note = note;
    }

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String score;
    public String getScore() {
        return score;
    }
    public void setScore(String score) {
        this.score = score;
    }

    private String subjectIdentifier;
    public String getSubjectIdentifier() {
        return subjectIdentifier;
    }
    public void setSubjectIdentifier(String subjectIdentifier) {
        this.subjectIdentifier = subjectIdentifier;
    }

    @OneToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, 
        orphanRemoval = true)
    private List<ImageData> imageList = new ArrayList<>();
    public List<ImageData> getImageList() { return this.imageList; }
    public void setImageList(List<ImageData> imageList) {
        this.imageList = imageList;
    }

    public void addImage(ImageData imageData) {
        this.imageList.add(imageData);
    }

    public void removeImage(ImageData imageData) {
        if (this.imageList.contains(imageData)) {
            this.imageList.remove(imageData);
        }
    }

    private String adjudicationResults;
    public String getAdjudicationResults() { return adjudicationResults; }
    public void setAdjudicationResults(String adjudicationResults) { this.adjudicationResults = adjudicationResults; }

    private String note;
    public String getNote() { return this.note; }
    public void setNote( String note ) { this.note = note; }

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
            catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
            result.append(newLineString);
        }
        result.append("}");

        return result.toString();
    }

}
