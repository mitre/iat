>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>

# Buffers Module

The Buffers module contains the Protocol Buffer message definitions shared by the Iris Analysis Toolkit (IAT) components. Maven generates Java and Python classes from the `.proto` files during the build.

This module is not a standalone service and does not require Docker or ActiveMQ Artemis to build.

## Building the Module
### Prerequisites
- Java 17.
- Maven.

### Build JAR
From the main `iat/` directory, run:
```sh
mvn -pl buffers -am package
```

The built JAR is written to `buffers/target/`. The generated Python modules are written to `buffers/target/python/buffers/`.

### Install JAR
To install the Buffers JAR in the local Maven repository for use outside this source tree, run:

```sh
mvn -pl buffers clean install
```

## Using the Module
The Web, BIQT, ACII, TSHEPII, PDM, and Inference components use the message schemas in `src/main/proto/`. When building a component with Maven's `-am` option, Maven builds Buffers automatically when it is a dependency.

Java consumers use the generated classes in the `org.mitre.iwp.buffers` package. Python consumers can use the generated modules from `buffers/target/python/buffers/` or generate the modules directly with `protoc` for their own project.

## Python Wheel
`src/main/python/setup.py` is an optional packaging helper for building a Python wheel from the generated protobuf modules. It is not required to build the Java JAR or generate the Python output.
