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

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.mitre.iwp.web.model.Annotation;
import org.mitre.iwp.web.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class ReportingUtil {
    private static final Logger log = LoggerFactory.getLogger(ReportingUtil.class);

    protected static int tableSpacing = 30;
    protected static float cellPadding = 5;
    protected static float cellBorder = 2;
    static Font boldFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
    static Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
    static int annotationCount = 1;

    protected static Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC,
            BaseColor.WHITE);
    protected static Font subheaderFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLDITALIC,
            BaseColor.BLACK);

    protected static Font pageHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);

    protected static Font listHeaderFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    protected static Font listSubitemFont = new Font(Font.FontFamily.HELVETICA, 12);

    public static byte[] createCaseOverview(ReviewCase cas)
            throws DocumentException {
        boldFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
        boldFont.setColor(BaseColor.BLACK);

        Document document = new Document(PageSize.A4, 25, 25, 25, 25);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);

        document.open();

        Map<String, Integer> statusCounts = new HashMap<>();

        // Add Case Info Table
        PdfPTable table = new PdfPTable(2);
        table.setSpacingBefore(tableSpacing);
        table.setSpacingAfter(tableSpacing);
        table.setWidthPercentage(100);

        table.addCell(buildHeaderCell(2, "Title"));
        table.addCell(buildIdentCell(2, cas.getTitle()));
        table.addCell(buildHeaderCell(2, "Description"));
        table.addCell(buildIdentCell(2, cas.getDescription()));
        if(cas.getCreatedDate() != null) {
            table.addCell(buildHeaderCell(2, "Created Date"));
            table.addCell(buildIdentCell(2, cas.getCreatedDate().toString()));
        }
        table.addCell(buildHeaderCell(2, "ID"));
        table.addCell(buildIdentCell(2, String.valueOf(cas.getId())));
        table.addCell(buildHeaderCell(2, "Priority"));
        table.addCell(buildIdentCell(2, cas.getPriority()));
        table.addCell(buildHeaderCell(2, "Created By"));
        table.addCell(buildIdentCell(2, cas.getCreatedBy()));
        if(cas.getDueDate() != null) {
            table.addCell(buildHeaderCell(2, "Due Date"));
            table.addCell(buildIdentCell(2, cas.getDueDate().toString()));
        }
        table.addCell(buildHeaderCell(2, "Searches and Reviews"));
        table.addCell(buildIdentCell(2, ""));
        table.addCell(buildHeaderCell(2, "Total number of searches/reviews"));
        statusCounts.forEach((key, val) -> {
            table.addCell(buildHeaderCell(2, key));
            table.addCell(buildIdentCell(2, val + ""));
        });

        document.add(table);
        document.newPage();

        // generate pie chart
        ReportingUtil.createCaseReportPieChart(statusCounts, writer);

        Paragraph paragraph = ReportingUtil.getNewDocumentParagraph(new Phrase("# Requests:"));
        document.add(paragraph);
        document.close();

        return out.toByteArray();
    }

    public static void createCaseReportPieChart(Map<String, Integer> statusCounts, PdfWriter writer) {
        /* Create Pie Chart */
        DefaultPieDataset<String> data = new DefaultPieDataset<>();
        statusCounts.forEach(data::setValue);
        JFreeChart chart = ChartFactory.createPieChart("Request Status", data, true, true, false);

        /* We have to insert this colored Pie Chart into the PDF file using iText now */
        PdfContentByte cb = writer.getDirectContent();
        float width = PageSize.A4.getWidth();
        float height = PageSize.A4.getHeight() / 2;
        // Pie chart
        PdfTemplate pie = cb.createTemplate(width, height);
        Graphics2D g2d1 = new PdfGraphics2D(pie, width, height);
        Rectangle2D r2d1 = new Rectangle2D.Double(0, 0, width, height);
        chart.draw(g2d1, r2d1);
        g2d1.dispose();
        cb.addTemplate(pie, 0, height);
    }

    protected static PdfPTable createTableWithHeader(java.util.List<String> headerItems) {
        PdfPTable table = new PdfPTable(headerItems.size());
        table.setWidthPercentage(100);
        table.setSpacingBefore(tableSpacing);
        table.setSpacingAfter(tableSpacing);
        Font fontH1 = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);
        fontH1.setColor(BaseColor.WHITE);
        for (String headerString : headerItems) {
            PdfPCell header = new PdfPCell();
            header.setBorderWidth(ReportingUtil.cellBorder);
            header.setPadding(cellPadding);
            header.setBackgroundColor(BaseColor.GRAY);
            header.setBorderWidth(ReportingUtil.cellBorder);
            header.setPhrase(new Phrase(headerString, fontH1));
            table.addCell(header);
        }

        return table;
    }

    public static void handleCaseInformation(ReportData reportData, Document document) throws DocumentException {
        if (null != reportData.getCaseInformation()) {
            java.util.List<String> subjectIdentifierList = new ArrayList<>();

            if (null != reportData.getProbe().getSubjectIdentifier()) {
                subjectIdentifierList.add(reportData.getProbe().getSubjectIdentifier());
            }

            for (Candidate candidate : reportData.getCandidates()) {
                if (null != candidate.getSubjectIdentifier()) {
                    subjectIdentifierList.add(candidate.getSubjectIdentifier());
                }
            }

            document.add(buildCaseInformationTable(reportData, subjectIdentifierList));

            document.newPage();
        }
    }

    protected static PdfPTable buildCaseInformationTable(ReportData reportData,
            java.util.List<String> subjectIdentifiers) {
        PdfPTable table = new PdfPTable(2);
        table.setSpacingBefore(tableSpacing);
        table.setSpacingAfter(tableSpacing);
        table.setWidthPercentage(100);

        CaseInformation caseInformation = reportData.getCaseInformation();

        // Add Comparison Header/Date Information
        table.addCell(buildHeaderCell(2, "Iris Image Comparisons"));
        table.addCell(buildIdentCell(2, DateFormatUtils.format(new Date(caseInformation.getComparisonDate()), "yyyy-MM-dd HH:mm:SS")));

        handlePersonInformation(table, reportData);

        table.addCell(buildHeaderCell(2, "Received Date"));
        table.addCell(buildIdentCell(2, DateFormatUtils.format(new Date(caseInformation.getReceivedDate()), "yyyy-MM-dd HH:mm:SS")));

        if (!subjectIdentifiers.isEmpty()) {
            table.addCell(buildHeaderCell(2, "Referenced Subject(s)"));
            table.addCell(buildIdentCell(2, String.join("\n", subjectIdentifiers)));
        }

        return table;
    }

    private static void handlePersonInformation(PdfPTable table, ReportData reportData) {
        java.util.List<String> conductedByList = getPersonList(reportData.getWorkedBy());
        java.util.List<String> requestedByList = getPersonList(reportData.getCaseInformation().getRequestedBy());

        if (!conductedByList.isEmpty() && !requestedByList.isEmpty()) {
            table.addCell(buildHeaderCell(1, "Conducted By"));
            table.addCell(buildHeaderCell(1, "Requested By"));
            table.addCell(buildIdentCell(1, String.join("\n", conductedByList)));
            table.addCell(buildIdentCell(1, String.join("\n", requestedByList)));
        } else if (!conductedByList.isEmpty()) {
            table.addCell(buildHeaderCell(2, "Conducted By"));
            table.addCell(buildIdentCell(2, String.join("\n", conductedByList)));
        } else if (!requestedByList.isEmpty()) {
            table.addCell(buildHeaderCell(2, "Requested By"));
            table.addCell(buildIdentCell(2, String.join("\n", requestedByList)));
        }
    }

    private static java.util.List<String> getPersonList(Person person) {
        java.util.List<String> elementList = new ArrayList<>();

        if (null == person)
            return elementList;

        if (StringUtils.isNotBlank(person.getName())) {
            elementList.add(person.getName());
        }

        if (StringUtils.isNotBlank(person.getTitle())) {
            elementList.add(person.getTitle());
        }

        if (StringUtils.isNotBlank(person.getDepartment())) {
            elementList.add(person.getDepartment());
        }

        if (StringUtils.isNotBlank(person.getAddress())) {
            elementList.add(person.getAddress());
        }

        if (StringUtils.isNotBlank(person.getEmail())) {
            elementList.add(person.getEmail());
        }

        if (StringUtils.isNotBlank(person.getPhone())) {
            elementList.add(person.getPhone());
        }

        return elementList;
    }

    private static java.util.List<String> getPersonList(UserAuthentication ua) {
        java.util.List<String> elementList = new ArrayList<>();

        Profile person = ua.getProfile();

        if (null == person)
            return elementList;

        if (StringUtils.isNotBlank(person.getFullName())) {
            elementList.add(person.getFullName());
        }

        if (StringUtils.isNotBlank(person.getTitle())) {
            elementList.add(person.getTitle());
        }

        if (StringUtils.isNotBlank(person.getDepartment())) {
            elementList.add(person.getDepartment());
        }

        if (StringUtils.isNotBlank(person.getAddress())) {
            elementList.add(person.getAddress());
        }

        if (StringUtils.isNotBlank(person.getEmail())) {
            elementList.add(person.getEmail());
        }

        if (StringUtils.isNotBlank(person.getPhoneNumber())) {
            elementList.add(person.getPhoneNumber());
        }

        return elementList;
    }

    public static PdfPCell buildHeaderCell(int colSpan, String phraseText) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BaseColor.GRAY);
        cell.setPaddingTop(1);
        cell.setPaddingBottom(7);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        cell.setColspan(colSpan);
        cell.setBorderWidth(ReportingUtil.cellBorder);

        cell.addElement(new Phrase(phraseText, ReportingUtil.subheaderFont));
        return cell;
    }

    private static PdfPCell buildIdentCell(int colSpan, String cellText) {
        PdfPCell cell = new PdfPCell();
        cell.setPaddingTop(1);
        cell.setPaddingBottom(7);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        cell.setColspan(colSpan);
        cell.setBorderWidth(ReportingUtil.cellBorder);

        Paragraph paragraph = new Paragraph();
        paragraph.setIndentationLeft(25);
        paragraph.add(new Phrase(cellText, regularFont));
        cell.addElement(paragraph);

        return cell;
    }

    public static void addEyeComparison(PdfPTable table, ImageData probeImage, ImageData candidateImage, String note)
            throws IOException, BadElementException {
        com.itextpdf.text.List probeEyeAnnotation = new com.itextpdf.text.List();
        com.itextpdf.text.List candidateEyeAnnotation = new com.itextpdf.text.List();

        buildAnnotationOutput("Probe Annotations", probeEyeAnnotation, probeImage.getAnnotations());
        buildAnnotationOutput("Candidate Annotations", candidateEyeAnnotation, candidateImage.getAnnotations());

        PdfPCell comparisonLabelCell = new PdfPCell();
        comparisonLabelCell.setColspan(2);
        comparisonLabelCell.setPadding(ReportingUtil.cellPadding);
        comparisonLabelCell.setBackgroundColor(BaseColor.WHITE);
        comparisonLabelCell.setBorderWidth(ReportingUtil.cellBorder);

        if (probeImage.getEyeLabel() == candidateImage.getEyeLabel()) {
            comparisonLabelCell.setPhrase(
                    new Phrase(String.format("%s Eye Comparison", probeImage.getEyeLabel().toString()), boldFont));
        } else {
            comparisonLabelCell.setPhrase(new Phrase(String.format("Probe %s Eye to Candidate %s Eye Comparison",
                    probeImage.getEyeLabel().toString(), candidateImage.getEyeLabel().toString()), boldFont));
        }

        table.addCell(comparisonLabelCell);

        if (note != null && !note.equalsIgnoreCase("")) {
            PdfPCell noteCell = new PdfPCell();
            noteCell.setColspan(2);
            noteCell.setPadding(ReportingUtil.cellPadding);
            noteCell.setBackgroundColor(BaseColor.WHITE);
            noteCell.setBorderWidth(ReportingUtil.cellBorder);

            noteCell.addElement(new Phrase("Examiner's Note:", boldFont));
            noteCell.addElement(new Phrase(note, regularFont));

            table.addCell(noteCell);
        }

        table.addCell(buildListCell(probeEyeAnnotation));
        table.addCell(buildListCell(candidateEyeAnnotation));

        if (StringUtils.isNotBlank(probeImage.getImageData())) {
            table.addCell(buildImageCell(
                    drawImageAnnotationLabels(probeImage.getImageData(), probeImage.getAnnotations()), false));
        } else {
            if (null != probeImage.getImageBytes() && probeImage.getImageBytes().length > 0) {
                table.addCell(buildImageCell(
                        drawImageAnnotationLabels(probeImage.getImageBytes(), probeImage.getAnnotations()), false));
            } else {
                table.addCell(buildTextCell("Error Building Image Cell."));
            }
        }

        if (StringUtils.isNotBlank(candidateImage.getImageData())) {
            table.addCell(buildImageCell(
                    drawImageAnnotationLabels(candidateImage.getImageData(), candidateImage.getAnnotations()), false));
        } else {
            if (null != candidateImage.getImageBytes() && candidateImage.getImageBytes().length > 0) {
                table.addCell(buildImageCell(
                        drawImageAnnotationLabels(candidateImage.getImageBytes(), candidateImage.getAnnotations()),
                        false));
            } else {
                table.addCell(buildTextCell("Error Building Image Cell."));
            }
        }

        if (StringUtils.isNotBlank(probeImage.getUnrollImageData())
                && StringUtils.isNotBlank(candidateImage.getUnrollImageData())) {
            table.addCell(buildImageCell(
                    drawUnrollImageAnnotationLabels(probeImage.getUnrollImageData(), probeImage.getAnnotations()),
                    true));
            table.addCell(buildImageCell(drawUnrollImageAnnotationLabels(candidateImage.getUnrollImageData(),
                    candidateImage.getAnnotations()), true));
        } else if (probeImage.getUnrollImageBytes().length > 0 && candidateImage.getUnrollImageBytes().length > 0) {
            table.addCell(buildImageCell(
                    drawUnrollImageAnnotationLabels(probeImage.getUnrollImageBytes(), probeImage.getAnnotations()),
                    true));
            table.addCell(buildImageCell(drawUnrollImageAnnotationLabels(candidateImage.getUnrollImageBytes(),
                    candidateImage.getAnnotations()), true));
        }

        probeImage.setReported(true);
        candidateImage.setReported(true);
    }

    protected static void buildAnnotationOutput(String annotationSeparator, List annotationList,
            java.util.List<Annotation> annotations) {
        if (null == annotations || annotations.isEmpty()) {
            return;
        }

        ListItem headerItem = new ListItem(annotationSeparator, listHeaderFont);
        headerItem.setAlignment(Element.ALIGN_JUSTIFIED);
        annotationList.setListSymbol("");
        annotationList.add(headerItem);
        annotationCount = 1;
        annotationList.add(walkAnnotations(annotations, 1));
    }

    private static List walkAnnotations(java.util.List<Annotation> annotations, int level) {
        float indent = 10f * level;
        List levelList = new List(indent);
        levelList.setListSymbol("");
        for (org.mitre.iwp.web.model.Annotation ann : annotations) {
            ListItem li;
            if(!ann.getTempChildren().isEmpty()){
                li = new ListItem(ann.getText(), listHeaderFont);
                li.setIndentationLeft(indent);
                levelList.add(li);

                levelList.add(walkAnnotations(ann.getTempChildren(), level + 1));
            } else {
                li = new ListItem(getAnnotationText(ann), listSubitemFont);
                li.setIndentationLeft(indent);
                levelList.add(li);
                annotationCount++;
            }
        }

        return levelList;
    }

    protected static String getAnnotationText(Annotation ann) {
        if (ann.getValue() == null || ann.getValue().getData() == null) {
            return ann.getText();
        }

        if (ann.getValue().getData().containsKey("adjustment")) {
            return String.format("%s (%s)", ann.getText(), ann.getValue().getData().get("adjustment"));
        } else if (ann.getValue().getData().containsKey("clipFactor")) { // Adaptive Contrast
            return String.format("%s (%s)", ann.getText(), ann.getValue().getData().get("clipFactor"));
        }

        return String.format("%d. %s", annotationCount, ann.getText());
    }

    protected static PdfPCell buildTextCell(String cellString) {
        PdfPCell cell = new PdfPCell(new Phrase(cellString));
        cell.setBorderWidth(ReportingUtil.cellBorder);
        cell.setPadding(cellPadding);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    protected static PdfPCell buildTextCell(Paragraph cellParagraph) {
        PdfPCell cell = new PdfPCell(new Phrase(cellParagraph));
        cell.setPadding(cellPadding);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    protected static PdfPCell buildListCell(List list) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidth(ReportingUtil.cellBorder);
        cell.addElement(list);
        cell.setPaddingBottom(20);
        return cell;
    }

    protected static PdfPCell buildImageCell(byte[] candidateImage, boolean shrinkToFit)
            throws IOException, BadElementException {
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidth(ReportingUtil.cellBorder);
        cell.setPadding(cellPadding);
        Image img = Image.getInstance(candidateImage);
        if (shrinkToFit) {
            img.scaleToFit(150, 150);
        }

        cell.addElement(img);

        return cell;
    }

    protected static PdfPCell buildImageCell(String imageString, boolean shrinkToFit)
            throws IOException, BadElementException {
        byte[] imageBytes = Base64.getDecoder().decode(imageString);
        return buildImageCell(imageBytes, shrinkToFit);
    }

    protected static Paragraph getNewDocumentParagraph(Element e) {
        Paragraph p = new Paragraph();
        p.setSpacingAfter(ReportingUtil.tableSpacing);
        p.add(e);
        return p;
    }

    public static boolean isValid(ReportData reportData) {
        if (reportData == null) {
            log.info("Missing Report Data object");
            return false;
        }

        // Check Probe Info
        if (reportData.getProbe() == null) {
            log.info("Missing Probe object");
            return false;
        } else {
            if (isMissingImageData(reportData.getProbe().getImageList())) {
                log.info("Missing Probe Image Data object");
                return false;
            }
        }

        // Check Candidate Info
        if (reportData.getCandidates() == null || reportData.getCandidates().isEmpty()) {
            log.info("Missing Candidate object");
            return false;
        } else {
            for (Candidate cand : reportData.getCandidates()) {
                if (isMissingImageData(cand.getImageList())) {
                    log.info("Missing Candidate Image Data object");
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isMissingImageData(java.util.List<org.mitre.iwp.web.model.ImageData> imageList) {
        for (ImageData imageData : imageList) {
            if (imageData.getEbtsImageId() == null && StringUtils.isEmpty(imageData.getImageData())) {
                return true;
            }
        }

        return false;
    }

    private static Integer getNumber(Object obj) {
        log.info("Received: {}", obj);
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Double) {
            return ((Double) obj).intValue();
        } else if (obj instanceof String) {
            Double tempDouble = Double.parseDouble(obj.toString());
            return tempDouble.intValue();
        }

        return null;
    }

    public static byte[] drawImageAnnotationLabels(byte[] img, java.util.List<Annotation> annotations)
            throws IOException {
        if (annotations == null) {
            return img;
        }

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(img));
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.green);

        annotationCount = 1;
        walkLabelAnnotations(annotations, graphics);


        ByteArrayOutputStream ret = new ByteArrayOutputStream();
        ImageIO.write(image, "png", ret);

        return ret.toByteArray();
    }

    private static void walkLabelAnnotations(java.util.List<Annotation> annotations, Graphics2D graphics) {
        for (Annotation annotation : annotations) {
            processAnnotation(annotation, graphics);
        }
    }

    private static void processPoint(Annotation annotation, Graphics2D graphics) {
        Integer x = getNumber(annotation.getValue().getPoints().get(0).getX());
        Integer y = getNumber(annotation.getValue().getPoints().get(0).getY());

        if (x != null && y != null) {
            y -= 10;
            graphics.drawString(Integer.toString(annotationCount), x, y);
            log.info("Added Point: {}, {}", x, y);
        } else {
            log.warn("Could not add Point");
        }
    }

    private static void processCircle(Annotation annotation, Graphics2D graphics) {
        Integer x = getNumber(annotation.getValue().getPoints().get(0).getX());
        Integer y = getNumber(annotation.getValue().getPoints().get(0).getY());

        Integer radius = getNumber(annotation.getValue().getData().get("radius"));

        if (x != null && y != null && radius != null) {
            y -= radius + 10;
            graphics.drawString(Integer.toString(annotationCount), x, y);
            log.info("Added Circle: {}, {}, {}", x, y, radius);
        } else {
            log.warn("Could not add Circle");
        }
    }

    private static void processPolygonAndPencil(Annotation annotation, Graphics2D graphics) {
        if (!annotation.getValue().getPoints().isEmpty()) {
            Integer x = getNumber(annotation.getValue().getPoints().get(0).getX());
            Integer y = getNumber(annotation.getValue().getPoints().get(0).getY());

            if (x != null && y != null) {
                y -= 10;
                graphics.drawString(Integer.toString(annotationCount), x, y);
                log.info("Added {}: {}, {}", annotation.getText(), x, y);
            } else {
                log.warn("Could not add {}", annotation.getText());
            }
        }
    }

    private static void handlePermanentAnnotations(Annotation annotation, Graphics2D graphics) {
        // pulled from annotation.ts from the AnnotationType enum
        switch (annotation.getType()) {
            case "0": // point
                processPoint(annotation, graphics);
                break;
            case "1": // circle
                processCircle(annotation, graphics);
                break;
            case "2", "3": // polygon, pencil
                processPolygonAndPencil(annotation, graphics);
                break;
            default: 
                log.info("The annotation type is not a point, circle, polygon, or pencil.");
        }// end of switch
    }
    
    private static void processAnnotation(Annotation annotation, Graphics2D graphics) {
        if (!annotation.getTempChildren().isEmpty()) {
            // Handle temporary children logic here
            walkLabelAnnotations(annotation.getTempChildren(), graphics);
        } else {
            // Process the rest of the annotation logic here
            handlePermanentAnnotations(annotation, graphics); 

            if (annotation.getValue() == null || annotation.getValue().getData() == null)
                return;

            annotation.getValue().getData().put("annotationNumber", String.valueOf(annotationCount));
            annotationCount++;
        }
    }

    public static byte[] drawImageAnnotationLabels(String img, java.util.List<Annotation> annotations)
            throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(img);
        return drawImageAnnotationLabels(imageBytes, annotations);
    }

    private static byte[] drawUnrollImageAnnotationLabels(byte[] img, java.util.List<Annotation> annotations)
            throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(img));
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.green);

        for (Annotation annotation : annotations) {
            // pulled from annotation.ts from the AnnotationType enum
            if (annotation.getType().equals("0")) {
                Integer x = getNumber(annotation.getValue().getData().get("unrollX"));
                Integer y = getNumber(annotation.getValue().getData().get("unrollY"));
                Integer tempLabel = getNumber(annotation.getValue().getData().get("annotationNumber"));
                String label = tempLabel == null ? "" : tempLabel.toString();

                if (x != null && y != null) {
                    y -= 10;
                    graphics.drawString(label, x, y);
                    log.info("Added annotation: {}, {}", x, y);
                } else {
                    log.warn("Could not add annotation");
                }
            }
        }

        ByteArrayOutputStream ret = new ByteArrayOutputStream();
        ImageIO.write(image, "png", ret);

        return ret.toByteArray();
    }

    private static byte[] drawUnrollImageAnnotationLabels(String img, java.util.List<Annotation> annotations)
            throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(img);
        return drawUnrollImageAnnotationLabels(imageBytes, annotations);

    }

    public static boolean checkAdjudication(Map<String, String> adjudicationResults, String candidateAdjudication) {
        if(adjudicationResults.containsKey(candidateAdjudication)){
            return adjudicationResults.get(candidateAdjudication).equalsIgnoreCase("TRUE");
        } else {
            log.error("Unknown adjudication value {}. Omitting candidate", candidateAdjudication);
        }

        return false;
    }
}
