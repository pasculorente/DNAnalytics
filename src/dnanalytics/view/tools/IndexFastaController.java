package dnanalytics.view.tools;

import dnanalytics.utils.OS;
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
        OS.setOpenFile(OS.FASTA_DESCRIPTION,
                OS.FASTA_DESCRIPTION, OS.FASTA_FILTERS, genome);
    }

    public String getGenome() {
        return genome.getText();
    }

    
}
