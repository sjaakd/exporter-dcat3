package io.gdcc.spi.export.parsing.datasetFileDetails;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DatasetFileDetail(

    @JsonProperty( "id" )
    int id,

    String originalFileName,

    String storageIdentifier,

    String description,

    int filesize,

    @JsonProperty( "UNF" )
    String uNF,

    String creationDate,

    boolean fileAccessRequest,

    String friendlyType,

    String persistentId,

    List<DataTablesItem> dataTables,

    String filename,

    int fileMetadataId,

    String originalFileFormat,

    int originalFileSize,

    boolean restricted,

    String originalFormatLabel,

    Checksum checksum,

    boolean tabularData,

    String contentType,

    String publicationDate,

    List<Object> varGroups,

    int rootDataFileId,

    String md5
) {
}