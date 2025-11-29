package com.ameliaWx.timelapseUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class LocalDeflicker {
	private static final String IN_FOLDER = 
			"/media/nvme1/RAW File Temp Storage/Chase 2025-11-11 Cheney State Park KS Auroras/darktable_exported_B_substorm_over_lake";
	
	public static void main(String[] args) throws IOException {
		List<File> allFiles = allFilesInDirectory(new File(IN_FOLDER));
		
		allFiles.sort(null);
		
		for(int i = 0; i < allFiles.size(); i++) {
			System.out.println(allFiles.get(i));
		}
		
		double targetLuminance = imageLuminanceLegacy(ImageIO.read(allFiles.get(0)));
		
		double previousLuminance0 = targetLuminance;
		double previousLuminance1 = targetLuminance;
		double previousLuminance2 = targetLuminance;
		double previousLuminance3 = targetLuminance;
		double previousLuminance4 = targetLuminance;
		double previousLuminance5 = targetLuminance;
		double previousLuminance6 = targetLuminance;
		double previousLuminance7 = targetLuminance;
		double previousLuminance8 = targetLuminance;
		double previousLuminance9 = targetLuminance;
		double previousLuminanceA = targetLuminance; // hex digits now lol
		double previousLuminanceB = targetLuminance;
		double previousLuminanceC = targetLuminance;
		double previousLuminanceD = targetLuminance;
		double previousLuminanceE = targetLuminance;

		for(int i = 1; i < allFiles.size(); i++) {
			File frame = allFiles.get(i);
			
			String filepath = frame.getAbsolutePath();
			
			String[] tokens = filepath.split("/");
			String filename = tokens[tokens.length - 1];
			
			String path = filepath.substring(0, filepath.length() - filename.length());
			
			String croppedFilepath = path + "l-deflickered/" + tokens[tokens.length - 1];
			
			if(!new File(path + "l-deflickered/").exists()) {
				new File(path + "l-deflickered/").mkdirs();
			}
			
			targetLuminance = average(previousLuminance0, previousLuminance1, previousLuminance2, previousLuminance3, previousLuminance4,
					previousLuminance5, previousLuminance6, previousLuminance7, previousLuminance8, previousLuminance9,
					previousLuminanceA, previousLuminanceB, previousLuminanceC, previousLuminanceD, previousLuminanceE);
			
			BufferedImage correctedImg = correctImageLuminanceLegacy(ImageIO.read(frame), targetLuminance);
			
			File correctedFrame = new File(croppedFilepath);
			
			System.out.println(croppedFilepath);
			
			ImageIO.write(correctedImg, "JPEG", correctedFrame);
			
			double oldLuminance = imageLuminanceLegacy(ImageIO.read(allFiles.get(i)));
			previousLuminanceE = previousLuminanceD;
			previousLuminanceD = previousLuminanceC;
			previousLuminanceC = previousLuminanceB;
			previousLuminanceB = previousLuminanceA;
			previousLuminanceA = previousLuminance9;
			previousLuminance9 = previousLuminance8;
			previousLuminance8 = previousLuminance7;
			previousLuminance7 = previousLuminance6;
			previousLuminance6 = previousLuminance5;
			previousLuminance5 = previousLuminance4;
			previousLuminance4 = previousLuminance3;
			previousLuminance3 = previousLuminance2;
			previousLuminance2 = previousLuminance1;
			previousLuminance1 = previousLuminance0;
			previousLuminance0 = oldLuminance;
		}
	}
	
	private static double average(double... data) {
		double sum = 0;
		
		for(int i = 0; i < data.length; i++) {
			sum += data[i];
		}
		
		return sum/data.length;
	}
	
	private static double imageLuminance(BufferedImage img) {
		double averageLuminance = 0.0;
		int pixelsCounted = 0;
		
		for(int i = 0; i < img.getWidth(); i++) {
			for(int j = 0; j < img.getHeight(); j++) {
				double luminance = HSLColor.fromRgb(new Color(img.getRGB(i, j))).getLuminance();
				pixelsCounted++;
				
				averageLuminance = (1 - 1.0 / pixelsCounted) * averageLuminance + (1.0 / pixelsCounted) * luminance;
			}
		}
		
		return averageLuminance;
	}
	
	@SuppressWarnings("unused")
	private static double imageLuminanceLegacy(BufferedImage img) {
		double averageLuminance = 0.0;
		int pixelsCounted = 0;
		
		for(int i = 0; i < img.getWidth(); i++) {
			for(int j = 0; j < img.getHeight(); j++) {
				double luminance = luminance(new Color(img.getRGB(i, j)));
				pixelsCounted++;
				
				averageLuminance = (1 - 1.0 / pixelsCounted) * averageLuminance + (1.0 / pixelsCounted) * luminance;
			}
		}
		
		return averageLuminance;
	}
	
	private static BufferedImage correctImageLuminance(BufferedImage img, double targetLuminance) {
		BufferedImage correctedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D gr = correctedImg.createGraphics();
		
		double originalLuminance = imageLuminance(img);
		
		for(int i = 0; i < img.getWidth(); i++) {
			for(int j = 0; j < img.getHeight(); j++) {
				Color c = new Color(img.getRGB(i, j));
				HSLColor hslC = HSLColor.fromRgb(c);
				
				double luminanceRatio = targetLuminance / originalLuminance;
				
				hslC.setLuminance(hslC.getLuminance() * luminanceRatio);
				
				gr.setColor(hslC.toRgb());
				gr.fillRect(i, j, 1, 1);
			}
		}
		
		return correctedImg;
	}
	
	@SuppressWarnings("unused")
	private static BufferedImage correctImageLuminanceLegacy(BufferedImage img, double targetLuminance) {
		BufferedImage correctedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D gr = correctedImg.createGraphics();
		
		double originalLuminance = imageLuminanceLegacy(img);
		
		System.out.println(targetLuminance + "/" + originalLuminance);
		
		for(int i = 0; i < img.getWidth(); i++) {
			for(int j = 0; j < img.getHeight(); j++) {
				Color c = new Color(img.getRGB(i, j));
				
				double r = c.getRed();
				double g = c.getGreen();
				double b = c.getBlue();

				// trying squaring
				r *= (targetLuminance / originalLuminance) * (targetLuminance / originalLuminance);
				g *= (targetLuminance / originalLuminance) * (targetLuminance / originalLuminance);
				b *= (targetLuminance / originalLuminance) * (targetLuminance / originalLuminance);
				
				r = (r > 255 ? 255 : r);
				g = (g > 255 ? 255 : g);
				b = (b > 255 ? 255 : b);

				double activation = midtoneActivationFunction((r + g + b)/3.0);
				double rr = activation * r + (1 - activation) * c.getRed();
				double gg = activation * g + (1 - activation) * c.getGreen();
				double bb = activation * b + (1 - activation) * c.getBlue();
				
				gr.setColor(new Color((int) rr, (int) gg, (int) bb));
				gr.fillRect(i, j, 1, 1);
			}
		}
		
		return correctedImg;
	}
	
	// custom defined luminance formula
	private static double luminance(Color c) {
		return luminance(c.getRed(), c.getGreen(), c.getBlue());
	}
	
	// custom defined luminance formula
	private static double luminance(int r, int g, int b) {
		double luminance = (r + g + b) / 3.0;
		
		return luminance;
	}

    private static List<File> allFilesInDirectory(File dir) {
    	List<File> allFiles = new ArrayList<>();
    	
        File[] files = dir.listFiles();

        if (files == null) return allFiles;

        for (File file : files) {
            if (file.isDirectory()) {
            	List<File> subdirFiles = allFilesInDirectory(file);
            	
            	allFiles.addAll(subdirFiles);
            } else {
            	if(file.getAbsolutePath().endsWith(".jpg") || file.getAbsolutePath().endsWith(".png"))
                allFiles.add(file);
            }
        }
        
        return allFiles;
    }

	private static double midtoneActivationFunction(double luminance) {
		double theta = luminance / 255.0 * Math.PI;

		double activation = Math.sin(theta);
		return activation;
	}
}
