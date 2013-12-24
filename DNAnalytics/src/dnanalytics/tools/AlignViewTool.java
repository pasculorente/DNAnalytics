package dnanalytics.tools;

import dnanalytics.utils.Settings;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.AlignViewController;
import dnanalytics.worker.Aligner;
import dnanalytics.worker.Worker;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class AlignViewTool implements Tool {

    private final static ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private AlignViewController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(AlignViewController.class.getResource("AlignView.fxml"), resources);
            try {
                view = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(AlignViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        // Check for all params
        if (Settings.getGenome() == null || !Settings.getGenome().exists()) {
            System.err.println(resources.getString("no.genome"));
            return null;
        }
        if (!new File(Settings.getProperty("tempDir")).exists()) {
            System.err.println(resources.getString("no.temp"));
            return null;
        }
        if (!new File(controller.getForward()).exists()) {
            System.err.println(resources.getString("no.forward"));
            return null;
        }
        if (!new File(controller.getReverse()).exists()) {
            System.err.println(resources.getString("no.reverse"));
            return null;
        }
        if (!new File(controller.getMills()).exists()) {
            System.err.println(resources.getString("no.mills"));
            return null;
        }
        if (!new File(controller.getPhase1()).exists()) {
            System.err.println(resources.getString("no.phase1"));
            return null;
        }
        if (!new File(controller.getDbsnp()).exists()) {
            System.err.println(resources.getString("no.dbsnp"));
            return null;
        }
        if (controller.getOutput().isEmpty()) {
            System.err.println(resources.getString("no.output"));
            return null;
        }

        // Everything seems legal. Launch Worker.
        return new Aligner(Settings.getProperty("tempDir"),
                controller.getForward(), controller.getReverse(),
                Settings.getGenome().getAbsolutePath(), controller.getDbsnp(),
                controller.getMills(), controller.getPhase1(), controller.getOutput(),
                controller.getEncoding().equals("isIllumina"), controller.isReduce());
    }

    @Override
    public String getTitle() {
        return resources.getString("align.title");
    }

    @Override
    public String getStyleID() {
        return "alignb";
    }
}
