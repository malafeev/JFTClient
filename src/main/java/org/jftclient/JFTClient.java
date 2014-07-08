package org.jftclient;

import java.awt.AWTException;
import java.io.IOException;

import org.jftclient.config.dao.ConfigDao;
import org.jftclient.config.dao.HostDao;
import org.jftclient.config.domain.Config;
import org.jftclient.config.domain.Host;
import org.jftclient.ssh.Connection;
import org.jftclient.ssh.ConnectionState;
import org.jftclient.terminal.LocalTerminal;
import org.jftclient.terminal.RemoteTerminal;
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
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author sergei.malafeev
 */
public class JFTClient extends Application {
    private static final Logger logger = LoggerFactory.getLogger(JFTClient.class);

    private ComboBox<String> hostField = new ComboBox<>();
    private TextField userField = new TextField();
    private PasswordField passwordField = new PasswordField();
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
    private TerminalPanel localTerminalPanel = new TerminalPanel();
    private TerminalPanel remoteTerminalPanel = new TerminalPanel();
    private AnnotationConfigApplicationContext context;
    private TabPane tabPane;
    private Tab remoteTerminalTab;
    private RemoteTerminal remoteTerminal;
    private LocalTerminal localTerminal;

    @Override
    public void start(Stage primaryStage) throws IOException, AWTException {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("JFTClient");

        initSpring();

        TitledPane localPane = new TitledPane("Local", createLocalTree());
        localPane.setPrefHeight(1000d);
        localPane.setCollapsible(false);

        remotePane.setText("Remote");
        remotePane.setPrefHeight(1000d);
        remotePane.setCollapsible(false);

        SplitPane splitTrees = new SplitPane();
        splitTrees.getItems().addAll(localPane, remotePane);
        splitTrees.setDividerPositions(0.5);

        tabPane = new TabPane();
        Tab tabOutput = new Tab();
        tabOutput.setText("Output");
        tabOutput.setContent(OutputPanel.getInstance().getScrollPane());
        tabOutput.setClosable(false);

        Tab tabTerminal = new Tab("Terminal");
        tabTerminal.setContent(localTerminalPanel.getTextArea());
        tabTerminal.setClosable(false);

        remoteTerminalTab = new Tab("Remote");
        remoteTerminalTab.setContent(remoteTerminalPanel.getTextArea());
        remoteTerminalTab.setClosable(false);

        tabPane.getTabs().addAll(tabOutput, tabTerminal);

        SplitPane splitHorizontal = new SplitPane();
        splitHorizontal.getItems().addAll(splitTrees, tabPane);
        splitHorizontal.setDividerPositions(0.7);
        splitHorizontal.setOrientation(Orientation.VERTICAL);

        MenuBar menuBar = createMenu();
        ToolBar toolBar = createToolbar();

        VBox topBox = new VBox();
        topBox.getChildren().addAll(menuBar, toolBar);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topBox);
        borderPane.setCenter(splitHorizontal);

        Scene scene = new Scene(borderPane, 950d, 700d, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("java.png"));

        tabTerminal.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                try {
                    localTerminal.connect();
                } catch (IOException e) {
                    logger.warn("failed to open local terminal", e);
                }
            }
        });

        remoteTerminalTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                try {
                    remoteTerminal.connect(connection, remoteTerminalPanel);
                } catch (JSchException | IOException e) {
                    logger.warn("failed to open remote terminal", e);
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
        remoteTerminal = context.getBean(RemoteTerminal.class);
        localTerminal = context.getBean(LocalTerminal.class);
        localTerminal.setLocalTerminalPanel(localTerminalPanel);
    }

    private MenuBar createMenu() {
        MenuBar menuBar = new MenuBar();

        Menu menuEdit = new Menu("Edit");
        MenuItem editHosts = new MenuItem("Hosts");

        editHosts.setOnAction(event -> createEditHostsDialog());

        menuEdit.getItems().add(editHosts);

        Menu menuView = new Menu("View");
        CheckMenuItem cmShowHiddenFiles = new CheckMenuItem("Show hidden files");
        cmShowHiddenFiles.setSelected(configDao.get().isShowHiddenFiles());

        cmShowHiddenFiles.selectedProperty().addListener((observable, oldValue, newValue) -> {
            Config config = configDao.get();
            config.setShowHiddenFiles(newValue);
            configDao.save(config);

            cellLocal.refreshTree();
            if (cellRemote != null) {
                cellRemote.refreshTree();
            }
        });

        menuView.getItems().addAll(cmShowHiddenFiles);

        Menu menuSettings = new Menu("Settings");
        CheckMenuItem cmSavePasswords = new CheckMenuItem("Save passwords");
        cmSavePasswords.setSelected(configDao.get().isSavePasswords());

        cmSavePasswords.selectedProperty().addListener((observable, oldValue, newValue) -> {
            Config config = configDao.get();
            config.setSavePasswords(newValue);
            configDao.save(config);
        });

        menuSettings.getItems().addAll(cmSavePasswords);

        menuBar.getMenus().addAll(menuEdit, menuView, menuSettings);
        return menuBar;
    }

    private void createEditHostsDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);


        ListView<String> listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        listView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                ObservableList<String> selectedItems = listView.getSelectionModel().getSelectedItems();
                if (selectedItems.isEmpty()) {
                    return;
                }
                for (String host : selectedItems) {
                    Host h = hostDao.getHostByName(host);
                    hostDao.delete(h);
                }
                listView.getItems().setAll(hostDao.getHostNames());
                hostField.getItems().setAll(hostDao.getHostNames());
            }
        });

        listView.getItems().addAll(hostDao.getHostNames());

        listView.setCellFactory(param -> new HostCell(hostDao, hostField));

        StackPane root = new StackPane();
        root.getChildren().add(listView);
        Scene dialogScene = new Scene(root);

        dialog.setScene(dialogScene);

        dialog.setHeight(150d);
        dialog.setWidth(300d);
        dialog.setTitle("Edit Hosts");

        double x = primaryStage.getX() + primaryStage.getWidth() / 2. - dialog.getWidth() / 2.;
        double y = primaryStage.getY() + primaryStage.getHeight() / 2. - dialog.getHeight() / 2.;

        dialog.setX(x);
        dialog.setY(y);

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            //TODO: strange hack on OS X
            dialog.show();
            dialog.toFront();
        } else {
            dialog.showAndWait();
        }
    }

    private TreeView<Node> createLocalTree() {

        TreeItem<Node> root = localTree.createRootNode();
        root.setExpanded(true);
        TreeView<Node> treeView = new TreeView<>(root);

        treeView.setEditable(true);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        treeView.setCellFactory(param -> {
            cellLocal = new NodeTreeCell(primaryStage, connection, localTree, commonTree);
            commonTree.setDragDropEvent(cellLocal, localTree);
            return cellLocal;
        });

        treeView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                commonTree.deleteSelectedItems(treeView, localTree);
            } else if (event.getCode() == KeyCode.F5) {
                commonTree.refresh(treeView, localTree);
            }
        });

        return treeView;
    }

    private void connect() {
        connection.disconnect();
        remoteTerminalPanel.getTextArea().clear();

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
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        treeView.setCellFactory(param -> {
            cellRemote = new NodeTreeCell(primaryStage, connection, remoteTree, commonTree);
            commonTree.setDragDropEvent(cellRemote, localTree);
            return cellRemote;
        });

        treeView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                commonTree.deleteSelectedItems(treeView, remoteTree);
            } else if (event.getCode() == KeyCode.F5) {
                commonTree.refresh(treeView, remoteTree);
            }
        });

        remotePane.setContent(treeView);

        if (tabPane.getTabs().size() == 2) {
            tabPane.getTabs().add(remoteTerminalTab);
        }

        if (remoteTerminalTab.isSelected()) {
            try {
                remoteTerminal.connect(connection, remoteTerminalPanel);
            } catch (JSchException | IOException e) {
                logger.warn("failed to open remote terminal", e);
            }
        }
    }

    private ToolBar createToolbar() {
        Label hostLabel = new Label("Host:");
        Label userLabel = new Label("User:");
        Label passwordLabel = new Label("Password:");

        userField.setText(System.getProperty("user.name"));

        hostField.setEditable(true);
        hostField.setPrefWidth(200d);

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

        Button button = new Button("Connect");
        button.setOnAction(event -> connect());

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connect();
            }
        });

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

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
                region
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
