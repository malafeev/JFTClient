package org.jftclient;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * @author smalafeev
 */
public class JFTText {
    public static final Text DONE = create("DONE", Color.BLUE);
    public static final Text FAILED = create("FAILED", Color.RED);

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

    private static Text create(String text, Color color) {
        Text out = new Text(text);
        out.setFill(color);
        return out;
    }

}
