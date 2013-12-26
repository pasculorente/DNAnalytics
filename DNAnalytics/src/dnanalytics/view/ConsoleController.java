package dnanalytics.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author Pascual
 */
public class ConsoleController  {
    @FXML
    private Label message;
    @FXML
    private Label started;
    @FXML
    private Label totalTime;
    @FXML
    private ProgressBar progress;
    @FXML
    private Button cancelButton;
    @FXML
    private TextArea textArea;

    @FXML
    private void cancel() {
    }

    /**
     * @return the message
     */
    public Label getMessage() {
        return message;
    }

    /**
     * @return the started
     */
    public Label getStarted() {
        return started;
    }

    /**
     * @return the totalTime
     */
    public Label getTotalTime() {
        return totalTime;
    }

    /**
     * @return the progress
     */
    public ProgressBar getProgress() {
        return progress;
    }

    /**
     * @return the cancelButton
     */
    public Button getCancelButton() {
        return cancelButton;
    }

    /**
     * 
     * @return the textArea
     */
    public TextArea getTextArea() {
        return textArea;
    }
}
