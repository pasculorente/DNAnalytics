/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dnanalytics.view.tools;

import dnanalytics.DNAnalytics;
import dnanalytics.utils.OS;
import java.io.File;
import java.util.Properties;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author uichuimi03
 */
public class RecalibrateController {

    @FXML private TextField input;
    @FXML private TextField output;
    @FXML private TextField mills;
    @FXML private TextField hapmap;
    @FXML private TextField omni;
    @FXML private TextField dbsnp;
    private final Properties properties = DNAnalytics.getProperties();

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        dbsnp.setText(properties.getProperty("call.dbsnp"));
        mills.setText(properties.getProperty("call.mills"));
        omni.setText(properties.getProperty("call.omni"));
        hapmap.setText(properties.getProperty("call.hapmap"));
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

    @FXML
    void selectInput() {
        OS.openVCF(input);
    }

    @FXML
    void selectOutput() {
        OS.saveVCF(output);
    }

    @FXML
    void selectDbsnp() {
        File f = OS.openVCF(dbsnp);
        if (f != null) {
            properties.setProperty("recal.dbsnp", f.getAbsolutePath());
        }
    }

    @FXML
    void selectHapmap() {
        File f = OS.openVCF(hapmap);
        if (f != null) {
            properties.setProperty("recal.hapmap", f.getAbsolutePath());
        }
    }

    @FXML
    void selectMills() {
        File f = OS.openVCF(mills);
        if (f != null) {
            properties.setProperty("recal.mills", f.getAbsolutePath());
        }
    }

    @FXML
    void selectOmni() {
        File f = OS.openVCF(omni);
        if (f != null) {
            properties.setProperty("recal.omni", f.getAbsolutePath());
        }
    }
}
