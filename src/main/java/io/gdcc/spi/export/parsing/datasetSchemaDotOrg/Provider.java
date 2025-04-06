package io.gdcc.spi.export.parsing.datasetSchemaDotOrg;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Provider(

	@JsonProperty("@type")
	String type,

	@JsonProperty("name")
	String name
) {
}