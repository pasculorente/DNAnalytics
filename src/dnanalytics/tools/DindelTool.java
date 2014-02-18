package dnanalytics.tools;

import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.DindelController;
import dnanalytics.worker.DindelWorker;
import dnanalytics.worker.Worker;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class DindelTool implements Tool {

    private final static ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private DindelController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(DindelController.class.getResource("Dindel.fxml"), resources);
            try {
                view = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(DindelController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return new DindelWorker(controller.getInput(), controller.getOutput());
    }

    @Override
    public String getTitle() {
        return resources.getString("dindel.title");
//        return "Dindel (Indels call)";
    }

    @Override
    public String getStyleID() {
        return "dindel";
    }

    @Override
    public String getDescription() {
        return resources.getString("dindel.description");
    }

}
