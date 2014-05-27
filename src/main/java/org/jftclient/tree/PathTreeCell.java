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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;


public class PathTreeCell extends TreeCell<Node> {
    private static final Logger logger = LoggerFactory.getLogger(PathTreeCell.class);
    private boolean isLocalTree;
    private ContextMenu dirMenu = new ContextMenu();
    private ConfigDao config;
    private Connection connection;
    private OutputPanel outputPanel;


    public PathTreeCell(boolean isLocalTree, Common common) {
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


        dirMenu.getItems().addAll(refreshMenu, deleteMenu);
    }

    @Override
    protected void updateItem(Node item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && item != null) {
            setText(item.getName());
            setGraphic(getTreeItem().getGraphic());

            if (item.getLinkDest() != null) {
                setTextFill(Color.BLUE);
            } else {
                setTextFill(Color.BLACK);
            }


            if (!getTreeItem().isLeaf()) {
                setContextMenu(dirMenu);
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

