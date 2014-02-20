package dnanalytics.view.tools;

import dnanalytics.utils.OS;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author Pascual
 */
public class AnnotationController {

    @FXML
    private TextField sift1;
    @FXML
    private TextField sift2;
    @FXML
    private TextField sift3;
    @FXML
    private TextField annovar;

    @FXML
    private void initialize() {
        sift1.setOnMouseClicked((MouseEvent t) -> {
            sift1.selectAll();
        });
        sift2.setOnMouseClicked((MouseEvent t) -> {
            sift2.selectAll();
        });
        sift3.setOnMouseClicked((MouseEvent t) -> {
            sift3.selectAll();
        });
        annovar.setOnMouseClicked((MouseEvent t) -> {
            annovar.selectAll();
        });
    }

    @FXML
    void siftIndels(ActionEvent event) {
        Platform.runLater(() -> {
            OS.browse("http://sift.bii.a-star.edu.sg/www/SIFT_indels2.html");
        });
    }

    @FXML
    void siftRestrict(ActionEvent event) {
        Platform.runLater(() -> {
            OS.browse("http://sift.bii.a-star.edu.sg/www/SIFT_intersect_coding_submit.html");
        });
    }

    @FXML
    void siftSNP(ActionEvent event) {
        Platform.runLater(() -> {
            OS.browse("http://siftdna.org/www/Extended_SIFT_chr_coords_submit.html");
        });

    }

    @FXML
    void annovar(ActionEvent event) {
        Platform.runLater(() -> {
            OS.browse("http://wannovar.usc.edu/");
        });

    }
}
