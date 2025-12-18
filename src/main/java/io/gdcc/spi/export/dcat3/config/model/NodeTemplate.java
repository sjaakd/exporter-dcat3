package io.gdcc.spi.export.dcat3.config.model;

import java.util.Map;

/**
 * @param kind bnode | iri
 * @param type CURIE or IRI
 */
public record NodeTemplate(
        String id, String kind, String iriConst, String type, Map<String, ValueSource> props) {}
