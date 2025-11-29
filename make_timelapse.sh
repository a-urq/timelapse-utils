ffmpeg -framerate $1 -pattern_type glob -i "IMG_*.jpg" -c:v libx264 -pix_fmt yuv420p $2
