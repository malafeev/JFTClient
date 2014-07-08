package org.jftclient.terminal;

import java.io.IOException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javafx.embed.swing.JFXPanel;

@Test
public class LocalTerminalTest {
    private LocalTerminal terminal;

    @BeforeClass
    public void beforeAllTests() {
        //required to init JavaFX
        JFXPanel fxPanel = new JFXPanel();
    }

    @BeforeMethod
    public void beforeEachTest() {
        terminal = new LocalTerminal();
    }

    @AfterMethod
    public void afterEachTest() {
        terminal.disconnect();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testIllegalState() throws IOException {
        terminal.connect();
    }

    public void testConnect() throws IOException {
        terminal.setLocalTerminalPanel(new TerminalPanel());
        terminal.connect();
    }

    public void testDisconnect() throws IOException {
        terminal.disconnect();
    }

}