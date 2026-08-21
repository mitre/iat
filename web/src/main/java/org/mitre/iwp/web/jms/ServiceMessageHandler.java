/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.jms;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import org.mitre.iwp.buffers.Iwp;
import org.mitre.iwp.buffers.Pdm;
import org.mitre.iwp.buffers.Tshepii;
import org.mitre.iwp.web.IwpWebProperties;
import org.mitre.iwp.web.data.EbtsImage;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(ServiceMessageHandler.class);

    private final IwpWebProperties iwpWebProperties;
    private final JmsTemplate jmsTemplate;

    private final AciiResponseRepository aciiResponseRepository;
    private final BiqtResponseRepository biqtResponseRepository;
    private final ContactClassifierResponseRepository contactClassifierResponseRepository;
    private final DualPDMResponseRepository dualPDMResponseRepository;
    private final DualPDMCompareResponseRepository dualPDMCompareResponseRepository;
    private final TshepiiResponseRepository tshepiiResponseRepository;
    private final TshepiiCompareResponseRepository tshepiiCompareResponseRepository;
    private final IrisAnnotationResponseRepository irisAnnotationResponseRepository;
    private final PdmResponseRepository pdmResponseRepository;

    @Autowired
    public ServiceMessageHandler(IwpWebProperties iwpWebProperties, JmsTemplate jmsTemplate,
                                 AciiResponseRepository aciiResponseRepository, BiqtResponseRepository biqtResponseRepository,
                                 ContactClassifierResponseRepository contactClassifierResponseRepository,
                                 DualPDMResponseRepository dualPDMResponseRepository,
                                 DualPDMCompareResponseRepository dualPDMCompareResponseRepository,
                                 TshepiiResponseRepository tshepiiResponseRepository,
                                 TshepiiCompareResponseRepository tshepiiCompareResponseRepository,
                                 IrisAnnotationResponseRepository irisAnnotationResponseRepository,
                                 PdmResponseRepository pdmResponseRepository) {
        this.iwpWebProperties = iwpWebProperties;
        this.jmsTemplate = jmsTemplate;
        this.aciiResponseRepository = aciiResponseRepository;
        this.biqtResponseRepository = biqtResponseRepository;
        this.contactClassifierResponseRepository = contactClassifierResponseRepository;
        this.dualPDMResponseRepository = dualPDMResponseRepository;
        this.dualPDMCompareResponseRepository = dualPDMCompareResponseRepository;
        this.tshepiiResponseRepository = tshepiiResponseRepository;
        this.tshepiiCompareResponseRepository = tshepiiCompareResponseRepository;
        this.irisAnnotationResponseRepository = irisAnnotationResponseRepository;
        this.pdmResponseRepository = pdmResponseRepository;
    }

    public List<org.mitre.iwp.web.model.Service> getServicesStatus(){
        List<org.mitre.iwp.web.model.Service> serviceList = new ArrayList<>();

        List<String> excludedServices = this.iwpWebProperties.getExcludedServices();

        // Expand as services are expanded

        org.mitre.iwp.web.model.Service irisAnnotation = new org.mitre.iwp.web.model.Service();
        irisAnnotation.setServiceEnabled(this.iwpWebProperties.getIrisAnnotationService());
        irisAnnotation.setServiceName(this.iwpWebProperties.getIrisAnnotationServiceName());
        irisAnnotation.setRequestQueue(this.iwpWebProperties.getIrisAnnotationQueueRequest());
        irisAnnotation.setResponseQueue(this.iwpWebProperties.getIrisAnnotationQueueResponse());
        irisAnnotation.setServiceExcluded(excludedServices.contains("annotation"));
        serviceList.add(irisAnnotation);

        org.mitre.iwp.web.model.Service tshepii = new org.mitre.iwp.web.model.Service();
        tshepii.setServiceEnabled(this.iwpWebProperties.getTshepiiService());
        tshepii.setServiceName(this.iwpWebProperties.getTshepiiServiceName());
        tshepii.setRequestQueue(this.iwpWebProperties.getTshepiiQueueRequest());
        tshepii.setResponseQueue(this.iwpWebProperties.getTshepiiQueueResponse());
        tshepii.setServiceExcluded(excludedServices.contains("tshepii"));
        serviceList.add(tshepii);

        org.mitre.iwp.web.model.Service acii = new org.mitre.iwp.web.model.Service();
        acii.setServiceEnabled(this.iwpWebProperties.getAciiService());
        acii.setServiceName(this.iwpWebProperties.getAciiServiceName());
        acii.setRequestQueue(this.iwpWebProperties.getAciiQueueRequest());
        acii.setResponseQueue(this.iwpWebProperties.getAciiQueueResponse());
        acii.setServiceExcluded(excludedServices.contains("acii"));
        serviceList.add(acii);

        org.mitre.iwp.web.model.Service biqt = new org.mitre.iwp.web.model.Service();
        biqt.setServiceEnabled(this.iwpWebProperties.getBiqtService());
        biqt.setServiceName(this.iwpWebProperties.getBiqtServiceName());
        biqt.setRequestQueue(this.iwpWebProperties.getBiqtQueueRequest());
        biqt.setResponseQueue(this.iwpWebProperties.getBiqtQueueResponse());
        biqt.setServiceExcluded(excludedServices.contains("biqt"));
        serviceList.add(biqt);

        org.mitre.iwp.web.model.Service pdm = new org.mitre.iwp.web.model.Service();
        pdm.setServiceEnabled(this.iwpWebProperties.getPdmService());
        pdm.setServiceName(this.iwpWebProperties.getPdmServiceName());
        pdm.setRequestQueue(this.iwpWebProperties.getPdmQueueRequest());
        pdm.setResponseQueue(this.iwpWebProperties.getPdmQueueResponse());
        pdm.setServiceExcluded(excludedServices.contains("pdm"));
        serviceList.add(pdm);

        org.mitre.iwp.web.model.Service dualPDM = new org.mitre.iwp.web.model.Service();
        dualPDM.setServiceEnabled(this.iwpWebProperties.getDualPDMService());
        dualPDM.setServiceName(this.iwpWebProperties.getDualPDMServiceName());
        dualPDM.setRequestQueue(this.iwpWebProperties.getDualPDMQueueRequest());
        dualPDM.setResponseQueue(this.iwpWebProperties.getDualPDMQueueResponse());
        dualPDM.setServiceExcluded(excludedServices.contains("dualpdm"));
        serviceList.add(dualPDM);

        return serviceList;
    }

    public void processImageDefault(EbtsImage eImage) { 
        if (this.iwpWebProperties.getAciiService()) {
            this.processAcii(eImage, null);
        }

        if (this.iwpWebProperties.getBiqtService()) {
            this.processBiqtContact(eImage, null);
        }

        if (this.iwpWebProperties.getTshepiiService()) {
            this.processTshepii(eImage, null);
        }

        if (this.iwpWebProperties.getIrisAnnotationService()) {
            this.processIrisAnnotation(eImage, null);
        }

        if (this.iwpWebProperties.getPdmService()) {
            this.processPdm(eImage, null);
        }

        if (this.iwpWebProperties.getDualPDMService()) {
            this.processDualPDM(eImage);
        }
    }

    public Long processAcii(EbtsImage eImage, Long ebtsImageId) {
        AciiResponse response = this.aciiResponseRepository.findByImageId(eImage.getId());
        if (response != null) {
            if (response.getStatus() != ServiceCallStatus.NotSent) {
                return response.getImageId();
            }
        } else {
            response = new AciiResponse();
            if (ebtsImageId != null) {
                response.setEbtsImageId(ebtsImageId);
            }
        }

        response.setStatus(ServiceCallStatus.Pending);
        response = this.aciiResponseRepository.save(response);

        try {
            this.pushMessageToQueue(this.iwpWebProperties.getAciiQueueRequest(),
                    ByteString.copyFrom(EbtsImage.getImageBytes(eImage)), response.getImageId());
        } catch (IOException ex) {

            response.setError(ex.getMessage());
            this.aciiResponseRepository.save(response);
        }
        return response.getImageId();
    }

    public Long processBiqtContact(EbtsImage eImage, Long ebtsImageId) {
        BiqtResponse biqtResponse = this.processBiqtService(eImage, ebtsImageId);
        ContactClassifierResponse contactClassifierResponse = this.processContactService(eImage, ebtsImageId);

        // BIQT & Contact's imageId should be the same since you can't send an image back to one service without sending it back to the other.
        Long responseImageId = biqtResponse.getImageId();

        try {
            this.pushMessageToQueue(this.iwpWebProperties.getBiqtQueueRequest(),
                    ByteString.copyFrom(EbtsImage.getImageBytes(eImage)), responseImageId);
        } catch (IOException ex) {

            biqtResponse.setError(ex.getMessage());
            this.biqtResponseRepository.save(biqtResponse);
            contactClassifierResponse.setError(ex.getMessage());
            this.contactClassifierResponseRepository.save(contactClassifierResponse);
        }

        return responseImageId;
    }

    private BiqtResponse processBiqtService(EbtsImage eImage, Long ebtsImageId) {
        BiqtResponse response = this.biqtResponseRepository.findByImageId(eImage.getId());
        if (response != null) {
            if (response.getStatus() != ServiceCallStatus.NotSent) {
                return response;
            }
        } else {
            response = new BiqtResponse();
            if (ebtsImageId != null) {
                response.setEbtsImageId(ebtsImageId);
            }
        }

        response.setStatus(ServiceCallStatus.Pending);
        response = this.biqtResponseRepository.save(response);

        return response;
    }

    private ContactClassifierResponse processContactService(EbtsImage eImage, Long ebtsImageId) {
        ContactClassifierResponse response = this.contactClassifierResponseRepository.findByImageId(eImage.getId());
        if (response != null) {
            if (response.getStatus() != ServiceCallStatus.NotSent) {
                return response;
            }
        } else {
            response = new ContactClassifierResponse();
            if (ebtsImageId != null) {
                response.setEbtsImageId(ebtsImageId);
            }
        }

        response.setStatus(ServiceCallStatus.Pending);
        response = this.contactClassifierResponseRepository.save(response);

        return response;

    }

    public Long processTshepii(EbtsImage eImage, Long ebtsImageId) {
        TshepiiResponse response = this.tshepiiResponseRepository.findByImageId(eImage.getId());
        if (response == null) {
            response = new TshepiiResponse();
            if (ebtsImageId != null) {
                response.setEbtsImageId(ebtsImageId);
            }
        }

        response.setStatus(ServiceCallStatus.Pending);
        response = this.tshepiiResponseRepository.save(response);

        try {
            this.pushMessageToQueue(this.iwpWebProperties.getTshepiiQueueRequest(),
                    ByteString.copyFrom(EbtsImage.getImageBytes(eImage)), response.getImageId());
        } catch (IOException ex) {
            log.info("Exception");
            response.setError(ex.getMessage());
            this.tshepiiResponseRepository.save(response);
        }

        return response.getImageId();
    }

    public boolean requestTshepiiComparison(Long probeImageId, Long candidateImageId) {
        TshepiiResponse probeResponse = this.tshepiiResponseRepository.findByImageId(probeImageId);
        TshepiiResponse candResponse = this.tshepiiResponseRepository.findByImageId(candidateImageId);

        boolean cancelCall = false;
        if (probeResponse == null || candResponse == null) {
            return false;
        }

        if (probeResponse.getStatus() != ServiceCallStatus.Received) {
            log.info("Probe Status is: {}", probeResponse.getStatus());
            cancelCall = true;
        }

        if (candResponse.getStatus() != ServiceCallStatus.Received) {
            log.info("Candidate Status is: {}", probeResponse.getStatus());
            cancelCall = true;
        }

        if (cancelCall) {
            log.info("Cancelling Tshepii Comparison Request");
            return false;
        }

        try {

            TshepiiCompareResponse compareResponse = new TshepiiCompareResponse();
            compareResponse.setImageId(probeResponse.getImageId());
            compareResponse.setEbtsImageId(probeResponse.getEbtsImageId());
            compareResponse.setCandidateImageId(candResponse.getImageId());
            compareResponse.setCandidateEbtsImageId(candResponse.getEbtsImageId());
            compareResponse.setStatus(ServiceCallStatus.Pending);
            this.tshepiiCompareResponseRepository.save(compareResponse);


            Tshepii.TShepiiCompareRequest compareRequest = Tshepii.TShepiiCompareRequest.newBuilder()
                    .setResponse1(Tshepii.TShepiiResponse.parseFrom(probeResponse.getServiceResponse()))
                    .setResponse2(Tshepii.TShepiiResponse.parseFrom(candResponse.getServiceResponse())).build();

            this.jmsTemplate.convertAndSend(this.iwpWebProperties.getTshepiiQueueRequest(),
                    compareRequest.toByteArray());
            return true;
        } catch (InvalidProtocolBufferException exc) {
            log.info(exc.getMessage());
        }

        return false;
    }

    public Long processIrisAnnotation(EbtsImage eImage, Long ebtsImageId) {
        IrisAnnotationResponse response = this.irisAnnotationResponseRepository.findByImageId(eImage.getId());
        if (response != null) {
            if (response.getStatus() != ServiceCallStatus.NotSent) {
                return response.getImageId();
            }
        } else {
            response = new IrisAnnotationResponse();
            if (ebtsImageId != null) {
                response.setEbtsImageId(ebtsImageId);
            }
        }

        response.setStatus(ServiceCallStatus.Pending);
        response = this.irisAnnotationResponseRepository.save(response);

        try {
            this.pushMessageToQueue(this.iwpWebProperties.getIrisAnnotationQueueRequest(),
                    ByteString.copyFrom(EbtsImage.getImageBytes(eImage)), response.getImageId());
        } catch (IOException ex) {
            log.info("Exception");
            response.setError(ex.getMessage());
            this.irisAnnotationResponseRepository.save(response);
        }

        return response.getImageId();
    }  
    
    public Long processPdm(EbtsImage eImage, Long ebtsImageId) {
        PdmResponse response = this.pdmResponseRepository.findByImageId(eImage.getId());
        if (response != null) {
            if (response.getStatus() != ServiceCallStatus.NotSent) {
                return response.getImageId();
            }
        } else {
            response = new PdmResponse();
            if (ebtsImageId != null) {
                response.setEbtsImageId(ebtsImageId);
            }
        }

        response.setStatus(ServiceCallStatus.Pending);
        response = this.pdmResponseRepository.save(response);
        try {
            this.pushMessageToQueue(this.iwpWebProperties.getPdmQueueRequest(),
                    ByteString.copyFrom(EbtsImage.getImageBytes(eImage)), response.getImageId());
        } catch (IOException ex) {

            response.setError(ex.getMessage());
            this.pdmResponseRepository.save(response);
        }
        return response.getImageId();
    }

    // Dual PDM; doesn't need a Long ebtsImageId since it can't be resubmitted.
    public void processDualPDM(EbtsImage eImage) {
        DualPDMResponse response = this.dualPDMResponseRepository.findByImageId(eImage.getId());

        if (response == null) {
            response = new DualPDMResponse();
            response.setEbtsImageId(eImage.getId());
        }

        response.setStatus(ServiceCallStatus.Pending);

        try {
            response.setImageBytes(EbtsImage.getImageBytes(eImage));
            this.dualPDMResponseRepository.save(response);
            response.setStatus(ServiceCallStatus.Received);
        } catch (Exception ex) {
            log.error("Failed to save image for id{}", eImage.getId());
            response.setError(ex.getMessage());
            this.dualPDMResponseRepository.save(response);
        }
    }

    public boolean requestDualPDMComparison(Long probeImageId, Long candidateImageId) {
        // Retrieve the probe and candidate responses from the database
        DualPDMResponse probeResponse = this.dualPDMResponseRepository.findByImageId(probeImageId);
        DualPDMResponse candidateResponse = this.dualPDMResponseRepository.findByImageId(candidateImageId);

        // Validate that both responses exist
        if (probeResponse == null || candidateResponse == null) {
            log.error("Probe or Candidate response not found for IDs: Probe={}, Candidate={}", probeImageId, candidateImageId);
            return false;
        }

        // Validate that both responses have the "Received" status
        boolean cancelCall = false;
        if (probeResponse.getStatus() != ServiceCallStatus.Received) {
            log.info("Probe Status is: {}", probeResponse.getStatus());
            cancelCall = true;
        }
        if (candidateResponse.getStatus() != ServiceCallStatus.Received) {
            log.info("Candidate Status is: {}", candidateResponse.getStatus());
            cancelCall = true;
        }

        if (cancelCall) {
            log.info("Cancelling Dual PDM Comparison Request due to invalid statuses");
            return false;
        }

        try {
            // Create a new DualPDMCompareResponse entity with "Pending" status
            DualPDMCompareResponse compareResponse = new DualPDMCompareResponse();
            compareResponse.setProbeId(probeImageId);
            compareResponse.setCandidateId(candidateImageId);
            compareResponse.setStatus(ServiceCallStatus.Pending);
            DualPDMCompareResponse savedCompareResponse = this.dualPDMCompareResponseRepository.save(compareResponse);

            // Construct the DualPDMComparison Protocol Buffers message
            Pdm.DualPDMComparison comparisonRequest = Pdm.DualPDMComparison.newBuilder()
                    .setProbe(ByteString.copyFrom(probeResponse.getImageBytes()))
                    .setCandidate(ByteString.copyFrom(candidateResponse.getImageBytes()))
                    .build();

            log.info("Pushing Message to DualPDM Request Queue");

            this.jmsTemplate.convertAndSend(
                this.iwpWebProperties.getDualPDMQueueRequest(),
                comparisonRequest.toByteArray(),
                message -> {
                    message.setLongProperty("probeId", savedCompareResponse.getProbeId());
                    message.setLongProperty("candidateId", savedCompareResponse.getCandidateId());
                    return message;
                }
            );

            return true;
        } catch (Exception exc) {
            log.error("Unexpected error while sending Dual PDM Comparison Request: {}", exc.getMessage());
        }

        return false;
    }

    private void pushMessageToQueue(String queueName, ByteString bytes, Long id) {
        log.info("Pushing Message to Queue: {}", queueName);
        Iwp.ImageServiceQuery isq = Iwp.ImageServiceQuery.newBuilder().setImageId(id.toString()).setImageBytes(bytes)
                .build();

        this.jmsTemplate.convertAndSend(queueName, isq.toByteArray());
    }

}
