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
import org.mitre.iwp.web.dto.EbtsMapper;
import org.mitre.iwp.web.dto.PersonDTO;
import org.mitre.iwp.web.dto.ProfileDTO;
import org.mitre.iwp.web.dto.UserMapper;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.model.*;
import org.mitre.iwp.web.security.IwpAuthService;
import org.mitre.iwp.web.service.IrisService;
import org.mitre.iwp.web.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.util.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

@RestController
public class EbtsController {
    private static final Logger log = LoggerFactory.getLogger(EbtsController.class);

    private final IrisService irisService;

    private final ReportService reportService;

    private final IwpAuthService authService;

    private final EbtsMapper ebtsMapper;

    private final UserMapper userMapper;

    private static final String APP_OCT_STRING = "application/octet-stream";

    public EbtsController(IrisService irisService, ReportService reportService,
            IwpAuthService authService, EbtsMapper ebtsMapper, UserMapper userMapper) {
        this.irisService = irisService;
        this.reportService = reportService;
        this.authService = authService;
        this.ebtsMapper = ebtsMapper;
        this.userMapper = userMapper;
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @PostMapping(value = "/api/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile mpFile) throws IOException {
        try {
            return ResponseEntity.ok()
                    .body(String
                            .valueOf(irisService.uploadImage(mpFile.getContentType(), mpFile.getBytes(), "", true)));
        } catch (Exception exception) {
            logError(exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @PostMapping(value = "/api/iris/create")
    public ResponseEntity<String> uploadCreateImage(MultipartHttpServletRequest request, Principal principal) {
        UserAuthentication auth;
        Gson gson = new Gson();
        try {
            auth = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        CreateIrisSearchRequest response = new CreateIrisSearchRequest();
        try {
            Iterator<String> itr = request.getFileNames();
            log.info("Uploading Create image");
            while (itr.hasNext()) {
                String uploadedFile = itr.next();
                MultipartFile file = request.getFile(uploadedFile);
                if (file != null) {
                    String mimeType = file.getContentType();
                    byte[] bytes = file.getBytes();
                    response = irisService.submitCreateImage(mimeType, bytes, auth.getId());
                } else {
                    response.getMessage().addErrorMessages(String.format("Unable to process file %s", uploadedFile));
                }
            }
        } catch (Exception e) {
            if (response.getMessage() == null) {
                response.setMessage(new EbtsMessageRequest());
            }

            response.getMessage().addErrorMessages(e.getMessage());
        }

        return ResponseEntity.ok().body(gson.toJson(response));
    }

    @Secured({ "ROLE_USER" })
    @GetMapping(value = "/api/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") long id) {
        try {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(irisService.getImage(id));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.IMAGE_PNG).body(null);
        }
    }

    @Secured({ "ROLE_USER" })
    @GetMapping(value = "/api/image/thumb/{id}")
    public ResponseEntity<byte[]> getImageThumbnail(@PathVariable("id") long id) {
        try {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(irisService.getImageThumbnail(id));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.IMAGE_PNG).body(null);
        }
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/iris/response/{srb}")
    public ResponseEntity<String> getResponseForSrbPath(@PathVariable("srb") String srb, Principal principal) {
        log.info("/api/iris/response/{}", srb);

        UserAuthentication auth;
        try {
            auth = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        ReportData returnData = this.reportService.getReportByFileName(srb);

        if (null != returnData) {
            Gson gson = new GsonBuilder().create();
            return ResponseEntity.ok().body(gson.toJson(returnData));
        }
        try {
            File f = new File(srb);
            byte[] srbFile = irisService.getSrbData(srb);
            ReportData response = irisService.submitSrb(srbFile, auth.getId(), f.getName());
            Gson gson = new GsonBuilder().create();
            return ResponseEntity.ok().body(gson.toJson(response));
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting response for srb " + srb);
        }
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @PostMapping(value = "/api/iris/response")
    public ResponseEntity<String> submitResponse(@RequestParam("file") MultipartFile mpFile, Principal principal) {
        UserAuthentication auth;
        try {
            auth = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        ReportData response = new ReportData();
        Gson gson = new GsonBuilder().create();
        log.info("/api/iris/response");
        try {
            String contentType = mpFile.getContentType();
            String mpFileName = mpFile.getOriginalFilename();
            if (contentType == null) {
                response.getErrorMessages().add("Unknown File Content Type");
            } else {
                byte[] srbFile = mpFile.getBytes();
                log.info("/api/iris/response contentType:{} bytes:{}", contentType, srbFile.length);
                if (contentType.equals(APP_OCT_STRING)) {
                    if (irisService.isMultiModal(srbFile)) {
                        List<ImageData> iDataList = irisService.createImageDataFromEbts(srbFile);
                        return ResponseEntity.ok().body(gson.toJson(iDataList));
                    } else {
                        response = irisService.submitSrb(srbFile, auth.getId(), mpFileName);
                    }
                } else if (contentType.equals("text/xml") || contentType.equals("application/xml")) {
                    response = irisService.submitIrisXmlRequest(srbFile, auth.getId(), mpFileName);
                } else if (contentType.startsWith("image")) {
                    List<ImageData> iDataList = new ArrayList<>();
                    if (mpFileName != null) {
                        String mpFileExtension = mpFileName.substring(mpFileName.lastIndexOf(".") + 1);
                        iDataList.add(
                                this.irisService.createImageData(contentType, checkImageType(mpFile, mpFileExtension)));
                        return ResponseEntity.ok().body(gson.toJson(iDataList));
                    } else {
                        log.error("Original filename is null!");
                        throw new IOException("Original filename is null!");
                    }
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            response.addErrorMessages(e.getMessage());
        }

        return ResponseEntity.ok().body(gson.toJson(response));
    }// end of submitSrb

    public byte[] checkImageType(MultipartFile mpFile, String fileExtension) throws IOException {
        // Read the multipart file into a BufferedImage
        BufferedImage img = ImageIO.read(mpFile.getInputStream());

            // Check whether the image loaded successfully.
        if (img != null) {
            // Get and check the image-data type.
            // If the image is 16-bit unsigned (ushort)
            int type = img.getType();
            if (type == BufferedImage.TYPE_USHORT_GRAY) {
                // Convert to 8-bit grayscale
                int width = img.getWidth();
                int height = img.getHeight();

                // TO CONVERT TO TYPE_BYTE_GRAY
                byte[] img8bit = new byte[width * height];

                int index = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = img.getRaster().getSample(x, y, 0);
                        // Normalize the 16-bit value to 8-bit
                        byte normalizedPixel = (byte) (((long) pixel * 255) / 65535);
                        img8bit[index++] = normalizedPixel;
                    }
                }

                // img8bit is now an 8-bit representation of your original image
                try {
                    BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
                    newImage.getRaster().setDataElements(0, 0, width, height, img8bit);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ImageIO.write(newImage, fileExtension, outputStream);
                    return outputStream.toByteArray();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } else {
            log.error("Error opening image!");
            throw new IOException("Error opening image!");
        }
        return mpFile.getBytes();
    }

    @Secured({ "ROLE_CASE_SUPERVISOR" })
    @PostMapping(value = "/api/caseinfo")
    public ResponseEntity<String> uploadPerson(@RequestBody PersonDTO person) {
        try {
            return ResponseEntity.ok()
                    .body(String.valueOf(this.reportService.savePerson(this.ebtsMapper.toPerson(person))));
        } catch (Exception exception) {
            logError(exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }
    }

    @Secured({ "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/caseinfo/persons")
    public ResponseEntity<String> listPersons() {
        List<Person> response = this.reportService.listPersons();
        Gson gson = new GsonBuilder().create();
        return ResponseEntity.ok().body(gson.toJson(response));
    }

    @Secured({ "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/caseinfo/persons/{id}")
    public ResponseEntity<String> getPerson(@PathVariable("id") long id) {
        Person person = this.reportService.getPerson(id);
        Gson gson = new GsonBuilder().create();
        return ResponseEntity.ok().body(gson.toJson(person));
    }

    @Secured({ "ROLE_CASE_SUPERVISOR" })
    @PostMapping(value = "/api/caseinfo/delete/person")
    public ResponseEntity<String> deletePerson(@RequestBody PersonDTO person) {
        this.reportService.removePerson(person.getId());
        return ResponseEntity.ok().body(String.format("Person '%s' deleted.", person.getId()));
    }

    @Secured({ "ROLE_CASE_WORKER" })
    @PostMapping(value = "/api/service/tshepii/compare")
    public ResponseEntity<String> getTshepiiCompare(@RequestBody Map<String, String> msg) {
        List<TshepiiResponse> responseList = new ArrayList<>();
        TshepiiResponse response = new TshepiiResponse();
        Gson gson = new GsonBuilder().create();

        responseList.add(response);
        return ResponseEntity.ok().body(gson.toJson(responseList));
    }

    @Secured({ "ROLE_CASE_WORKER" })
    @PostMapping(value = "/api/service/tshepii/compareResponses")
    public ResponseEntity<String> getTshepiiCompareFromResponse(@RequestBody Map<String, TshepiiResponse> msg) {
        List<TshepiiResponse> responseList = new ArrayList<>();
        TshepiiResponse response = new TshepiiResponse();
        Gson gson = new GsonBuilder().create();

        responseList.add(response);
        return ResponseEntity.ok().body(gson.toJson(responseList));
    }

    @Secured({ "ROLE_ADMIN" })
    @PostMapping(value = "/api/iris/create-profile")
    public ResponseEntity<String> createProfile(@RequestBody ProfileDTO profile) throws InvalidMessageException {
        long response = irisService.createProfile(this.userMapper.toProfile(profile));
        Gson gson = new GsonBuilder().create();
        return ResponseEntity.ok().body(gson.toJson(response));
    }

    @Secured({ "ROLE_ADMIN" })
    @GetMapping(value = "/api/iris/profiles")
    public ResponseEntity<String> listProfiles() {
        List<Profile> response = irisService.listProfiles();
        Gson gson = new GsonBuilder().create();
        return ResponseEntity.ok().body(gson.toJson(response));
    }

    @Secured({ "ROLE_ADMIN" })
    @PostMapping(value = "/api/iris/update-profile")
    public ResponseEntity<String> updateProfile(@RequestBody ProfileDTO profile) throws InvalidMessageException {
        long response = irisService.updateProfile(this.userMapper.toProfile(profile));
        Gson gson = new GsonBuilder().create();
        return ResponseEntity.ok().body(gson.toJson(response));
    }

    @Secured({ "ROLE_ADMIN" })
    @PostMapping(value = "/api/iris/deleteProfile/{id}")
    public ResponseEntity<String> removeProfile(@PathVariable("id") long id) throws InvalidMessageException {
        irisService.deleteProfile(id);
        return ResponseEntity.ok().body(String.format("Profile '%s' deleted.", id));
    }

    private void logError(Exception exc) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exc.printStackTrace(pw);
        log.error("Error: {}", sw);
    }
}
