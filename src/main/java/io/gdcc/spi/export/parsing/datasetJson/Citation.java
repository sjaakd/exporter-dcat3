package io.gdcc.spi.export.parsing.datasetJson;

import java.util.List;

public record Citation(

    String displayName,
    String name,
    List<Field> fields
) {
}