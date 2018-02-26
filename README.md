Android client takes the photo in JPEG format,encodes the photo with Base64 and sends encoded string to server. Server decodes received
string and activates process with Yolo Darknet exe file which analyzes decoded photo. Results are then sent back to client which draws
resulting rectangles around detected categories over the photo. 
