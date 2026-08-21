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

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.jnbis.BitmapWithMetadata;
import org.jnbis.WSQDecoder;
import org.mitre.iwp.common.ImageUtils;
import org.mitre.iwp.mismatchrequest.EncounterType;
import org.mitre.iwp.mismatchrequest.IdentityType;
import org.mitre.iwp.mismatchrequest.ImageType;
import org.mitre.iwp.mismatchrequest.Request;
import org.mitre.iwp.support.EbtsTransactionCreator;
import org.mitre.iwp.web.IwpWebProperties;
import org.mitre.iwp.web.data.*;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.jms.ServiceMessageHandler;
import org.mitre.iwp.web.model.*;
import org.mitre.jet.ebts.Ebts;
import org.mitre.jet.ebts.EbtsBuilder;
import org.mitre.jet.ebts.EbtsParser;
import org.mitre.jet.ebts.Type7Handling;
import org.mitre.jet.ebts.records.GenericRecord;
import org.mitre.jet.ebts.records.LogicalRecord;
import org.mitre.jet.exceptions.EbtsBuildingException;
import org.mitre.jet.exceptions.EbtsParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Service
public class IrisService {

    private static final Logger log = LoggerFactory.getLogger(IrisService.class);

    private final IrisMessageRepository irisRepository;

    private final EbtsImageRepository imageRepository;

    private final ProfileRepository profileRepository;

    private final ImageFileRepository imageFileRepository;

    private final CreateIrisSearchRequestRepository createIrisSearchRequestRepository;

    private final ReportDataRepository reportDataRepository;

    private final ImageDataRepository imageDataRepository;

    private final UserAuthenticationRepository userAuthenticationRepository;

    private final ServiceMessageHandler serviceMessageHandler;

    private final IwpWebProperties iwpWebProperties;

    private final String imagePath;

    private final String uploadPath;

    private List<Integer> skipCheckRecords;

    private static final String CREATE_DIR_ERR_MESSAGE = "Unable to create the directory {}";
    private static final String CREATE_FOLD_ERR_MESSAGE = "Unable to create folders for upload";
    private static final String CREATE_FILE_ERR_MESSAGE = "Unable to create file {}";
    private static final String DATA_NOT_FOUND_MESSAGE = "Ebts Image Data not found";

    public IrisService(IrisMessageRepository irisRepository, EbtsImageRepository imageRepository,
            ProfileRepository profileRepository, ImageFileRepository imageFileRepository,
            CreateIrisSearchRequestRepository createIrisSearchRequestRepository,
            ServiceMessageHandler serviceMessageHandler,
            ReportDataRepository reportDataRepository, ImageDataRepository imageDataRepository,
            UserAuthenticationRepository userAuthenticationRepository,
            IwpWebProperties iwpWebProperties, @Value("${iwp.image.path}") String imagePath,
            @Value("${iwp.upload.path}") String uploadPath) {
        this.irisRepository = irisRepository;
        this.imageRepository = imageRepository;
        this.profileRepository = profileRepository;
        this.imageFileRepository = imageFileRepository;
        this.createIrisSearchRequestRepository = createIrisSearchRequestRepository;
        this.reportDataRepository = reportDataRepository;
        this.imageDataRepository = imageDataRepository;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.serviceMessageHandler = serviceMessageHandler;
        this.iwpWebProperties = iwpWebProperties;
        this.imagePath = imagePath;
        this.uploadPath = uploadPath;

        this.skipCheckRecords = Arrays.asList(1, 2, 17);
    }

    public Long createProfile(Profile profile) throws InvalidMessageException {
        // Ensure name is unique
        List<Profile> currentList = listProfiles();

        for (Profile prof : currentList) {
            if (prof.getName().equals(profile.getName())) {
                throw new InvalidMessageException(
                        String.format("Profile name '%s' already exists in system", profile.getName()));
            }
        }

        profileRepository.save(profile);
        return profile.getId();
    }

    public List<Profile> listProfiles() {
        return IterableUtils.toList(profileRepository.findAll());
    }

    public Long updateProfile(Profile profile) throws InvalidMessageException {
        // Ensure profile id exists in system
        if (profile.getId() == null || profile.getId() == -1) {
            throw new InvalidMessageException("Profile Id is empty");
        }

        Profile temp = profileRepository.findById(profile.getId())
                .orElseThrow(() -> new InvalidMessageException("Profile not found"));
        if (null == temp) {
            throw new InvalidMessageException(String.format("No profile found with id '%s'", profile.getId()));
        }

        profileRepository.save(profile);
        return profile.getId();
    }

    public void deleteProfile(long profileId) throws InvalidMessageException {
        // Ensure profile id exists in system
        if (profileId == -1) {
            throw new InvalidMessageException("Profile Id is empty");
        }

        Optional<Profile> profileOptional = profileRepository.findById(profileId);
        if (profileOptional.isPresent()) {
            userAuthenticationRepository.deleteByProfile(profileOptional.get());
            profileRepository.deleteById(profileId);
        } else {
            throw new InvalidMessageException(String.format("No profile found with id '%s'", profileId));
        }
    }

    public CreateIrisSearchRequest saveSearchMessage(String contentType, byte[] imageData, Long userId)
            throws NoSuchAlgorithmException, InvalidMessageException {
        this.uploadImage(contentType, imageData, "", true);

        return this.submitCreateImage(contentType, imageData, userId);
    }

    private String sanitizeHash(String hash) {
        // Prevent propagation of malicious hashes
        if (hash == null || !hash.matches("^[a-fA-F0-9]{64}$")) {
            throw new IllegalArgumentException("Invalid hash");
        }
        return hash;
    }

    private String sanitizeContentType(String contentType) {
        // Prevent propagation of malicious content types
        List<String> allowedTypes = Arrays.asList("tiff", "jpg", "jpeg", "png", "gif", "bmp", "unknown");
        String type = "unknown";
        if (contentType != null && contentType.contains("/")) {
            type = contentType.split("/")[1].toLowerCase();
        }
        if (!allowedTypes.contains(type)) {
            throw new IllegalArgumentException("Invalid content type");
        }
        return type;
    }

    public String buildImagePath(String hash, String contentType) {
        // Setup path for image to be saved
        String sanitizedHash = sanitizeHash(hash);
        String sanitizedContentType = sanitizeContentType(contentType);

        String directory = sanitizedHash.substring(0,2) + "/" + sanitizedHash.substring(2, 4);
        String fileName = sanitizedHash + "." + sanitizedContentType;

        Path baseDir = Paths.get(imagePath).toAbsolutePath().normalize();
        Path filePath = baseDir.resolve(directory).resolve(fileName).normalize();

        // Verify directory hierarchy
        if (!filePath.startsWith(baseDir)) {
            throw new SecurityException("Path traversal attempt detected");
        }
        return filePath.toString();
    }

    public void saveImageFile(String path, byte[] imageData) throws IOException, InvalidMessageException{
        // Ensure directory exists and try to save image to file
        Path filePath = Paths.get(path).toAbsolutePath().normalize();
        Path baseDir = Paths.get(imagePath).toAbsolutePath().normalize();

        // Verify hierarchy
        if (!filePath.startsWith(baseDir)) {
            throw new SecurityException("Path traversal attempt detected");
        }
        File dir = filePath.getParent().toFile();
        if (!dir.mkdirs() && !dir.exists()) {
            log.error(CREATE_DIR_ERR_MESSAGE, dir.getPath());
            throw new InvalidMessageException(CREATE_FOLD_ERR_MESSAGE);
        }

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            log.info("Creating image file {}", filePath);
            fos.write(imageData);
            fos.flush();
        }
    }

    public EbtsImage createAndSaveImage(String contentType, String hash, String path) {
        // Create EbtsImage instance for the image and save to DB
        EbtsImage image = new EbtsImage();
        image.setOriginalImageType(contentType);
        image.setImageHash(hash);
        image.setPath(path);

        return imageRepository.save(image);
    }

    private void updateImageFile(String srbHash, String hash) {
        // Update previously made file with EbtsImage
        ImageFile sFile = this.imageFileRepository.findByFileHash(srbHash);
        if (sFile != null) {
            if (!sFile.getImageHash().contains(hash)) {
                sFile.getImageHash().add(hash);
                this.imageFileRepository.save(sFile);
            }
        } else {
            sFile = new ImageFile();
            sFile.setFileHash(srbHash);
            sFile.getImageHash().add(hash);
            this.imageFileRepository.save(sFile);
        }
    }

    public Long uploadImage(String contentType, byte[] imageData, String srbHash, boolean isType17)
            throws NoSuchAlgorithmException, InvalidMessageException {
        log.info("uploadImage content-type:{}, bytes:{}", contentType, imageData.length);
        if (contentType == null || contentType.isEmpty()) {
            log.info("Setting content type to unknown");
            contentType = "image/unknown";
        }

        // hash the image bytes
        String hash = this.getHash(imageData);

        EbtsImage image = imageRepository.findByImageHash(hash);

        // If the image doesn't not exist, create it
        if (image == null) {
            String path = buildImagePath(hash, contentType);
            log.info("Image NOT found in db, has HASH: {}", hash);
            try {
                saveImageFile(path, imageData);
            } catch (IOException e){
                throw new InvalidMessageException(e.getMessage());
            }

            try {
                image = createAndSaveImage(contentType, hash, path);
            } catch (DataIntegrityViolationException e) {
                image = imageRepository.findByImageHash(hash);
            }

            // Process all services upon initial image upload
            if (isType17 && this.checkSize(imageData)) {
                this.serviceMessageHandler.processImageDefault(image);
            }

            if (StringUtils.isBlank(srbHash))
                srbHash = hash;

            updateImageFile(srbHash, hash);
        }

        return image.getId();
    }

    private boolean checkSize(byte[] imageData) {
        // Images should be <= 640 width and 480 height
        try {
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(imageData));
            log.info("Width: {} x Height: {}", bi.getWidth(), bi.getHeight());
            if (bi.getWidth() <= this.iwpWebProperties.getMaxWidthImage() &&
                    bi.getHeight() <= this.iwpWebProperties.getMaxHeightImage()) {
                log.info("Size is valid");
                return true;
            }
            log.info("Size is Horrific");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    public String getImageData(CreateIrisSearchRequest searchRequest) throws InvalidMessageException, IOException {
        EbtsImage ebtsImage = this.imageRepository
                .findById(searchRequest.getMessage().getImageData().getEbtsImageId())
                .orElseThrow(() -> new InvalidMessageException(DATA_NOT_FOUND_MESSAGE));
        byte[] imageData = EbtsImage.getImageBytesPng(ebtsImage);
        return new String(Base64.getEncoder().encode(imageData));
    }

    public boolean submitSearchRequest(long searchId)
            throws IOException, InvalidMessageException, EbtsBuildingException {
        log.info("submitSearchRequest for id: {}", searchId);
        // get the message from mysql
        Optional<CreateIrisSearchRequest> searchRequestOptional = createIrisSearchRequestRepository.findById(searchId);
        if (!searchRequestOptional.isPresent()) {
            log.error("Invalid id {}", searchId);
            return false;
        }
        CreateIrisSearchRequest searchRequest = searchRequestOptional.get();

        EbtsMessageRequest tempMessage = searchRequest.getMessage();
        tempMessage.setDateOfSubmission(new Date().toString());
        searchRequest.setMessage(tempMessage);

        byte[] ebtsFileBytes = this.buildEbtsMessage(searchRequest.getMessage());

        if (ebtsFileBytes.length != 0) {
            String filename = new SimpleDateFormat("'generatedEbts_'yyyyMMddHHmm'.eft'").format(new Date());
            new ByteArrayResource(ebtsFileBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

            return true;
        } else {
            return false;
        }
    }

    public long uploadImage(String contentType, String imageData)
            throws NoSuchAlgorithmException, InvalidMessageException {
        byte[] imageBytes = Base64.getDecoder().decode(imageData);
        return uploadImage(contentType, imageBytes, "", true);
    }

    public byte[] getImage(long imageId) throws InvalidMessageException {
        EbtsImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new InvalidMessageException(DATA_NOT_FOUND_MESSAGE));

        if (image == null) {
            return new byte[0];
        }

        try {
            log.info("Getting bytes for image at {}", image.getPath());
            byte[] buffer = EbtsImage.getImageBytes(image);
            log.info("Image Size: {}", buffer.length);
            return this.convertImage(image.getOriginalImageType(), buffer);
        } catch (IOException e) {
            log.error("Unable to get image {}", e.getMessage());
            return new byte[0];
        }

    }

    public EbtsImage getEbtsImage(long imageId) throws InvalidMessageException {
        return imageRepository.findById(imageId).orElseThrow(() -> new InvalidMessageException(DATA_NOT_FOUND_MESSAGE));
    }

    public byte[] getImageThumbnail(long imageId) throws InvalidMessageException, IOException {
        EbtsImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new InvalidMessageException(DATA_NOT_FOUND_MESSAGE));
        if (image == null) {
            return new byte[0];
        }

        return EbtsImage.buildThumbnail(image);
    }

    public ReportData submitIrisXmlRequest(byte[] irisFile, Long userId, String fileName)
            throws NoSuchAlgorithmException, InvalidMessageException {
        String xmlHash = this.getHash(irisFile);
        ReportData response = new ReportData();
        if (irisFile != null && irisFile.length > 0) {
            Request req = parseXmlRequest(irisFile);
            response.setResponseType(ResponseType.XML);
            response.getProbe().setSubjectIdentifier(req.getProbe().getUcn());

            log.info("Not null or empty! Printing the xml Hash: {}\n", xmlHash);
            for (ImageType image : req.getProbe().getEncounter().getImages()) {
                response.getProbe().getImageList().add(this.createImageData(image, xmlHash));
            }

            for (IdentityType ident : req.getTargets().getTarget()) {
                Candidate candidate = new Candidate();
                candidate.setSubjectIdentifier(ident.getUcn());
                response.getCandidates().add(candidate);
                for (EncounterType encounter : ident.getEncounters().getEncounter()) {
                    for (ImageType image : encounter.getImages()) {
                        candidate.getImageList().add(this.createImageData(image, xmlHash));
                    }
                }
            }
        } else {
            // If file is null or empty, nothing to do
            response.getErrorMessages().add("Input XML file is empty");

        }

        UserAuthentication ua = userAuthenticationRepository.findById(userId).orElse(null);
        if (response.getCreatedBy() == null || response.getCreatedBy().getId() == null)
            response.setCreatedBy(ua);
        response.setWorkedBy(ua);
        response.getCaseInformation().setReceivedDate(Instant.now().toEpochMilli());
        response.getCaseInformation().setComparisonDate(Instant.now().toEpochMilli());
        response.setFileName(fileName);

        this.reportDataRepository.save(response);
        return response;
    }

    public ImageData createImageData(String contentType, byte[] imageData)
            throws NoSuchAlgorithmException, InvalidMessageException {
        String hash = this.getHash(imageData);
        log.info("Image Hash: {}", hash);
        ImageData iData = new ImageData();
        iData.setEbtsImageId(this.uploadImage(contentType, imageData, hash, true));
        return this.imageDataRepository.save(iData);
    }

    public ImageData createImageData(ImageType image, String xmlHash)
            throws NoSuchAlgorithmException, InvalidMessageException {
        ImageData imageData = new ImageData(this.uploadImage("image/png", image.getValue(), xmlHash, true));
        if (image.getDme() != null) {
            imageData.setDme(image.getDme().toString());
        }
        imageData.setEyeLabel(EyeLabel.valueOf(image.getElr().intValue()));

        imageData = this.imageDataRepository.save(imageData);
        this.imageDataRepository.flush();
        return imageData;
    }

    public void saveIrisMessage(IrisMessage irisMessage, String srbHash) throws NoSuchAlgorithmException {
        // go through the images and save them in the ebts images collection
        irisRepository.findByFileHash(srbHash);
        List<Type17> t17List = irisMessage.getType17();
        for (Type17 type17 : t17List) {
            try {
                type17.setEbtsImageId(this.uploadImage(Type17.getContentType(type17.getCompressionAlgorithm()),
                        type17.getData(), srbHash, true));
            } catch (InvalidMessageException e) {
                log.error("Error saving iris message image: {}", type17.getCompressionAlgorithm());
            }
        }

        irisRepository.save(irisMessage);
    }

    public ReportData saveReportData(byte[] fileInfo, Long userId, boolean isSrb)
            throws InvalidMessageException, NoSuchAlgorithmException {
        String fileName = UUID.randomUUID().toString();
        String directory = uploadPath;
        String path = directory + File.separator + fileName;

        File dir = new File(directory);
        if (!dir.mkdirs() && !dir.exists()) {
            log.error(CREATE_DIR_ERR_MESSAGE, directory);
            throw new InvalidMessageException(CREATE_FOLD_ERR_MESSAGE);
        }

        try (FileOutputStream fos = new FileOutputStream(path)) {
            log.info("Creating file {}", path);
            fos.write(fileInfo);
            fos.flush();
        } catch (IOException e) {
            log.error(CREATE_FILE_ERR_MESSAGE, fileName);
            throw new InvalidMessageException(e.getMessage());
        }

        if (isSrb) {
            return this.submitSrb(fileInfo, userId, fileName);
        } else {
            return this.submitIrisXmlRequest(fileInfo, userId, fileName);
        }
    }

    public byte[] getSrbData(String fileName) throws InvalidMessageException, IOException {
        String path = uploadPath + File.separator + fileName;
        File f = new File(path);
        if (!f.exists()) {
            log.error("Unable to find srb file {}", path);
            throw new InvalidMessageException("Unable to find srb file : " + path);
        }
        return Files.readAllBytes(f.toPath());
    }

    public ReportData submitSrb(byte[] srbInfo, Long userId, String fileName) throws NoSuchAlgorithmException {
        String srbHash = this.getHash(srbInfo);
        ReportData response = new ReportData();

        try {
            Ebts ebts = EbtsParser.parse(srbInfo, Type7Handling.FLEX);
            IrisMessage irisMessage = this.irisRepository.findByFileHash(srbHash);
            if (irisMessage == null) {
                irisMessage = IrisMessage.buildFromEbts(ebts);
                this.saveIrisMessage(irisMessage, srbHash);
            }
            response = getReportDataResponse(irisMessage);
        } catch (EbtsParsingException exc) {
            log.warn("Failed to parse EBTS File", exc);
            response.addErrorMessages(exc.getMessage());
        }

        UserAuthentication ua = userAuthenticationRepository.findById(userId).orElse(null);
        if (response.getCreatedBy() == null || response.getCreatedBy().getId() == null)
            response.setCreatedBy(ua);
        response.setWorkedBy(ua);

        response.getCaseInformation().setReceivedDate(Instant.now().toEpochMilli());
        response.getCaseInformation().setComparisonDate(Instant.now().toEpochMilli());
        response.setFileName(fileName);
        response = this.reportDataRepository.save(response);

        return response;
    }

    public ReportData getReportDataResponse(IrisMessage irisMessage) {
        ReportData response = new ReportData();

        if (irisMessage.getType1() == null) {
            String errorMessage = "Missing Type 1 Record";
            response.getErrorMessages().add(errorMessage);
            log.debug(errorMessage);
        } else {
            Type1 t1 = irisMessage.getType1();
            response.getProbe().setTransactionControlNumber(t1.getTransactionControlNumber());
            response.getProbe().setTransactionControlReference(t1.getTransactionControlReference());
        }

        List<CandidateInvesitgativeListField> cilList = new ArrayList<>();
        if (irisMessage.getType2() == null) {
            String errorMessage = "Missing Type 2 Record";
            response.getErrorMessages().add(errorMessage);
            log.debug(errorMessage);
        } else {
            Type2 t2 = irisMessage.getType2();

            if (!t2.getStatusErrorMessage().isEmpty()) {
                response.setResponseType(ResponseType.ERRB);
                for (String errorMessage : t2.getStatusErrorMessage()) {
                    response.getErrorMessages().add(errorMessage);
                    log.debug(errorMessage);
                }

            } else {
                response.setResponseType(ResponseType.SRB);
                if (t2.getCandidateInvesitgativeListField() == null
                        || t2.getCandidateInvesitgativeListField().isEmpty()) {
                    String errorMessage = "Missing Typ2 CNL Field";
                    response.getErrorMessages().add(errorMessage);
                    log.debug(errorMessage);
                } else {
                    cilList = t2.getCandidateInvesitgativeListField();
                }

                if (irisMessage.getType17() == null) {
                    String errorMessage = "Missing Type 17 Record";
                    response.getErrorMessages().add(errorMessage);
                    log.debug(errorMessage);
                } else {
                    List<Type17> t17List = irisMessage.getType17();

                    if (t17List.isEmpty()) {
                        String errorMessage = "Type 17 Record not contain image data";
                        response.getErrorMessages().add(errorMessage);
                        log.debug(errorMessage);
                    } else if (t17List.size() == 1) {
                        String errorMessage = "Type 17 Record missing probe image data";
                        response.getErrorMessages().add(errorMessage);
                        log.debug(errorMessage);
                    }

                    if (t17List.size() != cilList.size() + 1) {
                        String errorMessage = String.format(
                                "Type 17 record size %s does not correspond to Type 2 Candidate List Size %s",
                                t17List.size(), cilList.size());
                        response.getErrorMessages().add(errorMessage);
                        log.debug(errorMessage);
                    } else {

                        for (int i = 0; i < t17List.size(); i++) {
                            Type17 t17 = t17List.get(i); // Probe Image
                            if (t17.getData() == null) {
                                String errorMessage = String.format("Image data missing from Type17 Field %s", i);
                                response.getErrorMessages().add(errorMessage);
                                log.debug(errorMessage);
                            } else {
                                if (i == 0) {
                                    ImageData imageData = new ImageData(t17.getEbtsImageId());
                                    imageData.setRecordType(17);
                                    imageData = this.imageDataRepository.save(imageData);
                                    response.getProbe().getImageList().add(imageData);
                                } else {
                                    CandidateInvesitgativeListField cil = cilList.get(i - 1);
                                    Candidate cand = new Candidate();
                                    ImageData imageData = new ImageData(t17.getEbtsImageId());
                                    imageData.setRecordType(17);
                                    imageData = this.imageDataRepository.save(imageData);
                                    cand.getImageList().add(imageData);
                                    cand.setScore(Integer.toString(cil.getMatchScore()));
                                    cand.setSubjectIdentifier(cil.getSubjectIdentifier());
                                    response.getCandidates().add(cand);
                                }
                            }
                        }
                    }
                }
            }
        }

        return response;
    }

    public byte[] buildEbtsMessage(EbtsMessageRequest iMess)
            throws IOException, InvalidMessageException, EbtsBuildingException {
        Ebts ebts = new Ebts();
        if (iMess == null) {
            EbtsTransactionCreator creator = new EbtsTransactionCreator();
            ebts = creator.createDefault();
        } else if (iMess.isValid()) {
            LogicalRecord t1Record = new GenericRecord(1);
            LogicalRecord t2Record = new GenericRecord(2);
            LogicalRecord t17Record = new GenericRecord(17);

            // Handle Type 1 Record Mandatory Fields
            t1Record.setField(4, EbtsConversionHelper.getFieldFromString("EBTS"));
            t1Record.setField(5, EbtsConversionHelper.getFieldFromString(iMess.getDateOfSubmission()));
            t1Record.setField(7, EbtsConversionHelper.getFieldFromString(iMess.getDestinationAgencyIdentifier()));
            t1Record.setField(8, EbtsConversionHelper.getFieldFromString(iMess.getOriginatingAgencyIdentifier()));
            t1Record.setField(9, EbtsConversionHelper.getFieldFromString(iMess.getTransactionControlNumber()));

            // Handle Type 2 Record Mandatory Fields
            t2Record.setField(6, EbtsConversionHelper.getFieldFromString(iMess.getAttentionIndicator()));
            t2Record.setField(10,
                    EbtsConversionHelper.getFieldFromString(iMess.getCinPrefix(), iMess.getCinIdentifier()));
            t2Record.setField(11, EbtsConversionHelper.getFieldFromString(Integer.toString(iMess.getCaseExtension())));
            t2Record.setField(79,
                    EbtsConversionHelper.getFieldFromString(Integer.toString(iMess.getNumberOfCandidates())));

            // Handle Type 17 Record Mandatory Fields
            t17Record.setField(3, EbtsConversionHelper
                    .getFieldFromString(Integer.toString(iMess.getImageData().getEyeLabel().getValue())));
            t17Record.setField(4, EbtsConversionHelper.getFieldFromString(iMess.getSourceAgency()));
            t17Record.setField(5, EbtsConversionHelper.getFieldFromString(iMess.getIrisCaptureDate()));

            if (iMess.getRotationOfEye() >= 0) {
                t17Record.setField(14,
                        EbtsConversionHelper.getFieldFromString(Integer.toString(iMess.getRotationOfEye())));
                t17Record.setField(15,
                        EbtsConversionHelper.getFieldFromString(Integer.toString(iMess.getRotationUncertainty())));
            }

            if (iMess.getImageData() != null) {

                byte[] imageData = Base64.getDecoder().decode(iMess.getImageData().getImageData());
                InputStream in = new ByteArrayInputStream(imageData);
                BufferedImage image = ImageIO.read(in);

                t17Record.setImageData(imageData); // Shouldn't need to convert bytes???
                t17Record.setField(6, EbtsConversionHelper.getFieldFromInt(image.getWidth()));
                t17Record.setField(7, EbtsConversionHelper.getFieldFromInt(image.getHeight()));
                t17Record.setField(9, EbtsConversionHelper.getFieldFromInt(image.getWidth()));
                t17Record.setField(10, EbtsConversionHelper.getFieldFromInt(image.getHeight()));

                // Check ANN conditions
                if (iMess.getImageData().getId() == -1) {
                    EbtsImage eImage = imageRepository.findById(iMess.getImageData().getEbtsImageId())
                            .orElseThrow(() -> new InvalidMessageException(DATA_NOT_FOUND_MESSAGE));
                    String imageType = eImage.getOriginalImageType();
                    if (this.needs902(imageType)) {
                        // Get Current Date/Time in GMT - 2013-08-01T02:23:44Z
                        Date currDate = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

                        String nav = "APN-IWP";
                        String own = "MITRE";
                        String pro = String.format("MITRE developed algorithm to convert '%s' to 'image/png'",
                                imageType);

                        t17Record.setField(902,
                                EbtsConversionHelper.getFieldFromString(sdf.format(currDate), nav, own, pro));
                    }
                }
            }

            ebts.addRecord(t1Record);
            ebts.addRecord(t2Record);
            ebts.addRecord(t17Record);
        } else {
            log.debug(iMess.getErrors());
        }

        EbtsBuilder eBuilder = new EbtsBuilder();
        return eBuilder.build(ebts);
    }

    public byte[] storeEbtsFile(CreateIrisSearchRequest createIrisSearchRequest)
            throws InvalidMessageException, NoSuchAlgorithmException, IOException, EbtsBuildingException {
        byte[] ebtsFile = this.buildEbtsMessage(createIrisSearchRequest.getMessage());
        // Saving PDF to upload path, if the PDF was created successfully
        if (ebtsFile != null) {
            String hash = this.getHash(ebtsFile);
            String directory = uploadPath + "/" + hash.substring(0, 2) + "/" + hash.substring(2, 4);
            String path = String.format("%s/%s.ebts", directory, hash);

            File f = new File(path);
            log.info("File exists:{} path:{}", f.exists(), path);

            File dir = new File(directory);
            if (!dir.mkdirs() && !dir.exists()) {
                log.error(CREATE_DIR_ERR_MESSAGE, directory);
                throw new InvalidMessageException(CREATE_FOLD_ERR_MESSAGE);
            }

            createIrisSearchRequest.setFilePath(path);

            this.createIrisSearchRequestRepository.save(createIrisSearchRequest);
            try (FileOutputStream fos = new FileOutputStream(path)) {
                log.info("Creating pdf file {}", path);
                fos.write(ebtsFile);
                fos.flush();
            } catch (IOException e) {
                log.error(CREATE_FILE_ERR_MESSAGE, createIrisSearchRequest.getFilePath());
                throw new InvalidMessageException(e.getMessage());
            }
        }

        return ebtsFile;
    }

    public boolean needs902(String imageType) {
        String temp = imageType.toLowerCase();
        return temp.endsWith("jpeg") || temp.endsWith("jpg");
    }

    public Request parseXmlRequest(byte[] irisFile) {
        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(Request.class);

            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new ByteArrayInputStream(irisFile));

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Request) jaxbUnmarshaller.unmarshal(xsr);
        } catch (JAXBException | XMLStreamException exception) {
            log.debug(exception.getMessage());
            return null;
        }
    }

    private byte[] convertImage(String contentType, byte[] imageData) throws IOException {
        if (contentType.equals("image/png")) {
            return imageData;
        }

        contentType = contentType.replace("image/", "").toUpperCase();
        BufferedImage img;

        if (contentType.equalsIgnoreCase("wsq20")) {
            BitmapWithMetadata bitmapWithMetadata = WSQDecoder.decode(new ByteArrayInputStream(imageData));
            imageData = bitmapWithMetadata.getPixels();
            contentType = "NONE";
            img = ImageUtils.read(contentType, bitmapWithMetadata.getWidth(), bitmapWithMetadata.getHeight(),
                    8, imageData);
        } else {
            img = ImageUtils.read(contentType, 16, 16, 8, imageData);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(img, "png", outputStream);

        return outputStream.toByteArray();
    }

    public String getHash(byte[] byteArray) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(byteArray);
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    public CreateIrisSearchRequest submitCreateImage(String mimeType, byte[] bytes, Long userId) {
        CreateIrisSearchRequest createIrisSearchRequest = new CreateIrisSearchRequest();
        EbtsMessageRequest request = new EbtsMessageRequest();
        try {
            request.setImageData(this.createImageData(mimeType, bytes));

            Date irisCaptureDate = new Date();
            SimpleDateFormat dateToString = new SimpleDateFormat("yyyyMMdd");
            String irisCaptureDateString = dateToString.format(irisCaptureDate);
            request.setIrisCaptureDate(irisCaptureDateString);
        } catch (Exception e) {
            request.addErrorMessages(e.getMessage());
        }

        createIrisSearchRequest.setMessage(request);
        createIrisSearchRequest.setUser(userAuthenticationRepository.findById(userId).orElse(null));

        createIrisSearchRequest.setCreationDateEpoch(Instant.now().toEpochMilli());
        return this.createIrisSearchRequestRepository.save(createIrisSearchRequest);
    }

    public void createTshepiiCompareRequest(Long probeId, Long candidateId) {
        this.serviceMessageHandler.requestTshepiiComparison(probeId, candidateId);
    }

    public boolean isMultiModal(byte[] srbFile) throws EbtsParsingException {
        // Assume if the file contains image data thats NOT in type 17 records, it's a
        // multimodal request
        Ebts ebts = EbtsParser.parse(srbFile, Type7Handling.FLEX);

        Map<Integer, Integer> recordListing = ebts.getLogicalRecordCounts();

        for (Map.Entry<Integer, Integer> entry : recordListing.entrySet()) {
            Integer recordNumber = entry.getKey();

            if (this.skipCheckRecords.contains(recordNumber)) {
                // skip
                continue;
            }

            if (recordListing.get(recordNumber) > 0) {
                List<LogicalRecord> recordList = ebts.getRecordsByType(recordNumber);

                for (LogicalRecord logicalRecord : recordList) {
                    if (logicalRecord.getImageData().length > 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public List<ImageData> createImageDataFromEbts(byte[] srbFile) throws EbtsParsingException {
        List<ImageData> imageDataList = new ArrayList<>();

        Ebts ebts = EbtsParser.parse(srbFile, Type7Handling.FLEX);
        Map<Integer, Integer> occurrenceCount = new HashMap<>();

        for (LogicalRecord logicalRecord : ebts.getAllRecords()) {
            if (logicalRecord.getRecordType() == 1 ||
                    logicalRecord.getRecordType() == 2) {
                // skip
                continue;
            }

            if (logicalRecord.getImageData() == null || logicalRecord.getImageData().length == 0) {
                // nothing to do
                continue;
            }

            occurrenceCount.merge(logicalRecord.getRecordType(), 1, Integer::sum);

            byte[] imageData = logicalRecord.getImageData();
            String contentType = "image/none";
            if (logicalRecord.getRecordType() < 10) {
                if (logicalRecord.hasField(8)) {
                    contentType = Type17.getBinaryContentType(logicalRecord.getField(8).toString());
                }
            } else {
                if (logicalRecord.hasField(11)) {
                    contentType = Type17.getContentType(logicalRecord.getField(11).toString());
                }
            }
            try {
                String hash = this.getHash(imageData);
                log.info("Image Hash: {}", hash);
                log.info("Content Type {}", contentType);
                ImageData iData = new ImageData();
                iData.setImageNotes(String.valueOf(logicalRecord.getRecordType()));
                iData.setEbtsImageId(
                        this.uploadImage(contentType, imageData, hash, logicalRecord.getRecordType() == 17));
                iData.setEyeLabel(EyeLabel.UNDEFINED);
                imageDataList.add(this.imageDataRepository.save(iData));
                iData.setRecordType(logicalRecord.getRecordType());
                iData.setRecordIndex(occurrenceCount.get(logicalRecord.getRecordType()));

            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }

        return imageDataList;
    }

}
