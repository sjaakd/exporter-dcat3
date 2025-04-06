package io.gdcc.spi.export.parsing.datasetJson;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.gdcc.spi.export.parsing.ExportData;

@JsonDeserialize( using = ExportData.FieldDeserializer.class )
public record Field(

    String typeName,
    boolean multiple,
    String typeClass,
    List<Map<String, Field>> compoundValues,
    List<String> primitiveValue
) {
}