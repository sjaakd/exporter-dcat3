
package io.gdcc.spi.export.dcat3.mapping;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import io.gdcc.spi.export.dcat3.config.model.ResourceConfig;
import io.gdcc.spi.export.dcat3.config.model.NodeTemplate;
import io.gdcc.spi.export.dcat3.config.model.ValueSource;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

public class ResourceMapper {
    private final ResourceConfig resourceConfig;
    private final Prefixes prefixes;
    private final String resourceTypeCurieOrIri;

    public ResourceMapper(ResourceConfig resourceConfig, Prefixes prefixes, String resourceTypeCurieOrIri) {
        this.resourceConfig = resourceConfig;
        this.prefixes = prefixes;
        this.resourceTypeCurieOrIri = resourceTypeCurieOrIri;
    }

    public Model build(JaywayJsonFinder finder) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefixes( prefixes.jena() );

        List<JsonNode> scopes;
        if ( resourceConfig.scopeJson != null && !resourceConfig.scopeJson.isBlank() ) {
            scopes = finder.nodes( resourceConfig.scopeJson );
            if ( scopes.isEmpty() ) {
                return model;
            }
        }
        else {
            scopes = Collections.singletonList( null );
        }

        for ( JsonNode scopeNode : scopes ) {
            JaywayJsonFinder scoped = ( scopeNode == null ) ? finder : finder.at( scopeNode );
            Resource subject = createSubject( model, scoped );
            if ( resourceTypeCurieOrIri != null ) {
                subject.addProperty( RDF.type, model.createResource( prefixes.expand( resourceTypeCurieOrIri ) ) );
            }
            resourceConfig.props.forEach( (id, valueSource) -> addProperty( model, subject, scoped, valueSource ) );
        }
        return model;
    }

    private Resource createSubject(Model model, JaywayJsonFinder finder) {
        String iri = resourceConfig.subject.iriConst;
        if ( iri == null && resourceConfig.subject.iriTemplate != null ) {
            iri = resourceConfig.subject.iriTemplate;
        }
        if ( iri == null && resourceConfig.subject.iriFormat != null && resourceConfig.subject.iriJson != null ) {
            List<String> values = listScopedOrRoot( finder, resourceConfig.subject.iriJson );
            String value = values.isEmpty() ? null : values.get( 0 );
            if ( value != null ) {
                iri = resourceConfig.subject.iriFormat.replace( "${value}", value );
            }
        }
        if ( iri == null && resourceConfig.subject.iriJson != null ) {
            List<String> values = listScopedOrRoot( finder, resourceConfig.subject.iriJson );
            iri = values.isEmpty() ? null : values.get( 0 );
        }
        return ( iri == null || iri.isBlank() ) ? model.createResource() : model.createResource( iri );
    }

    private void addProperty(Model model, Resource subject, JaywayJsonFinder finder, ValueSource valueSource) {
        String predicateIri = prefixes.expand( valueSource.predicate );
        if ( predicateIri == null ) {
            return;
        }
        Property property = model.createProperty( predicateIri );
        for ( RDFNode rdfNode : resolveObjects( model, finder, valueSource ) ) {
            subject.addProperty( property, rdfNode );
        }
    }

    private List<RDFNode> resolveObjects(Model model, JaywayJsonFinder finder, ValueSource valueSource) {
        switch ( valueSource.as ) {
            case "node-ref":
                return Collections.singletonList( buildNodeRef( model, finder, valueSource ) );
            case "iri":
                return valuesFromSource( finder, valueSource ).stream()
                                                              .map( applyMapIfAny( valueSource ) )
                                                              .map( applyFormatIfAny( valueSource ) )
                                                              .filter( Objects::nonNull )
                                                              .map( model::createResource )
                                                              .collect( Collectors.toList() );
            case "literal":
            default:
                return valuesFromSource( finder, valueSource ).stream()
                                                              .map( applyMapIfAny( valueSource ) )
                                                              .map( applyFormatIfAny( valueSource ) )
                                                              .filter( Objects::nonNull )
                                                              .map( val -> literal( model, val, valueSource.lang, valueSource.datatype ) )
                                                              .collect( Collectors.toList() );
        }
    }

    private RDFNode buildNodeRef(Model model, JaywayJsonFinder finder, ValueSource valueSource) {
        NodeTemplate nodeTemplate = resourceConfig.nodes.get( valueSource.nodeRef );
        if ( nodeTemplate == null ) {
            return model.createResource(); // bnode
        }
        Resource resource = "iri".equals( nodeTemplate.kind ) && nodeTemplate.iriConst != null ? model.createResource( nodeTemplate.iriConst ) : model.createResource();
        if ( nodeTemplate.type != null ) {
            resource.addProperty( RDF.type, model.createResource( prefixes.expand( nodeTemplate.type ) ) );
        }
        nodeTemplate.props.forEach( (propertyId, propertyValueSource ) -> {
            Property property = model.createProperty( prefixes.expand( propertyValueSource .predicate ) );
            for ( RDFNode obj : resolveObjects( model, finder, propertyValueSource  ) ) {
                resource.addProperty( property, obj );
            }
        } );
        return resource;
    }

    private List<String> valuesFromSource(JaywayJsonFinder finder, ValueSource valueSource) {
        if ( valueSource.constValue != null ) {
            return Collections.singletonList( valueSource.constValue );
        }
        if ( valueSource.json != null ) {
            List<String> values = listScopedOrRoot( finder, valueSource.json );
            if ( valueSource.multi ) {
                return values;
            }
            return values.isEmpty() ? Collections.emptyList() : Collections.singletonList( values.get( 0 ) );
        }
        return Collections.emptyList();
    }

    /**
     * If JSONPath starts with "$$", query original root; else, current scope.
     */
    private List<String> listScopedOrRoot(JaywayJsonFinder finder, String jsonPath) {
        if ( jsonPath != null && jsonPath.startsWith( "$$" ) ) {
            return finder.listRoot( jsonPath.substring( 1 ) ); // strip one '$'
        }
        return finder.list( jsonPath );
    }

    private Function<String, String> applyMapIfAny(ValueSource valueSource) {
        return s -> {
            if ( s == null ) {
                return null;
            }
            if ( !valueSource.map.isEmpty() ) {
                return valueSource.map.getOrDefault( s, null );
            }
            return s;
        };
    }

    private Function<String, String> applyFormatIfAny(ValueSource valueSource) {
        return s -> {
            if ( s == null )
                return null;
            if ( valueSource.format != null && !valueSource.format.isBlank() ) {
                // simple ${value} substitution
                return valueSource.format.replace( "${value}", s );
            }
            return s;
        };
    }

    private Literal literal(Model model, String value, String lang, String datatypeIri) {
        if ( datatypeIri != null && !datatypeIri.isBlank() ) {
            RDFDatatype dt = TypeMapper.getInstance()
                                       .getSafeTypeByName( datatypeIri );
            return model.createTypedLiteral( value, dt );
        }
        if ( lang != null && !lang.isBlank() ) {
            return model.createLiteral( value, lang );
        }
        return model.createLiteral( value );
    }

}