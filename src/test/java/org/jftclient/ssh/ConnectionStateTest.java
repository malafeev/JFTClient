package org.jftclient.ssh;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ConnectionStateTest {

    @Test
    public void testConnectionState() {
        ConnectionState state = new ConnectionState();
        assertTrue(state.isSuccess());
        assertNull(state.getMsg());

        state = new ConnectionState(new Exception(), null);
        assertFalse(state.isSuccess());
        assertNotNull(state.getMsg());

        state = new ConnectionState(new Exception(), "text for fails");
        assertFalse(state.isSuccess());
        assertTrue(state.getMsg().contains("text for fails"));

        state = new ConnectionState(new Exception("exception text"), "text for fails");
        assertFalse(state.isSuccess());
        assertTrue(state.getMsg().contains("exception text"));
        assertTrue(state.getMsg().contains("text for fails"));
    }
}