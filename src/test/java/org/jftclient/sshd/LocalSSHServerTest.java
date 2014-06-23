package org.jftclient.sshd;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test
public class LocalSSHServerTest {
    private LocalSSHServer server;

    @BeforeMethod
    public void beforeEachMethod() {
        server = new LocalSSHServer();
    }

    @AfterMethod
    public void afterEachMethod() {
        server.stop();
    }

    public void testLocalSSHServer() {
        assertFalse(server.isRunning());
        assertEquals(server.getPort(), 0);

        server.start();
        assertTrue(server.isRunning());
        assertTrue(server.getPort() > 0);

        server.stop();
        assertFalse(server.isRunning());
    }

    public void testDoubleStart() {
        server.start();
        int port = server.getPort();

        server.start();
        assertEquals(server.getPort(), port);
    }

}