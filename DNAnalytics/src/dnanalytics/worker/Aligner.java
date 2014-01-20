package dnanalytics.worker;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Script for the alignment of a sample sequences. (1) Initial alignment (2)
 * Refinement (3) Realignment and recalibration (4) Reduce Reads (experimental
 * and optional)
 *
 * @author Pascual Lorente Arencibia
 */
public class Aligner extends Worker {

    private final String forward, reverse, genome, dbsnp, mills, phase1, output, temp;
    private final boolean illumina, reduceReads;

    public Aligner(String temp, String forward, String reverse, String genome, String dbsnp,
            String mills, String phase1, String output, boolean illumina, boolean reduceReads) {
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
    }

    @Override
    protected int start() {

        updateTitle("Aligning " + new File(output).getName());

        // USE ALL THE COOOORES!!!!
        int cores = Runtime.getRuntime().availableProcessors();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_");
        String timestamp = "aln_" + df.format(new Date());
        String seq1 = new File(temp, timestamp + "seq1.sai").getAbsolutePath();
        String seq2 = new File(temp, timestamp + "seq2.sai").getAbsolutePath();
        String bwa = new File(temp, timestamp + "bwa.sam").getAbsolutePath();
        String picard = "java -jar software" + File.separator + "picard" + File.separator;
        String picard1 = new File(temp, timestamp + "picard1.bam").getAbsolutePath();
        String picard2 = new File(temp, timestamp + "picard2.bam").getAbsolutePath();
        String picard3 = new File(temp, timestamp + "picard3.bam").getAbsolutePath();
        String picard4 = new File(temp, timestamp + "picard4.bam").getAbsolutePath();
        String metrics = new File(temp, timestamp + "dedup.metrics").getAbsolutePath();
        String gatk = "java -jar software" + File.separator + "gatk"
                + File.separator + "GenomeAnalysisTK.jar";
        String intervals = new File(temp, timestamp + "gatk.intervals").getAbsolutePath();
        String gatk1 = new File(temp, timestamp + "gatk1.bam").getAbsolutePath();
        String recal = new File(temp, timestamp + "recal.grp").getAbsolutePath();
        ArrayList<Command> commands = new ArrayList<>();
        /*
         * Phase A: Align/Map sequences.
         *  Burrows-Wheeler Aligner. As this project works with paired end sequences, we use 
         *  simple, basic-parameters workflow.
         * 
         * 1 and 2: Align both sequences.
         *   bwa aln -t 4 genome.fasta sequence1.fq.gz -I > seq1.sai
         *   bwa aln -t 4 genome.fasta sequence2.fq.gz -I > seq2.sai
         *     -I   : if the sequence is Illumina 1.3+ encoding
         *     -t 4 : number of threads
         *     The reference genome must be indexed
         * 
         * 3: Generate alignments.
         *   bwa sampe genome.fasta seq1.sai seq2.sai sequence1.fq.gz sequence2.fq.gz > bwa.sam
         */
        commands.add(new Command(resources.getString("align.forward"),
                "bwa", "aln", "-t", String.valueOf(cores),
                genome, forward, (illumina ? "-I" : ""), ">", seq1
        ));
        commands.add(new Command(resources.getString("align.reverse"),
                "bwa", "aln", "-t", String.valueOf(cores),
                genome, reverse, (illumina ? "-I" : ""), ">", seq2
        ));
        commands.add(new Command(resources.getString("align.sampe"),
                "bwa", "sampe", genome, seq1, seq2, forward, reverse, ">", bwa
        ));
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
        commands.add(new Command(resources.getString("align.clean"),
                picard + "CleanSam.jar",
                "INPUT=" + bwa,
                "OUTPUT=" + picard1
        ));
        commands.add(new Command(resources.getString("align.sort"),
                picard + "SortSam.jar",
                "INPUT=" + picard1,
                "OUTPUT=" + picard2,
                "SORT_ORDER=coordinate"
        ));
        commands.add(new Command(resources.getString("align.dedup"),
                picard + "MarkDuplicates.jar",
                "INPUT=" + picard2,
                "OUTPUT=" + picard3,
                "REMOVE_DUPLICATES=true",
                "METRICS_FILE=" + metrics
        ));
        commands.add(new Command(resources.getString("align.addheader"),
                picard + "AddOrReplaceReadGroups.jar",
                "INPUT=" + picard3,
                "OUTPUT=" + picard4,
                "RGPL=Illumina",
                "RGSM=niv",
                "RGPU=flowcell-barcode.lane",
                "RGLB=BAITS"
        ));
        commands.add(new Command(resources.getString("align.index"),
                picard + "BuildBamIndex.jar",
                "INPUT=" + picard4
        ));
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
        commands.add(new Command(resources.getString("align.prealign"),
                gatk, "-T", "RealignerTargetCreator",
                "-R", genome,
                "-I", picard4,
                "-known", mills,
                "-known", phase1,
                (illumina ? "" : "--fix_misencoded_quality_scores"),
                "-o", intervals
        ));
        commands.add(new Command(resources.getString("align.align"),
                gatk, "-T", "IndelRealigner",
                "-R", genome,
                "-I", picard4,
                "-known", mills,
                "-known", phase1,
                (illumina ? "" : " --fix_misencoded_quality_scores"),
                "-targetIntervals", intervals,
                "-o", gatk1
        ));
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
        commands.add(new Command(resources.getString("align.prerecal"),
                gatk, "-T", "BaseRecalibrator",
                "-I", gatk1,
                "-R", genome,
                "--knownSites", dbsnp,
                "--knownSites", mills,
                "--knownSites", phase1,
                "-o", recal
        ));
        commands.add(new Command(resources.getString("align.recal"),
                gatk, "-T", "PrintReads",
                "-R", genome,
                "-I", gatk1,
                "-BQSR", recal,
                "-o", output
        ));
        /* 
         * Phase E: Reduce reads
         *  Expererimental Tool that reduces the size of BAM Files. Optional.
         * 
         * 13: ReduceReads
         */
        if (reduceReads) {
            String reduced = new File(output.replace(".bam", "_reduced.bam")).getAbsolutePath();
            commands.add(new Command(resources.getString("align.reducereads"),
                    gatk, "-T", "ReduceReads",
                    "-R", genome,
                    "-I", output,
                    "-o", reduced
            ));
        }
        int i = 0;
        for (Command command : commands) {
            updateProgress(command.message, i++, commands.size());
            if (executeCommand(command.args) != 0) {
                return -1;
            }
        }
        new File(bwa).delete();
        new File(seq1).delete();
        new File(seq2).delete();
        new File(picard1).delete();
        new File(picard2).delete();
        new File(picard3).delete();
        new File(picard4).delete();
        new File(metrics).delete();
        new File(intervals).delete();
        new File(gatk1).delete();
        new File(recal).delete();

        updateProgress("Finished", 1, 1);
        return 0;
    }

    @Override
    public boolean importParameters() {
        // Check if all parameters are OK.
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

    private class Command {

        String message;
        String[] args;

        public Command(String message, String... args) {
            this.message = message;
            this.args = args;
        }

    }
}
