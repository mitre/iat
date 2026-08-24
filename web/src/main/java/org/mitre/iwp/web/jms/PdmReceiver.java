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

import com.google.protobuf.*;

import java.util.Map;
import org.mitre.iwp.buffers.Pdm.DeformedImage;
import org.mitre.iwp.buffers.Pdm.DeformerWrapper;
import org.mitre.iwp.web.model.*;
import org.mitre.iwp.web.service.PdmDeformedImageResponseRepository;
import org.mitre.iwp.web.service.PdmResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

@Component
@ConditionalOnProperty(prefix = "iris", name = "pdmService", havingValue = "true")
public class PdmReceiver {
    private static final Logger log = LoggerFactory.getLogger(PdmReceiver.class);
    private final PdmResponseRepository pdmResponseRepository;
    private final PdmDeformedImageResponseRepository pdmDeformedImageResponseRepository;

    @Autowired
    public PdmReceiver(PdmResponseRepository pdmResponseRepository, PdmDeformedImageResponseRepository pdmDeformedImageResponseRepository) {
        this.pdmResponseRepository = pdmResponseRepository;
        this.pdmDeformedImageResponseRepository = pdmDeformedImageResponseRepository;
    }

    @JmsListener(destination = "#{iwpWebProperties.getPdmQueueResponse()}")
    public void processMessage(BytesMessage message) {
        try {
            // Read byte array from bytemessage
            byte[] bytes = new byte[(int) message.getBodyLength()];
            message.readBytes(bytes);

            // Deserialize
            DeformerWrapper serviceResponse = DeformerWrapper.parseFrom(bytes);
            Map<String, DeformedImage> eyePreserveMap = serviceResponse.getEyePreserveResponseMap();
            String imageIdStr = serviceResponse.getImageId();

            // Process imageId
            Long responseImgId = Long.parseLong(imageIdStr);
            log.info("Pdm Response for Image Id: {}", responseImgId);

            // Retrieve/Create new PdmResponse entity
            PdmResponse pdmResponse = this.pdmResponseRepository.findByImageId(responseImgId);

            if (pdmResponse == null) {
                pdmResponse = new PdmResponse();
            }

            // Convert and save each deformed image
            for (Map.Entry<String, DeformedImage> entry : eyePreserveMap.entrySet()) {
                PdmDeformedImageResponse pdmDeformedImageResponse = new PdmDeformedImageResponse();
                String alphaScore = entry.getKey();
                DeformedImage image = entry.getValue();
                
                pdmDeformedImageResponse = convertToPdmDeformedImageResponse(pdmDeformedImageResponse, alphaScore, image, responseImgId);
                this.pdmDeformedImageResponseRepository.save(pdmDeformedImageResponse);


                // Set status and save
                pdmResponse.setStatus(ServiceCallStatus.Received);
                this.pdmResponseRepository.save(pdmResponse);
                
            } 
        } catch (JMSException | InvalidProtocolBufferException e) {
            log.error("Error processing message: ", e);
        } catch (NumberFormatException e) {
            log.error("Invalid imageId format: ", e);
        }
    }

    private PdmDeformedImageResponse convertToPdmDeformedImageResponse(PdmDeformedImageResponse pdmDeformedeImageResponse, String alphaScore, DeformedImage protoDeformedImage, Long imageid){
        pdmDeformedeImageResponse.setDeformedId(imageid, alphaScore);
        pdmDeformedeImageResponse.setImageData(protoDeformedImage.getImageData().toByteArray());
        pdmDeformedeImageResponse.setScore(protoDeformedImage.getScore());
        pdmDeformedeImageResponse.setImageType(protoDeformedImage.getImageType().name());
        pdmDeformedeImageResponse.setWidth(protoDeformedImage.getWidth());
        pdmDeformedeImageResponse.setHeight(protoDeformedImage.getHeight());
        return pdmDeformedeImageResponse;

    }
}
