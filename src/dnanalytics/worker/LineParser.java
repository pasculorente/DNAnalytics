package dnanalytics.worker;

/**
 *
 * @author uichuimi03
 */

public interface LineParser {

    public void updateLine(String line);

    public String getMessage();

    public double getProgress();

}
