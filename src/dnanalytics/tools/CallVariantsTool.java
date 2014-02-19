package dnanalytics.tools;

import dnanalytics.DNAnalytics;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.CallVariantsController;
import dnanalytics.worker.Haplotype;
import dnanalytics.worker.Worker;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class CallVariantsTool implements Tool {

    private static final ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private CallVariantsController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(CallVariantsController.class.getResource("CallVariants.fxml"),
                    resources);
            try {
                view = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(CallVariantsController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return new Haplotype(
                DNAnalytics.getProperties().getProperty("genome"), controller.getDbsnp(),
                controller.getOmni(), controller.getHapmap(), controller.getMills(),
                controller.getOutput(), controller.getInput(), controller.isRecalibrate(),
                DNAnalytics.getProperties().getProperty("tempDir"));
    }

    @Override
    public String getTitle() {
        return resources.getString("call.title");
    }

    @Override
    public String getIco() {
        return "call32.png";
    }

    @Override
    public String getDescription() {
        return resources.getString("call.description");
    }
}
