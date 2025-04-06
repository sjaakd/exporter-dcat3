package io.gdcc.spi.export.parsing.datasetFileDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VariableCategoriesItem(

    @JsonProperty( "isMissing" )
    boolean isMissing,

    @JsonProperty( "label" )
    String label,

    @JsonProperty( "value" )
    String value,

    @JsonProperty( "frequency" )
    Object frequency
) {
}