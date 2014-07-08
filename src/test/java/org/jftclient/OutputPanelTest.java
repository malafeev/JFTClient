package org.jftclient;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javafx.embed.swing.JFXPanel;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

@Test
public class OutputPanelTest {

    @BeforeClass
    public void beforeAllTests() {
        //required to init JavaFX
        JFXPanel fxPanel = new JFXPanel();
    }

    public void testGetInstance() {
        assertSame(OutputPanel.getInstance(), OutputPanel.getInstance());
        assertNotNull(OutputPanel.getInstance().getScrollPane());
    }
}