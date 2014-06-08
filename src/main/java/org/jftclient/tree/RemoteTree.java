package org.jftclient.tree;

import java.util.List;

import org.jftclient.ssh.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * @author smalafeev
 */
@Component
public class RemoteTree implements Tree {
    @Autowired
    private Connection connection;

    public TreeItem<Node> createRootNote() {
        Node node = new Node();
        node.setFile(false);
        node.setPath("/");
        node.setName("/");
        return createNode(node);
    }

    @Override
    public boolean isLocal() {
        return false;
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
            List<Node> files = connection.getNodes(f.getPath());
            if (files != null) {
                ObservableList<TreeItem<Node>> children = FXCollections.observableArrayList();

                for (Node childFile : files) {
                    children.add(createNode(childFile));
                }

                return children;
            }
        }

        return FXCollections.emptyObservableList();
    }
}
