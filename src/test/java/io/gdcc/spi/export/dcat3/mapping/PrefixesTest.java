package io.gdcc.spi.export.dcat3.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PrefixesTest {

    @Test
    @DisplayName("expand() resolves CURIEs to full IRIs; leaves absolute IRIs unchanged")
    void expand_resolves_curie() {
        Map<String, String> ns = new LinkedHashMap<String, String>();
        ns.put("dcat", "http://www.w3.org/ns/dcat#");
        ns.put("dct", "http://purl.org/dc/terms/");
        Prefixes prefixes = new Prefixes(ns);

        String dcatDataset = prefixes.expand("dcat:Dataset");
        assertThat(dcatDataset).isEqualTo("http://www.w3.org/ns/dcat#Dataset");

        String absolute = prefixes.expand("http://example.org/x");
        assertThat(absolute).isEqualTo("http://example.org/x");

        String nullInput = prefixes.expand(null);
        assertThat(nullInput).isNull();
    }

    @Test
    @DisplayName("jena() exposes prefix mapping usable by Jena Model")
    void jena_prefix_mapping() {
        Map<String, String> ns = new LinkedHashMap<String, String>();
        ns.put("dcat", "http://www.w3.org/ns/dcat#");
        ns.put("dct", "http://purl.org/dc/terms/");
        Prefixes prefixes = new Prefixes(ns);

        String uri = prefixes.jena().getNsPrefixURI("dcat");
        assertThat(uri).isEqualTo("http://www.w3.org/ns/dcat#");
    }
}
