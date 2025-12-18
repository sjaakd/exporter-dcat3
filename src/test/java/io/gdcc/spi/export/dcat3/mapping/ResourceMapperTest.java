package io.gdcc.spi.export.dcat3.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gdcc.spi.export.dcat3.config.model.ResourceConfig;
import io.gdcc.spi.export.dcat3.config.model.ValueSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResourceMapperTest {

    private static JsonNode jsonNode(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(json);
    }

    private static JaywayJsonFinder finderFor(String json) throws Exception {
        return new JaywayJsonFinder(jsonNode(json));
    }

    @Test
    @DisplayName("build() adds RDF.type and a literal property from constValue with language")
    void build_adds_type_and_literal_from_const() throws Exception {
        // Real Prefixes
        Map<String, String> ns = new LinkedHashMap<String, String>();
        ns.put("dcat", "http://www.w3.org/ns/dcat#");
        ns.put("dct", "http://purl.org/dc/terms/");
        Prefixes prefixes = new Prefixes(ns);

        // ResourceConfig with deep stubs for subject()
        ResourceConfig rc = mock(ResourceConfig.class, RETURNS_DEEP_STUBS);
        when(rc.subject().iriConst()).thenReturn("http://example.org/id");
        when(rc.subject().iriTemplate()).thenReturn(null);
        when(rc.subject().iriFormat()).thenReturn(null);
        when(rc.subject().iriJson()).thenReturn(null);

        // ValueSource for dct:title literal
        ValueSource vsTitle = mock(ValueSource.class);
        when(vsTitle.predicate()).thenReturn("dct:title");
        when(vsTitle.as()).thenReturn("literal");
        when(vsTitle.constValue()).thenReturn("Demo");
        when(vsTitle.lang()).thenReturn("en");
        when(vsTitle.datatype()).thenReturn(null);
        when(vsTitle.map()).thenReturn(java.util.Collections.emptyMap());
        when(vsTitle.jsonPaths()).thenReturn(java.util.Collections.emptyList());
        when(vsTitle.json()).thenReturn(null);
        when(vsTitle.multi()).thenReturn(false);
        when(vsTitle.format()).thenReturn(null);

        Map<String, ValueSource> props = new LinkedHashMap<String, ValueSource>();
        props.put("title", vsTitle);
        when(rc.props()).thenReturn(props);
        when(rc.nodes()).thenReturn(java.util.Collections.emptyMap());
        when(rc.scopeJson()).thenReturn(null);

        JaywayJsonFinder finder = finderFor("{\"dataset\":{\"title\":\"Demo\"}}");
        ResourceMapper mapper = new ResourceMapper(rc, prefixes, "dcat:Dataset");

        Model model = mapper.build(finder);
        assertThat(model).isNotNull();

        // Verify RDF.type triple
        List<Statement> typeStmts =
                model.listStatements(
                                (Resource) null,
                                model.getProperty(
                                        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                model.getResource("http://www.w3.org/ns/dcat#Dataset"))
                        .toList();
        assertThat(typeStmts).hasSize(1);

        // Verify title literal with language
        List<Statement> titleStmts =
                model.listStatements(
                                (Resource) null,
                                model.getProperty("http://purl.org/dc/terms/title"),
                                (org.apache.jena.rdf.model.RDFNode) null)
                        .toList();
        assertThat(titleStmts).hasSize(1);
        assertThat(titleStmts.get(0).getObject().asLiteral().getLanguage()).isEqualTo("en");
        assertThat(titleStmts.get(0).getObject().asLiteral().getString()).isEqualTo("Demo");
    }

    @Test
    @DisplayName(
            "build() maps IRI object from JSON path with as='iri' and default (single) selection")
    void build_maps_iri_from_json() throws Exception {
        // Real Prefixes
        Map<String, String> ns = new LinkedHashMap<String, String>();
        ns.put("dcat", "http://www.w3.org/ns/dcat#");
        ns.put("dct", "http://purl.org/dc/terms/");
        Prefixes prefixes = new Prefixes(ns);

        // ResourceConfig + subject
        ResourceConfig rc = mock(ResourceConfig.class, RETURNS_DEEP_STUBS);
        when(rc.subject().iriConst()).thenReturn("http://example.org/id");
        when(rc.subject().iriTemplate()).thenReturn(null);
        when(rc.subject().iriFormat()).thenReturn(null);
        when(rc.subject().iriJson()).thenReturn(null);

        // ValueSource to map an IRI found in JSON
        ValueSource vsId = mock(ValueSource.class);
        when(vsId.predicate()).thenReturn("dct:identifier");
        when(vsId.as()).thenReturn("iri");
        when(vsId.constValue()).thenReturn(null);
        when(vsId.json()).thenReturn("$.dataset.identifier");
        when(vsId.multi()).thenReturn(false);
        when(vsId.format()).thenReturn(null);
        when(vsId.lang()).thenReturn(null);
        when(vsId.datatype()).thenReturn(null);
        when(vsId.map()).thenReturn(java.util.Collections.emptyMap());
        when(vsId.jsonPaths()).thenReturn(java.util.Collections.emptyList());

        Map<String, ValueSource> props = new LinkedHashMap<String, ValueSource>();
        props.put("identifier", vsId);
        when(rc.props()).thenReturn(props);
        when(rc.nodes()).thenReturn(java.util.Collections.emptyMap());
        when(rc.scopeJson()).thenReturn(null);

        JaywayJsonFinder finder =
                finderFor("{\"dataset\":{\"identifier\":\"http://example.org/id-iri\"}}");
        ResourceMapper mapper = new ResourceMapper(rc, prefixes, "dcat:Dataset");

        Model model = mapper.build(finder);
        assertThat(model).isNotNull();

        // Verify identifier as IRI object
        List<Statement> idStmts =
                model.listStatements(
                                (Resource) null,
                                model.getProperty("http://purl.org/dc/terms/identifier"),
                                (org.apache.jena.rdf.model.RDFNode) null)
                        .toList();
        assertThat(idStmts).hasSize(1);
        assertThat(idStmts.get(0).getObject().isResource()).isTrue();
        assertThat(idStmts.get(0).getObject().asResource().getURI())
                .isEqualTo("http://example.org/id-iri");
    }
}
