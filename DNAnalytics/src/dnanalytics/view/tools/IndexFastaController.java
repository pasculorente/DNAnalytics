/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
