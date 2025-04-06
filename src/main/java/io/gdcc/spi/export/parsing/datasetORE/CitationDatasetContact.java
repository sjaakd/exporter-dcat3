package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CitationDatasetContact(

    @JsonProperty( "citation:datasetContactName" )
    String datasetContactName,

    @JsonProperty( "citation:datasetContactEmail" )
    String citationDatasetContactEmail
) {
}