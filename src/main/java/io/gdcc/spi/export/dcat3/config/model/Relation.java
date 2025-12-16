package io.gdcc.spi.export.dcat3.config.model;

public class Relation {
    public String subjectElementId;
    public String predicateCurieOrIri;
    public String objectElementId;
    public String cardinality; // e.g. "1..n"
}
