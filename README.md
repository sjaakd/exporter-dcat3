# DCAT3 Exporter for Dataverse

## Overview
This is a DCAT3 metadata exporter for [Dataverse](https://dataverse.org/). It implements the [DCAT3 specification](https://www.w3.org/TR/vocab-dcat-3/) as developed by the [DCAT3 Task Force Minutes](TODO) under the [MLCommons](https://mlcommons.org/en/) organization.

Currently, the exporter is in a prototype stage and build upon DCAT 3.0-AP-NL (the Dutch profile of DCAT3). A tab-separed-file is included to reflect the mapping that the Geological Survey of the Netherlands (TNO) used to map Dataverse metadata to DCAT3. This
work is not finalized. Checkout the config directory. 

It is all very much work in progress. The idea is to make the exporter fully configurable by means of a micro profile in which you can specify a mapping from the ExportData object to DCAT3 fields and also specify the kind of DCAT to produce. Apache Jena is used to create the RDF output. Shading was an issue. 

Ideas are sketched here: [Ideas](IDEAS.md)

## Installation

Please note: the DCAT3 exporter works best with Dataverse 6.2 and higher (where it updates the content of `<head>` as described in the [guides](https://preview.guides.gdcc.io/en/develop/admin/discoverability.html#schema-org-json-ld-DCAT3-metadata)) but can be used with 6.0 and higher (to get the export functionality).

1. First, download the DCAT3 jar file from Maven Central. Most likely, you will want the latest released version, which you can find at <https://repo1.maven.org/maven2/io/gdcc/export/dcat3/>. (Unreleased/snapshot versions are [available](https://s01.oss.sonatype.org/content/groups/staging/io/gdcc/export/dcat/) but not recommended for production use.)

1. Place the jar in the directory you specified for the [dataverse.spi.exporters.directory](https://guides.dataverse.org/en/latest/installation/config.html#dataverse-spi-exporters-directory) setting.

1. Restart Payara.

1. On published datasets, click "Metadata" and then "Export Metadata". You should see a "DCAT3" option in the drop down.

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

## Known issues

### Hopefully not TODO.

[versioning section]: https://mlcommons.github.io/DCAT3/docs/DCAT3-spec.html#dataset-versioningcheckpoints

For the reasons above, we are providing MAJOR.MINOR (e.g. "1.0") as the version.

We are using a string type for the "version" field in case we ever want to change Dataverse in the future to support MAJOR.MINOR.PATCH versions.

## Open questions

TODO

### The DCAT3 spec and task force

TODO

[spec]: https://www.w3.org/TR/vocab-dcat-3/
[DCAT3 Task Force Minutes]: TODO

### To run tests

```
mvn test
```

### To validate

Note: We are aware of warnings for the "version" field. See above.

```
./validate.sh
```

TODO: investigate? Is there a scheme

### To update expected JSON

As a convenience, you can run `mvn test` and then `update-expected.sh` after making code changes to update the expected DCAT3 ouput in the tests with new output.

### To format code

```
mvn spotless::apply
```

## To check the pom.xml file

```
mvn pomchecker:check-maven-central
```

### To build the DCAT3 jar

```
mvn package
```

### To use the DCAT3 jar in Docker

Under "environment" in the compose file, add the following.

```
DATAVERSE_SPI_EXPORTERS_DIRECTORY: "/dv/exporters"
```

Then create an `exporters` directory and copy the jar into it (dev example shown).

```
mkdir docker-dev-volumes/app/data/exporters
cp target/*jar docker-dev-volumes/app/data/exporters
```

Then stop and start the containers. On a dataset, click "Metadata" then "Export Metadata" and in the dropdown you should see "DCAT3" listed.

If you update the jar but not the dataset and want to see the changes, you can reexport all datasets or a specific dataset per https://guides.dataverse.org/en/6.2/admin/metadataexport.html#batch-exports-through-the-api

```
curl http://localhost:8080/api/admin/metadata/reExportAll
curl http://localhost:8080/api/admin/metadata/:persistentId/reExportDataset?persistentId=doi:10.5072/FK2/DZRHUP
```

### To use the DCAT3 jar in non-Docker

Same as above but use a JVM option in domain.xml such as the example below.

```
<jvm-options>-Ddataverse.spi.exporters.directory=/home/dataverse/dataverse-exporters/dcat-3/target</jvm-options>
```

### Changes under consideration

TODO
