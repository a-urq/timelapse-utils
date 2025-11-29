package com.ameliaWx.timelapseUtils;

import java.awt.Color;

public class HSLColor {
	private double hue;
	private double saturation;
	private double luminance;
	
	public HSLColor(double hue, double saturation, double luminance) {
		this.hue = hue;
		this.saturation = saturation;
		this.luminance = luminance;
	}
	
	public double getHue() {
		return hue;
	}

	public void setHue(double hue) {
		this.hue = (hue + 720) % 360;
	}

	public double getSaturation() {
		return saturation;
	}

	public void setSaturation(double saturation) {
		if(saturation > 1) {
			saturation = 1;
		}
		
		if(saturation < 0) {
			saturation = 0;
		}
		
		this.saturation = saturation;
	}

	public double getLuminance() {
		return luminance;
	}

	public void setLuminance(double luminance) {
		if(luminance > 1) {
			luminance = 1;
		}
		
		if(luminance < 0) {
			luminance = 0;
		}
		
		this.luminance = luminance;
	}

	public static HSLColor fromRgb(Color c) {
		double r = c.getRed() / 255.0;
		double g = c.getGreen() / 255.0;
		double b = c.getBlue() / 255.0;
		
		double min = Double.min(r, Double.min(g, b));
		double max = Double.max(r, Double.max(g, b));
		
		double luminance = (max + min) / 2;
		
		double saturation = 0;
		
		if(min != max) {
			if(luminance <= 0.5) {
				saturation = (max - min)/(max + min);
			} else {
				saturation = (max - min)/(2.0 - max - min);
			}
		}
		
		double hue = 0;
		if(r == g && r == b) {
			hue = 0;
		} else if(r == max) {
			hue = (g - b)/(max - min);
		} else if (g == max) {
			hue = 2.0 + (b - r)/(max - min);
		} else if (b == max) {
			hue = 4.0 + (r - g)/(max - min);
		}
		
		hue *= 60;
		
		// constrains to [0, 360)
		hue = (hue + 720) % 360;
				
		return new HSLColor(hue, saturation, luminance);
	}

	public Color toRgb() {
		double temporary1 = 0.0;
		double temporary2 = 0.0;
		
		if(luminance < 0.5) {
			temporary1 = luminance * (1.0 + saturation);
		} else {
			temporary1 = luminance + saturation - luminance * saturation;
		}
		
		temporary2 = 2 * luminance - temporary1;
		
		double tempHue = hue/360;
		
		double temporaryR = tempHue + 0.333;
		double temporaryG = tempHue;
		double temporaryB = tempHue - 0.333;
		
		double red = 0;
		double green = 0;
		double blue = 0;
		
		if(6 * temporaryR < 1) {
			red = temporary2 + (temporary1 - temporary2) * 6 * temporaryR;
		} else if(2 * temporaryR < 1) {
			red = temporary1;
		} else if(3 * temporaryR < 2) {
			red = temporary2 + (temporary1 - temporary2) * (0.666 - temporaryR) * 6;
		} else {
			red = temporary2;
		}
		
		if(6 * temporaryG < 1) {
			green = temporary2 + (temporary1 - temporary2) * 6 * temporaryG;
		} else if(2 * temporaryG < 1) {
			green = temporary1;
		} else if(3 * temporaryG < 2) {
			green = temporary2 + (temporary1 - temporary2) * (0.666 - temporaryG) * 6;
		} else {
			green = temporary2;
		}
		
		if(6 * temporaryB < 1) {
			blue = temporary2 + (temporary1 - temporary2) * 6 * temporaryB;
		} else if(2 * temporaryB < 1) {
			blue = temporary1;
		} else if(3 * temporaryB < 2) {
			blue = temporary2 + (temporary1 - temporary2) * (0.666 - temporaryB) * 6;
		} else {
			blue = temporary2;
		}
		
		red = Double.max(0, red);
		green = Double.max(0, green);
		blue = Double.max(0, blue);
		
//		System.out.printf("HSL <%3.0f, %4.2f, %4.2f> | RGB <%3.0f, %3.0f, %3.0f>\n", hue, saturation, luminance, 255 * red, 255 * green, 255 * blue);
		
		return new Color((int) (255 * red), (int) (255 * green), (int) (255 * blue));
	}
}
