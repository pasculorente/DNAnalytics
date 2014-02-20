package dnanalytics.view.tools;

import dnanalytics.utils.OS;
import dnanalytics.view.DNAMain;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Pascual
 */
public class CombineVariantsController {

    @FXML
    private ListView<String> vcfList;
    @FXML
    private TextField combinedVCF;
    @FXML
    private ToggleGroup combineGroup;

    @FXML
    private void addVCF() {
        File file = OS.openFile("Select VCF file",
                OS.VCF_DESCRIPTION, OS.VCF_FILTERS);
        if (file != null) {
            vcfList.getItems().add(file.getAbsolutePath());
        }
    }

    @FXML
    private void deleteVCF() {
        try {
            vcfList.getItems().remove(vcfList.getSelectionModel().getSelectedIndex());
        } catch (ArrayIndexOutOfBoundsException ex) {
            DNAMain.printMessage("Select a file in the list");
        }
    }

    @FXML
    private void selectOutput() {
        OS.saveVCF(combinedVCF);
    }

    public String getCombinedVCF() {
        return combinedVCF.getText();
    }

    public ListView<String> getVcfList() {
        return vcfList;
    }

    public String getOperation() {
        return ((ToggleButton) combineGroup.getSelectedToggle()).getId();
    }

}
