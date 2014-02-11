package dnanalytics.tools;

import dnanalytics.DNAnalytics;
import dnanalytics.utils.Command;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.CombineVariantsController;
import dnanalytics.worker.Worker;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 * FXML Controller class
 *
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
                view = (Node) loader.load();
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
            private final static String java7 = "/usr/local/jdk1.7.0_45/bin/java";

            @Override
            protected int start() {
                ObservableList<String> items = controller.getVcfList().getItems();
                updateTitle("Combinig " + new File(controller.getCombinedVCF()).getName());
                ArrayList<String> command = new ArrayList<>();
                // java7 -jar GenomeAnalysisTK.jar
                // -R genome.fasta -o output.vcf
                String[] common = {java7, "-jar",
                    "software" + File.separator + "gatk"
                    + File.separator + "GenomeAnalysisTK.jar",
                    "-R", DNAnalytics.getProperties().getProperty("genome"),
                    "-o", controller.getCombinedVCF()
                };
                command.addAll(Arrays.asList(common));
                switch (controller.getOperation()) {
                    case "intersection":
                        // -minN 3
                        // -T CombineVariants
                        // -V variats1.vcf -V variants2.vcf -V variants3.vcf
                        command.add("-minN");
                        command.add(String.valueOf(items.size()));
                        command.add("-T");
                        command.add("CombineVariants");
                        items.stream().forEach((s) -> {
                            command.add("-V");
                            command.add(s);
                        });
                        updateProgress(resources.getString("combine.intersect"), 1, 2);
                        break;
                    case "aggregation":
                        // -T CombineVariants
                        // -V variats1.vcf -V variants2.vcf -V variants3.vcf
                        command.add("-T");
                        command.add("CombineVariants");
                        for (String s : items) {
                            command.add("-V");
                            command.add(s);
                        }
                        updateProgress(resources.getString("combine.aggregate"), 1, 2);
                        break;
                    case "difference":
                        // -T SelectVariants
                        // -V variants1.vcf
                        // --discordance variants2.vcf
                        command.add("-T");
                        command.add("SelectVariants");
                        command.add("-V");
                        command.add(items.get(0));
                        command.add("--discordance");
                        command.add(items.get(1));
                        updateProgress(resources.getString("combine.difference"), 1, 2);
                }
                String[] args = new String[command.size()];
                for (int i = 0; i < command.size(); i++) {
                    args[i] = command.get(i);
                }
                return new Command(outStream, args).execute();
            }

            @Override
            public boolean importParameters() {
                if (!new File(DNAnalytics.getProperties().getProperty("genome")).exists()) {
                    System.err.println(resources.getString("no.genome"));
                    return false;
                }
                if (controller.getCombinedVCF().isEmpty()) {
                    System.err.println(resources.getString("no.output"));
                    return false;
                }
                if (controller.getVcfList().getItems().size() == 0) {
                    System.err.println(resources.getString("no.input"));
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
