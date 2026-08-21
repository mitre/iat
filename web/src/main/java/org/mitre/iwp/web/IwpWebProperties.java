/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.*;

@Configuration
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "iris")
public class IwpWebProperties {
    public boolean getBiqtService() {
        return biqtService;
    }
    public void setBiqtService(boolean biqtService) {
        this.biqtService = biqtService;
    }

    public String getBiqtQueueRequest() {
        return biqtQueueRequest;
    }
    public void setBiqtQueueRequest(String biqtQueueRequest) {
        this.biqtQueueRequest = biqtQueueRequest;
    }

    public String getBiqtQueueResponse() {
        return biqtQueueResponse;
    }
    public void setBiqtQueueResponse(String biqtQueueResponse) {
        this.biqtQueueResponse = biqtQueueResponse;
    }

    public boolean getTshepiiService() {
        return tshepiiService;
    }
    public void setTshepiiService(boolean tshepiiService) {
        this.tshepiiService = tshepiiService;
    }

    public String getTshepiiQueueRequest() {
        return tshepiiQueueRequest;
    }
    public void setTshepiiQueueRequest(String tshepiiQueueRequest) {
        this.tshepiiQueueRequest = tshepiiQueueRequest;
    }

    public String getTshepiiQueueResponse() {
        return tshepiiQueueResponse;
    }
    public void setTshepiiQueueResponse(String tshepiiQueueResponse) { this.tshepiiQueueResponse = tshepiiQueueResponse; }

    // Dual PDM
    public boolean getDualPDMService() {
        return dualPDMService;
    }
    public void setDualPDMService(boolean dualPDMService) {
        this.dualPDMService = dualPDMService;
    }

    public String getDualPDMQueueRequest() {
        return dualPDMQueueRequest;
    }
    public void setDualPDMQueueRequest(String dualPDMQueueRequest) {
        this.dualPDMQueueRequest = dualPDMQueueRequest;
    }

    public String getDualPDMQueueResponse() {
        return dualPDMQueueResponse;
    }
    public void setDualPDMQueueResponse(String dualPDMQueueResponse) {
        this.dualPDMQueueResponse = dualPDMQueueResponse;
    }

    public boolean getAciiService() { return aciiService; }
    public void setAciiService(boolean aciiService) {
        this.aciiService = aciiService;
    }

    public String getAciiQueueRequest() { return aciiQueueRequest; }
    public void setAciiQueueRequest(String aciiQueueRequest) {
        this.aciiQueueRequest = aciiQueueRequest;
    }

    public String getAciiQueueResponse() { return aciiQueueResponse; }
    public void setAciiQueueResponse(String aciiQueueResponse) {
        this.aciiQueueResponse = aciiQueueResponse;
    }

    public String getContactQueueResponse() { return contactQueueResponse; }
    public void setContactQueueResponse(String contactQueueResponse) {this.contactQueueResponse = contactQueueResponse;}

    public boolean getIrisAnnotationService() { return irisAnnotationService; }
    public void setIrisAnnotationService(boolean irisAnnotationService) { this.irisAnnotationService = irisAnnotationService; }

    public String getIrisAnnotationQueueRequest() { return irisAnnotationQueueRequest; }
    public void setIrisAnnotationQueueRequest(String irisAnnotationQueueRequest) { this.irisAnnotationQueueRequest = irisAnnotationQueueRequest; }

    public String getIrisAnnotationQueueResponse() { return this.irisAnnotationQueueResponse; }
    public void setIrisAnnotationQueueResponse(String irisAnnotationQueueResponse){this.irisAnnotationQueueResponse = irisAnnotationQueueResponse;}

    public boolean getPdmService() { return pdmService; }
    public void setPdmService(boolean pdmService) { this.pdmService = pdmService; }

    public String getPdmQueueRequest() { return pdmQueueRequest; }
    public void setPdmQueueRequest(String pdmQueueRequest) { this.pdmQueueRequest = pdmQueueRequest; }

    public String getPdmQueueResponse() { return pdmQueueResponse; }
    public void setPdmQueueResponse(String pdmQueueResponse) { this.pdmQueueResponse = pdmQueueResponse; }

    public List<String> getExcludedServices() { return this.excludedServices; }
    public void setExcludedServices(List<String> excludedServices){this.excludedServices = excludedServices;}

    public String getActiveMqAddress() { return activeMqAddress; }
    public void setActiveMqAddress(String activeMqAddress) { this.activeMqAddress = activeMqAddress; }

    public String getIwpWebAddress() { return iwpWebAddress; }
    public void setIwpWebAddress(String iwpWebAddress) { this.iwpWebAddress = iwpWebAddress; }


    public String getTot() {
        return tot;
    }

    public void setTot(String tot) {
        this.tot = tot;
    }

    public String getPry() {
        return pry;
    }

    public void setPry(String pry) {
        this.pry = pry;
    }

    public String getDai() {
        return dai;
    }

    public void setDai(String dai) {
        this.dai = dai;
    }

    public String getOri() {
        return ori;
    }

    public void setOri(String ori) {
        this.ori = ori;
    }

    public String getTcn() {
        return tcn;
    }

    public void setTcn(String tcn) {
        this.tcn = tcn;
    }

    public String getTcr() {
        return tcr;
    }

    public void setTcr(String tcr) {
        this.tcr = tcr;
    }

    public String getNsr() {
        return nsr;
    }

    public void setNsr(String nsr) {
        this.nsr = nsr;
    }

    public String getNtr() {
        return ntr;
    }

    public void setNtr(String ntr) {
        this.ntr = ntr;
    }

    public String getDnm() {
        return dnm;
    }

    public void setDnm(String dnm) {
        this.dnm = dnm;
    }

    public String getDvn() {
        return dvn;
    }

    public void setDvn(String dvn) {
        this.dvn = dvn;
    }

    public String getApo() {
        return apo;
    }

    public void setApo(String apo) {
        this.apo = apo;
    }

    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }

    public String getApv() {
        return apv;
    }

    public void setApv(String apv) {
        this.apv = apv;
    }

    public String getAtn() {
        return atn;
    }

    public void setAtn(String atn) {
        this.atn = atn;
    }

    public String getCix() {
        return cix;
    }

    public void setCix(String cix) {
        this.cix = cix;
    }

    public String getNcr() {
        return ncr;
    }

    public void setNcr(String ncr) {
        this.ncr = ncr;
    }

    public String getCga() {
        return cga;
    }

    public void setCga(String cga) {
        this.cga = cga;
    }

    public String getProfileDai() {
        return profileDai;
    }

    public void setProfileDai(String profileDai) {
        this.profileDai = profileDai;
    }

    public String getProfileOri() {
        return profileOri;
    }

    public void setProfileOri(String profileOri) {
        this.profileOri = profileOri;
    }

    public String getProfileAttIndicator() {
        return profileAttIndicator;
    }

    public void setProfileAttIndicator(String profileAttIndicator) {
        this.profileAttIndicator = profileAttIndicator;
    }

    public String getProfileSrcAgency() {
        return profileSrcAgency;
    }

    public void setProfileSrcAgency(String profileSrcAgency) {
        this.profileSrcAgency = profileSrcAgency;
    }

    public Boolean getIsProfileReportExclude() {
        return isProfileReportExclude;
    }

    public void setIsProfileReportExclude(Boolean isProfileReportExclude) {
        this.isProfileReportExclude = isProfileReportExclude;
    }

    public String getProfileDpt() {
        return profileDpt;
    }

    public void setProfileDpt(String profileDpt) {
        this.profileDpt = profileDpt;
    }

    public String getProfileAdd() {
        return profileAdd;
    }

    public void setProfileAdd(String profileAdd) {
        this.profileAdd = profileAdd;
    }

    public Integer getProfileStrokeWidth() {
        return profileStrokeWidth;
    }

    public void setProfileStrokeWidth(Integer profileStrokeWidth) {
        this.profileStrokeWidth = profileStrokeWidth;
    }

    public String getProfileDrawColor() {
        return profileDrawColor;
    }

    public void setProfileDrawColor(String profileDrawColor) {
        this.profileDrawColor = profileDrawColor;
    }

    public String getProfileFillColor() {
        return profileFillColor;
    }

    public void setProfileFillColor(String profileFillColor) {
        this.profileFillColor = profileFillColor;
    }

    public Integer getProfileNumberHistory() {
        return profileNumberHistory;
    }

    public Map<String, String> getAnnotations(){return annotations;}
    public Map<String, String> getAnnotationsColorHex(){return annotationsColorHex;}

    public void setAdjudicationValue(Map<String, String> adjudicationValue){
        this.adjudicationValueLoaded.clear();
        for(Map.Entry<String, String> entry : adjudicationValue.entrySet()){
            String[] adjudicationSettings = entry.getValue().split(",");

            if(adjudicationSettings.length == 2){
                this.adjudicationValueLoaded.put(adjudicationSettings[0], adjudicationSettings[1]);
            }
        }
    }

    public Map<String, String> getAdjudicationValue(){
        return this.adjudicationValueLoaded;
    }

    public List<String> getAdjudicationKeys() {
        return new ArrayList<>(this.adjudicationValueLoaded.keySet());
    }

    public void setProfileNumberHistory(Integer profileNumberHistory) {
        this.profileNumberHistory = profileNumberHistory;
    }

    public String getAciiServiceName() {
        return this.aciiServiceName;
    }

    public void setAciiServiceName(String aciiServiceName){
        this.aciiServiceName = aciiServiceName;
    }

    public String getBiqtServiceName(){
        return this.biqtServiceName;
    }

    public void setBiqtServiceName(String biqtServiceName){
        this.biqtServiceName = biqtServiceName;
    }

    public String getTshepiiServiceName() {
        return this.tshepiiServiceName;
    }

    public void setTshepiiServiceName(String tshepiiServiceName){
        this.tshepiiServiceName = tshepiiServiceName;
    }

    // Dual PDM
    public String getDualPDMServiceName() {
        return this.dualPDMServiceName;
    }

    public void setDualPDMServiceName(String dualPDMServiceName){
        this.dualPDMServiceName = dualPDMServiceName;
    }

    public String getDualPDMServiceVersion() {
        return dualPDMServiceVersion;
    }

    public void setDualPDMServiceVersion(String dualPDMServiceVersion) {
        this.dualPDMServiceVersion = dualPDMServiceVersion;
    }

    public String getIrisAnnotationServiceName() {
        return this.irisAnnotationServiceName;
    }

    public void setIrisAnnotationServiceName(String irisAnnotationServiceName){
        this.irisAnnotationServiceName = irisAnnotationServiceName;
    }

    public String getPdmServiceName() {
        return this.pdmServiceName;
    }

    public void setPdmServiceName(String pdmServiceName){
        this.pdmServiceName = pdmServiceName;
    }

    public String getBiqtServiceVersion() {
        return biqtServiceVersion;
    }

    public String getAciiServiceVersion() {
        return aciiServiceVersion;
    }

    public String getTshepiiServiceVersion() {
        return tshepiiServiceVersion;
    }

    public String getIrisAnnotationServiceVersion() {
        return irisAnnotationServiceVersion;
    }

    public String getPdmServiceVersion() {
        return pdmServiceVersion;
    }

    public String getIwpBackendVersion() {
        return iwpBackendVersion;
    }

    public String getIwpFrontendVersion() {
        return iwpFrontendVersion;
    }

    public void setBiqtServiceVersion(String biqtServiceVersion) {
        this.biqtServiceVersion = biqtServiceVersion;
    }

    public void setAciiServiceVersion(String aciiServiceVersion) {
        this.aciiServiceVersion = aciiServiceVersion;
    }

    public void setTshepiiServiceVersion(String tshepiiServiceVersion) {
        this.tshepiiServiceVersion = tshepiiServiceVersion;
    }


    public void setIrisAnnotationServiceVersion(String irisAnnotationServiceVersion) {
        this.irisAnnotationServiceVersion = irisAnnotationServiceVersion;
    }

    public void setPdmServiceVersion(String pdmServiceVersion) {
        this.pdmServiceVersion = pdmServiceVersion;
    }

    public void setIwpBackendVersion(String iwpBackendVersion) {
        this.iwpBackendVersion = iwpBackendVersion;
    }

    public void setIwpFrontendVersion(String iwpFrontendVersion) {
        this.iwpFrontendVersion = iwpFrontendVersion;
    }

    public String getActiveMqUserName() {
        return this.activeMqUserName;
    }

    public void setActiveMqUserName(String activeMqUserName){
        this.activeMqUserName = activeMqUserName;
    }

    public String getActiveMqPassword() {
        return this.activeMqPassword;
    }

    public void setActiveMqPassword(String activeMqPassword){
        this.activeMqPassword = activeMqPassword;
    }

    public String getActiveMqHttpAddress() {
        return this.activeMqHttpAddress;
    }

    public void setActiveMqHttpAddress(String activeMqHttpAddress){
        this.activeMqHttpAddress = activeMqHttpAddress;
    }

    public int getMaxWidthImage() {
        return maxWidthImage;
    }

    public void setMaxWidthImage(int maxWidthImage) {
        this.maxWidthImage = maxWidthImage;
    }

    public int getMaxHeightImage() {
        return maxHeightImage;
    }

    public void setMaxHeightImage(int maxHeightImage) {
        this.maxHeightImage = maxHeightImage;
    }

    private String tot;
    private String pry;
    private String dai;
    private String ori;
    private String tcn;
    private String tcr;
    private String nsr;
    private String ntr;
    private String dnm;
    private String dvn;
    private String apo;
    private String apn;
    private String apv;
    private String atn;
    private String cix;
    private String ncr;
    private String cga;
    private boolean biqtService;
    private boolean tshepiiService;
    private boolean dualPDMService;
    private boolean aciiService;
    private boolean irisAnnotationService;
    private boolean pdmService;
    private String biqtQueueRequest;
    private String biqtQueueResponse;
    private String aciiQueueRequest;
    private String aciiQueueResponse;
    private String tshepiiQueueRequest;
    private String tshepiiQueueResponse;
    private String dualPDMQueueRequest;
    private String dualPDMQueueResponse;
    private String contactQueueResponse;
    private String irisAnnotationQueueRequest;
    private String irisAnnotationQueueResponse;
    private String pdmQueueRequest;
    private String pdmQueueResponse;
    private String aciiServiceName;
    private String biqtServiceName;
    private String tshepiiServiceName;
    private String dualPDMServiceName;
    private String irisAnnotationServiceName;
    private String pdmServiceName;
    private List<String> excludedServices = new ArrayList<>();
    private String activeMqAddress;
    private String iwpWebAddress;
    private String activeMqUserName;
    private String activeMqPassword;
    private String activeMqHttpAddress;

    // Default Profiles
    private String profileDai;
    private String profileOri;
    private String profileAttIndicator;
    private String profileSrcAgency;
    private Boolean isProfileReportExclude;
    private String profileDpt;
    private String profileAdd;
    private int profileStrokeWidth;
    private String profileDrawColor;
    private String profileFillColor;
    private int profileNumberHistory;

    private String biqtServiceVersion;
    private String aciiServiceVersion;
    private String tshepiiServiceVersion;
    private String dualPDMServiceVersion;
    private String irisAnnotationServiceVersion;
    private String pdmServiceVersion;
    private String iwpBackendVersion;
    private String iwpFrontendVersion;

    private int maxWidthImage;
    private int maxHeightImage;

    private final Map<String, String> annotations = new HashMap<>();
    private final Map<String, String> annotationsColorHex = new HashMap<>();
    private Map<String, String> adjudicationValueLoaded = new LinkedHashMap<>();

}
