package io.gdcc.spi.export.parsing.datasetSchemaDotOrg;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreatorItem(

	@JsonProperty("affiliation")
	Affiliation affiliation,

	@JsonProperty("@type")
	String type,

	@JsonProperty("name")
	String name,

	@JsonProperty("identifier")
	String identifier,

	@JsonProperty("givenName")
	String givenName,

	@JsonProperty("familyName")
	String familyName,

	@JsonProperty("@id")
	String id,

	@JsonProperty("sameAs")
	String sameAs
) {
}