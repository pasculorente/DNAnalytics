package dnanalytics.view.tools;

import dnanalytics.DNAnalytics;
import dnanalytics.utils.OS;
import java.io.File;
import java.util.Properties;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

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

    private final Properties properties = DNAnalytics.getProperties();

    @FXML
    void initialize() {
        recalibrate.selectedProperty().addListener((ObservableValue<? extends Boolean> ov,
                Boolean t, Boolean t1) -> {
            omni.setDisable(!t1);
            hapmap.setDisable(!t1);
            mills.setDisable(!t1);
        });
        dbsnp.setText(properties.getProperty("call.dbsnp"));
        mills.setText(properties.getProperty("call.mills"));
        omni.setText(properties.getProperty("call.omni"));
        hapmap.setText(properties.getProperty("call.hapmap"));
        omni.setDisable(true);
        hapmap.setDisable(true);
        mills.setDisable(true);
    }

    @FXML
    void selectInput() {
        OS.openBAM(input);
    }

    @FXML
    void selectOutput() {
        OS.saveVCF(output);
    }

    @FXML
    void selectDbsnp() {
        File f = OS.openVCF(dbsnp);
        if (f != null) {
            properties.setProperty("call.dbsnp", f.getAbsolutePath());
        }
    }

    @FXML
    void selectHapmap() {
        File f = OS.openVCF(hapmap);
        if (f != null) {
            properties.setProperty("call.hapmap", f.getAbsolutePath());
        }
    }

    @FXML
    void selectMills() {
        File f = OS.openVCF(mills);
        if (f != null) {
            properties.setProperty("call.mills", f.getAbsolutePath());
        }
    }

    @FXML
    void selectOmni() {
        File f = OS.openVCF(omni);
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
