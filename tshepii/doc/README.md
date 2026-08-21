# TSHEPII #

A Software Tool Supporting the Human Examination of Post-mortem Iris Images.

## Features ##

- Pairwise comparison of iris images, particularly tuned for post-mortem samples.
- Automatic iris segmentation provided by integration with the OSIRIS [1] software.
- Cross-platform rich GUI for:
  - Manual iris segmentation.
  - Free-form annotation of mathing and non-matching iris regions.
  - Basic iris image processing, such as zooming, sharpness, brightness, and contrast enhancements.
- Iris-comparison report generation.
- Examination saving and loading for work pause and resume.
- Eleven methods of pairwise iris comparison:
    1. OSIRIS [1] Gabor-filter-based comparison score.
    2. BSIF-inspired [2] comparison score.
    3. Crypt-based [3] comparison score.
    4. TSHEPII<sup id="a1">[1](#f1)</sup> matched-keypoint comparison score.
    5. TSHEPII<sup id="a1">[1](#f1)</sup> unmatched-keypoint comparison score.
    6. SURF [4] matched-keypoint comparison score.
    7. SURF [4] unmatched-keypoint comparison score.
    8. SIFT [5] matched-keypoint comparison score.
    9. SIFT [5] unmatched-keypoint comparison score.
    10. MSER [6] matched-region comparison score.
    11. MSER [6] unmatched-region comparison score.
- Global comparison score based on majority voting of the available eleven methods.

## Screenshots ##

![Windows 10 main screen](screens/main_win10.png?raw=true)
![macOS High Sierra main screen](screens/main_cocoa.png?raw=true)

## Source Code ##

TSHEPII source code is available on CVRL<sup id="a2">[2](#f2)</sup> [GitHub](https://github.com/CVRL/TSHEPII) (private repository).
It currently has the following branches:

Branch                                                                                    | Description
------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------
[2018-apr-deliverable](https://github.com/CVRL/TSHEPII/tree/2018-apr-deliverable)         | 2018 April deliverable. Complete version of the software, with all the features.
[2018-feb-deliverable](https://github.com/CVRL/TSHEPII/tree/2018-feb-deliverable)         | 2018 February deliverable. Deprecated. Software prototype, containing only the main features.
[keypoint-adding-tutorial](https://github.com/CVRL/TSHEPII/tree/keypoint-adding-tutorial) | Extended version of the software, as a result of the software extension tutorial.
[master](https://github.com/CVRL/TSHEPII/tree/master)                                     | Main development code. It can be updated without notifications.

## Compilation ##

### Required Software ###

- [OpenCV 2.4.13](https://github.com/opencv/opencv/releases/tag/2.4.13).
- MS Visual Studio 2015 (or superior)
- PROTOBUF_VERSION="3.5.1"
**OR**
[CMake](https://cmake.org/download/) 2.8 (or superior).
- In case of macOS High Sierra, [AppKit](https://developer.apple.com/documentation/appkit).
- In case of Ubuntu 16.04, [GTK+ 3](https://developer.gnome.org/gtk3/3.0/).

### MS Visual Studio ###

TSHEPII was developed with MS Visual Studio 2015 (MVS):

1. Check-out GitHub [master](https://github.com/CVRL/TSHEPII/tree/master) branch.

   ``` git
   git clone https://github.com/CVRL/TSHEPII/tree/master
   ```

2. Use [TSHEPII.sln](TSHEPII2.sln) file for correctly opening the MVS project.

![MVS TSHEPII Project](screens/mvs.png?raw=true)

### CMake-based Compilation ###

Alternatively, CMake can be used for compiling TSHEPII in a cross-platform way:

1. Check-out GitHub [master](https://github.com/CVRL/TSHEPII/tree/master) branch.

   ``` git
   git clone https://github.com/CVRL/TSHEPII/tree/master
   ```

2. Set the correct *file separator* character in [Configuration.cpp](TSHEPII2/Configuration.cpp).
   Search for the *FILE_SEPARATOR* variable and follow the commented instructions.

   ``` C
   /* General configuration. */
   const string Configuration::FILE_SEPARATOR = "\\"; // Unix-like systems: change to "/" 
   ```

3. Create *build* folder.

   ``` bash
   mkdir build; cd build
   ```

4. Run *cmake* and *make* commands

   ``` bash
   cmake ..
   make
   ```

5. Execute the application

   ``` bash
   ./TSHEPII2
   ```

## Documentation ##

TSHEPII documentation is automatically generated with [Doxygen](http://www.stack.nl/~dimitri/doxygen/).
It is available [here](docs/html/index.html).

Additionally, there is a tutorial for adding other keypoint-based iris-comparison methods.
It is available [here](tutorial.pdf).

## Third-party Libraries ##

- [OpenCV 2.4.13](https://github.com/opencv/opencv/releases/tag/2.4.13).
- [CVUI](https://github.com/Dovyski/cvui).
- [Native File Dialog](https://github.com/mlabbe/nativefiledialog).
- OSIRIS [1].

## About ##

Developed by:

- University of Notre Dame.
- West Virginia University.

Developed for:

- The FBI Biometric Center of Excellence.

Development Team:

- Dr. Adam Czajka, Lead-PI.
- Dr. Kevin Bowyer, Co-PI.
- Dr. Patrick Flynn, Co-PI.
- Dr. Daniel Moreira, Post-doc Researcher.
- Ms. Natasha Scritchfield, Project Manager.

Support Contact:

- Dr. [Adam Czajka](aczajka@nd.edu).
- Dr. [Daniel Moreira](dhenriq1@nd.edu).

## References ##

- [1] N. Othman, B. Dorizzi, and S. Garcia-Salicetti. OSIRIS: An open source iris recognition software. Elsevier Pattern Recognition Letters, 82(2):124–131, 2016.
- [2] J. Kannala and E. Rahtu. BSIF: Binarized statistical image features. In Proceedings of the Intl. Conference on Pattern Recognition (ICPR), pp. 1363–1366, 2012.
- [3] J. Chen, F. Shen, D. Chen, and P. Flynn. Iris Recognition Based on Human-Interpretable Features. IEEE Transactions on Information Forensics and Security, 11(7):1476–1485, 2016.
- [4] H. Bay, A. Ess, T. Tuytelaars, and L. Van Gool. Speeded-up robust features (SURF). Computer Vision and Image Understanding, 110(3):346–359, 2008.
- [5] D. Lowe. Distinctive image features from scale-invariant keypoints. Springer Intl. Journal of Computer Vision, 60(2):91–110, 2004.
- [6] J. Matas, O. Chum, M. Urban, and T. Pajdla. Robust Wide Baseline Stereo from Maximally Stable Extremal Regions. Image and Vision Computing, 22(10):761–767, 2004.

<b id="f1">1</b> Novel iris-texture detector and descriptor developed in the context of the TSHEPII project. [↩](#a1)  
<b id="f2">2</b> CVRL: The Computer Vision Research Lab at the University of Notre Dame. [↩](#a2)
