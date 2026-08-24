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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotationValue {
    public AnnotationValue(){

    }

    public AnnotationValue(Long id, List<Point> points, Annotation annotation, Map<String, String> data) {
        this.id = id;
        this.points = points;
        this.annotation = annotation;
        this.data = data;
    }

    @Id
    @GeneratedValue
    private Long id;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ElementCollection
    @CollectionTable(name = "point", joinColumns = @JoinColumn(name = "annotation_value_id"))
    private List<Point> points = new ArrayList<>();

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    @OneToOne
    @JoinColumn(name = "annotation_id")
    @JsonIgnore
    private Annotation annotation = new Annotation();

    public Annotation getAnnotation() {
        return this.annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "annotation_value_data_map", joinColumns = {
            @JoinColumn(name = "annotation_value_data_id", referencedColumnName = "id") })
    private Map<String, String> data = new HashMap<>();

    public Map<String, String> getData() {
        return this.data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
