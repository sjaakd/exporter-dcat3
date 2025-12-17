package io.gdcc.spi.export.dcat3.config.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ValueSource {
    public String as = "literal"; // literal|iri|bnode|node-ref
    public String predicate;      // CURIE or absolute IRI
    public String lang;           // for literals
    public String datatype;       // absolute IRI
    public String json;           // JSONPath
    public String constValue;     // const
    public String nodeRef;        // node id (for node-ref)
    public boolean multi = false; // expand arrays or split by comma
    public String format;
    public String when;           // optional guard expression (future)
    public Map<String, String> map = new LinkedHashMap<>(); // value mapping
}
