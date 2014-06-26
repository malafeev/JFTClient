package org.jftclient.tree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.jftclient.JFTText;
import org.jftclient.LocalFileUtils;
import org.jftclient.OutputPanel;
import org.jftclient.command.CommandCopy;
import org.jftclient.command.CommandCopyExecution;
import org.jftclient.command.CommandCopyFactory;
import org.jftclient.ssh.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * @author sergei.malafeev
 */
@Component
public class CommonTree {
    private static final Logger logger = LoggerFactory.getLogger(CommonTree.class);
    private static DataFormat dataFormat = new DataFormat("tree");
    @Autowired
    private Connection connection;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private OutputPanel outputPanel = OutputPanel.getInstance();

    public void refresh(TreeView<Node> treeView, Tree tree) {
        List<TreeItem<Node>> items = new ArrayList<>(treeView.getSelectionModel().getSelectedItems());
        if (items.isEmpty() || items.size() > 1) {
            return;
        }

        treeView.getSelectionModel().clearSelection();
        items.get(0).getChildren().setAll(tree.buildChildren(items.get(0)));
        treeView.getSelectionModel().select(items.get(0));
    }

    public void deleteSelectedItems(TreeView<Node> treeView, Tree tree) {
        List<TreeItem<Node>> items = new ArrayList<>(treeView.getSelectionModel().getSelectedItems());
        if (items.isEmpty()) {
            return;
        }

        treeView.getSelectionModel().clearSelection();

        TreeItem<Node> parent = null;

        Map<String, TreeItem<Node>> reducedItems = reduceByPaths(items);

        for (Map.Entry<String, TreeItem<Node>> item : reducedItems.entrySet()) {
            if (item.getValue().getParent() != null) {
                if (parent == null) {
                    parent = item.getValue().getParent();
                } else {
                    if (item.getValue().getParent().getValue().isParentOf(parent.getValue())) {
                        parent = item.getValue().getParent();
                    }
                }
            }
            if (tree.isLocal()) {
                File src = new File(item.getKey());
                if (src.isFile()) {
                    if (!src.delete()) {
                        logger.warn("cannot delete file {}", src.getAbsolutePath());
                        outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("rm " +
                                src.getAbsolutePath() + " "), JFTText.failed());

                    } else {
                        outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("rm " +
                                src.getAbsolutePath()));
                    }
                } else {
                    try {
                        FileUtils.deleteDirectory(src);
                        outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("rm -rf " +
                                src.getAbsolutePath()));
                    } catch (IOException e) {
                        logger.warn("failed to remove dir", e);
                        outputPanel.println(JFTText.getLocalHost(), JFTText.textBlack("rm -rf " +
                                src.getAbsolutePath() + " "), JFTText.failed());
                    }
                }
            } else {
                connection.rm(item.getKey());
            }
        }
        if (parent != null) {
            parent.getChildren().setAll(tree.buildChildren(parent));
        }
    }

    public void setDragDropEvent(NodeTreeCell cell, LocalTree localTree) {
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
            NodeTreeCell source = (NodeTreeCell) event.getGestureSource();

            //check that if target is file then source is not directory:
            boolean validNodes = !(cell.getItem().isFile() && !source.getItem().isFile());

            if (event.getGestureSource() != cell && validNodes) {
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

            @SuppressWarnings({"unchecked"})
            List<Node> files = (List<Node>) db.getContent(dataFormat);

            String targetPath = cell.getItem().getPath();

            OutputPanel outputPanel = OutputPanel.getInstance();

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
                    cell.getTreeItem().getChildren().addAll(localTree.buildChildren(cell.getTreeItem()));
                }
            } else if (source.isLocalTree() && !target.isLocalTree()) {
                //Local to Remote
                List<CommandCopy> commandCopies = getCommands(files, source.isLocalTree(), target.isLocalTree(), targetPath);

                for (CommandCopy commandCopy : commandCopies) {
                    CommandCopyExecution commandCopyExecution = new CommandCopyExecution(commandCopy, connection);
                    Future<Boolean> task = executorService.submit(commandCopyExecution);

                }
            } else if (!source.isLocalTree() && !target.isLocalTree()) {
                //Remote to Remote
                List<CommandCopy> commandCopies = getCommands(files, source.isLocalTree(), target.isLocalTree(), targetPath);

                for (CommandCopy commandCopy : commandCopies) {
                    connection.sendCommand(commandCopy.toString());
                }
            } else if (!source.isLocalTree() && target.isLocalTree()) {
                //Remote to Local
                List<CommandCopy> commandCopies = getCommands(files, source.isLocalTree(), target.isLocalTree(), targetPath);

                for (CommandCopy commandCopy : commandCopies) {
                    CommandCopyExecution commandCopyExecution = new CommandCopyExecution(commandCopy, connection);
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

    private List<CommandCopy> getCommands(List<Node> files, boolean isSourceLocal, boolean isTargetLocal, String targetPath) {
        List<String> sources = new ArrayList<>();
        for (Node file : files) {
            sources.add(file.getPath());
        }

        CommandCopyFactory commandCopyFactory = new CommandCopyFactory(connection);
        return commandCopyFactory.buildCommands(isSourceLocal, isTargetLocal, targetPath, sources);
    }

    private Map<String, TreeItem<Node>> reduceByPaths(List<TreeItem<Node>> items) {
        Map<String, TreeItem<Node>> map = new TreeMap<>();
        for (TreeItem<Node> item : items) {
            map.put(item.getValue().getPath(), item);
        }
        Map<String, TreeItem<Node>> found = new TreeMap<>();
        for (Map.Entry<String, TreeItem<Node>> path : map.entrySet()) {
            if (found.isEmpty()) {
                found.put(path.getKey(), path.getValue());
            } else {
                boolean add = true;
                for (String foundPath : found.keySet()) {
                    if (path.getKey().startsWith(foundPath)) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    found.put(path.getKey(), path.getValue());
                }
            }
        }

        return found;
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdownNow();
    }
}
