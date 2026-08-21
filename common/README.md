>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>

# Common Module

The Common module is an Iris Analysis Toolkit (IAT) Java library shared by the Web component. It provides image-processing utilities, EBTS date utilities, and shared IAT constants. 
This module is not a standalone service and does not require Docker or ActiveMQ Artemis to build or test.

## Building the Module
### Prerequisites
- Java 17.
- Maven.

### Build JAR
From the main `iat/` directory, run:
```sh
mvn -pl common -am package
```

The built JAR is written to `common/target/`.

## Using the Module
The Web component depends on this module for image handling and EBTS support. When building the Web component with Maven's `-am` option, Maven builds Common automatically.

`ImageUtils` reads raw grayscale and RGB images as well as BMP, JPEG, JPEG2000, PNG, and TIFF images. `IwpEbtsUtils` formats and parses EBTS dates, and `IwpConstants` provides shared EBTS constants.

## Run Java Unit Tests
The module includes unit tests for `ImageUtils` using sample image files in `src/test/resources/file-types/`.
From the main `iat/` directory, run:
```sh
mvn -pl common -am test
```
