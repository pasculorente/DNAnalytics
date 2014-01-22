package dnanalytics.tools;

import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.TestToolController;
import dnanalytics.worker.LineParser;
import dnanalytics.worker.Worker;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 *
 * @author Pascual
 */
public class TestTool implements Tool {

    private FXMLLoader loader;
    private Node view;
    private TestToolController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(TestToolController.class.getResource("TestTool.fxml"), DNAMain.
                    getResources());
            try {
                view = loader.load();
            } catch (IOException ex) {
                //Logger.getLogger(TestTool.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return new Worker() {

            int lines;
            long millis;

            @Override
            public boolean importParameters() {
                try {
                    lines = controller.getLines();
                    millis = controller.getMilliseconds();
                } catch (NumberFormatException ex) {
                    System.err.println("Error en alguno de los numberos");
                    return false;
                }
                return true;
            }

            @Override
            protected int start() {
                updateTitle("hello!");
                int c = 0;
                long cp = 0;
                updateProgress("Empezamos", c, lines);
                executeCommand("pwd");
                executeCommand(new TestParser(), "software/test_script.sh "
                        + String.valueOf(controller.getLines()) + " "
                        + String.valueOf(controller.getMilliseconds() / 1000));
//                while (c < lines) {
//                    updateProgress("Test tool (" + c++ + "/" + lines + ")", c, lines);
//                    try {
//                        Thread.sleep(millis);
//                    } catch (InterruptedException ex) {
//                    }
//                }
                updateProgress("Test tool completed", 1, 1);
                return 0;
            }
        };
    }

    @Override
    public String getTitle() {
        return "Test Tool";
    }

    @Override
    public String getStyleID() {
        return "illumina";
    }

    public class TestParser implements LineParser{

        String line;
        
        @Override
        public void updateLine(String line) {
            this.line = line;
        }

        @Override
        public String getMessage() {
            return line.substring(4);
        }

        @Override
        public double getProgress() {
            return line.charAt(0);
        }

    }

}
