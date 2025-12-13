// src/test/java/io/gdcc/spi/export/dcat3/Dcat3ExporterAssertJTest.java
package io.gdcc.spi.export.dcat3;

import static io.gdcc.spi.export.util.TestUtil.getExportDataProvider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import io.gdcc.spi.export.dcat3.config.RootConfigLoader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Dcat3ExporterTest {

    private String originalProp;

    @BeforeEach
    void setUp() throws URISyntaxException {
        // Remember prior value (if any) and set property to a CLASSPATH resource name.
        URL dcatRootPropertiesUrl = getClass().getClassLoader().getResource( "input/config/dcat-root.properties" );
        assertThat( dcatRootPropertiesUrl ).isNotNull();
        File dcatRootPropetiesFile = new File( dcatRootPropertiesUrl.toURI() );
        originalProp = System.getProperty( RootConfigLoader.SYS_PROP );
        System.setProperty( RootConfigLoader.SYS_PROP, dcatRootPropetiesFile.getAbsolutePath() );
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
    void exportCatalog_produces_expected_triples() throws ExportException {
        // -- given
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Use your existing TestUtil to obtain the provider
        ExportDataProvider provider = getExportDataProvider( "src/test/resources/input/export_data_source_2" );

        // The exporter will load the root via RootConfigLoader.load() in the constructor,
        // which reads the system property we set in @BeforeEach. [2](https://365tno-my.sharepoint.com/personal/sjaak_derksen_tno_nl/Documents/Microsoft%20Copilot%20Chat%20Files/dcat-root.txt)[1](https://365tno-my.sharepoint.com/personal/sjaak_derksen_tno_nl/Documents/Microsoft%20Copilot%20Chat%20Files/RootConfigLoader.java)
        Dcat3Exporter exporter = new Dcat3Exporter();

        // Sanity on exporter capabilities
        assertThat( exporter.getFormatName() ).isEqualTo( "dcat3" );
        assertThat( exporter.getDisplayName( Locale.ROOT ) ).isEqualTo( "DCAT-3" );
        assertThat( exporter.isAvailableToUsers() ).isTrue();
        assertThat( exporter.isHarvestable() ).isFalse();

        // -- when
        exporter.exportDataset( provider, System.out );

        // -- then: parse RDF and assert
        byte[] bytes = out.toByteArray();
        assertThat( bytes ).as( "Exporter should write RDF bytes" )
                           .isNotEmpty();

        Model model = readModel( bytes ); // auto-detects RDF/XML/Turtle/JSON-LD

        // Prefix from root config
        assertThat( model.getNsPrefixURI( "dcat" ) ).as( "dcat prefix should come from root prefixes" )
                                                .isEqualTo(
                                                    "http://www.w3.org/ns/dcat#" ); // set in dcat-root.properties [1](https://365tno-my.sharepoint.com/personal/sjaak_derksen_tno_nl/Documents/Microsoft%20Copilot%20Chat%20Files/RootConfigLoader.java)

        // Catalog present and typed
        Resource DCAT_CATALOG = model.createResource( "http://www.w3.org/ns/dcat#Catalog" );
        Resource catalog = model.listResourcesWithProperty( RDF.type, DCAT_CATALOG )
                            .nextOptional()
                            .orElse( null );
        assertThat( catalog ).as( "a dcat:Catalog resource must be present" )
                             .isNotNull();

        // Titles (NL/EN) per catalog mapping
        Property DCT_TITLE = model.createProperty( "http://purl.org/dc/terms/title" );
        assertThat( model.listStatements( catalog, DCT_TITLE, (RDFNode) null )
                     .toList() ).as( "catalog must have dct:title" )
                                .isNotEmpty()
//                                .anySatisfy( stmt -> assertThat( stmt.getObject()
//                                                                     .asLiteral()
//                                                                     .getLanguage() ).isEqualToIgnoringCase( "nl" ) )
                                .anySatisfy( stmt -> assertThat( stmt.getObject()
                                                                     .asLiteral()
                                                                     .getLanguage() ).isEqualToIgnoringCase( "en" ) );
        // (Mapping defines title_nl from JSONPath and title_en as const) [3](https://365tno-my.sharepoint.com/personal/sjaak_derksen_tno_nl/Documents/Microsoft%20Copilot%20Chat%20Files/Dcat3Exporter.java)

        // contactPoint node (vcard:Kind with email + url)
        Property DCAT_CONTACT_POINT = model.createProperty( "http://www.w3.org/ns/dcat#contactPoint" );
        Resource VCARD_KIND = model.createResource( "http://www.w3.org/2006/vcard/ns#Kind" );
        Property VCARD_FN = model.createProperty( "http://www.w3.org/2006/vcard/ns#fn" );
        Property VCARD_HAS_EMAIL = model.createProperty( "http://www.w3.org/2006/vcard/ns#hasEmail" );
        Property VCARD_HAS_URL = model.createProperty( "http://www.w3.org/2006/vcard/ns#hasURL" );

        Statement cp = model.listStatements( catalog, DCAT_CONTACT_POINT, (RDFNode) null )
                        .nextOptional()
                        .orElse( null );
        assertThat( cp ).as( "catalog must have dcat:contactPoint" )
                        .isNotNull();

        Resource contact = cp.getObject()
                             .asResource();
        assertThat( model.contains( contact, RDF.type, VCARD_KIND ) ).isTrue();
        assertThat( model.contains( contact, VCARD_FN, (RDFNode) null ) ).isTrue();
        assertThat( model.contains( contact, VCARD_HAS_EMAIL, (RDFNode) null ) ).isTrue();
        assertThat( model.contains( contact, VCARD_HAS_URL, (RDFNode) null ) ).isTrue();
        // (All from dcat-catalog.properties) [3](https://365tno-my.sharepoint.com/personal/sjaak_derksen_tno_nl/Documents/Microsoft%20Copilot%20Chat%20Files/Dcat3Exporter.java)

        // publisher node (foaf:Agent + names + ROR type)
        Property DCT_PUBLISHER = model.createProperty( "http://purl.org/dc/terms/publisher" );
        Resource FOAF_AGENT = model.createResource( "http://xmlns.com/foaf/0.1/Agent" );
        Property FOAF_NAME = model.createProperty( "http://xmlns.com/foaf/0.1/name" );
        Property DCT_TYPE = model.createProperty( "http://purl.org/dc/terms/type" );

        Statement pub = model.listStatements( catalog, DCT_PUBLISHER, (RDFNode) null )
                         .nextOptional()
                         .orElse( null );
        assertThat( pub ).as( "catalog must have dct:publisher" )
                         .isNotNull();

        Resource publisher = pub.getObject()
                                .asResource();
        assertThat( model.contains( publisher, RDF.type, FOAF_AGENT ) ).isTrue();
        assertThat( model.contains( publisher, FOAF_NAME, (RDFNode) null ) ).isTrue();
        assertThat( model.contains( publisher, DCT_TYPE, model.createResource( "https://ror.org/01bnjb948" ) ) ).isTrue();
        // (Per catalog mapping) [3](https://365tno-my.sharepoint.com/personal/sjaak_derksen_tno_nl/Documents/Microsoft%20Copilot%20Chat%20Files/Dcat3Exporter.java)
    }

    private static Model readModel(byte[] rdf) {
        Model model = ModelFactory.createDefaultModel();
        try (InputStream in = new ByteArrayInputStream( rdf )) {
            model.read( in, null ); // Jena auto-detect
        }
        catch ( Exception e ) {
            fail( "Failed to parse RDF output", e );
        }
        return model;
    }
}
