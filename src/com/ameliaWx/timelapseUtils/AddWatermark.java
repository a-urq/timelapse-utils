package com.ameliaWx.timelapseUtils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class AddWatermark {
	public static void main(String[] args) throws IOException {
		addWatermark(new File("/media/nvme1/RAW File Temp Storage/Chase 2025-11-11 Cheney State Park KS Auroras/darktable_exported_D_substorm_with_tree/l-deflickered"),
				new File("/media/nvme1/Video Recordings/cheney sp aurora watermark.png"));
	}
	
	private static void addWatermark(File directory, File watermark) throws IOException {
		List<File> frames = allFilesInDirectory(directory);
		
		System.out.println(frames.size());
		
		for(File frame : frames) {
			System.out.print(frame.getAbsolutePath() + " - ");
			
			if(!frame.getAbsolutePath().endsWith(".jpg") || frame.getAbsolutePath().contains("marked")) {
				System.out.println("skipping");
				continue;
			} else {
				System.out.println("marking");
			}
			
			BufferedImage marked = addWatermarkToFrame(frame, watermark, 0, 0);
			
			String filepath = frame.getAbsolutePath();
			
			String[] tokens = filepath.split("/");
			String filename = tokens[tokens.length - 1];
			
			String path = filepath.substring(0, filepath.length() - filename.length());
			
			String markedFilepath = path + "marked/";
			new File(markedFilepath).mkdirs();
			
			File markedFrame = new File(markedFilepath + tokens[tokens.length - 1]);
			
			ImageIO.write(marked, "JPEG", markedFrame);
		}
	}
	
	@SuppressWarnings("unused")
	private static BufferedImage addWatermarkToFrame(File frame, File watermark) throws IOException {
		return addWatermarkToFrame(frame, watermark, 0, 0);
	}
	
	private static BufferedImage addWatermarkToFrame(File frame, File watermark, int x, int y) throws IOException {
		BufferedImage frameImg = ImageIO.read(frame);
		BufferedImage watermarkImg = ImageIO.read(watermark);
		
		Graphics2D g = frameImg.createGraphics();
		g.drawImage(watermarkImg, x, y, null);
		
		return frameImg;
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
