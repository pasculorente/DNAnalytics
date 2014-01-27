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

    /**
     * Creates a new command. errOut and stdOut cannot be null.
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
     * modified afterward.
     *
     * @return The return value of the system call.
     */
    public int execute() {
        String h = "";
        for (String s : args) {
            h += s + " ";
        }
        ProcessBuilder pb = new ProcessBuilder(args);
        PrintStream output = stdOut;
        if (errOut == null) {
            pb.redirectErrorStream(true);
            if (stdOut == null) {
                output = System.out;
            }
        }
        try {
            output.println("Command=" + h);
            Process p = pb.start();
            new Pipe(p.getInputStream(), output).start();
            if (errOut != null) {
                new Pipe(p.getErrorStream(), errOut).start();
            }
            return p.waitFor();
        } catch (IOException ex) {
            Logger.getLogger(Command.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } catch (InterruptedException ex) {
            Logger.getLogger(Command.class.getName()).log(Level.SEVERE, null, ex);
            return -2;
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
