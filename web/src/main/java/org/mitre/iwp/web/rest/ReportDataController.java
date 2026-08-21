/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.rest;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.mitre.iwp.web.IwpWebProperties;
import org.mitre.iwp.web.dto.ImageMessageMapper;
import org.mitre.iwp.web.dto.ReportDataDTO;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.model.*;
import org.mitre.iwp.web.security.IwpAuthService;
import org.mitre.iwp.web.service.IrisService;
import org.mitre.iwp.web.service.ReportDataRepository;
import org.mitre.iwp.web.service.ReportService;
import org.mitre.iwp.web.service.ReportDataService;
import org.mitre.iwp.web.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@RestController
public class ReportDataController {
    private static final Logger logger = LoggerFactory.getLogger(ReportDataController.class);

    private final ReportService reportService;

    private final IrisService irisService;

    private final ReportDataRepository reportDataRepository;

    private final ReportResponseRepository reportResponseRepository;

    private final IwpAuthService authService;

    private final ReportDataService reportDataService;

    private final ImageMessageMapper imageMessageMapper;

    private final IwpWebProperties iwpWebProperties;

    private static final String ATTACH_NAME_STRING = "attachment; filename=\"";

    public ReportDataController(ReportService reportService, IrisService irisService,
            ReportDataRepository reportDataRepository,
            ReportResponseRepository reportResponseRepository,
            ReportDataService reportDataService, IwpAuthService authService,
            ImageMessageMapper imageMessageMapper,
            IwpWebProperties iwpWebProperties) {
        this.reportService = reportService;
        this.irisService = irisService;
        this.reportDataRepository = reportDataRepository;
        this.authService = authService;
        this.reportResponseRepository = reportResponseRepository;
        this.reportDataService = reportDataService;
        this.imageMessageMapper = imageMessageMapper;
        this.iwpWebProperties = iwpWebProperties;
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @PostMapping(value = "/api/iris/report")
    public ResponseEntity<?> generatePdfReport(@RequestBody ReportDataDTO reportData, Principal principal) {
        UserAuthentication ua;
        Gson gson = new Gson();

        ReportData tempData;
        
        // Non-EBTS comparisons do not save automatically
        if (reportData.getId() == null) {
            ResponseEntity<?> responseEntity = saveReport(reportData, principal);
            // Check to see if save was successful
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                tempData = (ReportData) responseEntity.getBody();
            } else {
                return responseEntity;
            }
        } else {
            tempData = this.imageMessageMapper.toReportData(reportData);
        }

        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        if (tempData == null) {
            tempData = new ReportData();
        }

        try {
            if (reportData.getCreatedBy() == null || reportData.getCreatedBy().getId() == null) 
               tempData.setCreatedBy(ua);
            
            tempData.setWorkedBy(ua);

            ReportData returnReport = reportService.createPdfReport(tempData, ua);

            return ResponseEntity.ok().body(returnReport);
        } catch(Exception e){
            return ResponseEntity.badRequest().body(gson.toJson(e.getMessage()));
        }
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @GetMapping(value="/api/iris/report/download/{reportId}")
    public ResponseEntity<?> getPdfReport(@PathVariable("reportId") long reportId) {

        logger.info("/api/iris/report/download?id={}", reportId);
        try{
            String filePath = this.findReportById(reportId);
            try(FileInputStream fis = new FileInputStream(filePath)) {

                byte[] doc = new byte[fis.available()];
                if(fis.read(doc) <= 0){
                    throw new IOException(String.format("Unable to read file %s", filePath));
                }

                Resource file = new ByteArrayResource(doc);

                if (doc.length == 0) {
                    return ResponseEntity.badRequest().body("Error Pulling Report Info");
                }

                return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, ATTACH_NAME_STRING + new File(filePath).getName() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(file);
            }

        } catch (Exception docEx) {
            logError(docEx);
            return ResponseEntity.badRequest().body(docEx.getMessage());
        }
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @PostMapping(value = "/api/iris/report/save")
    public ResponseEntity<?> saveReport(@RequestBody ReportDataDTO reportData, Principal principal) {
        if (Boolean.TRUE.equals(reportData.getAllowNullRequester())) {
            reportData.setIsFinished(true);
            if (reportData.getCaseInformation().getRequestedBy().getId() == null){
                reportData.getCaseInformation().setRequestedBy(null);
            }
        }

        ReportData tempData = this.imageMessageMapper.toReportData(reportData);

        UserAuthentication ua;

        try {
            ua = authService.getCurrentUser(principal);
            return ResponseEntity.ok().body(this.saveReportData(tempData, ua));
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.SEE_OTHER);
        }

    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @GetMapping(value = "/api/iris/report/find/{reportId}")
    public ResponseEntity<?> getReport(@PathVariable("reportId") long reportId) {
        Optional<ReportData> reportOption = reportDataRepository.findById(reportId);
        if (reportOption.isPresent()) {
            return ResponseEntity.ok().body(reportOption.get());
        }
        return ResponseEntity.notFound().build();
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @GetMapping(value = "/api/iris/report/count")
    public ResponseEntity<?> countReports(Principal principal) {
        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(this.reportDataRepository.countAllByCreatedByOrWorkedBy(ua, ua), HttpStatus.OK);
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @GetMapping(value = "/api/iris/adjudication")
    public ResponseEntity<?> getAdjudicationKeys(Principal principal) {
        return new ResponseEntity<>(this.iwpWebProperties.getAdjudicationKeys(), HttpStatus.OK);
    }


    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @GetMapping(value = "/api/iris/report/list")
    public ResponseEntity<?> listReports(@RequestParam(value = "start", defaultValue = "0") Integer start,
            @RequestParam(value = "limit", defaultValue = "-1") Integer limit,
            @RequestParam(value = "sort", defaultValue = "caseInformation.comparisonDate") String sortColumn,
            @RequestParam(value = "direction", defaultValue = "desc") String direction, Principal principal) {

        Sort.Direction sortDirection = Sort.Direction.DESC;
        if (direction.equalsIgnoreCase("asc")) {
            sortDirection = Sort.Direction.ASC;
        }

        if (limit == -1) {
            limit = Integer.MAX_VALUE;
        }

        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(this.reportDataRepository.findByCreatedByOrWorkedBy(ua, ua,
                PageRequest.of(start, limit, Sort.by(sortDirection, sortColumn))), HttpStatus.OK);
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @GetMapping(value = "/api/iris/report/csv/download")
    public ResponseEntity<?> getReviewCsvReport(@RequestParam("id") String ids, Principal principal) {

        logger.info("/api/iris/report/download?id={}", ids);
        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        String[] reviewIds = ids.split(",");

        List<List<String>> outputList = new ArrayList<>();

        String[] header = { "Filename", "FileType", "Finished", "Conducted By", "Comparison Date", "Probe UCN",
                "Probe Notes" };

        int maxCandidateCount = 0;
        List<String> headerList = new ArrayList<>(Arrays.asList(header));
        try {

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (String stringId : reviewIds) {
                long id = Long.parseLong(stringId);
                ReportData rd = this.reportService.getReportDataById(id);
                Probe probe = rd.getProbe();

                List<Candidate> candidateList = rd.getCandidates();

                if (maxCandidateCount < candidateList.size()) {
                    maxCandidateCount = candidateList.size();
                }

                CaseInformation caseInf = rd.getCaseInformation();
                if (probe != null && caseInf != null) {
                    List<String> dataList = new ArrayList<>();

                    dataList.add(rd.getFileName());
                    dataList.add(rd.getResponseType().toString());
                    dataList.add(rd.getIsFinished().toString());
                    dataList.add(rd.getWorkedBy().getProfile().getName());
                    dataList.add(dateFormat.format(caseInf.getComparisonDate()));
                    dataList.add(probe.getSubjectIdentifier());
                    dataList.add(StringUtils.join(pullImageNotes(probe.getImageList())));

                    if (candidateList != null) {
                        for (Candidate c : candidateList) {
                            dataList.add(c.getSubjectIdentifier());
                            if (c.getAdjudicationResults() != null) {
                                dataList.add(c.getAdjudicationResults());
                            } else {
                                dataList.add("No Decision Made");
                            }
                            dataList.add(StringUtils.join(pullCandidateNotes(c)));
                        }
                    }

                    outputList.add(dataList);
                } else {
                    if (probe == null) {
                        logger.info("Probe is null");
                    }

                    if (caseInf == null) {
                        logger.info("Case Info is null");
                    }

                    if (candidateList.isEmpty()) {
                        logger.info("Candidate List is Empty");
                    }
                }
            }

            ByteArrayOutputStream csvFile = new ByteArrayOutputStream();

            OutputStreamWriter outputWriter = new OutputStreamWriter(csvFile);
            CSVWriter writer = new CSVWriter(outputWriter);

            for (int i = 0; i < maxCandidateCount; i++) {
                headerList.add(String.format("Candidate %d", i + 1));
                headerList.add(String.format("Decision %d", i + 1));
                headerList.add(String.format("Candidate Notes %d", i + 1));
            }
            writer.writeNext(headerList.toArray(new String[0]));

            for (List<String> line : outputList) {
                writer.writeNext(line.toArray(new String[0]));
            }

            writer.close();
            String fileName = String.format("%s.%s.csv", ua.getUsername(),
                    new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(new Date()));
            ByteArrayResource resourceFile = new ByteArrayResource(csvFile.toByteArray());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, ATTACH_NAME_STRING + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resourceFile);
        } catch (IOException ioe) {
            logError(ioe);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ioe.getMessage());
        }
    }

    private List<String> pullImageNotes(List<ImageData> imageData) {
        List<String> noteList = new ArrayList<>();

        for (ImageData id : imageData) {
            if (id.getImageNotes() != null) {
                noteList.add(id.getImageNotes());
            }
        }

        return noteList;
    }

    private List<String> pullCandidateNotes(Candidate candidate) {
        List<String> noteList = new ArrayList<>();
        if (candidate.getNote() != null) {
            noteList.add(candidate.getNote());
        }

        noteList.addAll(pullImageNotes(candidate.getImageList()));

        return noteList;
    }

    private void logError(Exception exc) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exc.printStackTrace(pw);
        logger.error("Error: {}", sw);
    }

    private ReportData saveReportData(ReportData reportData, UserAuthentication ua) throws NoSuchAlgorithmException, InvalidMessageException {
        // Check unrolled image id/bytes to see if they need stored
        // JPA transitions modified how this needs handled
        for(ImageData imageData : reportData.getProbe().getImageList()){
            this.checkSaveUnroll(imageData);
        }

        for( Candidate candidate : reportData.getCandidates()){
            for(ImageData imageData : candidate.getImageList()){
                this.checkSaveUnroll(imageData);
            }
        }

        if (reportData.getCreatedBy() == null || reportData.getCreatedBy().getId() == null)
            reportData.setCreatedBy(ua);
        reportData.setWorkedBy(ua);

        reportData.getCaseInformation().setComparisonDate(Instant.now().toEpochMilli());

        return reportDataService.saveReportData(reportData);
    }// end of saveReport

    private void checkSaveUnroll(ImageData imageData) throws NoSuchAlgorithmException, InvalidMessageException {
        if(imageData.getUnrollImageId() == null
                && imageData.getUnrollImageData() != null){
            imageData.setUnrollImageId(this.irisService.uploadImage("image/png", imageData.getUnrollImageData()));
        }
    }

    private String findReportById(Long reportId) throws InvalidMessageException {
        // Look for the report by Id
        ReportData reportData = reportDataRepository.findById(reportId).orElse(null);
        if (reportData == null) {
            throw new InvalidMessageException("Report cannot be found under this id.");
        }

        // Pull saved path from responseRepository
        ReportResponse reportResponse = reportResponseRepository.findReportResponseByReportDataId(reportId);

        // Create the resulting PDF
        logger.info("Id passed in is {}", reportId);
        
        return reportResponse.getFilePath();
    }
}
