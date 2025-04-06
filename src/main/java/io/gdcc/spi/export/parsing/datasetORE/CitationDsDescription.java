package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CitationDsDescription(

    @JsonProperty( "citation:dsDescriptionValue" )
    String citationDsDescriptionValue
) {
}