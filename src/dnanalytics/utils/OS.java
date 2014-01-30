package dnanalytics.utils;

import dnanalytics.view.DNAMain;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * Contains methods to control files in DNAnalytics and fields containing file
 * filters. Open and save files, set TextFileds with the name of the files and
 * compress and uncompress files.
 *
 * @author Pascual Lorente Arencibia
 */
public class OS {

    private static File lastPath;
    private static final ResourceBundle resources = DNAMain.getResources();
    /**
     * FASTQ Description (FASTQ file)
     */
    public static final String FASTQ_DESCRIPTION = resources.getString("file.fastq");
    /**
     * Standard extension for VCF Files (.vcf).
     */
    public static final String VCF_EXTENSION = ".vcf";
    /**
     * TSV Description (Tabular separated file)
     */
    public static final String TSV_DESCRIPTION = resources.getString("file.tsv");
    /**
     * Filters for FASTQ format (.fastq | .fq | .fastq.gz | .fq.gz)
     */
    public static final List<String> FASTQ_FILTERS = new ArrayList<String>() {
        {
            add("*.fq");
            add("*.fastq");
            add("*.fq.gz");
            add("*.fastq.gz");
        }
    };
    /**
     * VCF Description (Variant Call File)
     */
    public static final String VCF_DESCRIPTION = resources.getString("file.vcf");
    /**
     * Filters for Binary/Sequence Alignment/Map (.bam | .sam)
     */
    public static final List<String> SAM_BAM_FILTERS = new ArrayList<String>() {
        {
            add("*.bam");
            add("*.sam");
        }
    };
    /**
     * SAM/BAM Description (Binary/Sequence Alignment/Map File)
     */
    public static final String SAM_BAM_DESCRIPTION = resources.getString("file.sambam");
    /**
     * Filters for Tabular Separated Format (.tsv)
     */
    public static final List<String> TSV_FILTERS = new ArrayList<String>() {
        {
            add("*.tsv");
        }
    };
    /**
     * Filters for FASTA format (.fasta | .fa)
     */
    public static final List<String> FASTA_FILTERS = new ArrayList<String>() {
        {
            add("*.fasta");
            add("*.fa");
        }
    };
    /**
     * Filters for Variant Call Format (.vcf)
     */
    public static final List<String> VCF_FILTERS = new ArrayList<String>() {
        {
            add("*.vcf");
        }
    };
    /**
     * Standard extension for SAM Files (.sam).
     */
    public static final String SAM_EXTENSION = ".sam";
    /**
     * FASTA Description (FASTA File)
     */
    public static final String FASTA_DESCRIPTION = resources.getString(
            "file.fasta");
    /**
     * Standard extension for BAM Files (.bam).
     */
    public static final String BAM_EXTENSION = ".bam";

    public OS() {
        switch (System.getProperty("os.name")) {
            case "Windows 7":
                lastPath = new File(System.getenv("user.dir"));
                break;
            case "Linux":
            default:
                lastPath = new File(System.getenv("PWD"));
        }
    }

    /**
     * Unzip a .gz file.
     *
     * @param input File to unzip.
     * @return File unzipped.
     */
    public static File gunzip(File input) {
        OutputStream out = null;
        GZIPInputStream gZIPInputStream = null;
        File output = new File(input.getAbsolutePath().replace(".gz", ""));
        try {
            // Open/Create output file. New file loses .gz
            out = new FileOutputStream(output);
            // Open .gz file
            gZIPInputStream = new GZIPInputStream(new FileInputStream(input));
            // Read uncompressed data
            byte[] buf = new byte[1024];
            int len;
            while ((len = gZIPInputStream.read(buf)) > 0) {
                // Write in output file
                out.write(buf, 0, len);
            }
        } catch (IOException ex) {
            Logger.getLogger(DNAMain.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (gZIPInputStream != null) {
                    gZIPInputStream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(DNAMain.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return output;
    }

    /**
     * Compress a file to a new .gz file.
     *
     * @param input File to compress.
     * @return File compressed.
     */
    public static File gzip(File input) {
        File output = new File(input.getAbsolutePath() + ".gz");
        InputStream inputStream = null;
        GZIPOutputStream gZIPOutputStream = null;
        try {
            inputStream = new FileInputStream(input);
            gZIPOutputStream = new GZIPOutputStream(new FileOutputStream(output));
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                gZIPOutputStream.write(buf, 0, len);
            }
        } catch (IOException ex) {
            Logger.getLogger(DNAMain.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (gZIPOutputStream != null) {
                    gZIPOutputStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(DNAMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return output;
    }

    /**
     * Opens a dialog for the users to select a File from local directory.
     *
     * @param title Dialog title.
     * @param filterDesc Description of file type.
     * @param filters Regular expressions to filter file types (*.extension).
     * @return A File with user selected file, or null if user canceled.
     */
    public static File selectFile(String title, String filterDesc,
            List<String> filters) {
        FileChooser fileChooser = new FileChooser();
        if (title != null) {
            fileChooser.setTitle(title);
        }
        fileChooser.setInitialDirectory(lastPath);
        if (filterDesc != null && filters != null) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(filterDesc, filters));
        }
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            lastPath = file.getParentFile();
            return file;
        }
        return null;
    }

    /**
     * Opens a dialog for the user to select a file and sets textField text to
     * selected file name. If FileChooser is canceled, textField is not
     * modified.
     *
     * @param title FileChooser Title.
     * @param filterDesc Short description of the file filter.
     * @param filters List of regular expressions to filter.
     * @param textField The textField to modify.
     * @return The chosen file or null if the operation was canceled.
     */
    public static File setOpenFile(String title, String filterDesc,
            List<String> filters, TextField textField) {
        File file = selectFile(title, filterDesc, filters);
        if (file != null) {
            textField.setText(file.getAbsolutePath());
        }
        return file;
    }

    /**
     * Shows a dialog to the user to select a Variant Call File (.vcf). Sets the
     * text of the TextField to the name of the file.
     *
     * @param textField A TextField to contain the file name.
     * @return The file selected or null if user canceled.
     */
    public static File openVCF(TextField textField) {
        return setOpenFile(VCF_DESCRIPTION, VCF_DESCRIPTION, VCF_FILTERS, textField);
    }

    public static File openBAM(TextField textField) {
        return setOpenFile(SAM_BAM_DESCRIPTION, BAM_EXTENSION, SAM_BAM_FILTERS, textField);
    }

    /**
     * Opens a dialog for the user to create a File. File system file is not
     * created immediately. The File is just passed to one of the Workers. If
     * the Worker ends successfully, then the file will have been created.
     *
     * @param title Dialog title
     * @param filterDesc Description of file type
     * @param filters Regular expressions to filter file types (*.extension)
     * @param extension default extension
     * @return A File with the user selected file, or null if not file selected
     */
    public static File createFile(String title, String filterDesc,
            List<String> filters, String extension) {
        FileChooser fileChooser = new FileChooser();
        if (title != null) {
            fileChooser.setTitle(title);
        }
        fileChooser.setInitialDirectory(lastPath);
        if (filters != null && filterDesc != null) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(filterDesc, filters));
        }
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            lastPath = file.getParentFile();
            // Add extension to bad named files
            return file.getAbsolutePath().endsWith(extension) ? file : new File(
                    file.getAbsolutePath() + extension);
        }
        return null;
    }

    /**
     * Opens a dialog for the user to create a file and sets the text of the
     * TextField to the file name. File system file is not created immediately.
     * The File is just passed to one of the Workers. If the Worker ends
     * successfully, then the file will have been created.
     *
     * @param title Dialog title
     * @param filterDesc Description of file type
     * @param filters Regular expressions to filter file types (*.extension)
     * @param extension Default extension
     * @param textField textField associated to the file
     */
    public static void setSaveFile(String title, String filterDesc,
            List<String> filters, String extension, TextField textField) {
        File file = createFile(title, filterDesc, filters, extension);
        if (file != null) {
            textField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Opens a dialog for the user to create a Variant Call File (.vcf). The
     * file is not created immediately, just stored as text.
     *
     * @param textField textField containig VCF file name.
     */
    public static void saveVCF(TextField textField) {
        setSaveFile(VCF_DESCRIPTION, VCF_DESCRIPTION, VCF_FILTERS, ".vcf", textField);
    }

    /**
     * Opens a Dialog to select a folder.
     *
     * @param title The title for the DirectoryChooser.
     * @return A File or null if user canceled.
     */
    public static File selectFolder(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        if (title != null) {
            chooser.setTitle(title);
        }
        chooser.setInitialDirectory(lastPath);
        File file = chooser.showDialog(null);
        return (file != null) ? (lastPath = file) : null;
    }

    /**
     * Launches the default system web browser and opens the specified url.
     *
     * @param url URL to visit.
     */
    public static void browse(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
