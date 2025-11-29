This repository contains a collection of standalone Java programs that each perform a specific function in my timelapse photography workflow.

### Dependencies
* Joda Time version 2.12.2

### Programs
* AddAstroWatermark - Adds the watermark visible [here](https://x.com/ameliaUrquhart_/status/1901366562688282797) as an overlay atop all existing images in the folder. Requires EXIF data.
* AddWatermark - Adds a premade static watermark as an overlay atop all existing images in the folder, as seen [here](https://x.com/ameliaUrquhart_/status/1989242118381199425). Does not require EXIF data.
* CenterFramesOnMoon - Crops and centers every image on the moon. Keyframed offsets can be optionally added in the case of lunar eclipses, where the brightness centroid moves around relative to the center of the moon's disk. Purpose-built to help create [this timelapse](https://x.com/ameliaUrquhart_/status/1901366562688282797).
* Deflicker - Finds the mean brightness value of all pixels in the first image and brightens or darkens all other images in the folder to be that same brightness. Helps control shutter/aperture error flicker and EV step flicker.
* Local Deflicker - Finds the mean brightness value of all pixels in the preceding 15 frames of the timelapse and brightens or darkens each image in the folder in order to create smooth, gentle changes in brightness. Helps control shutter/aperture error flicker and EV step flicker. Much more suitable than Deflicker for cases where the character of the scene changes drastically within the timelapse, such as storms approaching head-on or sunsets that go from yellow and blue, to orange and red, and then to gray and red. Sometimes referred to as L-Deflicker for short.
* TrimToEvenDimensions - ffmpeg really hates it when the input JPEGs have odd-numbered widths or heights. This program chops off the lowest row and/or rightmost column to make all dimensions even. This is 100% effective at making ffmpeg stop complaining and doesn't perceptibly impact any image I've used this with.
* make_timelapse.sh - The BASH script that stitches together all the JPEGs into a video file.
