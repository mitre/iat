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

public class Service {
    private String serviceName;
    private String requestQueue;
    private String responseQueue;
    private boolean serviceEnabled;
    private boolean serviceExcluded;

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getRequestQueue() { return requestQueue; }
    public void setRequestQueue(String requestQueue) { this.requestQueue = requestQueue; }

    public String getResponseQueue() { return responseQueue; }
    public void setResponseQueue(String responseQueue) { this.responseQueue = responseQueue; }

    public Boolean getServiceEnabled() { return serviceEnabled; }
    public void setServiceEnabled(Boolean serviceEnabled) { this.serviceEnabled = serviceEnabled; }

    public Boolean getServiceExcluded() { return serviceExcluded; }
    public void setServiceExcluded(Boolean serviceExcluded) { this.serviceExcluded = serviceExcluded; }
}
