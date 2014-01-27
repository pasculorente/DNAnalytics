package dnanalytics.tools;

import dnanalytics.DNAnalytics;
import dnanalytics.utils.Command;
import dnanalytics.view.DNAMain;
import dnanalytics.view.tools.DindelController;
import dnanalytics.worker.Worker;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 *
 * @author uai
 */
public class DindelTool implements Tool {

    private final static ResourceBundle resources = DNAMain.getResources();

    private Node view;

    private FXMLLoader loader;

    private DindelController controller;

    @Override
    public Node getView() {
        if (loader == null) {
            loader = new FXMLLoader(DindelController.class.getResource("Dindel.fxml"), resources);
            try {
                view = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(DindelController.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller = loader.getController();
        }
        return view;
    }

    @Override
    public Worker getWorker() {
        return new Worker() {
            // If output is /path/to/myfile.vcf, name will be myfile
            private String name;
            private File temp, windows, windows2, dindel;
            String genome = DNAnalytics.getProperties().getProperty("genome");

            /**
             * Creates directory tree and initializes variables.
             */
            private void initializeSettings() {
                // + tempDir
                // | + name
                //   | - windows
                //   | - windows2
                name = new File(controller.getOutput()).getName().replace(".vcf", "");
                temp = new File(DNAnalytics.getProperties().getProperty("tempDir"), name);
                windows = new File(temp, "windows");
                windows2 = new File(temp, "windows2");
                temp.mkdir();
                windows.mkdir();
                windows2.mkdir();
                dindel = new File("software", "dindel");
            }

            @Override
            public boolean importParameters() {
                // Checking parameters
                if (!new File(controller.getInput()).exists()) {
                    System.err.println(resources.getString("no.input"));
                    return false;
                }
                if (controller.getOutput().isEmpty()) {
                    System.err.println(resources.getString("no.output"));
                    return false;
                }
                if (!new File(genome).exists()) {
                    System.err.println(resources.getString("no.genome"));
                    return false;
                }
                return true;
            }

            /**
             * First step for standard dindel workflow: extraction of potential
             * indels from the input BAM file. The output are 2 files, named
             * after sample name (name.libraries.txt and name.variants.txt)
             */
            private void extractCandidatesFromBAM() {
                // Command appearance:
                // /home/uai/dindel/dindel
                //  --analysis getCIGARindels \
                //  --bamFile input.bam \
                //  --ref reference.fasta \
                //  --outputFile temp/name
                new Command(outStream, new File(dindel, "dindel").getAbsolutePath(),
                        "--analysis", "getCIGARindels",
                        "--bamFile", controller.getInput(),
                        "--ref", genome,
                        "--outputFile", new File(temp, name).getAbsolutePath()).execute();
                // This will generate two files
                // temp/name/name.libraries.txt
                // temp/name/name.variants.txt

            }

            /**
             * Second step for standard dindel workflow: the indels obtained in
             * stage 1 from the BAM file are the candidate indels; they must be
             * grouped into windows of ∼ 120 basepairs, into a
             * realign-window-file. The included Python script makeWindows.py
             * will generate such a file from the file with candidate indels
             * inferred in the first stage. The output are hundreds of files
             * (temp/name/windows/window001.txt,
             * /temp/name/windows/window002.txt,
             * /temp/name/windows/windowXXX.txt)
             */
            private void createRealignWindows() {
                // Appearance of the command
                //  python /home/uai/dindel/makeWindows.py \
                //    --inputFile temp/name/name.variants.txt \
                //    --windowFilePrefix windows/name_window \
                //    --numWindowsPerFile 1000
                new Command(outStream, "python",
                        new File(dindel, "makeWindows.py").getAbsolutePath(),
                        "--inputVarFile", new File(temp, name + ".variants.txt").getAbsolutePath(),
                        "--windowFilePrefix", new File(windows, name + "_window").getAbsolutePath(),
                        "--numWindowsPerFile", "1000").execute();
                // So:
                // temp/name/windows/name_window001.txt
                // temp/name/windows/name_window002.txt
                // temp/name/windows/name_window003.txt
                // temp/name/windows/name_window004.txt
                // ... and so on ...
            }

            /**
             * Third step for dindel standard workflow: for every window,
             * DindelTool will generate candidate haplotypes from the candidate
             * indels and SNPs it detects in the BAM file, and realign the reads
             * to these candidate haplotypes. The realignment step is the
             * computationally most intensive step.
             */
            private void realignToHaplotypes() {
                // Command appearance (we must do this for every window):
                // /home/uai/dindel/dindel
                //   --analysis indels \
                //   --doDiploid \
                //   --bamFile input.bam \
                //   --ref reference.fasta \
                //   --varFile temp/name/windows/name_window001.txt \
                //   --libFile temp/name/name.libraries.txt \
                //   --outputFile temp/name/windows2/name_windows001.txt \
                //   &>/dev/null
                // For this phase we will take into account progress.

                long start = System.currentTimeMillis();
                int i = 1;
                File[] files = windows.listFiles();
                int t = files.length;
                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                outStream.println("ELAPSED TIME\tREMAINING\tWINDOWS");
                for (File file : files) {
                    updateProgress("Realigning " + i + " out of " + t + " windows",
                            20 + 70 * i / t, 100);
                    new Command(outStream, new File(dindel, "dindel").getAbsolutePath(),
                            "--analysis", "indels",
                            "--doDiploid",
                            "--bamFile", controller.getInput(),
                            "--ref", genome,
                            "--varFile", file.getAbsolutePath(),
                            "--libFile", new File(temp, name + ".libraries.txt").getAbsolutePath(),
                            "--outputFile", new File(windows2, file.getName()).getAbsolutePath()).execute();
                    long elapsed = System.currentTimeMillis() - start;
                    long remaining = (elapsed / i) * (t - i);
                    outStream.println(df.format(new Date(elapsed))
                            + "\t" + df.format(new Date(remaining))
                            + "\t" + i + "/" + t);
                    i++;
                }
            }

            /**
             * Last step for dindel standard workflow: interpreting the output
             * from DindelTool and produce indel calls and qualities in the VCF4
             * format.
             */
            private void mergeIndelsInVcf() {
                // Command appearance:
                //  python /home/uai/dindel/mergeOutputDiploid.py \
                //   --ref reference.fasta \
                //   --inputFiles fileList.txt \
                //   --outputFile output.vcf
                // where fileList contains a list of all windows2 files.
                File fileList = new File(temp, "fileList.txt");

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileList))) {
                    for (File f : windows2.listFiles()) {
                        bw.write(f.getPath());
                        bw.newLine();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(DindelTool.class.getName()).log(Level.SEVERE, null, ex);
                }
                new Command(outStream,"python",
                        new File(dindel, "mergeOutputDiploid.py").getAbsolutePath(),
                        "--ref", genome,
                        "--inputFiles", fileList.getAbsolutePath(),
                        "--outputFile", controller.getOutput()).execute();
            }

            @Override
            protected int start() {

                initializeSettings();

                updateProgress("Extracting candidate indels from BAM", 0, 100);
                extractCandidatesFromBAM();

                updateProgress("Creating realignment windows", 10, 100);
                createRealignWindows();

                updateProgress("Realigning", 20, 100);
                realignToHaplotypes();

                updateProgress("Generating VCF", 90, 100);
                mergeIndelsInVcf();
                updateProgress(1, 1);
                return 0;
            }
        };
    }

    @Override
    public String getTitle() {
        return "Dindel (Indels call)";
    }

    @Override
    public String getStyleID() {
        return "dindel";
    }
}
