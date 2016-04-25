package org.jftclient.tree;

import com.sun.javafx.scene.control.skin.TreeViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import javafx.scene.control.IndexedCell;
import javafx.scene.control.TreeView;

public class CustomTreeViewSkin<T extends IndexedCell> extends TreeViewSkin<T> {

    public CustomTreeViewSkin(TreeView treeView) {
        super(treeView);
    }

    @Override
    protected VirtualFlow createVirtualFlow() {
        return new CustomVirtualFlow<T>();
    }
}
