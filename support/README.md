>
> NOTICE
>
> This software (or technical data) was produced for the U. S. Government and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV (May 2014) - Alternative IV (Dec 2007).
>
> (c) 2024 The MITRE Corporation. All Rights Reserved.
>

# Support Module

The Support module is an Iris Analysis Toolkit (IAT) Java library used by the Web component to create default EBTS transactions. It also provides an SRB transaction creator for generating sample transaction files.

This module is not a standalone service and does not require Docker or ActiveMQ Artemis to build.

## Building the Module
### Prerequisites
- Java 17.
- Maven.
- The IAT JET dependency installed in the local Maven repository. From the main `iat/` directory, build it with:
  ```sh
  cd jet/Jet
  mvn install
  cd ../..
  ```

### Build JAR
From the main `iat/` directory, run:
```sh
mvn -pl support -am package
```

The built JAR is written to `support/target/`.

## Using the Module

The Web component uses `EbtsTransactionCreator` to generate a default EBTS transaction when an EBTS message is requested without custom transaction data. `SrbTransactionCreator` can generate a sample SRB transaction.

The default transaction values are configured in:

```text
src/main/resources/TransactionCreatorEbts.properties
src/main/resources/TransactionCreatorSrb.properties
```

## Change the Default Iris Image

The transaction-creator properties require a default iris image. A sample
image is provided at `src/main/resources/Image.png`. To use an image of your
choice, use one of the following options:

1. Replace `src/main/resources/Image.png`, keeping the filename and location
   unchanged.
2. Add the image under `src/main/resources/` and update the image property in
   the transaction-creator properties file.

   - `TransactionCreatorEbts.properties` uses `17.999`.
   - `TransactionCreatorSrb.properties` uses `17.999_1`, `17.999_2`, and
     `17.999_3`.

Rebuild the module after changing an image or transaction property.
