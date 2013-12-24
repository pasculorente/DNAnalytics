
package dnanalytics.view.tools;

import dnanalytics.utils.FileManager;
import dnanalytics.view.DNAMain;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        columnField.textProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            columnName.setText("Check first line");
            if (new File(frequencyFile.getText()).exists()) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(frequencyFile.
                            getText()));
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
                    Logger.getLogger(DNAMain.class.getName()).log(
                            Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(DNAMain.class.getName()).log(
                            Level.SEVERE, null, ex);
                } finally {
                    try {
                        if (br != null) {
                            br.close();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DNAMain.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                }
            }
        });

    }

    @FXML
    void selectFrequencyFile(ActionEvent event) {
        FileManager.setOpenFile(FileManager.TSV_DESCRIPTION, FileManager.TSV_DESCRIPTION,
                FileManager.TSV_FILTERS, frequencyFile);
    }

    public String getColumnField() {
        return columnField.getText();
    }

    public String getFrequencyFile() {
        return frequencyFile.getText();
    }
    
    public Double getMaxFrequency(){
        return Double.valueOf(maxFrequency.getText());
    }
    
    
}
