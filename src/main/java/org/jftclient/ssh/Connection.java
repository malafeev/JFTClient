package org.jftclient.ssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PreDestroy;

import org.jftclient.JFTText;
import org.jftclient.OutputPanel;
import org.jftclient.config.dao.ConfigDao;
import org.jftclient.tree.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import javafx.scene.text.Text;


/**
 * @author smalafeev
 */
@Component
public class Connection {
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    private static final int timeout = 5000;
    private Session session;
    private ChannelSftp sftpChannel;
    private String remoteHost;
    private String user;
    private String password;
    @Autowired
    private ConfigDao configDao;

    @PreDestroy
    public void destroy() {
        disconnect();
    }

    public synchronized void processSymLink(Node node) {
        try {
            String symPath = this.sftpChannel.readlink(node.getPath());
            node.setLinkDest(symPath);
            File file = new File(symPath);
            if (file.isAbsolute()) {
                node.setFile(!this.sftpChannel.stat(symPath).isDir());
            }
            String absPath = new File(new File(node.getPath()).getParent(), symPath).getAbsolutePath();
            node.setFile(!this.sftpChannel.stat(absPath).isDir());

        } catch (SftpException e) {
            if (e.getMessage() != null && e.getMessage().contains("No such file")) {
                return;
            }
            logger.error("failed to check if dir '{}' is symlink", node.getPath(), e);
        }
    }

    public synchronized List<Node> getNodes(String path) {
        List<Node> nodes = new ArrayList<>();

        if ((session == null) || !session.isConnected()) {
            return nodes;
        }

        for (ChannelSftp.LsEntry entry : getFiles(path)) {
            Node node = new Node();
            nodes.add(node);
            node.setName(entry.getFilename());
            if (path.endsWith("/")) {
                node.setPath(path + entry.getFilename());
            } else {
                node.setPath(path + "/" + entry.getFilename());
            }

            if (entry.getAttrs().isLink()) {
                processSymLink(node);
            } else {
                node.setFile(!entry.getAttrs().isDir());
            }
        }
        Collections.sort(nodes);
        return nodes;
    }

    /**
     * remove path
     *
     * @param path absolute path
     */
    public synchronized void rm(String path) {
        sendCommand("rm -rf " + path);
    }

    public synchronized void mv(String src, String dest) {
        sendCommand("mv -f " + src + " " + dest);
    }

    /**
     * create new directory
     *
     * @param path path of new directory
     */
    public synchronized void mkdir(String path) {
        sendCommand("mkdir -p " + path);
    }

    public synchronized void sendCommand(String command) {

        List<Text> output = new ArrayList<>();
        output.add(JFTText.getRemoteHost(remoteHost));
        output.add(JFTText.textBlack(command));

        try {
            Channel channel = this.session.openChannel("exec");

            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);

            OutputStream out = new PipedOutputStream();
            channel.setOutputStream(out);

            OutputStream outErr = new PipedOutputStream();
            ((ChannelExec) channel).setErrStream(outErr);

            PipedInputStream pout = new PipedInputStream((PipedOutputStream) out);
            PipedInputStream poutErr = new PipedInputStream((PipedOutputStream) outErr);

            channel.connect(timeout);


            try (BufferedReader consoleOutput = new BufferedReader(
                    new InputStreamReader(pout, Charset.defaultCharset()))) {
                String s;
                while ((s = consoleOutput.readLine()) != null) {
                    output.add(JFTText.textBlack("\n" + s));
                }
            }

            try (BufferedReader consoleErr = new BufferedReader(
                    new InputStreamReader(poutErr, Charset.defaultCharset()))) {
                String s;
                while ((s = consoleErr.readLine()) != null) {
                    output.add(JFTText.textRed("\n" + s));
                }
            }

            channel.disconnect();
        } catch (IOException | JSchException e) {
            logger.error("failed to send command: {}", command, e);
        }

        OutputPanel.getInstance().printlnOutputLater(output);
    }

    private synchronized List<ChannelSftp.LsEntry> getFiles(String path) {
        final List<ChannelSftp.LsEntry> files = new LinkedList<>();

        if ((path == null) || path.isEmpty()) {
            return files;
        }

        final boolean showHiddenFiles = configDao.get().isShowHiddenFiles();

        try {
            sftpChannel.ls(path, new ChannelSftp.LsEntrySelector() {
                @Override
                public int select(ChannelSftp.LsEntry entry) {
                    if (!showHiddenFiles) {
                        if (entry.getFilename().startsWith(".")) {
                            return ChannelSftp.LsEntrySelector.CONTINUE;
                        }
                    } else if (entry.getFilename().equals(".")
                            || entry.getFilename().equals("..")) {
                        return ChannelSftp.LsEntrySelector.CONTINUE;
                    }
                    files.add(entry);
                    return ChannelSftp.LsEntrySelector.CONTINUE;
                }
            });
        } catch (SftpException e) {
            // ignore exception. usually happens because of permission denied
        }
        return files;
    }

    /**
     * Connect to remote host
     *
     * @param remoteHost
     * @param user
     * @param password
     * @return true if connected otherwise false
     */
    public synchronized ConnectionState connect(String remoteHost, String user, String password) {
        disconnect();

        this.remoteHost = remoteHost;
        this.user = user;
        this.password = password;

        JSch jsch = new JSch();

        try {
            session = jsch.getSession(user, remoteHost, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect(timeout);

            // create sftp channel:
            Channel channel = session.openChannel("sftp");
            channel.connect(timeout);
            sftpChannel = (ChannelSftp) channel;

        } catch (JSchException e) {
            logger.error("failed to connect to {}", remoteHost, e);
            return new ConnectionState(e, "failed to connect to " + remoteHost);
        }
        return new ConnectionState();
    }

    public synchronized boolean isConnected() {
        if (session == null || !session.isConnected()) {
            return false;
        }
        return true;
    }

    /**
     * disconnect from remote host
     */
    public synchronized void disconnect() {
        if (sftpChannel != null) {
            sftpChannel.exit();
        }

        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    public synchronized String getUser() {
        return user;
    }

    public synchronized String getPassword() {
        return password;
    }

    public synchronized String getRemoteHost() {
        return remoteHost;
    }
}
