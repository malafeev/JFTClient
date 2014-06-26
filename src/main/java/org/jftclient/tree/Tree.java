package org.jftclient.tree;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * @author sergei.malafeev
 */
public interface Tree {
    boolean isLocal();

    TreeItem<Node> createNode(Node node);

    ObservableList<TreeItem<Node>> buildChildren(TreeItem<Node> treeItem);
}
