
package dnanalytics.view.tools;

import dnanalytics.utils.OS;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class CNVController {
    @FXML
    private TextField input;
    @FXML
    private TextField output;

    @FXML
    private void selectInput(ActionEvent event) {
        OS.openBAM(input);
    }

    @FXML
    private void selectOutput(ActionEvent event) {
        OS.saveVCF(output);
    }

    public String getInput() {
        return input.getText();
    }

    public String getOutput() {
        return output.getText();
    }
    
}
