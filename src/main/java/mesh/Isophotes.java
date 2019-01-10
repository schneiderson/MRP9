package mesh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

import knotwork.Edge;

import org.locationtech.jts.geom.Coordinate;

import stippling.main.Stippler;
import svg.SVGUtil;

public class Isophotes {
	
	String imgPath = "res/robot-2.jpg";
	int noBins = 10;
	int blurFactor = 5;
	
	public Isophotes(String imgPath, int noBins, int blurFactor){
		this.imgPath = imgPath;
		this.noBins = noBins;
		this.blurFactor = blurFactor;
	}

	public void init(){
		Stippler stippler = new Stippler();
		stippler.setBlurFactor(blurFactor);
		stippler.loadImage(imgPath);
		float[][] intensities = stippler.getIntensity();
		int[][] quantized = quantize(intensities);
		isophoteLines(quantized);
		System.out.println("Done.");
	}
	
	public int[][] quantize(float[][] img){
		
		int ht = img.length;
		int wd = img[0].length;
		
		int[][] quantized = new int[ht][wd];
		BufferedImage image = new BufferedImage(ht, wd, BufferedImage.TYPE_INT_ARGB);
		
		for (int i = 0; i < ht; i++){
			for (int j = 0; j < wd; j++){
				quantized[i][j] = Math.round(Math.round(img[i][j]*noBins) *(255/noBins));
				Color newColor = new Color(quantized[i][j], quantized[i][j], quantized[i][j]);
				image.setRGB(i, j, newColor.getRGB());
			}
		}
		
		displayImage(image);
		
		return quantized;
	}

	public void isophoteLines(int[][] quantizedImg){
		
		int ht = quantizedImg.length;
		int wd = quantizedImg[0].length;
		
		//ArrayList<Edge> edges = new ArrayList<Edge>();

		GradientCalculator gradCalc = new GradientCalculator();
		gradCalc.calculateGradientFromImage(quantizedImg, ht, wd);
		double[][] gradient = gradCalc.gradient;
		
		BufferedImage image = new BufferedImage(ht, wd, BufferedImage.TYPE_INT_ARGB);
		
		for (int i = 0; i < ht; i++){
			for (int j = 0; j < wd; j++){
				if(gradient[i][j] == 0)
					gradient[i][j] = 255;
				else
					gradient[i][j] = 0;
				Color newColor = new Color((int) gradient[i][j], (int) gradient[i][j], (int) gradient[i][j]);
				image.setRGB(i, j, newColor.getRGB());
			}
		}
		
		displayImage(image);
	}
	
	public boolean isNeighbour(Coordinate cur, Coordinate other){
		return (cur.distance(other) <= 2);
	}
	
	public void displayImage(BufferedImage img){
		JFrame frame = new JFrame();
		JLabel lblimage = new JLabel(new ImageIcon(img));
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(lblimage);
		
		frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
		frame.add(mainPanel);
		frame.setVisible(true);
	}
	
}
