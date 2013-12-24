package dnanalytics.tools;

import dnanalytics.utils.Settings;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.CallVariantsController;
import dnanalytics.worker.Haplotype;
import dnanalytics.worker.Worker;
import java.io.File;
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
            loader = new FXMLLoader(CallVariantsController.class.getResource("CallVariants.fxml"), resources);
            try {
                view = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(CallVariantsController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    private boolean checkParameters() {
        // Check for params
        // These files must exist: genome, tempDir, dbsnp, input
        // These string must not be empty: output
        // If recalibration, these files must exist: hapmap, omni, mills
        if (Settings.getGenome() == null || !Settings.getGenome().exists()) {
            System.err.println(resources.getString("no.genome"));
            return false;
        }
        if (!new File(controller.getDbsnp()).exists()) {
            System.err.println(resources.getString("no.dbsnp"));
            return false;
        }
        if (!new File(Settings.getProperty("tempDir")).exists()) {
            System.err.println(resources.getString("no.temp"));
            return false;
        }
        if (controller.getOutput().isEmpty()) {
            System.err.println(resources.getString("no.output"));
            return false;
        }
        if (!new File(controller.getInput()).exists()) {
            System.err.println(resources.getString("no.input"));
            return false;
        }
        if (controller.isRecalibrate()) {
            // Check for database
            if (!new File(controller.getMills()).exists()) {
                System.err.println(resources.getString("no.mills"));
                return false;
            }
            if (!new File(controller.getHapmap()).exists()) {
                System.err.println(resources.getString("no.hapmap"));
                return false;
            }
            if (!new File(controller.getOmni()).exists()) {
                System.err.println(resources.getString("no.omni"));
                return false;
            }
        }
        return true;
    }

    @Override
    public Worker getWorker() {

        if (checkParameters()) {
            return new Haplotype(
                    Settings.getGenome().getAbsolutePath(), controller.getDbsnp(),
                    controller.getOmni(), controller.getHapmap(), controller.getMills(),
                    controller.getOutput(), controller.getInput(), controller.isRecalibrate(),
                    Settings.getProperty("tempDir"));
        } else {
            return null;
        }

    }

    @Override
    public String getTitle() {
        return resources.getString("call.title");
    }

    @Override
    public String getStyleID() {
        return "callVariants";
    }
}
