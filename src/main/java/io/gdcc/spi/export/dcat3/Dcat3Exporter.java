package io.gdcc.spi.export.dcat3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
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
import io.gdcc.spi.export.dcat3.config.model.Config;
import io.gdcc.spi.export.dcat3.config.model.Element;
import io.gdcc.spi.export.dcat3.config.loader.PropertiesMappingLoader;
import io.gdcc.spi.export.dcat3.config.model.Relation;
import io.gdcc.spi.export.dcat3.config.model.RootConfig;
import io.gdcc.spi.export.dcat3.config.loader.RootConfigLoader;
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

    /*
     * The name of the format it creates. If this format is already provided by a
     * built-in exporter, this Exporter will override the built-in one. (Note that
     * exports are cached, so existing metadata export files are not updated
     * immediately.)
     */
    @Override
    public String getFormatName() {
        return "dcat3";
    }

    /**
     * The display name shown in the UI
     *
     * @param locale
     */
    @Override
    public String getDisplayName(Locale locale) {
        // This example includes the language in the name to demonstrate that locale is
        // available. A production exporter would instead use the locale to generate an
        // appropriate translation.
        return "DCAT-3";
    }

    /**
     * Whether the exported format should be available as an option for Harvesting
     */
    @Override
    public Boolean isHarvestable() {
        return true;
    }

    /**
     * Whether the exported format should be available for download in the UI and API
     */
    @Override
    public Boolean isAvailableToUsers() {
        return true;
    }

    /**
     * Defines the mime type of the exported format - used when metadata is downloaded, i.e. to
     * trigger an appropriate viewer in the user's browser.
     */
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

            // Build each element
            Map<String, Model> models = new LinkedHashMap<>();
            Map<String, Resource> subjects = new LinkedHashMap<>();
            Prefixes prefixes = new Prefixes( root.prefixes );

            for ( Element element : root.elements ) {
                // Load the element mapping through RootConfigLoader (relative to root file dir)
                try (InputStream in = RootConfigLoader.resolveElementFile( root, element.file ) ) {
                    Config modelConfig = new PropertiesMappingLoader().load( in );

                    ResourceMapper resourceMapper = new ResourceMapper( modelConfig, prefixes, element.typeCurieOrIri );
                    Model model = resourceMapper.build( rootJson );

                    // Remember model & try to locate the subject by rdf:type
                    String typeIri = prefixes.expand( element.typeCurieOrIri );
                    ResIterator it = model.listResourcesWithProperty( RDF.type, model.createResource( typeIri ) );
                    Resource subj = it.hasNext() ? it.next() : null;

                    models.put( element.id, model );
                    if ( subj != null ) {
                        subjects.put( element.id, subj );
                    }
                }
            }

            // Merge all element models
            Model model = ModelFactory.createDefaultModel();
            model.setNsPrefixes( prefixes.jena() );
            models.values().forEach( model::add );

            // Apply relations from root
            for ( Relation relation : root.relations ) {
                Resource subject = subjects.get( relation.subjectElementId );
                Resource objectElementId = subjects.get( relation.objectElementId );
                if ( subject == null || objectElementId == null ) {
                    continue; // could log a warning based on r.cardinality
                }

                Property property = model.createProperty( prefixes.expand( relation.predicateCurieOrIri ) );
                model.add( subject, property, objectElementId );
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
