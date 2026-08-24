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

import jakarta.persistence.Entity;

@Entity
public class BiqtResponse extends ServiceResponse {
    int quality = -1;
    public void setQuality(int quality) {
        if(quality >= 0 && quality <= 100) {
            this.quality = quality;
        }
    }
    public int getQuality() {
        return quality;
    }

    int irisCenterX;
    public void setIrisCenterX(int irisCenterX) {
        this.irisCenterX = irisCenterX;
    }
    public int getIrisCenterX() {
        return irisCenterX;
    }

    int irisCenterY;
    public int getIrisCenterY() {
        return irisCenterY;
    }
    public void setIrisCenterY(int irisCenterY) {
        this.irisCenterY = irisCenterY;
    }

    int percentVisibleIris;
    public int getPercentVisibleIris() { return this.percentVisibleIris; }
    public void setPercentVisibleIris(int percentVisibleIris) { this.percentVisibleIris = percentVisibleIris; }

    int irisDiameter;
    public int getIrisDiameter() { return this.irisDiameter; }
    public void setIrisDiameter(int irisDiameter) { this.irisDiameter = irisDiameter; }

}
