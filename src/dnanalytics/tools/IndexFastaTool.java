package dnanalytics.tools;

import dnanalytics.utils.Command;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.IndexFastaController;
import dnanalytics.worker.Worker;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class IndexFastaTool implements Tool {

    private final static ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private IndexFastaController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(IndexFastaController.class.getResource("IndexFasta.fxml"),
                    resources);
            try {
                view = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(IndexFastaController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return new Worker() {
            String genome;

            @Override
            protected int start() {
                updateTitle(resources.getString("index.index") + " " + new File(genome).getName());
                updateProgress(resources.getString("index.bwa"), 0.5, 3);
                new Command(outStream, "bwa", "index", "-a", "bwtsw", genome).execute();
                updateProgress(resources.getString("index.samtools"), 1.5, 3);
                new Command("samtools", "faidx", controller.getGenome()).execute();
                updateProgress(resources.getString("index.picard"), 2.5, 3);
                new Command(outStream, "java", "-jar", "software" + File.separator
                        + "picard" + File.separator + "CreateSequenceDictionary.jar",
                        "R=" + genome, "O=" + genome.replace(".fasta", ".dict")).execute();
                updateProgress(resources.getString("index.end"), 1, 1);
                return 0;
            }

            @Override
            public boolean importParameters() {

                if (!new File(controller.getGenome()).exists()) {
                    System.err.println(resources.getString("no.genome"));
                    return false;
                }
                genome = controller.getGenome();
                return true;
            }
        };
    }

    @Override
    public String getTitle() {
        return resources.getString("index.title");
    }

    @Override
    public String getStyleID() {
        return "indexFasta";
    }

}
