package io.gdcc.spi.export.parsing.datasetORE;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DvcoreFileTermsOfAccess(

    @JsonProperty( "dvcore:fileRequestAccess" )
    boolean dvcoreFileRequestAccess
) {
}