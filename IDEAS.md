# DCAT Exporter — Configuration-Driven Mapping & Serialization (Dataverse)

This document explains how to make your Dataverse DCAT exporter:

1. **Externally configurable** for field mappings using MicroProfile/SmallRye Config (`@ConfigMapping`), and  
2. **Flexible in output format** (Turtle, RDF/XML, JSON‑LD) using the same configuration mechanism.

It’s designed for inclusion in a GitHub repository, with ready-to-copy code snippets.

> **Why this approach?**  
> • Dataverse’s Exporter SPI lets you ship a **generic plugin** while installations customize behavior via configuration and standard inputs (`ExportDataProvider`). [1](https://www.youtube.com/watch?v=nxnMvBkoS0k)[2](https://github.com/quarkusio/quarkus/discussions/38522)  
> • MicroProfile Config & SmallRye Config provide **typed, overrideable config** from system properties, env vars, and `META‑INF/microprofile-config.properties`. [3](https://guides.dataverse.org/en/4.20/admin/metadatacustomization.html)  
> • DCAT v3 is RDF and supports multiple serializations (Turtle, RDF/XML, JSON‑LD). [1](https://www.youtube.com/watch?v=nxnMvBkoS0k)

---

## 1) Externalize DCAT Field Mappings with `@ConfigMapping` (SmallRye)

### 1.1 Define typed configuration

```java
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "dcat")
public interface DcatConfig {
  Catalog catalog();
  Dataset dataset();

  interface Catalog {
    String uri();
    Field title();
    Field description();
    Contact contact();
    Publisher publisher();
  }

  interface Dataset {
    String uri();
    Field title();
    Field description();
    Themes themes();
  }

  /** A value can come from JSON Pointer, bean path, or be constant. */
  interface Field {
    String pointer();      // e.g. /datasetORE/oreDescribes/schemaIsPartOf/schemaName
    String path();         // e.g. datasetORE.oreDescribes.schemaIsPartOf.schemaName
    String constValue();   // e.g. "Geological Survey of the Netherlands"
    String lang();         // e.g. "nl"
  }

  interface Contact {
    String fnNl(); String fnEn();
    String email(); String org();
  }

  interface Publisher {
    String nameNl(); String nameEn(); String type(); // e.g., ROR URI
  }

  /** Example: map identifiers to URIs via a resolver. */
  interface Themes {
    String pointer();      // array of identifiers in ExportData JSON
    String resolver();     // optional resolver name (see §1.3)
  }
}
````

### 1.2 Programmatic retrieval (no CDI)

```java
import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.ConfigProvider;

SmallRyeConfig sr = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
DcatConfig cfg = sr.getConfigMapping(DcatConfig.class);
```

> This non‑CDI usage (`unwrap(SmallRyeConfig.class)` → `getConfigMapping`) is supported by SmallRye Config and works in Dataverse’s runtime. [\[365tno-my....epoint.com\]](https://365tno-my.sharepoint.com/personal/sjaak_derksen_tno_nl/Documents/Microsoft%20Copilot%20Chat%20Files/GDN-orig.tsv)

### 1.3 Mapping engine (pointers, constants, resolvers)

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import java.util.*;

public final class DcatMappingEngine {
  private final DcatConfig cfg;
  private final ObjectMapper mapper = new ObjectMapper();

  public DcatMappingEngine(DcatConfig cfg) { this.cfg = cfg; }

  /** Build the Jena model from ExportData and its JSON view. */
  public Model toDcatModel(Object exportDataPojo, JsonNode rootJson) {
    Model m = ModelFactory.createDefaultModel();
    m.setNsPrefix("dcat", DCAT.NS);
    m.setNsPrefix("dct", DCTerms.NS);
    m.setNsPrefix("foaf", FOAF.NS);
    m.setNsPrefix("vcard", VCARD.uri);

    // --- Catalog ---
    Resource catalog = m.createResource(nonEmpty(cfg.catalog().uri()))
      .addProperty(RDF.type, DCAT.Catalog);

    addLang(m, catalog, DCTerms.title,       extract(rootJson, exportDataPojo, cfg.catalog().title()),       cfg.catalog().title().lang());
    addLang(m, catalog, DCTerms.description,  extract(rootJson, exportDataPojo, cfg.catalog().description()), cfg.catalog().description().lang());

    // Contact point (VCARD)
    Resource contact = m.createResource()
      .addProperty(RDF.type, VCARD.AGENT)
      .addProperty(VCARD.FN,    m.createLiteral(cfg.catalog().contact().fnNl(), "nl"))
      .addProperty(VCARD.FN,    m.createLiteral(cfg.catalog().contact().fnEn(), "en"))
      .addProperty(VCARD.EMAIL, m.createResource(cfg.catalog().contact().email()))
      .addProperty(VCARD.ORG,   m.createResource(cfg.catalog().contact().org()));
    catalog.addProperty(DCAT.contactPoint, contact);

    // Publisher (FOAF Agent + dct:type)
    Resource publisher = m.createResource()
      .addProperty(RDF.type, FOAF.Agent)
      .addProperty(DCTerms.type, m.createResource(cfg.catalog().publisher().type()))
      .addProperty(FOAF.name, m.createLiteral(cfg.catalog().publisher().nameNl(), "nl"))
      .addProperty(FOAF.name, m.createLiteral(cfg.catalog().publisher().nameEn(), "en"));
    catalog.addProperty(DCTerms.publisher, publisher);

    // --- Dataset ---
    Resource dataset = m.createResource(nonEmpty(cfg.dataset().uri()))
      .addProperty(RDF.type, DCAT.Dataset);
    addLang(m, dataset, DCTerms.title,       extract(rootJson, exportDataPojo, cfg.dataset().title()),       cfg.dataset().title().lang());
    addLang(m, dataset, DCTerms.description,  extract(rootJson, exportDataPojo, cfg.dataset().description()), cfg.dataset().description().lang());

    // Themes (identifier -> URI via resolver)
    for (String id : extractArray(rootJson, cfg.dataset().themes().pointer())) {
      String themeUri = ResolverRegistry.resolve(cfg.dataset().themes().resolver(), "theme", id);
      if (themeUri != null && !themeUri.isBlank()) {
        dataset.addProperty(DCAT.theme, m.createResource(themeUri));
      }
    }

    catalog.addProperty(DCAT.dataset, dataset);
    return m;
  }

  private static void addLang(Model m, Resource r, Property p, String text, String lang) {
    if (text != null && !text.isBlank()) r.addProperty(p, m.createLiteral(text, lang));
  }

  /** Extract value with priority: constValue → JSON Pointer → bean path. */
  private static String extract(JsonNode root, Object pojo, DcatConfig.Field f) {
    if (nonEmpty(f.constValue()) != null) return f.constValue();
    if (nonEmpty(f.pointer())   != null) return root.at(f.pointer()).asText();
    if (nonEmpty(f.path())      != null) return BeanPaths.get(pojo, f.path()); // implement a lightweight reflection helper
    return "";
  }

  private static List<String> extractArray(JsonNode root, String pointer) {
    ArrayNode arr = (ArrayNode) root.at(pointer);
    List<String> list = new ArrayList<>();
    if (arr != null) arr.forEach(n -> list.add(n.asText()));
    return list;
  }

  private static String nonEmpty(String s) { return (s == null || s.isBlank()) ? "" : s; }
}
```

> Jena’s DCAT/FOAF/VCARD/DCTerms vocabularies provide correct URIs/classes. [\[data.harvard.edu\]](https://data.harvard.edu/dataverse), [\[zenodo.org\]](https://zenodo.org/records/8133723/files/A%20Plug-in%20Approach%20to%20Controlled%20Vocabulary%20Support%20in%20Dataverse.pdf)

### 1.4 Configuration file (defaults)

Create `META‑INF/microprofile-config.properties` in your exporter JAR:

```properties
# --- Catalog ---
dcat.catalog.uri=https://example.org/catalog
dcat.catalog.title.pointer=/datasetORE/oreDescribes/schemaIsPartOf/schemaName
dcat.catalog.title.lang=nl
dcat.catalog.description.pointer=/datasetORE/oreDescribes/schemaIsPartOf/schemaDescription
dcat.catalog.description.lang=nl

dcat.catalog.contact.fnNl=Geologische Dienst Nederland
dcat.catalog.contact.fnEn=Geological Survey of the Netherlands
dcat.catalog.contact.email=mailto:support@geologischedienst.nl
dcat.catalog.contact.org=https://www.geologischedienst.nl/

dcat.catalog.publisher.nameNl=Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk Onderzoek (nl), TNO
dcat.catalog.publisher.nameEn=Netherlands Organisation for Applied Scientific Research
dcat.catalog.publisher.type=https://ror.org/01bnjb948

# --- Dataset ---
dcat.dataset.uri=https://example.org/dataset/${export.id:1234}
dcat.dataset.title.pointer=/metadata/title/0/value
dcat.dataset.title.lang=nl
dcat.dataset.description.pointer=/metadata/description/0/value
dcat.dataset.description.lang=nl

# Themes: identifiers are in export JSON; resolve to EU URIs
dcat.dataset.themes.pointer=/metadata/gdnThemeIds
dcat.dataset.themes.resolver=eu-theme
```

**Overrides:** Operators can override any value via **JVM system properties** or **environment variables**; MicroProfile precedence is **system > env > file**. [\[guides.dataverse.org\]](https://guides.dataverse.org/en/4.20/admin/metadatacustomization.html)

***

## 2) Configure Output Format (Turtle, RDF/XML, JSON‑LD)

### 2.1 Add serialization keys

```properties
dcat.output.format=turtle    # turtle | rdfxml | jsonld
dcat.output.pretty=true      # pretty-print when supported

# Optional JSON-LD extras
dcat.output.jsonld.context=https://www.w3.org/ns/dcat.jsonld
dcat.output.jsonld.frame=
```

### 2.2 Map format → Jena writer & media type

```java
enum DcatOutput {
  TURTLE("TURTLE", "text/turtle"),
  RDFXML("RDF/XML", "application/rdf+xml"),
  JSONLD("JSON-LD", "application/ld+json");

  final String jenaSyntax;
  final String mediaType;

  DcatOutput(String jenaSyntax, String mediaType) {
    this.jenaSyntax = jenaSyntax; this.mediaType = mediaType;
  }

  static DcatOutput from(String s) {
    if (s == null) return TURTLE;
    switch (s.toLowerCase()) {
      case "rdfxml": return RDFXML;
      case "jsonld": return JSONLD;
      default: return TURTLE;
    }
  }
}
```

### 2.3 Apply in your exporter

```java
import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.ConfigProvider;
import org.apache.jena.rdf.model.Model;

public class Dcat3Exporter implements io.gdcc.spi.export.Exporter {

  private DcatOutput output() {
    SmallRyeConfig sr = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
    String fmt = sr.getOptionalValue("dcat.output.format", String.class).orElse("turtle");
    return DcatOutput.from(fmt);
  }

  @Override
  public String getMediaType() {
    return output().mediaType;  // keep media type consistent with writer
  }

  @Override
  public void exportDataset(io.gdcc.spi.export.ExportDataProvider provider, java.io.OutputStream out)
      throws io.gdcc.spi.export.ExportException {

    // Build canonical ExportData + JSON view
    io.gdcc.spi.export.parsing.ExportData data = io.gdcc.spi.export.parsing.ExportData.builder()
        .provider(provider).build();
    com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper()
        .valueToTree(data);

    // Config mapping for DCAT fields
    SmallRyeConfig sr = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
    DcatConfig cfg = sr.getConfigMapping(DcatConfig.class);

    // DCAT model
    Model model = new DcatMappingEngine(cfg).toDcatModel(data, root);

    // Write using configured syntax
    model.write(out, output().jenaSyntax);
  }

  // ... other SPI methods (format name, display name, availability, etc.)
}
```

> Keep `getMediaType()` aligned with your writer (`text/turtle`, `application/rdf+xml`, or `application/ld+json`). DCAT v3 supports all these serializations. [\[youtube.com\]](https://www.youtube.com/watch?v=nxnMvBkoS0k)

***

## 3) Optional — Namespace prefixes from config

```java
void applyPrefixes(org.apache.jena.rdf.model.Model m, io.smallrye.config.SmallRyeConfig sr) {
  sr.getPropertyNames().forEachRemaining(name -> {
    if (name.startsWith("dcat.ns.")) {
      String prefix = name.substring("dcat.ns.".length());
      String uri = sr.getValue(name, String.class);
      m.setNsPrefix(prefix, uri);
    }
  });
}
```

**Example:**

```properties
dcat.ns.dcat=http://www.w3.org/ns/dcat#
dcat.ns.dct=http://purl.org/dc/terms/
dcat.ns.foaf=http://xmlns.com/foaf/0.1/
dcat.ns.vcard=http://www.w3.org/2006/vcard/ns#
```

***

## 4) Maven Shade Plugin (service merging)

When producing a single deployable JAR for Dataverse:

*   **Merge service descriptors** so `@AutoService(Exporter.class)` and SmallRye services are discoverable.
*   Avoid shading Jakarta EE APIs supplied by Payara; relocate only if you hit conflicts.

```xml
<build>
  <plugins>
    <!-- Annotation processing for AutoService -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.11.0</version>
      <configuration>
        <annotationProcessorPaths>
          <path>
            <groupId>com.google.auto.service</groupId>
            <artifactId>auto-service</artifactId>
            <version>1.1.1</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>

    <!-- Shade to build a single deployable JAR -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>3.5.0</version>
      <executions>
        <execution>
          <phase>package</phase>
          <goals><goal>shade</goal></goals>
          <configuration>
            <createDependencyReducedPom>true</createDependencyReducedPom>
            <transformers>
              <!-- Merge META-INF/services from dependencies -->
              <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
              <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer"/>
            </transformers>

            <!-- Optional relocation to avoid clashes; add only if needed
            <relocations>
              <relocation>
                <pattern>io.smallrye.config</pattern>
                <shadedPattern>your.pkg.shaded.smallrye.config</shadedPattern>
              </relocation>
            </relocations>
            -->
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

> Community exporters follow similar packaging patterns; Dataverse loads your JAR as an external exporter via the SPI. [\[youtube.com\]](https://www.youtube.com/watch?v=f93AawpGbEA)

***

## 5) Operational Notes

*   **Configuration precedence:** MicroProfile Config resolves properties from System properties (400), Env vars (300), and `META‑INF/microprofile-config.properties` (100). Ops can override without touching the JAR. [\[guides.dataverse.org\]](https://guides.dataverse.org/en/4.20/admin/metadatacustomization.html)
*   **Dataverse SPI inputs:** You receive dataset metadata via `ExportDataProvider` (Native JSON, OAI‑ORE, file details). Build `ExportData` once and map fields per config. [\[youtube.com\]](https://www.youtube.com/watch?v=nxnMvBkoS0k)
*   **Validation:** If needed, add SHACL validation for DCAT‑AP/DCAT‑US in tests; profiles often ship shapes for conformance. [\[github.com\]](https://github.com/erykkul/dataverse-transformer-exporter)

***

## 6) References

*   **Dataverse Exporter SPI & external exporters**: *Metadata Export Formats* and *Admin Guide* (automatic exports, batch exports). [\[youtube.com\]](https://www.youtube.com/watch?v=nxnMvBkoS0k), [\[github.com\]](https://github.com/quarkusio/quarkus/discussions/38522)
*   **MicroProfile Config** (sources & precedence) and SmallRye `@ConfigMapping`. [\[guides.dataverse.org\]](https://guides.dataverse.org/en/4.20/admin/metadatacustomization.html), [\[365tno-my....epoint.com\]](https://365tno-my.sharepoint.com/personal/sjaak_derksen_tno_nl/Documents/Microsoft%20Copilot%20Chat%20Files/GDN-orig.tsv)
*   **DCAT v3** (RDF vocabulary & serializations). [\[youtube.com\]](https://www.youtube.com/watch?v=nxnMvBkoS0k)
*   **Jena vocabularies** (DCAT/FOAF/VCARD/DCTerms). [\[data.harvard.edu\]](https://data.harvard.edu/dataverse), [\[zenodo.org\]](https://zenodo.org/records/8133723/files/A%20Plug-in%20Approach%20to%20Controlled%20Vocabulary%20Support%20in%20Dataverse.pdf)

***

## 7) Checklist

*   [ ] Add `DcatConfig` and `DcatMappingEngine`.
*   [ ] Implement `DcatOutput` enum and use it in `getMediaType()` + `model.write(...)`.
*   [ ] Provide `META‑INF/microprofile-config.properties` with defaults; document overrides.
*   [ ] Package with Shade (services merged); drop JAR into Dataverse external exporters location; restart Payara.
*   [ ] Verify in UI/API: “Download Metadata” shows your format; output matches configured serialization.

