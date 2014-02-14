package dnanalytics.worker;

import dnanalytics.DNAnalytics;
import dnanalytics.tools.DindelTool;
import dnanalytics.utils.Command;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author uichuimi03
 */
public class DindelWorker extends Worker {

    // If output is /path/to/myfile.vcf, name will be myfile
    private final String input, output;
    private File temp, windows, windows2, dindel;
    String genome = DNAnalytics.getProperties().getProperty("genome");
    private String name;
    private long startTime, lastChk;

    public DindelWorker(String input, String output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean importParameters() {
        // Checking parameters
        if (!new File(input).exists()) {
            System.err.println(resources.getString("no.input"));
            return false;
        }
        if (output.isEmpty()) {
            System.err.println(resources.getString("no.output"));
            return false;
        }
        if (!new File(genome).exists()) {
            System.err.println(resources.getString("no.genome"));
            return false;
        }
        // + tempDir
        // | + name
        //   | - windows
        //   | - windows2
        name = new File(output).getName().replace(".vcf", "");
        temp = new File(DNAnalytics.getProperties().getProperty("tempDir"), name);
        windows = new File(temp, "windows");
        windows2 = new File(temp, "windows2");
        temp.mkdir();
        windows.mkdir();
        windows2.mkdir();
        dindel = new File("software", "dindel");
        return true;
    }

    /**
     * First step for standard dindel workflow: extraction of potential indels
     * from the input BAM file. The output are 2 files, named after sample name
     * (name.libraries.txt and name.variants.txt)
     */
    private void extractCandidatesFromBAM() {
        // Command appearance:
        // /home/uai/dindel/dindel
        //  --analysis getCIGARindels \
        //  --bamFile input.bam \
        //  --ref reference.fasta \
        //  --outputFile temp/name
        new Command(new File(dindel, "dindel").getAbsolutePath(),
                "--analysis", "getCIGARindels",
                "--bamFile", input,
                "--ref", genome,
                "--outputFile", new File(temp, name).getAbsolutePath()).execute(outStream);
        // This will generate two files
        // temp/name/name.libraries.txt
        // temp/name/name.variants.txt

    }

    /**
     * Second step for standard dindel workflow: the indels obtained in stage 1
     * from the BAM file are the candidate indels; they must be grouped into
     * windows of ∼ 120 basepairs, into a realign-window-file. The included
     * Python script makeWindows.py will generate such a file from the file with
     * candidate indels inferred in the first stage. The output are hundreds of
     * files (temp/name/windows/window001.txt, /temp/name/windows/window002.txt,
     * /temp/name/windows/windowXXX.txt)
     */
    private void createRealignWindows() {
        // Appearance of the command
        //  python /home/uai/dindel/makeWindows.py \
        //    --inputFile temp/name/name.variants.txt \
        //    --windowFilePrefix windows/name_window \
        //    --numWindowsPerFile 1000
        new Command("python", new File(dindel, "makeWindows.py").getAbsolutePath(),
                "--inputVarFile", new File(temp, name + ".variants.txt").getAbsolutePath(),
                "--windowFilePrefix", new File(windows, name + "_window").getAbsolutePath(),
                "--numWindowsPerFile", "1000").execute(outStream);
        // So:
        // temp/name/windows/name_window001.txt
        // temp/name/windows/name_window002.txt
        // temp/name/windows/name_window003.txt
        // temp/name/windows/name_window004.txt
        // ... and so on ...
    }

    /**
     * Third step for dindel standard workflow: for every window, DindelTool
     * will generate candidate haplotypes from the candidate indels and SNPs it
     * detects in the BAM file, and realign the reads to these candidate
     * haplotypes. The realignment step is the computationally most intensive
     * step.
     */
    private void realignHaplotypes() {
        final File[] files = windows.listFiles();
        final Turnomatic turnomatic = new Turnomatic(files);
        final int cores = 4;
        Thread[] threads = new Thread[cores];
        for (int i = 0; i < cores; i++) {
            threads[i] = new Thread(new FileProcessor(turnomatic, files, i + ""));
            threads[i].start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(DindelWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void printStatus(boolean header, int index, int total) {
        DateFormat df = new SimpleDateFormat("HH:mm:ss (dd)");
        if (header) {
            startTime = System.currentTimeMillis();
            lastChk = startTime;
            outStream.println("WINDOWS\tSTEP TIME\tTOTAL TIME\tREAMINING");
        }
        long timestamp = System.currentTimeMillis();
        long chkTime = timestamp - lastChk;
        long totalTime = timestamp - startTime;
        long remaining = (index == 0) ? Long.MAX_VALUE
                : (totalTime / index) * (total - index);
        outStream.println(
                index + "/" + total
                + "\t" + df.format(chkTime)
                + "\t" + df.format(totalTime)
                + "\t" + df.format(remaining));
        updateProgress("Realigning " + index + " out of " + total + " windows",
                20 + 70 * index / total, 100);
        lastChk = timestamp;

    }

    private void printStatus(int index, int total) {
        printStatus(false, index, total);
    }

    /**
     * Last step for dindel standard workflow: interpreting the output from
     * DindelTool and produce indel calls and qualities in the VCF4 format.
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
        try (PrintStream devnull = new PrintStream("/dev/null")) {
            new Command("python",
                    new File(dindel, "mergeOutputDiploid.py").getAbsolutePath(),
                    "--ref", genome,
                    "--inputFiles", fileList.getAbsolutePath(),
                    "--outputFile", output).execute(null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DindelWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected int start() {

        updateTitle("Calling indels for " + new File(input).getName());

        updateProgress("Extracting candidate indels from BAM", 0, 100);
        extractCandidatesFromBAM();

        updateProgress("Creating realignment windows", 10, 100);
        createRealignWindows();

        updateProgress("Realigning", 20, 100);
        realignHaplotypes();

        updateProgress("Generating VCF", 90, 100);
        mergeIndelsInVcf();
        updateProgress("Done", 1, 1);
        return 0;
    }

    /**
     * Helpful class to assign next file to a FileProcessor.
     */
    class Turnomatic {

        private final int step, max;
        private int next = 0;

        public Turnomatic(File[] files) {
            max = files.length;
            step = (max + 50) / 100;
        }

        public synchronized int nextFile() {
            if (next % step == 0) {
                if (next == 0) {
                    printStatus(true, next, max);
                } else if (next != step) {
                    printStatus(next - step, max);
                }
            }
            if (next < max) {
                return next++;
            } else {
                printStatus(++next - step, max);
                return -1;
            }
        }
    }

    /**
     * This class will process files until the turnomatic returns -1. In that
     * moment, this thread stops.
     */
    class FileProcessor extends Task<Void> {

        private final Turnomatic turnomatic;
        private final File[] files;
        private final String d = new File(dindel, "dindel").getAbsolutePath();
        private final String lib = new File(temp, name + ".libraries.txt").getAbsolutePath();
        private final String id;

        /**
         * Creates a new FileProcessor assigning it a turnomatic and a list of
         * files.
         *
         * @param turnomatic A machine that gives sorted next file index.
         * @param files A list of files to process.
         * @param id Just a name.
         */
        public FileProcessor(Turnomatic turnomatic, File[] files, String id) {
            this.turnomatic = turnomatic;
            this.files = files;
            this.id = id;
        }

        /**
         * Start taking numbers from its Turn-o-matic n processing files, until
         * the turn-o-matic returns -1.
         *
         * @return nothing at all.
         */
        @Override
        public synchronized Void call() {
            int next;
            while ((next = turnomatic.nextFile()) != -1) {
                final String inputF = files[next].getAbsolutePath();
                final String output = new File(windows2, files[next].getName()).getAbsolutePath();
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
                new Command(d, "--analysis", "indels",
                        "--doDiploid",
                        "--bamFile", input,
                        "--ref", genome,
                        "--varFile", inputF,
                        "--libFile", lib,
                        "--outputFile", output).execute(null);
//                    outStream.println(this + ":" + next);
//                    Thread.sleep(new Random().nextInt(1000));
            }
            return null;
        }

        @Override
        public String toString() {
            return "FileProcessor(" + id + ")";
        }
    }
}
