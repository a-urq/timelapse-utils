package com.ameliaWx.timelapseUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class CenterFramesOnMoon {
	private static SortedMap<Integer, Point> offsetKeyframes = new TreeMap<>();
	
	static {
		offsetKeyframes.put(0, 		new Point(4, 0));
		offsetKeyframes.put(100, 	new Point(2, -3));
		offsetKeyframes.put(150, 	new Point(-5, 9));
		offsetKeyframes.put(200, 	new Point(-15, 16));
		offsetKeyframes.put(250, 	new Point(-38, 48));
		offsetKeyframes.put(300, 	new Point(-53, 74));
		offsetKeyframes.put(350, 	new Point(-86, 130));
		offsetKeyframes.put(400, 	new Point(-93, 180));
		offsetKeyframes.put(450, 	new Point(-93, 247));
		offsetKeyframes.put(493, 	new Point(-69, 290));
		offsetKeyframes.put(494, 	new Point(-74, 271));
		offsetKeyframes.put(500, 	new Point(-73, 273));
		offsetKeyframes.put(503, 	new Point(-72, 260));
		offsetKeyframes.put(504, 	new Point(-79, 236));
		offsetKeyframes.put(510, 	new Point(-71, 233));
		offsetKeyframes.put(511, 	new Point(-69, 202));
		offsetKeyframes.put(514, 	new Point(-64, 201));
		offsetKeyframes.put(515, 	new Point(-46, 151));
		offsetKeyframes.put(550, 	new Point(-13, 117));
		offsetKeyframes.put(600, 	new Point(20, 135));
		offsetKeyframes.put(650, 	new Point(63, 140));
		offsetKeyframes.put(700, 	new Point(115, 126));
		offsetKeyframes.put(750, 	new Point(136, 94));
		offsetKeyframes.put(800, 	new Point(123, 48));
		offsetKeyframes.put(850, 	new Point(123, 17));
		offsetKeyframes.put(878, 	new Point(129, 14));
		offsetKeyframes.put(879, 	new Point(145, 16));
		offsetKeyframes.put(887, 	new Point(149, 14));
		offsetKeyframes.put(888, 	new Point(184, 6));
		offsetKeyframes.put(890, 	new Point(188, 4));
		offsetKeyframes.put(891, 	new Point(249, 14));
		offsetKeyframes.put(896, 	new Point(264, 22));
		offsetKeyframes.put(897, 	new Point(281, 29));
		offsetKeyframes.put(900, 	new Point(288, 27));
		offsetKeyframes.put(950, 	new Point(237, -8));
		offsetKeyframes.put(1000, 	new Point(180, -25));
		offsetKeyframes.put(1050, 	new Point(122, -27));
		offsetKeyframes.put(1100, 	new Point(74, -22));
		offsetKeyframes.put(1150, 	new Point(36, -15));
		offsetKeyframes.put(1200, 	new Point(13, -7));
		offsetKeyframes.put(1250, 	new Point(7, -5));
		offsetKeyframes.put(1300, 	new Point(5, 0));
		offsetKeyframes.put(1350, 	new Point(2, -1));
		offsetKeyframes.put(1400, 	new Point(2, -1));
		offsetKeyframes.put(1450, 	new Point(2, -1));
		offsetKeyframes.put(1452, 	new Point(2, -1));
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(linInterp(offsetKeyframes, 370));
		
//		BufferedImage crop = centerAndCrop(new File("/media/nvme1/RAW File Temp Storage/Chase 2024-09-17 Partial Lunar Eclipse/darktable_exported_copyright/IMG_0299.jpg"), 1618, 1080, 32);
//		
//		ImageIO.write(crop, "PNG", new File("/media/nvme1/RAW File Temp Storage/Chase 2024-09-17 Partial Lunar Eclipse/darktable_exported_copyright/IMG_0299_crop.jpg"));
		
		centerFrames(new File("/media/nvme1/Astrophotography/2025-03-13 [Goessel KS] Total Lunar Eclipse/Full Timelapse/darktable_exported/trimmed"));
	}
	
	public static void centerFrames(File directory) throws IOException {
		List<File> allFrames = allFilesInDirectory(directory);
		
		allFrames.sort(null);
		
		for(int i = 0; i < allFrames.size(); i++) {
			File frame = allFrames.get(i);
			
			if(!frame.getAbsolutePath().endsWith(".jpg") || frame.getAbsolutePath().contains("centered")) {
				continue;
			}
			
			System.out.println(frame.getAbsolutePath());
			
//			BufferedImage img = centerAndCrop(frame, 1618, 1080, 100);
			BufferedImage img = centerAndCrop(frame, 3236, 2160, 100, i);
			
			String filepath = frame.getAbsolutePath();
			
			String[] tokens = filepath.split("/");
			String filename = tokens[tokens.length - 1];
			
			String path = filepath.substring(0, filepath.length() - filename.length());
			
			if(!new File(path + "centered/").exists()) {
				new File(path + "centered/").mkdirs();
			}
			
			String croppedFilepath = path + "centered/" + tokens[tokens.length - 1];
			
			File croppedFrame = new File(croppedFilepath);
			
			ImageIO.write(img, "JPEG", croppedFrame);
		}
	}

	@SuppressWarnings("unused")
	private static float gammaCorrect(float val, float gamma) {
		float gammaCorr = (float) Math.pow(val, 1 / gamma);

		return gammaCorr;
	}

	private static int gammaCorrect(int val, float gamma) {
		float gammaCorr = 255 * (float) Math.pow(val/255.0, 1 / gamma);

		return (int) gammaCorr;
	}
	
	private static BufferedImage centerAndCrop(File f, int width, int height, int truncationThreshold, int frameNumber) throws IOException {
		// load up frame as BufferedImage
		BufferedImage frame = ImageIO.read(f);
		
		int sumX = 0;
		int sumY = 0;
		int sumWeights = 0;
		
		// find truncated brightness centroid
		for(int i = 0; i < frame.getWidth(); i++) {
			for(int j = 0; j < frame.getHeight(); j++) {
				Color pixelColor = new Color(frame.getRGB(i, j));
				
				int r = pixelColor.getRed();
				int g = pixelColor.getGreen();
				int b = pixelColor.getBlue();
				
				int brightness = r + g + b;
				
				boolean useInCentroid = false;
				if (brightness > truncationThreshold * 3) {
					brightness = truncationThreshold * 3;
					useInCentroid = true;
				}
				
				if(useInCentroid) {
					sumX += i;
					sumY += j;
					sumWeights++;
				}
			}
		}
		
		int avgX = sumX/sumWeights;
		int avgY = sumY/sumWeights;
		
		BufferedImage croppedFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = croppedFrame.createGraphics();
		
		Point offset = linInterp(offsetKeyframes, frameNumber);
		
		g.drawImage(frame, (int) -avgX + width/2 - offset.x, (int) -avgY + height/2 - offset.y, null);
		
		return croppedFrame;
	}
	
	private static Point linInterp(SortedMap<Integer, Point> keyframes, int frame) {
		Object[] keys = keyframes.keySet().toArray();
		
		int keyLow = -1;
		int keyHigh = -1;
		int xLow = -1;
		int xHigh = -1;
		int yLow = -1;
		int yHigh = -1;
		
		for(int i = 0; i < keys.length; i++) {
			if((int) keys[i] == frame) {
				return keyframes.get(keys[i]);
			} else if((int) keys[i] > frame) {
				keyLow = (int) keys[i - 1];
				keyHigh = (int) keys[i];
				
				Point pointLow = keyframes.get(keys[i - 1]);
				Point pointHigh = keyframes.get(keys[i]);
				
				xLow = pointLow.x;
				xHigh = pointHigh.x;
				yLow = pointLow.y;
				yHigh = pointHigh.y;
				
				break;
			}
		}
		
		double weightLow = (double) (keyHigh - frame)/(keyHigh - keyLow);
		double weightHigh = (double) (frame - keyLow)/(keyHigh - keyLow);
		
		double x = weightLow * xLow + weightHigh * xHigh;
		double y = weightLow * yLow + weightHigh * yHigh;
		
		return new Point((int) x, (int) y);
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
                allFiles.add(file);
            }
        }
        
        return allFiles;
    }
}
