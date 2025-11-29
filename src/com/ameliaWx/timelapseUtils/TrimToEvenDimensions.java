package com.ameliaWx.timelapseUtils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class TrimToEvenDimensions {
	public static void main(String[] args) throws IOException {
		List<File> allFiles = allFilesInDirectory(new File("/media/nvme1/Astrophotography/2025-03-13 [Goessel KS] Total Lunar Eclipse/Full Timelapse/darktable_exported"));
		
		allFiles.sort(null);
		
		for(int i = 0; i < allFiles.size(); i++) {
			System.out.println(allFiles.get(i));
		}

		for(int i = 1; i < allFiles.size(); i++) {
			File frame = allFiles.get(i);

			if(!frame.getAbsolutePath().endsWith(".jpg") || frame.getAbsolutePath().contains("trimmed")) {
				continue;
			}
			
			String filepath = frame.getAbsolutePath();
			
			String[] tokens = filepath.split("/");
			String filename = tokens[tokens.length - 1];
			
			String path = filepath.substring(0, filepath.length() - filename.length());
			
			String croppedFilepath = path + "trimmed/" + tokens[tokens.length - 1];
			
			if(!new File(path + "trimmed/").exists()) {
				new File(path + "trimmed/").mkdirs();
			}
			
			BufferedImage correctedImg = trim(ImageIO.read(frame));
			
			File correctedFrame = new File(croppedFilepath);
			
			System.out.println(croppedFilepath);
			
			ImageIO.write(correctedImg, "JPEG", correctedFrame);
		}
	}
	
	private static BufferedImage trim(BufferedImage img) {
		BufferedImage correctedImg = new BufferedImage(img.getWidth() - (img.getWidth() % 2), img.getHeight() - (img.getHeight() % 2), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D gr = correctedImg.createGraphics();
		
		gr.drawImage(img, 0, 0, null);
		
		return correctedImg;
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
