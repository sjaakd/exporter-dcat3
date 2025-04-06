package io.gdcc.spi.export.parsing.datasetJson;

public record License(

    String rightsIdentifierScheme,
    String schemeUri,
    String name,
    String languageCode,
    String uri,
    String iconUri,
    String rightsIdentifier
) {
}