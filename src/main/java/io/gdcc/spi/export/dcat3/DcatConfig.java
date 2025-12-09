package io.gdcc.spi.export.dcat3;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "dcat")
public interface DcatConfig {
    Catalog catalog();
    Dataset dataset();

    interface Catalog {
        String uri();               // constant or expression
        Field title();              // pointer/path/const
        Field description();
        Contact contact();          // mostly consts
        Publisher publisher();
    }
    interface Dataset {
        String uri();
        Field title();
        Field description();
        Themes themes();            // example of multi-mapping
    }

    interface Field {
        String pointer();           // e.g. /datasetORE/oreDescribes/schemaIsPartOf/schemaName
        String path();              // e.g. datasetORE.oreDescribes.schemaIsPartOf.schemaName
        String constValue();        // e.g. "Geological Survey of the Netherlands"
        String lang();              // e.g. "nl"
    }

    interface Contact {
        String fnNl();
        String fnEn();
        String email();
        String org();
    }

    interface Publisher {
        String nameNl();
        String nameEn();
        String type(); // e.g. ROR URI
    }

    interface Themes {
        String pointer();           // points to array of theme identifiers
        String resolver();          // optional: name of a resolver (see §3) to turn ids->URIs
    }
}
