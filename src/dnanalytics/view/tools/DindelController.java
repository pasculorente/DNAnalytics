package dnanalytics.view.tools;

import dnanalytics.utils.OS;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Pascual
 */
public class DindelController {
    @FXML
    private TextField input;
    @FXML
    private TextField output;

    
    @FXML
    public void selectInput() {
        OS.openBAM(input);
    }

    @FXML
    public void selectOutput() {
        OS.saveVCF(output);
    }

    public String getInput() {
        return input.getText();
    }

    public String getOutput() {
        return output.getText();
    }
    
    
    
}
