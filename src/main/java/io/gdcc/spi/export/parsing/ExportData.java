package io.gdcc.spi.export.parsing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.parsing.dataCiteXml.DataCiteXml;
import io.gdcc.spi.export.parsing.datasetFileDetails.DatasetFileDetail;
import io.gdcc.spi.export.parsing.datasetJson.DatasetJson;
import io.gdcc.spi.export.parsing.datasetJson.Field;
import io.gdcc.spi.export.parsing.datasetORE.DatasetORE;
import io.gdcc.spi.export.parsing.datasetSchemaDotOrg.DatasetSchemaDotOrg;

public class ExportData {

    private final DatasetJson datasetJson;
    private final DatasetORE datasetORE;
    private final List<DatasetFileDetail> datasetFileDetails;
    private final DatasetSchemaDotOrg datasetSchemaDotOrg;
    private final DataCiteXml dataCiteDataCiteXml;

    public ExportData(DatasetJson datasetJson, DatasetORE datasetORE, List<DatasetFileDetail> datasetFileDetails, DatasetSchemaDotOrg datasetSchemaDotOrg, DataCiteXml dataCiteDataCiteXml) {
        this.datasetJson = datasetJson;
        this.datasetORE = datasetORE;
        this.datasetFileDetails = datasetFileDetails;
        this.datasetSchemaDotOrg = datasetSchemaDotOrg;
        this.dataCiteDataCiteXml = dataCiteDataCiteXml;
    }

    public static ExportDataBuilder builder() {
        return new ExportDataBuilder();
    }

    public DatasetJson getDatasetJson() {
        return datasetJson;
    }

    public DatasetORE getDatasetORE() {
        return datasetORE;
    }

    public List<DatasetFileDetail> getDatasetFileDetails() {
        return datasetFileDetails;
    }

    public DatasetSchemaDotOrg getDatasetSchemaDotOrg() {
        return datasetSchemaDotOrg;
    }

    public DataCiteXml getDataCiteResource() {
        return dataCiteDataCiteXml;
    }

    public static class ExportDataBuilder {

        private ExportDataProvider provider;

        public ExportDataBuilder provider(ExportDataProvider provider) {
            this.provider = provider;
            return this;
        }

        public ExportData build() {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                DatasetJson datasetJson = objectMapper.readValue( provider.getDatasetJson().toString(), DatasetJson.class );
                DatasetORE datasetORE = objectMapper.readValue( provider.getDatasetORE().toString(), DatasetORE.class );
                List<DatasetFileDetail> datasetFileDetails = objectMapper.readValue( provider.getDatasetFileDetails().toString(), new TypeReference<>() { } );
                DatasetSchemaDotOrg datasetSchemaDotOrg = objectMapper.readValue( provider.getDatasetSchemaDotOrg().toString(), DatasetSchemaDotOrg.class );
                XmlMapper xmlMapper = new XmlMapper();
                DataCiteXml dataCiteXml = xmlMapper.readValue( provider.getDataCiteXml(), DataCiteXml.class );
                return new ExportData( datasetJson, datasetORE, datasetFileDetails, datasetSchemaDotOrg, dataCiteXml );
            }
            catch ( JsonProcessingException e ) {
                throw new IllegalArgumentException( e );
            }
        }
    }

    public static class FieldDeserializer extends StdDeserializer<Field> {
        public FieldDeserializer() {
            super( Field.class );
        }

        @Override
        public Field deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree( p );
            String typeName = node.get( "typeName" ).asText();
            boolean multiple = node.get( "multiple" ).asBoolean( false );
            String typeClass = node.get( "typeClass" ).asText();
            JsonNode valueNode = node.get( "value" );

            return new Field(
                typeName, multiple, typeClass,
                getCompoundValuesWhenCompound( p, valueNode ),
                getPrimitiveValuesWhenPrimitive( valueNode ) );
        }

        private List<String> getPrimitiveValuesWhenPrimitive(JsonNode node) {
            if ( node.isArray() ) {
                List<String> result = new ArrayList<>();
                for ( JsonNode element : node ) {
                    if ( !element.isTextual() ) {
                        return null;
                    }
                    result.add( element.asText() );
                }
                return result;
            }
            else if ( node.isTextual() ) {
                return Collections.singletonList( node.asText() );
            }
            return null;
        }

        private List<Map<String, Field>> getCompoundValuesWhenCompound(JsonParser p, JsonNode node) throws JsonProcessingException {
            if ( node.isArray() ) {
                List<Map<String, Field>> result = new ArrayList<>();
                for ( JsonNode element : node ) {
                    if ( element.isTextual() ) {
                        return null;
                    }
                    result.add( getFieldMap( p, element ) );
                }
                return result;
            }
            else if ( !node.isTextual() ) {
                return Collections.singletonList( getFieldMap( p, node ) );
            }
            return null;
        }

        private Map<String, Field> getFieldMap(JsonParser p, JsonNode node) throws JsonProcessingException {
            Map<String, Field> result = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while ( fields.hasNext() ) {
                Map.Entry<String, JsonNode> entry = fields.next();
                Field field = p.getCodec().treeToValue( entry.getValue(), Field.class );
                result.put( entry.getKey(), field );
            }
            return result;
        }
    }
}
