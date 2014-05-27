package org.jftclient;

import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * @author smalafeev
 */
public class OutputPanel {
    private ScrollPane scrollPane = new ScrollPane();
    private TextFlow textFlow = new TextFlow();

    public OutputPanel() {
        scrollPane.setContent(textFlow);
    }

    public void printRed(String text) {
        print(JFTText.textRed(text));
    }

    public void printBlack(String text) {
        print(JFTText.textBlack(text));
    }


    public void printBlue(String text) {
        print(JFTText.textBlue(text));
    }


    private void print(Text... text) {
        textFlow.getChildren().addAll(text);
        scrollPane.setVvalue(1.0);
    }

    public void println(Text... text) {
        textFlow.getChildren().addAll(text);
        textFlow.getChildren().add(new Text("\n"));
        scrollPane.setVvalue(1.0);
    }

    public void printlnOutputLater(List<Text> out) {
        Platform.runLater(() -> {
            println(out.toArray(new Text[out.size()]));
        });
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }
}
