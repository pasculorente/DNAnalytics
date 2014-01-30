package dnanalytics.view.tools;

import dnanalytics.DNAnalytics;
import dnanalytics.utils.OS;
import java.io.File;
import java.util.Properties;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Pascual
 */
public class AlignViewController {

    @FXML
    private TextField forward;
    @FXML
    private TextField reverse;
    @FXML
    private TextField dbsnp;
    @FXML
    private TextField mills;
    @FXML
    private TextField phase1;
    @FXML
    private TextField output;
    @FXML
    private ToggleGroup x1;
    @FXML
    private CheckBox reduce;

    private final Properties properties = DNAnalytics.getProperties();

    @FXML
    void initialize() {
        // Recover settings
        dbsnp.setText(properties.getProperty("align.dbsnp", ""));
        mills.setText(properties.getProperty("align.mills", ""));
        phase1.setText(properties.getProperty("align.phase1", ""));
    }

    @FXML
    void selectForward(ActionEvent event) {
        OS.setOpenFile(OS.FASTQ_DESCRIPTION,
                OS.FASTQ_DESCRIPTION, OS.FASTQ_FILTERS, forward);
    }

    @FXML
    void selectReverse(ActionEvent event) {
        OS.setOpenFile(OS.FASTQ_DESCRIPTION,
                OS.FASTQ_DESCRIPTION, OS.FASTQ_FILTERS, reverse);
    }

    @FXML
    void selectDbsnp(ActionEvent event) {
        File f = OS.openVCF(dbsnp);
        if (f != null) {
            properties.setProperty("align.dbsnp", f.getAbsolutePath());
        }
    }

    @FXML
    void selectMills(ActionEvent event) {
        File f = OS.openVCF(mills);
        if (f != null) {
            properties.setProperty("align.mills", f.getAbsolutePath());
        }
    }

    @FXML
    void selectPhase1(ActionEvent event) {
        File f = OS.openVCF(phase1);
        if (f != null) {
            properties.setProperty("align.phase1", f.getAbsolutePath());
        }
    }

    @FXML
    void selectOutput(ActionEvent event) {
        OS.setSaveFile(OS.SAM_BAM_DESCRIPTION,
                OS.SAM_BAM_DESCRIPTION, OS.SAM_BAM_FILTERS,
                OS.BAM_EXTENSION, output);
    }

    public String getDbsnp() {
        return dbsnp.getText();
    }

    public String getForward() {
        return forward.getText();
    }

    public String getMills() {
        return mills.getText();
    }

    public String getOutput() {
        return output.getText();
    }

    public String getPhase1() {
        return phase1.getText();
    }

    public boolean isReduce() {
        return reduce.isSelected();
    }

    public String getReverse() {
        return reverse.getText();
    }

    public String getEncoding() {
        return ((RadioButton) x1.getSelectedToggle()).getId();
    }

}
