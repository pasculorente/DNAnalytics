package dnanalytics.tools;

import dnanalytics.utils.Command;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.IndexFastaController;
import dnanalytics.worker.Worker;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class IndexFastaTool implements Tool {

    private final static ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private IndexFastaController controller;

    private final ArrayList<Command> commands = new ArrayList<>();
    private final ArrayList<String> messages = new ArrayList<>();

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(IndexFastaController.class.getResource("IndexFasta.fxml"),
                    resources);
            try {
                view = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(IndexFastaController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return new Worker() {
            String genome;
            Command command = null;

            @Override
            public boolean cancel(boolean bln) {
                boolean ret = super.cancel(bln);
                if (command != null) {
                    command.kill();
                }
                return ret;
            }

            @Override
            protected int start() {
                genome = controller.getGenome();
                updateTitle(resources.getString("index.index") + " " + new File(genome).getName());
                updateProgress(resources.getString("index.bwa"), 0.5, 3);
                new Command("bwa", "index", "-a", "bwtsw", genome).execute(outStream);
                updateProgress(resources.getString("index.samtools"), 1.5, 3);
                new Command("samtools", "faidx", genome).execute(outStream);
                updateProgress(resources.getString("index.picard"), 2.5, 3);
                new Command("java", "-jar", "software" + File.separator
                        + "picard" + File.separator + "CreateSequenceDictionary.jar",
                        "R=" + genome, "O=" + genome.replace(".fasta", ".dict")).execute(outStream,
                                true);
                updateProgress(resources.getString("index.end"), 1, 1);
                return 0;
            }

            @Override
            public boolean importParameters() {
                if (!new File(controller.getGenome()).exists()) {
                    DNAMain.printMessage(resources.getString("no.genome"));
                    return false;
                }

                return true;
            }

        };
    }

    @Override
    public String getTitle() {
        return resources.getString("index.title");
    }

    @Override
    public String getIco() {
        return "index32.png";
    }

    @Override
    public String getDescription() {
        return resources.getString("index.description");
    }

}
