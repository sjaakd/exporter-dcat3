package io.gdcc.spi.export.parsing.datasetJson;

public record DatasetJson(

    int id,
    String identifier,
    String protocol,
    DatasetVersion datasetVersion,
    String storageIdentifier,
    String authority,
    String publisher,
    String datasetType,
    String separator,
    String publicationDate,
    String persistentUrl
) {
}