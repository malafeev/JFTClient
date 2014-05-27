package org.jftclient.command;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CommandCopyTest {

    @Test
    public void testMethods() {
        CommandCopy commandCopy = new CommandCopy("first");
        commandCopy.addArg("second");
        commandCopy.addArg("third");
        commandCopy.addArg(null);
        commandCopy.addArg("");

        assertEquals(commandCopy.toArray(), new String[]{"first", "second", "third"});
        assertEquals(commandCopy.toString(), "first second third");

        CommandCopy commandCopy2 = new CommandCopy(commandCopy);
        assertEquals(commandCopy2.toArray(), new String[]{"first", "second", "third"});
        assertEquals(commandCopy2.toString(), "first second third");
    }
}