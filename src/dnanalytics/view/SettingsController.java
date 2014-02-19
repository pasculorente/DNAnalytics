package dnanalytics.view;

import dnanalytics.DNAnalytics;
import dnanalytics.utils.OS;
import java.io.File;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author uichuimi03
 */
public class SettingsController {

    private final static Properties properties = DNAnalytics.getProperties();
    private final static ResourceBundle resources = DNAMain.getResources();
    
    @FXML
    private TextField genome;
    @FXML
    private TextField tempDir;
    @FXML
    private ChoiceBox<String> languagesBox;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        genome.setText(properties.getProperty("genome", ""));
        tempDir.setText(properties.getProperty("tempDir", ""));
        // Load languages from settings.
        final Locale currentLocale = resources.getLocale();
        DNAnalytics.getAppLocales().stream().forEach((locale) -> {
            languagesBox.getItems().add(locale.getDisplayName(currentLocale));
        });
        languagesBox.getSelectionModel().select(currentLocale.getDisplayName(currentLocale));

        // Give the languagesBox the ability to change system language.
        languagesBox.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String t, String current) -> {
                    for (Locale locale : DNAnalytics.getAppLocales()) {
                        if (current.equals(locale.getDisplayName(currentLocale))) {
                            properties.setProperty("language", locale.getLanguage());
                            properties.setProperty("country", locale.getCountry());
                            return;
                        }
                    }
                });
    }

    @FXML
    private void selectGenome(ActionEvent event) {
        // Get the file.
        File f = OS.openFile(OS.FASTA_DESCRIPTION, OS.FASTA_DESCRIPTION,
                OS.FASTA_FILTERS, genome);
        // Save it on settings.
        if (f != null) {
            properties.setProperty("genome", f.getAbsolutePath());
        }
    }

    @FXML
    private void selectTempDir(ActionEvent event) {
        File file = OS.selectFolder("Temporary directory");
        if (file != null) {
            tempDir.setText(file.getAbsolutePath());
            properties.setProperty("tempDir", file.getAbsolutePath());
        }
    }

}
