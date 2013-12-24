
package dnanalytics.view.tools;

import dnanalytics.utils.FileManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Pascual
 */
public class SelectVariantsController {
    @FXML
    private TextField input;
    @FXML
    private TextField output;
    @FXML
    private TextField expression;


    @FXML
    void selectVariantsInput(ActionEvent event) {
        FileManager.openVCF(input);
    }

    @FXML
    void selectVariantsOutput(ActionEvent event) {
        FileManager.saveVCF(output);
    }

    public String getExpression() {
        return expression.getText();
    }

    public String getInput() {
        return input.getText();
    }

    public String getOutput() {
        return output.getText();
    }
    
    
    
}
