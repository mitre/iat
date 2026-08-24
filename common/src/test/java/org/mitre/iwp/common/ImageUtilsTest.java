/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.common;



import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;

import java.awt.image.BufferedImage;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ImageUtilsTest {

    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);


    ClassLoader classloader = Thread.currentThread().getContextClassLoader();


    @Test
    public void testPixMapGrey() throws Exception {

        log.trace("---------- testPixMapGrey ----------");

        InputStream input = classloader.getResourceAsStream("file-types/image-bitmap.byte.gray");

        BufferedImage result = ImageUtils.read("NONE", 100, 67, 8, input);

        assertNotNull(result, "Greyscale null test");
        assertEquals(100, result.getWidth(), "Greyscale width test");
        assertEquals(67, result.getHeight(), "Greyscale height test");

    }

    @Test
    public void testPixMapRGB() throws Exception {

      log.trace("---------- testPixMapGrey3 ----------");

      InputStream input = classloader.getResourceAsStream("file-types/image-bitmap.rgb");

      BufferedImage result = ImageUtils.read("NONE", 100, 67, 24, input);

      assertNotNull(result, "Greyscale null test");
      assertEquals(100, result.getWidth(), "Greyscale width test");
      assertEquals(67, result.getHeight(), "Greyscale height test");

    }

  
    @Test
    public void testBitmap() throws Exception{

        log.trace("---------- testBitmap ----------");

        InputStream input = classloader.getResourceAsStream("file-types/image-bitmap.bmp");

        Image result = ImageUtils.read("BMP", 100, 67, 24, input);

        BufferedImage origImage = ImageIO.read(classloader.getResourceAsStream("file-types/image-bitmap.bmp"));
        BufferedImage resultImage = toBufferedImage(result);

        assertTrue(compareImages(origImage, resultImage), "Success: BMP Test 1");

    }

    @Test
    public void testBitmap2() throws Exception{

        log.trace("---------- testBitmap2 ----------");

        InputStream input = classloader.getResourceAsStream("file-types/image-bitmap.bmp");

        // convert file to byte array
        byte[] inputBytes = IOUtils.toByteArray(input);
        Image result = ImageUtils.read("BMP", 100, 67, 24, inputBytes);

        BufferedImage origImage = ImageIO.read(classloader.getResourceAsStream("file-types/image-bitmap.bmp"));
        BufferedImage resultImage = toBufferedImage(result);

        assertTrue(compareImages(origImage, resultImage), "Success: BMP Test 2");

    }

    @Test
    public void testJPEG2000() throws Exception{
      log.trace("----------test jpeg 2000----------");
      InputStream input = classloader.getResourceAsStream("file-types/image-jp2.jp2");

      byte[] inputBytes = IOUtils.toByteArray(input);
      BufferedImage ret = ImageUtils.read("JP2", -1, -1, -1, inputBytes);

      assertEquals(100, ret.getWidth(), "Check JP2 image width");
      assertEquals(67, ret.getHeight(), "Check JP2 iamge height");
    }

    @Test
    public void testJPEG() throws Exception {

        log.trace("---------- testJPEG ----------");

        InputStream input = classloader.getResourceAsStream("file-types/image-jpeg.jpeg");

        BufferedImage result = ImageUtils.read("JPEG", 100, 67, 24, input);

        assertEquals(100, result.getWidth(), "Check JPEG image width");
        assertEquals(67, result.getHeight(), "Check JPEG iamge height");

    }

    @Test
    public void testJPG() throws Exception {

        log.trace("---------- testJPG ----------");

        InputStream input = classloader.getResourceAsStream("file-types/image-jpeg.jpg");

        BufferedImage result = ImageUtils.read("JPG", 100, 67, 24, input);

        assertEquals(100, result.getWidth(), "Check JPG image width");
        assertEquals(67, result.getHeight(), "Check JPG iamge height");

    }


    @Test
    public void testPNG() throws Exception {

        log.trace("---------- testPNG ----------");

        InputStream input = classloader.getResourceAsStream("file-types/image-png.png");

        Image result = ImageUtils.read("PNG", 100, 67, 24, input);

        if(result == null){
            System.out.println("result is null");
        }

        BufferedImage origImage = ImageIO.read(classloader.getResourceAsStream("file-types/image-png.png"));
        BufferedImage resultImage = toBufferedImage(result);

        assertTrue(compareImages(origImage, resultImage), "Success: PNG Test 1");
    }

    @Test
    public void testPNG2() throws Exception{

        log.trace("---------- testPNG2 ----------");


        InputStream input = classloader.getResourceAsStream("file-types/image-png.png");

        // convert file to byte array
        byte[] inputBytes = IOUtils.toByteArray(input);
        Image result = ImageUtils.read("PNG", 100, 67, 24, inputBytes);

        BufferedImage origImage = ImageIO.read(classloader.getResourceAsStream("file-types/image-png.png"));
        BufferedImage resultImage = toBufferedImage(result);

        assertTrue(compareImages(origImage, resultImage), "Success: PNG Test 2");

    }

    @Test
    public void testTIF() throws Exception{

        log.trace("---------- testTIF ----------");


        InputStream input = classloader.getResourceAsStream("file-types/image-tif.tif");

        BufferedImage result = ImageUtils.read("TIF", 100, 67, 24, input);

        assertEquals(100, result.getWidth(), "Check TIF image width");
        assertEquals(67, result.getHeight(), "Check TIF iamge height");
    }

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public static boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
        // The images must be the same size.
        if (imgA.getWidth() == imgB.getWidth() && imgA.getHeight() == imgB.getHeight()) {
            int width = imgA.getWidth();
            int height = imgA.getHeight();

            // Loop over every pixel.
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Compare the pixels for equality.
                    if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }
}
