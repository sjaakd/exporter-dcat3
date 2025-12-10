# DCAT‑3 Exporter (PoC) — Approach, Rationale & Next Steps

> **Status**: Proof of Concept (PoC)  
> **Scope**: Externalized DCAT‑3/DCAT‑AP‑NL mapping for Dataverse → RDF (RDF/XML, Turtle, JSON‑LD)  
> **Owner**: Sjaak Derksen

---

## Why this approach?

There are **many DCAT application profiles** in the wild—some **domain‑specific** (medical, geo‑spatial, ETD), some **regulatory** (DCAT‑AP, DCAT‑AP‑NL), and some **organisation‑specific**. Each profile tends to introduce **additional metadata requirements** (often captured as extra blocks/TSVs in Dataverse). I want a solution that:

- **Externalizes the mapping** from Dataverse JSON → RDF (no hard‑coded Java mapping).
- Is **profile‑agnostic** and **extensible**: adding or changing a profile is editing a `.properties` file, not rewriting code.
- Treats the exporter’s input as a generic **`ExportData`** object (wrapping `datasetJson`, files, etc.) and uses **JSONPath** to fetch values.
- Keeps **prefixes centrally** in `dcat-root.properties`, while element mapping files stay clean (only CURIEs/IRIs).

This separation gives us **config‑driven portability**: moving from DCAT‑AP‑NL to a medical or ETD profile is just a new mapping bundle and a small root configuration—not touching code.

---

## Architecture (PoC)

### 1) Root configuration (`dcat-root.properties`)
- **Global prefixes** (`prefix.*`) live here.
- **Elements** to build: `catalog`, `dataset`, `distribution` (and any future ones: `dataService`, `catalogRecord`, etc.).
- **Relations** (stitching): e.g., `catalog --dcat:dataset--> dataset`, `dataset --dcat:distribution--> distribution`.
- **Output format**: `rdfxml | turtle | jsonld`.

### 2) Element mapping files (`*.properties`)
- **Subject IRI** source (const/template/JSONPath).
- A set of **property value sources**:
  - `as = literal | iri | bnode | node-ref`
  - `lang`, `datatype`, `multi`, `map.*`, etc.
- **JSONPath** to Dataverse fields under **`$.datasetJson`**.  
  Example:  
  ```properties
  props.title_en.json = $.datasetJson.datasetVersion.metadataBlocks.citation.fields[?(@.typeName=='title')].value

*   **Resilience**: use **recursive descent** (`$..GDNDatasetMetadata`) for organisation/domain blocks when nesting may vary.

### 3) Exporter runtime

*   Loads **root** → loads **element mappings** → **builds Jena models** per element → **merges** → applies **relations** → **serializes** to configured format.
*   **CURIE expansion** uses root prefixes.
*   **Model prefixes** set from root for human‑readable output.

***

## What works today

*   ✅ **Catalog** mapping produces expected triples: `dct:publisher` (foaf:Agent with names + ROR `dct:type`), `dcat:contactPoint` (vcard:Kind with email, URL, names), multilingual `dct:description` and `dct:title`.
*   ✅ **Dataset & Distribution** mappings created to match **DCAT‑AP‑NL‑GDN** sheet:
    *   Dataset: identifier, titles, descriptions, access rights, themes, keywords, publisher, contact‑point, HVD legislation + category.
    *   Distribution: accessURL, license, byteSize, mediaType (IANA), file format (EU File‑Type NAL).
*   ✅ **Root prefixes centralized**; element files are CURIE‑only.
*   ✅ **Unit tests** parse exporter output and assert core triples; parser explicitly set to the chosen format (RIOT + `Lang.*`).

***

## Action points (short‑term)

1.  **Unit test improvements**
    *   [ ] Switch parsing to **RIOT** with explicit `Lang` based on `exporter.getMediaType()` (avoid auto‑detect pitfalls).
    *   [ ] Add **AssertJ** checks for:
        *   Presence of `dcat:Catalog`, `dcat:Dataset`, `dcat:Distribution`.
        *   Core relations: `catalog dcat:dataset dataset`, `dataset dcat:distribution distribution`.
        *   Required properties per element (titles, license, accessURL).
    *   [ ] Add **SHACL validation** (Jena SHACL) to reduce assertion boilerplate:
        *   Minimal shapes for Catalog/Dataset/Distribution conformance.
        *   Single assert: `report.conforms()`.

2.  **Input validation (mapping loader)**
    *   [ ] Fail fast on missing `predicate` or invalid `as` values.
    *   [ ] Validate `node-ref` has target `node`.
    *   [ ] Optionally warn (not fail) when JSONPath returns empty for **recommended** fields.

3.  **Output format completeness**
    *   [ ] Confirm coverage for DCAT‑AP‑NL required/recommended properties:
        *   Dataset: `dct:title`, `dct:description`, `dct:identifier`, `dct:publisher`, `dcat:theme`, `dcat:keyword`, `dct:accessRights`, (conditional) `dcatap:applicableLegislation`, `dcatap:hvdCategory`.
        *   Distribution: `dcat:accessURL`, `dct:license`, `dcat:mediaType` (IANA), `dct:format` (EU NAL), `dcat:byteSize`.
    *   [ ] Add any missing fields you care about (creator, language, temporal/spatial, etc.) as new `props.*` with JSONPath.

4.  **Rudimentary guide (README additions)**
    *   [ ] Explain how to **set the system property** (`dataverse.dcat3.config`) so `RootConfigLoader` finds `dcat-root.properties`.
    *   [ ] Show folder layout for root and element mapping files.
    *   [ ] Document **JSONPath basics** (direct child vs `..` recursive descent).
    *   [ ] Describe how to turn **Dataverse fields** (e.g., `metadataBlocks.citation`) into JSONPath.
    *   [ ] Explain **CURIEs** and **prefixes** (centralized in root).

5.  **Profile extensibility**
    *   [ ] Provide a template for adding new application profiles (e.g., medical or ETD).
    *   [ ] Describe how to introduce **new Dataverse TSVs** and reference them via JSONPath without code changes.
    *   [ ] Keep a `profiles/` directory of mapping bundles.

6.  **Per‑file distributions (optional enhancement)**
    *   [ ] Mint one `dcat:Distribution` per `files[*]` with `subject.iri.template` (e.g., hash or `{index}`), set `multi=true` on properties.
    *   [ ] Update relations: dataset → multiple distributions.

***

## Language support (to investigate)

Dataverse metadata content is usually in **one language** (often English). Multiple resource bundles affect **UI labels and controlled vocabulary tags**, not free-text fields.  
For DCAT-AP-NL multilingual requirements (`@nl`, `@en`), we may need:

*   Additional metadata blocks for translations, or
*   Post-processing to inject alternative language values.

This PoC assumes **English-only content** for now; multilingual handling is a future action point.

***

## Testing strategy (PoC → robust)

*   **RIOT**: Apache Jena’s RDF Input/Output Technology for parsing/serializing RDF. It supports explicit format handling (RDF/XML, Turtle, JSON-LD) and proper language-tag parsing, making tests deterministic and multilingual checks reliable.

### A) AssertJ + RIOT

```java
String mediaType = exporter.getMediaType(); // "application/rdf+xml"|"text/turtle"|"application/ld+json"
Lang lang = switch (mediaType.toLowerCase()) {
  case "application/rdf+xml" -> Lang.RDFXML;
  case "application/ld+json" -> Lang.JSONLD;
  default -> Lang.TURTLE;
};

Model m = ModelFactory.createDefaultModel();
RDFDataMgr.read(m, new ByteArrayInputStream(bytes), lang);

// Check language tags
Property DCT_TITLE = m.createProperty("http://purl.org/dc/terms/title");
m.listStatements(catalog, DCT_TITLE, (RDFNode) null).forEachRemaining(stmt -> {
    System.out.println(stmt.getObject().asLiteral().getLanguage());
});
```

### B) SHACL (Jena)

*   Shapes file: require `dcat:Catalog` to have `dcat:dataset`; `dcat:Dataset` to have `dcat:distribution`; `dcat:Distribution` to have `dcat:accessURL` and `dct:license`.
*   Test:

```java
Shapes shapes = Shapes.parse(RDFDataMgr.loadGraph("src/test/resources/shapes/dcat.shapes.ttl"));
ValidationReport report = ShaclValidator.get().validate(shapes, m.getGraph());
assertThat(report.conforms()).isTrue();
```

### C) JSONPath smoke tests

*   Verify JSONPaths against `ExportData.datasetJson` to catch typos (e.g., `value` vs `primitiveValue`).

***

## Config conventions

*   **Root system property**: set `-Ddataverse.dcat3.config=/path/to/dcat-root.properties` (or classpath resource name).
*   **Element files**: stored alongside the root or on classpath—resolved by `RootConfigLoader.resolveElementFile(...)`.
*   **Prefixes**: only in root; element files use **CURIEs**.
*   **JSONPath**: `$.datasetJson...` for Dataverse content; `..` for resilient GDN block reads.

***

## Example mapping fragment (Dataset)

```properties
# Subject (dataset IRI)
subject.iri.json = $.datasetJson.persistentUrl

# Title (EN)
props.title_en.predicate = dct:title
props.title_en.as = literal
props.title_en.lang = en
props.title_en.json = $.datasetJson.datasetVersion.metadataBlocks.citation.fields[?(@.typeName=='title')].value

# Access Rights (EU NAL)
props.accessRights.predicate = dct:accessRights
props.accessRights.as = iri
props.accessRights.json = $..GDNDatasetMetadata.fields[?(@.typeName=='GDNaccessRights')].value
props.accessRights.map.public     = http://publications.europa.eu/resource/authority/access-right/PUBLIC
props.accessRights.map.restricted = http://publications.europa.eu/resource/authority/access-right/RESTRICTED
props.accessRights.map.non-public = http://publications.europa.eu/resource/authority/access-right/NON_PUBLIC
```

***

## Roadmap

*   **v0.1 (PoC)**: Catalog + Dataset + Distribution; AssertJ tests; explicit parser; centralized prefixes.
*   **v0.2**: SHACL test suite; per‑file distributions; creator/publisher refinement; spatial/temporal (optional).
*   **v0.3**: Profile bundles (`profiles/…`) with documentation; CI pipeline (mvn test + SHACL).
*   **v1.0**: Hardened validation paths, error reporting, profile selector, sample datasets and tutorials.

***

## Contributing

*   Propose new application profile mappings via PR in `profiles/`.
*   Add/adjust JSONPaths to support additional Dataverse TSVs.
*   Extend SHACL shapes to capture profile‑specific constraints.

***

## License & attribution

*   This PoC composes RDF using **Apache Jena**; validation examples reference **Jena SHACL**.
*   Media types reference **IANA** registry; concept mappings reference **EU Vocabularies** Named Authority Lists.
*   See respective licenses and registries for details.

