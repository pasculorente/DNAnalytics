package dnanalytics.tools;

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
            loader = new FXMLLoader(FilterFrequenciesController.class.getResource(
                    "FilterFrequencies.fxml"), resources);
            try {
                view = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(FilterFrequenciesController.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        final String input = controller.getFrequencyFile();
        final int column = controller.getColumn();
        final double freq = controller.getMaxFrequency();

        return new Worker() {
            @Override
            protected int start() {
                String line;
                String[] columns;

                updateTitle("Filtering " + new File(input).getName());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FilterFrequenciesTool.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
                File fr = new File(input);
                long total = fr.length();
                try (BufferedReader br = new BufferedReader(new FileReader(fr));
                        BufferedWriter bw = new BufferedWriter(new FileWriter(input.replace(".tsv",
                                                "_filtered.tsv")));) {
                    long read = 0;
                    updateProgress(read, total);
                    // Looking for column title
                    if ((line = br.readLine()) != null) {
                        columns = line.split("\t");
                        if (column <= columns.length && column > 0) {
                            outStream.println("Looking at " + columns[column - 1]);
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
                    errStream.println("File " + input + " not found");
                } catch (IOException ex) {
                    errStream.println("Error reading file");
                } 
                return 0;
            }

            @Override
            public boolean importParameters() {
                if (input.isEmpty() || !new File(input).exists()) {
                    System.err.println(resources.getString("no.output"));
                    return false;
                }
                if (column == -1) {
                    errStream.println("Negative column");
                    return false;
                }
                if (freq < 0) {
                    errStream.println("Frequency under 0");
                    return false;
                }
                return true;
            }

        };
    }

    @Override
    public String getTitle() {
        return resources.getString("filter.title");
    }

    @Override
    public String getIco() {
        return "filter32.png";
    }

    @Override
    public String getDescription() {
        return resources.getString("filter.description");
    }
}
