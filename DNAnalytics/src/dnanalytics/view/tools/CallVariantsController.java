package dnanalytics.view.tools;

import dnanalytics.DNAnalytics;
import dnanalytics.utils.FileManager;
import java.io.File;
import java.util.Properties;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Pascual
 */
public class CallVariantsController {

    @FXML
    private TextField dbsnp;
    @FXML
    private TextField hapmap;
    @FXML
    private TextField input;
    @FXML
    private TextField mills;
    @FXML
    private TextField omni;
    @FXML
    private TextField output;
    @FXML
    private CheckBox recalibrate;
    @FXML
    private VBox trainingDatasets;

    private final Properties properties = DNAnalytics.getProperties();
    
    @FXML
    void initialize() {
        recalibrate.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                    trainingDatasets.setDisable(!t1);
        });
        dbsnp.setText(properties.getProperty("call.dbsnp"));
        mills.setText(properties.getProperty("call.mills"));
        omni.setText(properties.getProperty("call.omni"));
        hapmap.setText(properties.getProperty("call.hapmap"));
    }

    @FXML
    void selectInput(ActionEvent event) {
        FileManager.setOpenFile(FileManager.SAM_BAM_DESCRIPTION,
                FileManager.SAM_BAM_DESCRIPTION, FileManager.SAM_BAM_FILTERS, input);
    }

    @FXML
    void selectOutput(ActionEvent event) {
        FileManager.saveVCF(output);
    }

    @FXML
    void selectDbsnp(ActionEvent event) {
        File f = FileManager.openVCF(dbsnp);
        if (f != null) {
            properties.setProperty("call.dbsnp", f.getAbsolutePath());
        }
    }

    @FXML
    void selectHapmap(ActionEvent event) {
        File f = FileManager.openVCF(hapmap);
        if (f != null) {
            properties.setProperty("call.hapmap", f.getAbsolutePath());
        }
    }

    @FXML
    void selectMills(ActionEvent event) {
        File f = FileManager.openVCF(mills);
        if (f != null) {
            properties.setProperty("call.mills", f.getAbsolutePath());
        }
    }

    @FXML
    void selectOmni(ActionEvent event) {
        File f = FileManager.openVCF(omni);
        if (f != null) {
            properties.setProperty("call.omni", f.getAbsolutePath());
        }
    }

    
    public String getDbsnp() {
        return dbsnp.getText();
    }

    public String getHapmap() {
        return hapmap.getText();
    }

    public String getInput() {
        return input.getText();
    }

    public String getMills() {
        return mills.getText();
    }

    public String getOmni() {
        return omni.getText();
    }

    public String getOutput() {
        return output.getText();
    }

    public boolean isRecalibrate() {
        return recalibrate.isSelected();
    }

}
