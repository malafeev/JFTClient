package org.jftclient;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * @author sergei.malafeev
 */
public class JFTText {
    private static final String DONE = " DONE";
    private static final String FAILED = " FAILED";

    private JFTText() {
    }

    public static Text getLocalHost() {
        Text text = new Text("[localhost] ");
        text.setFill(Color.BLUE);
        return text;
    }

    public static Text getRemoteHost(String host) {
        Text text = new Text("[" + host + "] ");
        text.setFill(Color.BLUE);
        return text;
    }

    public static Text textBlue(String text) {
        Text out = new Text(text);
        out.setFill(Color.BLUE);
        return out;
    }

    public static Text textBlack(String text) {
        Text out = new Text(text);
        out.setFill(Color.BLACK);
        return out;
    }

    public static Text textRed(String text) {
        Text out = new Text(text);
        out.setFill(Color.RED);
        return out;
    }

    public static Text done() {
        Text out = new Text(DONE);
        out.setFill(Color.BLUE);
        return out;
    }

    public static Text failed() {
        Text out = new Text(FAILED);
        out.setFill(Color.RED);
        return out;
    }
}
