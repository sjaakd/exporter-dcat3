package io.gdcc.spi.export.dcat3.config;



import java.util.*;

public class MappingModel {
    public static class Subject {
        public String iriConst;
        public String iriTemplate;   // supports {var} from json
        public String iriJson;       // JSONPath
    }

    public static class ValueSource {
        public String as = "literal"; // literal|iri|bnode|node-ref
        public String predicate;      // CURIE or absolute IRI
        public String lang;           // for literals
        public String datatype;       // absolute IRI
        public String json;           // JSONPath
        public String constValue;     // const
        public String nodeRef;        // node id (for node-ref)
        public boolean multi = false; // expand arrays or split by comma
        public String when;           // optional guard expression (future)
        public Map<String,String> map = new LinkedHashMap<>(); // value mapping
    }

    public static class NodeTemplate {
        public String id;
        public String kind = "bnode"; // bnode|iri
        public String iriConst;
        public String type;           // CURIE or IRI
        public Map<String, ValueSource> props = new LinkedHashMap<>();
    }

    public static class Config {
        public Subject subject = new Subject();
        public Map<String,String> prefixes = new LinkedHashMap<>();
        public Map<String,ValueSource> props = new LinkedHashMap<>();
        public Map<String,NodeTemplate> nodes = new LinkedHashMap<>();
    }
}
