package org.jftclient.terminal;

import java.io.IOException;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;

/**
 * @author smalafeev
 */
public class TerminalPanel {
    private static final Logger logger = LoggerFactory.getLogger(TerminalPanel.class);
    private TextArea textArea;


    public TerminalPanel() {
        textArea = new TextArea();
        textArea.setFont(Font.font("Monospaced", 15));
    }

    public void setPrintStream(PrintStream printStream) {

        textArea.addEventHandler(KeyEvent.KEY_RELEASED,
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        event.consume();
                    }
                });

        textArea.addEventHandler(KeyEvent.KEY_TYPED,
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        event.consume();
                    }
                }
        );

        textArea.addEventHandler(KeyEvent.KEY_PRESSED,
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {

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

                        if (event.getCode() == KeyCode.LEFT) {
                            return;
                        } else if (event.getCode() == KeyCode.RIGHT) {
                            return;
                        }
                        event.consume();
                    }
                }
        );
    }

    public TextArea getTextArea() {
        return textArea;
    }
}
