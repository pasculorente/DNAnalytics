package dnanalytics.view.tools;

import dnanalytics.utils.Browser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * FXML Controller class
 *
 * @author Pascual
 */
public class AnnotationController {

    @FXML
    void siftIndels(ActionEvent event) {
        Browser.browse("http://sift.bii.a-star.edu.sg/www/SIFT_indels2.html");
    }

    @FXML
    void siftRestrict(ActionEvent event) {
        Browser.browse("http://sift.bii.a-star.edu.sg/www/SIFT_intersect_coding_submit.html");
    }

    @FXML
    void siftSNP(ActionEvent event) {
        Browser.browse("http://siftdna.org/www/Extended_SIFT_chr_coords_submit.html");
    }

    @FXML
    void annovar(ActionEvent event) {
        Browser.browse("http://wannovar.usc.edu/");
    }
}
