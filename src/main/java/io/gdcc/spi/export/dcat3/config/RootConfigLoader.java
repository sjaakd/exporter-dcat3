package io.gdcc.spi.export.dcat3.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RootConfigLoader {

    public static final String SYS_PROP = "dataverse.dcat3.config";

    private RootConfigLoader() {
    }

    /**
     * Load the root config from the location specified in the system property or fallbacks: relative, relative to user home or resource directory.
     * @return RootConfig
     * @throws IOException
     */
    public static RootConfig load() throws IOException {

        String rootProperty = System.getProperty( SYS_PROP );
        if ( rootProperty == null || rootProperty.trim().isEmpty() ) {
            throw new IllegalArgumentException( "System property '" + SYS_PROP + "' not set; please provide a path to dcat-root.properties" );
        }

        // Resolve path: absolute → relative to CWD → relative to user.home → classpath
        InputStream in = null;
        Path baseDir = null;

        Path path = Paths.get( rootProperty );
        if ( Files.isRegularFile( path ) && Files.isReadable( path ) ) {
            in = Files.newInputStream( path );
            baseDir = path.getParent();
        }
        else {
            Path relativeCurrentWorkingDir = Paths.get( "" ).toAbsolutePath().resolve( rootProperty ).normalize();
            if ( Files.isRegularFile( relativeCurrentWorkingDir ) && Files.isReadable( relativeCurrentWorkingDir ) ) {
                in = Files.newInputStream( relativeCurrentWorkingDir );
                baseDir = relativeCurrentWorkingDir.getParent();
            }
            else {
                String userHome = System.getProperty( "user.home" );
                if ( userHome != null ) {
                    Path relativeToUserHome = Paths.get( userHome ).resolve( rootProperty ).normalize();
                    if ( Files.isRegularFile( relativeToUserHome ) && Files.isReadable( relativeToUserHome ) ) {
                        in = Files.newInputStream( relativeToUserHome );
                        baseDir = relativeToUserHome.getParent();
                    }
                }
                if ( in == null ) {
                    // classpath fallback
                    in = Thread.currentThread().getContextClassLoader().getResourceAsStream( rootProperty );
                    if ( in == null ) {
                        in = RootConfigLoader.class.getResourceAsStream( "/" + rootProperty );
                    }
                }
            }
        }
        if ( in == null ) {
            throw new FileNotFoundException( "Cannot locate root properties at: " + rootProperty );
        }

        Properties properties = new Properties();
        try (InputStream closeMe = in) {
            properties.load( closeMe );
        }

        RootConfig rootConfig = parse( properties );
        rootConfig.baseDir = baseDir;
        return rootConfig;
    }

    private static RootConfig parse(Properties properties) {
        RootConfig rootConfig = new RootConfig();
        rootConfig.outputFormat = properties.getProperty( "dcat.output.format", "turtle" );
        rootConfig.trace = Boolean.parseBoolean( properties.getProperty( "dcat.trace.enabled", "false" ) );

        // prefixes.*
        properties.stringPropertyNames()
         .stream()
         .filter( k -> k.startsWith( "prefix." ) )
         .forEach( k -> rootConfig.prefixes.put( k.substring( "prefix.".length() ), properties.getProperty( k ) ) );

        // elements: element.<name>.(id|type|file)
        Pattern elId = Pattern.compile( "^element\\.([^.]+)\\.id$" );
        for ( String key : properties.stringPropertyNames() ) {
            Matcher m = elId.matcher( key );
            if ( !m.matches() ) {
                continue;
            }
            String base = "element." + m.group( 1 );
            RootConfig.Element el = new RootConfig.Element();
            el.id = properties.getProperty( base + ".id" );
            el.typeCurieOrIri = properties.getProperty( base + ".type" );
            el.file = properties.getProperty( base + ".file" );
            rootConfig.elements.add( el );
        }

        // relations: relation.<name>.(subject|predicate|object|cardinality)
        Pattern relPred = Pattern.compile( "^relation\\.([^.]+)\\.predicate$" );
        for ( String key : properties.stringPropertyNames() ) {
            Matcher m = relPred.matcher( key );
            if ( !m.matches() ) {
                continue;
            }
            String base = "relation." + m.group( 1 );
            RootConfig.Relation r = new RootConfig.Relation();
            r.subjectElementId = properties.getProperty( base + ".subject" );
            r.predicateCurieOrIri = properties.getProperty( base + ".predicate" );
            r.objectElementId = properties.getProperty( base + ".object" );
            r.cardinality = properties.getProperty( base + ".cardinality" );
            rootConfig.relations.add( r );
        }
        return rootConfig;
    }

    /**
     * Resolve an element file relative to the root’s directory, then cwd, then user.home, then classpath.
     */
    public static InputStream resolveElementFile(RootConfig rootConfig, String fileName) throws IOException {
        // 1) relative to root file’s directory
        if ( rootConfig.baseDir != null ) {
            Path relative = rootConfig.baseDir.resolve( fileName ).normalize();
            if ( Files.isRegularFile( relative ) && Files.isReadable( relative ) ) {
                return Files.newInputStream( relative );
            }
        }
        // 2) cwd
        Path currentWorkingDir = Paths.get( "" ).toAbsolutePath().resolve( fileName );
        if ( Files.isRegularFile( currentWorkingDir ) && Files.isReadable( currentWorkingDir ) ) {
            return Files.newInputStream( currentWorkingDir );
        }
        // 3) user.home
        String home = System.getProperty( "user.home" );
        if ( home != null ) {
            Path relHome = Paths.get( home ).resolve( fileName );
            if ( Files.isRegularFile( relHome ) && Files.isReadable( relHome ) ) {
                return Files.newInputStream( relHome );
            }
        }
        // 4) classpath
        InputStream classPath = Thread.currentThread().getContextClassLoader().getResourceAsStream( fileName );
        if ( classPath != null ) {
            return classPath;
        }
        classPath = RootConfigLoader.class.getResourceAsStream( "/" + fileName );
        if ( classPath != null ) {
            return classPath;
        }

        throw new FileNotFoundException( "Element properties not found: " + fileName );
    }
}
