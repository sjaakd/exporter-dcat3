package io.gdcc.spi.export.parsing.datasetJson;

public record File(

    boolean restricted,
    String directoryLabel,
    String description,
    DataFile dataFile,
    String label,
    int version,
    int datasetVersionId
) {
}