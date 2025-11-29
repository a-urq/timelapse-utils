package com.ameliaWx.timelapseUtils;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class AddAstroWatermark {
	private static SortedMap<DateTime, String> phases = new TreeMap<>();
	
	// start times of each phase
	static {
		phases.put(new DateTime(2025, 3, 14, 0, 0, DateTimeZone.UTC), "No Eclipse");
		phases.put(new DateTime(2025, 3, 14, 3, 57, DateTimeZone.UTC), "Penumbral Phase");
		phases.put(new DateTime(2025, 3, 14, 5, 9, DateTimeZone.UTC), "Partial Phase");
		phases.put(new DateTime(2025, 3, 14, 6, 26, DateTimeZone.UTC), "Total Phase");
		phases.put(new DateTime(2025, 3, 14, 7, 31, DateTimeZone.UTC), "Partial Phase");
		phases.put(new DateTime(2025, 3, 14, 8, 47, DateTimeZone.UTC), "Penumbral Phase");
		phases.put(new DateTime(2025, 3, 14, 10, 0, DateTimeZone.UTC), "No Eclipse");
	}
	
	private static final String imageDirectory = "/media/nvme1/Astrophotography/2025-03-13 [Goessel KS] Total Lunar Eclipse/Full Timelapse/darktable_exported/trimmed/centered/l-deflickered";
	private static final String exifDirectory = "/media/nvme1/Astrophotography/2025-03-13 [Goessel KS] Total Lunar Eclipse/Full Timelapse";

	private static final DateTimeZone CENTRAL_TIME = DateTimeZone.forID("America/Chicago");
	private static final String TZ_CODE = "CST";
	
	public static void main(String[] args) {
		try {
			timestampFiles(imageDirectory, exifDirectory, 11155, 11195, 60 * 60);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void timestampFiles(String imageDirectory, String exifDirectory, int firstFileId, int lastFileId, int manualOffsetSeconds) throws IOException {
		List<File> jpgList = allFilesInDirectory(new File(imageDirectory));
		List<File> xmpList = allFilesInDirectory(new File(exifDirectory));

		jpgList.sort(null);
		xmpList.sort(null);
		
		HashMap<Integer, File> jpgFiles = new HashMap<>();
		HashMap<Integer, File> xmpFiles = new HashMap<>();
		
		for(int id = firstFileId; id <= lastFileId; id++) {
			File jpgFileAtId = findFileInDirectory(jpgList, id, ".jpg");
			File xmpFileAtId = findFileInDirectory(xmpList, id, ".CR3.xmp");
			
			System.out.println("jpgFileAtId: " + jpgFileAtId);
//			System.exit(0);
			
			if(jpgFileAtId != null && xmpFileAtId != null) {
				jpgFiles.put(id, jpgFileAtId);
				xmpFiles.put(id, xmpFileAtId);
			}
		}

		System.out.println("jpgList.size(): " + jpgList.size());
		System.out.println("jpgFiles.size(): " + jpgFiles.size());
		
		HashMap<Integer, DateTime> imgTimes = new HashMap<>();
		
		for(int id = firstFileId; id <= lastFileId; id++) {
			boolean containsKey = xmpFiles.containsKey(id);
			
			if(!containsKey) {
				continue;
			}
			
			File xmpFile = xmpFiles.get(id);
			
			try {
				DateTime imgTime = loadTimestamp(xmpFile).plusSeconds(manualOffsetSeconds);
				
				System.out.println(dateString(imgTime, CENTRAL_TIME, TZ_CODE));
				
				imgTimes.put(id, imgTime);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				continue;
			} catch (TimestampNotFoundException e) {
				continue;
			}
		}
		
		System.out.println("imgTimes.size(): " + imgTimes.size());
		
		for(int id = firstFileId; id <= lastFileId; id++) {
			if(imgTimes.containsKey(id)) {
				String filepath = jpgFiles.get(id).getAbsolutePath();
				
				String[] tokens = filepath.split("/");
				String filename = tokens[tokens.length - 1];
				
				String path = filepath.substring(0, filepath.length() - filename.length());
				
				if(!new File(path + "watermarked/").exists()) {
					new File(path + "watermarked/").mkdirs();
				}
				
				BufferedImage watermarked = watermarkImage(jpgFiles.get(id), imgTimes.get(id));
				
				String watermarkedFilepath = path + "watermarked/" + tokens[tokens.length - 1];
				
				File watermarkedFrame = new File(watermarkedFilepath);
				
				ImageIO.write(watermarked, "JPEG", watermarkedFrame);
			}
		}
	}
	
	public static final Font WATERMARK_FONT = new Font("VCR OSD Mono", Font.ITALIC, 75);

	private static BufferedImage watermarkImage(File jpg, DateTime timestamp) {
		BufferedImage jpgImg = null;
		try {
			jpgImg = ImageIO.read(jpg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BufferedImage annotatedFrame = new BufferedImage(jpgImg.getWidth(), jpgImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = annotatedFrame.createGraphics();
		g.setFont(WATERMARK_FONT);
		
		g.drawImage(jpgImg, null, 0, 0);
		
		drawRightAlignedString("© Amelia R H Urquhart", g, jpgImg.getWidth() - 20, jpgImg.getHeight() - 260);
		drawRightAlignedString(dateString(timestamp, CENTRAL_TIME, TZ_CODE), g, jpgImg.getWidth() - 20, jpgImg.getHeight() - 160);
		drawRightAlignedString(getPhase(timestamp), g, jpgImg.getWidth() - 20, jpgImg.getHeight() - 60);
		
		g.dispose();
		
		return annotatedFrame;
	}
	
	private static String getPhase(DateTime dt) {
		Object[] phaseKeysArr = phases.keySet().toArray();
//		System.out.println("phaseKeysArr.size()");
		
		ArrayList<DateTime> phaseKeys = new ArrayList<>();
		
		for(int i = 0; i < phaseKeysArr.length; i++) {
			phaseKeys.add((DateTime) phaseKeysArr[i]);
		}
		
		phaseKeys.sort(null);
		
		for(int i = 0; i < phases.size(); i++) {
			System.out.println(dt);
			System.out.println(dt.toDateTime(DateTimeZone.UTC));
			System.out.println(phaseKeys.get(i));
			System.out.println(dt.toDateTime(DateTimeZone.UTC).isAfter(phaseKeys.get(i)));
			if(dt.toDateTime(DateTimeZone.UTC).isBefore(phaseKeys.get(i))) {
				return phases.get(phaseKeys.get(i - 1));
			}
		}
		
		return "No Eclipse";
	}
	public static void drawRightAlignedString(String s, Graphics2D g, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		int ht = fm.getAscent() + fm.getDescent();
		int width = fm.stringWidth(s);
		g.drawString(s, x - width, y + (fm.getAscent() - ht / 2));
	}
	
	private static DateTime loadTimestamp(File cr2xmp) throws FileNotFoundException, TimestampNotFoundException {
		Scanner sc = new Scanner(cr2xmp);
		
		String timestampLine = "";
		
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			
			if(line.contains("exif:DateTimeOriginal")) {
				timestampLine = line;
			}
		}
		
		sc.close();
		
		String[] tokens = timestampLine.split("\\\"");
		
		String timestamp = tokens[1];
		
		int year = Integer.valueOf(timestamp.substring(0, 4));
		int month = Integer.valueOf(timestamp.substring(5, 7));
		int day = Integer.valueOf(timestamp.substring(8, 10));
		int hour = Integer.valueOf(timestamp.substring(11, 13));
		int minute = Integer.valueOf(timestamp.substring(14, 16));
		int second = Integer.valueOf(timestamp.substring(17, 19));
		
		DateTime dateTime = new DateTime(year, month, day, hour, minute, second, CENTRAL_TIME);
		
		return dateTime;
	}

	private static String dateString(DateTime d, DateTimeZone tz, String tzCode) {
		DateTime c = d.toDateTime(tz);

		String daylightCode = tzCode.substring(0, tzCode.length() - 2) + "DT";

		boolean isPm = c.getHourOfDay() >= 12;
		boolean is12 = c.getHourOfDay() == 0 || c.getHourOfDay() == 12;
		return String.format("%04d", c.getYear()) + "-" + String.format("%02d", c.getMonthOfYear()) + "-"
				+ String.format("%02d", c.getDayOfMonth()) + " "
				+ String.format("%02d", c.getHourOfDay() % 12 + (is12 ? 12 : 0)) + ":"
				+ String.format("%02d", c.getMinuteOfHour()) + ":" + String.format("%02d", c.getSecondOfMinute()) + " " + (isPm ? "PM" : "AM") + " "
				+ (TimeZone.getTimeZone(tz.getID()).inDaylightTime(d.toDate()) ? daylightCode : tzCode);
	}
	
	private static final boolean COPYRIGHT_SWITCH = false;
	// inefficient as all hell but should get the job done
	private static File findFileInDirectory(List<File> files, int id, String extension) {
		String possibleFilename1 = String.format("IMG_%04d" + extension, id);
		String possibleFilename2 = String.format("IMG_%05d" + extension, id);
		
		for(int i = 0; i < files.size(); i++) {
//			if(i == 100) System.exit(0);
				
			File f = files.get(i);
			
			String[] absolutePathTokens = f.getAbsolutePath().split("/");
			String filename = absolutePathTokens[absolutePathTokens.length - 1];
			
//			System.out.println("filename: " + filename);
//			System.out.println("possibleFilename1: " + possibleFilename1);
//			System.out.println("possibleFilename2: " + possibleFilename2);
			
			if(".jpg".equals(extension)) {
				boolean fileIsCopyrighted = f.getAbsolutePath().contains("copyright");
				boolean fileIsStill = f.getAbsolutePath().contains("still");
				
				if(fileIsCopyrighted != COPYRIGHT_SWITCH || fileIsStill) {
					continue;
				}
			}

//			System.out.println("possibleFilename1.equals(filename): " + possibleFilename1.equals(filename));
			if(possibleFilename1.equals(filename)) {
				System.out.println("returning " + f);
				return f;
			}

//			System.out.println("possibleFilename2.equals(filename): " + possibleFilename2.equals(filename));
			if(possibleFilename2.equals(filename)) {
				System.out.println("returning " + f);
				return f;
			}
		}
		return null;
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
