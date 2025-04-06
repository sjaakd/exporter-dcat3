package io.gdcc.spi.export.parsing.dataCiteXml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class NameIdentifier {
    @JacksonXmlProperty( isAttribute = true )
    private String schemeURI;

    @JacksonXmlProperty( isAttribute = true )
    private String nameIdentifierScheme;

    @JacksonXmlText
    private String value;

    public String getSchemeURI() {
        return schemeURI;
    }

    public void setSchemeURI(String schemeURI) {
        this.schemeURI = schemeURI;
    }

    public String getNameIdentifierScheme() {
        return nameIdentifierScheme;
    }

    public void setNameIdentifierScheme(String nameIdentifierScheme) {
        this.nameIdentifierScheme = nameIdentifierScheme;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}