package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Contributor(
    @JsonProperty( "citation:contributorType" )
    String citationContributorType,
    @JsonProperty( "citation:contributorName" )
    String citationContributorName
) {
}
