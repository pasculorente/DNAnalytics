package dnanalytics.worker;

import dnanalytics.utils.Command;
import static dnanalytics.worker.Worker.resources;
import java.io.File;

/**
 * Script for the alignment of a sample sequences. (1) Initial alignment (2)
 * Refinement (3) Realignment and recalibration (4) Reduce Reads (experimental
 * and optional)
 *
 * @author Pascual Lorente Arencibia
 */
public class Aligner extends Worker {

    private final String forward, reverse, genome, dbsnp, mills, phase1, output, temp, name;
    private final boolean illumina, reduceReads;
    private final Command command = null;
    // timestamp = "aln_20140128_120917_";
    //private final String name;
    private final int cores;
    private int counter;
    private final int total;
    // GATK does NOT work with Java 8, so I installed back java7.
    private final static String java7 = "/usr/local/jdk1.7.0_45/bin/java";
    private final static String gatk = "software" + File.separator + "gatk"
            + File.separator + "GenomeAnalysisTK.jar";

    public Aligner(String temp, String forward, String reverse, String genome, String dbsnp,
            String mills, String phase1, String output, boolean illumina, boolean reduceReads) {
        this.counter = 0;
        this.cores = Runtime.getRuntime().availableProcessors();
        //this.timestamp = "aln_" + new SimpleDateFormat("yyyyMMdd_HHmmss_").format(new Date());
        //this.name = "aln_20140206_110022";
        this.temp = temp;
        this.forward = forward;
        this.reverse = reverse;
        this.genome = genome;
        this.dbsnp = dbsnp;
        this.mills = mills;
        this.phase1 = phase1;
        this.output = output;
        this.illumina = illumina;
        this.reduceReads = reduceReads;
        this.name = new File(output).getName().replace(".bam", "");
        total = reduceReads ? 13 : 12;
    }

    @Override
    public boolean cancel(boolean bln) {
        if (command != null) {
            command.kill();
        }
        return super.cancel(bln);
    }

    /**
     * Phase A: Align/Map sequences. Burrows-Wheeler Aligner. As this project
     * works with paired end sequences, we use simple, basic-parameters
     * workflow.
     * <p>
     * 1 and 2: Align both sequences.</p>
     * <p>
     * bwa aln -t 4 genome.fasta sequence1.fq.gz -I > seq1.sai</p>
     * <p>
     * bwa aln -t 4 genome.fasta sequence2.fq.gz -I > seq2.sai</p>
     * <p>
     * -I : if the sequence is Illumina 1.3+ encoding</p>
     * -t 4 : number of threads The reference genome must be indexed 3: Generate
     * alignments. bwa sampe genome.fasta seq1.sai seq2.sai sequence1.fq.gz
     * sequence2.fq.gz > bwa.sam
     */
    private int firstAlignment() {
        String seq1 = new File(temp, name + "_seq1.sai").getAbsolutePath();
        String seq2 = new File(temp, name + "_seq2.sai").getAbsolutePath();
        String bwa = new File(temp, name + "_bwa.sam").getAbsolutePath();
        updateProgress(resources.getString("align.forward"), counter++, total);
        int ret;
        updateProgress(resources.getString("align.forward"), counter++, total);
        if ((ret = new Command(
                "bwa", "aln", "-t", String.valueOf(cores),
                (illumina ? "-I" : ""), genome, reverse, ">", seq1)
                .execute(outStream, true)) != 0) {
            return ret;
        }
        updateProgress(resources.getString("align.reverse"), counter++, total);
        if ((ret = new Command(
                "bwa", "aln", "-t", String.valueOf(cores),
                (illumina ? "-I" : ""), genome, reverse, ">", seq2)
                .execute(outStream, true)) != 0) {
            return ret;
        }
        updateProgress(resources.getString("align.sampe"), counter++, total);
        ret = new Command("bwa", "sampe", "-P", genome, seq1, seq2,
                forward, reverse, ">", bwa).execute(outStream, true);
        if (ret != 0) {
            return ret;
        }
        new File(seq1).delete();
        new File(seq2).delete();
        return 0;
    }

    /*
     * Phase B: Prepare BAM for GATK
     *   SAM file from BWA must pass several filters before entering GATK. 
     * 
     * 4: Clean SAM
     * Perform two fix-ups
     *   Soft-clip an alignment that hangs off the end of its reference sequence
     *   Set MAPQ to 0 if a read is unmapped
     *   
     * 5: Sort Sam
     *   SortOrder: coordinate
     * 
     * 6: Remove Duplicated Reads
     *
     * 7: Fix RG Header
     *  RGPL = "Illumina"
     *  RGSM = "niv"
     *  RGPU = "flowcell-barcode.lane"
     *  RGLB = "BAITS"
     * 
     * 8: BAM Index
     *   Generates an Index of the BAM file (.bai)
     */
    private int refineBAM() {
        String bwa = new File(temp, name + "_bwa.sam").getAbsolutePath();
        String picard = "software" + File.separator + "picard" + File.separator;
        String picard1 = new File(temp, name + "_picard1.bam").getAbsolutePath();
        String picard2 = new File(temp, name + "_picard2.bam").getAbsolutePath();
        String picard3 = new File(temp, name + "_picard3.bam").getAbsolutePath();
        String picard4 = new File(temp, name + "_picard4.bam").getAbsolutePath();
        String metrics = new File(temp, name + "_dedup.metrics").getAbsolutePath();

        updateProgress(resources.getString("align.clean"), counter++, total);
        int ret = new Command("java", "-jar", picard + "CleanSam.jar",
                "INPUT=" + bwa,
                "OUTPUT=" + picard1).execute(outStream, true);
        if (ret != 0) {
            return ret;
        }
        new File(bwa).delete();

        updateProgress(resources.getString("align.sort"), counter++, total);
        ret = new Command("java", "-jar", picard + "SortSam.jar",
                "INPUT=" + picard1,
                "OUTPUT=" + picard2,
                "SORT_ORDER=coordinate").execute(outStream, true);
        if (ret != 0) {
            return ret;
        }

        new File(picard1).delete();
        updateProgress(resources.getString("align.dedup"), counter++, total);
        ret = new Command("java", "-jar", picard + "MarkDuplicates.jar",
                "INPUT=" + picard2,
                "OUTPUT=" + picard3,
                "REMOVE_DUPLICATES=true",
                "METRICS_FILE=" + metrics).execute(outStream, true);
        if (ret != 0) {
            return ret;
        }
        new File(picard2).delete();
        new File(metrics).delete();

        updateProgress(resources.getString("align.addheader"), counter++, total);
        ret = new Command("java", "-jar", picard + "AddOrReplaceReadGroups.jar",
                "INPUT=" + picard3,
                "OUTPUT=" + picard4,
                "RGPL=ILLUMINA",
                "RGSM=" + name,
                "RGPU=flowcell-barcode.lane",
                "RGLB=BAITS").execute(outStream, true);
        if (ret != 0) {
            return ret;
        }
        new File(picard3).delete();

        updateProgress(resources.getString("align.index"), counter++, total);
        ret = new Command("java", "-jar", picard + "BuildBamIndex.jar",
                "INPUT=" + picard4).execute(outStream, true);
        if (ret != 0) {
            return ret;
        }

        return 0;
    }

    /* 
     * Phase C: Realign around Indels
     *   GATK has an algorithm to avoid false positives which consists in looking at high
     *   probably Indel areas and realigning them, so no false SNPs appear.
     * 
     * 9: RealignerTargetCreator
     *   Generates the intervals to reealign at. Known indels are taken from two databases:
     *    Mills and 1000 Genome Gold standard Indels
     *    1000 Genomes Phase 1 Indels
     * 
     * 10: IndelRealigner
     *    Makes the realigment
     */
    private int realignBAM() {
        String picard4 = new File(temp, name + "_picard4.bam").getAbsolutePath();
        String intervals = new File(temp, name + "_gatk.intervals").getAbsolutePath();
        String gatk1 = new File(temp, name + "_gatk1.bam").getAbsolutePath();

        updateProgress(resources.getString("align.prealign"), counter++, total);
        int ret = new Command(java7, "-jar", gatk,
                "-T", "RealignerTargetCreator",
                "-R", genome, "-I", picard4,
                "-known", mills, "-known", phase1,
                "-o", intervals).execute(outStream);
        if (ret != 0) {
            return ret;
        }
        updateProgress(resources.getString("align.align"), counter++, total);
        ret = new Command(java7, "-jar", gatk,
                "-T", "IndelRealigner",
                "-R", genome, "-I", picard4,
                "-known", mills, "-known", phase1,
                "-targetIntervals", intervals,
                "-o", gatk1).execute(outStream);
        if (ret != 0) {
            return ret;
        }
        new File(picard4).delete();
        new File(picard4.replace(".bam", ".bai")).delete();
        new File(intervals).delete();

        return 0;
    }

    /* 
     * Phase D: Base Quality Score Recalibration
     *   GATK uses Quality Scores to generate a calibrated error model and apply it to alignments
     * 
     * 11: BaseRecalibrator
     *   Builds the error model. As reference, these databases:
     *    Mills and 100 Genome Gold standard Indels
     *    1000 Genomes Phase 1 Indels
     *    dbSNP 
     * 
     * 12: PrintReads
     *   Applies the recalibration
     */
    private int recalibrateBAM() {
        String gatk1 = new File(temp, name + "_gatk1.bam").getAbsolutePath();
        String recal = new File(temp, name + "_recal.grp").getAbsolutePath();

        updateProgress(resources.getString("align.prerecal"), counter++, total);
        int ret = new Command(java7, "-jar", gatk,
                "-T", "BaseRecalibrator",
                "-I", gatk1,
                "-R", genome,
                "--knownSites", dbsnp,
                "--knownSites", mills,
                "--knownSites", phase1,
                "-o", recal).execute(outStream, true);
        if (ret != 0) {
            return ret;
        }

        updateProgress(resources.getString("align.recal"), counter++, total);
        ret = new Command(java7, "-jar", gatk,
                "-T", "PrintReads",
                "-R", genome,
                "-I", gatk1,
                "-BQSR", recal,
                "-o", output).execute(outStream);
        if (ret != 0) {
            return ret;
        }
        new File(gatk1).delete();
        new File(gatk1.replace(".bam", ".bai")).delete();
        new File(recal).delete();

        return 0;
    }

    /* 
     * Phase E: Reduce reads
     *  Expererimental Tool that reduces the size of BAM Files. Optional.
     * 
     * 13: ReduceReads
     */
    private int reduceReads() {
        String reduced = output.replace(".bam", "_reduced.bam");
        updateProgress(resources.getString("align.reducereads"), counter++, total);
        int ret = new Command(java7, "-jar", gatk,
                "-T", "ReduceReads",
                "-R", genome,
                "-I", output,
                "-o", reduced).execute(outStream);
        if (ret != 0) {
            return ret;
        }
        return 0;
    }

    @Override
    protected int start() {

        updateTitle("Aligning " + new File(output).getName());

        int ret;
        if ((ret = firstAlignment()) != 0) {
            return ret;
        }
        if ((ret = refineBAM()) != 0) {
            return ret;
        }
        if ((ret = realignBAM()) != 0) {
            return ret;
        }
        if ((ret = recalibrateBAM()) != 0) {
            return ret;
        }

        if (reduceReads) {
            if ((ret = reduceReads()) != 0) {
                return ret;
            }
        }
        updateProgress("Completed " + output, 1, 1);
        return 0;
    }

    @Override
    public boolean importParameters() {
        if (!new File(genome).exists()) {
            errStream.println(resources.getString("no.genome"));
            return false;
        }
        if (!new File(forward).exists()) {
            errStream.println(resources.getString("no.forward"));
            return false;
        }
        if (!new File(reverse).exists()) {
            errStream.println(resources.getString("no.reverse"));
            return false;
        }
        if (!new File(dbsnp).exists()) {
            errStream.println(resources.getString("no.dbsnp"));
            return false;
        }
        if (!new File(mills).exists()) {
            errStream.println(resources.getString("no.mills"));
            return false;
        }
        if (!new File(phase1).exists()) {
            errStream.println(resources.getString("no.phase1"));
            return false;
        }
        if (!new File(temp).exists()) {
            errStream.println(resources.getString("no.temp"));
            return false;
        }
        if (output.isEmpty()) {
            errStream.println(resources.getString("no.output"));
            return false;
        }
        return true;
    }

}
