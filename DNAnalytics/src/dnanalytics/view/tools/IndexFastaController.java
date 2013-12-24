package dnanalytics.view.tools;

import dnanalytics.utils.FileManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Pascual
 */
public class IndexFastaController {

    @FXML
    private TextField genome;

    @FXML
    void selectFASTA() {
        FileManager.setOpenFile(FileManager.FASTA_DESCRIPTION,
                FileManager.FASTA_DESCRIPTION, FileManager.FASTA_FILTERS, genome);
    }

    public String getGenome() {
        return genome.getText();
    }

    
}
