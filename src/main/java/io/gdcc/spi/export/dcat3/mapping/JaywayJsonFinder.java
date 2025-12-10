// JaywayJsonFinder.java
package io.gdcc.spi.export.dcat3.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JaywayJsonFinder {

    private final ReadContext ctx;

    public JaywayJsonFinder(JsonNode root) {
        // Configuration using Jackson provider & mapping provider
        Configuration config = Configuration.builder()
                                            .jsonProvider( new JacksonJsonProvider() )
                                            .mappingProvider( new JacksonMappingProvider() )
                                            .options( Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS )
                                            .build();

        // Parse the already-parsed Jackson tree directly. If you ever see provider mismatch,
        // switch to: JsonPath.using(config).parse(root.toString());
        this.ctx = JsonPath.using( config )
                           .parse( root );
    }

    /**
     * Execute a JSONPath and return a list of stringified values.
     * Path should start with '$' (JSONPath).
     */
    public List<String> list(String jsonPath) {
        if ( jsonPath == null || jsonPath.isEmpty() ) {
            return Collections.emptyList();
        }
        // Force list typing with TypeRef
        List<Object> raw = ctx.read( jsonPath, new TypeRef<List<Object>>() {
        } );
        if ( raw == null ) {
            return Collections.emptyList();
        }

        List<String> out = new ArrayList<>( raw.size() );
        for ( Object o : raw ) {
            if ( o == null ) {
                continue;
            }
            if ( o instanceof CharSequence ) {
                out.add( o.toString() );
            }
            else if ( o instanceof Number || o instanceof Boolean ) {
                out.add( String.valueOf( o ) );
            }
            else {
                // Complex nodes (maps, arrays, JsonNodes) -> toString()
                out.add( String.valueOf( o ) );
            }
        }
        return out;
    }
}
