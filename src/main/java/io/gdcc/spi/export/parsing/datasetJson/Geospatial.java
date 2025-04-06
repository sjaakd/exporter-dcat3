package io.gdcc.spi.export.parsing.datasetJson;

import java.util.List;

public record Geospatial(

    String displayName,
    String name,
    List<Field> fields
) {
}