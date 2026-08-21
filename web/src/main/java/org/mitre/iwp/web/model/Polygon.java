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

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    @Expose
    public String name;
    public String getName(){return this.name; }
    public void setName(String name) { this.name = name; }

    @Expose
    public String code;
    public String getCode(){ return this.code;}
    public void setCode(String code){this.code = code;}

    @Expose
    public String color;
    public String getColor() { return this.color; }
    public void setColor(String color) { this.color = color; }

    @Expose
    public List<Point> outline = new ArrayList<>();
    public List<Point> getOutline(){ return this.outline; }
    public void setOutline(List<Point> points){ this.outline = points; }
}
