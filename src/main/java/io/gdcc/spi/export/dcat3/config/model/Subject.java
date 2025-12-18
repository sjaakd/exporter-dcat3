package io.gdcc.spi.export.dcat3.config.model;

public class Subject {

    /** Constant IRI, if provided. */
    public String iriConst;

    /** A template IRI, currently used as-is (optional). */
    public String iriTemplate;

    /** JSONPath to read the subject IRI value (or id) from input. */
    public String iriJson;

    /**
     * Optional format string to mint an absolute IRI from a JSONPath value, e.g.,
     * "https://example.org/distribution/${value}".
     */
    public String iriFormat;
}
