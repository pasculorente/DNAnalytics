package dnanalytics.tools;

import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.LowFrequencyController;
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
                view = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(LowFrequencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        System.out.println("Called"
                + " " + controller.getInput()
                + " " + controller.getOutput()
                + " " + controller.getThreshold()
                + ", but not supported yet");
        return null;
    }

    @Override
    public String getTitle() {
        return "Low Frequency";
    }

    @Override
    public String getStyleID() {
        return "lowFrequency";
    }

}
