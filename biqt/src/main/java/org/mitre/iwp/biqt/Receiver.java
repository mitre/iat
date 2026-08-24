/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.biqt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;

import jakarta.annotation.PostConstruct;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;

import org.json.simple.JSONObject;
import org.mitre.biqt.BIQT;
import org.mitre.biqt.ProviderInfo;
import org.mitre.iwp.buffers.ContactClassifier;
import org.mitre.iwp.buffers.Messages;
import org.mitre.iwp.buffers.Biqt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.ProviderNotFoundException;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Component
public class Receiver {

    // A logger for this class.
    private static final Logger log = LoggerFactory.getLogger(Receiver.class);

    // The JMS template that will be used to send messages.
    private JmsTemplate jmsTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    // A re-usable BIQT instance.
    private BIQT biqtInstance;

    // The name of the BIQTIris provider exposed through the framework. This may
    // vary slightly
    // from version to version, so it's not hard-coded here. Instead, it's set in
    // the post constructor.
    private List<String> availableProviderList = new ArrayList<>();
    @Value("${iwp.biqt.requestedProviders}")
    private List<String> requestedProviderList;

    @Value("${iwp.biqt.response_queue}")
    private String responseBiqtQueue;

    @Value("${iwp.contactdetection.response_queue}")
    private String responseContactQueue;

    @Value("${iwp.contactdetection.detection_threshold}")
    private Double contactDetectionThreshold;

    @Value("${iwp.biqt.request_queue}")
    private String listenerQ;

    @Value("${spring.activemq.broker-url}")
    private String jmsBroker;

    private enum ProviderType {
        BIQTCONTACTDETECTOR,
        BIQTFACE,
        BIQTIRIS,
        NA
    }

    @Autowired
    public Receiver(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
        this.biqtInstance = new BIQT();
        log.info("JMS Template and BIQT instance instantiated");
    }

    @PostConstruct
    public void init() {

        for (ProviderInfo providerInfo : biqtInstance.getProviders()) {
            String providerName = providerInfo.getName();
            availableProviderList.add(providerName);
        }

        if (availableProviderList == null || availableProviderList.isEmpty()) {
            throw new ProviderNotFoundException("There are no providers installed or provided by the BIQT instance.");
        } else {
            log.info("BIQT Installed Provider(s): {}", availableProviderList.isEmpty() ? "" : Arrays.deepToString(availableProviderList.toArray()));
            availableProviderList.replaceAll(String::toLowerCase);
        }

        Set<String> availableProviderSet = new HashSet<>(availableProviderList);

        if (requestedProviderList == null || requestedProviderList.isEmpty()) {
            throw new ProviderNotFoundException("There are no providers requested in the configuration properties");
        } else {
            log.info("requestedProvider(s) from configuration file: {}", requestedProviderList.toArray());
        }

        ProviderType checkProviderType;

        for (String currentProvider : requestedProviderList) {
            if (availableProviderSet.contains(currentProvider.toLowerCase())) {
                checkProviderType = ProviderType.valueOf(currentProvider.toUpperCase());

                switch (checkProviderType) {
                    case BIQTCONTACTDETECTOR:
                    case BIQTIRIS:
                        log.info("The {} provider is installed and available ", currentProvider);
                        break;
                    case BIQTFACE:
                        log.info("The requested {} provider is present but messaging " +
                                "has not yet been implemented to support it", currentProvider);
                        break;
                    default:
                        log.info("The {} provider is not supported", currentProvider);
                        throw new ProviderNotFoundException("The " + currentProvider + " provider is not supported. BIQT will terminate.");
                }
            } else {
                // Shoudln't be in this since it should have terminated
                // during initialization when we failed to find the desired provider.
                log.info("The requested {} provider does not appear to be installed", currentProvider);
                throw new ProviderNotFoundException("The requested " + currentProvider + " provider does not appear to be installed. BIQT will terminate.");
            }
        }

        log.info("The BIQT service is alive and waiting at: {}:{}", jmsBroker, listenerQ);
    }

    private Messages.ImageServiceQuery unpack(BytesMessage message) {
        try {
            // Attempt to parse the request from the incoming message body.
            int length = (int) message.getBodyLength();
            byte[] data = new byte[length];
            message.readBytes(data);
            return Messages.ImageServiceQuery.parseFrom(data);
        } catch (JMSException | InvalidProtocolBufferException e) {
            // Failure to unpack the request. This isn't exceptional, so we can return null for now.
            return null;
        }
    }

    @JmsListener(destination = "${iwp.biqt.request_queue}", containerFactory = "factory")
    public void processRequestMessage(@Headers Map<String, Object> headers, Message message) {

        // Get the broker-provided message id and the user-provided ImageServiceQuery id if it's available.
        // Combine them so that it's easier to trace the processing of a request.
        String messageId = headers.getOrDefault(JmsHeaders.MESSAGE_ID, "NoMessageIdProvided").toString();
        Messages.ImageServiceQuery query = unpack((BytesMessage) message);

        if (query == null) {
            log.warn("[{}] The request could not be unpacked.", messageId);
            return;
        }

        final String finalMessageId = messageId + "|" + query.getId();

        String replyToBiqt = responseBiqtQueue;
        String replyToContact = responseContactQueue;

        if (!(message instanceof BytesMessage)) {
            log.warn("[{}] Expected a {}, but received a {}. This message will not be processed.",
                    messageId, BytesMessage.class.getSimpleName(), message.getClass().getSimpleName());
            return;
        }

        new Thread() {
            @Override
            public void run() {
                for (String currentProviderName : requestedProviderList) {
                    ProviderType currentProviderType = ProviderType.valueOf(currentProviderName.toUpperCase());

                    switch (currentProviderType) {
                        case BIQTIRIS: {
                            Biqt.BiqtResponse.Builder biqtReply = Biqt.BiqtResponse.newBuilder()
                                    .setImageId(query.getId());
                            try {
                                // Finish building the response.
                                processIrisRequestInternal(finalMessageId, query, biqtReply, currentProviderName);
                            } catch (Exception exception) {
                                log.error("[{}] Processing or response generation failed.", finalMessageId, exception);

                                // Set the quality to a very large negative number to communicate the error.
                                biqtReply.setQuality(Integer.MIN_VALUE);
                            }

                            jmsTemplate.send(replyToBiqt, new MessageCreator() {
                                @SuppressWarnings("null")
                                @Override
                                public Message createMessage(Session session) throws JMSException {
                                    BytesMessage message = session.createBytesMessage();

                                    message.writeBytes(biqtReply.build().toByteArray());
                                    log.info("Message {} Response sent to {}.", biqtReply.getImageId(), replyToBiqt);

                                    return message;
                                }
                            });

                            log.info("[{}] Quality: {}.", biqtReply.getImageId(), biqtReply.getQuality());
                        }
                            break;

                        case BIQTCONTACTDETECTOR: {
                            ContactClassifier.ContactClassifierResponse.Builder contactReply = ContactClassifier.ContactClassifierResponse
                                    .newBuilder()
                                    .setImageId(query.getId());

                            try {
                                // Finish building the response.
                                processContactRequestInternal(finalMessageId, query, contactReply, currentProviderName);
                            } catch (Exception exception) {
                                log.error("[{}] Processing or response generation failed.", finalMessageId, exception);

                                // Set the detection code -1 to communicate the error.
                                contactReply.setDetectionCode("-1"); // -1 = Error
                            }

                            jmsTemplate.send(replyToContact, new MessageCreator() {
                                @SuppressWarnings("null")
                                @Override
                                public Message createMessage(Session session) throws JMSException {
                                    BytesMessage message = session.createBytesMessage();

                                    message.writeBytes(contactReply.build().toByteArray());
                                    log.info("Message {} Response sent to {}.", contactReply.getImageId(), replyToContact);

                                    return message;
                                }
                            });

                            log.info("[{}] Contact Detection: {}.", contactReply.getImageId(), contactReply.getDetectionCode());
                        }
                            break;
                        default:
                            log.warn("The {} provider is not supported", currentProviderType);
                            break;
                    }
                }
            }
        }.start();
    }

    private File downloadToFile(String messageId, URI source) {
        try {
            File target = File.createTempFile("iwp-biqt", ".bin");
            target.deleteOnExit();

            try (InputStream stream = source.toURL().openStream()) {
                byte[] data = stream.readAllBytes();
                Files.write(target.toPath(), data, StandardOpenOption.APPEND);
            }
            return target;
        } catch (IOException ioe) {
            log.error("[{}] Failed to download '{}'.", messageId, source, ioe);
            return null;
        }
    }

    private File extractImageDataToFile(String messageId, Messages.ImageServiceQuery request) {
        try {
            File target = File.createTempFile("iwp-biqt", ".bin");
            target.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(target)) {
                request.getImageBytes().writeTo(fos);
            }
            return target;
        } catch (IOException ioe) {
            log.error("[{}] Failed to copy the embedded image data to a file.", messageId, ioe);
            return null;
        }
    }

    private void processIrisRequestInternal(String messageId,
            Messages.ImageServiceQuery request,
            Biqt.BiqtResponse.Builder response,
            String providerName) throws IOException {

        List<JSONObject> jsonObjects = new ArrayList<>();
        int quality = Integer.MIN_VALUE;

        log.info("Processing with provider: {}.", providerName);

        // Check if the path and the image bytes aren't null
        if (Boolean.TRUE.equals(isIrisRequestPathOrBytesNull(messageId, request, response, providerName, jsonObjects))) {
            return;
        }

        if (jsonObjects.size() != 1) {
            log.warn(
                    "[{}] BIQT returned an unexpected number of results ({}). Only the first (if available) will be considered.",
                    messageId, jsonObjects.size());
        }

        for (JSONObject obj : jsonObjects) {
            log.debug("json objects: [{}] ", obj);
            try {
                BiqtResult biqtResult = mapper.readValue(obj.toJSONString(), BiqtResult.class);

                if (biqtResult.getErrorCode() != 0) {
                    log.warn("[{}] BIQT return error code {}.", messageId, biqtResult.getErrorCode());
                } else if (biqtResult.getQualityResult().size() != 1) {
                    log.warn("[{}] BIQT returned an unexpected number of quality results ({}).",
                            messageId, biqtResult.getQualityResult().size());
                } else if (biqtResult.getQualityResult().get(0).getMetrics() == null) {
                    log.warn("[{}] BIQT did not return metrics for this request.", messageId);
                } else {
                    quality = (int) (biqtResult.getQualityResult().get(0).getMetrics().getQuality());
                }
            } catch (IOException ioe) {
                log.warn("[{}] Failed to make sense of BIQT's json output.", messageId, ioe);
            }
            break;
        }
        response.setQuality(quality);
        log.info("setQuality {}", response.getQuality());
    }

    private Boolean isIrisRequestPathOrBytesNull(String messageId,
            Messages.ImageServiceQuery request,
            Biqt.BiqtResponse.Builder response,
            String providerName,
            List<JSONObject> jsonObjects) throws IOException {

        if (request.getPath() != null && request.getPath().length() > 0) {
            // Get the URI which is expected to start with http://, https://, file://, etc.
            URI uri = URI.create(request.getPath());

            // If it doesn't start with file://, it's considered a remote file and will need
            // to be downloaded to the local host.
            boolean isRemoteFile = !uri.getScheme().equalsIgnoreCase("file");
            File target = isRemoteFile ? downloadToFile(messageId, uri) : new File(uri.getPath());

            if (target == null) {
                // Couldn't download the file.
                response.setQuality(Integer.MIN_VALUE);
                return true;
            } else {
                // The file was a local file or it was successfully downloaded. Run BIQT on it.
                // If it was a remote file, clean it up after BIQT is done.
                try {
                    jsonObjects.addAll(biqtInstance.runProvider(providerName, Arrays.asList(target.getAbsolutePath())));
                } finally {
                    if (isRemoteFile) {
                        Files.delete(target.toPath());
                    }
                }
            }
        } else if (request.getImageBytes() != null && request.getImageBytes().size() > 0) {
            // The image was embedded in the request. Extract it to a file, then run BIQT on it.
            File target = extractImageDataToFile(messageId, request);
            if (target != null) {
                try {
                    jsonObjects.addAll(biqtInstance.runProvider(providerName, Arrays.asList(target.getAbsolutePath())));
                } finally {
                    Files.delete(target.toPath());
                }
            }
        } else {
            log.warn("[{}] The request did not contain an image path or image data.", messageId);
            response.setQuality(Integer.MIN_VALUE);

            return true;
        }

        return false;
    }

    private void processContactRequestInternal(String messageId,
            Messages.ImageServiceQuery request,
            ContactClassifier.ContactClassifierResponse.Builder response,
            String providerName) throws IOException {

        List<JSONObject> jsonObjects = new ArrayList<>();
        double detectionCode = -1;
        String errorMessageString = "ERROR";

        log.info("Processing with provider: {}.", providerName);

        if (Boolean.TRUE.equals(isContactRequestPathOrBytesNull(messageId, request, response, providerName, 
                errorMessageString, jsonObjects))) {
            return;
        }

        if (jsonObjects.size() != 1) {
            log.warn(
                    "[{}] BIQT returned an unexpected number of results ({}). Only the first (if available) will be considered.",
                    messageId, jsonObjects.size());
        }

        for (JSONObject obj : jsonObjects) {
            log.debug("json objects: [{}] ", obj);
            try {

                BiqtResult biqtResult = mapper.readValue(obj.toJSONString(), BiqtResult.class);

                if (biqtResult.getErrorCode() != 0) {
                    log.warn("[{}] BIQT return error code {}.", messageId, biqtResult.getErrorCode());
                } else if (biqtResult.getQualityResult().size() != 1) {
                    log.warn("[{}] BIQT returned an unexpected number of quality results ({}).",
                            messageId, biqtResult.getQualityResult().size());
                } else if (biqtResult.getQualityResult().get(0).getMetrics() == null) {
                    log.warn("[{}] BIQT did not return metrics for this request.", messageId);
                } else {
                    detectionCode = (biqtResult.getQualityResult().get(0).getMetrics().getCosmeticContactConfidence());
                    log.info("cosmetic_contact_confidence: {} ", detectionCode);
                }
            } catch (IOException ioe) {
                log.warn("[{}] Failed to make sense of BIQT's json output.", messageId, ioe);
                response.setResponse(errorMessageString);
                response.setMessage(ioe.getMessage());
            }
        }

        log.info("Using contactDetectionThreshold : {} ", contactDetectionThreshold);

        if (detectionCode > contactDetectionThreshold) {
            response.setDetectionCode("1"); // 1 = Cosmetic
            response.setResponse("OK");
        } else if (detectionCode > 0 && contactDetectionThreshold > detectionCode) {
            response.setDetectionCode("0"); // 0 = none
            response.setResponse("OK");
        } else {
            response.setDetectionCode("-1"); // -1 = Error
        }

        log.info("contactResponse Result {}", response);
    }

    private Boolean isContactRequestPathOrBytesNull(String messageId,
            Messages.ImageServiceQuery request,
            ContactClassifier.ContactClassifierResponse.Builder response,
            String providerName,
            String errorMessageString,
            List<JSONObject>jsonObjects) throws IOException {
        if (request.getPath() != null && request.getPath().length() > 0) {
            // Get the URI which is expected to start with http://, https://, file://, etc.
            URI uri = URI.create(request.getPath());

            // If it doesn't start with file://, it's considered a remote file and will need
            // to be downloaded to the local host.
            boolean isRemoteFile = !uri.getScheme().equalsIgnoreCase("file");
            File target = isRemoteFile ? downloadToFile(messageId, uri) : new File(uri.getPath());

            if (target == null) {
                // Couldn't download the file.
                response.setResponse(errorMessageString);
                response.setMessage("The request did not contain an image path or image data.");
                response.setDetectionCode("-1");

                return true;
            } else {
                // The file was a local file or it was successfully downloaded. Run BIQT on it.
                // If it was a remote file, clean it up after BIQT is done.
                try {
                    jsonObjects.addAll(submitContactRequest(providerName, Arrays.asList(target.getAbsolutePath())));
                } finally {
                    if (isRemoteFile) {
                        Files.delete(target.toPath());
                    }
                }
            }
        } else if (request.getImageBytes() != null && request.getImageBytes().size() > 0) {
            // The image was embedded in the request. Extract it to a file, then run BIQT on it.
            File target = extractImageDataToFile(messageId, request);
            if (target != null) {
                try {
                    jsonObjects.addAll(submitContactRequest(providerName, Arrays.asList(target.getAbsolutePath())));
                } finally {
                    Files.delete(target.toPath());
                }
            }
        } else {
            log.warn("[{}] The request did not contain an image path or image data.", messageId);
            response.setResponse(errorMessageString);
            response.setMessage("The request did not contain an image path or image data.");
            response.setDetectionCode("-1");
            return true;
        }

        return false;
    }

    private synchronized List<JSONObject> submitContactRequest(String providerName, List<String> paths) {
        return biqtInstance.runProvider(providerName, paths);
    }
}
