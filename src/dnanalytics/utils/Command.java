package dnanalytics.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pascual
 */
public class Command {

    private final String[] args;
    private final PrintStream errOut, stdOut;
    private Process process = null;

    /**
     * Creates a new command. errOut and stdOut cannot be null. It is not
     * recommended to put equal errOut and stdOut. Use another constructor if
     * you want to merge both outputs.
     *
     * @param errOut Where to flush the error output.
     * @param stdOut Where to flush the standard output.
     * @param args The command and its arguments.
     */
    public Command(PrintStream errOut, PrintStream stdOut, String... args) {
        this.args = args;
        this.errOut = errOut;
        this.stdOut = stdOut;
    }

    /**
     * Creates a new command. output cannot be null. Both error and standard
     * error will be flushed to output.
     *
     * @param output Where to flush both stdout and errout.
     * @param args The command and its arguments.
     */
    public Command(PrintStream output, String... args) {
        this.args = args;
        this.errOut = null;
        this.stdOut = output;
    }

    /**
     * Creates a new command. Standard and error outputs will be flushed to
     * System.out.
     *
     * @param args The command and its arguments
     */
    public Command(String... args) {
        this.args = args;
        this.errOut = null;
        this.stdOut = null;
    }

    /**
     * Executes command. Command must be created with constructor. Cannot be
     * modified afterwards. If useBash is true, command will be encapsulated in
     * a /bin/bash call. Use this if you are able to execute your command in a
     * shell but not with execute() or if your command has shell pipes (> |).
     *
     * @param useBash If you want to encapsulate your command in a /bin/bash -c
     * shell.
     * @return The return value of the system call.
     */
    public int execute(boolean useBash) {
        ProcessBuilder pb;
        if (useBash) {
            String h = "";
            for (String s : args) {
                h += s + " ";
            }
            pb = new ProcessBuilder("/bin/bash", "-c", h);
        } else {
            pb = new ProcessBuilder(args);
        }
        OutputStream output = stdOut;
        if (errOut == null) {
            pb.redirectErrorStream(true);
            if (stdOut == null) {
                output = System.out;
            }
        }
        try {
            process = pb.start();
            new Pipe(process.getInputStream(), output).start();
            if (errOut != null) {
                new Pipe(process.getErrorStream(), errOut).start();
            }
            return process.waitFor();
        } catch (IOException ex) {
            Logger.getLogger(Command.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } catch (InterruptedException ex) {
            //Logger.getLogger(Command.class.getName()).log(Level.SEVERE, null, ex);
            return -2;
        }
    }

    /**
     * Executes this command. This is equivalent to execute(false);
     *
     * @return The return code of the command execution.
     */
    public int execute() {
        return execute(false);
    }

    public void kill() {
        if (process != null) {
            process.destroy();
            System.out.println("killing" + process);
        }
    }

    static class Pipe extends Thread {

        private final InputStream input;
        private final OutputStream output;

        public Pipe(InputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public synchronized void start() {
            int c;
            try {
                while ((c = input.read()) != -1) {
                    output.write(c);
                }
            } catch (IOException ex) {
                Logger.getLogger(Command.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
