package io.gdcc.spi.export.dcat3.config.loader;

import io.gdcc.spi.export.dcat3.config.model.ValueSource;

public final class Util {

    private Util() {
        // deliberately empty
    }

    public static void applyValue(ValueSource valueSource, String keyTail, String value) {
        switch ( keyTail ) {
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
                valueSource.json = value;
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
            default:
                if ( keyTail.startsWith( "map." ) ) {
                    String k = keyTail.substring( "map.".length() );
                    valueSource.map.put( k, value );
                }
        }
    }
}
