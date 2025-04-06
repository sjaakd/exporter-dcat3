package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DatasetORE(

    @JsonProperty( "@id" )
    String id,

    @JsonProperty( "dcterms:creator" )
    String dctermsCreator,

    @JsonProperty( "@type" )
    String type,

    @JsonProperty( "schema:additionalType" )
    String schemaAdditionalType,

    @JsonProperty( "dcterms:modified" )
    String dctermsModified,

    @JsonProperty( "@context" )
    Context context,

    @JsonProperty( "dvcore:generatedBy" )
    DvcoreGeneratedBy dvcoreGeneratedBy,

    @JsonProperty( "ore:describes" )
    OreDescribes oreDescribes
) {
}