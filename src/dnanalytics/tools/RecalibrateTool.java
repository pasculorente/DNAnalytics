/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dnanalytics.tools;

import dnanalytics.DNAnalytics;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.RecalibrateController;
import dnanalytics.worker.Recalibrate;
import dnanalytics.worker.Worker;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class RecalibrateTool implements Tool {

    private static final ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private RecalibrateController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(RecalibrateController.class.getResource("Recalibrate.fxml"),
                    resources);
            try {
                view = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(RecalibrateTool.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        String input = controller.getInput();
        String output = controller.getOutput();
        String mills = controller.getMills();
        String omni = controller.getOmni();
        String hapmap = controller.getHapmap();
        String dbsnp = controller.getDbsnp();
        String genome = DNAnalytics.getProperties().getProperty("genome");
        String temp = DNAnalytics.getProperties().getProperty("tempDir");
        return new Recalibrate(genome, dbsnp, omni, hapmap, mills, output, temp, input);
    }

    @Override
    public String getTitle() {
        return resources.getString("recal.title");
    }

    @Override
    public String getIco() {
        return "recal32.png";
    }

    @Override
    public String getDescription() {
        return resources.getString("recal.description");
    }

}
