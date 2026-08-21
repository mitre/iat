/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.reporting;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.lang3.StringUtils;
import org.mitre.iwp.web.data.EbtsImage;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.model.Candidate;
import org.mitre.iwp.web.model.ImageData;
import org.mitre.iwp.web.model.Probe;
import org.mitre.iwp.web.model.ReportData;
import org.mitre.iwp.web.service.EbtsImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;

@Service
public class MultiModalReporting {
    private static final Logger log = LoggerFactory.getLogger(MultiModalReporting.class);

    private final EbtsImageRepository imageRepository;

    private static final String NOT_FOUND_MESSAGE = "Image data not found";
    
    private Font boldFont;

    public MultiModalReporting(EbtsImageRepository ebtsImageRepository){
        this.imageRepository = ebtsImageRepository;
    }

    public byte[] createPdfReport(ReportData reportData) throws DocumentException {
        log.info("Generating report for id: {}", reportData.getId());
        if(ReportingUtil.isValid(reportData)) {

            boldFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
            boldFont.setColor(BaseColor.BLACK);

            // Create Pdf Document elements
            Document document = new Document(PageSize.A4, 25, 25, 25, 25);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();

            // Parse Data
            parseReportData(reportData, document);

            document.close();

            return out.toByteArray();
        }

        return new byte[0];
    }

    private void parseReportData(ReportData reportData, Document document) {
        Probe probe = reportData.getProbe();
        Candidate candidate = reportData.getCandidates().get(0);

        try {
            // Parse Case Information (if applicable)
            ReportingUtil.handleCaseInformation(reportData, document);

            // Add Comparison Metadata
            Paragraph paragraph = ReportingUtil.getNewDocumentParagraph(buildComparisonMetadataTable(probe, candidate));
            document.add(paragraph);

            // Add Iris Image Comparison Table(s)
            createIrisTables(document, probe, candidate);
        } catch (com.itextpdf.text.DocumentException e){
            log.info(e.getMessage());
        }

    }

    private void createIrisTables(Document document, Probe probe, Candidate candidate) {
        for(ImageData imageData : probe.getImageList()){
            if(imageData.getRecordType() == 17){
                addIrisImageTable(document, imageData, probe.getSubjectIdentifier());
            }
        }

        for(ImageData imageData : candidate.getImageList()){
            if(imageData.getRecordType() == 17){
                addIrisImageTable(document, imageData, candidate.getSubjectIdentifier());
            }
        }
    }

    private void addIrisImageTable(Document document, ImageData imageData, String subjectIdentifier) {
        if (imageData.getEbtsImageId() != null) {

            try {
                document.newPage();
                String irisLabel = String.format("%s and %s eye", (subjectIdentifier == null || subjectIdentifier.trim().isEmpty()) ? "Subject" : subjectIdentifier, imageData.getEyeLabel());
                PdfPTable irisTable = new PdfPTable(1);
                irisTable.setWidthPercentage(100);
                irisTable.setSpacingBefore(ReportingUtil.tableSpacing);
                irisTable.setSpacingAfter(ReportingUtil.tableSpacing);

                PdfPCell header = new PdfPCell();
                header.setColspan(1);
                header.setPadding(ReportingUtil.cellPadding);
                header.setBackgroundColor(BaseColor.BLACK);
                header.setBorderWidth(ReportingUtil.cellBorder);
                header.setPhrase(new Phrase(irisLabel, ReportingUtil.headerFont));
                irisTable.addCell(header);

                EbtsImage ebtsImage;
                if (imageData.getRasterizeEbtsImageId() == null) {
                    ebtsImage = imageRepository.findById(imageData.getEbtsImageId()).orElseThrow(() -> new InvalidMessageException(NOT_FOUND_MESSAGE));
                } else {
                    ebtsImage = imageRepository.findById(imageData.getRasterizeEbtsImageId()).orElseThrow(() -> new InvalidMessageException(NOT_FOUND_MESSAGE));
                }

                if (null != ebtsImage) {
                    imageData.setImageBytes(EbtsImage.getImageBytesPng(ebtsImage));
                } else {
                    return;
                }

                //get the unroll images
                if (imageData.getUnrollImageId() != null) {
                    ebtsImage = imageRepository.findById(imageData.getUnrollImageId()).orElseThrow(() -> new InvalidMessageException(NOT_FOUND_MESSAGE));
                    if (ebtsImage != null)
                        imageData.setUnrollImageBytes(EbtsImage.getImageBytesPng(ebtsImage));
                }

                com.itextpdf.text.List eyeAnnotation = new com.itextpdf.text.List();
                ReportingUtil.buildAnnotationOutput(String.format("%s Annotations", irisLabel), eyeAnnotation, imageData.getAnnotations());
                irisTable.addCell(ReportingUtil.buildListCell(eyeAnnotation));

                if (StringUtils.isNotBlank(imageData.getImageData())) {
                    irisTable.addCell(ReportingUtil.buildImageCell(
                            ReportingUtil.drawImageAnnotationLabels(imageData.getImageData(), imageData.getAnnotations()), false));
                } else {
                    if (null != imageData.getImageBytes() && imageData.getImageBytes().length > 0) {
                        irisTable.addCell(ReportingUtil.buildImageCell(
                                ReportingUtil.drawImageAnnotationLabels(imageData.getImageBytes(), imageData.getAnnotations()), false));
                    } else {
                        irisTable.addCell(ReportingUtil.buildTextCell("Error Building Image Cell."));
                    }
                }

                document.add(irisTable);
            } catch (Exception e){
                log.info(e.getMessage());
            }
        }
    }

    private PdfPTable buildComparisonMetadataTable(Probe probe, Candidate candidate){
        PdfPTable table = new PdfPTable(3);
        table.setSpacingBefore(ReportingUtil.tableSpacing);
        table.setSpacingAfter(ReportingUtil.tableSpacing);
        table.setWidthPercentage(100);

        PdfPCell header = new PdfPCell();
        header.setColspan(3);
        header.setPadding(ReportingUtil.cellPadding);
        header.setBackgroundColor(BaseColor.BLACK);
        header.setBorderWidth(ReportingUtil.cellBorder);
        header.setPhrase(new Phrase("Uploaded Files Information", ReportingUtil.headerFont));
        table.addCell(header);

        table.addCell(createCell("Record Type", true));
        table.addCell(createCell("Probe Images", true));
        table.addCell(createCell("Candidate Images", true));
        // Add Identifier Row
        buildTableRow(table, "Subject Identifier", probe.getSubjectIdentifier(), candidate.getSubjectIdentifier());

        buildImageSummaryRows(table, probe.getImageList(), candidate.getImageList());
        return table;
    }

    private void buildImageSummaryRows(PdfPTable table, List<ImageData> probeImageList, List<ImageData> candidateImageList) {
        Map<Integer, Integer> probeImageMap = this.countImageTypes(probeImageList);
        Map<Integer, Integer> candidateImageMap = this.countImageTypes(candidateImageList);

        Set<Integer> recordTypeSet= new TreeSet<>();

        recordTypeSet.addAll(probeImageMap.keySet());
        recordTypeSet.addAll(candidateImageMap.keySet());


        for(Integer recordType : recordTypeSet){
            table.addCell(this.createCell(String.format("Type %d", recordType), true));

            if(probeImageMap.containsKey(recordType)) {
                table.addCell(this.createCell(String.valueOf(probeImageMap.get(recordType)), false));
            } else {
                table.addCell(this.createCell("0", false));
            }

            if(candidateImageMap.containsKey(recordType)) {
                table.addCell(this.createCell(String.valueOf(candidateImageMap.get(recordType)), false));
            } else {
                table.addCell(this.createCell("0", false));
            }
        }

    }

    private Map<Integer, Integer> countImageTypes(List<ImageData> imageDataList){
        Map<Integer, Integer> returnMap = new HashMap<>();
        for(ImageData imageData : imageDataList){
            returnMap.merge(imageData.getRecordType(), 1, Integer::sum);
        }

        return returnMap;
    }

    private void buildTableRow(PdfPTable table, String key, String probeValue, String candidateValue){
        if(StringUtils.isBlank(probeValue) ||
        StringUtils.isBlank(candidateValue))
            return;

        table.addCell(this.createCell(key, true));
        table.addCell(this.createCell(probeValue, false));
        table.addCell(this.createCell(candidateValue, false));

    }

    private PdfPCell createCell(String phrase, boolean useBoldFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(ReportingUtil.cellPadding);
        cell.setBorderWidth(ReportingUtil.cellBorder);
        if(useBoldFont) {
            cell.setPhrase(new Phrase(phrase, this.boldFont));
        } else {
            cell.setPhrase(new Phrase(phrase));
        }

        return cell;
    }
}
