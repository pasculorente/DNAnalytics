package dnanalytics.worker;

import dnanalytics.view.DNAMain;
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

    protected final static ResourceBundle resources = DNAMain.getResources();
    private long startTime;
    protected PrintStream outStream = System.out;
    protected PrintStream errStream = System.err;
    protected final StringProperty elapsedTime = new SimpleStringProperty();
    protected final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private Timer timer;

    {
        // It was giving problems with the hours. This line fixes it,
        // but I'm not happy at all, because I'm not sure if this is portable.
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

        timer = new Timer();
        new Thread(timer).start();

        // User execution
        int ret = start();

        timer.stop = true;
        updateProgress(1, 1);
        return ret;
    }

    @Override
    public boolean cancel(boolean bln) {
        errStream.println(resources.getString("worker.cancel"));
        timer.stop = true;
        updateProgress(1, 1);
        return super.cancel(bln); //To change body of generated methods, choose Tools | Templates.

    }

    protected void updateProgress(String message, double progress, double max) {
        updateMessage(message);
        updateProgress(progress, max);

    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * Write the translation of your script here. Use the class Command to
     * execute external commands.
     *
     * @return process return value.
     */
    protected abstract int start();

    /**
     * Override this method to ensure all parameters are OK. Check here if files
     * exist and parameters are logic. If this method returns false, no new tab
     * will be added to DNAnalytics, and Worker won't be launched.
     *
     * @return true if parameters are OK. false if Worker shouldn't be run.
     */
    public abstract boolean importParameters();

    class Timer extends Task<Void> {

        boolean stop = false;

        @Override
        protected Void call() throws Exception {
            while (!stop) {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        elapsedTime.setValue(dateFormat.format(System.currentTimeMillis() - startTime));
                    });
                } catch (InterruptedException ex) {
                }
            }
            return null;
        }

    }
}
