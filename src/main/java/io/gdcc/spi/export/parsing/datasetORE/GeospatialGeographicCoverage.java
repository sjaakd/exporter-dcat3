package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GeospatialGeographicCoverage(
    @JsonProperty( "geospatial:geographicCoverage" )
    String geographicCoverage,

    @JsonProperty( "geospatial:country" )
    String country,

    @JsonProperty( "geospatial:state" )
    String state,

    @JsonProperty( "geospatial:city" )
    String city,

    @JsonProperty( "geospatial:otherGeographicCoverage" )
    String otherGeographicCoverage
) {
}
