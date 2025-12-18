package io.gdcc.spi.export.dcat3.config.model;

/**
 * @param iriConst Constant IRI, if provided.
 * @param iriTemplate A template IRI, currently used as-is (optional).
 * @param iriJson JSONPath to read the subject IRI value (or id) from input.
 * @param iriFormat Optional format string to mint an absolute IRI from a JSONPath value.
 */
public record Subject(String iriConst, String iriTemplate, String iriJson, String iriFormat) {}
