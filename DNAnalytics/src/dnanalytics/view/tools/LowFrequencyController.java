/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
public class LowFrequencyController {
    @FXML
    private TextField input;
//    @FXML
//    private Slider threshold;
    @FXML
    private TextField threshold;
    @FXML
    private TextField output;

    @FXML
    private void setInput(ActionEvent event) {
        FileManager.openBAM(input);
    }

    @FXML
    private void setOutput(ActionEvent event) {
        FileManager.setSaveFile("TSV", "Tabulated", null, ".tsv", output);
    }
    
    public String getInput(){
        return input.getText();
    }
    
    public String getOutput(){
        return output.getText();
    }
    
    public int getThreshold(){
        return Integer.valueOf(threshold.getText());
    }
}
