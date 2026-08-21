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
import org.mitre.iwp.web.IwpWebProperties;
import org.mitre.iwp.web.data.EbtsImage;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.model.*;
import org.mitre.iwp.web.service.EbtsImageRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

@Service
public class XmlReporting {
    private final EbtsImageRepository imageRepository;

    private final IwpWebProperties iwpWebProperties;

    private static final String notFoundMessage = "Image data not found";

    public XmlReporting(EbtsImageRepository imageRepository,
                        IwpWebProperties iwpWebProperties) {
        this.imageRepository = imageRepository;
        this.iwpWebProperties = iwpWebProperties;
    }

    public byte[] createPdfReport(ReportData reportData) throws DocumentException, InvalidMessageException, IOException {
        if(ReportingUtil.isValid(reportData))
        {
            // Create Pdf Document elements
            Document document = new Document(PageSize.A4, 25, 25, 25, 25);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();

            // Parse Case Information (if applicable)
            ReportingUtil.handleCaseInformation(reportData, document);

            // Parse Data
            parseReportData(reportData, document);

            document.close();

            return out.toByteArray();
        }

        return new byte[0];
    }

    private void parseReportData(ReportData reportData, Document document) throws InvalidMessageException, IOException, DocumentException {
        Probe probe = reportData.getProbe();

        // Loop through candidates/images and compare to probe
        for (Candidate candidate : reportData.getCandidates()) {
            if(ReportingUtil.checkAdjudication(this.iwpWebProperties.getAdjudicationValue(), candidate.getAdjudicationResults())) {
                for (ImageData candidateImage : candidate.getImageList()) {
                    for (ImageData probeImage : probe.getImageList()) {
                        if (candidateImage.getEyeLabel() == probeImage.getEyeLabel()) {
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
                            header.setPhrase(new Phrase(String.format("Comparison of %s and %s", (probeIdentifier == null || probeIdentifier.trim().isEmpty()) ? "PROBE" : probeIdentifier , (candidateIdentifier == null || candidateIdentifier.trim().isEmpty()) ? "CANDIDATE" : candidateIdentifier), ReportingUtil.headerFont));
                            comparisonTable.addCell(header);

                            PdfPCell resultCell = new PdfPCell();
                            resultCell.setColspan(2);
                            resultCell.setPadding(ReportingUtil.cellPadding);
                            resultCell.setPadding(ReportingUtil.cellPadding);
                            resultCell.setBackgroundColor(BaseColor.GRAY);
                            resultCell.setBorderWidth(ReportingUtil.cellBorder);
                            resultCell.setPhrase(new Phrase(candidate.getAdjudicationResults(), ReportingUtil.subheaderFont));
                            comparisonTable.addCell(resultCell);

                            // Build Eye Comparison
                            if (probeImage.getEbtsImageId() != null) {
                                EbtsImage ebtsImage;
                                if (probeImage.getRasterizeEbtsImageId() == null) {
                                    ebtsImage = imageRepository.findById(probeImage.getEbtsImageId()).orElseThrow(() -> new InvalidMessageException(notFoundMessage));
                                } else {
                                    ebtsImage = imageRepository.findById(probeImage.getRasterizeEbtsImageId()).orElseThrow(() -> new InvalidMessageException(notFoundMessage));
                                }

                                if (null != ebtsImage) {
                                    probeImage.setImageBytes(EbtsImage.getImageBytesPng(ebtsImage));
                                } else {
                                    break;
                                }
                            }

                            if (candidateImage.getEbtsImageId() != null) {
                                EbtsImage ebtsImage;
                                if (candidateImage.getRasterizeEbtsImageId() == null) {
                                    ebtsImage = imageRepository.findById(candidateImage.getEbtsImageId()).orElseThrow(() -> new InvalidMessageException(notFoundMessage));
                                } else {
                                    ebtsImage = imageRepository.findById(candidateImage.getRasterizeEbtsImageId()).orElseThrow(() -> new InvalidMessageException(notFoundMessage));
                                }

                                if (null != ebtsImage) {
                                    candidateImage.setImageBytes(EbtsImage.getImageBytesPng(ebtsImage));
                                } else {
                                    break;
                                }
                            }


                            //get the unroll images
                            if (probeImage.getUnrollImageId() != null) {
                                EbtsImage ebtsImage;
                                ebtsImage = imageRepository.findById(probeImage.getUnrollImageId()).orElseThrow(() -> new InvalidMessageException(notFoundMessage));
                                if (ebtsImage != null)
                                    probeImage.setUnrollImageBytes(EbtsImage.getImageBytesPng(ebtsImage));
                            }

                            if (candidateImage.getUnrollImageId() != null) {
                                EbtsImage ebtsImage;
                                ebtsImage = imageRepository.findById(candidateImage.getUnrollImageId()).orElseThrow(() -> new InvalidMessageException(notFoundMessage));
                                if (ebtsImage != null)
                                    candidateImage.setUnrollImageBytes(EbtsImage.getImageBytesPng(ebtsImage));
                            }


                            ReportingUtil.addEyeComparison(comparisonTable, probeImage, candidateImage, candidate.getNote());
                            document.add(ReportingUtil.getNewDocumentParagraph(comparisonTable));
                            document.newPage();
                        }
                    }
                }
            }
        }

        Paragraph paragraph = ReportingUtil.getNewDocumentParagraph(new Chunk("Appendix - Unmatched Images", ReportingUtil.pageHeaderFont));
        boolean paragraphAdded = false;

        for (ImageData image : probe.getImageList()) {
            if (!image.isReported()) {
                if (!paragraphAdded) {
                    document.add(paragraph);
                    paragraphAdded = true;
                } else {
                    document.newPage();
                }
                document.add(addUnreportedTable(image, probe.getSubjectIdentifier()));
            }
        }

        for (Candidate candidate : reportData.getCandidates()) {
            for (ImageData image : candidate.getImageList()) {
                if (!image.isReported()) {
                    if (!paragraphAdded) {
                        document.add(paragraph);
                        paragraphAdded = true;
                    } else {
                        document.newPage();
                    }

                    document.add(addUnreportedTable(image, candidate.getSubjectIdentifier()));
                }
            }
        }

        //add the appendix for the original images
        document.newPage();
        Paragraph originalImagesAppendix = ReportingUtil.getNewDocumentParagraph(new Chunk("Appendix - Original Probe Images", ReportingUtil.pageHeaderFont));

        document.add(originalImagesAppendix);

        PdfPTable probeTable = ReportingUtil.createTableWithHeader(Arrays.asList("Original Image", "Notes"));

        for( ImageData data : probe.getImageList() ) {
          if(data.getEbtsImageId() != null) {
            probeTable.addCell(buildImageCellFromId(data.getEbtsImageId(), false));
            probeTable.addCell(ReportingUtil.buildTextCell(data.getImageNotes()));
          }
        }//end of for

        Paragraph imageParagraph = ReportingUtil.getNewDocumentParagraph(probeTable);
        document.add(imageParagraph);

        for( Candidate candidate : reportData.getCandidates() ) {
            if(ReportingUtil.checkAdjudication(this.iwpWebProperties.getAdjudicationValue(), candidate.getAdjudicationResults())) {
                document.newPage();

                Paragraph candidateAppendix = ReportingUtil.getNewDocumentParagraph(new Chunk(String.format("Appendix - Candidate %s Original Images", candidate.getSubjectIdentifier()), ReportingUtil.pageHeaderFont));
                document.add(candidateAppendix);

                PdfPTable candidateTable = ReportingUtil.createTableWithHeader(Arrays.asList("Original Image", "Notes"));

                for (ImageData data : candidate.getImageList()) {
                    if (data.getEbtsImageId() != null) {
                        candidateTable.addCell(buildImageCellFromId(data.getEbtsImageId(), false));
                        candidateTable.addCell(ReportingUtil.buildTextCell(data.getImageNotes()));
                    }
                }//end of for

                Paragraph candidateImagesParagraph = ReportingUtil.getNewDocumentParagraph(candidateTable);
                document.add(candidateImagesParagraph);
            }

        }//end of for loop

    }//end of parse report


    private PdfPCell buildImageCellFromId(long imageId, boolean shrinkToFit) throws BadElementException, IOException, InvalidMessageException {
      EbtsImage ebtsImage = imageRepository.findById(imageId).orElseThrow(()->new InvalidMessageException(notFoundMessage));
      return ReportingUtil.buildImageCell(EbtsImage.getImageBytesPng(ebtsImage), shrinkToFit);
    }

    private PdfPTable addUnreportedTable(ImageData image, String subjectIdentifier) throws InvalidMessageException, BadElementException, IOException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(50);
        table.setSpacingBefore(ReportingUtil.tableSpacing);
        table.setSpacingAfter(ReportingUtil.tableSpacing);

        PdfPCell header = new PdfPCell();
        header.setPadding(ReportingUtil.cellPadding);
        header.setBackgroundColor(BaseColor.GRAY);
        header.setBorderWidth(ReportingUtil.cellBorder);
        header.setPhrase(new Phrase(String.format("%s - %s Eye", subjectIdentifier, image.getEyeLabel().toString()), ReportingUtil.subheaderFont));
        table.addCell(header);

        if(image.getEbtsImageId() == 0) {
            com.itextpdf.text.List eyeAnnotation = new com.itextpdf.text.List();
            ReportingUtil.buildAnnotationOutput("Annotations", eyeAnnotation, image.getAnnotations());
            table.addCell(ReportingUtil.buildListCell(eyeAnnotation));

            EbtsImage ebtsImage = imageRepository.findById(image.getEbtsImageId()).orElseThrow(()->new InvalidMessageException(notFoundMessage));
            table.addCell(ReportingUtil.buildImageCell(EbtsImage.getImageBytesPng(ebtsImage), false));
            table.addCell(new PdfPCell(new Phrase("")));

        } else {
            EbtsImage ebtsImage = imageRepository.findById(image.getEbtsImageId()).orElseThrow(()->new InvalidMessageException(notFoundMessage));

            if(null == ebtsImage){
                table.addCell(ReportingUtil.buildTextCell("Error Building Image Cell."));
            } else {
                table.addCell(ReportingUtil.buildImageCell(EbtsImage.getImageBytesPng(ebtsImage), false));
            }
        }

        return table;
    }
}
