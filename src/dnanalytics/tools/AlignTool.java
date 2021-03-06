package dnanalytics.tools;

import dnanalytics.DNAnalytics;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.AlignViewController;
import dnanalytics.worker.Aligner;
import dnanalytics.worker.Worker;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class AlignTool implements Tool {

    private final static ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private AlignViewController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(AlignViewController.class.getResource("AlignView.fxml"), resources);
            try {
                view = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(AlignViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return new Aligner(DNAnalytics.getProperties().getProperty("tempDir"),
                controller.getForward(), controller.getReverse(),
                DNAnalytics.getProperties().getProperty("genome"), controller.getDbsnp(),
                controller.getMills(), controller.getPhase1(), controller.getOutput(),
                controller.getEncoding().equals("isIllumina"), controller.isReduce());
    }

    @Override
    public String getTitle() {
        return resources.getString("align.title");
    }

    @Override
    public String getIco() {
        return "align32.png";
    }

    @Override
    public String getDescription() {
        return resources.getString("align.description");
    }
}
