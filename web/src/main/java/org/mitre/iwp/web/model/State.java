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
import java.util.Map;

@Entity
public class State {
    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    @OneToOne(orphanRemoval = true)
    private Annotation annotation;
    public Annotation getAnnotation() { return annotation; }
    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    @ElementCollection
    @MapKeyColumn(name="name")
    @Column(name="value")
    @CollectionTable(name="state_data_map", joinColumns={@JoinColumn(name="state_data_id", referencedColumnName = "id")})
    private Map<String, String> data;
    public Map<String, String> getData() { return data; }
    public void setData(Map<String, String> data) {
        this.data = data;
    }

    private int type;
    public int getType() { return type; }
    public void setType(int type) {
        this.type = type;
    }
}
