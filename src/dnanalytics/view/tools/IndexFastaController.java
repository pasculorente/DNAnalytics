package dnanalytics.view.tools;

import dnanalytics.utils.OS;
import dnanalytics.view.DNAMain;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
    private Label indexed;

    @FXML
    void selectFASTA() {
        OS.openFile(OS.FASTA_DESCRIPTION,
                OS.FASTA_DESCRIPTION, OS.FASTA_FILTERS, genome);
        indexed.setText(isIndexed()
                ? DNAMain.getResources().getString("label.indexed") : "");
    }

    public String getGenome() {
        return genome.getText();
    }

    /**
     * Check if a genome is indexed.
     * <p/>
     * @return true if all the index files are created, but does not check their
     * integrity.
     */
    private boolean isIndexed() {
        String g = genome.getText();
        String[] ends = {".sa", ".bwt", ".amb", ".ann", ".pac"};
        for (String end : ends) {
            if (!new File(g + end).exists()) {
                return false;
            }
        }
        return true;
    }

}
