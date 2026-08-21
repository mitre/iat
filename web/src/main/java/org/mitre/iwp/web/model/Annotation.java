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
import java.util.Comparator;
import java.util.List;

@Entity
public class Annotation {
    private static final Logger log = LoggerFactory.getLogger(Annotation.class);

    public Annotation(){}

    public Annotation(String id, String text, String type, Long creationDateEpoch, AnnotationValue value, List<Annotation> tempChildren, String parentId) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.creationDateEpoch = creationDateEpoch;
        this.value = value;
        this.tempChildren = tempChildren;
        this.parentId = parentId;
    }

    @Id
    @Column(length = 30)
    private String id;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private Long creationDateEpoch = 0L;

    public void setCreationDateEpoch(Long creationDateEpoch) {
        this.creationDateEpoch = creationDateEpoch;
    }

    public Long getCreationDateEpoch() {
        return this.creationDateEpoch;
    }
    // NOTE: Because of the current way we're doing saving of annotations/groups,
    // we can't easily support cascading of delete. This needs to be handled
    // separately!
    @OneToOne(mappedBy = "annotation", cascade = { CascadeType.MERGE, CascadeType.PERSIST }, orphanRemoval = true)
    private AnnotationValue value;

    public AnnotationValue getValue() {
        return this.value;
    }

    public void setValue(AnnotationValue value) {
        if (this.value != null) {
            this.value.setAnnotation(null);
        }
        if (value != null) {
            value.setAnnotation(this);
        }

        this.value = value;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Annotation> tempChildren = new ArrayList<>();
        
    public List<Annotation> getTempChildren() {
        Comparator<Annotation> creationDateEpochSorter
                = (a1, a2) -> a1.getCreationDateEpoch().compareTo(a2.getCreationDateEpoch());
        this.tempChildren.sort(creationDateEpochSorter.reversed());

        return tempChildren;
    }

    public void setTempChildren(List<Annotation> children) {
        this.tempChildren = children;
    }

    private String parentId;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLineString = System.getProperty("line.separator");

        result.append(this.getClass().getName());
        result.append(" Object {");
        result.append(newLineString);

        // determine fields declared in this class only (no fields of superclass)
        java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();

        // print field names paired with their values
        for (java.lang.reflect.Field field : fields) {
            result.append("  ");
            result.append(field.getName());
            result.append(": ");
            // requires access to private field:
            try {
                result.append(field.get(this));
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
            result.append(newLineString);
        }
        result.append("}");

        return result.toString();
    }
}
