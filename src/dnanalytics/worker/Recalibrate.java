/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dnanalytics.worker;

import dnanalytics.utils.Command;
import dnanalytics.view.DNAMain;
import static dnanalytics.worker.Worker.resources;
import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Recalibrate extends Worker {

    private final String genome, dbsnp, omni, hapmap, mills, output, temp, input;
    private final static String java7 = "/usr/local/jdk1.7.0_45/bin/java";

    /**
     *
     * @param genome Standard reference genome.
     * @param dbsnp dbSNP dataset.
     * @param omni 1000G OMNI dataset.
     * @param hapmap HAPMAP dataset.
     * @param mills 1000G and Mills Gold Standard indels dataset.
     * @param output the output file name.
     * @param temp the temp folder.
     * @param input the input file name.
     */
    public Recalibrate(String genome, String dbsnp, String omni, String hapmap, String mills,
            String output, String temp, String input) {
        this.genome = genome;
        this.dbsnp = dbsnp;
        this.omni = omni;
        this.hapmap = hapmap;
        this.mills = mills;
        this.output = output;
        this.temp = temp;
        this.input = input;
    }

    @Override
    protected int start() {
        String gatk = "software" + File.separator + "gatk" + File.separator + "GenomeAnalysisTK.jar";
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_");
        final String timestamp = "recal_" + df.format(new Date());
        File snpTemp = new File(temp, timestamp + "snp.vcf");
        File snpTranches = new File(temp, timestamp + "snp.tranches");
        File snpRecal = new File(temp, timestamp + "snp.recal");
        File indelTranches = new File(temp, timestamp + "indel.tranches");
        File indelRecal = new File(temp, timestamp + "indel.recal");
        updateTitle("Recalibrating " + new File(output).getName());

        updateProgress(resources.getString("recal.presnp"), 0.5, 4);
        // 1/4 snp model
        new Command(java7, "-jar", gatk,
                "-T", "VariantRecalibrator",
                "-R", genome, "-input", input,
                "-tranchesFile", snpTranches.getAbsolutePath(),
                "-recalFile", snpRecal.getAbsolutePath(),
                "-resource:hapmap,known=false,training=true,truth=true,prior=15.0", hapmap,
                "-resource:omni,known=false,training=true,truth=false,prior=12.0", omni,
                "-resource:mills,known=true,training=true,truth=true,prior=12.0", dbsnp,
                "-an", "QD", "-an", "MQRankSum", "-an", "ReadPosRankSum",
                "-an", "FS", "-an", "DP", "-mode", "SNP",
                "-tranche", "100.0", "-tranche", "99.9",
                "-tranche", "99.0", "-tranche", "90.0").execute(outStream);
        updateProgress(resources.getString("recal.applysnp"), 1.5, 4);
        // 2/4 snp recalibration
        new Command(java7, "-jar", gatk,
                "-T", "ApplyRecalibration",
                "-R", genome, "-input", input,
                "-tranchesFile", snpTranches.getAbsolutePath(),
                "-recalFile", snpRecal.getAbsolutePath(),
                "-o", snpTemp.getAbsolutePath(),
                "--ts_filter_level", "90.0", "-mode", "SNP").execute(outStream);
        updateProgress(resources.getString("recal.preindel"), 2.5, 4);
        // 3/4 indel model
        new Command(java7, "-jar", gatk,
                "-T", "VariantRecalibrator",
                "-R", genome, "-input", snpTemp.getAbsolutePath(),
                "-tranchesFile", indelTranches.getAbsolutePath(),
                "-recalFile", indelRecal.getAbsolutePath(),
                "-resource:mills,known=true,training=true,truth=true,prior=12.0", mills,
                "-an", "MQRankSum", "-an", "ReadPosRankSum",
                "-an", "FS", "-an", "DP", "-mode", "INDEL",
                "-tranche", "100.0", "-tranche", "99.9",
                "-tranche", "99.0", "-tranche", "90.0").execute(outStream);
        updateProgress(resources.getString("recal.applyindel"), 3.5, 4);
        // 4/4 indel rcalibration
        new Command(java7, "-jar", gatk,
                "-T", "ApplyRecalibration",
                "-R", genome, "-input", snpTemp.getAbsolutePath(),
                "-tranchesFile", indelTranches.getAbsolutePath(),
                "-recalFile", indelRecal.getAbsolutePath(),
                "-o", output,
                "--ts_filter_level", "90.0", "-mode", "INDEL").execute(outStream);
        FileFilter ff = (File pathname) -> pathname.getName().startsWith(timestamp);
        updateProgress(resources.getString("recal.completed"), 3.5, 4);
        for (File subfile : new File(temp).listFiles(ff)) {
            subfile.delete();
        }
        return 0;
    }

    @Override
    public boolean importParameters() {
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
        if (!new File(mills).exists()) {
            DNAMain.printMessage(resources.getString("no.input"));
            return false;
        }
        if (!new File(omni).exists()) {
            DNAMain.printMessage(resources.getString("no.input"));
            return false;
        }
        if (!new File(hapmap).exists()) {
            DNAMain.printMessage(resources.getString("no.input"));
            return false;
        }
        return true;
    }

}
