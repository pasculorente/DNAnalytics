package dnanalytics.view.tools;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Pascual
 */
public class TestToolController {
    @FXML
    private TextField lines;
    @FXML
    private TextField milliseconds;

    public int getLines() throws NumberFormatException {
        return Integer.valueOf(lines.getText());
    }

    public long getMilliseconds() throws NumberFormatException {
        return Integer.valueOf(milliseconds.getText());
    }

}
