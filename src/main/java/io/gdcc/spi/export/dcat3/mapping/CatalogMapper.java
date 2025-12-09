package io.gdcc.spi.export.dcat3.mapping;

// CatalogMapper.java

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

public class CatalogMapper {

    private final MappingModel.Config cfg;
    private final Prefixes prefixes;

    public CatalogMapper(MappingModel.Config cfg) {
        this.cfg = cfg;
        this.prefixes = new Prefixes( cfg.prefixes );
    }

    public Model build(JsonNode source) {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefixes( prefixes.jena() );

        Resource catalog = subjectResource( m, source );

        // rdf:type dcat:Catalog (if not already described through mapping)
        Property rdfType = m.createProperty( RDF.type.getURI() );
        Resource dcatCatalog = m.createResource( prefixes.expand( "dcat:Catalog" ) );
        catalog.addProperty( rdfType, dcatCatalog );

        // simple properties
        JsonValueFinder finder = new JsonValueFinder( source );
        cfg.props.forEach( (id, vs) -> addProperty( m, catalog, finder, vs ) );

        return m;
    }

    private Resource subjectResource(Model m, JsonNode source) {
        String iri = cfg.subject.iriConst;
        if ( iri == null && cfg.subject.iriTemplate != null ) {
            // naive template; you can expand with {var} -> dotted path lookup
            iri = cfg.subject.iriTemplate;
        }
        if ( iri == null && cfg.subject.iriJson != null ) {
            // here you’d call JSONPath; using dotted fallback:
            List<String> vals = new JsonValueFinder( source ).findByDotted( trimJsonPath( cfg.subject.iriJson ) );
            iri = vals.isEmpty() ? null : vals.get( 0 );
        }
        if ( iri == null ) {
            // last resort: blank node catalog
            return m.createResource();
        }
        return m.createResource( iri );
    }

    private void addProperty(Model model, Resource subject, JsonValueFinder finder, MappingModel.ValueSource valueSource) {
        String predIri = prefixes.expand( valueSource.predicate );
        if ( predIri == null ) {
            return;
        }
        Property p = model.createProperty( predIri );

        // Produce zero..n object values
        List<RDFNode> objects = resolveObjects( model, finder, valueSource );
        for ( RDFNode o : objects ) {
            subject.addProperty( p, o );
        }
    }

    private List<RDFNode> resolveObjects(Model model, JsonValueFinder finder, MappingModel.ValueSource valueSource) {
        switch ( valueSource.as ) {
            case "node-ref":
                // Java 8: use Collections.singletonList instead of List.of
                return List.of( buildNodeRef( model, valueSource ) );

            case "iri":
                return valuesFromSource( finder, valueSource ).stream()
                                                              // applyMapIfAny(vs) returns Function<String,String>, which Stream.map accepts
                                                              .map( applyMapIfAny( valueSource ) ).filter( Objects::nonNull ).map( model::createResource ) // turn IRI strings into Jena Resources
                                                              .collect( Collectors.toList() );

            case "literal":
            default:
                return valuesFromSource( finder, valueSource ).stream().map( applyMapIfAny( valueSource ) ).filter( Objects::nonNull )
                                                              .map( val -> literal( model, val, valueSource.lang, valueSource.datatype ) ).collect( Collectors.toList() );
        }
    }

    private RDFNode buildNodeRef(Model model, MappingModel.ValueSource valueSource) {
        MappingModel.NodeTemplate nodeTemplate = cfg.nodes.get( valueSource.nodeRef );
        if ( nodeTemplate == null ) {
            return model.createResource(); // bnode
        }
        Resource r = "iri".equals( nodeTemplate.kind ) && nodeTemplate.iriConst != null ? model.createResource( nodeTemplate.iriConst ) : model.createResource(); // bnode
        if ( nodeTemplate.type != null ) {
            r.addProperty( RDF.type, model.createResource( prefixes.expand( nodeTemplate.type ) ) );
        }
        // materialize node’s internal properties
        nodeTemplate.props.forEach( (pid, pvs) -> {
            Property p = model.createProperty( prefixes.expand( pvs.predicate ) );
            for ( RDFNode obj : resolveObjects( model, new JsonValueFinder( null ), pvs ) ) {
                r.addProperty( p, obj );
            }
        } );
        return r;
    }

    private List<String> valuesFromSource(JsonValueFinder finder, MappingModel.ValueSource valueSource) {
        if ( valueSource.constValue != null ) {
            return List.of( valueSource.constValue );
        }
        if ( valueSource.json != null && finder != null ) {
            String path = trimJsonPath( valueSource.json );
            List<String> vals = finder.findByDotted( path );
            if ( valueSource.multi ) {
                return vals;
            }
            // if array but multi=false -> take first
            return vals.isEmpty() ? List.of() : List.of( vals.get( 0 ) );
        }
        return List.of();
    }

    private String trimJsonPath(String json) {
        // Convert $.a.b to a.b if needed
        if ( json.startsWith( "$." ) ) {
            return json.substring( 2 );
        }
        if ( json.startsWith( "$" ) ) {
            return json.substring( 1 );
        }
        return json;
    }

    private Function<String, String> applyMapIfAny(MappingModel.ValueSource valueSource) {
        return s -> {
            if ( s == null ) {
                return null;
            }
            if ( !valueSource.map.isEmpty() ) {
                return valueSource.map.getOrDefault( s, null ); // drop if unmapped
            }
            return s;
        };
    }

    private Literal literal(Model model, String value, String lang, String datatypeIri) {
        if ( datatypeIri != null && !datatypeIri.isBlank() ) {
            RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName( datatypeIri );
            return model.createTypedLiteral( value, dt );
        }
        if ( lang != null && !lang.isBlank() ) {
            return model.createLiteral( value, lang );
        }
        return model.createLiteral( value );
    }
}
