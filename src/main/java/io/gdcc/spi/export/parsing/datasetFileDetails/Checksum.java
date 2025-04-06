package io.gdcc.spi.export.parsing.datasetFileDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Checksum(

    @JsonProperty( "type" )
    String type,

    @JsonProperty( "value" )
    String value
) {
}