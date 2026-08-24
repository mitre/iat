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

import org.mitre.iwp.web.model.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationValueDTO {
    public AnnotationValueDTO(){}

    public AnnotationValueDTO(Long id, List<Point> points, AnnotationDTO annotation, Map<String, String> data) {
        this.id = id;
        this.points = points;
        this.annotation = annotation;
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public AnnotationDTO getAnnotation() {
        return annotation;
    }

    public void setAnnotation(AnnotationDTO annotation) {
        this.annotation = annotation;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    Long id;
    List<Point> points = new ArrayList<>();
    AnnotationDTO annotation = new AnnotationDTO();
    Map<String, String> data = new HashMap<>();
}
