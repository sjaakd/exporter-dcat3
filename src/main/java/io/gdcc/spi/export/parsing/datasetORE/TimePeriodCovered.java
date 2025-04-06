package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TimePeriodCovered(
    @JsonProperty( "citation:timePeriodCoveredStart" )
    String citationTimePeriodCoveredStart,
    @JsonProperty( "citation:timePeriodCoveredEnd" )
    String citationTimePeriodCoveredEnd
) {
}
