package org.jftclient.terminal;

import java.io.IOException;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.Event;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;

/**
 * @author sergei.malafeev
 */
public class TerminalPanel {
    private static final Logger logger = LoggerFactory.getLogger(TerminalPanel.class);
    private TextArea textArea;


    public TerminalPanel() {
        textArea = new TextArea();
        textArea.setFont(Font.font("Monospaced", 14));
    }

    public void setPrintStream(PrintStream printStream) {

        textArea.addEventHandler(KeyEvent.KEY_RELEASED, Event::consume);

        textArea.addEventHandler(KeyEvent.KEY_TYPED, Event::consume);

        textArea.addEventHandler(KeyEvent.KEY_PRESSED,
                event -> {
                    byte[] code = TerminalUtils.getCode(event, TerminalPanel.this);
                    if (code != null) {
                        try {
                            printStream.write(code);
                        } catch (IOException e) {
                            logger.warn("failed to write to stream", e);
                        }
                    } else {
                        printStream.print(event.getText());
                    }

                    event.consume();
                }
        );
    }

    public TextArea getTextArea() {
        return textArea;
    }
}
