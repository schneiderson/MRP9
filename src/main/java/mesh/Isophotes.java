package mesh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.*;

import knotwork.Edge;

import org.locationtech.jts.geom.Coordinate;

import stippling.main.Stippler;
import svg.SVGUtil;

public class Isophotes {
	
	String imgPath = "res/robot-2.jpg";
	int noBins = 10;
	int blurFactor = 5;
	ArrayList<Coordinate> borderCoords = new ArrayList<Coordinate>();
	ArrayList<Coordinate> innerCoords = new ArrayList<Coordinate>();
	double[][] gradient;
	
	public Isophotes(String imgPath, int noBins, int blurFactor){
		this.imgPath = imgPath;
		this.noBins = noBins;
		this.blurFactor = blurFactor;
	}

	public void init() {
		Stippler stippler = new Stippler();
		stippler.setBlurFactor(blurFactor);
		stippler.loadImage(imgPath);
		float[][] intensities = stippler.getIntensity();
		int[][] quantized = quantize(intensities);
		SVGUtil svgutil = isophoteLines(quantized);
		System.out.println("Done.");
	}
	
	public int[][] quantize(float[][] img) {
		
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

	public SVGUtil isophoteLines(int[][] quantizedImg){
		
		int ht = quantizedImg.length;
		int wd = quantizedImg[0].length;
		
		ArrayList<Edge> edges = new ArrayList<Edge>();

		GradientCalculator gradCalc = new GradientCalculator();
		gradCalc.calculateGradientFromImage(quantizedImg, ht, wd);
		double[][] gradient = gradCalc.gradient;
		
		BufferedImage image = new BufferedImage(ht, wd, BufferedImage.TYPE_INT_ARGB);
		
		for (int i = 0; i < ht; i++){
			for (int j = 0; j < wd; j++){
				if(gradient[i][j] == 0)
					gradient[i][j] = 255;
				else{
					gradient[i][j] = 0;
					if (i == 0 || j == 0 || i == ht-1 || j == wd-1)
						borderCoords.add(new Coordinate(i,j));
					else
						innerCoords.add(new Coordinate(i,j));
				}
				Color newColor = new Color((int) gradient[i][j], (int) gradient[i][j], (int) gradient[i][j]);
				image.setRGB(i, j, newColor.getRGB());
			}
		}

		displayImage(image);
		
		
		ArrayList<ArrayList<Coordinate>> isophotes = new ArrayList<ArrayList<Coordinate>>();
		
		//System.out.println("Number border coordinates: " + borderCoords.size());
		System.out.println("Number inner coordinates: " + innerCoords.size());
		
		boolean loop = true;
		while (loop){
			if(innerCoords.size() < 1){
				loop = false;
				break;
			}
			
			Coordinate c = innerCoords.get(0);
			
			int i = (int) c.x;
			int j = (int) c.y;
			
			Coordinate frst = null;
			Coordinate cur = new Coordinate(i,j);
			int cnt = 0;
			ArrayList<Coordinate> ring = new ArrayList<Coordinate>();
			while (cur != null){
				ring.add(cur);
				Coordinate temp = cur;
				cur = next(frst, temp, cnt);
				cnt++;
				if (frst == null)
					frst = new Coordinate(temp.x, temp.y);
			}
			isophotes.add(ring);
		}
		
		
		for (ArrayList<Coordinate> r : isophotes){
			System.out.println("Number final coordinates: " + r.size());
			for (int i = 1; i < r.size(); i++){
				edges.add(new Edge(r.get(i-1), r.get(i)));
			}
			edges.add(new Edge(r.get(0), r.get(r.size()-1)));
		}
		
		SVGUtil svgwrite = new SVGUtil(edges, null);
        svgwrite.createSVG(System.getProperty("user.dir") + "/res/isophoteLines.svg");
        
        return svgwrite;
	}
	
	public boolean isNeighbour(Coordinate cur, Coordinate other){
		return (cur.distance(other) <= 0);
	}
	
	public Coordinate next(Coordinate first, Coordinate cur, int count){
		Coordinate test, next = null;
		int dist = 3;
		
		int ix = -dist;
		int jy = -dist;
		
		// add other side borders?
		if (cur.x < dist && cur.y < dist){
			ix = 0;
			jy = 0;
		}
		else if (cur.x < dist)
			ix = 0;
		else if (cur.y < dist)
			jy = 0;
		
		for (int i = ix; i <= dist; i++){
			for (int j = jy; j <= dist; j++){
				if (Math.abs(i) == Math.abs(j)){
					test = new Coordinate(cur.x+i, cur.y+j);
					innerCoords.remove(cur);
					// euqals first within first three
					if (test.equals(first) && count > 10){
						System.out.println("Ring completed");
						return null;
					}
					if (innerCoords.contains(test)){
						next = test;
						innerCoords.remove(test);
						//System.out.println("list size: " + innerCoords.size());
						//System.out.println("Next x = " + next.x + ", next y = " + next.y);
					}	
				}
			}
		}
		
		for (int i = ix; i <= dist; i++){
			for (int j = jy; j <= dist; j++){
				if (Math.abs(i) != Math.abs(j)){
					test = new Coordinate(cur.x+i, cur.y+j);
					innerCoords.remove(cur);
					// euqals first within first three
					if (test.equals(first) && count > 10){
						System.out.println("Ring completed");
						return null;
					}
					if (innerCoords.contains(test)){
						next = test;
						innerCoords.remove(test);
						//System.out.println("list size: " + innerCoords.size());
						//System.out.println("Next x = " + next.x + ", next y = " + next.y);
					}	
				}
			}
		}
		
		if (next == null)
			System.out.println("WARNING: LINE ENDS IN NOWHERE!");
		
		return next;
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
