package dnanalytics.worker;

import dnanalytics.view.DNAMain;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

/**
 * Extends Worker so just need to implement start(). It provides
 * executeCommand(String string) to launch system commands to a /bin/bash
 * console. Remember:
 * <ul><li>From Worker
 * <ol>
 * <li>extend Worker</li>
 * <li>use outStream and errStream</li>
 * <li>implement toolTip</li>
 * </ol>
 * </li></ul>
 * <ul><li>From WorkerScript
 * <ol>
 * <li>extend WorkerScript</li>
 * <li>use executeCommand() for /bin/bash commands</li>
 * </ol>
 * </li></ul>
 *
 * @author Pascual Lorente Arencibia
 */
public abstract class WorkerScript extends Worker {

    private Process process;
    private final static ResourceBundle resources = DNAMain.getResources();
    private long startTime;
    private boolean exit;

    @Override
    protected Integer call() {
        // Previous tasks
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        startTime = System.currentTimeMillis();
        outStream.println(resources.getString("time.start") + " " + dateFormat.
                format(Calendar.getInstance().getTime()));
        exit = false;

        // User execution
        int ret = start();

        // Afterwards tasks
        outStream.println(resources.getString("time.end") + " "
                + dateFormat.format(Calendar.getInstance().getTime()));
        outStream.println(resources.getString("time.total") + " "
                + dateFormat.format(System.currentTimeMillis() - startTime));
        updateProgress(1, 1);
        return ret;
    }

    protected int executeCommand(String command) {
        // Simple trigger to terminate execution
        if (exit) {
            return 2;
        }
        // Uncomment this line will show the precise command on the console, really useful to debug.
        outStream.println("Command: " + command);
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        processBuilder.redirectErrorStream(true);
        int ret = 0;
        try {
            process = processBuilder.start();
            // Capture output of the process character by character.
            // Although this method seems inefficient (opposite to readLine()), it gives more control
            // and the process is asleep if there are no characters to read. 
            int c;
            try {
                while ((c = process.getInputStream().read()) != -1) {
                    outStream.print((char) c);
                }
            } catch (IOException ex) {
                errStream.println(resources.getString("worker.lost"));
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
        
    }

    protected ResourceBundle getResourceBundle() {
        return resources;
    }

    /**
     * Write the translation of your script here. Use executeCommand() to run an
     * external command.
     *
     * @return
     */
    protected abstract int start();
}
