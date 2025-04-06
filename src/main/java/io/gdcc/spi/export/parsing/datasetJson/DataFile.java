package io.gdcc.spi.export.parsing.datasetJson;

public record DataFile(

    int id,
    String storageIdentifier,
    String description,
    int filesize,
    String creationDate,
    boolean fileAccessRequest,
    String friendlyType,
    String persistentId,
    String filename,
    Checksum checksum,
    boolean tabularData,
    String contentType,
    String publicationDate,
    int rootDataFileId,
    String md5
) {
}