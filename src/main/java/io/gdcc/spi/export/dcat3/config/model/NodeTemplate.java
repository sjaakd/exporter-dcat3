package io.gdcc.spi.export.dcat3.config.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class NodeTemplate {
    public String id;
    public String kind = "bnode"; // bnode|iri
    public String iriConst;
    public String type;           // CURIE or IRI
    public Map<String, ValueSource> props = new LinkedHashMap<>();
}
