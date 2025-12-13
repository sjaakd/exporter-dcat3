// JaywayJsonFinder.java
package io.gdcc.spi.export.dcat3.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JaywayJsonFinder {

    private static final Logger logger = Logger.getLogger( JaywayJsonFinder.class.getCanonicalName());

    private final ReadContext ctx;

    public JaywayJsonFinder(JsonNode root) {
        // Configuration using Jackson provider & mapping provider
        Configuration config = Configuration.builder()
                                            .jsonProvider( new JacksonJsonProvider() )
                                            .mappingProvider( new JacksonMappingProvider() )
                                            .options( Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS )
                                            .build();

        // note: root.toString() creates a JSON string representation of the JsonNode that is required to make this parser work
        this.ctx = JsonPath.using( config ).parse( root.toString() );
    }

    /**
     * Execute a JSONPath and return a list of stringified values.
     * Path should start with '$' (JSONPath).
     */
    public List<String> list(String jsonPath) {
        if ( jsonPath == null || jsonPath.isEmpty() ) {
            logger.warning( "jsonPath is null or empty" );
            return Collections.emptyList();
        }
        // Force list typing with TypeRef
        List<Object> raw = ctx.read( jsonPath, new TypeRef<List<Object>>() { } );
        if ( raw == null || raw.isEmpty() ) {
            logger.warning( "cannot resolve json path: " + jsonPath );
            return Collections.emptyList();
        }

        List<String> out = new ArrayList<>( raw.size() );
        for ( Object object : raw ) {
            if ( object == null ) {
                continue;
            }
            if ( object instanceof CharSequence ) {
                out.add( object.toString() );
            }
            else if ( object instanceof Number || object instanceof Boolean ) {
                out.add( String.valueOf( object ) );
            }
            else {
                // Complex nodes (maps, arrays, JsonNodes) -> toString()
                out.add( String.valueOf( object ) );
            }
        }
        return out;
    }
}
