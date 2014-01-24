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

    /**
     * Creates a new TextAreaWriter.
     *
     * @param area The TextArea where to print.
     */
    public TextAreaWriter(TextArea area) {
        this.area = area;
    }

    @Override
    public void write(int b) throws IOException {
        final String character = String.valueOf((char) b);
        Platform.runLater(() -> {
            area.appendText(character);
        });
        flush();
    }
    
    

}
