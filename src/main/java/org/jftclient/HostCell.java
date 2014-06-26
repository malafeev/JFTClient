package org.jftclient;

import org.jftclient.config.dao.HostDao;
import org.jftclient.config.domain.Host;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;

/**
 * @author sergei.malafeev
 */
public class HostCell extends ListCell<String> {
    private ContextMenu contextMenu;
    private HostDao hostDao;
    private ComboBox<String> hostField;

    public HostCell(HostDao hostDao, ComboBox<String> hostField) {
        this.hostDao = hostDao;
        this.hostField = hostField;

        MenuItem deleteMenu = new MenuItem("Delete");
        deleteMenu.setOnAction(event -> {
            deleteItems();
        });

        contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(deleteMenu);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && item != null) {
            setContextMenu(contextMenu);
        }

        setText(item == null ? null : item);
        setGraphic(null);
    }

    private void deleteItems() {
        for (String host : getListView().getSelectionModel().getSelectedItems()) {
            Host h = hostDao.getHostByName(host);
            hostDao.delete(h);
        }
        getListView().getItems().setAll(hostDao.getHostNames());
        hostField.getItems().setAll(hostDao.getHostNames());
    }
}
