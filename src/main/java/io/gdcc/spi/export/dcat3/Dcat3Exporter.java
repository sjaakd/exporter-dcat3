package io.gdcc.spi.export.dcat3;

import java.io.OutputStream;
import java.util.Locale;

import com.google.auto.service.AutoService;
import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import io.gdcc.spi.export.Exporter;
import io.gdcc.spi.export.parsing.ExportData;
import jakarta.ws.rs.core.MediaType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

@AutoService(Exporter.class)
public class Dcat3Exporter implements Exporter {

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

    /** Whether the exported format should be available as an option for Harvesting */
    @Override
    public Boolean isHarvestable() {
        return false;
    }

    /** Whether the exported format should be available for download in the UI and API */
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

        // Create a catalog
        Resource catalog = model.createResource("http://example.org/catalog")
                                .addProperty( RDF.type, DCAT.Catalog)
                                .addProperty( DCTerms.title, exportData.getDatasetORE().oreDescribes().title() );

        // Create a dataset
        Resource dataset = model.createResource("http://example.org/dataset")
                                .addProperty(RDF.type, DCAT.Dataset)
                                .addProperty(DCTerms.title, "Example Dataset")
                                .addProperty(DCTerms.description, "This is an example dataset.");

        // Add the dataset to the catalog
        catalog.addProperty(DCAT.dataset, dataset);

        // Write the model to the console
        model.write(System.out, "TURTLE");
    }
}
