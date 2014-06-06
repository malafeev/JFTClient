package org.jftclient;

import java.awt.AWTException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jftclient.config.ConfigDao;
import org.jftclient.config.Host;
import org.jftclient.ssh.ConnectionState;
import org.jftclient.tree.Node;
import org.jftclient.tree.NodeTreeCell;
import org.jftclient.tree.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
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
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author smalafeev
 */
public class JFTClient extends Application {
    private static final Logger logger = LoggerFactory.getLogger(JFTClient.class);

    private ComboBox<String> hostField = new ComboBox<>();
    private TextField userField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private CheckBox cbxHiddenFiles = new CheckBox("show hidden files");
    private NodeTreeCell cellLocal;
    private NodeTreeCell cellRemote;
    private TitledPane remotePane = new TitledPane();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException, AWTException {
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
        tabOutput.setContent(Common.getInstance().getOutputPanel().getScrollPane());
        tabOutput.setClosable(false);

        tabPane.getTabs().add(tabOutput);

        SplitPane splitHorizontal = new SplitPane();
        splitHorizontal.getItems().addAll(splitTrees, tabPane);
        splitHorizontal.setDividerPositions(0.7f);
        splitHorizontal.setOrientation(Orientation.VERTICAL);

        MenuBar menuBar = createMenu();
        ToolBar toolBar = createToolbar();

        VBox topBox = new VBox();
        topBox.getChildren().addAll(menuBar, toolBar);


        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topBox);
        borderPane.setCenter(splitHorizontal);

        Scene scene = new Scene(borderPane, 950, 700, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("java.png"));
        primaryStage.show();
    }

    private MenuBar createMenu() {
        MenuBar menuBar = new MenuBar();
        ConfigDao config = Common.getInstance().getConfig();

        Menu menuSettings = new Menu("Settings");
        CheckMenuItem cmSavePasswords = new CheckMenuItem("Save passwords");
        cmSavePasswords.setSelected(config.isSavePasswords());

        cmSavePasswords.selectedProperty().addListener((observable, oldValue, newValue) -> {
            config.setSavePasswords(newValue);
            config.save();
        });

        menuSettings.getItems().addAll(cmSavePasswords);
        menuBar.getMenus().addAll(menuSettings);
        return menuBar;
    }

    private TreeView<Node> createLocalTree() {
        Node node = new Node();
        node.setName("/");
        node.setFile(false);
        node.setPath("/");
        TreeItem<Node> root = TreeUtils.createLocalNode(node);
        root.setExpanded(true);
        TreeView<Node> treeView = new TreeView<>(root);

        treeView.setEditable(true);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        treeView.setCellFactory(param -> {
            cellLocal = new NodeTreeCell(true, primaryStage);
            TreeUtils.setDragDropEvent(cellLocal, executorService);
            return cellLocal;
        });

        return treeView;
    }

    @Override
    public void stop() {
        Common.getInstance().getConnection().disconnect();
        executorService.shutdownNow();
    }

    private void connect() {
        Common.getInstance().getConnection().disconnect();

        String host = hostField.getValue();
        if (Strings.isNullOrEmpty(host)) {
            Common.getInstance().getOutputPanel().printRed("host is empty\n");
            return;
        }

        String user = userField.getText();
        if (user.isEmpty()) {
            Common.getInstance().getOutputPanel().printRed("user is empty\n");
            return;
        }

        String password = passwordField.getText();
        if (password.isEmpty()) {
            Common.getInstance().getOutputPanel().printRed("password is empty\n");
            return;
        }

        ConnectionState connectionState = Common.getInstance().getConnection().connect(host, user, password);
        if (!connectionState.isSuccess()) {
            logger.warn("failed to connect");
            return;
        }

        Common.getInstance().getConfig().addHost(user, host, Common.getInstance().getConfig().isSavePasswords() ? password : "");
        Common.getInstance().getConfig().save();


        Node node = new Node();
        node.setFile(false);
        node.setPath("/");
        node.setName("/");

        TreeItem<Node> root = TreeUtils.createRemoteNode(Common.getInstance().getConnection(), node);
        root.setExpanded(true);
        TreeView<Node> treeView = new TreeView<>(root);

        treeView.setCellFactory(param -> {
            cellRemote = new NodeTreeCell(false, primaryStage);
            TreeUtils.setDragDropEvent(cellRemote, executorService);
            return cellRemote;
        });

        remotePane.setContent(treeView);
    }

    private ToolBar createToolbar() {
        Label hostLabel = new Label("Host:");
        Label userLabel = new Label("User:");
        Label passwordLabel = new Label("Password:");

        userField.setText(System.getProperty("user.name"));

        hostField.setEditable(true);
        hostField.setPrefWidth(200.0);
        hostField.getItems().addAll(Common.getInstance().getConfig().getHostNames());

        hostField.valueProperty().addListener((observable, oldValue, newValue) -> {
            Host host = Common.getInstance().getConfig().findHostByName(newValue);
            if (host == null) {
                return;
            }
            userField.setText(host.getUsername());
            if (!Strings.isNullOrEmpty(host.getPassword())) {
                passwordField.setText(host.getPassword());
            }
        });

        cbxHiddenFiles.setSelected(Common.getInstance().getConfig().showHiddenFiles());

        Button button = new Button("Connect");
        button.setOnAction(event -> connect());

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connect();
            }
        });

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        cbxHiddenFiles.selectedProperty().addListener((ov, oldVal, newVal) -> {

            Common.getInstance().getConfig().setShowHiddenFiles(newVal);
            Common.getInstance().getConfig().save();

            cellLocal.refreshTree(Common.getInstance().getConnection());
            if (cellRemote != null) {
                cellRemote.refreshTree(Common.getInstance().getConnection());
            }

        });

        return new ToolBar(
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
