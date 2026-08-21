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

public class UserSettingDTO {
    public UserSettingDTO(){}

    public UserSettingDTO(Long id, int strokeWidth, String drawColor, String fillColor, int numHistory, int tshepiiStrokeOpacity){
        this.id = id;
        this.strokeWidth = strokeWidth;
        this.drawColor = drawColor;
        this.fillColor = fillColor;
        this.numHistory = numHistory;
        this.tshepiiStrokeOpacity = tshepiiStrokeOpacity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public String getDrawColor() {
        return drawColor;
    }

    public void setDrawColor(String drawColor) {
        this.drawColor = drawColor;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public int getNumHistory() {
        return numHistory;
    }

    public void setNumHistory(int numHistory) {
        this.numHistory = numHistory;
    }

    public int getTshepiiStrokeOpacity() {
        return tshepiiStrokeOpacity;
    }

    public void setTshepiiStrokeOpacity(int tshepiiStrokeOpacity) {
        this.tshepiiStrokeOpacity = tshepiiStrokeOpacity;
    }

    Long id;
    int strokeWidth;
    String drawColor;
    String fillColor;
    int numHistory;
    int tshepiiStrokeOpacity;
}
