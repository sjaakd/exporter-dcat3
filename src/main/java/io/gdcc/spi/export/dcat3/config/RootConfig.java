package io.gdcc.spi.export.dcat3.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RootConfig {
    public String outputFormat = "turtle";
    public boolean trace = false;
    public Map<String, String> prefixes = new LinkedHashMap<>();

    public static class Element {
        public String id;
        public String typeCurieOrIri;
        public String file;
    }

    public static class Relation {
        public String subjectElementId;
        public String predicateCurieOrIri;
        public String objectElementId;
        public String cardinality; // e.g. "1..n"
    }

    public List<Element> elements = new ArrayList<>();
    public List<Relation> relations = new ArrayList<>();
    /**
     * Directory of the root file; used to resolve element files relative to it
     */
    public Path baseDir;
}
