// ResourceMapper.java
package io.gdcc.spi.export.dcat3.mapping;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import io.gdcc.spi.export.dcat3.config.MappingModel;
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

    private final MappingModel.Config cfg;
    private final Prefixes prefixes;
    private final String resourceTypeCurieOrIri;

    public ResourceMapper(MappingModel.Config config, Prefixes prefixes, String resourceTypeCurieOrIri) {
        this.cfg = config;
        this.prefixes = prefixes;
        this.resourceTypeCurieOrIri = resourceTypeCurieOrIri;
    }

    public Model build(JsonNode source) {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefixes( prefixes.jena() );

        // Jayway finder over the JSON tree
        JaywayJsonFinder finder = new JaywayJsonFinder( source );

        Resource subject = createSubject( m, finder );

        // rdf:type
        if ( resourceTypeCurieOrIri != null ) {
            subject.addProperty( RDF.type, m.createResource( prefixes.expand( resourceTypeCurieOrIri ) ) );
        }

        // properties
        cfg.props.forEach( (id, vs) -> addProperty( m, subject, finder, vs ) );

        return m;
    }

    private Resource createSubject(Model model, JaywayJsonFinder finder) {
        String iri = cfg.subject.iriConst;

        if ( iri == null && cfg.subject.iriTemplate != null ) {
            // TODO (optional): implement template vars resolved via JSONPath
            iri = cfg.subject.iriTemplate;
        }

        if ( iri == null && cfg.subject.iriJson != null ) {
            List<String> vals = finder.list( cfg.subject.iriJson );
            iri = vals.isEmpty() ? null : vals.get( 0 );
        }

        return ( iri == null ) ? model.createResource() : model.createResource( iri );
    }

    private void addProperty(Model model, Resource subject, JaywayJsonFinder finder, MappingModel.ValueSource valueSource) {
        String predicateIri = prefixes.expand( valueSource.predicate );
        if ( predicateIri == null ) {
            return;
        }
        Property property = model.createProperty( predicateIri );

        for ( RDFNode rdfNode : resolveObjects( model, finder, valueSource ) ) {
            subject.addProperty( property, rdfNode );
        }
    }

    private List<RDFNode> resolveObjects(Model model, JaywayJsonFinder finder, MappingModel.ValueSource valueSource) {
        switch ( valueSource.as ) {
            case "node-ref":
                return Collections.singletonList( buildNodeRef( model, finder, valueSource ) );

            case "iri":
                return valuesFromSource( finder, valueSource ).stream()
                                                     .map( applyMapIfAny( valueSource ) )
                                                     .filter( Objects::nonNull )
                                                     .map( model::createResource )
                                                     .collect( Collectors.toList() );

            case "literal":
            default:
                return valuesFromSource( finder, valueSource ).stream()
                                                     .map( applyMapIfAny( valueSource ) )
                                                     .filter( Objects::nonNull )
                                                     .map( val -> literal( model, val, valueSource.lang, valueSource.datatype ) )
                                                     .collect( Collectors.toList() );
        }
    }

    private RDFNode buildNodeRef(Model model, JaywayJsonFinder finder, MappingModel.ValueSource valueSource) {
        MappingModel.NodeTemplate nt = cfg.nodes.get( valueSource.nodeRef );
        if ( nt == null ) {
            return model.createResource(); // bnode
        }

        Resource r = "iri".equals( nt.kind ) && nt.iriConst != null ? model.createResource( nt.iriConst ) : model.createResource(); // bnode

        if ( nt.type != null ) {
            r.addProperty( RDF.type, model.createResource( prefixes.expand( nt.type ) ) );
        }

        // Node’s internal properties (can be const or JSONPath)
        nt.props.forEach( (pid, pvs) -> {
            Property p = model.createProperty( prefixes.expand( pvs.predicate ) );
            for ( RDFNode obj : resolveObjects( model, finder, pvs ) ) {
                r.addProperty( p, obj );
            }
        } );
        return r;
    }

    private List<String> valuesFromSource(JaywayJsonFinder finder, MappingModel.ValueSource valueSource) {
        if ( valueSource.constValue != null ) {
            return Collections.singletonList( valueSource.constValue );
        }

        if ( valueSource.json != null ) {
            List<String> vals = finder.list( valueSource.json );
            if ( valueSource.multi ) {
                return vals;
            }
            return vals.isEmpty() ? Collections.emptyList() : Collections.singletonList( vals.get( 0 ) );
        }

        return Collections.emptyList();
    }

    private Function<String, String> applyMapIfAny(MappingModel.ValueSource vs) {
        return s -> {
            if ( s == null ) {
                return null;
            }
            if ( !vs.map.isEmpty() ) {
                return vs.map.getOrDefault( s, null ); // drop if unmapped
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