package com.ameliaWx.timelapseUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class DespeckleAstrophoto {
	public static void main(String[] args) throws IOException {
		BufferedImage in = ImageIO.read(new File("/media/nvme1/Astrophotography/Long Term Winter 2025 Projects/Triangulum Galaxy (M33)/Multi-Night Composite Directory/nights_1_4_fits/darktable_exported/result_22530s_final_gimpedit.jpg"));
		
		BufferedImage out = despeckle(in);
		
		ImageIO.write(out, "JPG", new File("/media/nvme1/Astrophotography/Long Term Winter 2025 Projects/Triangulum Galaxy (M33)/Multi-Night Composite Directory/nights_1_4_fits/darktable_exported/result_22530s_final_gimpedit-despeckle.jpg"));
	}

	private static BufferedImage despeckle(BufferedImage img) {
		Graphics2D g = img.createGraphics();

		for (int i = 0; i < img.getWidth(); i++) {
			System.out.println("i: " + i);
			for (int j = 0; j < img.getHeight(); j++) {
				int iNeg = i - 1;
				int jNeg = j - 1;
				int iPos = i + 1;
				int jPos = j + 1;

				if (iNeg < 0)
					iNeg = 0;
				if (jNeg < 0)
					jNeg = 0;
				if (iPos >= img.getWidth())
					iPos = img.getWidth() - 1;
				if (jPos >= img.getHeight())
					jPos = img.getHeight() - 1;

				Color colorNN = new Color(img.getRGB(iNeg, jNeg));
				Color colorMN = new Color(img.getRGB(i, jNeg));
				Color colorPN = new Color(img.getRGB(iPos, jNeg));
				Color colorNM = new Color(img.getRGB(iNeg, j));
				Color colorPM = new Color(img.getRGB(iPos, j));
				Color colorNP = new Color(img.getRGB(iNeg, jPos));
				Color colorMP = new Color(img.getRGB(i, jPos));
				Color colorPP = new Color(img.getRGB(iPos, jPos));

				g.setColor(Color.BLACK);
				if (colorNN == Color.BLACK && colorMN == Color.BLACK && colorPN == Color.BLACK && colorNM == Color.BLACK
						&& colorPM == Color.BLACK && colorNP == Color.BLACK && colorMP == Color.BLACK
						&& colorPP == Color.BLACK) {
					g.fillRect(i, j, 1, 1);
				}
			}
		}
		
		g.dispose();
		
		return img;
	}
}
