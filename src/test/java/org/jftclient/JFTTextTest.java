package org.jftclient;

import org.testng.annotations.Test;

import javafx.scene.paint.Color;

import static org.testng.Assert.assertEquals;

@Test
public class JFTTextTest {

    public void testGetLocalHost() {
        assertEquals(JFTText.getLocalHost().getText(), "[localhost] ");
        assertEquals(JFTText.getLocalHost().getFill(), Color.BLUE);
    }

    public void testGetRemoteHost() {
        assertEquals(JFTText.getRemoteHost("remote_host7").getText(), "[remote_host7] ");
        assertEquals(JFTText.getRemoteHost("remote_host7").getFill(), Color.BLUE);
    }

    public void testTextBlue() {
        assertEquals(JFTText.textBlue("test text").getText(), "test text");
        assertEquals(JFTText.textBlue("test text").getFill(), Color.BLUE);
    }

    public void testTextBlack() {
        assertEquals(JFTText.textBlack("test text").getText(), "test text");
        assertEquals(JFTText.textBlack("test text").getFill(), Color.BLACK);
    }

    public void testTextRed() {
        assertEquals(JFTText.textRed("test text").getText(), "test text");
        assertEquals(JFTText.textRed("test text").getFill(), Color.RED);
    }

    public void testDone() {
        assertEquals(JFTText.done().getText(), " DONE");
        assertEquals(JFTText.done().getFill(), Color.BLUE);
    }

    public void testFailed() {
        assertEquals(JFTText.failed().getText(), " FAILED");
        assertEquals(JFTText.failed().getFill(), Color.RED);
    }
}