package dnanalytics.view;

import dnanalytics.tools.AlignViewTool;
import dnanalytics.utils.FileManager;
import dnanalytics.utils.Settings;
import dnanalytics.worker.Worker;
import dnanalytics.utils.DNAOutputStream;
import dnanalytics.tools.Tool;
import dnanalytics.tools.CallVariantsTool;
import dnanalytics.tools.CombineVariantsTool;
import dnanalytics.tools.DindelTool;
import dnanalytics.tools.FilterFrequenciesTool;
import dnanalytics.tools.IndexFastaTool;
import dnanalytics.tools.SelectVariantsTool;
import dnanalytics.tools.AnnotationTool;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/**
 * DNAnalytics Controller for the FXML View. It controls the main view of the GUI. Main tasks of
 * DNAMain are:
 * <ul>
 * <li>To control settings: reference genome, temp directory and language.</li>
 * <li>To show the view of the selected Tool, by requesting it to each Tool.</li>
 * <li>To launch the Worker of the selected Tool.</li>
 * <li>To show main console, with System output (standard and error).</li>
 * </ul>
 * <p/>
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        resources = rb;

        //Initialize Settings
        String value;
        value = Settings.getProperty("genome");
        genome.setText((value != null) ? value : "");
        value = Settings.getProperty("tempDir");
        tempDir.setText(value != null ? value : "");
        if (isIndexed()) {
            alreadyIndexed.setText(getResources().getString(
                    "label.indexed"));
        }
        // Add Tools
        addTool(new IndexFastaTool());
        addTool(new AlignViewTool());
        addTool(new CallVariantsTool());
        addTool(new CombineVariantsTool());
        addTool(new SelectVariantsTool());
        addTool(new FilterFrequenciesTool());
        addTool(new AnnotationTool());
        addTool(new DindelTool());

        // Prepare tools pane
        currentTool.setCollapsible(false);
        currentTool.setText(resources.getString("label.selecttool"));
        /* Do the magic, when the user selects a tool, make it visible in the currentTool pane */
        toolButtons.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) -> {
                    ToggleButton button = (ToggleButton) t1;
                    if (t1 != null) {
                        Tool tool = tools.get(toolButtons.
                                getToggles().indexOf(t1));
                        currentTool.setContent(tool.getView());
                        ImageView a = (ImageView) button.getGraphic();
                        currentTool.setGraphic(new ImageView(a.getImage()));
                        currentTool.setText(tool.getTitle());
                    }
                });

        // Redirect outputs
        System.setErr(new PrintStream(new DNAOutputStream(console, "err>")));
        System.setOut(new PrintStream(new DNAOutputStream(console, ">")));
        System.out.println(getResources().getString("label.welcome"));
        System.out.println(getResources().getString("label.version"));

        // Load languages for settings.
        languagesBox.getItems().clear();
        for (Locale locale : Settings.getLocales()) {
            languagesBox.getItems().add(locale.getDisplayName(Settings.getLocale()));
            if (Settings.getLocale().equals(locale)) {
                languagesBox.getSelectionModel().selectLast();
            }
        }

        // Give the languagesBox the ability to change 
        // system language.
        languagesBox.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String old, String current) -> {
                    for (Locale locale : Settings.getLocales()) {
                        if (current.equals(locale.getDisplayName(Settings.getLocale()))) {
                            Settings.setLocale(locale);
                            return;
                        }
                    }
                });
    }

    /**
     * Adds a Tool to the ToolsPanel. Creates a new ToggleButton to select it in the main view. The
     * Tool is added to a List, binded with its ToggleButton.
     * <p/>
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
     * Launches the selected worker. This method will request the Worker to the Tool.
     * <p/>
     */
    @FXML
    void start() {
        // First of all, check if there is a tool selected
        if (toolButtons.getSelectedToggle() != null) {
            // And if the tool has a worker
            final Worker worker = tools.get(toolButtons.getToggles().indexOf(toolButtons.
                    getSelectedToggle())).getWorker();
            if (worker != null) {
                // Create an sets the console
                // a TextArea which gets Worker output
                final TextArea area = new TextArea();
                area.setId("console");
                area.setWrapText(true);
                area.setEditable(false);
                worker.setStreams(
                        new PrintStream(new DNAOutputStream(area, ">")),
                        new PrintStream(new DNAOutputStream(area, "err>")));
                // Create the button to cancel the Worker
                Button button = new Button(resources.
                        getString("button.cancel"));
                button.setMaxWidth(Float.MAX_VALUE);
                // And a label with the Worker's message.
                Label message = new Label(worker.
                        getMessage());
                message.textProperty().bind(worker.
                        messageProperty());
                // Put it all together, in a VBox.
                VBox box = new VBox();
                box.setAlignment(Pos.TOP_CENTER);
                box.setFillWidth(true);
                VBox.setVgrow(area, Priority.SOMETIMES);
                box.getChildren().addAll(message, button,
                        area);

                // Creates a Progress Bar binded with 
                // Worker's progress
                ProgressBar bar = new ProgressBar();
                bar.progressProperty().bind(worker.
                        progressProperty());
                // And a label binded with Worker's title
                Label label = new Label(worker.getTitle(), bar);
                label.textProperty().bind(worker.titleProperty());
                label.setContentDisplay(ContentDisplay.BOTTOM);

                // Create the new tab, and sets its content
                // and graphic.
                final Tab tab = new Tab();
                tab.setContent(box);
                tab.setGraphic(label);
                tab.setClosable(false);
                consoleTabPane.getTabs().add(tab);
                consoleTabPane.getSelectionModel().select(tab);
                // Add the worker the ability to set the tab
                // closing policy.
                worker.setOnSucceeded(
                        (WorkerStateEvent t) -> {
                            tab.setClosable(true);
                        });

                // Add the button the ability to cancel 
                // the Worker.
                button.setOnAction(
                        (ActionEvent t) -> {
                            worker.cancel(true);
                            tab.setClosable(true);
                        });

                // Everything is ready, let's go.
                new Thread(worker).start();
            }
        }
    }

    /**
     * Properly stops all running Workers before closing the app. Nop, do nothing.
     * <p/>
     * @param event
     * @return
     */
    @FXML
    public boolean closeApp(ActionEvent event) {
        return false;
    }

    /* *****************************************************
     * TAB: Settings ************************************************** */
    @FXML
    void selectGenome(ActionEvent event) {
        // Get the file.
        File f = FileManager.setOpenFile(
                FileManager.FASTA_DESCRIPTION,
                FileManager.FASTA_DESCRIPTION,
                FileManager.FASTA_FILTERS, genome);
        // Save it on settings.
        if (f != null) {
            Settings.setProperty("genome", f.
                    getAbsolutePath());
        }
        // Check if indexed.
        alreadyIndexed.setText(
                isIndexed() ? getResources().getString(
                        "label.indexed") : "");
    }

    /**
     * Check if a genome is indexed.
     * <p/>
     * @return true if all the index files are created, but does not check their integrity.
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
        File file = FileManager.selectFolder(
                "Temporary directory");
        if (file != null) {
            tempDir.setText(file.getAbsolutePath());
            Settings.setProperty("tempDir", file.
                    getAbsolutePath());
        }
    }

    /**
     * @return the resources
     */
    public static ResourceBundle getResources() {
        return resources;
    }
}
