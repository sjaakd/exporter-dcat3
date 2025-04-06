package io.gdcc.spi.export.parsing;

import static io.gdcc.spi.export.util.TestUtil.getExportDataProvider;

import io.gdcc.spi.export.ExportDataProvider;
import org.junit.jupiter.api.Test;

class ExportDataTest {

    @Test
    void testMaxSetParsesCorrectly() {

        ExportDataProvider exportDataProvider = getExportDataProvider(  "src/test/resources/max/in" );
        ExportData.builder().provider( exportDataProvider ).build();
    }
}