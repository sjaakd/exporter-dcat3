package io.gdcc.spi.export.parsing.dataCiteXml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Contributor {

    @JacksonXmlProperty( isAttribute = true )
    private String contributorType;
    private String contributorName;

    public String getContributorType() {
        return contributorType;
    }

    public void setContributorType(String contributorType) {
        this.contributorType = contributorType;
    }

    public String getContributorName() {
        return contributorName;
    }

    public void setContributorName(String contributorName) {
        this.contributorName = contributorName;
    }
}
