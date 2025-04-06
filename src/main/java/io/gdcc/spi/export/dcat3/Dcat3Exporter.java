package io.gdcc.spi.export.dcat3;

import java.io.OutputStream;
import java.util.Locale;

import com.google.auto.service.AutoService;
import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import io.gdcc.spi.export.Exporter;
import io.gdcc.spi.export.parsing.ExportData;
import io.gdcc.spi.export.parsing.datasetORE.SchemaIsPartOf;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD;

@AutoService( Exporter.class )
public class Dcat3Exporter implements Exporter {

    public static final String NL = "nl";
    public static final String EN = "en";

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
        return false;
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
        return " application/rdf+xml";
    }

    /**
     * This method is called by Dataverse when metadata for a given dataset in this format is
     * requested.
     */
    @Override
    public void exportDataset(ExportDataProvider dataProvider, OutputStream outputStream) throws ExportException {

        ExportData exportData = ExportData.builder().provider( dataProvider ).build();
        Model model = ModelFactory.createDefaultModel();

        String exampleMS = "http://example.org/resource/";

        // Add prefixes to the model
        model.setNsPrefix( "dcat", DCAT.NS );
        model.setNsPrefix( "dct", DCTerms.NS );
        model.setNsPrefix( "vcard", VCARD.uri );
        model.setNsPrefix( "foaf", FOAF.NS );
        model.setNsPrefix( "exampleMS", exampleMS );

        // Create a catalog
        Resource catalog = createCatalog( model, exportData.getDatasetORE().oreDescribes().schemaIsPartOf(), exampleMS );

        // Create a dataset
        Resource dataset = model.createResource( exampleMS )
                                .addProperty( RDF.type, DCAT.Dataset )
                                .addProperty( DCTerms.title, "Example Dataset" )
                                .addProperty( DCTerms.description, "This is an example dataset." );

        // Add the dataset to the catalog
        catalog.addProperty( DCAT.dataset, dataset );

        // Write the model to the console
        model.write( System.out, "TURTLE" );
    }

    private Resource createCatalog(Model model, SchemaIsPartOf schemaIsPartOf, String exampleMS) {

        // Create the catalog. TODO: origin of this data is dubious.
        Resource catalog = model.createResource( exampleMS )
                                .addProperty( RDF.type, DCAT.Catalog )
                                .addProperty( DCTerms.title, model.createLiteral( schemaIsPartOf.schemaName(), NL ) )
                                .addProperty( DCTerms.description, model.createLiteral( schemaIsPartOf.schemaDescription(), NL ) );

        // Create the contact point. TODO: where to get the data from?
        Resource contactPoint = model.createResource()
                                     .addProperty( RDF.type, VCARD.AGENT )
                                     .addProperty( VCARD.FN, model.createLiteral( "Geologische Dienst Nederland", NL ) )
                                     .addProperty( VCARD.FN, model.createLiteral( "Geological Survey of the Netherlands", EN ) )
                                     .addProperty( VCARD.EMAIL, model.createResource( "mailto:support@geologischedienst.nl" ) )
                                     .addProperty( VCARD.ORG, model.createResource( "https://www.geologischedienst.nl/" ) )
                                     .addProperty( VCARD.Orgunit, model.createLiteral( "Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk Onderzoek (nl), TNO", NL ) )
                                     .addProperty( VCARD.Orgunit, model.createLiteral( "Netherlands Organisation for Applied Scientific Research", EN ) );

        catalog.addProperty( DCAT.contactPoint, contactPoint );

        // Create the publisher. TODO: where to get the data from?
        Resource publisher = model.createResource()
                                  .addProperty( RDF.type, FOAF.Agent )
                                  .addProperty( DCTerms.type, model.createResource( "https://ror.org/01bnjb948" ) )
                                  .addProperty( FOAF.name, model.createLiteral( "Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk Onderzoek (nl), TNO", NL ) )
                                  .addProperty( FOAF.name, model.createLiteral( "Netherlands Organisation for Applied Scientific Research", EN ) );

        catalog.addProperty( DCTerms.publisher, publisher );

        return catalog;
    }
}
