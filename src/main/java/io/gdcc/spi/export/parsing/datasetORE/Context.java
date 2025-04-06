package io.gdcc.spi.export.parsing.datasetORE;

public record Context(
    String author,
    String authorIdentifier,
    String authorIdentifierScheme,
    String citation,
    String contributor,
    String dateOfDeposit,
    String dcterms,
    String dvcore,
    String geospatial,
    String grantNumber,
    String ore,
    String publication,
    String publicationCitation,
    String publicationIDNumber,
    String publicationIDType,
    String publicationURL,
    String schema,
    String subject,
    String timePeriodCovered,
    String title
) {
}