/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.model;

import com.google.gson.annotations.Expose;
import org.mitre.iwp.buffers.Tshepii;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TshepiiResponse extends ServiceResponse {
    private static final Logger log = LoggerFactory.getLogger(TshepiiResponse.class);
    public TshepiiResponse(){
        // Empty Constructor to satisfy JPA requirement
    }

    @Lob
	@Column(columnDefinition="LONGBLOB")
    private byte[] serviceResponse;
    public void setServiceResponse(byte[] response) { this.serviceResponse = response; }
    public byte[] getServiceResponse() { return this.serviceResponse; }

    @Transient
    @Expose
    private Point cropCenter;

    @Transient
    @Expose
    private Point originalIrisCenter;

    @Transient
    @Expose
    private Point originalPupilCenter;

    @Transient
    @Expose
    private double cropRadius = 0;

    @Transient
    @Expose
    private double originalIrisRadius = 0;

    @Transient
    @Expose
    private double originalPupilRadius = 0;

    @Transient
    @Expose
    private List<Crypt> cryptList = new ArrayList<>();
    public List<Crypt> getCryptList() { return this.cryptList; }
    public void setCryptList(List<Crypt> cryptList) { this.cryptList = cryptList; }

    @Transient
    @Expose
    private List<Point> keyPoints = new ArrayList<>();

    @Transient
    public List<Point> getKeyPoints() {
        return this.keyPoints;
    }

    @Transient
    @Expose
    private List<Integer> matchIndex = new ArrayList<>();

    public Point getCropCenter() {
        return cropCenter;
    }

    public void setCropCenter(Point center) { this.cropCenter = center; }
    public double getCropRadius(){
        return this.cropRadius;
    }
    public void setCropRadius(double radius){
        this.cropRadius = radius;
    }

    public Point getOriginalIrisCenter() {
        return this.originalIrisCenter;
    }
    public void setOriginalIrisCenter(Point center) { this.originalIrisCenter = center; }

    public double getOriginalIrisRadius(){
        return this.originalIrisRadius;
    }
    public void setOriginalIrisRadius(double radius){
        this.originalIrisRadius = radius;
    }

    public Point getOriginalPupilCenter() { return originalPupilCenter; }
    public void setOriginalPupilCenter(Point center) { this.originalPupilCenter = center; }

    public double getOriginalPupilRadius(){
        return this.originalPupilRadius;
    }
    public void setOriginalPupilRadius(double radius){
        this.originalPupilRadius = radius;
    }

    public List<Integer> getMatchIndex() { return this.matchIndex; }

    public int getCryptCount(){
        return this.cryptList.size();
    }

    public List<Point> getCryptAt(int index) {
        if(index < 0 || index >= this.cryptList.size())
            return new ArrayList<>();

        Crypt crypt = this.cryptList.get(index);
        return crypt.getPointList();
    }

    public void buildFromCryptResponse(Tshepii.TShepiiResponse tshepiiServiceResponse){

        if(tshepiiServiceResponse == null) {
            log.info("Tshepii Protobuff did not properly parse. Unknown reason");
            return;
        }

        this.setEbtsImageId(Long.parseLong(tshepiiServiceResponse.getImageId()));

        this.setCropCenter(new Point(tshepiiServiceResponse.getCropIris().getCenter().getX(), tshepiiServiceResponse.getCropIris().getCenter().getY()));
        this.setCropRadius(tshepiiServiceResponse.getCropIris().getRadius());

        this.setOriginalIrisCenter(new Point(tshepiiServiceResponse.getOriginalIris().getCenter().getX(), tshepiiServiceResponse.getOriginalIris().getCenter().getY()));
        this.setOriginalIrisRadius(tshepiiServiceResponse.getOriginalIris().getRadius());

        this.setOriginalPupilCenter(new Point(tshepiiServiceResponse.getOriginalPupil().getCenter().getX(), tshepiiServiceResponse.getOriginalPupil().getCenter().getY()));
        this.setOriginalPupilRadius(tshepiiServiceResponse.getOriginalPupil().getRadius());

        for(Tshepii.Crypt cryptListItem : tshepiiServiceResponse.getCryptsList()){
            List<Point> pointList = new ArrayList<>();

            for(Tshepii.TPoint cryptPoint : cryptListItem.getPointsList()){
                pointList.add(new Point(cryptPoint.getX(), cryptPoint.getY()));
            }

            Crypt crypt = new Crypt(pointList);
            this.cryptList.add(crypt);
        }

        for(int matchItem : tshepiiServiceResponse.getMatchIndexList()){
            this.getMatchIndex().add(matchItem);
        }

        for(Tshepii.TPoint tpoint : tshepiiServiceResponse.getKeypointsList()){
            this.keyPoints.add(new Point(tpoint.getX(), tpoint.getY()));
        }
    }
}
