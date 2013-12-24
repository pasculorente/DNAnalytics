package dnanalytics;

import dnanalytics.utils.Settings;
import dnanalytics.view.DNAMain;
import java.util.ResourceBundle;
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

    @Override
    public void start(Stage stage) throws Exception {
        ResourceBundle dnaBundle = ResourceBundle.getBundle("dnanalytics.view.dnanalytics",
                Settings.getLocale());
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
     * <p/>
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
