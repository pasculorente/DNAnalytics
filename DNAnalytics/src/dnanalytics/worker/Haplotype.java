package dnanalytics.worker;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Script for calling variants using HaplotypeCaller from
 * GATK. If 'recalibrate', output file will pass a Variant
 * Quality Score Recalibration (VQSR). See Worker to know
 * how to launch it.
 * <p/>
 * @author Pascual Lorente Arencibia
 */
public class Haplotype extends WorkerScript {

    private String genome, dbsnp, omni, hapmap, mills, output, temp, input;
    private boolean recalibrate;

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
    public Haplotype(String genome, String dbsnp,
            String omni, String hapmap,
            String mills, String output, String input,
            boolean recalibrate,
            String temp) {
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
        ResourceBundle controller = getResourceBundle();
        int noFileErr = -1;
        // Check if all parameters are OK.
        if (!new File(genome).exists()) {
            System.err.println(controller.getString(
                    "no.genome"));
            return noFileErr;
        }
        if (!new File(dbsnp).exists()) {
            System.err.println(controller.getString(
                    "no.dbsnp"));
            return noFileErr;
        }
        /*if (!new File(temp).exists()) {
            System.err.println(controller.getString(
                    "no.temp"));
            return noFileErr;
        }*/
        if (output.isEmpty()) {
            System.err.println(controller.getString(
                    "no.output"));
            return noFileErr;
        }
        if (!new File(input).exists()) {
            System.err.println(controller.getString(
                    "no.input"));
            return noFileErr;
        }
        updateTitle("Calling " + new File(output).getName());
        // Haha, this is what one has to do to avoid /s/l/a/s/h/e/s/
        // although in the end this is not running in Windows OS
        String gatk = "java -jar software"
                + File.separator + "gatk"
                + File.separator + "GenomeAnalysisTK.jar";

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_");
        String timestamp = "call_" + df.format(new Date());

        updateMessage(controller.getString("call.call"));
        updateProgress(1, (recalibrate ? 3 : 2));
        executeCommand(gatk
                + " -T HaplotypeCaller"
                + " -R " + genome
                + " -I " + input
                + " -o " + output
                + " --dbsnp " + dbsnp);

        // Recalibrate
        if (recalibrate) {
            // Check for database
            if (!new File(mills).exists()) {
                System.err.println(controller.getString(
                        "no.mills"));
                return noFileErr;
            }
            if (!new File(hapmap).exists()) {
                System.err.println(controller.getString(
                        "no.hapmap"));
                return noFileErr;
            }
            if (!new File(omni).exists()) {
                System.err.println(controller.getString(
                        "no.omni"));
                return noFileErr;
            }
            File tranches = new File(temp, timestamp + "tranches");
            File recal = new File(temp, timestamp + "recal");
            File outputRecalibrated = new File(output.
                    replace(".vcf", "_recalibrated.vcf"));
            updateProgress(1.5, 3);
            updateMessage(controller.getString(
                    "call.prerecal"));
            executeCommand(gatk
                    + " -T VariantRecalibrator"
                    + " -R " + genome
                    + " -input " + output
                    + " -tranchesFile " + tranches
                    + " -recalFile " + recal
                    + " -resource:hapmap,known=false,training=true,truth=true,prior=15.0 "
                    + hapmap
                    + " -resource:omni,known=false,training=true,truth=false,prior=12.0 "
                    + omni
                    + " -resource:mills,known=true,training=true,truth=true,prior=12.0 "
                    + mills
                    + " -resource:dbsnp,known=true,training=false,truth=false,prior=6.0 "
                    + dbsnp
                    + " -an QD -an MQRankSum -an ReadPosRankSum -an FS -an MQ -an DP"
                    + " -mode BOTH");

            updateMessage(controller.getString("call.recal"));
            updateProgress(2.5, 3);
            executeCommand(gatk
                    + " -T ApplyRecalibration"
                    + " -R " + genome
                    + " -input " + output
                    + " -tranchesFile " + tranches
                    + " -recalFile " + recal
                    + " -o " + outputRecalibrated
                    + " --ts_filter_level 97.0"
                    + " -mode BOTH");
            
            tranches.delete();
            recal.delete();
            updateMessage(controller.getString(
                    "call.completed"));
            updateProgress(1, 1);
        }

        return 0;
    }
}
