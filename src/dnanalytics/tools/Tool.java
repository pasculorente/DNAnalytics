package dnanalytics.tools;

import dnanalytics.worker.Worker;
import javafx.scene.Node;

/**
 * Interface used in DNAnalytics to pack a view for selecting parameters with a
 * worker to do the task. Every Tool must implement 4 methods:
 * <dl>
 * <dt>getView()</dt>
 * <dd>the View for selecting parameters and options</dd>
 * <dt>getWorker()</dt>
 * <dd>the Worker which works with the parameters</dd>
 * <dt>getTitle()</dt>
 * <dd>A short title to show in DNAnalytics</dd>
 * <dt>getStyleID()</dt>
 * <dd>The corresponding string to an entry on dnanalytics.css where at least
 * and -fx-graphic property is set. We like 16x16 icons.</dd>
 * </dl>
 *
 * @author Pascual Lorente Arencibia.
 */
public interface Tool {

    /**
     * Gets the view of the Tools. Let user set parameters and options.
     *
     * @return a JavaFX Node containing the view of the tool.
     */
    public Node getView();

    /**
     * Gets a ready to run Worker. This method will be called by DNAMain, so be
     * sure to set the Worker properly.
     *
     * @return the ready to run Worker.
     */
    public Worker getWorker();

    /**
     * Gets the name of the Tool.
     *
     * @return a name for the tool.
     */
    public String getTitle();

    /**
     * Style ID for the ToggleButton. Create an entry on the css with
     * identically ID.
     *
     * @return an String with the css Style String.
     */
    public String getStyleID();

    /**
     * A paragraph describing the tool. How it works, what is it for and so.
     * 
     * @return a string with a paragraph description.
     */
    public String getDescription();
}
