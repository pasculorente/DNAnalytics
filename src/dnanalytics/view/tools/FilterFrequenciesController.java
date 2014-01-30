package dnanalytics.view.tools;

import dnanalytics.utils.OS;
import dnanalytics.view.DNAMain;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Pascual
 */
public class FilterFrequenciesController {

    @FXML
    private TextField frequencyFile;
    @FXML
    private TextField columnField;
    @FXML
    private Label columnName;
    @FXML
    private TextField maxFrequency;

    public void initialize() {
        columnField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                columnName.setText("Check first line");
                if (new File(frequencyFile.getText()).exists()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(frequencyFile.getText()))) {
                        String line = br.readLine();
                        if (line != null) {
                            String[] columns = line.split("\t");
                            int column = Integer.valueOf(columnField.getText());
                            if (column <= columns.length && column > 0) {
                                columnName.setText(columns[column - 1]);
                            }
                        }
                    } catch (NumberFormatException ex) {
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(DNAMain.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(DNAMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

    }

    @FXML
    void selectFrequencyFile(ActionEvent event) {
        OS.setOpenFile(OS.TSV_DESCRIPTION, OS.TSV_DESCRIPTION,
                OS.TSV_FILTERS, frequencyFile);
    }

    public int getColumn() {
        try {
            return Integer.valueOf(columnField.getText());
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    public String getFrequencyFile() {
        return frequencyFile.getText();
    }

    public Double getMaxFrequency() {
        try {
            return Double.valueOf(maxFrequency.getText());
        } catch (NumberFormatException ex) {
            return -1.0;
        }

    }

}
