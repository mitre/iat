/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.data;

public enum EbtsImageFormat {
    NULL(null),
    PNG("image/png");

    private String contentType;
    public String getContentType() { return contentType; }

    EbtsImageFormat(String contentType) {
        this.contentType = contentType;
    }
}
