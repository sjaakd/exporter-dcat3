package io.gdcc.spi.export.dcat3.mapping;

// Prefixes.java

import java.util.Map;

import org.apache.jena.shared.PrefixMapping;

public class Prefixes {
    private final PrefixMapping pm;

    public Prefixes(Map<String, String> prefixes) {
        this.pm = PrefixMapping.Factory.create();
        prefixes.forEach( pm::setNsPrefix );
    }

    public String expand(String curieOrIri) {
        if ( curieOrIri == null ) {
            return null;
        }
        if ( curieOrIri.contains( ":" ) && !curieOrIri.startsWith( "http" ) ) {
            return pm.expandPrefix( curieOrIri );
        }
        return curieOrIri;
    }

    public PrefixMapping jena() {
        return pm;
    }
}
