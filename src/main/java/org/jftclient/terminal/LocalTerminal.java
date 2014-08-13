package org.jftclient.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.pty4j.PtyProcess;

/**
 * @author sergei.malafeev
 */
@Component
public class LocalTerminal {
    private Thread thread;
    private TerminalPanel localTerminalPanel;
    private PtyProcess pty;

    public void connect() throws IOException {
        if (localTerminalPanel == null) {
            throw new IllegalStateException("local terminal panel is not set");
        }
        if (pty != null && pty.isAlive()) {
            return;
        }

        String[] cmd = {"/bin/bash", "-i"};
        Map<String, String> envs = new HashMap<>(System.getenv());
        envs.remove("TERM_PROGRAM"); // for OS X
        envs.put("TERM", "vt102");

        pty = PtyProcess.exec(cmd, envs, System.getProperty("user.home"));

        OutputStream os = pty.getOutputStream();
        InputStream is = pty.getInputStream();

        PrintStream printStream = new PrintStream(os, true);
        localTerminalPanel.setPrintStream(printStream);

        Runnable run = new TerminalWatcher(is, localTerminalPanel.getTextArea());
        thread = new Thread(run);
        thread.start();
    }

    @PreDestroy
    public void disconnect() {
        if (pty != null) {
            pty.destroy();
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void setLocalTerminalPanel(TerminalPanel localTerminalPanel) {
        this.localTerminalPanel = localTerminalPanel;
    }
}
