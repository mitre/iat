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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import com.google.gson.annotations.Expose;


@Entity
public class DualPDMResponse extends ServiceResponse {

    @Lob
	@Column(columnDefinition="LONGBLOB")
    @Expose
    private byte[] imageBytes;

    public DualPDMResponse(){
        // Empty Constructor to satisfy JPA requirement
    }

    public int getByteSize() {
        return imageBytes.length;
    }

    // Getter and Setters
    public byte[] getImageBytes() {
        return imageBytes;
    }
    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }
}
