package org.jftclient.terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;

/**
 * @author sergei.malafeev
 */
public class TermOutputWatcher implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TermOutputWatcher.class);
    private InputStream outFromChannel;
    private ReentrantLock lock;
    private TerminalPanel terminalPanel;

    public TermOutputWatcher(InputStream outFromChannel, ReentrantLock lock, TerminalPanel terminalPanel) {
        this.outFromChannel = outFromChannel;
        this.lock = lock;
        this.terminalPanel = terminalPanel;
    }

    public void run() {
        InputStreamReader isr = new InputStreamReader(outFromChannel);
        BufferedReader br = new BufferedReader(isr);
        try {
            char[] buff = new char[1024];
            int read;
            while ((read = br.read(buff)) != -1) {

                String s = new String(buff, 0, read);
                if (s.contains(" \r")) {
                    s = s.replaceAll("\\s\r", "");
                }

                if (lock != null) {
                    lock.lock();
                }

                boolean backspace = false;
                if (s.contains("\b")) {
                    backspace = true;
                }

                String res = removeEscapes(s);

                if (!res.isEmpty()) {
                    Platform.runLater(() -> {
                        //logger.info("watcher: '{}'", res);
                        terminalPanel.getPanel().appendText(res);
                    });
                } else if (backspace) {
                    Platform.runLater(() -> {
                        terminalPanel.getPanel().deletePreviousChar();
                    });
                }

                if (lock != null) {
                    lock.unlock();
                }
            }
        } catch (InterruptedIOException e) {
            //ignore
        } catch (IOException e) {
            logger.warn("failed to read from ssh", e);
        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String removeEscapes(String source) {
        String target = source.replaceAll("\u001B[\\(\\)][AB012]", "");

        target = target.replaceAll("\u001B\\[\\?*\\d*;*\\d*[a-zA-Z]", "");

        target = target.replaceAll("\u001B[><=A-Z]", "");

        target = target.replaceAll("\\[\\d*;*\\d*m", "");

        target = target.replaceAll("\u000F", "");

        target = target.replaceAll("\u0007", "");

        target = target.replaceAll("\b", "");

        return target;
    }
}

