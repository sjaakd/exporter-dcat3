package io.gdcc.spi.export.parsing.datasetJson;

import java.util.List;
import java.util.Map;

public record DatasetVersion(

    int id,
    String versionState,
    String releaseTime,
    String citation,
    String storageIdentifier,
    boolean fileAccessRequest,
    int versionNumber,
    String citationDate,
    License license,
    String createTime,
    String latestVersionPublishingState,
    int datasetId,
    List<File> files,
    String datasetPersistentId,
    int versionMinorNumber,
    Map<String, MetadataBlock> metadataBlocks,
    String publicationDate,
    String lastUpdateTime
) {
}