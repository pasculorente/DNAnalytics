package dnanalytics.view.tools;

import dnanalytics.utils.FileManager;
import dnanalytics.view.DNAMain;
import dnanalytics.worker.Worker;
import dnanalytics.worker.WorkerScript;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class IndexFasta implements Tool {

    @FXML
    private static TextField genome;
    
    private static ResourceBundle resources = DNAMain.getResources();

    @FXML
    void selectFASTA() {
        FileManager.setOpenFile(FileManager.FASTA_DESCRIPTION,
                FileManager.FASTA_DESCRIPTION, FileManager.FASTA_FILTERS, genome);
    }

    @Override
    public Node getView() {
        try {
            return FXMLLoader.load(getClass().getResource("IndexFasta.fxml"),
                    resources);
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public Worker getWorker() {
        if (!new File(genome.getText()).exists()) {
            System.err.println(resources.getString("no.genome"));
            return null;
        }
        return new WorkerScript() {
            @Override
            protected int start() {
                updateTitle("Indexing " + new File(genome.getText()).getName());
                updateMessage(resources.getString("index.index"));
                return executeCommand("bwa index -a bwtsw " + genome.getText());
            }
        };
    }

    @Override
    public String getTitle() {
        return resources.getString("index.title");
    }

    @Override
    public String getStyleID() {
        return "indexFasta";
    }
}
