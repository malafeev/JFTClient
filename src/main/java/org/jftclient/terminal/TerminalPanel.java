package org.jftclient.terminal;

import java.io.IOException;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;

/**
 * @author smalafeev
 */
@Component
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

                        byte[] code = TermUtils.getCode(event, TerminalPanel.this);
                        if (code != null) {
                            try {
                                printStream.write(code);
                            } catch (IOException e) {
                                logger.warn("failed to write to stream", e);
                            }
                        } else {
                            printStream.print(event.getText());
                        }

                        //logger.info("panel '{}'", event.getText());
                        event.consume();
                    }
                }
        );
    }

    public TextArea getPanel() {
        return textArea;
    }
}
