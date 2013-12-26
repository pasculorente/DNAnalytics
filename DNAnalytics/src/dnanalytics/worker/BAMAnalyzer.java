package dnanalytics.worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the typical procedural programation encapsulated into a class. Just call
 * BAMAnalyzer.analyzeBAM(String filename), and 24 files will be written in the same directory than
 * the filename, each one containing the exons affected by poor regions in the BAM file. For now,
 * the algorythm requires about 1GB~1.5GB of memory to run. Given a BAM file, it will generate 24
 * tab separated files, each one containing the exons affected by poor regions in the BAM file. A
 * poor region is defined as a group of consecutive positions in the reference where the number of
 * reads are below the threshold.
 *
 * @author Pascual
 */
public class BAMAnalyzer extends WorkerScript {

    private static final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    private static final int[] lengths = new int[24];//1..22, X, Y
    private final int threshold;
    private final String input, output;
    private int[] depths;
    private long startTime, stepTime;
    private int currentChromosome;
    private int topPosition;

    /**
     * @param input Ruta del fichero a procesar.
     * @param output Output filename path.
     * @param threshold Minimun threshold to mean a representated area.
     */
    public BAMAnalyzer(String input, String output, int threshold) {
        this.threshold = threshold;
        this.input = input;
        this.output = output;
    }

    /**
     * Selects the correct file with exons. Chromosome format should be: chr1, chr01, 1 or 01.
     * Chromosome file will be: chromosome01.txt or chromosomeX.txt.
     *
     * @param chromosome the chromosome to parse.
     * @return a string with the filename of the file containing the exons of the chromosome.
     */
    private String whichChromosome(String chromosome) {
        if (chromosome.contains("X")) {
            return "chromosomeX.txt";
        }
        if (chromosome.contains("Y")) {
            return "chromosomeY.txt";
        }
        try {
            int i = Integer.valueOf(chromosome.toLowerCase().replace("chr", ""));
            if (i < 10) {
                return "chromosome0" + i + ".txt";
            } else if (i < 23) {
                return "chromosome" + i + ".txt";
            } else {
                errStream.println("Really? Chromosome " + chromosome + " is not a standard chromosome");
                return null;
            }
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Calculates the list of regions with a depth less than the dp parameter. Each region is
     * represented by an entry in the returning TreeMap, with the key being the start position and
     * the value the ending position.
     * <p/>
     * @param coordinates A TreeMap which contains in the key the chromosomic position and in the
     * value the number of reads.
     * @return
     */
    private TreeMap<Integer, Integer> calculatePoorAreas() {
        TreeMap<Integer, Integer> map = new TreeMap<>();
        int start = 1, c = 0;
        boolean hasStarted = true;
        for (int i = 0; i < topPosition; i++) {
            if (depths[i] > threshold) {
                if (hasStarted) {
                    hasStarted = false;
                    map.put(start, i);
                    c++;
                }
            } else if (!hasStarted) {
                start = i + 1;
                hasStarted = true;
            }
        }
        printProgress(c + " poor areas.");
        return map;
    }

    /**
     * Returns a ProcessBuilder with the command ready to launch depending on the operating system.
     * For Linux it will be a call to "/bin/bash -c samtools view filename", and for Windows, to
     * "cmd /C D:\\samtools.exe".
     * <p/>
     * @param filename the name of the BAM file to parse.
     * @return A ProcessBuilder created with the correct String.
     */
    private ProcessBuilder comandoSAMTools(String filename) {
        
        switch (System.getProperty("os.name")) {
        case "Windows 7":
            return new ProcessBuilder("cmd", "/C", "D:\\samtools.exe view " + filename);
        case "Linux":
            return new ProcessBuilder("/bin/bash", "-c", "samtools view " + filename);
        default:
            errStream.println("No OS detected");
            return null;
        }
    }

    /**
     *
     * @param info Some message
     */
    private void printProgress(String info) {
        updateMessage(info);
        updateProgress(currentChromosome, lengths.length);
        outStream.println(df.format(System.currentTimeMillis() - startTime)
                + "\t" + df.format(System.currentTimeMillis() - stepTime)
                + "\t" + info);
    }

    private void startProgress() {
        outStream.println("Total time\tStep time\tInfo");
        startTime = System.currentTimeMillis();
        stepTime = startTime;
    }

    private void restartStepTime() {
        stepTime = System.currentTimeMillis();
    }

    /**
     * Executes the tasks after samtools finishes extracting one chromosome. Id est, creates a map
     * with poor regions, and check which exons math that regions.
     * <p/>
     * @param coordinates The map with the reads at every reference position.
     * @param currentRNAME The name of the current chromosome (in the format 1,2,3,4...,X,Y).
     * @param output The name of the output file.
     */
    private void parseChromosome(String currentRNAME) {
        restrictToExons(currentRNAME);
        clearDepths();
    }

    /**
     * Look for the exons that do have regions with read depths under the threshold.
     * <p/>
     * @param regions The poor regions.
     * @param chr The name of the cromosome.
     * @param output The name of the output file.
     */
    private void restrictToExons(String chr) {
        TreeMap<Integer, Integer> regions = calculatePoorAreas();
        TreeMap<Integer, PrimateExon> exons = loadPrimateExons(chr);
        //String chromosome = whichChromosome(chr);

        BufferedWriter bw = null;
        int windowsStart, windowsEnd, start, end, c = 0;
        try {
            // First of all, the header is written.
            bw = new BufferedWriter(new FileWriter(output, true));
            bw.write("CHROMOSOME\tGENE_ID\tEXON_NUMBER\tEXON_START\tEXON_END"
                    + "\tSEQ_STARTt\trSEQ_END");
            bw.newLine();
            boolean affected;

            for (Map.Entry<Integer, PrimateExon> entry : exons.entrySet()) {
                affected = false;
                PrimateExon primateExon = entry.getValue();
                windowsStart = primateExon.start - 10;
                windowsEnd = primateExon.end + 10;
                // We will look for regions until we reach the end of the exon.
                Map.Entry<Integer, Integer> region = regions.floorEntry(windowsStart);
                start = region.getKey();
                end = region.getValue();
                while (start < windowsEnd) {
                    // Condición de coincidencia
                    if (end > windowsStart) {
                        bw.write(chr
                                + "\t" + primateExon.geneID + "\t" + primateExon.number
                                + "\t" + primateExon.start + "\t" + primateExon.end
                                + "\t" + start + "\t" + end);
                        bw.newLine();
                        if (!affected) {
                            affected = true;
                            c++;
                        }
                    }
                    if ((region = regions.ceilingEntry(end + 1)) == null) {
                        break;
                    }
                    start = region.getKey();
                    end = region.getValue();
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BAMAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BAMAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BAMAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        printProgress(c + " exons affected.");
    }

    /**
     * Returns a TreeMap with all exons loaded. The TreeMap is indexed by exons starting position.
     * The TreeMap will only contain exons from the chromosome given as a parameter.
     *
     * @param chr The chromosome whose exons must be loaded.
     * @return a TreeMap containing the exons in the corresponding file.
     */
    private TreeMap<Integer, PrimateExon> loadPrimateExons(String chr) {
        TreeMap<Integer, PrimateExon> map = new TreeMap<>();
        try {
            BufferedReader br;
            String chromosome = whichChromosome(chr);
            switch (System.getProperty("os.name")) {
            case "Windows 7":
                br = new BufferedReader(new FileReader("D:\\exons\\" + chromosome));
                // skip header, only in windows
                br.readLine();
                break;
            case "Linux":
            default:
                br = new BufferedReader(new FileReader(
                        "/home/uai/DNA_Sequencing/DNAnalytics/exons/" + chromosome));
            }
            String line;
            String[] fields;
            while ((line = br.readLine()) != null) {
                fields = line.split("\t");
                int start = Integer.valueOf(fields[3]);
                map.put(start, new PrimateExon(fields[0], start, Integer.valueOf(fields[4]),
                        Integer.valueOf(fields[1])));
            }
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BAMAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BAMAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

    private void clearDepths() {
        topPosition = lengths[++currentChromosome];
        for (int i = 0; i < topPosition; i++) {
            depths[i] = 0;
        }
    }

    public void initializeLengths(String filename) {
        ProcessBuilder builder;
        switch (System.getProperty("os.name")) {
        case "Windows 7":
            builder = new ProcessBuilder("cmd", "/C", "D:\\samtools.exe view -H " + filename);
            break;
        case "Linux":
        default:
            builder = new ProcessBuilder("/bin/bash", "-c", "samtools view -H " + filename);
        // System.out.println("No OS detected");
        }
        try {
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String[] fields;
            int c = 0;
            while ((line = br.readLine()) != null) {
                fields = line.split("\t");
                if (fields[0].startsWith("@SQ")) {
                    if (whichChromosome(fields[1].substring(3)) != null) {
                        lengths[c++] = Integer.valueOf(fields[2].substring(3));
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(BAMAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public void locateCommonLostExons(String[] inputs, String output) {
//        List<List<PrimateExon>> exons;
//        exons = new ArrayList<>();
//        for (String f : inputs) {
//            exons.add(findPoorExons(f));
//        }
//    }

//    private List<PrimateExon> findPoorExons(String input) {
//        List<PrimateExon> exons = new ArrayList<>();
//        return exons;
//    }

    @Override
    protected int start() {
        // In general, the algorithm is:
        // for each RNAME
        //     fill coordinates
        //     calculate read depth
        //     restrict to exons
        //     write to disk

        outStream.println("Parsing BAM File: " + input);
        updateTitle("Parsing " + input);
        startProgress();
        // It was giving problems with the hours, this line fixes it, but I'm not happy at all,
        // cause I'm not sure if this is portable.
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Process to read the BAM file.
        // This is an external process. We use samtools.
        ProcessBuilder builder = comandoSAMTools(input);
        builder.redirectErrorStream(true);
        Process process;

        // Counts number of sequences.
        int c = 0;
        String currentRNAME = "void";

        // Let's go.
        try {
            process = builder.start();
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String rName;
            int pos, length;
            String[] BAMfields;
            currentChromosome = 0;
            initializeLengths(input);
            depths = new int[lengths[currentChromosome]];
            topPosition = lengths[currentChromosome];
            // For each line it must be checked if RNAME changes, assuming it is sorted.
            String line;
            while ((line = br.readLine()) != null) {
                BAMfields = line.split("\t");
                rName = BAMfields[2];
                pos = Integer.valueOf(BAMfields[3]);
                length = BAMfields[9].length();

                // When a RNAME is terminated, depths are calculated and restricted to given
                // exons of the chromosome.
                if (!rName.equals(currentRNAME)) {
                    if (whichChromosome(currentRNAME) != null) {
                        printProgress(c + " sequences read.");
                        parseChromosome(currentRNAME);
                        restartStepTime();
                    }
                    if (currentRNAME.equals("Y")) {
                        break;
                    }
                    currentRNAME = rName;
                    printProgress("CHROMOSOME " + rName + " (" + topPosition + ")");
                    c = 0;
                }

                // Only standard chromosomes can be read.
                if (whichChromosome(rName) != null) {
                    // it is important to fill the whole sequence (from pos to pos + length).
                    int top = pos + length - 1;
                    for (int i = pos; i <= top; i++) {
                        depths[i]++;
                    }
                }
                c++;
            }
            // At the end, it must be checked if there is a chromosome not written yet.
            if (whichChromosome(currentRNAME) != null && !currentRNAME.equals("Y")) {
                printProgress(c + " sequences read.");
                parseChromosome(currentRNAME);
            }
        } catch (IOException ex) {
            Logger.getLogger(BAMAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateProgress(1, 1);
        return 0;
    }

    class PrimateExon {

        private String geneID;
        private String chromosome;
        private int start;
        private int end;
        private int number;

        public PrimateExon(String geneID, String chromosome, int start, int end, int number) {
            this.geneID = geneID;
            this.chromosome = chromosome;
            this.start = start;
            this.end = end;
            this.number = number;
        }

        public PrimateExon(String geneID, int start, int end, int number) {
            this.geneID = geneID;
            this.start = start;
            this.end = end;
            this.number = number;
        }
    }
}
