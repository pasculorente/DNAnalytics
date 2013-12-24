/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dnanalytics.view.tools;

import dnanalytics.utils.FileManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
