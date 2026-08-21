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
import org.mitre.iwp.web.IwpWebProperties;
import org.mitre.iwp.web.data.EbtsImage;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.model.*;
import org.mitre.iwp.web.service.EbtsImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

@Service
public class SrbReporting {
    private static final Logger log = LoggerFactory.getLogger(SrbReporting.class);

    private final EbtsImageRepository imageRepository;

    private final IwpWebProperties iwpWebProperties;

    private static final String notFoundMessage = "Image data not found";

    private Font boldFont;

    public SrbReporting(EbtsImageRepository imageRepository,
                        IwpWebProperties iwpWebProperties) {
        this.imageRepository = imageRepository;
        this.iwpWebProperties = iwpWebProperties;
    }


    public byte[] createPdfReport(ReportData reportData) throws DocumentException, InvalidMessageException, IOException {
        boldFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
        boldFont.setColor(BaseColor.BLACK);

        Probe probe = reportData.getProbe();

        if(null == probe.getImageList().get(0)){
          log.error("probe image list is empty");
          return new byte[0];
        }

        ImageData probeImage = probe.getImageList().get(0);

        Document document = new Document(PageSize.A4, 25, 25, 25, 25);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        ReportingUtil.handleCaseInformation(reportData, document);

        // Add Probe Table
        PdfPTable probeTable = buildProbeTable(probe);
        Paragraph paragraph = ReportingUtil.getNewDocumentParagraph(probeTable);
        document.add(paragraph);
        document.newPage();

        PdfPTable candidateTable = ReportingUtil.createTableWithHeader(Arrays.asList("Candidate UCN", "Original Image", "Notes"));

        for(Candidate candidate : reportData.getCandidates()) {
            if(ReportingUtil.checkAdjudication(this.iwpWebProperties.getAdjudicationValue(), candidate.getAdjudicationResults())) {
                addCandidateRow(candidateTable, candidate);
                for(ImageData candidateImage : candidate.getImageList()){
                    PdfPTable comparisonTable = new PdfPTable(2);
                    comparisonTable.setWidthPercentage(100);
                    comparisonTable.setSpacingBefore(ReportingUtil.tableSpacing);
                    comparisonTable.setSpacingAfter(ReportingUtil.tableSpacing);

                    PdfPCell header = new PdfPCell();
                    String probeIdentifier = probe.getSubjectIdentifier();
                    String candidateIdentifier = candidate.getSubjectIdentifier();
                    header.setColspan(2);
                    header.setPadding(ReportingUtil.cellPadding);
                    header.setBackgroundColor(BaseColor.BLACK);
                    header.setBorderWidth(ReportingUtil.cellBorder);
                    header.setPhrase(new Phrase(String.format("Comparison of %s and %s", (probeIdentifier == null || probeIdentifier.trim().isEmpty()) ? "PROBE" : probeIdentifier , (candidateIdentifier == null || candidateIdentifier.trim().isEmpty()) ? "CANDIDATE" : candidateIdentifier), ReportingUtil.headerFont));
                    comparisonTable.addCell(header);

                    PdfPCell resultCell = new PdfPCell();
                    resultCell.setColspan(2);
                    resultCell.setPadding(ReportingUtil.cellPadding);
                    resultCell.setBackgroundColor(BaseColor.GRAY);
                    resultCell.setBorderWidth(ReportingUtil.cellBorder);
                    String phrase = "";
                    if(candidate.getAdjudicationResults() != null){
                        phrase=candidate.getAdjudicationResults();
                    }
                    resultCell.setPhrase(new Phrase(phrase, ReportingUtil.subheaderFont));
                    comparisonTable.addCell(resultCell);

                    // Build Eye Comparison
                    if(probeImage.getEbtsImageId() != null) {
                      EbtsImage ebtsImage;
                        log.info("EbtsImageId: {}", probeImage.getEbtsImageId());
                        log.info("RasterizedEbtsImageId: {}", probeImage.getRasterizeEbtsImageId());

                        if( probeImage.getRasterizeEbtsImageId() == null) {
                        ebtsImage = imageRepository.findById(probeImage.getEbtsImageId()).orElseThrow(()->new InvalidMessageException(notFoundMessage));
                      } else {
                        ebtsImage = imageRepository.findById(probeImage.getRasterizeEbtsImageId()).orElseThrow(()->new InvalidMessageException(notFoundMessage));
                      }

                      if(null != ebtsImage) {
                          try {
                              probeImage.setImageBytes(EbtsImage.getImageBytesPng(ebtsImage));
                          }catch(FileNotFoundException fnfe){
                              log.error("File not found {}",ebtsImage);
                          }
                      } else {
                          break;
                      }
                    }

                    if(candidateImage.getEbtsImageId() != null) {
                      EbtsImage ebtsImage;
                      if( candidateImage.getRasterizeEbtsImageId() == null) {
                        ebtsImage = imageRepository.findById(candidateImage.getEbtsImageId()).orElseThrow(()->new InvalidMessageException(notFoundMessage));
                      } else {
                        ebtsImage = imageRepository.findById(candidateImage.getRasterizeEbtsImageId()).orElseThrow(()->new InvalidMessageException(notFoundMessage));
                      }

                      if(null != ebtsImage) {
                          try {
                              candidateImage.setImageBytes(EbtsImage.getImageBytesPng(ebtsImage));
                          }catch(FileNotFoundException fnfe){
                              log.error("File not found {}",ebtsImage);
                          }
                      } else {
                          break;
                      }
                    }

                    //get the unroll images
                    if( probeImage.getUnrollImageId() != null ) {
                      EbtsImage ebtsImage;
                      ebtsImage = imageRepository.findById(probeImage.getUnrollImageId()).orElseThrow(()->new InvalidMessageException(notFoundMessage));
                      if( ebtsImage != null ) probeImage.setUnrollImageBytes(EbtsImage.getImageBytesPng(ebtsImage));
                    }

                    if( candidateImage.getUnrollImageId() != null ) {
                      EbtsImage ebtsImage;
                      ebtsImage = imageRepository.findById(candidateImage.getUnrollImageId()).orElseThrow(()->new InvalidMessageException(notFoundMessage));
                      if( ebtsImage != null ) candidateImage.setUnrollImageBytes(EbtsImage.getImageBytesPng(ebtsImage));
                    }


                    ReportingUtil.addEyeComparison(comparisonTable, probeImage, candidateImage, candidate.getNote());
                    document.add(ReportingUtil.getNewDocumentParagraph(comparisonTable));
                    document.newPage();
                }
            }
        }

        document.newPage();
        paragraph = ReportingUtil.getNewDocumentParagraph(new Chunk("Appendix - Candidate Images", ReportingUtil.pageHeaderFont));
        document.add(paragraph);
        paragraph = ReportingUtil.getNewDocumentParagraph(candidateTable);
        document.add(paragraph);
        document.close();

        return out.toByteArray();
    }

    private PdfPTable buildProbeTable(Probe probe) throws BadElementException, InvalidMessageException, IOException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(ReportingUtil.tableSpacing);
        table.setSpacingAfter(ReportingUtil.tableSpacing);

        PdfPCell header = new PdfPCell();
        header.setColspan(2);
        header.setPadding(ReportingUtil.cellPadding);
        header.setBackgroundColor(BaseColor.BLACK);
        header.setBorderWidth(ReportingUtil.cellBorder);
        header.setPhrase(new Phrase("Probe Information", ReportingUtil.headerFont));
        table.addCell(header);

        // Set Subject Identifier Information
        PdfPCell resultCell = new PdfPCell();
        resultCell.setColspan(2);
        resultCell.setPadding(ReportingUtil.cellPadding);
        resultCell.setBackgroundColor(BaseColor.GRAY);
        resultCell.setBorderWidth(ReportingUtil.cellBorder);

        if(StringUtils.isNotBlank(probe.getSubjectIdentifier())) {
            resultCell.setPhrase(new Phrase(probe.getSubjectIdentifier(), ReportingUtil.subheaderFont));
        } else {
            resultCell.setPhrase(new Phrase("Missing Subject Identifier", ReportingUtil.subheaderFont));
        }

        table.addCell(resultCell);

        // Set Probe Image
        PdfPCell keyCell = new PdfPCell();
        keyCell.setPadding(ReportingUtil.cellPadding);
        keyCell.setBorderWidth(ReportingUtil.cellBorder);
        keyCell.setPhrase(new Phrase("Original Image", boldFont));
        table.addCell(keyCell);
        table.addCell(buildImageCellFromId(probe.getImageList().get(0).getEbtsImageId(), false));

        buildTableRow(table, "Name", probe.getName());
        buildTableRow(table, "Transaction Control Number", probe.getTransactionControlNumber());
        buildTableRow(table, "Transaction Control Reference", probe.getTransactionControlReference());
        buildTableRow(table, "Image Notes", probe.getImageList().get(0).getImageNotes());

        return table;
    }

    private void buildTableRow(PdfPTable table, String key, String value) {
        if(StringUtils.isBlank(value))
            return;

        PdfPCell keyCell = new PdfPCell();
        keyCell.setPadding(ReportingUtil.cellPadding);
        keyCell.setBorderWidth(ReportingUtil.cellBorder);
        keyCell.setPhrase(new Phrase(key, boldFont));

        PdfPCell valueCell = new PdfPCell();
        valueCell.setPadding(ReportingUtil.cellPadding);
        valueCell.setBorderWidth(ReportingUtil.cellBorder);
        valueCell.setPhrase(new Phrase(value));

        table.addCell(keyCell);
        table.addCell(valueCell);
    }

    private void addCandidateRow(PdfPTable table, Candidate candidate) throws BadElementException, InvalidMessageException, IOException{
        for(ImageData image : candidate.getImageList()) {
            if(image.getEbtsImageId() != null) {
                table.addCell(ReportingUtil.buildTextCell((candidate.getSubjectIdentifier())));
                table.addCell(buildImageCellFromId(image.getEbtsImageId(), false));
                table.addCell(ReportingUtil.buildTextCell(image.getImageNotes()));
            }
        }
    }

    private PdfPCell buildImageCellFromId(long imageId, boolean shrinkToFit) throws InvalidMessageException, BadElementException, IOException {
        EbtsImage ebtsImage = imageRepository.findById(imageId).orElseThrow(()->new InvalidMessageException(notFoundMessage));
        try {
            return ReportingUtil.buildImageCell(EbtsImage.getImageBytesPng(ebtsImage), shrinkToFit);
        }catch(FileNotFoundException fne){
            log.error("File not found for image:id {}",imageId);
            PdfPCell ce = new PdfPCell();
            ce.setPadding(ReportingUtil.cellPadding);
            ce.setBorderWidth(ReportingUtil.cellBorder);
            ce.setPhrase(new Phrase("Image not available", boldFont));
            return ce;
        }
    }
}
