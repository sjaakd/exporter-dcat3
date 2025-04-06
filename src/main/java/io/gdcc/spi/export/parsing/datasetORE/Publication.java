package io.gdcc.spi.export.parsing.datasetORE;

public record Publication(
    String publicationCitation,
    String publicationIDType,
    String publicationIDNumber,
    String publicationURL
) {
}