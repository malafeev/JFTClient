package org.jftclient.tree;

import java.io.File;
import java.util.Collections;

import org.jftclient.config.dao.ConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * @author sergei.malafeev
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
        node.setLocal(true);
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
                    if (!configDao.get().isShowHiddenFiles() && childFile.isHidden()) {
                        continue;
                    }
                    children.add(createNode(new Node(childFile)));
                }
                Collections.sort(children, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

                return children;
            }
        }

        return FXCollections.emptyObservableList();
    }
}
