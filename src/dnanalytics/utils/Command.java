package dnanalytics.utils;

import dnanalytics.view.DNAMain;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author Pascual
 */
public class Command {

    private final String[] args;
    //private final PrintStream errOut, stdOut;
    private Process process = null;

    /**
     * Creates a new command. errOut and stdOut cannot be null. It is not recommended to put equal
     * errOut and stdOut. Use another constructor if you want to merge both outputs.
     *
     * @param errOut Where to flush the error output.
     * @param stdOut Where to flush the standard output.
     * @param args The command and its arguments.
     */
//    public Command(PrintStream errOut, PrintStream stdOut, String... args) {
//        this.args = args;
//        this.errOut = errOut;
//        this.stdOut = stdOut;
//    }
//    /**
//     * Creates a new command. output cannot be null. Both error and standard
//     * error will be flushed to output.
//     *
//     * @param output Where to flush both stdout and errout.
//     * @param args The command and its arguments.
//     */
//    public Command(PrintStream output, String... args) {
//        this.args = args;
//        this.errOut = null;
//        this.stdOut = output;
//    }
    /**
     * Creates a new command. Standard and error outputs will be flushed to System.out.
     *
     * @param args The command and its arguments
     */
    public Command(String... args) {
        this.args = args;
//        this.errOut = null;
//        this.stdOut = null;
    }

    /**
     * Executes command. Command must be created with constructor. Cannot be modified afterwards. If
     * useBash is true, command will be encapsulated in a /bin/bash call. Use this if you are able
     * to execute your command in a shell but not with execute() or if your command has shell pipes
     * (> |).
     *
     * @param output
     * @param useBash If you want to encapsulate your command in a /bin/bash -c shell.
     * @return The return value of the system call.
     */
    public int execute(PrintStream output, boolean useBash) {
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
        pb.redirectErrorStream(true);
        try {
            process = pb.start();
            if (output != null) {
                int c;
                while ((c = process.getInputStream().read()) != -1) {
                    output.write(c);
                }
            } else {
                // Consume pipe, otherwise, it will be blocked ad infinitum.
                while (process.getInputStream().read() != -1) {
                }
            }
            return process.waitFor();
        } catch (InterruptedException | IOException ex) {
            DNAMain.printMessage(DNAMain.getResources().getString("no.program"));
            return -1;
        }
    }

    /**
     * Executes this command. This is equivalent to execute(false);
     *
     * @return The return code of the command execution.
     */
    public int execute() {
        return execute(System.out, false);
    }

    public int execute(PrintStream output) {
        return execute(output, false);
    }

    public void kill() {
        if (process != null) {
            process.destroy();
//            System.out.println("killing" + process);
        }
    }

    @Override
    public String toString() {
        String ret = "";
        for (String s : args) {
            ret += s + " ";
        }
        return "Command=" + ret;
    }

}
