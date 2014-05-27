package org.jftclient.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jftclient.ssh.Connection;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CommandCopyFactoryTest {
    private CommandCopyFactory commandCopyFactory;
    @Mock
    private Connection connection;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        commandCopyFactory = new CommandCopyFactory(connection);
        when(connection.getRemoteHost()).thenReturn("host");
        when(connection.getPassword()).thenReturn("password");
        when(connection.getUser()).thenReturn("user");
    }

    @Test
    public void testBuildCommandsWithEmptyInput() {
        List<CommandCopy> commandCopies = commandCopyFactory.buildCommands(true, true, null, Arrays.asList("src1"));
        assertTrue(commandCopies.isEmpty());

        commandCopies = commandCopyFactory.buildCommands(true, true, "", Arrays.asList("src"));
        assertTrue(commandCopies.isEmpty());

        commandCopies = commandCopyFactory.buildCommands(true, true, "dst", null);
        assertTrue(commandCopies.isEmpty());

        commandCopies = commandCopyFactory.buildCommands(true, true, "dst", new ArrayList<>());
        assertTrue(commandCopies.isEmpty());
    }

    @Test
    public void testBuildCommands() {
        List<CommandCopy> commandCopies = commandCopyFactory.buildCommands(true, true, "destination", Arrays.asList("src1"));
        assertEquals(commandCopies.size(), 1);

        commandCopies = commandCopyFactory.buildCommands(true, true, "destination", Arrays.asList("src1", "src2"));
        assertEquals(commandCopies.size(), 1);

        commandCopies = commandCopyFactory.buildCommands(false, false, "destination", Arrays.asList("src1", "src2"));
        assertEquals(commandCopies.size(), 1);

        commandCopies = commandCopyFactory.buildCommands(true, false, "destination", Arrays.asList("src1", "src2"));
        assertEquals(commandCopies.size(), 1);

        commandCopies = commandCopyFactory.buildCommands(false, true, "destination", Arrays.asList("src1", "src2", "src3"));
        assertEquals(commandCopies.size(), 3);
    }
}
