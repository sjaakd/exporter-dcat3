package io.gdcc.spi.export.dcat3.config.loader;


// PropertiesMappingLoader.java

import static io.gdcc.spi.export.dcat3.config.loader.Util.applyValue;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.gdcc.spi.export.dcat3.config.model.NodeTemplate;
import io.gdcc.spi.export.dcat3.config.model.ResourceConfig;
import io.gdcc.spi.export.dcat3.config.model.ValueSource;

public class ResourceConfigLoader {

    private static final Pattern PROPPERTY_PATTERN = Pattern.compile( "^props\\.([^.]+)\\.(.+)$" );
    private static final Pattern NODE_PATTERN = Pattern.compile( "^nodes\\.([^.]+)\\.(.+)$" );
    private static final Pattern NODE_PROPERTY_PATTERN = Pattern.compile( "^props\\.([^.]+)\\.(.+)$" );

    public ResourceConfig load(InputStream in) throws Exception {
        Properties property = new Properties();
        property.load( in );

        ResourceConfig resourceConfig = new ResourceConfig();

        // Subject
        resourceConfig.subject.iriConst = property.getProperty( "subject.iri.const" );
        resourceConfig.subject.iriTemplate = property.getProperty( "subject.iri.template" );
        resourceConfig.subject.iriJson = property.getProperty( "subject.iri.json" );

        // Props
        for ( String propertyName : property.stringPropertyNames() ) {
            Matcher matcher = PROPPERTY_PATTERN.matcher( propertyName );
            if ( !matcher.matches() ) {
                continue;
            }
            String id = matcher.group( 1 );
            String tail = matcher.group( 2 );
            ValueSource vs = resourceConfig.props.computeIfAbsent( id, _k -> new ValueSource() );
            applyValue( vs, tail, property.getProperty( propertyName ) );
        }

        // Nodes
        for ( String propertyName : property.stringPropertyNames() ) {
            Matcher propertyMatcher = NODE_PATTERN.matcher( propertyName );
            if ( !propertyMatcher.matches() ) {
                continue;
            }
            String nodeId = propertyMatcher.group( 1 );
            String tail = propertyMatcher.group( 2 );
            NodeTemplate nodeTemplate = resourceConfig.nodes.computeIfAbsent( nodeId, _k -> {
                NodeTemplate template = new NodeTemplate();
                template.id = nodeId;
                return template;
            } );

            if ( tail.equals( "kind" ) ) {
                nodeTemplate.kind = property.getProperty( propertyName );
            }
            else if ( tail.equals( "iri.const" ) ) {
                nodeTemplate.iriConst = property.getProperty( propertyName );
            }
            else if ( tail.equals( "type" ) ) {
                nodeTemplate.type = property.getProperty( propertyName );
            }
            else {
                Matcher nodePropertyPatternMatcher = NODE_PROPERTY_PATTERN.matcher( tail );
                if ( nodePropertyPatternMatcher.matches() ) {
                    String propId = nodePropertyPatternMatcher.group( 1 );
                    String propTail = nodePropertyPatternMatcher.group( 2 );
                    ValueSource valueSource = nodeTemplate.props.computeIfAbsent( propId, _k -> new ValueSource() );
                    applyValue( valueSource, propTail, property.getProperty( propertyName ) );
                }
            }
        }

        return resourceConfig;
    }

}
