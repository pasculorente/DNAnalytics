package dnanalytics.utils;

import java.io.IOException;
import java.io.OutputStream;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 * A very good class to use as output stream in a TextArea. Just instantiate the
 * TextArea and a String as lineStart.
 *
 * @author Pascual Lorente Arencibia
 */
public class TextAreaWriter extends OutputStream {

    private final TextArea area;
    private final String lineStart;
    private boolean newLine = true;

    /**
     * Creates a new TextAreaWriter.
     *
     * @param area The TextArea where to print.
     * @param lineStart A nice lineStarting
     */
    public TextAreaWriter(TextArea area, String lineStart) {
        this.area = area;
        this.lineStart = lineStart;
    }

    @Override
    public void write(int b) throws IOException {
        if (newLine) {
            Platform.runLater(() -> {
                area.appendText(lineStart);
            });
        }
        final String character = String.valueOf((char) b);
        Platform.runLater(() -> {
            area.appendText(character);
        });
        newLine = "\n".equals(character);
        flush();
    }

}
