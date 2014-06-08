package org.jftclient.tree;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;

import org.jftclient.config.ConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * @author smalafeev
 */
@Component
public class LocalTree implements Tree {
    @Autowired
    private ConfigDao configDao;

    public TreeItem<Node> createRootNode() {
        Node node = new Node();
        node.setName("/");
        node.setFile(false);
        node.setPath("/");
        return createNode(node);
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public TreeItem<Node> createNode(Node node) {
        return new TreeItem<Node>(node) {
            private boolean isFirstTimeChildren = true;

            @Override
            public ObservableList<TreeItem<Node>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                return getValue().isFile();
            }
        };
    }

    @Override
    public ObservableList<TreeItem<Node>> buildChildren(TreeItem<Node> treeItem) {
        Node f = treeItem.getValue();
        if (f != null && !f.isFile()) {
            File[] files = new File(f.getPath()).listFiles();
            if (files != null) {
                ObservableList<TreeItem<Node>> children = FXCollections.observableArrayList();

                for (File childFile : files) {
                    if (!configDao.showHiddenFiles()) {
                        if (childFile.isHidden()) {
                            continue;
                        }
                    }
                    children.add(createNode(new Node(childFile)));
                }
                Collections.sort(children, new Comparator<TreeItem<Node>>() {
                    @Override
                    public int compare(TreeItem<Node> o1, TreeItem<Node> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                });

                return children;
            }
        }

        return FXCollections.emptyObservableList();
    }
}
