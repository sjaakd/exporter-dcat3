package io.gdcc.spi.export.parsing.datasetORE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OreDescribes(

    @JsonProperty( "@id" )
    String id,

    @JsonProperty( "schema:version" )
    String schemaVersion,

    @JsonProperty( "schema:includedInDataCatalog" )
    String schemaIncludedInDataCatalog,

    @JsonProperty( "author" )
    @JsonFormat( with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY )
    List<Author> authors,

    @JsonProperty( "subject" )
    @JsonFormat( with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY )
    List<String> subjects,

    @JsonProperty( "@type" )
    @JsonFormat( with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY )
    List<String> types,

    @JsonProperty( "citation:keyword" )
    @JsonFormat( with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY )
    List<CitationKeyword> keywords,

    @JsonProperty( "contributor" )
    @JsonFormat( with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY )
    List<Contributor> contributors,

    TimePeriodCovered timePeriodCovered,

    GrantNumber grantNumber,

    @JsonProperty( "geospatial:geographicCoverage" )
    @JsonFormat( with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY )
    List<GeospatialGeographicCoverage> geospatialGeographicCoverage,

    @JsonProperty( "schema:hasPart" )
    @JsonFormat( with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY )
    List<Object> schemaHasPart,

    @JsonProperty( "schema:datePublished" )
    String schemaDatePublished,

    String title,

    @JsonProperty( "schema:license" )
    String schemaLicense,

    @JsonProperty( "citation:depositor" )
    String citationDepositor,

    @JsonProperty( "ore:aggregates" )
    List<Object> oreAggregates,

    @JsonProperty( "schema:dateModified" )
    String schemaDateModified,

    String dateOfDeposit,

    @JsonProperty( "citation:datasetContact" )
    CitationDatasetContact citationDatasetContact,

    @JsonProperty( "citation:dsDescription" )
    CitationDsDescription citationDsDescription,

    @JsonProperty( "schema:creativeWorkStatus" )
    String schemaCreativeWorkStatus,

    @JsonProperty( "schema:name" )
    String schemaName,

    @JsonProperty( "dvcore:fileTermsOfAccess" )
    DvcoreFileTermsOfAccess dvcoreFileTermsOfAccess,

    @JsonProperty( "schema:isPartOf" )
    SchemaIsPartOf schemaIsPartOf,

    Publication publication
) {
}