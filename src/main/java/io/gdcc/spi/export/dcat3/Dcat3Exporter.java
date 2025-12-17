
package io.gdcc.spi.export.dcat3;

import static io.gdcc.spi.export.dcat3.config.loader.FileResolver.resolveElementFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;

import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import io.gdcc.spi.export.Exporter;
import io.gdcc.spi.export.dcat3.config.loader.ResourceConfigLoader;
import io.gdcc.spi.export.dcat3.config.loader.RootConfigLoader;
import io.gdcc.spi.export.dcat3.config.model.ResourceConfig;
import io.gdcc.spi.export.dcat3.config.model.Element;
import io.gdcc.spi.export.dcat3.config.model.Relation;
import io.gdcc.spi.export.dcat3.config.model.RootConfig;
import io.gdcc.spi.export.dcat3.mapping.JaywayJsonFinder;
import io.gdcc.spi.export.dcat3.mapping.Prefixes;
import io.gdcc.spi.export.dcat3.mapping.ResourceMapper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

@AutoService( Exporter.class )
public class Dcat3Exporter implements Exporter {

    private static final Logger logger = Logger.getLogger( Dcat3Exporter.class.getCanonicalName());

    private RootConfig root;

    public Dcat3Exporter() {
        try {
            this.root = RootConfigLoader.load();
        }
        catch ( IOException e ) {
            logger.warning( "cannot read configuration: " + e.getMessage() );
        }
    }

    @Override
    public String getFormatName() {
        return "dcat3";
    }

    @Override
    public String getDisplayName(Locale locale) {
        return "DCAT-3";
    }

    @Override
    public Boolean isHarvestable() {
        return true;
    }

    @Override
    public Boolean isAvailableToUsers() {
        return true;
    }

    @Override
    public String getMediaType() {
        // Use root.outputFormat
        switch ( root.outputFormat.toLowerCase( Locale.ROOT ) ) {
            case "rdfxml":
                return "application/rdf+xml";
            case "jsonld":
                return "application/ld+json";
            default:
                return "text/turtle";
        }
    }

    @Override
    public void exportDataset(ExportDataProvider dataProvider, OutputStream outputStream) throws ExportException {
        try {
            ExportData exportData = ExportData.builder().provider( dataProvider ).build();
            ObjectMapper mapper = new ObjectMapper();
            if ( root.trace ) {
                try {
                    String json = mapper.writerWithDefaultPrettyPrinter()
                                        .writeValueAsString( exportData );
                    logger.info( json );
                }
                catch ( JsonProcessingException e ) {
                    logger.warning( e.getMessage() );
                    // does not make sense to continue export on this error
                    return;
                }
            }

            JsonNode rootJson = mapper.valueToTree( exportData );
            JaywayJsonFinder jaywayJsonFinder = new JaywayJsonFinder( rootJson );

            // Build each element
            Map<String, Model> models = new LinkedHashMap<>();
            Map<String, List<Resource>> subjects = new LinkedHashMap<>();
            Prefixes prefixes = new Prefixes( root.prefixes );

            for ( Element element : root.elements ) {
                // Load the element mapping through RootConfigLoader (relative to root file dir)
                try (InputStream in = resolveElementFile( root.baseDir, element.file ) ) {
                    ResourceConfig resourceConfig = new ResourceConfigLoader().load( in );

                    ResourceMapper resourceMapper = new ResourceMapper( resourceConfig, prefixes, element.typeCurieOrIri );
                    Model model = resourceMapper.build( jaywayJsonFinder );

                    // Collect all subjects by rdf:type
                    String typeIri = prefixes.expand( element.typeCurieOrIri );
                    ResIterator it = model.listResourcesWithProperty( RDF.type, model.createResource(typeIri) );
                    List<Resource> subjectList = new ArrayList<>();
                    while (it.hasNext()) {
                        subjectList.add(it.next());
                    }
                    models.put( element.id, model );
                    if ( !subjectList.isEmpty() ) {
                        subjects.put( element.id, subjectList );
                    }
                }
            }

            // Merge all element models
            Model model = ModelFactory.createDefaultModel();
            model.setNsPrefixes( prefixes.jena() );
            models.values().forEach( model::add );

            // Apply relations from root (n:m)
            for ( Relation relation : root.relations ) {
                List<Resource> subjList = subjects.get( relation.subjectElementId );
                List<Resource> objList  = subjects.get( relation.objectElementId );
                if (subjList == null || subjList.isEmpty() || objList == null || objList.isEmpty()) {
                    continue; // could log a warning based on r.cardinality
                }
                Property property = model.createProperty( prefixes.expand( relation.predicateCurieOrIri ) );
                for (Resource s : subjList) {
                    for (Resource o : objList) {
                        model.add(s, property, o);
                    }
                }
            }

            // Serialize in the configured format
            String outputFormat = root.outputFormat.toLowerCase( Locale.ROOT ).trim();
            switch ( outputFormat ) {
                case "rdfxml":
                    model.write( outputStream, "RDF/XML" );
                    break;
                case "jsonld":
                    model.write( outputStream, "JSON-LD" );
                    break;
                default:
                    model.write( outputStream, "TURTLE" );
                    break;
            }
        }
        catch ( Throwable t ) {
            throw new ExportException( "DCAT export failed", t );
        }
    }
}
