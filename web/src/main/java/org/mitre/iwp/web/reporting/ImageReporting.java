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
import java.io.IOException;
import java.util.Collections;

@Service
public class ImageReporting {
    private static final Logger log = LoggerFactory.getLogger(ImageReporting.class);

    private final EbtsImageRepository imageRepository;

    private static final String notFoundMessage = "Image data not found";

    public ImageReporting(EbtsImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public byte[] createPdfReport(ReportData reportData) throws DocumentException, InvalidMessageException, IOException {
        log.info("Generating report for id: {}", reportData.getId());
        if(ReportingUtil.isValid(reportData)) {

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);
            headerFont.setColor(BaseColor.WHITE);

            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLDITALIC);
            subHeaderFont.setColor(BaseColor.BLACK);

            Probe probe = reportData.getProbe();
            Candidate candidate = reportData.getCandidates().get(0);

            if (probe.getImageList().size() != 1) {
                log.info("Probe Image List is does not equal 1. Returning");
                return new byte[0];
            }

            if (candidate.getImageList().size() != 1) {
                log.info("Candidate Image List is does not equal 1. Returning");
                return new byte[0];
            }

            ImageData probeImage = probe.getImageList().get(0);
            ImageData candidateImage = candidate.getImageList().get(0);

            Document document = new Document(PageSize.A4, 25, 25, 25, 25);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();

            ReportingUtil.handleCaseInformation(reportData, document);

            PdfPTable comparisonTable = new PdfPTable(2);
            comparisonTable.setWidthPercentage(100);
            comparisonTable.setSpacingBefore(ReportingUtil.tableSpacing);
            comparisonTable.setSpacingAfter(ReportingUtil.tableSpacing);
            comparisonTable.setSpacingBefore(ReportingUtil.tableSpacing);
            comparisonTable.setSpacingAfter(ReportingUtil.tableSpacing);

            PdfPCell header = new PdfPCell();
            String probeIdentifier = probe.getSubjectIdentifier();
            String candidateIdentifier = candidate.getSubjectIdentifier();
            header.setColspan(2);
            header.setPadding(ReportingUtil.cellPadding);
            header.setBackgroundColor(BaseColor.BLACK);
            header.setBorderWidth(ReportingUtil.cellBorder);
            header.setPhrase(new Phrase(String.format("Comparison of %s and %s", (probeIdentifier == null || probeIdentifier.trim().isEmpty()) ? "PROBE" : probeIdentifier , (candidateIdentifier == null || candidateIdentifier.trim().isEmpty()) ? "CANDIDATE" : candidateIdentifier), headerFont));

            comparisonTable.addCell(header);

            PdfPCell resultCell = new PdfPCell();
            resultCell.setColspan(2);
            resultCell.setPadding(ReportingUtil.cellPadding);
            resultCell.setPadding(ReportingUtil.cellPadding);
            resultCell.setBackgroundColor(BaseColor.GRAY);
            resultCell.setBorderWidth(ReportingUtil.cellBorder);
            resultCell.setPhrase(new Phrase(candidate.getAdjudicationResults(), subHeaderFont));
            comparisonTable.addCell(resultCell);

            probeImage.setImageBytes(this.getImageBytes(probeImage));
            candidateImage.setImageBytes(this.getImageBytes(candidateImage));

            if (probeImage.getUnrollImageId() != null) {
                probeImage.setUnrollImageBytes(this.getImageBytes(probeImage.getUnrollImageId()));
            }

            if (candidateImage.getUnrollImageId() != null) {
                candidateImage.setUnrollImageBytes(this.getImageBytes(candidateImage.getUnrollImageId()));
            }

            // Build Eye Comparison
            ReportingUtil.addEyeComparison(comparisonTable, probeImage, candidateImage, candidate.getNote());

            document.add(ReportingUtil.getNewDocumentParagraph(comparisonTable));
            document.newPage();

            Paragraph paragraph = ReportingUtil.getNewDocumentParagraph(new Chunk("Appendix - Original Image Data", ReportingUtil.pageHeaderFont));
            paragraph.setSpacingAfter(10);
            document.add(paragraph);
            buildTable(probe.getImageList().get(0), "Image 1", document);
            buildTable(candidate.getImageList().get(0), "Image 2", document);

            document.close();

            return out.toByteArray();
        }

        log.info("Report is not valid");
        return new byte[0];
    }

    private byte[] getImageBytes(ImageData imageData) throws InvalidMessageException, IOException {
        EbtsImage ebtsImage;
        if( imageData.getRasterizeEbtsImageId() == null) {
          ebtsImage = imageRepository.findById(imageData.getEbtsImageId()).orElseThrow(() -> new InvalidMessageException(notFoundMessage));
        } else {
          ebtsImage = imageRepository.findById(imageData.getRasterizeEbtsImageId()).orElseThrow(() -> new InvalidMessageException(notFoundMessage));
        }

        return EbtsImage.getImageBytesPng(ebtsImage);
    }

    private byte[] getImageBytes(long ebtsImageId) throws IOException, InvalidMessageException {
        EbtsImage ebtsImage;
        ebtsImage = imageRepository.findById(ebtsImageId).orElseThrow(() -> new InvalidMessageException(notFoundMessage));
        return EbtsImage.getImageBytesPng(ebtsImage);
    }


    private void buildTable(ImageData imageData, String header, Document document) throws InvalidMessageException, IOException, DocumentException {
        PdfPTable table = ReportingUtil.createTableWithHeader(Collections.singletonList(header));
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingAfter(1);
        table.setSpacingBefore(1);
        EbtsImage ebtsImage = imageRepository.findById(imageData.getEbtsImageId()).orElseThrow(() -> new InvalidMessageException(notFoundMessage));
        table.addCell(ReportingUtil.buildImageCell(EbtsImage.getImageBytesPng(ebtsImage), true));
        table.addCell(ReportingUtil.buildTextCell(imageData.getImageNotes()));

        Paragraph paragraph = ReportingUtil.getNewDocumentParagraph(table);
        document.add(paragraph);
    }
}
