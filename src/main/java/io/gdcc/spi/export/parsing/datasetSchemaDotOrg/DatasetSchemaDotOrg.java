package io.gdcc.spi.export.parsing.datasetSchemaDotOrg;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DatasetSchemaDotOrg(

	@JsonProperty("funder")
	List<FunderItem> funder,

	@JsonProperty("identifier")
	String identifier,

	@JsonProperty("creator")
	List<CreatorItem> creator,

	@JsonProperty("keywords")
	List<String> keywords,

	@JsonProperty("citation")
	List<CitationItem> citation,

	@JsonProperty("@type")
	String type,

	@JsonProperty("author")
	List<AuthorItem> author,

	@JsonProperty("description")
	String description,

	@JsonProperty("dateModified")
	String dateModified,

	@JsonProperty("spatialCoverage")
	List<String> spatialCoverage,

	@JsonProperty("distribution")
	List<DistributionItem> distribution,

	@JsonProperty("@context")
	String context,

	@JsonProperty("version")
	String version,

	@JsonProperty("datePublished")
	String datePublished,

	@JsonProperty("license")
	String license,

	@JsonProperty("temporalCoverage")
	List<String> temporalCoverage,

	@JsonProperty("provider")
	Provider provider,

	@JsonProperty("includedInDataCatalog")
	IncludedInDataCatalog includedInDataCatalog,

	@JsonProperty("name")
	String name,

	@JsonProperty("publisher")
	Publisher publisher,

	@JsonProperty("@id")
	String id
) {
}