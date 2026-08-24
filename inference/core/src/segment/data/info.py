#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government 
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2024 The MITRE Corporation. All Rights Reserved.
# 

"""
Segmentation dataset information.
"""


# Integer label to RGB Color code
label_to_color_map = {
    0: (0,0,0),         # Background
    # 1: (255,255,255),   # Pupil
    1: (255,0,0),   # Pupil
    # 2: (0,0,255),       # Iris
    2: (255,255,255),       # Iris
    3: (0,255,0),       # Sclera
    # 4: (255,0,0),       # Eyelash
    4: (0,0,255),       # Eyelash
    5: (0,255,255),      # Eyebrow
    6: (255,255,0),      # Highlight #6248 and below
    7: (255,0,255),      # Border
    8: (51,255,255),      # Crypt
    9: (255,153,51),     # Ridge
    10: (0,165,255)      # Contact
}



# RGB color code to integer label map
color_to_label_map = {}
for (k,v) in label_to_color_map.items():
    color_to_label_map[v] = k

# RGB Color (HEX) code
color_hex_map = {
    0: "#000000", 
    # 1: "#FFFFFF", 
    1: "#FF0000", 
    # 2: "#0000FF", 
    2: "#FFFFFF", 
    3: "#00FF00",
    # 4: "#FF0000",
    4: "#0000FF",
    5: "#00FFFF",
    6: "#FFFF00", #6248 and below
    7: "#FF00FF",
    8: "#33FFFF",
    9: "#FF9933",
    10: "#00A5FF"
}

# Gray is used by LG4000 sensor for regions out of bounds
background_color = 127
background_label = 0

# Number of segmentation classes
num_labels = len(list(label_to_color_map.keys()))

# Image dimensions
width = 640
height = 480