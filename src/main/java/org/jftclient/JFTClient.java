package org.jftclient;

import java.awt.AWTException;
import java.io.IOException;

import org.jftclient.config.dao.ConfigDao;
import org.jftclient.config.dao.HostDao;
import org.jftclient.config.domain.Config;
import org.jftclient.config.domain.Host;
import org.jftclient.ssh.Connection;
import org.jftclient.ssh.ConnectionState;
import org.jftclient.terminal.LocalSSHServer;
import org.jftclient.terminal.TermUtils;
import org.jftclient.terminal.TerminalPanel;
import org.jftclient.tree.CommonTree;
import org.jftclient.tree.LocalTree;
import org.jftclient.tree.Node;
import org.jftclient.tree.NodeTreeCell;
import org.jftclient.tree.RemoteTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.base.Strings;
import com.jcraft.jsch.JSchException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    private Stage primaryStage;
    private ConfigDao configDao;
    private HostDao hostDao;
    private Connection connection;
    private LocalTree localTree;
    private RemoteTree remoteTree;
    private CommonTree commonTree;
    private LocalSSHServer localSSHServer;
    private TerminalPanel terminalPanel;
    private AnnotationConfigApplicationContext context;

    @Override
    public void start(Stage primaryStage) throws IOException, AWTException {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("JFTClient");

        initSpring();

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
        tabOutput.setContent(OutputPanel.getInstance().getScrollPane());
        tabOutput.setClosable(false);

        Tab tabTerminal = new Tab("Terminal");
        tabTerminal.setContent(terminalPanel.getPanel());
        tabTerminal.setClosable(false);

        tabPane.getTabs().addAll(tabOutput, tabTerminal);

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

        tabTerminal.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    if (localSSHServer.isRunning()) {
                        return;
                    }

                    try {
                        TermUtils.openLocalTerm(localSSHServer, terminalPanel);
                    } catch (JSchException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        primaryStage.show();
    }

    @Override
    public void stop() {
        context.close();
    }

    private void initSpring() {
        context = new AnnotationConfigApplicationContext(JFTConfiguration.class);

        configDao = context.getBean(ConfigDao.class);
        if (configDao.get() == null) {
            Config config = new Config();
            configDao.save(config);
        }
        hostDao = context.getBean(HostDao.class);
        connection = context.getBean(Connection.class);
        localTree = context.getBean(LocalTree.class);
        remoteTree = context.getBean(RemoteTree.class);
        commonTree = context.getBean(CommonTree.class);
        localSSHServer = context.getBean(LocalSSHServer.class);
        terminalPanel = context.getBean(TerminalPanel.class);
    }

    private MenuBar createMenu() {
        MenuBar menuBar = new MenuBar();

        Menu menuSettings = new Menu("Settings");
        CheckMenuItem cmSavePasswords = new CheckMenuItem("Save passwords");
        cmSavePasswords.setSelected(configDao.get().isSavePasswords());

        cmSavePasswords.selectedProperty().addListener((observable, oldValue, newValue) -> {
            Config config = configDao.get();
            config.setSavePasswords(newValue);
            configDao.save(config);
        });

        menuSettings.getItems().addAll(cmSavePasswords);
        menuBar.getMenus().addAll(menuSettings);
        return menuBar;
    }

    private TreeView<Node> createLocalTree() {

        TreeItem<Node> root = localTree.createRootNode();
        root.setExpanded(true);
        TreeView<Node> treeView = new TreeView<>(root);

        treeView.setEditable(true);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        treeView.setCellFactory(param -> {
            cellLocal = new NodeTreeCell(primaryStage, connection, localTree);
            commonTree.setDragDropEvent(cellLocal, localTree);
            return cellLocal;
        });

        return treeView;
    }

    private void connect() {
        connection.disconnect();

        String host = hostField.getValue();
        if (Strings.isNullOrEmpty(host)) {
            OutputPanel.getInstance().printRed("host is empty\n");
            return;
        }

        String user = userField.getText();
        if (user.isEmpty()) {
            OutputPanel.getInstance().printRed("user is empty\n");
            return;
        }

        String password = passwordField.getText();
        if (password.isEmpty()) {
            OutputPanel.getInstance().printRed("password is empty\n");
            return;
        }

        ConnectionState connectionState = connection.connect(host, user, password);
        if (!connectionState.isSuccess()) {
            logger.warn("failed to connect");
            OutputPanel.getInstance().printRed(connectionState.getMsg());
            return;
        }

        Config config = configDao.get();
        Host host1 = hostDao.getHostByName(host);
        if (host1 == null) {
            host1 = new Host();
            host1.setHostname(host);
        }
        host1.setUsername(user);
        host1.setPassword(config.isSavePasswords() ? password : "");
        hostDao.save(host1);

        TreeItem<Node> root = remoteTree.createRootNote();
        root.setExpanded(true);
        TreeView<Node> treeView = new TreeView<>(root);

        treeView.setCellFactory(param -> {
            cellRemote = new NodeTreeCell(primaryStage, connection, remoteTree);
            commonTree.setDragDropEvent(cellRemote, localTree);
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

        hostField.getItems().addAll(hostDao.getHostNames());

        hostField.valueProperty().addListener((observable, oldValue, newValue) -> {

            Host host = hostDao.getHostByName(newValue);
            if (host == null) {
                return;
            }
            userField.setText(host.getUsername());
            if (!Strings.isNullOrEmpty(host.getPassword())) {
                passwordField.setText(host.getPassword());
            }
        });

        cbxHiddenFiles.setSelected(configDao.get().isShowHiddenFiles());

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

            Config config = configDao.get();
            config.setShowHiddenFiles(newVal);
            configDao.save(config);

            cellLocal.refreshTree();
            if (cellRemote != null) {
                cellRemote.refreshTree();
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
