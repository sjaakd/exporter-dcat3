package io.gdcc.spi.export.dcat3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.gdcc.spi.export.ExportDataProvider;

/**
 * @param datasetJson native JSON tree
 * @param datasetORE ORE JSON tree
 * @param datasetFileDetails array JSON tree
 * @param datasetSchemaDotOrg schema.org JSON tree
 * @param dataCiteXml DataCite as JSON tree (converted from XML
 */
public record ExportData(
        JsonNode datasetJson,
        JsonNode datasetORE,
        JsonNode datasetFileDetails,
        JsonNode datasetSchemaDotOrg,
        JsonNode dataCiteXml) {

    public static ExportDataBuilder builder() {
        return new ExportDataBuilder();
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
