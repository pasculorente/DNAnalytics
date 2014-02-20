package dnanalytics.tools;

import dnanalytics.utils.Command;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.TestToolController;
import dnanalytics.worker.LineParser;
import dnanalytics.worker.Worker;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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
                view = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(TestTool.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return new Worker() {
            private final static String java7 = "/usr/local/jdk1.7.0_45/bin/java";
            int lines;
            long millis;

            @Override
            public boolean importParameters() {
                try {
                    lines = controller.getLines();
                    millis = controller.getMilliseconds();
                } catch (NumberFormatException ex) {
                    DNAMain.printMessage("Bad number format");
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
                //executeCommand("pwd");
                testOne();
                return 0;
            }

            void testOne() {
                updateTitle("Testing things");
                updateProgress("Waiting for lines", 0, lines);
                for (int i = 1; i <= lines; i++) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(millis);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TestTool.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    outStream.println("This is line number " + i + "/" + lines);
                    updateProgress("Lines processed: " + i, i, lines);
                }
                updateProgress("Completed", lines, lines);
            }

            void testTwo() {
                String picard = "software" + File.separator + "picard" + File.separator;
                String gatk = "software" + File.separator + "gatk"
                        + File.separator + "GenomeAnalysisTK.jar";
                updateProgress("Testing picard...", 0, 4);
                new Command("java", "-jar", picard + "CleanSam.jar").execute(outStream);
                updateProgress("Testing GATK...", 1, 4);
                new Command(java7, "-jar", gatk, "-T", "HaplotypeCaller").execute(outStream);
                updateProgress("Testing bwa", 2, 4);
                new Command("bwa").execute(outStream);
                updateProgress("Testing samtools", 3, 4);
                new Command("samtools").execute(outStream);
                new Command("software/test_script.sh",
                        String.valueOf(controller.getLines()),
                        String.valueOf(controller.getMilliseconds() / 1000)).execute(outStream);
                updateProgress("Test tool completed", 1, 1);
            }
        };
    }

    @Override
    public String getTitle() {
        return "Test Tool";
    }

    @Override
    public String getIco() {
        return "test.png";
    }

    @Override
    public String getDescription() {
        return "Test tool";
    }

    public class TestParser implements LineParser {

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
