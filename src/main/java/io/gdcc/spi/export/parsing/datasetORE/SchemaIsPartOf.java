package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SchemaIsPartOf(

    @JsonProperty( "schema:description" )
    String schemaDescription,

    @JsonProperty( "@id" )
    String id,

    @JsonProperty( "schema:name" )
    String schemaName,

    @JsonProperty( "schema:isPartOf" )
    SchemaIsPartOf schemaIsPartOf
) {
}