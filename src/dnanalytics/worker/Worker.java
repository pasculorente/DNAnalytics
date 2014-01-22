package dnanalytics.worker;

import dnanalytics.view.DNAMain;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

/**
 * A Worker is an abstract class used to package all single tasks DNAnalytics
 * does. Every Worker runs in background. They are similar to Linux scripts. As
 * it is an extension of the Task class, it is started as a Task, being
 * recommended Thread encapsulated method:
 * <p>
 * new <b>Thread</b>(new <b>Worker</b>()).start().
 * <p>
 * Into DNAnalytics, a Worker is required into a Tool, and it is not needed to
 * launch it manually, the DNAMain will do it. When implementing a Worker,
 * outStream and errStream can be used to have messages printed in a separated
 * console.
 *
 * Every Worker just needs to implement start(). It provides
 * executeCommand(String string) to launch system commands to a /bin/bash
 * console.
 *
 * @author Pascual Lorente Arencibia
 */
public abstract class Worker extends Task<Integer> {

    private Process process;
    protected final static ResourceBundle resources = DNAMain.getResources();
    private long startTime;
    private boolean exit;
    protected PrintStream outStream = System.out;
    protected PrintStream errStream = System.err;
    protected final StringProperty elapsedTime = new SimpleStringProperty();
    protected final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    {
        // It was giving problems with the hours. This line fixes it, but I'm not happy at all,
        // cause I'm not sure if this is portable.
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Redirect outputs of the Worker. By default, the streams are redirected to
     * the System streams (System.out and System.err). Use this method if you
     * want to print the output elsewhere.
     *
     * @param output The new standard output stream.
     * @param error The new error output stream.
     */
    public void setStreams(PrintStream output, PrintStream error) {
        this.outStream = output;
        this.errStream = error;
    }

    /**
     * @return the elapsedTime
     */
    public StringProperty getElapsedTime() {
        return elapsedTime;
    }

    @Override
    protected Integer call() {
        // Previous tasks
        startTime = System.currentTimeMillis();
        exit = false;

        // User execution
        int ret = start();

        // Afterwards tasks
//        outStream.println(resources.getString("time.end") + " "
//                + dateFormat.format(Calendar.getInstance().getTime()));
//        outStream.println(resources.getString("time.total") + " "
//                + dateFormat.format(System.currentTimeMillis() - startTime));
        updateProgress(1, 1);
        return ret;
    }

    /**
     * Use the system to execute the command passed through the args. In Linux,
     * "/bin/bash -c" will be called. In Windows, "cmd -C".
     *
     * @param args Arguments, separated by commands.
     * @return the system return value when the command is finished.
     */
    protected int executeCommand(String... args) {
        return execute(null, args);
    }

    protected int executeCommand(LineParser parser, String... args) {
        return execute(parser, args);

    }

    private int execute(LineParser parser, String... args) {
        // Simple trigger to terminate execution
        if (exit) {
            return 2;
        }
        String h = "";
        for (String s : args) {
            h += s + " ";
        }
        // Uncomment this line will show the precise command on the console, really useful to debug.
        outStream.println("Command=" + h);
        String[] args2 = new String[args.length + 2];
        switch (System.getProperty("os.name")) {
            case "Windows 7":
                args2[0] = "cmd";
                args2[1] = "/C";
                break;
            case "Linux":
            default:
                args2[0] = "/bin/bash";
                args2[1] = "-c";
        }
        int i = 2;
        for (String s : args) {
            args2[i++] = s;
        }
        ProcessBuilder builder;
        builder = new ProcessBuilder(args2);
        builder.redirectErrorStream(true);
        int ret = 0;
        try {
            process = builder.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.
                    getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    outStream.println(line);
                    if (parser != null) {
                        parser.updateLine(line);
                        updateProgress(parser.getMessage(), parser.getProgress(), 1);
                    }
                }
            }
            ret = process.waitFor();
        } catch (InterruptedException | IOException ex) {
            process.destroy();
        }
        //outStream.println("Return value: " + ret);
        // Check if command was succesful
        if (ret != 0) {
            errStream.println(resources.getString("worker.error"));
            // Stop thread
            cancel(true);
        }
        return 0;
    }

    @Override
    protected void cancelled() {
        errStream.println(resources.getString("worker.cancel"));
        if (process != null) {
            process.destroy();
        }
        exit = true;
        updateProgress(1, 1);
//        cancel();

    }

    /**
     * Calls updateProgress and updateMessage from Task, but also updates
     * timestamps.
     *
     * @param message The message for updateMessage.
     * @param progress The progress.
     * @param max The end of the progress.
     */
    protected void updateProgress(String message, double progress, double max) {
        updateMessage(message);
        updateProgress(progress, max);
        Platform.runLater(() -> {
            elapsedTime.setValue(dateFormat.format(System.currentTimeMillis() - startTime));
        });
    }

    protected void parseLine(String line) {

    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * Write the translation of your script here. Use executeCommand() to run an
     * external command.
     *
     * @return process return value.
     */
    protected abstract int start();

    /**
     * Override this method to ensure all parameters are OK. Check here if files
     * exist and parameters are logic. If this method returns false, no new tab
     * will be added to DNAnalytics, and Worker won't be lauched.
     *
     * @return true if parameters are OK. false if Worker shouldn't be run.
     */
    public abstract boolean importParameters();

}
