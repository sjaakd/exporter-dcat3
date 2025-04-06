package io.gdcc.spi.export.parsing.datasetFileDetails;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DataTablesItem(

    @JsonProperty( "caseQuantity" )
    int caseQuantity,

    @JsonProperty( "dataVariables" )
    List<DataVariablesItem> dataVariables,

    @JsonProperty( "varQuantity" )
    int varQuantity,

    @JsonProperty( "UNF" )
    String uNF
) {
}