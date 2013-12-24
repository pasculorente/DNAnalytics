package dnanalytics.tools;

import dnanalytics.worker.WorkerScript;
import dnanalytics.utils.Settings;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.SelectVariantsController;
import dnanalytics.worker.Worker;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class SelectVariantsTool implements Tool {

    private static final ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private SelectVariantsController controller;
    
    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(SelectVariantsController.class.getResource("SelectVariants.fxml"), resources);
            try {
                view = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(SelectVariantsController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        // Check the params in the main thread, to avoid launching a dummy Worker.
        if (Settings.getGenome() == null) {
            System.err.println(resources.getString("no.genome"));
            return null;
        }
        if (!new File(controller.getInput()).exists()) {
            System.err.println(resources.getString("no.input"));
            return null;
        }
        if (controller.getOutput().isEmpty()) {
            System.err.println(resources.getString("no.output"));
            return null;
        }
        if (controller.getExpression().isEmpty()) {
            System.err.println(resources.getString("no.expression"));
            return null;
        }

        // The Worker selecter is created, it will select the variants in background
        return new WorkerScript() {
            @Override
            protected int start() {
                updateTitle("Selecting " + new File(controller.getInput()).getName());
                updateMessage(resources.getString("select.select"));
                executeCommand(
                        "java -jar software" + File.separator + "gatk" + File.separator + "GenomeAnalysisTK.jar"
                        + " -T SelectVariants"
                        + " -R " + Settings.getGenome()
                        + " -V " + controller.getInput()
                        + " -restrictAllelesTo BIALLELIC"
                        + " -o " + controller.getOutput()
                        + " -select \"" + controller.getExpression() + '\"');
                return 0;
            }
        };
    }

    @Override
    public String getTitle() {
        return resources.getString("select.title");
    }

    @Override
    public String getStyleID() {
        return "selectVariants";
    }
}
