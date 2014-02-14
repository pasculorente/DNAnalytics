package dnanalytics.worker;

import dnanalytics.DNAnalytics;
import dnanalytics.utils.Command;
import java.io.File;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class XHMM extends Worker {

    private final String input, output, genome, temp, name;
    private final static String JAVA7 = "/usr/local/jdk1.7.0_45/bin/java";
    private final static String GATK = "software" + File.separator + "gatk"
            + File.separator + "GenomeAnalysisTK.jar";
    private final static String XHMM = "software" + File.separator + "xhmm"
            + File.separator + "xhmm";
    private final static String INTERVALS_FILE = "software" + File.separator
            + "xhmm" + File.separator + "exome.interval_list";
    private final static String PARAMS_FILE = "software" + File.separator
            + "xhmm" + File.separator + "params.txt";

    private final String step1, step2, step21, step3, step31, step4, step41,
            step42, step5, step6, step61, step62, step7, step8, step81, step82;

    public XHMM(String input, String output) {
        this.input = input;
        this.output = output;
        this.genome = DNAnalytics.getProperties().getProperty("genome");
        this.temp = DNAnalytics.getProperties().getProperty("tempDir");
        this.name = new File(output).getName().replace(".vcf", "");
        String rush = temp + File.separator + name + "_";
        step1 = rush + "group.data";
        step21 = rush + "group.data.sample_interval_summary";
        step2 = rush + "DATA.RD.txt";
        step3 = rush + "DATA.locus_GC.txt";
        step31 = rush + "extreme_GC_targets.txt";
        step4 = rush + "DATA.filtered_centered.RD.txt";
        step41 = rush + "DATA.filtered_centered.RD.txt.filtered_targets.txt";
        step42 = rush + "DATA.filtered_centered.RD.txt.filtered_samples.txt";
        step5 = rush + "DATA.RD_PCA";
        step6 = rush + "DATA.PCA_normalized.filtered.sample_zscores.RD.txt";
        step61 = rush + "DATA.PCA_normalized.filtered.sample_zscores.RD.txt.filtered_targets.txt";
        step62 = rush + "DATA.PCA_normalized.filtered.sample_zscores.RD.txt.filtered_samples.txt";
        step7 = rush + "DATA.same_filtered.RD.txt";
        String outFile = new File(output).getParent() + File.separator + name;
        step8 = outFile + ".xcnv";
        step81 = outFile + ".aux_xcnv";
        step82 = outFile;
    }

    @Override
    public boolean importParameters() {
        if (!new File(input).exists()) {
            return false;
        }
        if (!new File(genome).exists()) {
            return false;
        }
        if (!new File(temp).exists()) {
            return false;
        }
        return !output.isEmpty();
    }

    @Override
    protected int start() {
        updateTitle("Calling CNV for " + new File(input).getName());
        int total = 9;
        int step = 0;
        int ret;
        updateProgress("Calculating depths", step++, total);
        if ((ret = calculateDepths()) != 0) {
            return ret;
        }
        updateProgress("Combining depths", step++, total);
        if ((ret = mergeDepths()) != 0) {
            return ret;
        }
        updateProgress("Looking for extreme GC content", step++, total);
        if ((ret = searchGCcontent()) != 0) {
            return ret;
        }
        updateProgress("Filtering and centering", step++, total);
        if ((ret = filterAndCenter()) != 0) {
            return ret;
        }
        updateProgress("Running PCA", step++, total);
        if ((ret = runPCA()) != 0) {
            return ret;
        }
        updateProgress("Normalizing data", step++, total);
        if ((ret = zScoring()) != 0) {
            return ret;
        }
        updateProgress("Refiltering data", step++, total);
        if ((ret = refilterData()) != 0) {
            return ret;
        }
        updateProgress("Discovering Copy Number Variants", step++, total);
        if ((ret = discoverCNV()) != 0) {
            return ret;
        }
        updateProgress("Genotyping Copy Number Variants", step++, total);
        if ((ret = genotypeCNV()) != 0) {
            return ret;
        }
        updateProgress("Cleaning files", step++, total);
        if ((ret = clean()) != 0) {
            return ret;
        }
        updateProgress("Done", 1, 1);
        return 0;
    }

    /**
     * Calculates depth for listed exons at intervals file.
     *
     * @return GATK command return value. 0 if everything goes well.
     */
    private int calculateDepths() {
        return new Command(JAVA7, "-jar", GATK,
                "-T", "DepthOfCoverage", "-R", genome, "-I", input,
                "--omitLocusTable", "--omitDepthOutputAtEachBase",
                "--includeRefNSites", "-L", INTERVALS_FILE,
                "-o", step1).execute(outStream);
    }

    /**
     * Combines GATK Depth-of-Coverage outputs for multiple samples (at same
     * loci). Converts GATK format to xhmm format.
     *
     * @return XHMM command return value. 0 if everything goes fine.
     */
    private int mergeDepths() {
        return new Command(XHMM, "--mergeGATKdepths",
                "--GATKdepths", step21, "-o", step2).execute(null);
    }

    /**
     * Runs GATK to calculate the per-target GC content and create a list of the
     * targets with extreme GC content.
     *
     * @return 0 if everything OK.
     */
    private int searchGCcontent() {
        int ret;
        if ((ret = new Command(JAVA7, "-jar", GATK,
                "-T", "GCContentByInterval", "-R", genome,
                "-L", INTERVALS_FILE, "-o", step3).execute(outStream, true)) != 0) {
            return ret;
        }
        // This must be done by shell
        // It can be done by java. BufferedReader...
        if ((ret = new Command("cat", step3, "|",
                "awk", "'{if ($2 < 0.1 || $2 > 0.9) print $1}'",
                ">", step31).execute(outStream, true)) != 0) {
            return ret;
        }
        return 0;
    }

    /**
     * Filters samples and targets and then mean-centers the targets.
     *
     * @return 0 if OK.
     */
    private int filterAndCenter() {
        return new Command( XHMM, "--matrix", "-r", step2,
                "--centerData", "--centerType", "sample", "-o", step4,
                "--outputExcludedTargets", step41,
                "--outputExcludedSamples", step42,
                "--excludeTargets", step31).execute(null);
    }

    /**
     * Runs PCA (Principal Component Analysis) on mean-centered data.
     *
     * @return 0 if OK.
     */
    private int runPCA() {
        return new Command(XHMM, "--PCA", "-r", step4,
                "--PCAfiles", step5).execute(null);
    }

    /**
     * Filters and z-score centers (by sample) the PCA-normalized data.
     *
     * @return 0 if OK.
     */
    private int zScoring() {
        return new Command(XHMM, "--matrix", "-r", step4,
                "--centerData", "--centerType", "sample",
                "--zScoreData", "-o", step6,
                "--outputExcludedTargets", step61,
                "--outputExcludedSamples", step62,
                "--maxSdTargetRD", "30").execute(null);
    }

    /**
     * Filters original read-depth data to be the same as filtered, normalized
     * data.
     *
     * @return 0 if OK.
     */
    private int refilterData() {
        return new Command(XHMM, "--matrix", "-r", step2,
                "--excludeTargets", step41,
                "--excludeTargets", step61,
                "--excludeSamples", step42,
                "--excludeSamples", step62,
                "-o", step7).execute(null);
    }

    /**
     * Filters original read-depth data to be the same as filtered, normalized
     * data.
     *
     * @return 0 if OK.
     */
    private int discoverCNV() {
        return new Command( XHMM, "--discover", "-p", PARAMS_FILE,
                "-r", step6, "-R", step7,
                "-c", step8, "-a", step81, "-s", step82).execute(null);
    }

    /**
     * Genotypes discovered CNVs in all samples.
     *
     * @return 0 if OK.
     */
    private int genotypeCNV() {
        return new Command(XHMM, "--genotype", "-p", PARAMS_FILE,
                "-r", step6, "-R", step7,
                "-g", step8, "-F", genome,
                "-v", output).execute(null);
    }

    private int clean() {
        File[] files = new File(temp).listFiles((File pathname) -> {
            return pathname.getName().startsWith(name);
        });
        for (File f : files){
            f.delete();
        }
        return 0;
    }

}
