package dnanalytics.worker;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Script for the alignment of a sample sequences. <p>Alignment General View</p>
 * <ol>
 * <li>Initial alignment</li> <ol type="a"> <li>[BWA] aln 1</li> <li>[BWA] aln
 * 2</li> <li>[BWA] sampe 1 n 2
 * </li> </ol> <li> Refinement </li> <ol type="a"> <li>[Picard] CleanSam</li>
 * <li>[Picard] SortSam</li> <li>[Picard] MarkDuplicates</li> <li>[Picard]
 * AddOrReplaceRGHeader</li> <li>[Picard] BuildBamIndex</li> </ol>
 * <li>Realignment and recalibration</li> <ol type="a"> <li>[GATK]
 * RealignerTargetCreator</li> <li>[GATK] IndelRealigner</li> <li>[GATK]
 * BaseRecalibrator</li>
 * <li>[GATK] PrintReads</li> </ol> <li>Reduce Reads (experimental and
 * optional)</li> <ol type="a">
 * <li>[GATK] ReduceReads</li> </ol> </ol>
 *
 * @author Pascual Lorente Arencibia
 */
public class Aligner extends WorkerScript {

    private final String forward, reverse, genome, dbsnp, mills, phase1, output, temp;
    private final boolean illumina, reduceReads;

    public Aligner(String temp, String forward, String reverse, String genome,
            String dbsnp, String mills, String phase1, String output,
            boolean illumina, boolean reduceReads) {
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
        int noFileErr = 1;
        // Check if all parameters are OK.
        if (!new File(genome).exists()) {
            errStream.println(getResourceBundle().getString("no.genome"));
            return noFileErr;
        }
        if (!new File(forward).exists()) {
            errStream.println(getResourceBundle().getString("no.forward"));
            return noFileErr;
        }
        if (!new File(reverse).exists()) {
            errStream.println(getResourceBundle().getString("no.reverse"));
            return noFileErr;
        }
        if (!new File(dbsnp).exists()) {
            errStream.println(getResourceBundle().getString("no.dbsnp"));
            return noFileErr;
        }
        if (!new File(mills).exists()) {
            errStream.println(getResourceBundle().getString("no.mills"));
            return noFileErr;
        }
        if (!new File(phase1).exists()) {
            errStream.println(getResourceBundle().getString("no.phase1"));
            return noFileErr;
        }
        if (!new File(temp).exists()) {
            errStream.println(getResourceBundle().getString("no.temp"));
            return noFileErr;
        }
        if (output.isEmpty()) {
            errStream.println(getResourceBundle().getString("no.output"));
            return noFileErr;
        }

        updateTitle("Aligning " + new File(output).getName());

        // USE ALL THE COOOORES!!!!
        int cores = Runtime.getRuntime().availableProcessors();
        double counter = 0.5;
        final int total = 14;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_");
        String timestamp = "aln_" + df.format(new Date());
        /*
         * Phase A: Align/Map sequences.
         *  Burrows-Wheeler Alignment. As this project works with paired end sequences, we use 
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
        File seq1 = new File(temp, timestamp + "seq1.sai");
        File seq2 = new File(temp, timestamp + "seq2.sai");
        File bwa = new File(temp, timestamp + "bwa.sam");

        updateProgress(counter++, total);
        updateMessage(getResourceBundle().getString("align.forward"));
        executeCommand(
                "bwa aln"
                + " -t " + cores
                + " " + genome
                + " " + forward
                + (illumina ? " -I" : "")
                + " > " + seq1);

        updateProgress(counter, total);

        updateMessage(getResourceBundle().getString("align.reverse"));
        executeCommand(
                "bwa aln"
                + " -t " + cores
                + " " + genome
                + " " + reverse
                + (illumina ? " -I" : "")
                + " > " + seq2);

        updateProgress(counter, total);

        updateMessage(getResourceBundle().getString("align.sampe"));
        executeCommand(
                "bwa sampe"
                + " " + genome
                + " " + seq1
                + " " + seq2
                + " " + forward
                + " " + reverse
                + " > " + bwa);

        seq1.delete();

        seq2.delete();

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
        String picard = "java -jar software" + File.separator + "picard" + File.separator;
        File picard1 = new File(temp, timestamp + "picard1.bam");
        File picard2 = new File(temp, timestamp + "picard2.bam");
        File picard3 = new File(temp, timestamp + "picard3.bam");
        File picard4 = new File(temp, timestamp + "picard4.bam");
        File metrics = new File(temp, timestamp + "dedup.metrics");

        updateProgress(counter++, total);
        updateMessage(getResourceBundle().getString("align.clean"));
        executeCommand(picard
                + "CleanSam.jar"
                + " INPUT=" + bwa
                + " OUTPUT=" + picard1);
        bwa.delete();

        updateProgress(counter++, total);
        updateMessage(getResourceBundle().getString("align.sort"));
        executeCommand(picard
                + "SortSam.jar"
                + " INPUT=" + picard1
                + " OUTPUT=" + picard2
                + " SORT_ORDER=coordinate");
        picard1.delete();

        updateProgress(counter++, total);
        updateMessage(getResourceBundle().getString("align.dedup"));
        executeCommand(picard
                + "MarkDuplicates.jar"
                + " INPUT=" + picard2
                + " OUTPUT=" + picard3
                + " REMOVE_DUPLICATES=true"
                + " METRICS_FILE=" + metrics);
        picard2.delete();

        metrics.delete();

        updateProgress(counter++, total);
        updateMessage(getResourceBundle().getString("align.addheader"));
        executeCommand(picard
                + "AddOrReplaceReadGroups.jar"
                + " INPUT=" + picard3
                + " OUTPUT=" + picard4
                + " RGPL=Illumina"
                + " RGSM=niv"
                + " RGPU=flowcell-barcode.lane"
                + " RGLB=BAITS");
        picard3.delete();

        updateProgress(counter++, total);
        updateMessage(getResourceBundle().getString("align.index"));
        executeCommand(picard
                + "BuildBamIndex.jar"
                + " INPUT=" + picard4);

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

        // GATK commandLine
        //  -Xmx10g : Limits use of memory (for Java execution) to 10 GB
        //  -nct    : Number of threads
        String gatk = "java -jar software"
                + File.separator + "gatk"
                + File.separator + "GenomeAnalysisTK.jar";
        File intervals = new File(temp, timestamp + "gatk.intervals");
        File gatk1 = new File(temp, timestamp + "gatk1.bam");

        updateProgress(counter++, total);
        updateMessage(getResourceBundle().getString("align.prealign"));
        executeCommand(
                gatk
                + " -T RealignerTargetCreator"
                + " -R " + genome
                + " -I " + picard4
                + " -known " + mills
                + " -known " + phase1
                + (illumina ? "" : " --fix_misencoded_quality_scores")
                + " -o " + intervals);

        updateProgress(counter++, total);
        updateMessage(getResourceBundle().getString("align.align"));
        executeCommand(
                gatk
                + " -T IndelRealigner"
                + " -R " + genome
                + " -I " + picard4
                + " -known " + mills
                + " -known " + phase1
                + (illumina ? "" : " --fix_misencoded_quality_scores")
                + " -targetIntervals " + intervals
                + " -o " + gatk1);
        picard4.delete();

        intervals.delete();

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
        File recal = new File(temp, timestamp + "recal.grp");

        updateProgress(counter++, total);
        updateMessage(getResourceBundle().getString("align.prerecal"));
        executeCommand(
                gatk
                + " -T BaseRecalibrator"
                + " -I " + gatk1
                + " -R " + genome
                + " --knownSites " + dbsnp
                + " --knownSites " + mills
                + " --knownSites " + phase1
                + " -o " + recal);


        updateProgress(counter++, total);
        updateMessage(getResourceBundle().getString("align.recal"));
        executeCommand(
                gatk
                + " -T PrintReads"
                + " -R " + genome
                + " -I " + gatk1
                + " -BQSR " + recal
                + " -o " + output);

        gatk1.delete();

        recal.delete();

        /* 
         * Phase E: Reduce reads
         *  Expererimental Tool that reduces the size of BAM Files. Optional.
         * 
         * 13: ReduceReads
         */
        if (reduceReads) {
            File reduced = new File(output.replace(".bam", "_reduced.bam"));

            updateProgress(counter++, total);
            updateMessage(getResourceBundle().getString("align.reducereads"));
            executeCommand(
                    gatk
                    + " -T ReduceReads"
                    + " -R " + genome
                    + " -I " + output
                    + " -o " + reduced);
        }

        updateMessage(getResourceBundle().getString("align.completed"));
        outStream.println(output + " generated succesfully (^o^)");
        updateProgress(
                1, 1);

        return 0;
    }
// Comments
//        String[] args = {};
//
//        outStream.println(getResourceBundle().getString("align.clean"));
//        updateMessage(getResourceBundle().getString("align.clean"));
//        CleanSam cleanSam = new CleanSam();
//        cleanSam.INPUT = bwa;
//        cleanSam.OUTPUT = picard1;
//        cleanSam.instanceMain(args);
//        bwa.delete();
//
//        outStream.println(getResourceBundle().getString("align.sort"));
//        updateMessage(getResourceBundle().getString("align.sort"));
//        SortSam sortSam = new SortSam();
//        sortSam.INPUT = picard1;
//        sortSam.OUTPUT = picard2;
//        sortSam.SORT_ORDER = SAMFileHeader.SortOrder.coordinate;
//        sortSam.instanceMain(args);
//        picard1.delete();
//
//        outStream.println("Starting dups");
//        updateMessage(getResourceBundle().getString("align.dedup"));
//        MarkDuplicates markDuplicates = new MarkDuplicates();
//        markDuplicates.INPUT = new ArrayList<>();
//        markDuplicates.INPUT.add(picard2);
//        markDuplicates.OUTPUT = picard3;
//        markDuplicates.REMOVE_DUPLICATES = true;
//        markDuplicates.METRICS_FILE = metrics;
//        markDuplicates.instanceMain(args);
//        picard2.delete();
//        metrics.delete();
//
//        updateMessage(getResourceBundle().getString("align.addRGheader"));
//        AddOrReplaceReadGroups addOrReplaceReadGroups = new AddOrReplaceReadGroups();
//        addOrReplaceReadGroups.INPUT = picard3;
//        addOrReplaceReadGroups.OUTPUT = picard4;
//        addOrReplaceReadGroups.RGPL = "Illumina";
//        addOrReplaceReadGroups.RGSM = "niv";
//        addOrReplaceReadGroups.RGPU = "flowcell-barcode.lane";
//        addOrReplaceReadGroups.RGLB = "BAITS";
//        addOrReplaceReadGroups.instanceMain(args);
//        picard3.delete();
//
//        updateMessage(getResourceBundle().getString("align.index"));
//        BuildBamIndex buildBamIndex = new BuildBamIndex();
//        buildBamIndex.INPUT = picard4.getAbsolutePath();
//        buildBamIndex.instanceMain(args);

}
