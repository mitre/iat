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

import java.util.HashMap;
import java.util.Map;

public class VersionInfo {
    public String getIwpVersion() {
        return iwpVersion;
    }
    public void setIwpVersion(String iwpVersion) {
        this.iwpVersion = iwpVersion;
    }
    public String getUiVersion() {
        return uiVersion;
    }
    public void setUiVersion(String uiVersion) {
        this.uiVersion = uiVersion;
    }
    public Map<String, String> getServiceVersions() {
        return serviceVersions;
    }
    public void setServiceVersions(Map<String, String> serviceVersions) {
        this.serviceVersions = serviceVersions;
    }
    
    private String iwpVersion = "";
    private String uiVersion = "";
    private Map<String, String> serviceVersions = new HashMap<>();
}
