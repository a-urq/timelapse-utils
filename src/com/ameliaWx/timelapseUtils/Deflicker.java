package com.ameliaWx.timelapseUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class Deflicker {
	private static final String IN_FOLDER =
			"/media/nvme1/RAW File Temp Storage/Grand Western Road Trip 2025/2025-07-21 (Day 8)/darktable_exported_sunset_timelapse";

	private static int LOCK_IN_INDEX = 332;
	private static  double LOCK_IN_RATIO = -1;

	public static void main(String[] args) throws IOException {
		List<File> allFiles = allFilesInDirectory(new File(IN_FOLDER));
		
		allFiles.sort(null);
		
		for(int i = 0; i < allFiles.size(); i++) {
			System.out.println(allFiles.get(i));
		}
		
		double targetLuminance = imageLuminanceLegacy(ImageIO.read(allFiles.get(0)));

		for(int i = 1; i < allFiles.size(); i++) {
			File frame = allFiles.get(i);
			
			String filepath = frame.getAbsolutePath();
			
			String[] tokens = filepath.split("/");
			String filename = tokens[tokens.length - 1];
			
			String path = filepath.substring(0, filepath.length() - filename.length());
			
			String croppedFilepath = path + "deflickered/" + tokens[tokens.length - 1];
			
			if(!new File(path + "deflickered/").exists()) {
				new File(path + "deflickered/").mkdirs();
			}

			double originalLuminance = imageLuminanceLegacy(ImageIO.read(frame));

			if(i >= LOCK_IN_INDEX) {
				if(i == LOCK_IN_INDEX) {
					LOCK_IN_RATIO = targetLuminance/originalLuminance;
				}

				BufferedImage correctedImg = correctImageLuminanceLegacy(ImageIO.read(frame), LOCK_IN_RATIO * originalLuminance);

				File correctedFrame = new File(croppedFilepath);

				System.out.println(croppedFilepath);

				ImageIO.write(correctedImg, "JPEG", correctedFrame);
			} else {
				BufferedImage correctedImg = correctImageLuminanceLegacy(ImageIO.read(frame), targetLuminance);

				File correctedFrame = new File(croppedFilepath);

				System.out.println(croppedFilepath);

				ImageIO.write(correctedImg, "JPEG", correctedFrame);
			}
		}
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
				
				r *= (targetLuminance / originalLuminance);
				g *= (targetLuminance / originalLuminance);
				b *= (targetLuminance / originalLuminance);
				
				r = (r > 255 ? 255 : r);
				g = (g > 255 ? 255 : g);
				b = (b > 255 ? 255 : b);
				
				gr.setColor(new Color((int) r, (int) g, (int) b));
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
}
