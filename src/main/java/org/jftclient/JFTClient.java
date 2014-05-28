package org.jftclient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jftclient.command.CommandCopy;
import org.jftclient.command.CommandCopyExecution;
import org.jftclient.command.CommandCopyFactory;
import org.jftclient.ssh.ConnectionState;
import org.jftclient.tree.Node;
import org.jftclient.tree.PathTreeCell;
import org.jftclient.tree.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author smalafeev
 */
public class JFTClient extends Application {
    private static final Logger logger = LoggerFactory.getLogger(JFTClient.class);

    private ToolBar toolBar;
    private ComboBox<String> hostField = new ComboBox<>();
    private TextField userField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private DataFormat dataFormat = new DataFormat("tree");
    private CheckBox cbxHiddenFiles = new CheckBox("show hidden files");
    private PathTreeCell cellLocal;
    private PathTreeCell cellRemote;
    private TitledPane remotePane = new TitledPane();
    private Common common = new Common();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("JFTClient");

        TitledPane localPane = new TitledPane("Local", createLocalTree());
        localPane.setPrefHeight(1000f);
        localPane.setCollapsible(false);

        remotePane.setText("Remote");
        remotePane.setPrefHeight(1000f);
        remotePane.setCollapsible(false);

        SplitPane splitTrees = new SplitPane();
        splitTrees.getItems().addAll(localPane, remotePane);
        splitTrees.setDividerPositions(0.5f);

        TabPane tabPane = new TabPane();
        Tab tabOutput = new Tab();
        tabOutput.setText("Output");
        tabOutput.setContent(common.getOutputPanel().getScrollPane());
        tabOutput.setClosable(false);

        tabPane.getTabs().add(tabOutput);

        SplitPane splitHorizontal = new SplitPane();
        splitHorizontal.getItems().addAll(splitTrees, tabPane);
        splitHorizontal.setDividerPositions(0.7f);
        splitHorizontal.setOrientation(Orientation.VERTICAL);

        createToolbar();

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(toolBar);
        borderPane.setCenter(splitHorizontal);

        Scene scene = new Scene(borderPane, 950, 700, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TreeView<Node> createLocalTree() {
        Node node = new Node();
        node.setName("/");
        node.setFile(false);
        node.setPath("/");
        TreeItem<Node> root = TreeUtils.createLocalNode(node, common.getConfig());
        root.setExpanded(true);
        TreeView<Node> treeView = new TreeView<>(root);

        treeView.setEditable(true);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        treeView.setCellFactory(param -> {
            cellLocal = new PathTreeCell(true, common, primaryStage);
            setDragDropEvent(cellLocal);
            return cellLocal;
        });

        return treeView;
    }

    private void setDragDropEvent(PathTreeCell cell) {

        //Source:
        cell.setOnDragDetected(event -> {
            Dragboard db = cell.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();

            List<Node> files = new ArrayList<>();
            ObservableList<TreeItem<Node>> selectedItems = cell.getTreeView().getSelectionModel().getSelectedItems();
            for (TreeItem<Node> node : selectedItems) {
                files.add(node.getValue());
            }

            content.put(dataFormat, files);
            db.setContent(content);

            event.consume();
        });

        //Target:
        cell.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        });


        //Target:
        cell.setOnDragEntered(event -> {
            cell.setStyle("-fx-background-color: green;");
            event.consume();
        });

        //Target
        cell.setOnDragExited(event -> {
            cell.setStyle("");
            event.consume();
        });

        //Target
        cell.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            event.setDropCompleted(true);

            PathTreeCell source = (PathTreeCell) event.getGestureSource();
            PathTreeCell target = (PathTreeCell) event.getGestureTarget();

            List<Node> files = (List<Node>) db.getContent(dataFormat);
            String targetPath = cell.getItem().getPath();

            if (source.isLocalTree() && target.isLocalTree()) {
                //Local to Local
                for (Node file : files) {
                    File src = new File(file.getPath());
                    File dest = new File(targetPath);

                    if (LocalFileUtil.copy(src, dest)) {
                        common.getOutputPanel().println(JFTText.getLocalHost(), JFTText.textBlack("cp " +
                                file.getPath() + " " + cell.getItem().getPath()));
                    } else {
                        common.getOutputPanel().println(JFTText.getLocalHost(), JFTText.textBlack("cp " +
                                file.getPath() + " " + cell.getItem().getPath() + " "), JFTText.FAILED);
                    }

                    cell.getTreeItem().getChildren().clear();
                    cell.getTreeItem().getChildren().addAll(TreeUtils.buildLocalChildren(cell.getTreeItem(), common.getConfig()));
                }
            } else if (source.isLocalTree() && !target.isLocalTree()) {
                //Local to Remote
                List<CommandCopy> commandCopies = getCommands(files, source.isLocalTree(), target.isLocalTree(), targetPath);

                for (CommandCopy commandCopy : commandCopies) {
                    CommandCopyExecution commandCopyExecution = new CommandCopyExecution(commandCopy, common);
                    Future<Boolean> task = executorService.submit(commandCopyExecution);

                }
            } else if (!source.isLocalTree() && !target.isLocalTree()) {
                //Remote to Remote
                List<CommandCopy> commandCopies = getCommands(files, source.isLocalTree(), target.isLocalTree(), targetPath);

                for (CommandCopy commandCopy : commandCopies) {
                    common.getConnection().sendCommand(commandCopy.toString());
                }
            } else if (!source.isLocalTree() && target.isLocalTree()) {
                //Remote to Local
                List<CommandCopy> commandCopies = getCommands(files, source.isLocalTree(), target.isLocalTree(), targetPath);

                for (CommandCopy commandCopy : commandCopies) {
                    CommandCopyExecution commandCopyExecution = new CommandCopyExecution(commandCopy, common);
                    Future<Boolean> task = executorService.submit(commandCopyExecution);
                }
            }
            //cell.getTreeView().getSelectionModel().select(cell.getTreeItem());
            event.consume();
        });

        //Source
        cell.setOnDragDone(event -> {
            event.consume();
        });

    }

    private List<CommandCopy> getCommands(List<Node> files, boolean isSourceLocal, boolean isTargetLocal, String targetPath) {
        List<String> sources = new ArrayList<>();
        for (Node file : files) {
            sources.add(file.getPath());
        }

        CommandCopyFactory commandCopyFactory = new CommandCopyFactory(common.getConnection());
        return commandCopyFactory.buildCommands(isSourceLocal, isTargetLocal, targetPath, sources);
    }

    @Override
    public void stop() {
        common.getConnection().disconnect();
        executorService.shutdownNow();
    }

    private void connect() {
        common.getConnection().disconnect();

        String host = hostField.getValue();
        if (Strings.isNullOrEmpty(host)) {
            common.getOutputPanel().printRed("host is empty\n");
            return;
        }

        String user = userField.getText();
        if (user.isEmpty()) {
            common.getOutputPanel().printRed("user is empty\n");
            return;
        }

        String password = passwordField.getText();
        if (password.isEmpty()) {
            common.getOutputPanel().printRed("password is empty\n");
            return;
        }

        ConnectionState connectionState = common.getConnection().connect(host, user, password);
        if (!connectionState.isSuccess()) {
            logger.warn("failed to connect");
            return;
        }

        if (common.getConfig().addHostName(host)) {
            common.getConfig().save();
        }

        Node node = new Node();
        node.setFile(false);
        node.setPath("/");
        node.setName("/");

        TreeItem<Node> root = TreeUtils.createRemoteNode(common.getConnection(), node);
        root.setExpanded(true);
        TreeView<Node> treeView = new TreeView<>(root);

        treeView.setCellFactory(param -> {
            cellRemote = new PathTreeCell(false, common, primaryStage);
            setDragDropEvent(cellRemote);
            return cellRemote;
        });

        remotePane.setContent(treeView);
    }

    private void createToolbar() {
        Label hostLabel = new Label("Host:");
        Label userLabel = new Label("User:");
        Label passwordLabel = new Label("Password:");

        userField.setText(System.getProperty("user.name"));

        hostField.setEditable(true);
        hostField.setPrefWidth(200.0);
        hostField.getItems().addAll(common.getConfig().getHostNames());

        cbxHiddenFiles.setSelected(common.getConfig().showHiddenFiles());

        Button button = new Button("Connect");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                connect();
            }
        });

        passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    connect();
                }
            }
        });

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        cbxHiddenFiles.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                                Boolean oldVal, Boolean newVal) {

                common.getConfig().setShowHiddenFiles(newVal);
                common.getConfig().save();

                cellLocal.refreshTree(common.getConnection());
                if (cellRemote != null) {
                    cellRemote.refreshTree(common.getConnection());
                }

            }
        });

        toolBar = new ToolBar(
                hostLabel,
                hostField,
                new Label("  "),
                userLabel,
                userField,
                new Label("  "),
                passwordLabel,
                passwordField,
                new Label("  "),
                button,
                region,
                cbxHiddenFiles
        );
    }


    public static void main(String[] args) {
        launch(args);
    }
}
