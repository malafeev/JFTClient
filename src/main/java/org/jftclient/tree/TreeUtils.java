package org.jftclient.tree;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jftclient.config.ConfigDao;
import org.jftclient.ssh.Connection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * @author smalafeev
 */
public class TreeUtils {

    public static TreeItem<Node> createRemoteNode(Connection connection, Node path) {
        return new TreeItem<Node>(path) {
            private boolean isFirstTimeChildren = true;

            @Override
            public ObservableList<TreeItem<Node>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildRemoteChildren(connection, this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                return getValue().isFile();
            }
        };
    }

    public static TreeItem<Node> createLocalNode(final Node f, ConfigDao config) {
        return new TreeItem<Node>(f) {
            private boolean isFirstTimeChildren = true;

            @Override
            public ObservableList<TreeItem<Node>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildLocalChildren(this, config));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                return getValue().isFile();
            }
        };
    }

    public static ObservableList<TreeItem<Node>> buildLocalChildren(TreeItem<Node> treeItem, ConfigDao config) {
        Node f = treeItem.getValue();
        if (f != null && !f.isFile()) {
            File[] files = new File(f.getPath()).listFiles();
            if (files != null) {
                ObservableList<TreeItem<Node>> children = FXCollections.observableArrayList();

                for (File childFile : files) {
                    if (!config.showHiddenFiles()) {
                        if (childFile.isHidden()) {
                            continue;
                        }
                    }
                    children.add(createLocalNode(new Node(childFile), config));
                }
                Collections.sort(children, new Comparator<TreeItem<Node>>() {
                    @Override
                    public int compare(TreeItem<Node> o1, TreeItem<Node> o2) {
                        return o1.getValue().getName().compareTo(o2.getValue().getName());
                    }
                });

                return children;
            }
        }

        return FXCollections.emptyObservableList();
    }

    public static ObservableList<TreeItem<Node>> buildRemoteChildren(Connection connection, TreeItem<Node> treeItem) {
        Node f = treeItem.getValue();
        if (f != null && !f.isFile()) {
            List<Node> files = connection.getNodes(f.getPath());
            if (files != null) {
                ObservableList<TreeItem<Node>> children = FXCollections.observableArrayList();

                for (Node childFile : files) {
                    children.add(createRemoteNode(connection, childFile));
                }

                return children;
            }
        }

        return FXCollections.emptyObservableList();
    }
}
