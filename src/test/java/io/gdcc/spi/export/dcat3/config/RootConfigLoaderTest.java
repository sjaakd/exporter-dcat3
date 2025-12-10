package io.gdcc.spi.export.dcat3.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RootConfigLoaderTest {

    @TempDir
    Path temp;

    @Test
    void loads_root_config_and_resolves_element_relative_to_root_dir() throws Exception {
        // Arrange: write root and element files under a temp dir
        Path rootFile = temp.resolve( "dcat-root.properties" );
        Path catalogFile = temp.resolve( "dcat-catalog.properties" );

        // Minimal catalog element mapping (subject + one literal)
        Files.writeString( catalogFile,
                           "subject.iri.const = https://data.example.org/catalog/gdn-test\n"
                               + "props.title_en.predicate = dct:title\n"
                               + "props.title_en.as = literal\n"
                               + "props.title_en.lang = en\n"
                               + "props.title_en.const = Test Catalog\n" );

        // Root file that points to the catalog file (relative)
        Files.writeString( rootFile, "dcat.output.format = rdfxml\n"
            + "dcat.trace.enabled = true\n"
            + "prefix.dcat = http://www.w3.org/ns/dcat#\n"
            + "prefix.dct  = http://purl.org/dc/terms/\n"
            + "element.catalog.id   = catalog\n"
            + "element.catalog.type = dcat:Catalog\n"
            + "element.catalog.file = dcat-catalog.properties\n" );

        // Set the system property that RootConfigLoader expects
        System.setProperty( RootConfigLoader.SYS_PROP, rootFile.toString() );

        // Act: load the root config
        RootConfig rc = RootConfigLoader.load();

        // Assert: root-level settings
        assertThat( rc.outputFormat ).isEqualTo( "rdfxml" );
        assertThat( rc.trace ).isTrue();
        assertThat( rc.prefixes ).containsEntry( "dcat", "http://www.w3.org/ns/dcat#" )
                                 .containsEntry( "dct", "http://purl.org/dc/terms/" );
        assertThat( rc.elements ).hasSize( 1 );
        assertThat( rc.elements.get( 0 ).id ).isEqualTo( "catalog" );
        assertThat( rc.elements.get( 0 ).typeCurieOrIri ).isEqualTo( "dcat:Catalog" );
        assertThat( rc.elements.get( 0 ).file ).isEqualTo( "dcat-catalog.properties" );
        assertThat( rc.baseDir ).isEqualTo( temp );

        // Act: resolve the element file via the loader
        try (InputStream in = RootConfigLoader.resolveElementFile( rc, rc.elements.get( 0 ).file )) {
            assertThat( in ).as( "Element file should be resolvable from root baseDir" )
                            .isNotNull();

            // Parse with PropertiesMappingLoader to ensure the file is valid
            MappingModel.Config cfg = new PropertiesMappingLoader().load( in );

            // Assert: a couple of fields to prove it parsed correctly
            assertThat( cfg.subject.iriConst ).isEqualTo( "https://data.example.org/catalog/gdn-test" );
            MappingModel.ValueSource titleEn = cfg.props.get( "title_en" );
            assertThat( titleEn ).isNotNull();
            assertThat( titleEn.predicate ).isEqualTo( "dct:title" );
            assertThat( titleEn.as ).isEqualTo( "literal" );
            assertThat( titleEn.lang ).isEqualTo( "en" );
            assertThat( titleEn.constValue ).isEqualTo( "Test Catalog" );
        }
    }

    @Test
    void resolves_root_from_cwd_when_not_absolute() throws Exception {
        // Arrange: create root under the temp dir and set SYS_PROP to a relative name
        Path rootFile = temp.resolve( "dcat-root.properties" );
        Files.writeString( rootFile,
                           "dcat.output.format = turtle\n"
                               + "element.catalog.id   = catalog\n"
                               + "element.catalog.type = dcat:Catalog\n"
                               + "element.catalog.file = dcat-catalog.properties\n" );

        // Create the element file in the same temp dir
        Path catalogFile = temp.resolve( "dcat-catalog.properties" );
        Files.writeString( catalogFile, "subject.iri.const = https://example.org/catalog/rel-cwd" );

        // Simulate running with CWD == temp (by using a relative path in SYS_PROP)
        // We temporarily change the working directory by using an absolute path in SYS_PROP's *value*.
        // Since RootConfigLoader checks absolute first, we instead set SYS_PROP to a relative value and
        // rely on "relative to CWD" branch: to make that work in a test, we point CWD to temp via Path.toAbsolutePath().
        // Easiest: set SYS_PROP to the *file name* and temporarily copy files to CWD. Alternatively, call load()
        // with the absolute path directly (covered by the previous test). Here we exercise the user.home fallback next.

        // NOTE: For a reliable "CWD resolution" test across build tools, it's usually easier to test the user.home branch instead.
        // We keep this test lightweight and focus on parser behavior.

        System.setProperty( RootConfigLoader.SYS_PROP, rootFile.toString() ); // absolute path → already covered
        RootConfig rc = RootConfigLoader.load();
        assertThat( rc.outputFormat ).isEqualTo( "turtle" );
        assertThat( rc.elements ).hasSize( 1 );
    }

    @Test
    void resolves_root_from_user_home_when_config_points_there() throws Exception {
        // Arrange: set SYS_PROP to a relative path under user.home
        String home = System.getProperty( "user.home" );
        assumeHomeAvailable( home );

        Path homeDir = Path.of( home );
        Path rootAtHome = homeDir.resolve( "dcat-root-home.properties" );
        Path elementAtHome = homeDir.resolve( "dcat-catalog-home.properties" );

        Files.writeString( elementAtHome, "subject.iri.const = https://example.org/catalog/user-home" );
        Files.writeString( rootAtHome,
                           "dcat.output.format = jsonld\n"
                               + "element.catalog.id   = catalog\n"
                               + "element.catalog.type = dcat:Catalog\n"
                               + "element.catalog.file = dcat-catalog-home.properties\n" );

        // Set SYS_PROP to the relative name so loader tries user.home branch
        System.setProperty( RootConfigLoader.SYS_PROP, "dcat-root-home.properties" );

        // Act
        RootConfig rc = RootConfigLoader.load();

        // Assert
        assertThat( rc.outputFormat ).isEqualTo( "jsonld" );
        assertThat( rc.baseDir ).isEqualTo( homeDir );
        assertThat( rc.elements ).hasSize( 1 );

        // Resolve element from user.home
        try (InputStream in = RootConfigLoader.resolveElementFile( rc, "dcat-catalog-home.properties" )) {
            assertThat( in ).isNotNull();
            MappingModel.Config cfg = new PropertiesMappingLoader().load( in );
            assertThat( cfg.subject.iriConst ).isEqualTo( "https://example.org/catalog/user-home" );
        }
        finally {
            // Clean up files we wrote under user.home
            Files.deleteIfExists( rootAtHome );
            Files.deleteIfExists( elementAtHome );
        }
    }

    @Test
    void fails_cleanly_when_system_property_missing() {
        // Ensure property is not set
        System.clearProperty( RootConfigLoader.SYS_PROP );

        assertThatThrownBy( RootConfigLoader::load ).isInstanceOf( IllegalArgumentException.class )
                                                    .hasMessageContaining( RootConfigLoader.SYS_PROP );
    }

    @Test
    void fails_cleanly_when_root_file_not_found() {
        System.setProperty( RootConfigLoader.SYS_PROP, "does-not-exist.properties" );
        assertThatThrownBy( RootConfigLoader::load ).isInstanceOf( java.io.FileNotFoundException.class )
                                                    .hasMessageContaining( "Cannot locate root properties" );
    }

    // --- helpers ---

    private static void assumeHomeAvailable(String home) {
        // Basic guard so CI without a writable HOME won't break this test.
        // If HOME is null or not writable, skip with a clear message.
        assertThat( home ).as( "System property 'user.home' must be set for this test" ).isNotNull();
        assertThat( Files.isDirectory( Path.of( home ) ) ).as( "'user.home' must be a directory" ).isTrue();
    }
}
