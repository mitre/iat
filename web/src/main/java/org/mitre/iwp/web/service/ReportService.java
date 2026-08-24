/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.service;

import com.google.gson.Gson;
import com.itextpdf.text.DocumentException;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.model.*;
import org.mitre.iwp.web.reporting.ImageReporting;
import org.mitre.iwp.web.reporting.MultiModalReporting;
import org.mitre.iwp.web.reporting.SrbReporting;
import org.mitre.iwp.web.reporting.XmlReporting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.io.File;
import java.io.FileOutputStream;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ReportDataRepository reportRepository;

    private final ReportResponseRepository reportResponseRepository;

    private final AppNotificationRepository appNotificationRepository;

    private final PersonRepository personRepository;

    private final SrbReporting srbReporting;

    private final XmlReporting xmlReporting;

    private final ImageReporting imageReporting;

    private final MultiModalReporting multiModalReporting;

    private final String uploadPath;

    public ReportService(ReportDataRepository reportRepository,
                         ReportResponseRepository reportResponseRepository,
                         AppNotificationRepository appNotificationRepository,
                         PersonRepository personRepository,
                         SrbReporting srbReporting,
                         XmlReporting xmlReporting,
                         ImageReporting imageReporting,
                         MultiModalReporting multiModalReporting,
                         @Value("${iwp.upload.path}") String uploadPath) {
        this.reportRepository = reportRepository;
        this.reportResponseRepository = reportResponseRepository;
        this.appNotificationRepository = appNotificationRepository;
        this.personRepository = personRepository;
        this.srbReporting = srbReporting;
        this.xmlReporting = xmlReporting;
        this.imageReporting = imageReporting;
        this.multiModalReporting = multiModalReporting;
        this.uploadPath = uploadPath;
    }

    public ReportData getReportDataById(long id) {
        Optional<ReportData> reportOption = this.reportRepository.findById(id);
        if(reportOption.isPresent()) {
            return reportOption.get();
        }

        return null;
    }

    public ReportData getReportByFileName(String srbFileName) {
        log.info("Getting ReportData for FileName: {}", srbFileName);
        List<ReportData> returnList = this.reportRepository.findByFileName(srbFileName);

        log.info("{} Results returned", returnList.size());
        if (returnList.size() > 1) {
            log.info("More than 1 report found, returning first item");
            log.info("{} Total reports with name: {}", returnList.size(), srbFileName);
        }

        if (!returnList.isEmpty())
            return returnList.get(0);

        return null;
    }

    public ReportData createPdfReport(ReportData reportData, UserAuthentication ua) {
        AppNotification appNotification = new AppNotification();
        if( ua != null ) {
            appNotification.setUserId(ua.getId());

            if (reportData.getCreatedBy() == null || reportData.getCreatedBy().getId() == null) {
                reportData.setCreatedBy(ua);
            }

            reportData.setWorkedBy(ua);
        }

        try {
            this.storePdfFile(reportData);

            Long savedId = reportData.getId();
            log.info("Report Saved");

            appNotification.setMessage("Review Report Ready for Download");
            appNotification.setDateCreated(Instant.now().toString());
            appNotification.setIsNew(true);
            appNotification.setAction("/api/iris/report/download/" + savedId);
            appNotification.setIcon("notifications");
            log.info("Trying to save App Notification");
            appNotificationRepository.save(appNotification);
            log.info("App Notification Saved");
            if (savedId != null) {
                log.info("pdf file ID is {}", savedId);
            } else {
                log.info("pdf file does not have ID");
            }


        }catch(Exception e){
            appNotification.setMessage(String.format("Error Generating Report: %s", e.getMessage()));
            appNotification.setDateCreated(Instant.now().toString());
            appNotification.setIsNew(true);
            appNotification.setIcon("notifications");
            Gson gson = new Gson();
            log.info(gson.toJson(appNotification));
            log.info("Trying to save Erred App Notification");
            appNotificationRepository.save(appNotification);
            log.info("Erred App Notification Saved");
        }

        return reportData;
    }

    public void storePdfFile(ReportData reportData) throws DocumentException, InvalidMessageException, IOException, NoSuchAlgorithmException {
        byte[] createdPdf = new byte[0];

        if (!reportData.isValid()) {
            String errorMessage = String.format("Error in Report Data: %s",
                    StringUtils.joinWith("\n\t", reportData.getErrorMessages()));
            log.info(errorMessage);
        } else {
            log.info("Type: {}", reportData.getResponseType());

            // Creating the PDF
            if (reportData.getResponseType() == ResponseType.SRB) {
                createdPdf = srbReporting.createPdfReport(reportData);
            } else if (reportData.getResponseType() == ResponseType.XML) {
                createdPdf = xmlReporting.createPdfReport(reportData);
            } else if (reportData.getResponseType() == ResponseType.Unknown || reportData.getResponseType() == ResponseType.Image) {
                if(reportData.getProbe().getImageList().size() > 1 ||
                reportData.getCandidates().get(0).getImageList().size() > 1) {
                    log.info("ReponseType: {}", reportData.getResponseType());
                    createdPdf = multiModalReporting.createPdfReport(reportData);
                } else {
                    log.info("ReponseType: {}", reportData.getResponseType());
                    createdPdf = imageReporting.createPdfReport(reportData);
                }
            } else {
                log.info("Unable to parse Response Type {}", reportData.getResponseType());
            }

            // Saving PDF to upload path, if the PDF was created successfully
            if (createdPdf.length != 0) {
                String hash = this.getHash(createdPdf);
                String directory = uploadPath + "/" + hash.substring(0,2) + "/" + hash.substring(2,4);
                String path = String.format("%s/%s.pdf", directory, hash);

                File f = new File(path);
                log.info("File exists:{} path:{}", f.exists(), path);

                File dir = new File(directory);
                if (!dir.mkdirs() && !dir.exists()) {
                    log.error("Unable to create the directory {}", directory);
                    throw new InvalidMessageException("Unable to create folders for upload");
                }

                ReportResponse reportResponse = null;

                if(reportData.getId() != null){
                    reportResponse = this.reportResponseRepository.findReportResponseByReportDataId(reportData.getId());
                }

                if(reportResponse == null){
                    reportResponse = new ReportResponse();
                    reportResponse.setReportDataId(reportData.getId());
                    reportResponse.setUserId(reportData.getUserId());
                }
                reportResponse.setFilePath(path);

                this.reportResponseRepository.save(reportResponse);
                try(FileOutputStream fos = new FileOutputStream(path)) {
                    log.info("Creating pdf file {}", path);
                    fos.write(createdPdf);
                    fos.flush();
                } catch (IOException e) {
                    log.error("Unable to create file {}", reportData.getFileName());
                    throw new InvalidMessageException(e.getMessage());
                }
            }
        }
    }

    public String getHash(byte[] byteArray) throws NoSuchAlgorithmException  {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(byteArray);
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    public void removeAllReports() {
        reportRepository.deleteAll();
    }

    public Long savePerson(Person person) {
        this.personRepository.save(person);
        return person.getId();
    }

    public List<Person> listPersons() {
        return IterableUtils.toList(this.personRepository.findAll());
    }

    public Person getPerson(long id) {
        Optional<Person> personOptional = this.personRepository.findById(id);
        return personOptional.orElse(null);

    }

    public void removeAllPersons() {
        personRepository.deleteAll();
    }

    public void removePerson(long id) {
        this.personRepository.deleteById(id);
    }
}
