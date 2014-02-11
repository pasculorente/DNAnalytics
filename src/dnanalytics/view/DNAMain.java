package dnanalytics.view;

import dnanalytics.DNAnalytics;
import dnanalytics.tools.AlignTool;
import dnanalytics.tools.AnnotationTool;
import dnanalytics.tools.CallVariantsTool;
import dnanalytics.tools.CombineVariantsTool;
import dnanalytics.tools.DindelTool;
import dnanalytics.tools.FilterFrequenciesTool;
import dnanalytics.tools.IndexFastaTool;
import dnanalytics.tools.LowFrequencyTool;
import dnanalytics.tools.SelectVariantsTool;
import dnanalytics.tools.Tool;
import dnanalytics.utils.OS;
import dnanalytics.utils.TextAreaWriter;
import dnanalytics.worker.Worker;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

/**
 * DNAnalytics Controller for the FXML View. It controls the main view of the
 * GUI. Main tasks of DNAMain are: (1) to control settings: reference genome,
 * temp directory and language; (2) to show the view of the selected Tool, by
 * requesting it to each Tool; (3) to launch the Worker of the selected Tool;
 * (4) to show the main console, with System output (standard and error).
 *
 * @author Pasucal Lorente Arencibia
 */
public class DNAMain implements Initializable {

    // Values injected by FXMLLoader
    @FXML
    private static ResourceBundle resources;
    @FXML
    private Label alreadyIndexed;
    @FXML
    private TextField genome;
    @FXML
    private TextArea console;
    @FXML
    private ChoiceBox<String> languagesBox;
    @FXML
    private TextField tempDir;
    @FXML
    private TabPane consoleTabPane;
    @FXML
    private TilePane buttonsPane;
    @FXML
    private TitledPane currentTool;
    // Local variables
    private final ToggleGroup toolButtons = new ToggleGroup();
    private final ArrayList<Tool> tools = new ArrayList<>();
    private final ArrayList<Worker> workers = new ArrayList<>();

    private final DateFormat df = new SimpleDateFormat("HH:mm:ss");

    private Properties properties;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        resources = rb;
        this.properties = DNAnalytics.getProperties();
        //Initialize Settings
        genome.setText(properties.getProperty("genome", ""));
        tempDir.setText(properties.getProperty("tempDir", ""));
        if (isIndexed()) {
            alreadyIndexed.setText(resources.getString("label.indexed"));
        }
        // Add Tools
        addTool(new IndexFastaTool());
        addTool(new AlignTool());
        addTool(new CallVariantsTool());
        addTool(new CombineVariantsTool());
        addTool(new SelectVariantsTool());
        addTool(new FilterFrequenciesTool());
        addTool(new AnnotationTool());
        addTool(new DindelTool());
        addTool(new LowFrequencyTool());
//        addTool(new TestTool());

        // Prepare tools pane
        currentTool.setCollapsible(false);
        currentTool.setText(resources.getString("label.selecttool"));
        /* Do the magic, when the user selects a tool, make it visible in the currentTool pane */

        toolButtons.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) -> {
            ToggleButton button = (ToggleButton) t1;
            if (t1 != null) {
                Tool tool = tools.get(toolButtons.getToggles().indexOf(t1));
                currentTool.setContent(tool.getView());
                ImageView a = (ImageView) button.getGraphic();
                currentTool.setGraphic(new ImageView(a.getImage()));
                currentTool.setText(tool.getTitle());
            }
        });

        // Redirect outputs
        System.setErr(new PrintStream(new TextAreaWriter(console, "e>")));
        System.setOut(new PrintStream(new TextAreaWriter(console, ">")));
        System.out.println(resources.getString("label.welcome"));
        System.out.println(resources.getString("label.version"));

        // Load languages from settings.
        final Locale currentLocale = resources.getLocale();
        DNAnalytics.getAppLocales().stream().forEach((locale) -> {
            languagesBox.getItems().add(locale.getDisplayName(currentLocale));
        });
        languagesBox.getSelectionModel().select(currentLocale.getDisplayName(currentLocale));

        // Give the languagesBox the ability to change system language.
        languagesBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String t, String current) -> {
            for (Locale locale : DNAnalytics.getAppLocales()) {
                if (current.equals(locale.getDisplayName(currentLocale))) {
                    properties.setProperty("language", locale.getLanguage());
                    properties.setProperty("country", locale.getCountry());
                    return;
                }
            }
        });
    }

    /**
     * Adds a Tool to the ToolsPanel. Creates a new ToggleButton to select it in
     * the main view. The Tool is added to a List, binded with its ToggleButton.
     * 
     * @param tool The Tool to add to the main view
     */
    private void addTool(Tool tool) {
        tools.add(tool);
        ToggleButton button = new ToggleButton(tool.getTitle());
        button.setMaxWidth(Double.MAX_VALUE);
        button.setToggleGroup(toolButtons);
        button.setId(tool.getStyleID());
        button.setAlignment(Pos.TOP_LEFT);
        buttonsPane.getChildren().add(button);
    }

    /**
     * Launches the selected worker. This method will request the Worker to the
     * Tool.
     */
    @FXML
    void start() {
        // First of all, check if there is a tool selected
        if (toolButtons.getSelectedToggle() != null) {
            // And if the tool has a worker
            final Worker worker = tools.get(toolButtons.getToggles().indexOf(toolButtons.
                    getSelectedToggle())).getWorker();
            if (!worker.importParameters()) {
                System.err.println("Error en los parámetros.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Console.fxml"), resources);
            Node node = null;
            try {
                node = (Node) loader.load();
            } catch (IOException ex) {
                Logger.getLogger(DNAMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            final ConsoleController controller = loader.getController();
            // Binds output, message and progress.
            worker.setStreams(
                    new PrintStream(new TextAreaWriter(controller.getTextArea(), ""), true),
                    new PrintStream(new TextAreaWriter(controller.getTextArea(), ""), true));
           // worker.setStreams(System.out, System.err);
            controller.getMessage().textProperty().bind(worker.messageProperty());
            controller.getProgress().progressProperty().bind(worker.progressProperty());
            controller.getStarted().setText(df.format(System.currentTimeMillis()));
            controller.getTotalTime().textProperty().bind(worker.getElapsedTime());
            // Tab title, binded to worker title.
            Label label = new Label(worker.getTitle());
            label.textProperty().bind(worker.titleProperty());
            // Create the new tab, and set its content
            final Tab tab = new Tab();
            tab.setContent(node);
            tab.setGraphic(label);
            tab.setClosable(false);
            consoleTabPane.getTabs().add(tab);
            consoleTabPane.getSelectionModel().select(tab);
            // Add the worker the ability to set the tab closing policy.
            worker.setOnSucceeded((WorkerStateEvent t) -> {
                tab.setClosable(true);
                controller.getCancelButton().setDisable(true);
            });
            // Add the button the ability to cancel the Worker.
            controller.getCancelButton().setOnAction((ActionEvent t) -> {
                worker.cancel(true);
                tab.setClosable(true);
                controller.getCancelButton().setDisable(true);
            });
            workers.add(worker);
            // Everything is ready, let's go.
            new Thread(worker).start();
        }
    }

    /**
     * Properly stops all running Workers before closing the app. Nop, do
     * nothing.
     *
     * @param event ?
     * @return always false
     */
    @FXML
    public boolean closeApp(ActionEvent event) {
        workers.stream().filter((worker) -> (worker != null)).forEach((worker) -> {
            worker.cancel(true);
        });
        return false;
    }

    /* *****************************************************
     * TAB: Settings
     ************************************************** */
    @FXML
    void selectGenome(ActionEvent event) {
        // Get the file.
        File f = OS.setOpenFile(
                OS.FASTA_DESCRIPTION,
                OS.FASTA_DESCRIPTION,
                OS.FASTA_FILTERS, genome);
        // Save it on settings.
        if (f != null) {
            properties.setProperty("genome", f.getAbsolutePath());
//            Settings.setProperty("genome", f.getAbsolutePath());
        }
        // Check if indexed.
        alreadyIndexed.setText(
                isIndexed() ? resources.getString("label.indexed") : "");
    }

    /**
     * Check if a genome is indexed.
     * <p/>
     * @return true if all the index files are created, but does not check their
     * integrity.
     */
    private boolean isIndexed() {
        String g = genome.getText();
        String[] ends = {".sa", ".bwt", ".amb", ".ann", ".pac"};
        for (String end : ends) {
            if (!new File(g + end).exists()) {
                return false;
            }
        }
        return true;
    }

    @FXML
    void selectTempDir(ActionEvent event) {
        File file = OS.selectFolder("Temporary directory");
        if (file != null) {
            tempDir.setText(file.getAbsolutePath());
            properties.setProperty("tempDir", file.getAbsolutePath());
        }
    }

    /**
     * @return the resources
     */
    public static ResourceBundle getResources() {
        return resources;
    }
}
