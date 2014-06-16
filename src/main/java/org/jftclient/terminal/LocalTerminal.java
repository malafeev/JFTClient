package org.jftclient.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.locks.ReentrantLock;

import org.jftclient.sshd.LocalSSHServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author smalafeev
 */
@Component
public class LocalTerminal {
    @Autowired
    private LocalSSHServer localSSHServer;
    private Session session;
    private Thread thread1;
    private Thread thread2;
    private TerminalPanel localTerminalPanel;

    public void connect() throws JSchException, IOException {
        if (localTerminalPanel == null) {
            throw new IllegalStateException("local terminal panel is not set");
        }
        localSSHServer.start();

        JSch jsch = new JSch();
        session = jsch.getSession("user", "localhost", localSSHServer.getPort());
        session.setPassword("");
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(5000);
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setPtyType("vt102");
        channel.setEnv("TERM_PROGRAM", ""); // for OS X

        OutputStream inputToChannel = channel.getOutputStream();
        PrintStream printStream = new PrintStream(inputToChannel, true);

        localTerminalPanel.setPrintStream(printStream);

        ReentrantLock lock = new ReentrantLock();
        Runnable run = new TerminalWatcher(channel.getExtInputStream(), lock, localTerminalPanel.getTextArea());
        thread1 = new Thread(run);
        thread1.start();

        Runnable run2 = new TerminalWatcher(channel.getInputStream(), lock, localTerminalPanel.getTextArea());
        thread2 = new Thread(run2);
        thread2.start();

        channel.connect();
    }

    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
        if (thread1 != null) {
            thread1.interrupt();
        }
        if (thread2 != null) {
            thread2.interrupt();
        }
    }

    public void setLocalTerminalPanel(TerminalPanel localTerminalPanel) {
        this.localTerminalPanel = localTerminalPanel;
    }
}
