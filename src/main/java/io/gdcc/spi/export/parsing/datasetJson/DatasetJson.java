package io.gdcc.spi.export.parsing.datasetJson;

public record DatasetJson(

    int id,
    String identifier,
    String persistentUrl,
    String protocol,
    String authority,
    String separator,
    String publisher,
    String publicationDate,
    String storageIdentifier,
    String datasetType,
    DatasetVersion datasetVersion
) {
}