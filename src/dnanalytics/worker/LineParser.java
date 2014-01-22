package dnanalytics.worker;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author uichuimi03
 */

public abstract class LineParser {

    private final StringProperty message = new SimpleStringProperty();
    private final DoubleProperty progress = new SimpleDoubleProperty();

    public abstract void parseLine(String line);

    public StringProperty getMessage() {
        return message;
    }

    public DoubleProperty getProgress() {
        return progress;
    }

}
