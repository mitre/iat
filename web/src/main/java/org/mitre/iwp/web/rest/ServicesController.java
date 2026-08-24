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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mitre.iwp.web.IwpWebProperties;
import org.mitre.iwp.web.jms.ServiceMessageHandler;
import org.mitre.iwp.web.model.AciiResponse;
import org.mitre.iwp.web.model.BiqtResponse;
import org.mitre.iwp.web.model.ServiceCallStatus;
import org.mitre.iwp.web.model.ContactClassifierResponse;
import org.mitre.iwp.web.model.TshepiiResponse;
import org.mitre.iwp.web.model.TshepiiCompareResponse;
import org.mitre.iwp.web.model.IrisAnnotationResponse;
import org.mitre.iwp.web.model.PdmResponse;
import org.mitre.iwp.web.model.DualPDMResponse;
import org.mitre.iwp.web.model.DualPDMCompareResponse;
import org.mitre.iwp.web.model.VersionInfo;
import org.mitre.iwp.web.model.Polygon;
import org.mitre.iwp.web.model.PdmDeformedImageResponse;
import org.mitre.iwp.web.service.IrisService;
import org.mitre.iwp.web.service.EbtsImageRepository;
import org.mitre.iwp.web.service.PdmDeformedImageResponseRepository;
import org.mitre.iwp.web.service.AciiResponseRepository;
import org.mitre.iwp.web.service.BiqtResponseRepository;
import org.mitre.iwp.web.service.ContactClassifierResponseRepository;
import org.mitre.iwp.web.service.DualPDMResponseRepository;
import org.mitre.iwp.web.service.DualPDMCompareResponseRepository;
import org.mitre.iwp.web.service.TshepiiResponseRepository;
import org.mitre.iwp.web.service.TshepiiCompareResponseRepository;
import org.mitre.iwp.web.service.IrisAnnotationResponseRepository;
import org.mitre.iwp.web.service.PdmResponseRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@RestController
public class ServicesController {
    private static final Logger log = LoggerFactory.getLogger(ServicesController.class);
    private final IwpWebProperties iwpWebProperties;
    private final AciiResponseRepository aciiResponseRepository;
    private final BiqtResponseRepository biqtResponseRepository;
    private final ContactClassifierResponseRepository contactClassifierResponseRepository;
    private final TshepiiResponseRepository tshepiiResponseRepository;
    private final DualPDMResponseRepository dualPDMResponseRepository;
    private final TshepiiCompareResponseRepository tshepiiCompareResponseRepository;
    private final DualPDMCompareResponseRepository dualPDMCompareResponseRepository;
    private final IrisAnnotationResponseRepository irisAnnotationResponseRepository;
    private final IrisService irisService;
    private final ServiceMessageHandler serviceMessageHandler;
    private final EbtsImageRepository ebtsImageRepository;
    private final PdmResponseRepository pdmResponseRepository;
    private final PdmDeformedImageResponseRepository pdmDeformedImageResponseRepository;

    private static final String FAILED_STRING = "Failed: ";
    private static final String PNG_STRING = "image/png";
    private static final String RESUBMIT_STRING = "Resubmit flag: {}";
    private static final String ARTEMIS_QUEUE_KEY = "queue=\"";

    public ServicesController(IwpWebProperties iwpWebProperties,
                              AciiResponseRepository aciiResponseRepository, BiqtResponseRepository biqtResponseRepository,
                              ContactClassifierResponseRepository contactClassifierResponseRepository,
                              TshepiiResponseRepository tshepiiResponseRepository,
                              DualPDMResponseRepository dualPDMResponseRepository,
                              TshepiiCompareResponseRepository tshepiiCompareResponseRepository,
                              DualPDMCompareResponseRepository dualPDMCompareResponseRepository,
                              IrisAnnotationResponseRepository irisAnnotationResponseRepository, IrisService irisService,
                              ServiceMessageHandler serviceMessageHandler,
                              EbtsImageRepository ebtsImageRepository,
                              PdmResponseRepository pdmResponseRepository,
                              PdmDeformedImageResponseRepository pdmDeformedImageResponseRepository) {
        this.iwpWebProperties = iwpWebProperties;
        this.aciiResponseRepository = aciiResponseRepository;
        this.biqtResponseRepository = biqtResponseRepository;
        this.contactClassifierResponseRepository = contactClassifierResponseRepository;
        this.tshepiiResponseRepository = tshepiiResponseRepository;
        this.dualPDMResponseRepository = dualPDMResponseRepository;
        this.tshepiiCompareResponseRepository = tshepiiCompareResponseRepository;
        this.dualPDMCompareResponseRepository = dualPDMCompareResponseRepository;
        this.irisAnnotationResponseRepository = irisAnnotationResponseRepository;
        this.irisService = irisService;
        this.serviceMessageHandler = serviceMessageHandler;
        this.ebtsImageRepository = ebtsImageRepository;
        this.pdmResponseRepository = pdmResponseRepository;
        this.pdmDeformedImageResponseRepository = pdmDeformedImageResponseRepository;
    }

    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @GetMapping(value="/api/service/status")
    public ResponseEntity<String> getServiceStatus(){
        Gson gson = new Gson();
        return ResponseEntity.ok().body(gson.toJson(this.serviceMessageHandler.getServicesStatus()));
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/activeMQ/services/status")
    public ResponseEntity<String> getActiveMqServicesStatus() throws IOException {
        try {
            HttpGet get = new HttpGet(iwpWebProperties.getActiveMqHttpAddress());
            String activeMqAuth = iwpWebProperties.getActiveMqUserName() + ":" + iwpWebProperties.getActiveMqPassword();
            get.setHeader("Authorization", "Basic "
                    + Base64.getEncoder().encodeToString(activeMqAuth.getBytes(StandardCharsets.UTF_8)));
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(iwpWebProperties.getActiveMqUserName(),
                    iwpWebProperties.getActiveMqPassword());
            provider.setCredentials(AuthScope.ANY, credentials);
            HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
            HttpResponse response = client.execute(get);

            String responseBody = EntityUtils.toString(response.getEntity());
            JSONArray array = new JSONArray();

            if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                log.info("ActiveMQ status request failed: {}", responseBody);
                return ResponseEntity.ok().body(array.toString());
            }

            JSONObject responseJson = new JSONObject(responseBody);
            JSONObject queues = responseJson.optJSONObject("value");

            if (queues == null) {
                log.info("ActiveMQ status response did not contain a value object");
                return ResponseEntity.ok().body(array.toString());
            }

            int index = 0;
            for (String mbeanName : queues.keySet()) {
                JSONObject queueStats = queues.optJSONObject(mbeanName);
                String name = getArtemisQueueName(mbeanName);

                if (queueStats == null || name == null || !name.startsWith("iris.")) {
                    continue;
                }

                JSONObject queue = new JSONObject();
                queue.put("name", name);
                queue.put("consumerCount", String.valueOf(queueStats.optInt("ConsumerCount", 0)));
                queue.put("enqueueCount", String.valueOf(queueStats.optLong("MessagesAdded", 0)));
                queue.put("dequeueCount", String.valueOf(queueStats.optLong("MessagesAcknowledged", 0)));
                array.put(index++, queue);
            }

            return ResponseEntity.ok().body(array.toString());
        } catch (IOException | JSONException e) {
            log.info(e.getMessage());
        }
        return null;
    }

    private static String getArtemisQueueName(String mbeanName) {
        int queueStart = mbeanName.indexOf(ARTEMIS_QUEUE_KEY);

        if (queueStart == -1) {
            return null;
        }

        int nameStart = queueStart + ARTEMIS_QUEUE_KEY.length();
        int nameEnd = mbeanName.indexOf("\"", nameStart);

        if (nameEnd == -1) {
            return null;
        }

        return mbeanName.substring(nameStart, nameEnd);
    }

    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @GetMapping(value="/api/service/versions")
    public ResponseEntity<String> getVersionInfo() {
        VersionInfo info = new VersionInfo();
        info.setIwpVersion(this.iwpWebProperties.getIwpBackendVersion());
        info.setUiVersion(this.iwpWebProperties.getIwpFrontendVersion());

        info.getServiceVersions().put("biqt", this.iwpWebProperties.getBiqtServiceVersion());
        info.getServiceVersions().put("acii", this.iwpWebProperties.getAciiServiceVersion());
        info.getServiceVersions().put("tshepii", this.iwpWebProperties.getTshepiiServiceVersion());
        info.getServiceVersions().put("annotation", this.iwpWebProperties.getIrisAnnotationServiceVersion());
        info.getServiceVersions().put("pdm", this.iwpWebProperties.getPdmServiceVersion());

        Gson gson = new Gson();
        return ResponseEntity.ok().body(gson.toJson(info));
    }

    // ACII
    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @PostMapping(value={"/api/service/acii/snapshot/{ebtsImageId}"})
    public ResponseEntity<Long> requestAciiSnapshot(@PathVariable("ebtsImageId") Long ebtsImageId,  @RequestBody String imageData){
        log.info("Requesting ACII Snapshot");
        Long snapshotImageId = -1l;
   
        try{
            byte[] imageBytes = Base64.getDecoder().decode(imageData);

            snapshotImageId = this.irisService.uploadImage(PNG_STRING, imageBytes, "", false);
            
        } catch (Exception e){
            log.info(e.getMessage());
            log.error(FAILED_STRING, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(-1l);
        }

        Long responseId = this.serviceMessageHandler.processAcii(this.ebtsImageRepository.getReferenceById(snapshotImageId), ebtsImageId);

        return ResponseEntity.ok().body(responseId);
    }

    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @GetMapping(value="/api/service/acii/status/{ebtsImageId}/{modifiedImageId}")
    public ResponseEntity<ServiceCallStatus> getAciiResponseStatus(@PathVariable("ebtsImageId") Long ebtsImageId, @PathVariable("modifiedImageId") Long modifiedImageId) {
        Long queryImageId = ebtsImageId;
        if (modifiedImageId != -1) {
            queryImageId = modifiedImageId;
        }

        AciiResponse response = this.aciiResponseRepository.findByImageId(queryImageId);

        if (response == null) {
            return ResponseEntity.ok(ServiceCallStatus.NotSent);
        }

        this.aciiResponseRepository.save(response);

        return ResponseEntity.ok().body(response.getStatus());
    }

    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @GetMapping(value={"/api/service/acii/{imageId}", "/api/service/acii/{imageId}/{resubmit}"})
    public ResponseEntity<AciiResponse> getAciiResponse(@PathVariable("imageId") Long imageId,
            @PathVariable("resubmit") Optional<Boolean> resubmitFlag) {
        AciiResponse response = this.aciiResponseRepository.findByImageId(imageId);
        AciiResponse returnResponse = response;

        log.debug(RESUBMIT_STRING, resubmitFlag);

        if (resubmitFlag.isPresent() && Boolean.TRUE.equals(resubmitFlag.get())) {
            this.aciiResponseRepository.save(response);
        }

        return ResponseEntity.ok().body(returnResponse);
    }

    // BIQT & Contact
    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @PostMapping(value={"/api/service/biqtContact/snapshot/{ebtsImageId}"})
    public ResponseEntity<Long> requestBiqtContactSnapshot(@PathVariable("ebtsImageId") Long ebtsImageId, @RequestBody String imageData){
        log.info("Requesting BIQT and Contact Snapshot");
        Long snapshotImageId = -1l;
   
        try{
            byte[] imageBytes = Base64.getDecoder().decode(imageData);

            snapshotImageId = this.irisService.uploadImage(PNG_STRING, imageBytes, "", false);
            
        } catch (Exception e){
            log.info(e.getMessage());
            log.error(FAILED_STRING, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(-1l);
        }

        Long responseId = this.serviceMessageHandler.processBiqtContact(this.ebtsImageRepository.getReferenceById(snapshotImageId), ebtsImageId);

        return ResponseEntity.ok().body(responseId);
    }

    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @GetMapping(value="/api/service/biqtContact/status/{ebtsImageId}/{modifiedImageId}")
    public ResponseEntity<ServiceCallStatus[]> getBiqtContactResponseStatus(@PathVariable("ebtsImageId") Long ebtsImageId, @PathVariable("modifiedImageId") Long modifiedImageId){
        ServiceCallStatus biqtStatus = this.getBiqtResponseStatus(ebtsImageId, modifiedImageId);
        ServiceCallStatus contactStatus = this.getContactResponseStatus(ebtsImageId, modifiedImageId);
        
        ServiceCallStatus[] response = {biqtStatus, contactStatus};
        return ResponseEntity.ok().body(response);
    }

    private ServiceCallStatus getBiqtResponseStatus(Long ebtsImageId, Long modifiedImageId){
        Long queryImageId = ebtsImageId;
        if (modifiedImageId != -1) {
            queryImageId = modifiedImageId;
        }

        BiqtResponse response = this.biqtResponseRepository.findByImageId(queryImageId);
        
        if (response == null) {
            return ServiceCallStatus.NotSent;
        }

        this.biqtResponseRepository.save(response);

        return response.getStatus();
    }

    private ServiceCallStatus getContactResponseStatus(Long ebtsImageId, Long modifiedImageId) {
        Long queryImageId = ebtsImageId;
        if (modifiedImageId != -1) {
            queryImageId = modifiedImageId;
        }

        ContactClassifierResponse response = this.contactClassifierResponseRepository.findByImageId(queryImageId);

        if (response == null) {
            return ServiceCallStatus.NotSent;
        }

        this.contactClassifierResponseRepository.save(response);

        return response.getStatus();
    }

    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @GetMapping(value={"/api/service/biqtContact/biqt/{imageId}","/api/service/biqtContact/biqt/{imageId}/{resubmit}"})
    public ResponseEntity<BiqtResponse> getBiqtResponse(@PathVariable("imageId") Long imageId, @PathVariable("resubmit") Optional<Boolean> resubmitFlag){
        BiqtResponse response = this.biqtResponseRepository.findByImageId(imageId);
        
        log.debug(RESUBMIT_STRING, resubmitFlag);

        if (resubmitFlag.isPresent() && Boolean.TRUE.equals(resubmitFlag.get())) {
            this.biqtResponseRepository.save(response);
        }

        return ResponseEntity.ok().body(response);
    }

    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @GetMapping(value={"/api/service/biqtContact/contact/{imageId}","/api/service/biqtContact/contact/{imageId}/{resubmit}"})
    public ResponseEntity<ContactClassifierResponse> getContactResponse(@PathVariable("imageId") Long imageId, @PathVariable("resubmit") Optional<Boolean> resubmitFlag){
        ContactClassifierResponse response = this.contactClassifierResponseRepository.findByImageId(imageId);
        if (resubmitFlag.isPresent() && Boolean.TRUE.equals(resubmitFlag.get())) {
            this.contactClassifierResponseRepository.save(response);
        }
        return ResponseEntity.ok().body(response);
    }

    // Iris Annotation
    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @PostMapping(value={"/api/service/annotation/snapshot/{ebtsImageId}"})
    public ResponseEntity<Long> requestIrisAnnotationSnapshot(@PathVariable("ebtsImageId") Long ebtsImageId, @RequestBody String imageData){
        log.info("Requesting Annotation Snapshot");
        Long snapshotImageId = -1l;
   
        try{
            byte[] imageBytes = Base64.getDecoder().decode(imageData);

            snapshotImageId = this.irisService.uploadImage(PNG_STRING, imageBytes, "", false);
            
        } catch (Exception e){
            log.info(e.getMessage());
            log.error(FAILED_STRING, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(-1l);
        }

        Long responseId = this.serviceMessageHandler.processIrisAnnotation(this.ebtsImageRepository.getReferenceById(snapshotImageId), ebtsImageId);

        return ResponseEntity.ok().body(responseId);
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/service/annotation/status/{ebtsImageId}/{modifiedImageId}")
    public ResponseEntity<ServiceCallStatus> getIrisAnnotationResponseStatus(@PathVariable("ebtsImageId") Long ebtsImageId, @PathVariable("modifiedImageId") Long modifiedImageId) {
        Long queryImageId = ebtsImageId;
        if (modifiedImageId != -1) {
            queryImageId = modifiedImageId;
        }

        IrisAnnotationResponse response = this.irisAnnotationResponseRepository.findByImageId(queryImageId);

        if (response == null) {
            return ResponseEntity.ok(ServiceCallStatus.NotSent);
        }

        this.irisAnnotationResponseRepository.save(response);

        return ResponseEntity.ok(response.getStatus());
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = {"/api/service/annotation/{imageId}", "/api/service/annotation/{imageId}/{resubmit}"})
    public ResponseEntity<String> getIrisAnnotationResponse(@PathVariable("imageId") Long imageId,
            @PathVariable("resubmit") Optional<Boolean> resubmitFlag) {

        IrisAnnotationResponse response = this.irisAnnotationResponseRepository.findByImageId(imageId);
        IrisAnnotationResponse returnResponse = response;

        log.debug(RESUBMIT_STRING, resubmitFlag);

        if (resubmitFlag.isPresent() && Boolean.TRUE.equals(resubmitFlag.get())) {
            this.irisAnnotationResponseRepository.save(response);
        }

        if (returnResponse.getStatus() == ServiceCallStatus.Received) {
            returnResponse.buildIrisAnnotationReply();
        }

        for (Polygon poly : returnResponse.getPolygonList()) {
            if (this.iwpWebProperties.getAnnotations().containsKey(poly.code)) {
                poly.setName(this.iwpWebProperties.getAnnotations().get(poly.code));
                poly.setColor(this.iwpWebProperties.getAnnotationsColorHex().get(poly.code));
            }
        }

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        return ResponseEntity.ok(gson.toJson(returnResponse));
    }

    // Tshepii
    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @PostMapping(value="/api/service/tshepii/snapshot/{probeEbtsImageId}/{candidateEbtsImageId}")
    public ResponseEntity<List<Long>> requestTshepiiSnapshot(@PathVariable("probeEbtsImageId") Long probeEbtsImageId, @PathVariable("candidateEbtsImageId") Long candidateEbtsImageId, @RequestBody List<String> imageData){
        log.info("Requesting Tshepii Snapshot");
        Long probeSnapshotImageId = -1l;
        Long candidateSnapshotImageId = -1l;

        // Creating and setting the return array as a list of -1
        List<Long> returnArray = new ArrayList<>();
        returnArray.add(-1l);
        returnArray.add(-1l);

        String probeImageData = imageData.get(0);
        String candidateImageData = imageData.get(1);
   
        try{
            byte[] probeImageBytes = Base64.getDecoder().decode(probeImageData);
            byte[] candidateImageBytes = Base64.getDecoder().decode(candidateImageData);

            probeSnapshotImageId = this.irisService.uploadImage(PNG_STRING, probeImageBytes, "", false);
            candidateSnapshotImageId = this.irisService.uploadImage(PNG_STRING, candidateImageBytes, "", false);
            
        } catch (Exception e){
            log.info(e.getMessage());
            log.error(FAILED_STRING, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnArray);
        }

        Long probeResponseId = this.serviceMessageHandler.processTshepii(this.ebtsImageRepository.getReferenceById(probeSnapshotImageId), probeEbtsImageId);
        Long candidateResponseId = this.serviceMessageHandler.processTshepii(this.ebtsImageRepository.getReferenceById(candidateSnapshotImageId), candidateEbtsImageId);

        returnArray.set(0, probeResponseId);
        returnArray.set(1, candidateResponseId);

        return ResponseEntity.ok().body(returnArray);
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/service/tshepii/status/{probeEbtsImageId}/{candidateEbtsImageId}/{probeModifiedImageId}/{candidateModifiedImageId}")
    public ResponseEntity<ServiceCallStatus> getTshepiiResponseStatus(@PathVariable("probeEbtsImageId") Long probeEbtsImageId, @PathVariable("candidateEbtsImageId") Long candidateEbtsImageId, @PathVariable("probeModifiedImageId") Long probeModifiedImageId, @PathVariable("candidateModifiedImageId") Long candidateModifiedImageId) {

        Long queryProbeImageId = probeEbtsImageId;
        if (probeModifiedImageId != -1) {
            queryProbeImageId = probeModifiedImageId;
        }

        Long queryCandidateImageId = candidateEbtsImageId;
        if (candidateModifiedImageId != -1) {
            queryCandidateImageId = candidateModifiedImageId;
        }

        TshepiiResponse responseProbe = this.tshepiiResponseRepository.findByImageId(queryProbeImageId);
        TshepiiResponse responseCand = this.tshepiiResponseRepository.findByImageId(queryCandidateImageId);

        if (responseProbe == null || responseCand == null) {
            return ResponseEntity.ok(ServiceCallStatus.NotSent);
        }

        responseProbe.setEbtsImageId(probeEbtsImageId);
        this.tshepiiResponseRepository.save(responseProbe);

        responseCand.setEbtsImageId(candidateEbtsImageId);
        this.tshepiiResponseRepository.save(responseCand);

        if (responseProbe.getStatus() == ServiceCallStatus.Received
                && responseCand.getStatus() == ServiceCallStatus.Received) {
            return ResponseEntity.ok().body(ServiceCallStatus.Received);
        }

        if (responseProbe.getStatus() == ServiceCallStatus.Error || responseCand.getStatus() == ServiceCallStatus.Error) {
            return ResponseEntity.ok().body(ServiceCallStatus.Error);
        }

        if (responseProbe.getStatus() == ServiceCallStatus.NotSent
                || responseCand.getStatus() == ServiceCallStatus.NotSent) {
            return ResponseEntity.ok().body(ServiceCallStatus.NotSent);
        }

        return ResponseEntity.ok().body(ServiceCallStatus.Pending);
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/service/tshepii/compare/status/{probeEbtsImageId}/{candidateEbtsImageId}/{probeModifiedImageId}/{candidateModifiedImageId}")
    public ResponseEntity<ServiceCallStatus> getTshepiiCompareResponseStatus(@PathVariable("probeEbtsImageId") Long probeEbtsImageId, @PathVariable("candidateEbtsImageId") Long candidateEbtsImageId, @PathVariable("probeModifiedImageId") Long probeModifiedImageId, @PathVariable("candidateModifiedImageId") Long candidateModifiedImageId) {

        Long queryProbeImageId = probeEbtsImageId;
        if (probeModifiedImageId != -1) {
            queryProbeImageId = probeModifiedImageId;
        }

        Long queryCandidateImageId = candidateEbtsImageId;
        if (candidateModifiedImageId != -1) {
            queryCandidateImageId = candidateModifiedImageId;
        }

        TshepiiCompareResponse response = this.tshepiiCompareResponseRepository.findByImageIdAndCandidateImageId(queryProbeImageId, queryCandidateImageId);

        if (response == null) {
            return ResponseEntity.ok().body(ServiceCallStatus.NotSent);
        }

        response.setEbtsImageId(probeEbtsImageId);
        response.setCandidateEbtsImageId(candidateEbtsImageId);
        this.tshepiiCompareResponseRepository.save(response);

        return ResponseEntity.ok(response.getStatus());
    }

    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @GetMapping(value={"/api/service/tshepii/compare/{probeId}/{candidateId}", "/api/service/tshepii/compare/{probeId}/{candidateId}/{resubmit}"})
    public ResponseEntity<String> getTshepiiCompareResponse(@PathVariable("probeId") Long probeId, @PathVariable("candidateId") Long candidateId,
                    @PathVariable("resubmit") Optional<Boolean> resubmitFlag) {

        log.info("ComparisonResponse Request Received for Probe: {} and Candidate: {}", probeId, candidateId);
        TshepiiCompareResponse response = this.tshepiiCompareResponseRepository.findByImageIdAndCandidateImageId(probeId, candidateId);

        if(response == null) {
            this.irisService.createTshepiiCompareRequest(probeId, candidateId);
            response = new TshepiiCompareResponse();
            // // Can info - this is not the actual object stored in the DB
            // // May need to change if we screw something up in communication between UI and backend
            response.setStatus(ServiceCallStatus.Pending);
            response.setImageId(probeId);
            response.setCandidateImageId(candidateId);
        }

        if(response.getStatus() == ServiceCallStatus.Received){
            boolean responseBuilt = response.buildFromServiceResponse();
            if(!responseBuilt){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating Compare Response");
            }
        }

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return ResponseEntity.ok(gson.toJson(response));
    }

    // DUAL PDM
    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/service/dualpdm/status/{probeEbtsImageId}/{candidateEbtsImageId}/{probeModifiedImageId}/{candidateModifiedImageId}")
    public ResponseEntity<ServiceCallStatus> getDualPDMResponseStatus(@PathVariable("probeEbtsImageId") Long probeEbtsImageId, @PathVariable("candidateEbtsImageId") Long candidateEbtsImageId, @PathVariable("probeModifiedImageId") Long probeModifiedImageId, @PathVariable("candidateModifiedImageId") Long candidateModifiedImageId) {

        Long queryProbeImageId = probeEbtsImageId;
        if (probeModifiedImageId != -1) {
            queryProbeImageId = probeModifiedImageId;
        }

        Long queryCandidateImageId = candidateEbtsImageId;
        if (candidateModifiedImageId != -1) {
            queryCandidateImageId = candidateModifiedImageId;
        }

        DualPDMResponse responseProbe = this.dualPDMResponseRepository.findByImageId(queryProbeImageId);
        DualPDMResponse responseCandidate = this.dualPDMResponseRepository.findByImageId(queryCandidateImageId);

        // Probe AND/OR Candidate have not been saved yet
        if (responseProbe == null || responseCandidate == null) {
            log.info("Probe AND/OR Candidate have not been saved yet");
            return ResponseEntity.ok(ServiceCallStatus.NotSent);
        }

        responseProbe.setEbtsImageId(probeEbtsImageId);
        this.dualPDMResponseRepository.save(responseProbe);

        responseCandidate.setEbtsImageId(candidateEbtsImageId);
        this.dualPDMResponseRepository.save(responseCandidate);

        // Probe AND Candidate have been received
        if (responseProbe.getStatus() == ServiceCallStatus.Received && responseCandidate.getStatus() == ServiceCallStatus.Received) {
            log.info("Both Probe AND Candidate have been received individually and sending the request for the comparison.");
            if (this.dualPDMCompareResponseRepository.findByProbeIdAndCandidateId(queryProbeImageId, queryCandidateImageId) == null) {
                this.serviceMessageHandler.requestDualPDMComparison(queryProbeImageId, queryCandidateImageId); // Create a new request
            }
            return ResponseEntity.ok().body(ServiceCallStatus.Received);
        }

        // Probe AND/OR Candidate have caused an error
        if (responseProbe.getStatus() == ServiceCallStatus.Error || responseCandidate.getStatus() == ServiceCallStatus.Error) {
            log.info("Probe AND/OR Candidate have caused an error");
            return ResponseEntity.ok().body(ServiceCallStatus.Error);
        }

        // Not NULL but Probe AND/OR Candidate have not been sent
        if (responseProbe.getStatus() == ServiceCallStatus.NotSent || responseCandidate.getStatus() == ServiceCallStatus.NotSent) {
            log.info("NOT NULL but Probe//Candidate are NOT SENT");
            return ResponseEntity.ok().body(ServiceCallStatus.NotSent);
        }

        return ResponseEntity.ok().body(ServiceCallStatus.Pending);
    }


    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/service/dualpdm/compare/status/{probeEbtsImageId}/{candidateEbtsImageId}/{probeModifiedImageId}/{candidateModifiedImageId}")
    public ResponseEntity<ServiceCallStatus> getDualPDMCompareResponseStatus(@PathVariable("probeEbtsImageId") Long probeEbtsImageId, @PathVariable("candidateEbtsImageId") Long candidateEbtsImageId, @PathVariable("probeModifiedImageId") Long probeModifiedImageId, @PathVariable("candidateModifiedImageId") Long candidateModifiedImageId) {
        Long queryProbeImageId = probeEbtsImageId;
        if (probeModifiedImageId != -1) {
            queryProbeImageId = probeModifiedImageId;
        }

        Long queryCandidateImageId = candidateEbtsImageId;
        if (candidateModifiedImageId != -1) {
            queryCandidateImageId = candidateModifiedImageId;
        }

        DualPDMCompareResponse response = this.dualPDMCompareResponseRepository.findByProbeIdAndCandidateId(queryProbeImageId, queryCandidateImageId);

        if (response == null) {
            return ResponseEntity.ok().body(ServiceCallStatus.NotSent);
        }

        response.setEbtsImageId(probeEbtsImageId);
        response.setCandidateEbtsImageId(candidateEbtsImageId);
        this.dualPDMCompareResponseRepository.save(response);

        return ResponseEntity.ok(response.getStatus());
    }

    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @GetMapping(value={"/api/service/dualpdm/compare/{probeId}/{candidateId}", "/api/service/dualpdm/compare/{probeId}/{candidateId}/{resubmit}"})
    public ResponseEntity<String> getDualPDMCompareResponse(@PathVariable("probeId") Long probeId, @PathVariable("candidateId") Long candidateId,
                    @PathVariable("resubmit") Optional<Boolean> resubmitFlag) {

        log.info("Dual PDM ComparisonResponse Request Received for Probe: {} and Candidate: {}", probeId, candidateId);

        DualPDMCompareResponse response = this.dualPDMCompareResponseRepository.findByProbeIdAndCandidateId(probeId, candidateId);
        
        // Handle the case where no response exists in the database
        if (response == null) {
            log.info("No existing Dual PDM comparison response found. Creating a new request.");
            // Create a new request and send JMS Message
            this.serviceMessageHandler.requestDualPDMComparison(probeId, candidateId);
            response = new DualPDMCompareResponse();
            response.setStatus(ServiceCallStatus.Pending);
            response.setProbeId(probeId);
            response.setCandidateId(candidateId);
        }

        // If the response status is "Received", process the response further
        if (response.getStatus() == ServiceCallStatus.Received) {
            boolean responseBuilt = response.buildFromServiceResponse(); // Build the response from the service data
            if (!responseBuilt) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Dual PDM Error generating Compare Response");
            }
        }

        // Serialize the response to JSON and return it
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return ResponseEntity.ok(gson.toJson(response));
    }

    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @GetMapping(value="/api/service/dualpdm/response/{imageId}")
    public ResponseEntity<String> getDualPDMResponseImage(@PathVariable("imageId") String imageId) {
        long imgId = Long.parseLong(imageId);
        DualPDMResponse response = this.dualPDMResponseRepository.findByImageId(imgId);
    
    
        log.info("DualPDM Response Image Request received for imageId {} of size {}", response.getImageId(), response.getByteSize());
    
        Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
        return ResponseEntity.ok(gson.toJson(response));
    }

    // PDM
    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @PostMapping(value={"/api/service/pdm/snapshot/{ebtsImageId}"})
    public ResponseEntity<Long> requestPdmSnapshot(@PathVariable("ebtsImageId") Long ebtsImageId, @RequestBody String imageData){
        log.info("Requesting PDM Snapshot");
        Long snapshotImageId = -1l;
   
        try{
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            
            snapshotImageId = this.irisService.uploadImage(PNG_STRING, imageBytes, "", false);
            log.info("Requesting PDM Snapshot id {}", snapshotImageId);

            
        } catch (Exception e){
            log.info(e.getMessage());
            log.error(FAILED_STRING, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(-1l);
        }

        Long responseId = this.serviceMessageHandler.processPdm(this.ebtsImageRepository.getReferenceById(snapshotImageId), ebtsImageId);

        return ResponseEntity.ok().body(responseId);
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/service/pdm/status/{ebtsImageId}/{modifiedImageId}")
    public ResponseEntity<ServiceCallStatus> getPdmResponseStatus(@PathVariable("ebtsImageId") Long ebtsImageId, @PathVariable("modifiedImageId") Long modifiedImageId) {
        Long queryImageId = ebtsImageId;
        if (modifiedImageId != -1) {
            queryImageId = modifiedImageId;
        }

        PdmResponse response = this.pdmResponseRepository.findByImageId(queryImageId);


        if (response == null) {
            return ResponseEntity.ok(ServiceCallStatus.NotSent);
        }

        this.pdmResponseRepository.save(response);

        if (response.getStatus() == ServiceCallStatus.Received) {
            return ResponseEntity.ok().body(ServiceCallStatus.Received);
        }

        if (response.getStatus() == ServiceCallStatus.Error) {
            return ResponseEntity.ok().body(ServiceCallStatus.Error);
        }

        if (response.getStatus() == ServiceCallStatus.NotSent) {
            return ResponseEntity.ok().body(ServiceCallStatus.NotSent);
        }

        return ResponseEntity.ok().body(ServiceCallStatus.Pending);
    }

    @Secured({"ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR"})
    @GetMapping(value="/api/service/pdm/response/{deformedId}")
    public ResponseEntity<String> getPdmResponse(@PathVariable("deformedId") String deformedId) {
        PdmDeformedImageResponse response = this.pdmDeformedImageResponseRepository.findByDeformedId(deformedId);

        log.info("PdmResponse Request received for deformedId: {}", response.getDeformedId());

        Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
        return ResponseEntity.ok(gson.toJson(response));
    }
}
