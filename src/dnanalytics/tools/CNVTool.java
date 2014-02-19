package dnanalytics.tools;

import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.CNVController;
import dnanalytics.worker.Worker;
import dnanalytics.worker.XHMM;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 *
 * @author uichuimi03
 */
public class CNVTool implements Tool {

    private final static ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private CNVController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(CNVController.class.getResource("CNVView.fxml"), resources);
            try {
                view = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(CNVController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return new XHMM(controller.getInput(), controller.getOutput());
    }

    @Override
    public String getTitle() {
        return resources.getString("cnv.title");
    }

    @Override
    public String getIco() {
        return "cnv32.png";
    }

    @Override
    public String getDescription() {
        return resources.getString("cnv.description");
    }

}
