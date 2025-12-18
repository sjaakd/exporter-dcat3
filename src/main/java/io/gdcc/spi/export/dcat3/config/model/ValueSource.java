package io.gdcc.spi.export.dcat3.config.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Declarative mapping for a single property value. */
public class ValueSource {
    /** CURIE/IRI of the predicate to emit. */
    public String predicate;

    /** How to emit the object: "literal" | "iri" | "node-ref". */
    public String as;

    // Literal metadata
    public String lang; // optional language tag
    public String datatype; // optional datatype IRI (CURIE allowed via Prefixes)

    // Single-source selector
    public String json; // JSONPath (scoped or root via $$ convention)
    public String constValue; // constant value

    // Multi-source selectors: ordered list json.1, json.2, ...
    public List<String> jsonPaths = new ArrayList<>();

    // Node reference (for as=node-ref)
    public String nodeRef;

    // Multiplicity: if true and json resolves to multiple values, emit all
    public boolean multi;

    // Conditional emission (future use)
    public String when;

    // Mapping table (optional): map.raw -> mapped
    public Map<String, String> map = new LinkedHashMap<>();

    // formatting template. Supports ${value}, ${1}, ${2}, ... and
    // inline JSONPath placeholders like ${$.path} or ${$$.path}.
    public String format;
}
