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
    private ContextMenu contextMenu = new ContextMenu();
    private ConfigDao config;
    private Connection connection;
    private OutputPanel outputPanel;

    public PathTreeCell(boolean isLocalTree, Common common, Stage primaryStage) {
        this.isLocalTree = isLocalTree;
        this.config = common.getConfig();
        this.connection = common.getConnection();
        this.outputPanel = common.getOutputPanel();

        MenuItem refreshMenu = new MenuItem("Refresh");
        refreshMenu.setOnAction((ActionEvent event) -> {

            getTreeView().getSelectionModel().clearSelection();

            if (isLocalTree) {
                getTreeItem().getChildren().setAll(TreeUtils.buildLocalChildren(getTreeItem(), config));
            } else {
                getTreeItem().getChildren().setAll(TreeUtils.buildRemoteChildren(connection, getTreeItem()));
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
                                src.getAbsolutePath() + " "), JFTText.failed());

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
                                src.getAbsolutePath() + " "), JFTText.failed());
                    }
                }

                parent.getChildren().setAll(TreeUtils.buildLocalChildren(parent, config));
            } else {
                connection.rm(getItem().getPath());
                parent.getChildren().setAll(TreeUtils.buildRemoteChildren(connection, parent));
            }

            getTreeView().getSelectionModel().select(parent);
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
                        getTreeItem().getChildren().setAll(TreeUtils.buildLocalChildren(getTreeItem(), config));
                    } else {
                        outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("mkdir -p " + folderPath + " "), JFTText.failed());
                    }
                } else {
                    connection.mkdir(folderPath);
                    getTreeItem().getChildren().setAll(TreeUtils.buildRemoteChildren(connection, getTreeItem()));
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

        contextMenu.getItems().addAll(refreshMenu, deleteMenu, newFolderMenu);
    }

    @Override
    protected void updateItem(Node item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && item != null) {
            if (!getTreeItem().isLeaf()) {
                setContextMenu(contextMenu);
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

    public void refreshTree(Connection connection) {
        getTreeView().getSelectionModel().clearSelection();
        TreeItem<Node> root = getTreeView().getRoot();

        if (isLocalTree) {
            root.getChildren().setAll(TreeUtils.buildLocalChildren(root, config));
        } else {
            root.getChildren().setAll(TreeUtils.buildRemoteChildren(connection, root));
        }
    }
}

