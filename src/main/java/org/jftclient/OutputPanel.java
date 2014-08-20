package org.jftclient;

import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * @author sergei.malafeev
 */
public class OutputPanel {
    private static final OutputPanel instance = new OutputPanel();
    private ScrollPane scrollPane;
    private TextFlow textFlow;

    private OutputPanel() {
        textFlow = new TextFlow();
        scrollPane = new ScrollPane(textFlow);
    }

    public static OutputPanel getInstance() {
        return instance;
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
        textFlow.autosize(); // Required after update 20
        scrollPane.setVvalue(1.0);
    }

    public void println(Text... text) {
        textFlow.getChildren().addAll(text);
        textFlow.getChildren().add(new Text("\n"));
        textFlow.autosize(); // Required after update 20
        scrollPane.setVvalue(1.0);
    }

    public void printOutputLater(Text... text) {
        Platform.runLater(() -> print(text));
    }

    public void printlnOutputLater(Text... text) {
        Platform.runLater(() -> println(text));
    }

    public void printlnOutputLater(List<Text> out) {
        Platform.runLater(() -> println(out.toArray(new Text[out.size()])));
    }

    public void printOutputLater(List<Text> out) {
        Platform.runLater(() -> print(out.toArray(new Text[out.size()])));
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }
}
