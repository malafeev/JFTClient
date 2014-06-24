package org.jftclient.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 * @author sergei.malafeev
 */
public class TerminalWatcher implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TerminalWatcher.class);
    private InputStream outFromChannel;
    private ReentrantLock lock;
    private TextArea textArea;

    public TerminalWatcher(InputStream outFromChannel, ReentrantLock lock, TextArea textArea) {
        this.outFromChannel = outFromChannel;
        this.lock = lock;
        this.textArea = textArea;
    }

    public void run() {
        InputStreamReader isr = new InputStreamReader(outFromChannel);
        try {
            char[] buff = new char[1024];
            int read;
            while ((read = isr.read(buff)) != -1) {
                String s = new String(buff, 0, read);

                if (lock != null) {
                    lock.lock();
                }

                boolean backspace = false;
                if (s.contains("\b\u001B[K")) {
                    backspace = true;
                }

                String res = removeEscapes(s);

                if (!res.isEmpty()) {
                    Platform.runLater(() -> {
                        textArea.appendText(res);
                    });
                } else if (backspace) {
                    Platform.runLater(() -> {
                        textArea.deletePreviousChar();
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

