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

@Entity
public class CaseInformation {
    private static final Logger log = LoggerFactory.getLogger(CaseInformation.class);

    public CaseInformation(){

    }

    public CaseInformation(Long id, Long comparisonDate, Long receivedDate, Person requestedBy) {
        this.id = id;
        this.comparisonDate = comparisonDate;
        this.receivedDate = receivedDate;
        this.requestedBy = requestedBy;
    }

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    public Person requestedBy;

    private Long comparisonDate = 0L;
    private Long receivedDate = 0L;

    public Person getRequestedBy() {
        return requestedBy;
    }
    public void setRequestedBy(Person requestedBy) {
        this.requestedBy = requestedBy;
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
