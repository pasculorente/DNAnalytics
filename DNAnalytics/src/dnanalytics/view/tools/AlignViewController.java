package dnanalytics.view.tools;

import dnanalytics.utils.FileManager;
import dnanalytics.utils.Settings;
import java.io.File;
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

        @FXML
    void initialize() {
        // Recover settings
        String value = Settings.getProperty("align.dbsnp");
        dbsnp.setText(value != null ? value : "");
        value = Settings.getProperty("align.mills");
        mills.setText(value != null ? value : "");
        value = Settings.getProperty("align.phase1");
        phase1.setText(value != null ? value : "");
    }

    @FXML
    void selectForward(ActionEvent event) {
        FileManager.setOpenFile(FileManager.FASTQ_DESCRIPTION,
                FileManager.FASTQ_DESCRIPTION, FileManager.FASTQ_FILTERS, forward);
    }

    @FXML
    void selectReverse(ActionEvent event) {
        FileManager.setOpenFile(FileManager.FASTQ_DESCRIPTION,
                FileManager.FASTQ_DESCRIPTION, FileManager.FASTQ_FILTERS, reverse);
    }

    @FXML
    void selectDbsnp(ActionEvent event) {
        File f = FileManager.openVCF(dbsnp);
        if (f != null) {
            Settings.setProperty("align.dbsnp", f.getAbsolutePath());
        }
    }

    @FXML
    void selectMills(ActionEvent event) {
        File f = FileManager.openVCF(mills);
        if (f != null) {
            Settings.setProperty("align.mills", f.getAbsolutePath());
        }
    }

    @FXML
    void selectPhase1(ActionEvent event) {
        File f = FileManager.openVCF(phase1);
        if (f != null) {
            Settings.setProperty("align.phase1", f.getAbsolutePath());
        }
    }

    @FXML
    void selectOutput(ActionEvent event) {
        FileManager.setSaveFile(FileManager.SAM_BAM_DESCRIPTION,
                FileManager.SAM_BAM_DESCRIPTION, FileManager.SAM_BAM_FILTERS,
                FileManager.BAM_EXTENSION, output);
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
