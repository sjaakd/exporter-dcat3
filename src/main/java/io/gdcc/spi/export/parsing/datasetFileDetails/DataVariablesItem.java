package io.gdcc.spi.export.parsing.datasetFileDetails;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DataVariablesItem(

    @JsonProperty( "variableIntervalType" )
    String variableIntervalType,

    @JsonProperty( "summaryStatistics" )
    SummaryStatistics summaryStatistics,

    @JsonProperty( "variableFormatType" )
    String variableFormatType,

    @JsonProperty( "name" )
    String name,

    @JsonProperty( "isOrderedCategorical" )
    boolean isOrderedCategorical,

    @JsonProperty( "fileOrder" )
    int fileOrder,

    @JsonProperty( "id" )
    int id,

    @JsonProperty( "label" )
    String label,

    @JsonProperty( "variableMetadata" )
    List<Object> variableMetadata,

    @JsonProperty( "UNF" )
    String uNF,

    @JsonProperty( "weighted" )
    boolean weighted,

    @JsonProperty( "variableCategories" )
    List<VariableCategoriesItem> variableCategories,

    @JsonProperty( "format" )
    String format
) {
}