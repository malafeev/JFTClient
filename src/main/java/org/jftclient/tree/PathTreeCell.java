package org.jftclient.tree;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jftclient.Common;
import org.jftclient.JFTText;
import org.jftclient.OutputPanel;
import org.jftclient.config.ConfigDao;
import org.jftclient.ssh.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class PathTreeCell extends TreeCell<Node> {
    private static final Logger logger = LoggerFactory.getLogger(PathTreeCell.class);
    private boolean isLocalTree;
    private ContextMenu dirMenu = new ContextMenu();
    private ConfigDao config;
    private Connection connection;
    private OutputPanel outputPanel;
    private Stage primaryStage;


    public PathTreeCell(boolean isLocalTree, Common common, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.isLocalTree = isLocalTree;
        this.config = common.getConfig();
        this.connection = common.getConnection();
        this.outputPanel = common.getOutputPanel();

        MenuItem refreshMenu = new MenuItem("Refresh");
        refreshMenu.setOnAction((ActionEvent event) -> {

            getTreeView().getSelectionModel().clearSelection();
            getTreeItem().getChildren().clear();

            if (isLocalTree) {
                getTreeItem().getChildren().addAll(TreeUtils.buildLocalChildren(getTreeItem(), config));
            } else {
                getTreeItem().getChildren().addAll(TreeUtils.buildRemoteChildren(connection, getTreeItem()));
            }

            getTreeView().getSelectionModel().select(getTreeItem());
        });

        MenuItem deleteMenu = new MenuItem("Delete");
        deleteMenu.setOnAction((ActionEvent event) -> {

            getTreeView().getSelectionModel().clearSelection();

            TreeItem<Node> parent = getTreeItem().getParent();

            if (isLocalTree) {
                File src = new File(getItem().getPath());
                if (src.isFile()) {
                    if (!src.delete()) {
                        logger.warn("cannot delete file {}", src.getAbsolutePath());
                        common.getOutputPanel().println(JFTText.getLocalHost(), JFTText.textBlack("rm " +
                                src.getAbsolutePath() + " "), JFTText.FAILED);

                    } else {
                        common.getOutputPanel().println(JFTText.getLocalHost(), JFTText.textBlack("rm " +
                                src.getAbsolutePath()));
                    }
                } else {
                    try {
                        FileUtils.deleteDirectory(src);
                        common.getOutputPanel().println(JFTText.getLocalHost(), JFTText.textBlack("rm " +
                                src.getAbsolutePath()));
                    } catch (IOException e) {
                        logger.warn("failed to remove dir", e);
                        common.getOutputPanel().println(JFTText.getLocalHost(), JFTText.textBlack("rm " +
                                src.getAbsolutePath() + " "), JFTText.FAILED);
                    }
                }

                parent.getChildren().clear();
                parent.getChildren().addAll(TreeUtils.buildLocalChildren(parent, config));
            } else {
                connection.rm(getItem().getPath());
                parent.getChildren().clear();
                parent.getChildren().addAll(TreeUtils.buildRemoteChildren(connection, parent));
            }
        });

        MenuItem newFolderMenu = new MenuItem("New Folder");
        newFolderMenu.setOnAction((ActionEvent event) -> {

            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(primaryStage);


            VBox vbox = new VBox();
            vbox.setPadding(new Insets(10));
            vbox.setSpacing(8);
            Button btnOk = new Button("OK");
            Button btnCancel = new Button("Cancel");
            HBox hbox = new HBox();
            hbox.setPadding(new Insets(10, 10, 10, 10));
            hbox.setSpacing(10);
            hbox.setAlignment(Pos.CENTER);
            btnOk.setPrefWidth(80d);
            btnCancel.setPrefWidth(80d);
            hbox.getChildren().addAll(btnOk, btnCancel);

            TextField folderField = new TextField();

            vbox.getChildren().addAll(new Text("Please enter name:"), folderField, hbox);
            Scene myDialogScene = new Scene(vbox);

            dialog.setScene(myDialogScene);
            dialog.setHeight(150d);
            dialog.setWidth(300d);
            dialog.setTitle("New Folder");

            double x = primaryStage.getX() + primaryStage.getWidth() / 2. - dialog.getWidth() / 2;
            double y = primaryStage.getY() + primaryStage.getHeight() / 2. - dialog.getHeight() / 2;

            dialog.setX(x);
            dialog.setY(y);

            btnOk.setOnAction(event1 -> {
                String folderName = folderField.getText().trim();
                if (folderName.isEmpty()) {
                    dialog.close();
                    return;
                }

                File folder = new File(getItem().getPath(), folderName);
                String folderPath = folder.getAbsolutePath();

                getTreeView().getSelectionModel().clearSelection();
                if (isLocalTree) {
                    if (folder.mkdirs()) {
                        outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("mkdir -p " + folderPath));
                        getTreeItem().getChildren().clear();
                        getTreeItem().getChildren().addAll(TreeUtils.buildLocalChildren(getTreeItem(), config));
                    } else {
                        outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("mkdir -p " + folderPath + " "), JFTText.FAILED);
                    }
                } else {
                    connection.mkdir(folderPath);
                    getTreeItem().getChildren().clear();
                    getTreeItem().getChildren().addAll(TreeUtils.buildRemoteChildren(connection, getTreeItem()));
                }

                getTreeView().getSelectionModel().select(getTreeItem());
                dialog.close();
            });

            btnCancel.setOnAction(event1 -> dialog.close());

            String os = System.getProperty("os.name").toLowerCase();
            if (os.indexOf("mac") >= 0) {
                //TODO: strange hack on OS X
                dialog.show();
                dialog.hide();
            }

            dialog.showAndWait();
        });

        dirMenu.getItems().addAll(refreshMenu, deleteMenu, newFolderMenu);
    }

    @Override
    protected void updateItem(Node item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && item != null) {
            if (!getTreeItem().isLeaf()) {
                setContextMenu(dirMenu);
            }

            setText(item.getName());
            setGraphic(getTreeItem().getGraphic());

            if (item.getLinkDest() != null) {
                setTextFill(Color.BLUE);
            } else {
                setTextFill(Color.BLACK);
            }

        } else {
            setText(null);
            setGraphic(null);
        }
    }

    public boolean isLocalTree() {
        return isLocalTree;
    }

    public void refresh(Connection connection) {
        getTreeView().getSelectionModel().clearSelection();
        TreeItem<Node> root = getTreeView().getRoot();

        root.getChildren().clear();

        if (isLocalTree) {
            root.getChildren().addAll(TreeUtils.buildLocalChildren(root, config));
        } else {
            root.getChildren().addAll(TreeUtils.buildRemoteChildren(connection, root));
        }
    }
}

