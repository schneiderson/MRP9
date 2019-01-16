package mesh.thinning;
/**
 * File: MyImage.java
 * 
 * Description:
 * This file contains MyImage class which is used to create BufferedImage objects.
 * 
 * @author Yusuf Shakeel
 * @version 1.0
 * date: 26-01-2014 sun
 * 
 * www.github.com/yusufshakeel/Java-Image-Processing-Project
 * 
 * The MIT License (MIT)
 * Copyright (c) 2014 Yusuf Shakeel
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mesh.GradientCalculator;
import stippling.main.Filters;

public class MyImage {
    
    //////////////////////////// VARIABLES /////////////////////////////////////
    
    /** Store the Image reference */
    private BufferedImage image;

    /** Store the image width and height */
    private int width, height;

    /** Pixels value - ARGB */
    private int pixels[];
    
    /** Total number of pixel in an image*/
    private int totalPixels;
    
    /** 
     * Image type example: jpg|png 
     * JPG does not support alpha (transparency is lost) while PNG supports alpha.
     */
    private enum ImageType{
        JPG, PNG
    };
    
    private ImageType imgType;
    
    ////////////////////////////////// CONSTRUCTORS ////////////////////////////
    
    /** Default constructor */
    public MyImage(){}
    
    /** 
     * Constructor to create a new image object
     * 
     * @param width width of the image passed by the user
     * @param height height of the image passed by the user
     */
    public MyImage(int width, int height){
        this.width = width;
        this.height = height;
        this.totalPixels = this.width * this.height;
        this.pixels = new int[this.totalPixels];
        image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        this.imgType = ImageType.PNG;
        initPixelArray();
    }
    
    /** 
     * Constructor to create a copy of a previously created image object.
     * 
     * @param img The image object whose copy is created.
     */
    public MyImage(MyImage img){
        this.width = img.getImageWidth();
        this.height = img.getImageHeight();
        this.totalPixels = this.width * this.height;
        this.pixels = new int[this.totalPixels];
        
        this.imgType = img.imgType;
        if(this.imgType == ImageType.PNG){
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }else{
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
        
        //copy original image pixels value to new image and pixels array
        for(int y = 0; y < this.height; y++){
            for(int x = 0; x < this.width; x++){
                this.image.setRGB(x, y, img.getPixel(x, y));
                this.pixels[x+y*this.width] = img.getPixel(x, y);
            }
        }
    }
    
    /////////////////////////////////////// METHODS ////////////////////////////
    
    /**
     * This method will modify the image object.
     * 
     * @param width The width of the new image.
     * @param height The height of the new image.
     * @param bi The new image that will replace the old image.
     */
    public void modifyImageObject(int width, int height, BufferedImage bi){
        this.width = width;
        this.height = height;
        this.totalPixels = this.width * this.height;
        this.pixels = new int[this.totalPixels];
        if(this.imgType == ImageType.PNG){
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }else{
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D g2d = this.image.createGraphics();
        g2d.drawImage(bi, 0, 0, null);
        g2d.dispose();
        initPixelArray();
    }
    
    /** 
     * Read the image using the image file path passed
     * 
     * @param filePath the path of the image file
     * Example filePath = "D:\\LogoJava.jpg"
     */
    public void readImage(String filePath){
        try{
            File f = new File(filePath);
            image = ImageIO.read(f);
            String fileType = filePath.substring(filePath.lastIndexOf('.')+1);
            if("jpg".equals(fileType)){
                imgType = ImageType.JPG;
            }else{
                imgType = ImageType.PNG;
            }
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.totalPixels = this.width * this.height;
            this.pixels = new int[this.totalPixels];
            initPixelArray();
        }catch(IOException e){
            System.out.println("Error Occurred!\n"+e);
        }
        displayImage();
    }
    
    /**
     * Write the content of the BufferedImage object 'image' to a file
     * 
     * @param filePath complete file path of the output image file to be created
     * Example filePath = "D:\\Output.jpg"
     */
    public void writeImage(String filePath){
        try{
            File f = new File(filePath);
            String fileType = filePath.substring(filePath.lastIndexOf('.')+1);
            ImageIO.write(image, fileType, f);
        }catch(IOException e){
            System.out.println("Error Occurred!\n"+e);
        }
    }
    
    /**
     * Initialize the pixel array
     * Image origin is at coordinate (0,0)
     * (0,0)--------> X-axis
     *     |
     *     |
     *     |
     *     v
     *   Y-axis
     * 
     * This method will store the value of each pixels of a 2D image in a 1D array.
     */
    private void initPixelArray(){
        PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
        try{
            pg.grabPixels();
        }catch(InterruptedException e){
            System.out.println("Error Occurred: "+e);
        }
    }
    
    /**
     * This method will check for equality of two images.
     * If we have two image img1 and img2
     * Then calling img1.isEqual(img2) will return TRUE if img1 and img2 are equal
     * else it will return FALSE.
     * 
     * @param img The image to check with.
     * @return TRUE if two images are equal else FALSE.
     */
    public boolean isEqual(MyImage img){
        //check dimension
        if(this.width != img.getImageWidth() || this.height != img.getImageHeight()){
            return false;
        }
        
        //check pixel values
        for(int y = 0; y < this.height; y++){
            for(int x = 0; x < this.width; x++){
                if(this.pixels[x+y*this.width] != img.getPixel(x, y)){
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /////////////////////////// GET, SET AND UPDATE METHODS ////////////////////
    
    /**
     * Return the image width
     * 
     * @return the width of the image
     */
    public int getImageWidth(){
        return width;
    }
    
    /**
     * Return the image height
     * 
     * @return the height of the image
     */
    public int getImageHeight(){
        return height;
    }
    
    /**
     * Return total number of pixels in the image
     * 
     * @return the total number of pixels
     */
    public int getImageTotalPixels(){
        return totalPixels;
    }
    
    /**
     * This method will return the amount of alpha value between 0-255 at the pixel (x,y)
     * 
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the amount of alpha (transparency)
     * 
     * 0 means transparent
     * 255 means opaque
     */
    public int getAlpha(int x, int y){
        return (pixels[x+(y*width)] >> 24) & 0xFF;
    }
    
    /**
     * This method will return the amount of red value between 0-255 at the pixel (x,y)
     * 
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the amount of red
     * 
     * 0 means none
     * 255 means fully red
     */
    public int getRed(int x, int y){
        return (pixels[x+(y*width)] >> 16) & 0xFF;
    }
    
    /**
     * This method will return the amount of green value between 0-255 at the pixel (x,y)
     * 
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel 
     * @return the amount of green
     * 
     * 0 means none
     * 255 means fully green
     */
    public int getGreen(int x, int y){
        return (pixels[x+(y*width)] >> 8) & 0xFF;
    }
    
    /**
     * This method will return the amount of blue value between 0-255 at the pixel (x,y)
     * 
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the amount of blue
     * 
     * 0 means none
     * 255 means fully blue
     */
    public int getBlue(int x, int y){
        return pixels[x+(y*width)] & 0xFF;
    }
    
    /**
     * This method will return the pixel value of the pixel at the coordinate (x,y)
     * 
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixel value in integer [Value can be negative and positive.]
     */
    public int getPixel(int x, int y){
        return pixels[x+(y*width)];
    }
    
    /**
     * This method will return the pixel value of the image as 1D array.
     * 
     * @return 1D array containing value of each pixels of the image.
     */
    public int[] getPixelArray(){
        return pixels;
    }
        
    /**
     * This method will set the amount of alpha value between 0-255 at the pixel (x,y)
     * 
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param alpha the alpha value to set
     * 
     * 0 means transparent
     * 255 means opaque
     */
    public void setAlpha(int x, int y, int alpha){
        pixels[x+(y*width)] = (alpha<<24) | (pixels[x+(y*width)] & 0x00FFFFFF);
        updateImagePixelAt(x,y);
    }
    
    /**
     * This method will set the amount of red value between 0-255 at the pixel (x,y)
     * 
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param red the red value to set
     * 
     * 0 means none
     * 255 means fully red
     */
    public void setRed(int x, int y, int red){
        pixels[x+(y*width)] = (red<<16) | (pixels[x+(y*width)] & 0xFF00FFFF);
        updateImagePixelAt(x,y);
    }
    
    /**
     * This method will set the amount of green value between 0-255 at the pixel (x,y)
     * 
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param green the green value to set
     * 
     * 0 means none
     * 255 means fully green
     */
    public void setGreen(int x, int y, int green){
        pixels[x+(y*width)] = (green<<8) | (pixels[x+(y*width)] & 0xFFFF00FF);
        updateImagePixelAt(x,y);
    }
    
    /**
     * This method will set the amount of blue value between 0-255 at the pixel (x,y)
     * 
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param blue the blue value to set
     * 
     * 0 means none
     * 255 means fully blue
     */
    public void setBlue(int x, int y, int blue){
        pixels[x+(y*width)] = blue | (pixels[x+(y*width)] & 0xFFFFFF00);
        updateImagePixelAt(x,y);
    }
    
    /**
     * This method will set pixel(x,y) to ARGB value.
     * 
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param alpha the alpha value to set [value from 0-255]
     * @param red the red value to set [value from 0-255]
     * @param green the green value to set [value from 0-255]
     * @param blue the blue value to set  [value from 0-255]
     */
    public void setPixel(int x, int y, int alpha, int red, int green, int blue){
        pixels[x+(y*width)] = (alpha<<24) | (red<<16) | (green<<8) | blue;
        updateImagePixelAt(x,y);
    }
    
    /**
     * This method will set pixel (x,y) to pixelValue.
     * 
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param pixelValue the pixel value to set
     */
    public void setPixelToValue(int x, int y, int pixelValue){
        pixels[x+(y*width)] = pixelValue;
        updateImagePixelAt(x,y);
    }
    
    /**
     * This method will set the image pixel array to the value of the 1D pixelArray.
     * 
     * @param pixelArray 1D array containing values that is set to the image pixel array.77
     */
    public void setPixelArray(int pixelArray[]){
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                pixels[x+y*width] = pixelArray[x+y*width];
                updateImagePixelAt(x,y);
            }
        }
    }
    
    /**
     * This method will update the image pixel at coordinate (x,y)
     * 
     * @param x the x coordinate of the pixel that is set
     * @param y the y coordinate of the pixel that is set
     */
    private void updateImagePixelAt(int x, int y){
        image.setRGB(x, y, pixels[x+(y*width)]);
    }
    
    
    ////////////////////// BLUR IMAGE ///////////////////////////////
    
    /**
	 * Blur the source image.
	 */
	public void blurImage(int blurFactor) {
		int tempWidth = width;
		int tempHeight = height;
		
		addBorder(blurFactor, tempWidth, tempHeight);
		
  		image = Filters.gaussianBlurFilter(blurFactor, true).filter(image, null);
  		image = Filters.gaussianBlurFilter(blurFactor, false).filter(image, null);

  		removeBorder(blurFactor, tempWidth, tempHeight);
  		
  		writeImage("res/BLURRED.png");
  		displayImage();
	}

	/**
	 * Create border around image
	 */
	void addBorder(int blurFactor, int tempWidth, int tempHeight){

		final int borderWidth = blurFactor;

		int borderedImageWidth = width + (borderWidth * 2);
		int borderedImageHeight = height + (borderWidth * 2);
		
		int[] temp = pixels;
		modifyImageObject(borderedImageWidth, borderedImageHeight, new BufferedImage(borderedImageWidth, borderedImageHeight, BufferedImage.TYPE_3BYTE_BGR));
		int[] background = new int[borderedImageWidth*borderedImageHeight];
		Arrays.fill(background, 0);
		for (int y = borderWidth; y < borderedImageHeight-borderWidth; y++){
			for (int x = borderWidth; x < borderedImageWidth-borderWidth; x++){
				background[x+(y*borderedImageWidth)] = (int) temp[x-borderWidth+((y-borderWidth)*tempWidth)];
			}
		}
		setPixelArray(background);
		
//		img.createGraphics();
//
//		Graphics2D g = (Graphics2D) img.getGraphics();
//		g.setColor(Color.BLACK);
//		g.fillRect(0, 0, borderedImageWidth, borderedImageHeight);
//
//		g.drawImage(image, borderWidth, borderWidth, width + borderWidth, height + borderWidth,
//				0, 0, width, height, Color.YELLOW, null);
	}


	/**
	 * Remove border around image
	 */
	void removeBorder(int blurFactor, int tempWidth, int tempHeight){
		final int borderWidth = blurFactor;
		modifyImageObject(tempWidth, tempHeight, image.getSubimage(borderWidth, borderWidth, tempWidth, tempHeight));
		//image = image.getSubimage(borderWidth, borderWidth, width, height);
	}
	
	/** 
	 * Turn image into grayscale image.
	 */
	public void toGrayscale() {
		for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int rgba = getPixel(x, y);
                int intensity = (int) Math.round(intensity(rgba));
                setRed(x, y, intensity);
                setGreen(x, y, intensity);
                setBlue(x, y, intensity);
            }
        }
		writeImage("res/GRAYSCALE.png");
	}

	//-------------------------------------------------------------------------

	/**
	 * @param rgba
	 * @return Intensity.
	 */
	public static double intensity(final int rgba)
	{
		final int r = (rgba >> 16) & 0xff;
		final int g = (rgba >>  8) & 0xff;
		final int b = (rgba      ) & 0xff;
		//final int a = (rgba >> 24) & 0xff;
		final double value = (r + g + b) / 3.0;
		return value;
	}
	
	public void quantize(int noBins) {
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
//				int quantized = Math.round(Math.round(getRed(x, y)*noBins) *(255/noBins));
                int quantized = (int) Math.round(Math.ceil((getRed(x, y)/(255/noBins)))*(255/noBins));
//				int quantized = 33;
                setRed(x, y, quantized);
                setGreen(x, y, quantized);
                setBlue(x, y, quantized);
            }
        }
        writeImage("res/QUANTIZED.png");
        displayImage();
	}
	
	public void calculateGradient(){
		GradientCalculator gradCalc = new GradientCalculator();
		gradCalc.calculateGradientFromImage(toMap(), width, height);
		float[][] gradient = gradCalc.gradient;
		//resetImageTo(0);
		for (int y = 0; y < height; y++){
			for (int x = 0; x < width; x++){
				if (gradient[x][y] == 0){
					setRed(x, y, 0);
					setBlue(x, y, 0);
					setGreen(x, y, 0);
				}
				else {
					setRed(x, y, 255);
					setBlue(x, y, 255);
					setGreen(x, y, 255);
				}

			}
		}
	}

	
	public void binaryToBW(){
		for (int y = 0; y < height; y++){
			for (int x = 0; x < width; x++){
				if (getRed(x, y) == 0){
					setRed(x, y, 255);
					setGreen(x, y, 255);
					setBlue(x, y, 255);
				}
				else{
					setRed(x, y, 0);
					setGreen(x, y, 0);
					setBlue(x, y, 0);
				}
			}
		}
		displayImage();
	}
	
	public void invertBinary(){
		float[][] temp = toMap();
		resetImageTo(255);
		for (int y = 0; y < height; y++){
			for (int x = 0; x < width; x++){
				if (temp[x][y] == 255){
					setRed(x, y, 0);
					setGreen(x, y, 0);
					setBlue(x, y, 0);
				}
			}
		}
	}
    
	public float[][] toMap(){
		float[][] map = new float[width][height];
		
		for (int y = 0; y < height; y++){
			for (int x = 0; x < width; x++){
				map[x][y] = getRed(x, y);
			}
		}
		return map;
	}
	
	public void update(float[][] map){
		for (int y = 0; y < height; y++){
			for (int x = 0; x < width; x++){
				setRed(x, y, (int) map[x][y]);
				setGreen(x, y, (int) map[x][y]);
				setBlue(x, y, (int) map[x][y]);
			}
		}
	}
	
	private void resetImageTo(int value){
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++){
				setRed(x, y, value);
				setGreen(x, y, value);
				setBlue(x, y, value);
			}
	}
	
	public void setOpaque(){
		for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                setAlpha(x, y, 255);
            }
        }
	}
	
	public void displayImage(){
		JFrame frame = new JFrame();
		JLabel lblimage = new JLabel(new ImageIcon(image));
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(lblimage);
		
		frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
		frame.add(mainPanel);
		frame.setVisible(true);
	}
	
}//class ImageFX ends here