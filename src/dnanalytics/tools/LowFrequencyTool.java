package dnanalytics.tools;

import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.LowFrequencyController;
import dnanalytics.worker.BAMAnalyzer;
import dnanalytics.worker.Worker;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 *
 * @author Pascual
 */
public class LowFrequencyTool implements Tool {

    private static final ResourceBundle resources = DNAMain.getResources();
    private Node view;
    private FXMLLoader loader;
    private LowFrequencyController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(LowFrequencyController.class.getResource("LowFrequency.fxml"), resources);
            try {
                view = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(LowFrequencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return new BAMAnalyzer(controller.getInput(), controller.getOutput(), controller.getThreshold());
    }

    @Override
    public String getTitle() {
        return "Low Frequency";
    }

    @Override
    public String getIco() {
        return "lowFrequency";
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
