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

public class Crypt {
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public Crypt(List<Point> pointList){
        this.pointList = pointList;
    }
    public Crypt(){
        this.pointList = new ArrayList<>();
    }

    @Expose
    List<Point> pointList;
    public void setPointList(List<Point> pList) {
        this.pointList = pList;
    }
    public List<Point> getPointList() {
        return this.pointList;
    }
}
