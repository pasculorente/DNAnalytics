package dnanalytics.utils;

import dnanalytics.view.DNAMain;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains the method to open the system default web browser.
 * 
 * @author Pascual Lorente Arencibia
 */
public class Browser {
    
    /**
     * Launches the default system web browser and opens the specified url.
     *
     * @param url URL to visit.
     */
    public static void browse(String url) {
        try {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (URISyntaxException ex) {
                Logger.getLogger(DNAMain.class.getName()).
                        log(Level.SEVERE, null,
                        ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(DNAMain.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
}
