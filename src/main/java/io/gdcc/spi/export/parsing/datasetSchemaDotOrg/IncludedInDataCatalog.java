package io.gdcc.spi.export.parsing.datasetSchemaDotOrg;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IncludedInDataCatalog(

	@JsonProperty("@type")
	String type,

	@JsonProperty("name")
	String name,

	@JsonProperty("url")
	String url
) {
}