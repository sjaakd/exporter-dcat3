package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Author(

    @JsonProperty( "citation:authorAffiliation" )
    String citationAuthorAffiliation,

    @JsonProperty( "citation:authorName" )
    String citationAuthorName,

    String authorIdentifierScheme,
    String authorIdentifier
) {
}