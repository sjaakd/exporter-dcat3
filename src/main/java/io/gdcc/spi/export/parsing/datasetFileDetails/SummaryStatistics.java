package io.gdcc.spi.export.parsing.datasetFileDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SummaryStatistics(

    @JsonProperty( "mode" )
    String mode,

    @JsonProperty( "medn" )
    String medn,

    @JsonProperty( "invd" )
    String invd,

    @JsonProperty( "min" )
    String min,

    @JsonProperty( "vald" )
    String vald,

    @JsonProperty( "max" )
    String max,

    @JsonProperty( "mean" )
    String mean,

    @JsonProperty( "stdev" )
    String stdev
) {
}