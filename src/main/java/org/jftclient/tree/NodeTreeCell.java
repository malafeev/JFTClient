package org.jftclient.tree;

import java.io.File;

import org.jftclient.JFTText;
import org.jftclient.OutputPanel;
import org.jftclient.ssh.Connection;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class NodeTreeCell extends TreeCell<Node> {
    private static final Image FOLDER_COLLAPSE_IMAGE = new Image(ClassLoader.getSystemResourceAsStream("folder.png"));
    private static final Image FOLDER_EXPAND_IMAGE = new Image(ClassLoader.getSystemResourceAsStream("folder-open.png"));
    private static final Image FILE_IMAGE = new Image(ClassLoader.getSystemResourceAsStream("file.png"));

    private ContextMenu contextFileMenu = new ContextMenu();
    private ContextMenu contextFolderMenu = new ContextMenu();
    private Connection connection;
    private OutputPanel outputPanel;
    private Image currentImage;
    private Tree tree;
    private CommonTree commonTree;

    public NodeTreeCell(Stage primaryStage, Connection connection, Tree tree, CommonTree commonTree) {
        this.connection = connection;
        this.outputPanel = OutputPanel.getInstance();
        this.tree = tree;
        this.commonTree = commonTree;

        MenuItem refreshMenuFile = new MenuItem("Refresh");
        refreshMenuFile.setOnAction((ActionEvent event) -> refreshItem());
        refreshMenuFile.setAccelerator(new KeyCodeCombination(KeyCode.F5));

        MenuItem refreshMenuFolder = new MenuItem("Refresh");
        refreshMenuFolder.setOnAction((ActionEvent event) -> refreshItem());
        refreshMenuFolder.setAccelerator(new KeyCodeCombination(KeyCode.F5));

        MenuItem deleteMenuFile = new MenuItem("Delete");
        deleteMenuFile.setOnAction((ActionEvent event) -> deleteItems());
        deleteMenuFile.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));

        MenuItem deleteMenuFolder = new MenuItem("Delete");
        deleteMenuFolder.setOnAction((ActionEvent event) -> deleteItems());
        deleteMenuFolder.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));

        MenuItem newFolderMenu = new MenuItem("New Folder");
        newFolderMenu.setOnAction((ActionEvent event) -> createNewFolderDialog(primaryStage));

        MenuItem renameMenuFile = new MenuItem("Rename");
        renameMenuFile.setOnAction((ActionEvent event) -> createRenameDialog(primaryStage));

        MenuItem renameMenuFolder = new MenuItem("Rename");
        renameMenuFolder.setOnAction((ActionEvent event) -> createRenameDialog(primaryStage));

        contextFolderMenu.getItems().addAll(newFolderMenu, refreshMenuFolder, deleteMenuFolder, renameMenuFolder);
        contextFileMenu.getItems().addAll(refreshMenuFile, deleteMenuFile, renameMenuFile);
    }

    private void createRenameDialog(Stage primaryStage) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10d));
        vbox.setSpacing(8d);
        Button btnOk = new Button("OK");
        Button btnCancel = new Button("Cancel");
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10d, 10d, 10d, 10d));
        hbox.setSpacing(10d);
        hbox.setAlignment(Pos.CENTER);
        btnOk.setPrefWidth(80d);
        btnCancel.setPrefWidth(80d);
        hbox.getChildren().addAll(btnOk, btnCancel);

        TextField itemField = new TextField(getTreeItem().getValue().getName());

        vbox.getChildren().addAll(new Text("Please enter a new name:"), itemField, hbox);
        Scene myDialogScene = new Scene(vbox);

        dialog.setScene(myDialogScene);
        dialog.setHeight(150d);
        dialog.setWidth(300d);
        dialog.setTitle("Rename");

        double x = primaryStage.getX() + primaryStage.getWidth() / 2. - dialog.getWidth() / 2.;
        double y = primaryStage.getY() + primaryStage.getHeight() / 2. - dialog.getHeight() / 2.;

        dialog.setX(x);
        dialog.setY(y);

        itemField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                renameItem(itemField.getText().trim(), dialog);
            }
        });

        btnOk.setOnAction(event1 -> {
            renameItem(itemField.getText().trim(), dialog);
        });

        btnCancel.setOnAction(event1 -> dialog.close());

        dialog.showAndWait();
    }

    private void createNewFolderDialog(Stage primaryStage) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10d));
        vbox.setSpacing(8d);
        Button btnOk = new Button("OK");
        Button btnCancel = new Button("Cancel");
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10d, 10d, 10d, 10d));
        hbox.setSpacing(10d);
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

        double x = primaryStage.getX() + primaryStage.getWidth() / 2. - dialog.getWidth() / 2.;
        double y = primaryStage.getY() + primaryStage.getHeight() / 2. - dialog.getHeight() / 2.;

        dialog.setX(x);
        dialog.setY(y);

        folderField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                createFolder(folderField, dialog);
            }
        });
        btnOk.setOnAction(event1 -> {
            createFolder(folderField, dialog);
        });

        btnCancel.setOnAction(event1 -> dialog.close());

        dialog.showAndWait();
    }

    private void renameItem(String newName, Stage dialog) {
        if (newName.isEmpty()) {
            dialog.close();
            return;
        }

        getTreeView().getSelectionModel().clearSelection();

        TreeItem<Node> parent = getTreeItem().getParent();

        File newFile = new File(getTreeItem().getParent().getValue().getPath(), newName);
        File oldFile = new File(getItem().getPath());

        if (isLocalTree()) {
            if (!oldFile.renameTo(newFile)) {
                outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("failed rename  " + oldFile.getAbsolutePath() + " to " +
                        newFile.getAbsolutePath()), JFTText.failed());
                dialog.close();
                return;
            }
        } else {
            connection.mv(oldFile.getAbsolutePath(), newFile.getAbsolutePath());
        }

        Node node = getItem();
        node.setName(newName);
        super.startEdit();
        super.commitEdit(node);

        dialog.close();

        parent.getChildren().setAll(tree.buildChildren(parent));
    }

    private void createFolder(TextField folderField, Stage dialog) {
        String folderName = folderField.getText().trim();
        if (folderName.isEmpty()) {
            dialog.close();
            return;
        }

        File folder = new File(getItem().getPath(), folderName);
        String folderPath = folder.getAbsolutePath();

        getTreeView().getSelectionModel().clearSelection();
        if (tree.isLocal()) {
            if (folder.mkdirs()) {
                outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("mkdir -p " + folderPath));
                getTreeItem().getChildren().setAll(tree.buildChildren(getTreeItem()));
            } else {
                outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("mkdir -p " + folderPath + " "), JFTText.failed());
            }
        } else {
            connection.mkdir(folderPath);
            getTreeItem().getChildren().setAll(tree.buildChildren(getTreeItem()));
        }

        getTreeView().getSelectionModel().select(getTreeItem());
        dialog.close();
    }

    private void deleteItems() {
        commonTree.deleteSelectedItems(getTreeView(), tree);
    }

    @Override
    protected void updateItem(Node item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && item != null) {
            if (getTreeItem().getValue().isFile()) {
                setContextMenu(contextFileMenu);
            } else {
                setContextMenu(contextFolderMenu);
            }

            setText(item.getName());
            if (item.isFile()) {
                currentImage = FILE_IMAGE;
            } else {
                if (getTreeItem().isExpanded()) {
                    currentImage = FOLDER_EXPAND_IMAGE;
                } else {
                    currentImage = FOLDER_COLLAPSE_IMAGE;
                }
            }
            setGraphic(new ImageView(currentImage));

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
        return tree.isLocal();
    }

    public void refreshTree() {
        getTreeView().getSelectionModel().clearSelection();
        TreeItem<Node> root = getTreeView().getRoot();
        root.getChildren().setAll(tree.buildChildren(root));
    }

    private void refreshItem() {
        commonTree.refresh(getTreeView(), tree);
    }

    public Image getCurrentImage() {
        return currentImage;
    }
}

