package io.gdcc.spi.export.dcat3.config.loader;


// PropertiesMappingLoader.java

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.gdcc.spi.export.dcat3.config.model.Config;
import io.gdcc.spi.export.dcat3.config.model.NodeTemplate;
import io.gdcc.spi.export.dcat3.config.model.ValueSource;

public class PropertiesMappingLoader {

    public Config load(InputStream in) throws Exception {
        Properties property = new Properties();
        property.load( in );

        Config config = new Config();

        // Subject
        config.subject.iriConst = property.getProperty( "subject.iri.const" );
        config.subject.iriTemplate = property.getProperty( "subject.iri.template" );
        config.subject.iriJson = property.getProperty( "subject.iri.json" );

        // Props
        Pattern proppertyPattern = Pattern.compile( "^props\\.([^.]+)\\.(.+)$" );
        for ( String propertyName : property.stringPropertyNames() ) {
            Matcher matcher = proppertyPattern.matcher( propertyName );
            if ( !matcher.matches() ) {
                continue;
            }
            String id = matcher.group( 1 );
            String tail = matcher.group( 2 );
            ValueSource vs = config.props.computeIfAbsent( id, _k -> new ValueSource() );
            applyValue( vs, tail, property.getProperty( propertyName ) );
        }

        // Nodes
        Pattern nodePattern = Pattern.compile( "^nodes\\.([^.]+)\\.(.+)$" );
        Pattern nodePropertyPattern = Pattern.compile( "^props\\.([^.]+)\\.(.+)$" );
        for ( String propertyName : property.stringPropertyNames() ) {
            Matcher propertyMatcher = nodePattern.matcher( propertyName );
            if ( !propertyMatcher.matches() ) {
                continue;
            }
            String nodeId = propertyMatcher.group( 1 );
            String tail = propertyMatcher.group( 2 );
            NodeTemplate nodeTemplate = config.nodes.computeIfAbsent( nodeId, _k -> {
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
                Matcher nodePropertyPatternMatcher = nodePropertyPattern.matcher( tail );
                if ( nodePropertyPatternMatcher.matches() ) {
                    String propId = nodePropertyPatternMatcher.group( 1 );
                    String propTail = nodePropertyPatternMatcher.group( 2 );
                    ValueSource valueSource = nodeTemplate.props.computeIfAbsent( propId, _k -> new ValueSource() );
                    applyValue( valueSource, propTail, property.getProperty( propertyName ) );
                }
            }
        }

        return config;
    }

    private void applyValue(ValueSource valueSource, String keyTail, String value) {
        switch ( keyTail ) {
            case "predicate":
                valueSource.predicate = value;
                break;
            case "as":
                valueSource.as = value;
                break;
            case "lang":
                valueSource.lang = value;
                break;
            case "datatype":
                valueSource.datatype = value;
                break;
            case "json":
                valueSource.json = value;
                break;
            case "const":
                valueSource.constValue = value;
                break;
            case "node":
                valueSource.nodeRef = value;
                break;
            case "multi":
                valueSource.multi = Boolean.parseBoolean( value );
                break;
            case "when":
                valueSource.when = value;
                break;
            default:
                if ( keyTail.startsWith( "map." ) ) {
                    String k = keyTail.substring( "map.".length() );
                    valueSource.map.put( k, value );
                }
        }
    }
}
