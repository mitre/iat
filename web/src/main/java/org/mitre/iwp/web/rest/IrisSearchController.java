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

import com.google.common.net.HttpHeaders;
import org.mitre.iwp.web.dto.CreateIrisSearchRequestDTO;
import org.mitre.iwp.web.dto.ImageMessageMapper;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.model.*;
import org.mitre.iwp.web.security.IwpAuthService;
import org.mitre.iwp.web.service.AnnotationRepository;
import org.mitre.iwp.web.service.AnnotationValueRepository;
import org.mitre.iwp.web.service.CreateIrisSearchRequestRepository;
import org.mitre.iwp.web.service.IrisService;
import org.mitre.iwp.web.service.ReportDataService;
import org.mitre.jet.exceptions.EbtsBuildingException;
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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@RestController
public class IrisSearchController {

    private static final Logger logger = LoggerFactory.getLogger(IrisSearchController.class);

    private final IrisService irisService;

    private final CreateIrisSearchRequestRepository createIrisSearchRequestRepository;

    private final IwpAuthService authService;

    private final ImageMessageMapper imageMessageMapper;

    private final AnnotationValueRepository avRepository;

	private final ReportDataService reportDataService;

	private final AnnotationRepository annotationRepository;


    public IrisSearchController(IrisService irisService,
                                CreateIrisSearchRequestRepository createIrisSearchRequestRepository, IwpAuthService authService,
                                ImageMessageMapper imageMessageMapper, AnnotationValueRepository avRepository, ReportDataService reportDataService, AnnotationRepository annotationRepository) {
        this.irisService = irisService;
        this.createIrisSearchRequestRepository = createIrisSearchRequestRepository;
        this.authService = authService;
        this.imageMessageMapper = imageMessageMapper;
		this.avRepository = avRepository;
		this.reportDataService = reportDataService;
		this.annotationRepository = annotationRepository;
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/iris/search/count")
    public ResponseEntity<?> getSearchRequestCount(Principal principal) {
        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }
        Long count = this.createIrisSearchRequestRepository.countAllByUser_Id(ua.getId());
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/iris/search/image")
    public ResponseEntity<?> getSearchRequestImage(@RequestParam(value = "id") long id) {
        Optional<CreateIrisSearchRequest> cisrOption = createIrisSearchRequestRepository.findById(id);
        if (cisrOption.isPresent()) {
            CreateIrisSearchRequest request = cisrOption.get();
            byte[] imageBytes = request.getMessage().getImageData().getImageBytes();
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes);
        }
        return ResponseEntity.notFound().build();
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/iris/search/list")
    @ResponseBody
    public ResponseEntity<?> getCreateIrisSearchMessages(
            @RequestParam(value = "start", defaultValue = "0") Integer start,
            @RequestParam(value = "limit", defaultValue = "-1") Integer limit,
            @RequestParam(value = "sort", defaultValue = "creationDateEpoch") String sortColumn,
            @RequestParam(value = "direction", defaultValue = "desc") String direction, Principal principal) {

        Sort.Direction sortDirection = Sort.Direction.DESC;
        if (direction.equalsIgnoreCase("asc")) {
            sortDirection = Sort.Direction.ASC;
        }

        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        if (limit == -1) {
            limit = Integer.MAX_VALUE;
        }

        logger.info("Pulling Search Requests for userId: {}", ua.getId());
        List<CreateIrisSearchRequest> returnList = this.createIrisSearchRequestRepository.findByUser_Id(ua.getId(),
                PageRequest.of(start, limit, Sort.by(sortDirection, sortColumn)));
        logger.info("Return list size: {}", returnList.size());

        return ResponseEntity.ok().body(returnList);
    }

    // creates an ebts search message from a previously created
    // 'CreateIrisSearchRequest"
    // used on the history page to create the ebts search message
    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/iris/search/download/{id}")
    public ResponseEntity<?> downloadIrisSearchMessage(@PathVariable(value = "id") long id, Principal principal) {
        logger.info("/api/iris/search/download Trying to download file with id: {}", id);
        try{
            String filePath = this.findReportById(id);
            try(FileInputStream fis = new FileInputStream(filePath)) {
                byte[] doc = new byte[fis.available()];
                if(fis.read(doc) <= 0) {
                    throw new IOException(String.format("Unable to read file %s", filePath));
                }
                Resource file = new ByteArrayResource(doc);

                if (doc.length == 0) {
                    return ResponseEntity.badRequest().body("Error Pulling Report Info");
                }

                return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + new File(filePath).getName() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(file);
            }

        } catch (Exception docEx) {
            logger.info(docEx.getMessage());
            return ResponseEntity.badRequest().body(docEx.getMessage());
        }
    }

    // creates a new create iris search message and saves it
    private CreateIrisSearchRequest saveCreateIrisSearchMessage(CreateIrisSearchRequest createIrisSearchRequest) throws InvalidMessageException {
        if (createIrisSearchRequest.getMessage() != null
                && createIrisSearchRequest.getMessage().getImageData() != null) {
            if (createIrisSearchRequest.getId() == null || createIrisSearchRequest.getId() == 0) {
                createIrisSearchRequest.setCreationDateEpoch(Instant.now().getEpochSecond());
            } 

			Optional<CreateIrisSearchRequest> originalCISR = createIrisSearchRequest.getId() != null ? this.createIrisSearchRequestRepository.findById(createIrisSearchRequest.getId()) : Optional.empty();

			if(originalCISR.isPresent()) {
				List<Annotation> annotations = originalCISR.get().getMessage().getImageData().getAnnotations();

				Set<String> collectedAnnotations = new HashSet<>();

				//Gather all annotations IDs together into a set
				for (Annotation a : annotations) {
					reportDataService.collectAnnotationIds(collectedAnnotations, a);
				}

				//Go through the annotations and remove the annotation values from the variable and then delete them from the DB
				for (String annID : collectedAnnotations) {
					if(this.annotationRepository.existsById(annID)){

						Annotation currAnnotation = this.annotationRepository.getReferenceById(annID);
						
						if (currAnnotation.getValue() != null) {
							long id = currAnnotation.getValue().getId();
							currAnnotation.setValue(null);
							avRepository.deleteById(id);
							avRepository.flush();
						} else {
							currAnnotation.setValue(null);
						}

						avRepository.flush();
					}
				}

				//Dereference all annotations from the list and remove from the DB
				for (Iterator<Annotation> iterator = annotations.iterator(); iterator.hasNext(); ) {
					
					Annotation currAnnotation = iterator.next();
					String id = currAnnotation.getId();

					logger.debug("Deleting annotation with value: {}", id);

					iterator.remove();

					if(this.annotationRepository.existsById(id)) {
						logger.debug("Deleting extra annotation from annotationRepository with value: {}", this.annotationRepository.getReferenceById(id).getId());
						this.annotationRepository.deleteById(this.annotationRepository.getReferenceById(id).getId());
					}
					this.annotationRepository.flush();

				}

			}

			logger.debug("createIrisSearchRequest annotations are: {}", createIrisSearchRequest.getMessage().getImageData().getAnnotations());

            createIrisSearchRequest = this.createIrisSearchRequestRepository.save(createIrisSearchRequest);

			logger.debug("This CISR has the following annotations: {}", createIrisSearchRequest.getMessage().getImageData().getAnnotations());

            logger.debug("Saved iris search request {}", createIrisSearchRequest.getId());
            return createIrisSearchRequest;
        } else {
            throw new InvalidMessageException("Invalid create message.  There needs to be an image object");
        }
    }

    // Saves a transaction that will be finished later
    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @PostMapping(value = "/api/iris/search/save", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> saveIrisSearchMessage(@RequestBody CreateIrisSearchRequestDTO createIrisSearchRequestDTO,
            Principal principal) throws InvalidMessageException {
        CreateIrisSearchRequest tempCISR = this.imageMessageMapper.toCreateIrisSearchRequest(createIrisSearchRequestDTO);

        if (tempCISR.getMessage().getImageData().getImageData().length() == 0) {
            logger.info("Image data is Empty");
        }

        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        this.checkSetUserAuthentication(tempCISR, ua);
        return ResponseEntity.ok().body(this.saveCreateIrisSearchMessage(tempCISR));
    }

    // creates a 'CreateIrisSearchRequest' and builds an ebts search request from it
    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @PostMapping(value = "/api/iris/search", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> createEbtsMessage(@RequestBody CreateIrisSearchRequestDTO createIrisSearchRequest,
            Principal principal) throws NoSuchAlgorithmException, InvalidMessageException, IOException, EbtsBuildingException {

        logger.info("/api/iris/search POST creating iris message");

        CreateIrisSearchRequest tempCISR = this.imageMessageMapper.toCreateIrisSearchRequest(createIrisSearchRequest);
        EbtsMessageRequest tempMessage = tempCISR.getMessage();
        tempMessage.setDateOfSubmission(new Date().toString());
        createIrisSearchRequest.setMessage(tempMessage);

        UserAuthentication ua;

        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        this.checkSetUserAuthentication(tempCISR, ua);

        byte[] ebtsFileBytes = this.irisService.storeEbtsFile(tempCISR);

        try {
            this.saveCreateIrisSearchMessage(tempCISR);
        } catch (Exception e) {
            logger.error("/api/iris/search Error while saving iris search message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid create message.  There needs to be an image id");
        }

        
        if (ebtsFileBytes.length != 0) {
            String filename = new SimpleDateFormat("'generatedEbts_'yyyyMMddHHmm'.eft'").format(new Date());
            ByteArrayResource file = new ByteArrayResource(ebtsFileBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

            return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
        }

        logger.error("Invalid file");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating file");

    }

    private void checkSetUserAuthentication(CreateIrisSearchRequest tempCISR, UserAuthentication ua) {
        if (tempCISR.getUser() == null || tempCISR.getUser().getId() == null) {
            tempCISR.setUser(ua);
        }
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/iris/search/{searchId}/submit")
    public ResponseEntity<?> submitSearch(@PathVariable(value = "searchId") long searchId) throws IOException, InvalidMessageException, EbtsBuildingException{
        logger.info("/api/iris/search/{}/submit ", searchId);
        if (this.irisService.submitSearchRequest(searchId)) {
            return ResponseEntity.status(HttpStatus.OK).body("OK");
        }
        logger.error("Error in sending email");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error in submitting");
    }

    @Secured({ "ROLE_CASE_WORKER", "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/dev/iris/search/{id}")
    public ResponseEntity<?> getIrisSearchMessage(@PathVariable(value = "id") long id) {
        logger.info("/dev/iris/search/{} ", id);
        // get the message from mysql and check for username
        Optional<CreateIrisSearchRequest> requestOptional = createIrisSearchRequestRepository.findById(id);
        if (requestOptional.isPresent()) {
            return ResponseEntity.ok().body(requestOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    @Secured({ "ROLE_CASE_SUPERVISOR", "ROLE_CASE_WORKER" })
    @GetMapping(value = "/api/iris/search/find/{searchId}")
    public ResponseEntity<?> getSearch(@PathVariable("searchId") long searchId) {
        Optional<CreateIrisSearchRequest> requestOptional = createIrisSearchRequestRepository.findById(searchId);
        if (requestOptional.isPresent()) {
            return ResponseEntity.ok().body(requestOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    private String findReportById(Long reportId) throws InvalidMessageException {
        // Look for the report by Id
        CreateIrisSearchRequest searchRequest = createIrisSearchRequestRepository.findById(reportId).orElse(null);
        if (searchRequest == null) {
            throw new InvalidMessageException("Search Request cannot be found under this id.");
        }

        return searchRequest.getFilePath();
    }
}
