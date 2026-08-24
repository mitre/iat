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
public class Probe {
    private static final Logger log = LoggerFactory.getLogger(Probe.class);

    public Probe(){

    }

    public Probe(Long id, String transactionControlReference, String transactionControlNumber, List<ImageData> imageList, String name, String subjectIdentifier) {
        this.id = id;
        this.transactionControlReference = transactionControlReference;
        this.transactionControlNumber = transactionControlNumber;
        this.imageList = imageList;
        this.name = name;
        this.subjectIdentifier = subjectIdentifier;
    }

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String transactionControlReference;
    public String getTransactionControlReference() { return this.transactionControlReference; }
    public void setTransactionControlReference(String tcr) { this.transactionControlReference = tcr; }

    private String transactionControlNumber;
    public String getTransactionControlNumber() { return this.transactionControlNumber; }
    public void setTransactionControlNumber(String tcn) { this.transactionControlNumber = tcn; }

    @OneToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, 
        orphanRemoval = true)
    private List<ImageData> imageList = new ArrayList<>();
    public List<ImageData> getImageList() { return this.imageList; }
    public void setImageList(List<ImageData> image) {
        this.imageList = image;
    }

    public void addImage(ImageData imageData) {
        this.imageList.add(imageData);
    }

    public void removeImage(ImageData imageData) {
        if (this.imageList.contains(imageData)) {
            this.imageList.remove(imageData);
        }
    }



    private String name;
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    private String subjectIdentifier;
    public String getSubjectIdentifier() {
        return this.subjectIdentifier;
    }
    public void setSubjectIdentifier(String subjectIdentifier) {
        this.subjectIdentifier = subjectIdentifier;
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
}
