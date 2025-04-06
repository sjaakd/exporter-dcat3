package io.gdcc.spi.export.parsing.dataCiteXml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ResourceType {
    @JacksonXmlProperty( isAttribute = true )
    private String resourceTypeGeneral;

    public String getResourceTypeGeneral() {
        return resourceTypeGeneral;
    }

    public void setResourceTypeGeneral(String resourceTypeGeneral) {
        this.resourceTypeGeneral = resourceTypeGeneral;
    }
}