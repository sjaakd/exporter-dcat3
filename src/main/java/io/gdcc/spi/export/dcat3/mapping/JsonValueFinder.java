package io.gdcc.spi.export.dcat3.mapping;


// JsonValueFinder.java

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class JsonValueFinder {
    private final JsonNode root;

    public JsonValueFinder(JsonNode root) { this.root = root; }

    // Dotted path: a.b.c ; returns 0..n values (arrays flattened)
    public List<String> findByDotted(String dottedPath) {
        List<String> out = new ArrayList<>();
        JsonNode cur = root;
        for (String p : dottedPath.split("\\.")) {
            if (cur == null) break;
            cur = cur.get(p);
        }
        if (cur == null || cur.isNull()) return out;
        if (cur.isArray()) {
            cur.forEach(n -> out.add(n.isValueNode() ? n.asText() : n.toString()));
        } else {
            out.add(cur.isValueNode() ? cur.asText() : cur.toString());
        }
        return out;
    }

    // Placeholder for JSONPath (Jayway):
    // public List<String> findByJsonPath(String jsonPath) { ... }
}
