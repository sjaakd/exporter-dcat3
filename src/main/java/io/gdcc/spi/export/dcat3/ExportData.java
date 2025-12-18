package io.gdcc.spi.export.dcat3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.gdcc.spi.export.ExportDataProvider;

public class ExportData {

    private JsonNode datasetJson; // native JSON tree
    private JsonNode datasetORE; // ORE JSON tree
    private JsonNode datasetFileDetails; // array JSON tree
    private JsonNode datasetSchemaDotOrg; // schema.org JSON tree
    private JsonNode dataCiteXml; // DataCite as JSON tree (converted from XML

    public ExportData(
            JsonNode datasetJson,
            JsonNode datasetORE,
            JsonNode datasetFileDetails,
            JsonNode datasetSchemaDotOrg,
            JsonNode dataCiteXml) {
        this.datasetJson = datasetJson;
        this.datasetORE = datasetORE;
        this.datasetFileDetails = datasetFileDetails;
        this.datasetSchemaDotOrg = datasetSchemaDotOrg;
        this.dataCiteXml = dataCiteXml;
    }

    public static ExportDataBuilder builder() {
        return new ExportDataBuilder();
    }

    public JsonNode getDatasetJson() {
        return datasetJson;
    }

    public JsonNode getDatasetORE() {
        return datasetORE;
    }

    public JsonNode getDatasetFileDetails() {
        return datasetFileDetails;
    }

    public JsonNode getDatasetSchemaDotOrg() {
        return datasetSchemaDotOrg;
    }

    public JsonNode getDataCiteXml() {
        return dataCiteXml;
    }

    public static class ExportDataBuilder {

        private ExportDataProvider provider;

        public ExportDataBuilder provider(ExportDataProvider provider) {
            this.provider = provider;
            return this;
        }

        public ExportData build() {
            ObjectMapper jsonMapper = new ObjectMapper();
            jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                // provider already returns JsonObject/JsonArray for JSON sources
                // read them as JsonNode via a single roundtrip (or better — convert directly)
                JsonNode datasetJson = jsonMapper.readTree(provider.getDatasetJson().toString());
                JsonNode datasetORE = jsonMapper.readTree(provider.getDatasetORE().toString());
                JsonNode datasetFileDetails =
                        jsonMapper.readTree(provider.getDatasetFileDetails().toString());
                JsonNode datasetSchemaDotOrg =
                        jsonMapper.readTree(provider.getDatasetSchemaDotOrg().toString());

                // DataCite XML → JsonNode once
                JsonNode dataCiteXml = xmlMapper.readTree(provider.getDataCiteXml());

                // Now construct ExportData with JsonNodes
                return new ExportData(
                        datasetJson,
                        datasetORE,
                        datasetFileDetails,
                        datasetSchemaDotOrg,
                        dataCiteXml);

            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
