package dnanalytics.tools;

import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.AnnotationController;
import dnanalytics.worker.Worker;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;


public class AnnotationTool implements Tool{

   private final static ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private AnnotationController controller;
    
    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(AnnotationController.class.getResource("Annotation.fxml"), resources);
            try {
                view = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(AnnotationController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return null;
    }

    @Override
    public String getTitle() {
        return resources.getString("sift.title");
    }

    @Override
    public String getIco() {
        return "annotate32.png";
    }

    @Override
    public String getDescription() {
        return resources.getString("annotate.description");
    }

}
