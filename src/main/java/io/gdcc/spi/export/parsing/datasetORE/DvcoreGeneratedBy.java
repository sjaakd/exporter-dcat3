package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DvcoreGeneratedBy(

    @JsonProperty( "schema:version" )
    String schemaVersion,

    @JsonProperty( "schema:url" )
    String schemaUrl,

    @JsonProperty( "@type" )
    String type,

    @JsonProperty( "schema:name" )
    String schemaName
) {
}