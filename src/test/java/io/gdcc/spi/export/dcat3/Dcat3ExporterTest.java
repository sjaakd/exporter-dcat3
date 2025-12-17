// src/test/java/io/gdcc/spi/export/dcat3/Dcat3ExporterAssertJTest.java
package io.gdcc.spi.export.dcat3;

import static io.gdcc.spi.export.util.TestUtil.getExportDataProvider;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.Locale;

import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.dcat3.config.loader.RootConfigLoader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Dcat3ExporterTest {

    private String originalProp;

    @BeforeEach
    void setUp()  {
        originalProp = System.getProperty( RootConfigLoader.SYS_PROP );
    }

    @AfterEach
    void tearDown() {
        // Restore system property to avoid cross-test interference.
        if ( originalProp != null ) {
            System.setProperty( RootConfigLoader.SYS_PROP, originalProp );
        }
        else {
            System.clearProperty( RootConfigLoader.SYS_PROP );
        }
    }

    @Test
    void exportSet1Test() throws Exception {

        // -- prepare configuration
        URL dcatRootPropertiesUrl = getClass().getClassLoader().getResource( "input/config_1/dcat-root.properties" );
        assertThat( dcatRootPropertiesUrl ).isNotNull();
        File dcatRootPropetiesFile = new File( dcatRootPropertiesUrl.toURI() );
        System.setProperty( RootConfigLoader.SYS_PROP, dcatRootPropetiesFile.getAbsolutePath() );

        // -- prepare export data provider
        ExportDataProvider provider = getExportDataProvider( "src/test/resources/input/export_data_source_1" );

        // -- prepare exporter
        Dcat3Exporter exporter = new Dcat3Exporter();

        // -- action test all the fields
        assertThat( exporter.getFormatName() ).isEqualTo( "dcat3" );
        assertThat( exporter.getDisplayName( Locale.ROOT ) ).isEqualTo( "DCAT-3" );
        assertThat( exporter.isAvailableToUsers() ).isTrue();
        assertThat( exporter.isHarvestable() ).isTrue();
        assertThat( exporter.getMediaType() ).isEqualTo(  "application/rdf+xml" );

        // -- action test export function
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.exportDataset( provider, out );
        byte[] bytes = out.toByteArray();

        // -- result sanity check
        assertThat( bytes ).as( "Exporter should write RDF bytes" ).isNotEmpty();

        // -- result SHACL validation
        Model dataModel = readModel( bytes, Lang.RDFXML ); // your helper—auto-detects syntax
        Model shapes = ModelFactory.createDefaultModel();
        shapes.read( getClass().getClassLoader().getResourceAsStream( "input/shacl_1.ttl" ), null, "TURTLE" );
        ValidationReport report = ShaclValidator.get().validate( shapes.getGraph(), dataModel.getGraph() );
        assertThat( report.conforms() ).as( toValidationReport( report ) ).isTrue();
    }

    @Test
    void exportSet2Test() throws Exception {

        // -- prepare configuration
        URL dcatRootPropertiesUrl = getClass().getClassLoader().getResource( "input/config_2/dcat-root.properties" );
        assertThat( dcatRootPropertiesUrl ).isNotNull();
        File dcatRootPropetiesFile = new File( dcatRootPropertiesUrl.toURI() );
        System.setProperty( RootConfigLoader.SYS_PROP, dcatRootPropetiesFile.getAbsolutePath() );

        // -- prepare export data provider
        ExportDataProvider provider = getExportDataProvider( "src/test/resources/input/export_data_source_2" );

        // -- prepare exporter
        Dcat3Exporter exporter = new Dcat3Exporter();

        // -- action test all the fields
        assertThat( exporter.getFormatName() ).isEqualTo( "dcat3" );
        assertThat( exporter.getDisplayName( Locale.ROOT ) ).isEqualTo( "DCAT-3" );
        assertThat( exporter.isAvailableToUsers() ).isTrue();
        assertThat( exporter.isHarvestable() ).isTrue();
        assertThat( exporter.getMediaType() ).isEqualTo(  "text/turtle" );

        // -- action test export function
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.exportDataset( provider, out );
        byte[] bytes = out.toByteArray();

        // -- result sanity check
        assertThat( bytes ).as( "Exporter should write RDF bytes" ).isNotEmpty();

        // -- result SHACL validation
        Model dataModel = readModel( bytes, Lang.TURTLE ); // your helper—auto-detects syntax
        Model shapes = ModelFactory.createDefaultModel();
        shapes.read( getClass().getClassLoader().getResourceAsStream( "input/shacl_2.ttl" ), null, "TURTLE" );
        ValidationReport report = ShaclValidator.get().validate( shapes.getGraph(), dataModel.getGraph() );
        assertThat( report.conforms() ).as( toValidationReport( report ) ).isTrue();
    }

    private static String toValidationReport( ValidationReport report) {
        StringBuilder sb = new StringBuilder( "DCAT/DCAT-AP minimal SHACL should conform for generated RDF" ).append(  System.lineSeparator() );
        report.getEntries()
              .forEach( entry -> sb.append( " - focusNode: " )
                                   .append( entry.focusNode() )
                                   .append( System.lineSeparator() )
                                   .append( "   path: " )
                                   .append( entry.resultPath() )
                                   .append( System.lineSeparator() )
                                   .append( "   message: " )
                                   .append( entry.message() )
                                   .append( System.lineSeparator() )
                                   .append( "   severity: " )
                                   .append( entry.severity() )
                                   .append( System.lineSeparator() ) );
        return sb.toString();
    }

    private static Model readModel(byte[] rdf, Lang lang) {

        Model model = ModelFactory.createDefaultModel();
        RDFParser.create()
                 .source( new ByteArrayInputStream( rdf ) )
                 .lang( lang )
                 .parse( model );
        return model;

    }
}
