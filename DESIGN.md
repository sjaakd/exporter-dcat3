
# DESIGN.md

## Overview
This repository contains a **DCAT‑3 exporter for Dataverse**. It produces RDF metadata conforming to **DCAT 3.0** and the Dutch application profile **DCAT‑AP‑NL 3.0**. The exporter is designed for extensibility and strict validation using **SHACL** shapes tailored to DCAT‑AP‑NL.

## Architectural Split
The system is consciously split into three layers that mirror the flow from Dataverse input to DCAT output:

1. **Configuration Parsing (Loaders)**
   - Purpose: Read and parse configuration files that *describe the mapping* from the Dataverse input structure to DCAT‑3 resources and properties.
   - Main components:
     - `RootConfigLoader` — loads the **root configuration**, including global prefixes, element lists, relation definitions, and base directory settings.
     - `ResourceConfigLoader` — loads **per‑element resource configuration** files (one per DCAT resource type), defining JSON path extraction, RDF type, property mappings, and value transformations.
   - Outcome: In‑memory configuration **model objects** that reflect the structure of the configuration (see next section).

2. **Mapping (Model Construction)**
   - Purpose: Transform Dataverse metadata (obtained via `ExportDataProvider`) into RDF **Jena Models** according to configuration.
   - Main components:
     - `JaywayJsonFinder` — navigates the input JSON tree with robust path expressions.
     - `ResourceMapper` — builds a `Model` for each configured DCAT element, asserts `rdf:type`, sets properties, expands CURIEs using `Prefixes`, and produces RDF resources (subjects).
   - Outcome: One `Model` per element plus subject collections identified by `rdf:type`.

3. **Validation (Model Validation)**
    - Purpose: Aid the user on providing correct configuration data with  meaningful messages
    - Main components:
      - `Validators` - calls all the specific validators
    - Outcome: Valid configuration (or at least reasonably valid), circumventing problems later.

3. **Writing (Serialization)**
   - Purpose: Merge element models, apply configured **relations** (n:m), then serialize the combined model.
   - Main components:
     - `Dcat3ExporterBase` — shared orchestration: load root config, build element models, apply relations, and write via a format‑specific Jena writer.
     - Format implementations:
       - `Dcat3ExporterTurtle` → writer `"TURTLE"`, media type `text/turtle`.
       - `Dcat3ExporterJsonLd` → writer `"JSON-LD"`, media type `application/ld+json`.
       - `Dcat3ExporterRdfXml` → writer `"RDF/XML"`, media type `application/rdf+xml`.
   - Outcome: Deterministic, profile‑compliant RDF output, independent of configuration keys for format.

## Configuration Model
Configuration is represented by **strongly‑typed model classes** that parallel file content:

- **Root level** (`RootConfig`)
  - `baseDir` — base directory for locating per‑element files.
  - `prefixes` — map of CURIE prefixes → IRIs.
  - `elements` — list of `Element` descriptors, each pointing to a resource configuration file and the element’s RDF type (`typeCurieOrIri`).
  - `relations` — list of `Relation` descriptors (subject element id, predicate CURIE/IRI, object element id).
  - `trace` — optional diagnostics to log the input data snapshot.

- **Resource level** (`ResourceConfig`)
  - Declares the **mapping rules** for a single DCAT resource type, including value extraction (JSON paths), constant values, conditional mappings, and property targets (CURIE/IRI expansion via `Prefixes`).

This mirroring ensures loaders can validate and report configuration issues early and gives the mapper a stable, explicit contract.

## Loaders and File Resolution
- **`RootConfigLoader`** loads the main configuration file from the classpath or filesystem.
- **`ResourceConfigLoader`** loads per‑element config files referenced by `RootConfig.elements[i].file`.
- **`FileResolver.resolveElementFile(baseDir, element.file)`** applies the following **fallback chain** to locate configuration:
  1. **Absolute path** — if `element.file` is absolute, use it as‑is.
  2. **Relative to `baseDir`** — if `element.file` is relative and `baseDir` is set, resolve `baseDir/element.file`.
  3. **Classpath resource** — if not found on filesystem, attempt to load `element.file` from the application classpath (e.g., `src/main/resources`).
  4. **Failure** — emit a clear error stating the search order and the path(s) attempted.

This mechanism lets you bundle defaults within the JAR **and** override them with deployment‑specific files.

## Standards and Validation
- **DCAT‑3.0‑AP‑NL**: The exporter targets the Dutch application profile of DCAT 3.0. Mappings and prefixes should reflect AP‑NL vocabularies and constraints.
- **SHACL**: Generated RDF can be validated against **SHACL shapes** (e.g., node shapes for `dcat:Dataset`, `dcat:Distribution`, `vcard:Kind`, etc.). The SHACL shapes enforce:
  - Cardinalities (`sh:minCount`, `sh:maxCount`).
  - Datatypes (`xsd:string`, `rdf:langString`, `xsd:date`, `xsd:dateTime`).
  - Node kinds (IRI vs literal) and nested shape conformance (`sh:node`).

> Recommendation: Include a validation step in CI using **Apache Jena SHACL** to assert conformance to DCAT‑AP‑NL. Store the shapes under `src/main/resources/shacl/` and fail builds on violations.

## Service Registration
All three format exporters are annotated with `@AutoService(Exporter.class)`. During the build, `META-INF/services/io.gdcc.spi.export.Exporter` entries are generated automatically so Dataverse can discover them via `ServiceLoader`.

## Deployment
1. Build: `mvn package`.
2. Copy the JAR to the Dataverse SPI exporters directory.
3. Restart Payara.
4. In Dataverse, use **Metadata → Export Metadata** and select the desired **DCAT‑3 format**.

## Testing and Tooling
- Unit tests can assert mapping behavior by comparing expected models to the output (`mvn test`).
- Include a **validation script** (e.g., `validate.sh`) to run SHACL checks on produced RDF.
- Provide scripts to update expected outputs after mapping changes.

## Extending the System
To add a new format:
1. Create a subclass of `Dcat3ExporterBase`.
2. Implement `getFormatName()`, `getMediaTypeValue()`, and `getJenaWriterName()`.
3. Annotate with `@AutoService(Exporter.class)` and rebuild.

To add a new DCAT element:
1. Add an `Element` entry to `RootConfig` referencing a new `ResourceConfig` file.
2. Provide the resource mapping rules in the config file.
3. (Optional) Update SHACL shapes if the element introduces new constraints.


## TODO
- **Validate input `.properties` configuration**  
  Ensure robust checks for required keys, value types, and fallback defaults.

- **Extend unit test coverage**  
  Cover edge cases in configuration parsing, mapping logic, and serialization.

- **Split TSV file**  
  Separate into:
    - A **DCAT‑AP‑NL 3.0 extension** of the metadata.
    - An **organization-specific TSV** for custom fields.

- **Review current `.properties` configuration mapping capabilities**  
  Add support for:
    - Optional flags.
    - Default values.
