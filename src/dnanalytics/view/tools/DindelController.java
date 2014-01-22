package dnanalytics.view.tools;

import dnanalytics.utils.FileManager;
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
        FileManager.openBAM(input);
    }

    @FXML
    public void selectOutput() {
        FileManager.saveVCF(output);
    }

    public String getInput() {
        return input.getText();
    }

    public String getOutput() {
        return output.getText();
    }
    
    
    
}
