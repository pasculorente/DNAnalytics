package dnanalytics;

import dnanalytics.view.DNAMain;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Lanzador de la interfaz gráfica.
 *
 * @author Pascual Lorente Arencibia
 */
public class DNAnalytics extends Application {

    DNAMain controller;

    private static Properties properties;
    private final static String PROPERTIES_FILE = "properties.txt";
    private static final ArrayList<Locale> appLocales = new ArrayList<>();

    {
        appLocales.add(Locale.US);
        appLocales.add(new Locale("es", "ES"));
    }
    @Override
    public void start(Stage stage) throws Exception {
        properties = new Properties();
        properties.load(new FileInputStream(PROPERTIES_FILE));
        Locale l = new Locale(properties.getProperty("language"), properties.getProperty("country"));
        ResourceBundle dnaBundle = ResourceBundle.getBundle("dnanalytics.view.dnanalytics", l);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/DNAMain.fxml"), dnaBundle);
        Parent root = (Parent) loader.load();
        controller = (DNAMain) loader.getController();

        Scene scene = new Scene(root);

        scene.getStylesheets().add("dnanalytics/view/DNAnalytics.css");
        stage.setTitle("DNAnalytics");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application. main() serves only as
     * fallback in case the application can not be launched through deployment artifacts, e.g., in
     * IDEs with limited FX support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        try {
            properties.store(new FileOutputStream(PROPERTIES_FILE), "DNAnalytics properties");
        } catch (IOException ex) {
            Logger.getLogger(DNAnalytics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static ArrayList<Locale> getAppLocales() {
        return appLocales;
    }
}
