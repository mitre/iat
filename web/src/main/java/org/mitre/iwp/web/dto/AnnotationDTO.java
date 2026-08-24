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

import java.util.ArrayList;
import java.util.List;

public class AnnotationDTO {
    public AnnotationDTO(){}

    public AnnotationDTO(String id, String text, String type, Long creationDateEpoch, AnnotationValueDTO value, List<AnnotationDTO> tempChildren, String parentId) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.creationDateEpoch = creationDateEpoch;
        this.value = value;
        this.tempChildren = tempChildren;
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCreationDateEpoch() {
        return creationDateEpoch;
    }

    public void setCreationDateEpoch(Long creationDateEpoch) {
        this.creationDateEpoch = creationDateEpoch;
    }

    public AnnotationValueDTO getValue() {
        return value;
    }

    public void setValue(AnnotationValueDTO value) {
        this.value = value;
    }

    public List<AnnotationDTO> getTempChildren() {
        return tempChildren;
    }

    public void setTempChildren(List<AnnotationDTO> tempChildren) {
        this.tempChildren = tempChildren;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    String id;
    String text;
    String type;
    Long creationDateEpoch = 0L;
    AnnotationValueDTO value = new AnnotationValueDTO();
    List<AnnotationDTO> tempChildren = new ArrayList<>();
    String parentId;
}
