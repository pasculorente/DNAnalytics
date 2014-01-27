package dnanalytics.worker;

import dnanalytics.utils.Command;
import java.util.ArrayList;

/**
 *
 * Use this class if you're planning to develop a non-java script. Fill commands
 * with initializeCommands and messages with initializeMessages.
 *
 * @author Pascual Lorente Arencibia
 */
public abstract class ScriptWorker extends Worker {

    @Override
    protected int start() {
        ArrayList<Command> commands = initializeCommands();
        ArrayList<String> messages = initializeMessages();
        int ret = 0;
        for (int i = 0; i < commands.size(); i++) {
            updateProgress(messages.get(i), i, commands.size());
            ret = commands.get(i).execute();
            if (ret != 0) {
                return ret;
            }
        }
        return ret;
    }

    /**
     * Implement this method to specify the commands to launch.
     *
     * @return a list with commands.
     */
    protected abstract ArrayList<Command> initializeCommands();

    /**
     * Returns a list with messages for the commands. It must be the same length
     * as initializeCommands list.
     *
     * @return
     */
    protected abstract ArrayList<String> initializeMessages();

}
