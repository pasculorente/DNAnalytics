package dnanalytics.tools;

import dnanalytics.worker.WorkerScript;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.FilterFrequenciesController;
import dnanalytics.worker.Worker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class FilterFrequenciesTool implements Tool {

    private static final ResourceBundle resources = DNAMain.getResources();
    
     private Node view;

    private FXMLLoader loader;

    private FilterFrequenciesController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(FilterFrequenciesController.class.getResource("FilterFrequencies.fxml"), resources);
            try {
                view = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(FilterFrequenciesController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        if (controller.getFrequencyFile().isEmpty()) {
            System.err.println(resources.getString("no.output"));
            return null;
        }
        final int column;
        try {
            column = Integer.valueOf(controller.getColumnField());
        } catch (NumberFormatException ex) {
            System.err.println("Wrong column number format");
            return null;
        }
        final double freq = controller.getMaxFrequency();
        
        return new WorkerScript() {
            @Override
            protected int start() {
                String line;
                BufferedReader br = null;
                BufferedWriter bw = null;
                String[] columns;

                updateTitle("Filtering "
                        + new File(controller.getFrequencyFile()).getName());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FilterFrequenciesTool.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
                try {
                    File fr = new File(controller.getFrequencyFile());
                    br = new BufferedReader(new FileReader(fr));
                    bw = new BufferedWriter(new FileWriter(controller.getFrequencyFile().replace(".tsv", "_filtered.tsv")));
                    long total = fr.length();
                    long read = 0;
                    updateProgress(read, total);
                    // Looking for column title
                    if ((line = br.readLine()) != null) {
                        columns = line.split("\t");
                        if (column <= columns.length && column > 0) {
                            outStream.println(
                                    "Looking at " + columns[column - 1]);
                        }
                        bw.write(line);
                        bw.newLine();
                    }
                    // Here we go, give me one line
                    while ((line = br.readLine()) != null) {
                        // Nice progess bar view
                        updateProgress(read, total);
                        read += line.length();

                        // So, you have partitioned it in 'tab' separated fields?
                        columns = line.split("\t");
                        // Don't lie to me (told me column 6), are there at least 6 columns?
                        if (columns.length >= column) {
                            Pattern pattern = Pattern.compile("-?[\\d\\.]+");
                            // Oh oh, this field is empty, i will leave it as it was.
                            if (columns[column - 1].isEmpty()) {
                                bw.write(line);
                                bw.newLine();
                                continue;
                            }
                            // You told me column 6? This is 5 for a computer
                            Matcher m = pattern.matcher(columns[column - 1]);
                            // Ok, are these really a number?
                            while (m.find()) {
                                try {
                                    double d = Double.valueOf(m.group());
                                    // Oh god, is any of this frequencies less than your maximum frequency?
                                    if (d <= freq) {
                                        // Yes? Write line and get out of here, NEXT LINE!!!!
                                        bw.write(line);
                                        bw.newLine();
                                        break;
                                    }
                                } catch (NumberFormatException ex) {
                                }
                            }
                        }
                    }
                } catch (FileNotFoundException ex) {
                    errStream.println(
                            "File " + controller.getFrequencyFile() + "not found");
                } catch (IOException ex) {
                    System.err.println("Error reading file");
                } finally {
                    try {
                        if (br != null) {
                            br.close();
                        }
                        if (bw != null) {
                            bw.close();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DNAMain.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                }
                return 0;
            }

        };
    }

    @Override
    public String getTitle() {
        return resources.getString("filter.title");
    }

    @Override
    public String getStyleID() {
        return "filterFrequencies";
    }
}