package dnanalytics.view.tools;

import dnanalytics.utils.OS;
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
    void selectVariantsInput( ) {
        OS.openVCF(input);
    }

    @FXML
    void selectVariantsOutput( ) {
        OS.saveVCF(output);
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
