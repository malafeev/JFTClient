package org.jftclient.tree;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author smalafeev
 */
public class Node implements Comparable<Node>, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private static final long serialVersionUID = 1L;
    private String name;
    private String path;
    private String linkDest;
    private boolean isFile;
    private boolean isLocal;


    public Node() {
    }

    public Node(File file) {
        path = file.getAbsolutePath();
        isFile = file.isFile();
        name = file.getName();
        isLocal = true;

        try {
            if (Files.isSymbolicLink(file.toPath())) {
                linkDest = Files.readSymbolicLink(file.toPath()).toString();
            }
        } catch (IOException e) {
            logger.error("Cannot read symbolic link " + file, e);
        }

    }

    public String getName() {
        if (linkDest == null) {
            return name;
        }
        return name + " -> " + linkDest;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLinkDest() {
        return linkDest;
    }

    public void setLinkDest(String linkDest) {
        this.linkDest = linkDest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Node node = (Node) o;

        if (isFile != node.isFile) {
            return false;
        }
        if (linkDest != null ? !linkDest.equals(node.linkDest) : node.linkDest != null) {
            return false;
        }
        if (name != null ? !name.equals(node.name) : node.name != null) {
            return false;
        }
        if (path != null ? !path.equals(node.path) : node.path != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (isFile ? 1 : 0);
        result = 31 * result + (linkDest != null ? linkDest.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Node o) {
        if (!this.isFile && o.isFile) {
            return -1;
        } else if (this.isFile && !o.isFile) {
            return 1;
        }
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }
}

