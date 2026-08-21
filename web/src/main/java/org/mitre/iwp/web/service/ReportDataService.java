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

import org.springframework.stereotype.Service;
import org.mitre.iwp.web.model.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReportDataService {

    private static final Logger log = LoggerFactory.getLogger(ReportDataService.class);

    @PersistenceContext
    private EntityManager em;

    private AnnotationValueRepository avRepository;

    public ReportDataService(AnnotationValueRepository annotationValueRepository) {
        this.avRepository = annotationValueRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public ReportData saveReportData(ReportData reportData) {
        ReportData oldReportData = reportData.getId() != null ? em.find(ReportData.class, reportData.getId()) : null;

        // If there's no old report data, we can just save the item
        if (oldReportData == null) {
            log.info("No old report data is found -- save report");
            return mergeReportData(reportData);
        }

        // In the case of an existing reportData, we're going to have to do some
        // removal on our own because of the current way we're saving entities...

        // Because of the weird situation we have; if an annotation is being deleted
        // We need to handle that first
        // So find what annotations used to exist.
        log.info("Finding associated annotations of old report");
        Set<String> oldAnnotations = new HashSet<>();
        if (oldReportData != null) {
            log.info("Going through candidates first...");
            for (Candidate c : oldReportData.getCandidates()) {
                for (ImageData img : c.getImageList()) {
                    for (Annotation annotation : img.getAnnotations()) {
                        collectAnnotationIds(oldAnnotations, annotation);
                    }
                }
            }

            log.info("Going through probe...");
            for (ImageData img : oldReportData.getProbe().getImageList()) {
                for (Annotation annotation : img.getAnnotations()) {
                    collectAnnotationIds(oldAnnotations, annotation);

                }
            }
        }

        // Compare this to the new annotations we're trying to write
        Set<String> newAnnotations = new HashSet<>();

        log.info("Finding associated annotations of new report");
        log.info("Going through candidates first...");
        for (Candidate c : reportData.getCandidates()) {
            for (ImageData img : c.getImageList()) {
                for (Annotation annotation : img.getAnnotations()) {
                    collectAnnotationIds(newAnnotations, annotation);
                }
            }
        }

        log.info("Going through probe...");
        for (ImageData img : reportData.getProbe().getImageList()) {
            for (Annotation annotation : img.getAnnotations()) {
                collectAnnotationIds(newAnnotations, annotation);
            }
        }

        // Do the delete
        cleanUpAnnotations(oldAnnotations, newAnnotations);

        // Now try to do the merge
        return mergeReportData(reportData);
    }

    /**
     * Save reportData into the database.
     * 
     * @param reportData
     * @return Saved report data (and resulting ids)
     */
    @Transactional(rollbackFor = Exception.class)
    public ReportData mergeReportData(ReportData reportData) {
        log.info("Attempting to save report...");
        return em.merge(reportData);
    }

    /**
     * Remove associated annotation values of deleted annotations.
     * 
     * @param oldAnno Set of ids of old annotations
     * @param newAnno Set of ids for new annotations
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanUpAnnotations(Set<String> oldAnno, Set<String> newAnno) {
        // Take the Set difference between old annotations and new annotations
        log.info("Old Anno: {}", oldAnno);
        log.info("New Anno: {}", newAnno);
        oldAnno.removeAll(newAnno);

        log.info("Old - New AV: {}", oldAnno);

        // If this is less than 0
        if (!oldAnno.isEmpty()) {
            // Because of foreign keys -- clean up the Annotation Values
            // This should cascade into AnnotationValue.points
            avRepository.deleteByAnnotation_IdIn(oldAnno);

        }

    }

    /**
     * Because of nested ids, recursively go through the annotations + children
     * 
     * @param ids        Set where ids should be added to
     * @param annotation Parent annotation
     * @return Ids of annotation and nested children
     */
    public Set<String> collectAnnotationIds(Set<String> ids, Annotation annotation) {
        if (annotation == null) {
            return ids;
        }

        ids.add(annotation.getId());
        for (Annotation child : annotation.getTempChildren()) {
            collectAnnotationIds(ids, child);
        }

        return ids;
    }

}