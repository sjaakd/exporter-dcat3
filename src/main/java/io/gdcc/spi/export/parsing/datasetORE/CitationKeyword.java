package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CitationKeyword(
    @JsonProperty( "citation:keyword" )
    String citationKeyword,
    @JsonProperty( "citation:keywordValue" )
    String citationKeywordValue
) {
}
