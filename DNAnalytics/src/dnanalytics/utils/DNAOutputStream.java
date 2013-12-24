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
public class DNAOutputStream extends OutputStream {

    private boolean newline = false;
    private TextArea area;
    private String lineStart;

    /**
     * Creates a new DNAOutputStream.
     *
     * @param area The TextArea where to print.
     * @param lineStart A String with the beginning for new lines. Not supported.
     */
    public DNAOutputStream(TextArea area, String lineStart) {
        this.area = area;
        this.lineStart = lineStart;
    }

    @Override
    public void write(int b) throws IOException {
        final String character = String.valueOf((char) b);
        newline = character.equals(System.lineSeparator());
//        System.out.println("c="+ character + "lb" + System.lineSeparator());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                area.appendText(character);
//                area.appendText(newline ? lineStart + character : character);
            }
        });
    }

}
