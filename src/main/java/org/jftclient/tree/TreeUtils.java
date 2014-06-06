package org.jftclient.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.jftclient.Common;
import org.jftclient.JFTText;
import org.jftclient.LocalFileUtils;
import org.jftclient.OutputPanel;
import org.jftclient.command.CommandCopy;
import org.jftclient.command.CommandCopyExecution;
import org.jftclient.command.CommandCopyFactory;
import org.jftclient.ssh.Connection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * @author smalafeev
 */
public class TreeUtils {
    private static DataFormat dataFormat = new DataFormat("tree");

    public static TreeItem<Node> createRemoteNode(Connection connection, Node node) {
        return new TreeItem<Node>(node) {
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

    public static TreeItem<Node> createLocalNode(final Node node) {
        return new TreeItem<Node>(node) {
            private boolean isFirstTimeChildren = true;

            @Override
            public ObservableList<TreeItem<Node>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildLocalChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                return getValue().isFile();
            }
        };
    }

    public static ObservableList<TreeItem<Node>> buildLocalChildren(TreeItem<Node> treeItem) {
        Node f = treeItem.getValue();
        if (f != null && !f.isFile()) {
            File[] files = new File(f.getPath()).listFiles();
            if (files != null) {
                ObservableList<TreeItem<Node>> children = FXCollections.observableArrayList();

                for (File childFile : files) {
                    if (!Common.getInstance().getConfig().showHiddenFiles()) {
                        if (childFile.isHidden()) {
                            continue;
                        }
                    }
                    children.add(createLocalNode(new Node(childFile)));
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

    public static void setDragDropEvent(NodeTreeCell cell, ExecutorService executorService) {

        //Source:
        cell.setOnDragDetected(event -> {
            Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
            db.setDragView(cell.getCurrentImage());

            ClipboardContent content = new ClipboardContent();

            List<Node> files = new ArrayList<>();
            ObservableList<TreeItem<Node>> selectedItems = cell.getTreeView().getSelectionModel().getSelectedItems();
            for (TreeItem<Node> node : selectedItems) {
                files.add(node.getValue());
            }

            content.put(dataFormat, files);
            db.setContent(content);

            event.consume();
        });

        //Target:
        cell.setOnDragOver(event -> {
            if (event.getGestureSource() != cell) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        //Target:
        cell.setOnDragEntered(event -> {
            if (event.getGestureSource() != cell) {
                cell.setStyle("-fx-background-color: green;");
            }
            event.consume();
        });

        //Target
        cell.setOnDragExited(event -> {
            cell.setStyle("");
            event.consume();
        });

        //Target
        cell.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            event.setDropCompleted(true);

            NodeTreeCell source = (NodeTreeCell) event.getGestureSource();
            NodeTreeCell target = (NodeTreeCell) event.getGestureTarget();

            List<Node> files = (List<Node>) db.getContent(dataFormat);
            String targetPath = cell.getItem().getPath();

            OutputPanel outputPanel = Common.getInstance().getOutputPanel();

            if (source.isLocalTree() && target.isLocalTree()) {
                //Local to Local
                for (Node file : files) {
                    File src = new File(file.getPath());
                    File dest = new File(targetPath);

                    if (src.getParentFile().equals(dest)) {
                        outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("cp " +
                                file.getPath() + " " + cell.getItem().getPath() + " "), JFTText.failed());
                        outputPanel.println(JFTText.textBlack("Source and destination are the same"));
                        continue;
                    }

                    if (LocalFileUtils.copy(src, dest)) {
                        outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("cp " +
                                file.getPath() + " " + cell.getItem().getPath()));
                    } else {
                        outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("cp " +
                                file.getPath() + " " + cell.getItem().getPath() + " "), JFTText.failed());
                    }

                    cell.getTreeItem().getChildren().clear();
                    cell.getTreeItem().getChildren().addAll(TreeUtils.buildLocalChildren(cell.getTreeItem()));
                }
            } else if (source.isLocalTree() && !target.isLocalTree()) {
                //Local to Remote
                List<CommandCopy> commandCopies = getCommands(files, source.isLocalTree(), target.isLocalTree(), targetPath);

                for (CommandCopy commandCopy : commandCopies) {
                    CommandCopyExecution commandCopyExecution = new CommandCopyExecution(commandCopy);
                    Future<Boolean> task = executorService.submit(commandCopyExecution);

                }
            } else if (!source.isLocalTree() && !target.isLocalTree()) {
                //Remote to Remote
                List<CommandCopy> commandCopies = getCommands(files, source.isLocalTree(), target.isLocalTree(), targetPath);

                for (CommandCopy commandCopy : commandCopies) {
                    Common.getInstance().getConnection().sendCommand(commandCopy.toString());
                }
            } else if (!source.isLocalTree() && target.isLocalTree()) {
                //Remote to Local
                List<CommandCopy> commandCopies = getCommands(files, source.isLocalTree(), target.isLocalTree(), targetPath);

                for (CommandCopy commandCopy : commandCopies) {
                    CommandCopyExecution commandCopyExecution = new CommandCopyExecution(commandCopy);
                    Future<Boolean> task = executorService.submit(commandCopyExecution);
                }
            }
            //cell.getTreeView().getSelectionModel().select(cell.getTreeItem());
            event.consume();
        });

        //Source
        cell.setOnDragDone(event -> {
            event.consume();
        });
    }

    private static List<CommandCopy> getCommands(List<Node> files, boolean isSourceLocal, boolean isTargetLocal, String targetPath) {
        List<String> sources = new ArrayList<>();
        for (Node file : files) {
            sources.add(file.getPath());
        }

        CommandCopyFactory commandCopyFactory = new CommandCopyFactory(Common.getInstance().getConnection());
        return commandCopyFactory.buildCommands(isSourceLocal, isTargetLocal, targetPath, sources);
    }
}
