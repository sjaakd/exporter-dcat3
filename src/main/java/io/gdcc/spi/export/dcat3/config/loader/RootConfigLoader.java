package io.gdcc.spi.export.dcat3.config.loader;

import static io.gdcc.spi.export.dcat3.config.loader.FileResolver.resolveFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.gdcc.spi.export.dcat3.config.model.Element;
import io.gdcc.spi.export.dcat3.config.model.Relation;
import io.gdcc.spi.export.dcat3.config.model.RootConfig;

public final class RootConfigLoader {

    public static final String SYS_PROP = "dataverse.dcat3.config";
    private static final Pattern ELEMENT_ID_PATTERN = Pattern.compile( "^element\\.([^.]+)\\.id$" );
    private static final Pattern RELATION_PREDICATE_PATTERN = Pattern.compile( "^relation\\.([^.]+)\\.predicate$" );

    private RootConfigLoader() {
    }

    /**
     * Load the root config from the location specified in the system property or fallbacks: relative, relative to user home or resource directory.
     *
     * @return RootConfig
     * @throws IOException
     */
    public static RootConfig load() throws IOException {
        String rootProperty = System.getProperty( SYS_PROP );
        if ( rootProperty == null || rootProperty.trim().isEmpty() ) {
            throw new IllegalArgumentException( "System property '" + SYS_PROP + "' not set; please provide a path to dcat-root.properties" );
        }

        FileResolver.ResolvedFile resolved = resolveFile( null, rootProperty );
        Properties properties = new Properties();
        try (InputStream closeMe = resolved.in()) {
            properties.load( closeMe );
        }

        RootConfig rootConfig = parse( properties );
        rootConfig.baseDir = resolved.baseDir(); // may be null when loaded from classpath
        return rootConfig;
    }

    private static RootConfig parse(Properties properties) {
        RootConfig rootConfig = new RootConfig();
        rootConfig.trace = Boolean.parseBoolean( properties.getProperty( "dcat.trace.enabled", "false" ) );

        // prefixes.*
        properties.stringPropertyNames()
                  .stream()
                  .filter( k -> k.startsWith( "prefix." ) )
                  .forEach( k -> rootConfig.prefixes.put( k.substring( "prefix.".length() ), properties.getProperty( k ) ) );

        // elements: element.<name>.(id|type|file)
        for ( String key : properties.stringPropertyNames() ) {
            Matcher matcher = ELEMENT_ID_PATTERN.matcher( key );
            if ( !matcher.matches() ) {
                continue;
            }
            String base = "element." + matcher.group( 1 );
            Element element = new Element();
            element.id = properties.getProperty( base + ".id" );
            element.typeCurieOrIri = properties.getProperty( base + ".type" );
            element.file = properties.getProperty( base + ".file" );
            rootConfig.elements.add( element );
        }

        // relations: relation.<name>.(subject|predicate|object|cardinality)
        for ( String key : properties.stringPropertyNames() ) {
            Matcher matcher = RELATION_PREDICATE_PATTERN.matcher( key );
            if ( !matcher.matches() ) {
                continue;
            }
            String base = "relation." + matcher.group( 1 );
            Relation relation = new Relation();
            relation.subjectElementId = properties.getProperty( base + ".subject" );
            relation.predicateCurieOrIri = properties.getProperty( base + ".predicate" );
            relation.objectElementId = properties.getProperty( base + ".object" );
            relation.cardinality = properties.getProperty( base + ".cardinality" );
            rootConfig.relations.add( relation );
        }
        return rootConfig;
    }
}
