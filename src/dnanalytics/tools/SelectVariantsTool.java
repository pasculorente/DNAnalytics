package dnanalytics.tools;

import dnanalytics.DNAnalytics;
import dnanalytics.utils.Command;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.SelectVariantsController;
import dnanalytics.worker.Worker;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
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
            loader = new FXMLLoader(SelectVariantsController.class.
                    getResource("SelectVariants.fxml"), resources);
            try {
                view = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(SelectVariantsController.class.getName()).log(Level.SEVERE, null,
                        ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {

        // The Worker selecter is created, it will select the variants in background
        return new Worker() {
            Properties properties = DNAnalytics.getProperties();
            private final static String java7 = "/usr/local/jdk1.7.0_45/bin/java";

            @Override
            protected int start() {
                updateTitle("Selecting " + new File(controller.getInput()).getName());
                updateMessage(resources.getString("select.select"));
                // java -jar GenomeAnalysisTK.jar
                // -T Selectvariants -R genome.fasta
                // -V input.vcf - o output.vcf
                // -restrictAllelesTo BIALLELIC
                // -select "expression"
                new Command(java7, "-jar",
                        "software" + File.separator + "gatk"
                        + File.separator + "GenomeAnalysisTK.jar",
                        "-T", "SelectVariants",
                        "-R", properties.getProperty("genome"),
                        "-V", controller.getInput(),
                        "-restrictAllelesTo", "BIALLELIC",
                        "-o", controller.getOutput(),
                        "-select", controller.getExpression()).execute(outStream);
                return 0;
            }

            @Override
            public boolean importParameters() {
                // Check the params in the main thread, to avoid launching a dummy Worker.
                if (!new File(properties.getProperty("genome")).exists()) {
                    System.err.println(resources.getString("no.genome"));
                    return false;
                }
                if (!new File(controller.getInput()).exists()) {
                    System.err.println(resources.getString("no.input"));
                    return false;
                }
                if (controller.getOutput().isEmpty()) {
                    System.err.println(resources.getString("no.output"));
                    return false;
                }
                if (controller.getExpression().isEmpty()) {
                    System.err.println(resources.getString("no.expression"));
                    return false;
                }
                return true;
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

    @Override
    public String getDescription() {
        return resources.getString("select.description");
    }
}
