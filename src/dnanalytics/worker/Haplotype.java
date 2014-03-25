package dnanalytics.worker;

import dnanalytics.utils.Command;
import dnanalytics.view.DNAMain;
import java.io.File;

/**
 * Script for calling variants using HaplotypeCaller from GATK. If 'recalibrate', output file will
 * pass a Variant Quality Score Recalibration (VQSR). See Worker to know how to launch it.
 *
 * @author Pascual Lorente Arencibia
 */
public class Haplotype extends Worker {

    private final String genome, dbsnp, output, input;
    private final static String java7 = "/usr/local/jdk1.7.0_45/bin/java";

    /**
     *
     * @param genome reference genome. Indexed please.
     * @param dbsnp dbSNP.
     * @param output vcf output
     * @param input bam input
     */
    public Haplotype(String genome, String dbsnp, String output, String input) {
        this.genome = genome;
        this.dbsnp = dbsnp;
        this.output = output;
        this.input = input;
    }

    @Override
    protected int start() {
        updateTitle("Calling " + new File(output).getName());
        String gatk = "software" + File.separator + "gatk" + File.separator + "GenomeAnalysisTK.jar";
        updateProgress(resources.getString("call.call"), 1, 2);
        new Command(java7, "-jar", gatk,
                "-T", "HaplotypeCaller", "-R", genome,
                "-I", input, "-o", output,
                "--dbsnp", dbsnp).execute(outStream);
        updateProgress(resources.getString("call.completed"), 1, 1);
        return 0;
    }

    @Override
    public boolean importParameters() {
        // Check if all parameters are OK.
        if (!new File(genome).exists()) {
            DNAMain.printMessage(resources.getString("no.genome"));
            return false;
        }
        if (!new File(dbsnp).exists()) {
            DNAMain.printMessage(resources.getString("no.dbsnp"));
            return false;
        }
        if (output.isEmpty()) {
            DNAMain.printMessage(resources.getString("no.output"));
            return false;
        }
        if (!new File(input).exists()) {
            DNAMain.printMessage(resources.getString("no.input"));
            return false;
        }
        return true;
    }
}
