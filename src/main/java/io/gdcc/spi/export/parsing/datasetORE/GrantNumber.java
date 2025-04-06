package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GrantNumber(

    @JsonProperty( "citation:grantNumberAgency" )
    String citationTimeGrantNumberAgency,

    @JsonProperty( "citation:grantNumberValue" )
    String citationGrantNumberValue
) {
}
