package io.gdcc.spi.export.parsing.datasetSchemaDotOrg;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CitationItem(

	@JsonProperty("identifier")
	String identifier,

	@JsonProperty("@type")
	String type,

	@JsonProperty("name")
	String name,

	@JsonProperty("@id")
	String id,

	@JsonProperty("url")
	String url
) {
}