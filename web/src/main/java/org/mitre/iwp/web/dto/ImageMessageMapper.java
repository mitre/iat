/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.dto;

import org.mitre.iwp.web.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ImageMessageMapper {
    UserMapper userMapper;

    EbtsMapper ebtsMapper;

    public ImageMessageMapper(UserMapper userMapper,
                              EbtsMapper ebtsMapper){
        this.userMapper = userMapper;
        this.ebtsMapper = ebtsMapper;
    }


    public ReportDataDTO toReportDataDTO(ReportData reportData){
        if(reportData == null){
            return new ReportDataDTO();
        }

        List<CandidateDTO> candidateDTOCollection = new ArrayList<>();

        for(Candidate candidate : reportData.getCandidates()){
            candidateDTOCollection.add(this.toCandidateDTO(candidate));
        }

        return new ReportDataDTO(reportData.getId(),
                reportData.getFileName(),
                this.toProbeDTO(reportData.getProbe()),
                candidateDTOCollection,
                reportData.getIncludeExclude(),
                reportData.getResponseType(),
                this.ebtsMapper.toCaseInformationDTO(reportData.getCaseInformation()),
                reportData.getResults(),
                reportData.getInvalidFields(),
                reportData.getErrorMessages(),
                this.userMapper.toUserAuthenticationDTO(reportData.getCreatedBy()),
                this.userMapper.toUserAuthenticationDTO(reportData.getWorkedBy()),
                reportData.getIsFinished(),
                reportData.getAllowNullRequester());
    }

    public ReportData toReportData(ReportDataDTO reportData){
        if(reportData == null){
            return new ReportData();
        }

        List<Candidate> candidateCollection = new ArrayList<>();

        for(CandidateDTO candidate : reportData.getCandidates()){
            candidateCollection.add(this.toCandidate(candidate));
        }

        return new ReportData(reportData.getId(),
                reportData.getFileName(),
                this.toProbe(reportData.getProbe()),
                candidateCollection,
                reportData.isIncludeExclude(),
                reportData.getResponseType(),
                this.ebtsMapper.toCaseInformation(reportData.getCaseInformation()),
                reportData.getResults(),
                reportData.getInvalidFields(),
                reportData.getErrorMessages(),
                this.userMapper.toUserAuthentication(reportData.getCreatedBy()),
                this.userMapper.toUserAuthentication(reportData.getWorkedBy()),
                reportData.getIsFinished(),
                reportData.getAllowNullRequester());
    }

    public CreateIrisSearchRequestDTO toCreateIrisSearchRequestDTO(CreateIrisSearchRequest createIrisSearchRequest){
        if(createIrisSearchRequest == null){
            return new CreateIrisSearchRequestDTO();
        }

        return new CreateIrisSearchRequestDTO(
                createIrisSearchRequest.getId(),
                createIrisSearchRequest.getMessage(),
                this.userMapper.toUserAuthenticationDTO(createIrisSearchRequest.getUser()),
                createIrisSearchRequest.getCreationDateEpoch(),
                createIrisSearchRequest.getIsFinished(),
                createIrisSearchRequest.getFilePath());
    }

    public CreateIrisSearchRequest toCreateIrisSearchRequest(CreateIrisSearchRequestDTO createIrisSearchRequest){
        if(createIrisSearchRequest == null){
            return new CreateIrisSearchRequest();
        }
        
        return new CreateIrisSearchRequest(createIrisSearchRequest.getId(),
                createIrisSearchRequest.getMessage(),
                this.userMapper.toUserAuthentication(createIrisSearchRequest.getUser()),
                createIrisSearchRequest.getCreationDateEpoch(),
                createIrisSearchRequest.getIsFinished(),
                createIrisSearchRequest.getFilePath());
    }

    public EbtsMessageRequestDTO toEbtsMessageRequestDTO(EbtsMessageRequest ebtsMessageRequest){
        if(ebtsMessageRequest == null){
            return new EbtsMessageRequestDTO();
        }

        return new EbtsMessageRequestDTO(ebtsMessageRequest.getId(),
                ebtsMessageRequest.getMapData(),
                this.toImageDataDTO(ebtsMessageRequest.getImageData()),
                ebtsMessageRequest.getCinPrefix(),
                ebtsMessageRequest.getCinIdentifier(),
                ebtsMessageRequest.getNumberOfCandidates(),
                ebtsMessageRequest.getCaseExtension(),
                ebtsMessageRequest.getDateOfSubmission(),
                ebtsMessageRequest.getDestinationAgencyIdentifier(),
                ebtsMessageRequest.getOriginatingAgencyIdentifier(),
                ebtsMessageRequest.getTransactionControlNumber(),
                ebtsMessageRequest.getAttentionIndicator(),
                ebtsMessageRequest.getSourceAgency(),
                ebtsMessageRequest.getIrisCaptureDate(),
                ebtsMessageRequest.getRotationOfEye(),
                ebtsMessageRequest.getRotationUncertainty(),
                ebtsMessageRequest.getErrorMessages(),
                ebtsMessageRequest.getInvalidFields());
    }

    public EbtsMessageRequest toEbtsMessageRequest(EbtsMessageRequestDTO ebtsMessageRequest){
        if(ebtsMessageRequest == null){
            return new EbtsMessageRequest();
        }

        return new EbtsMessageRequest(ebtsMessageRequest.getId(),
                ebtsMessageRequest.getMapData(),
                this.toImageData(ebtsMessageRequest.getImageData()),
                ebtsMessageRequest.getCinPrefix(),
                ebtsMessageRequest.getCinIdentifier(),
                ebtsMessageRequest.getNumberOfCandidates(),
                ebtsMessageRequest.getCaseExtension(),
                ebtsMessageRequest.getDateOfSubmission(),
                ebtsMessageRequest.getDestinationAgencyIdentifier(),
                ebtsMessageRequest.getOriginatingAgencyIdentifier(),
                ebtsMessageRequest.getTransactionControlNumber(),
                ebtsMessageRequest.getAttentionIndicator(),
                ebtsMessageRequest.getSourceAgency(),
                ebtsMessageRequest.getIrisCaptureDate(),
                ebtsMessageRequest.getRotationOfEye(),
                ebtsMessageRequest.getRotationUncertainty(),
                ebtsMessageRequest.getErrorMessages(),
                ebtsMessageRequest.getInvalidFields());
    }

    public ProbeDTO toProbeDTO(Probe probe){
        if(probe == null){
            return new ProbeDTO();
        }

        return new ProbeDTO(probe.getId(),
                probe.getTransactionControlReference(),
                probe.getTransactionControlNumber(),
                probe.getImageList(),
                probe.getName(),
                probe.getSubjectIdentifier());
    }

    public Probe toProbe(ProbeDTO probe){
        if(probe == null){
            return new Probe();
        }

        return new Probe(probe.getId(),
                probe.getTransactionControlReference(),
                probe.getTransactionControlNumber(),
                probe.getImageList(),
                probe.getName(),
                probe.getSubjectIdentifier());
    }

    public CandidateDTO toCandidateDTO(Candidate candidate){
        if(candidate == null){
            return new CandidateDTO();
        }

        return new CandidateDTO(candidate.getId(),
                candidate.getScore(),
                candidate.getSubjectIdentifier(),
                candidate.getImageList(),
                candidate.getAdjudicationResults(),
                candidate.getNote());
    }

    public Candidate toCandidate(CandidateDTO candidate){
        if(candidate == null){
            return new Candidate();
        }

        return new Candidate(candidate.getId(),
                candidate.getScore(),
                candidate.getSubjectIdentifier(),
                candidate.getImageList(),
                candidate.getAdjudicationResults(),
                candidate.getNote());
    }

    public ImageDataDTO toImageDataDTO(ImageData imageData){
        if(imageData == null){
            return new ImageDataDTO();
        }

        List<AnnotationDTO> annotationDTOList = new ArrayList<>();

        for(Annotation annotation : imageData.getAnnotations()){
            annotationDTOList.add(this.toAnnotationDTO(annotation));
        }

        return new ImageDataDTO(imageData.getId(),
                imageData.getEbtsImageId(),
                imageData.getUnrollImageId(),
                imageData.getUnrollRingData(),
                imageData.getRasterizeEbtsImageId(),
                imageData.getEyeLabel(),
                imageData.getContactType(),
                annotationDTOList,
                imageData.getAciiHorizontal(),
                imageData.isReported(),
                imageData.getImageNotes(),
                imageData.getDme(),
                imageData.getImageData(),
                imageData.getUnrollImageData(),
                imageData.getImageBytes(),
                imageData.getUnrollImageBytes(),
                imageData.getRecordType(),
                imageData.getRecordIndex(),
                imageData.getQualityScore());
    }

    public ImageData toImageData(ImageDataDTO imageData){
        if(imageData == null){
            return new ImageData();
        }

        List<Annotation> annotationList = new ArrayList<>();

        for(AnnotationDTO annotation : imageData.getAnnotations()){
            annotationList.add(this.toAnnotation(annotation));
        }

        return new ImageData(imageData.getId(),
                imageData.getEbtsImageId(),
                imageData.getUnrollImageId(),
                imageData.getUnrollRingData(),
                imageData.getRasterizeEbtsImageId(),
                imageData.getEyeLabel(),
                imageData.getContactType(),
                annotationList,
                imageData.getAciiHorizontal(),
                imageData.isReported(),
                imageData.getImageNotes(),
                imageData.getDme(),
                imageData.getImageData(),
                imageData.getUnrollImageData(),
                imageData.getImageBytes(),
                imageData.getUnrollImageBytes(),
                imageData.getRecordType(),
                imageData.getRecordIndex(),
                imageData.getQualityScore());
    }

    public AnnotationDTO toAnnotationDTO(Annotation annotation){
        if(annotation == null){
            return new AnnotationDTO();
        }

        List<AnnotationDTO> annotationDTOList = new ArrayList<>();

        for(Annotation child : annotation.getTempChildren()){
            annotationDTOList.add(this.toAnnotationDTO(child));
        }

        return new AnnotationDTO(annotation.getId(),
                annotation.getText(),
                annotation.getType(),
                annotation.getCreationDateEpoch(),
                this.toAnnotationValueDTO(annotation.getValue()),
                annotationDTOList,
                annotation.getParentId());
    }

    public Annotation toAnnotation(AnnotationDTO annotation){
        if(annotation == null){
            return new Annotation();
        }

        List<Annotation> annotationList = new ArrayList<>();

        for(AnnotationDTO child : annotation.getTempChildren()){
            annotationList.add(this.toAnnotation(child));
        }

        return new Annotation(annotation.getId(),
                annotation.getText(),
                annotation.getType(),
                annotation.getCreationDateEpoch(),
                this.toAnnotationValue(annotation.getValue()),
                annotationList,
                annotation.getParentId());
    }

    public AnnotationValueDTO toAnnotationValueDTO(AnnotationValue annotationValue){
        if(annotationValue == null){
            return new AnnotationValueDTO();
        }

        return new AnnotationValueDTO(annotationValue.getId(),
                annotationValue.getPoints(),
                this.toAnnotationDTO(annotationValue.getAnnotation()),
                annotationValue.getData());
    }

    public AnnotationValue toAnnotationValue(AnnotationValueDTO annotationValue){
        if(annotationValue == null){
            return new AnnotationValue();
        }

        return new AnnotationValue(annotationValue.getId(),
                annotationValue.getPoints(),
                this.toAnnotation(annotationValue.getAnnotation()),
                annotationValue.getData());
    }

}
