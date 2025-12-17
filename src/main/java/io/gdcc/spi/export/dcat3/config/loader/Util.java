
package io.gdcc.spi.export.dcat3.config.loader;

import io.gdcc.spi.export.dcat3.config.model.ValueSource;

public final class Util {
    private Util() {}

    public static void applyValue(ValueSource valueSource, String keyTail, String value) {
        switch (keyTail) {
            case "predicate":
                valueSource.predicate = value;
                break;
            case "as":
                valueSource.as = value;
                break;
            case "lang":
                valueSource.lang = value;
                break;
            case "datatype":
                valueSource.datatype = value;
                break;
            case "json":
                valueSource.json = value; // legacy single source
                break;
            case "const":
                valueSource.constValue = value;
                break;
            case "node":
                valueSource.nodeRef = value;
                break;
            case "multi":
                valueSource.multi = Boolean.parseBoolean( value );
                break;
            case "when":
                valueSource.when = value;
                break;
            case "format": // NEW: formatter with placeholders
                valueSource.format = value;
                break;
            default:
                if ( keyTail.startsWith( "json." ) ) {
                    // Supports json.1, json.2, ...; keep declaration order
                    valueSource.jsonPaths.add( value );
                }
                else if ( keyTail.startsWith( "map." ) ) {
                    String k = keyTail.substring( "map.".length() );
                    valueSource.map.put( k, value );
                }
        }
    }
}
