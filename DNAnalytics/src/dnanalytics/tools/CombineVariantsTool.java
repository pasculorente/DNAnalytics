package dnanalytics.tools;

import dnanalytics.DNAnalytics;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.CombineVariantsController;
import dnanalytics.worker.Worker;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListView;

/**
 * FXML Controller class
 * @author Pascual Lorente Arencibia
 */
public class CombineVariantsTool implements Tool {

    private static final ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private CombineVariantsController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(CombineVariantsController.class.getResource(
                    "CombineVariants.fxml"), resources);
            try {
                view = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(CombineVariantsController.class.getName()).log(Level.SEVERE, null,
                        ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return new Worker() {
            ListView<String> vcfList = controller.getVcfList();
            String gatk = "";
            String variants = "";
            String genome = DNAnalytics.getProperties().getProperty("genome");
            
            private void initializeSettings() {
                variants = vcfList.getItems().stream().map((var) -> " -V " + var).reduce(variants,
                        String::concat);

                gatk = "java -jar software" + File.separator + "gatk" + File.separator
                        + "GenomeAnalysisTK.jar";
            }

            private void intersectVariants() {
                executeCommand(gatk
                        + " -T CombineVariants"
                        + " -R " + genome
                        + " -minN " + vcfList.getItems().size()
                        + " -o " + controller.getCombinedVCF()
                        + variants);

            }

            private void aggregateVariants() {
                executeCommand(gatk
                        + " -T CombineVariants"
                        + " -R " + genome
                        + " -o " + controller.getCombinedVCF()
                        + variants);
            }

            private void deductVariants() {
                executeCommand(gatk
                        + " -T SelectVariants"
                        + " -R " + genome
                        + " -V " + vcfList.getItems().get(0)
                        + " -o " + controller.getCombinedVCF()
                        + " --discordance " + vcfList.getItems().get(1));
            }

            @Override
            protected int start() {
                initializeSettings();
                updateTitle("combinig " + new File(controller.getCombinedVCF()).getName());

                switch (controller.getOperation()) {
                case "intersection":
                    intersectVariants();
                    break;
                case "aggregation":
                    aggregateVariants();
                    break;
                case "difference":
                    deductVariants();
                }
                return 0;
            }

            @Override
            public boolean importParameters() {
                if (!new File(genome).exists()) {
                    System.err.println(DNAMain.getResources().getString("no.genome"));
                    return false;
                }
                if (controller.getCombinedVCF().isEmpty()) {
                    System.err.println(DNAMain.getResources().getString("no.output"));
                    return false;
                }
                return true;
            }

        };
    }

    @Override
    public String getTitle() {
        return resources.getString("combine.title");
    }

    @Override
    public String getStyleID() {
        return "combineVariants";
    }
}
