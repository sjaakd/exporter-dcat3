package io.gdcc.spi.export.dcat3;

import static io.gdcc.spi.export.util.TestUtil.getExportDataProvider;
import static org.assertj.core.api.Assertions.assertThat;

import io.gdcc.spi.export.ExportDataProvider;
import org.junit.jupiter.api.Test;

class ExportDataTest {

    @Test
    void testMaxSetParsesCorrectly() {

        // -- prepare
        ExportDataProvider exportDataProvider =
                getExportDataProvider("src/test/resources/input/export_data_source_1");
        ExportData result = ExportData.builder().provider(exportDataProvider).build();

        // -- verify
        assertThat(result).isNotNull();
        assertThat(result.getDatasetJson()).isNotNull();
        assertThat(result.getDataCiteXml()).isNotNull();
        assertThat(result.getDatasetORE()).isNotNull();
        assertThat(result.getDatasetFileDetails()).isNotNull();
        assertThat(result.getDatasetSchemaDotOrg()).isNotNull();
    }

    @Test
    void testSpecificSetParsesCorrectly() {

        // -- prepare
        ExportDataProvider exportDataProvider =
                getExportDataProvider("src/test/resources/input/export_data_source_2");
        ExportData result = ExportData.builder().provider(exportDataProvider).build();

        // -- verify
        assertThat(result).isNotNull();
        assertThat(result.getDatasetJson()).isNotNull();
        assertThat(result.getDataCiteXml()).isNotNull();
        assertThat(result.getDatasetORE()).isNotNull();
        assertThat(result.getDatasetFileDetails()).isNotNull();
        assertThat(result.getDatasetSchemaDotOrg()).isNotNull();
    }
}
