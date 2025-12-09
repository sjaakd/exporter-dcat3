package io.gdcc.spi.export.dcat3;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

/**
 * Builds a local SmallRyeConfig that:
 * - includes default sources (System props, Env vars, standard files)
 * - adds an optional 'config.properties' sitting next to this JAR
 * - registers your @ConfigMapping interface
 */
public final class LocalConfig {
    private LocalConfig() {
    }

    public static SmallRyeConfig build(Class<?> anchor, Class<?>... mappings) {


        // 1) Resolve dcat3.properties"

        String filePath = System.getProperty( "dataverse.dcat3.config" );
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("System property 'dataverse.dcat3.config' is not set or empty.");
        }

        Path cfgFile = Paths.get( filePath );

        // 2) Convert to Map<String,String> if file exists
        Map<String, String> extra = Map.of();
        if ( Files.isReadable( cfgFile ) ) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream( cfgFile )) {
                p.load( in );
            }
            catch ( Exception ignore ) { /* best-effort */ }
            extra = p.entrySet().stream().collect( Collectors.toMap( e -> e.getKey().toString(), e -> e.getValue().toString() ) );
        }

        // 3) Build a detached SmallRyeConfig
        SmallRyeConfigBuilder configBuilder = new SmallRyeConfigBuilder().addDefaultSources(); // env + sys + standard files (per SmallRye docs)

        if ( !extra.isEmpty() ) {
            // Ordinal 250: sits between Env(300) and classpath defaults(100)
            configBuilder.withSources( new PropertiesConfigSource( extra, "jar-dir-config", 250 ) );
        }

        for ( Class<?> m : mappings ) {
            configBuilder.withMapping( m ); // registers your @ConfigMapping interface(s) explicitly
        }

        return configBuilder.build();
    }

}
