package dnanalytics.tools;

import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.IndexFastaController;
import dnanalytics.worker.Worker;
import dnanalytics.worker.WorkerScript;
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
        return new WorkerScript() {
            @Override
            protected int start() {
                updateTitle("Indexing " + new File(controller.getGenome()).getName());
                updateMessage(resources.getString("index.index"));
                return executeCommand("bwa index -a bwtsw " + controller.getGenome());
            }

            @Override
            public boolean checkParameters() {

                if (!new File(controller.getGenome()).exists()) {
                    System.err.println(resources.getString("no.genome"));
                    return false;
                }
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
