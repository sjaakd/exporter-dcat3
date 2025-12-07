package io.gdcc.spi.export.dcat3;

import static io.gdcc.spi.export.util.TestUtil.getExportDataProvider;

import java.io.OutputStream;

import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import org.junit.jupiter.api.Test;

public class Dcat3ExporterTest {


    @Test
    public void testExport() throws ExportException {

        // -- prepare
        OutputStream outputStream = System.out;
        ExportDataProvider exportDataProvider = getExportDataProvider(  "src/test/resources/max/in" );
        Dcat3Exporter exporter = new Dcat3Exporter();
        exporter.exportDataset( exportDataProvider, System.out );
    }

}
