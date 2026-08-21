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
import java.util.ArrayList;
import java.util.List;

@Entity
public class ImageFile {
    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    String fileHash;
    public String getFileHash() { return this.fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    @ElementCollection(fetch=FetchType.EAGER)
    private List<String> imageHash = new ArrayList<>();
    public List<String> getImageHash() { return this.imageHash; }
    public void setImageHash(List<String> imageHash) { this.imageHash = imageHash; }
}
