/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dnanalytics.view.tools;

import dnanalytics.utils.FileManager;
import dnanalytics.utils.Settings;
import java.io.File;
import java.net.URL;
import javafx.beans.value.ChangeListener;
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

    @FXML
    void initialize() {

        recalibrate.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                    trainingDatasets.setDisable(!t1);
        });

        String value = Settings.getProperty("call.dbsnp");
        dbsnp.setText(value != null ? value : "");
        value = Settings.getProperty("call.mills");
        mills.setText(value == null ? "" : value);
        value = Settings.getProperty("call.omni");
        omni.setText(value == null ? "" : value);
        value = Settings.getProperty("call.hapmap");
        hapmap.setText(value == null ? "" : value);
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
            Settings.setProperty("call.dbsnp", f.getAbsolutePath());
        }
    }

    @FXML
    void selectHapmap(ActionEvent event) {
        File f = FileManager.openVCF(hapmap);
        if (f != null) {
            Settings.setProperty("call.hapmap", f.getAbsolutePath());
        }
    }

    @FXML
    void selectMills(ActionEvent event) {
        File f = FileManager.openVCF(mills);
        if (f != null) {
            Settings.setProperty("call.mills", f.getAbsolutePath());
        }
    }

    @FXML
    void selectOmni(ActionEvent event) {
        File f = FileManager.openVCF(omni);
        if (f != null) {
            Settings.setProperty("call.omni", f.getAbsolutePath());
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
