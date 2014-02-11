package dnanalytics.worker;

import dnanalytics.utils.Command;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Script for calling variants using HaplotypeCaller from GATK. If
 * 'recalibrate', output file will pass a Variant Quality Score Recalibration
 * (VQSR). See Worker to know how to launch it.
 *
 * @author Pascual Lorente Arencibia
 */
public class Haplotype extends Worker {

    private final String genome, dbsnp, omni, hapmap, mills, output, temp, input;
    private final boolean recalibrate;
    private final static String java7 = "/usr/local/jdk1.7.0_45/bin/java";

    /**
     *
     * @param genome reference genome. Indexed please.
     * @param dbsnp dbSNP.
     * @param omni 1000G OMNI
     * @param hapmap HAPMAP
     * @param mills 1000G and Mills Gold Standard indels
     * @param output vcf output
     * @param input bam input
     * @param recalibrate true for recalibration
     * @param temp temp folder
     */
    public Haplotype(String genome, String dbsnp, String omni, String hapmap, String mills,
            String output, String input, boolean recalibrate, String temp) {
        this.genome = genome;
        this.dbsnp = dbsnp;
        this.omni = omni;
        this.hapmap = hapmap;
        this.mills = mills;
        this.output = output;
        this.input = input;
        this.recalibrate = recalibrate;
        this.temp = temp;
    }

    @Override
    protected int start() {
        updateTitle("Calling " + new File(output).getName());
        System.out.println("Starting call");
        String gatk = "software" + File.separator + "gatk"
                + File.separator + "GenomeAnalysisTK.jar";

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_");
        String timestamp = "call_" + df.format(new Date());

        updateProgress(resources.getString("call.call"), 1, (recalibrate ? 3 : 2));
        System.out.println("First command");
        new Command(outStream, java7, "-jar", gatk,
                "-T", "HaplotypeCaller", "-R", genome,
                "-I", input, "-o", output,
                "--dbsnp", dbsnp).execute();
        // Recalibrate
        if (recalibrate) {
            String tranches = new File(temp, timestamp + "tranches").getAbsolutePath();
            String recal = new File(temp, timestamp + "recal").getAbsolutePath();
            String outputRecalibrated = output.replace(".vcf", "_recalibrated.vcf");
            updateProgress(resources.getString("call.prerecal"), 1.5, 3);
            new Command(outStream, java7, "-jar", gatk,
                    "-T", "VariantRecalibrator",
                    "-R", genome, "-input", output,
                    "-tranchesFile", tranches,
                    "-recalFile ", recal,
                    "-resource:hapmap,known=false,training=true,truth=true,prior=15.0",
                    hapmap,
                    "-resource:omni,known=false,training=true,truth=false,prior=12.0",
                    omni,
                    "-resource:mills,known=true,training=true,truth=true,prior=12.0",
                    mills,
                    "-resource:dbsnp,known=true,training=false,truth=false,prior=6.0",
                    dbsnp,
                    "-an", "QD", "-an", "MQRankSum", "-an", "ReadPosRankSum",
                    "-an", "FS", "-an", "MQ", "-an", "DP", "-mode BOTH").execute();
            updateProgress(resources.getString("call.recal"), 2.5, 3);
            new Command(outStream, java7, "-jar", gatk,
                    "-T", "ApplyRecalibration",
                    "-R", genome, "-input", output,
                    "-tranchesFile", tranches,
                    "-recalFile", recal,
                    "-o", outputRecalibrated,
                    "--ts_filter_level", "97.0", "-mode", "BOTH").execute();
            new File(tranches).delete();
            new File(recal).delete();
            updateProgress(resources.getString("call.completed"), 1, 1);
        }
        return 0;
    }

    @Override
    public boolean importParameters() {

        // Check if all parameters are OK.
        if (!new File(genome).exists()) {
            System.err.println(resources.getString("no.genome"));
            return false;
        }
        if (!new File(dbsnp).exists()) {
            System.err.println(resources.getString("no.dbsnp"));
            return false;
        }
        /*if (!new File(temp).exists()) {
         System.err.println(controller.getString(
         "no.temp"));
         return noFileErr;
         }*/
        if (output.isEmpty()) {
            System.err.println(resources.getString("no.output"));
            return false;
        }
        if (!new File(input).exists()) {
            System.err.println(resources.getString("no.input"));
            return false;
        }
        if (recalibrate) {
            // Check for database
            if (!new File(mills).exists()) {
                System.err.println(resources.getString("no.mills"));
                return false;
            }
            if (!new File(hapmap).exists()) {
                System.err.println(resources.getString("no.hapmap"));
                return false;
            }
            if (!new File(omni).exists()) {
                System.err.println(resources.getString("no.omni"));
                return false;
            }
        }
        return true;
    }
}
