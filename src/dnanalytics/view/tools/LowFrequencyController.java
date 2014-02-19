package dnanalytics.view.tools;

import dnanalytics.utils.OS;
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
        OS.openBAM(input);
    }

    @FXML
    private void setOutput(ActionEvent event) {
        OS.saveFile("TSV", "Tabulated", null, ".tsv", output);
    }

    public String getInput() {
        return input.getText();
    }

    public String getOutput() {
        return output.getText();
    }

    public int getThreshold() {
        try {
            return Integer.valueOf(threshold.getText());
        } catch (NumberFormatException exception) {
            return -1;
        }
    }
}
