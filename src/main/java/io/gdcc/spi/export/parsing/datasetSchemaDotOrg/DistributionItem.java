package io.gdcc.spi.export.parsing.datasetSchemaDotOrg;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DistributionItem(

	@JsonProperty("contentUrl")
	String contentUrl,

	@JsonProperty("@type")
	String type,

	@JsonProperty("contentSize")
	int contentSize,

	@JsonProperty("name")
	String name,

	@JsonProperty("encodingFormat")
	String encodingFormat,

	@JsonProperty("description")
	String description
) {
}