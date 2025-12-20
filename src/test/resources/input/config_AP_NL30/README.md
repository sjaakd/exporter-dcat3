# DCAT-AP-NL 3.0 Mapping for Dataverse (GDN)

This package contains properties mapping files to generate DCAT/DCAT-AP-NL 3.0
RDF from Dataverse exports. It is tailored for the Geological Survey of the Netherlands (GDN)
and aligned to the **Geonovum DCAT-AP-NL 3.0 SHACL** shapes.

## Files

- `dcat-root.txt` — root configuration (prefixes, elements, relations).
- `dcat-dataset.txt` — Dataset mapping (`dcat:Dataset`).
- `dcat-distribution.txt` — Distribution mapping (`dcat:Distribution`).
- `dcat-dataservice.txt` — DataService mapping (`dcat:DataService`).

## Key compliance notes (why these properties are present)

### Distribution (`dcat:Distribution`)
- **`dcat:byteSize`** is typed as `xsd:nonNegativeInteger` (NL SHACL expects non-negative integer).
- **`dcat:mediaType`** is a **node** typed as `dct:MediaType` with an **IANA media type URI**.
- **`dct:language`** is present (ENG) and typed as `dct:LinguisticSystem`.
- **`dct:conformsTo`** is present, using a minimal `dct:Standard` node.
- **`dcat:accessURL` / `dcat:downloadURL`** are emitted as **typed `rdfs:Resource` nodes**.
- **`dct:rights`** is a node typed as `dct:RightsStatement` (EU Access Rights NAL).
- **`dct:license`** is a node typed as `dct:LicenseDocument`.
- **`dcat:accessService`** points to a typed **`dcat:DataService`** (`http://localhost:8080/api/`).
- **SPDX checksum**: `spdx:checksumValue` is `xsd:hexBinary`; `spdx:algorithm` is typed as `spdx:ChecksumAlgorithm`.

### Dataset (`dcat:Dataset`)
- **`dcat:landingPage`** and **`foaf:page`** are typed as **`foaf:Document`** (web page).
- **`dct:language`** present (ENG) and typed.
- **`dct:conformsTo`** minimal `dct:Standard` node present.
- **`dct:accessRights`** is a node typed as `dct:RightsStatement`.
- **`dcat:theme`** is a `skos:Concept`; we provide a local `skos:prefLabel` so SHACL does not need remote vocab fetch.
- **Creators/Publisher** are **FOAF Agents**; if organizational identifiers (e.g., ROR) exist, they are used as IRIs.
- **`dcatap:applicableLegislation`** is typed as **`eli:LegalResource`**.

### DataService (`dcat:DataService`)
The NL shapes enforce many `minCount=1` constraints on DataService. We therefore include:
- **Title, Description, Identifier**
- **Endpoint URL** (typed `rdfs:Resource`) and **Endpoint Description** (typed `foaf:Document`)
- **Language (ENG)**, **Keywords**, **Access Rights (PUBLIC)**
- **Theme** (`skos:Concept` + `prefLabel`), **License** (`dct:LicenseDocument`)
- **Publisher** and **Creator** as `foaf:Agent`
- **Contact Point** as `vcard:Kind`

## Enablement (root)
In `dcat-root.txt`, ensure these blocks exist:

```properties
element.dataservice.id    = dataservice
element.dataservice.type  = dcat:DataService
element.dataservice.file  = dcat-dataservice.properties

relation.dataset_has_service.subject   = dataset
relation.dataset_has_service.predicate = dcat:service
relation.dataset_has_service.object    = dataservice
```

## Validation tips
- Use the **Geonovum DCAT-AP-NL 3.0 SHACL** shapes to validate. They check class ranges and cardinalities strictly.
- Ensure objects are **typed nodes** where required (`dct:MediaType`, `dct:RightsStatement`, `dct:LicenseDocument`, `foaf:Document`, `eli:LegalResource`, `skos:Concept`).
- For `skos:Concept` values (e.g., themes), provide a local `skos:prefLabel`; SHACL validators generally **do not** fetch labels from the web.
- For SPDX checksums, ensure `checksumValue` is `xsd:hexBinary` and the `algorithm` node is typed `spdx:ChecksumAlgorithm`.

## References
- **DCAT v3 (W3C Recommendation, 2024)** — properties, classes, DataService modelling: https://www.w3.org/TR/vocab-dcat-3/
- **DCAT-AP 3.0 (SEMIC, 2024)** — profile rules, cardinalities, guidance: https://semiceu.github.io/DCAT-AP/releases/3.0.0/
- **Geonovum DCAT-AP-NL 3.0 SHACL** — validation shapes (strict minCounts & ranges): https://github.com/Geonovum/DCAT-AP-NL30/blob/main/shapes/dcat-ap-nl-SHACL.ttl
- **IANA Media Types** — use as URIs for `dcat:mediaType`: https://www.iana.org/assignments/media-types/media-types.xhtml
- **SPDX RDF Terms** — checksum and algorithm classes/properties: https://spdx.org/rdf/
