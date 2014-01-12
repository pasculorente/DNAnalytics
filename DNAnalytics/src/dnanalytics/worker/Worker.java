package dnanalytics.worker;

import java.io.PrintStream;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

/**
 * A Worker is an abstract class used to package all single tasks DNAnalytics does. Every Worker
 * runs in background. They are similar to Linux scripts. As it is an extension of the Task class,
 * it is started as a Task, being recommended Thread encapsulated method:
 * <p>
 * new <b>Thread</b>(new <b>Worker</b>()).start().
 * <p>
 * Into DNAnalytics, a Worker is required into a Tool, and it is not needed to launch it manually,
 * the DNAMain will do it. When implementing a Worker, outStream and errStream can be used to have
 * messages printed in a separated console.
 *
 * @author Pascual Lorente Arencibia
 */
public abstract class Worker extends Task<Integer> {

    protected PrintStream outStream = System.out;
    protected PrintStream errStream = System.err;
    protected final StringProperty elapsedTime = new SimpleStringProperty();

    /**
     * Redirect outputs of the Worker. By default, the streams are redirected to the System streams
     * (System.out and System.err). Use this method if you want to print the output elsewhere.
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

    /**
     * Override this method to ensure all parameters are OK. Check here if files exist and
     * parameters are logic. If this method returns false, no new tab will be added to DNAnalytics,
     * and Worker won't be lauched.
     *
     * @return true if parameters are OK. false if Worker shouldn't be run.
     */
    public abstract boolean checkParameters();
}
