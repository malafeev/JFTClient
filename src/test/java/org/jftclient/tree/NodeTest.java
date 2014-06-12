package org.jftclient.tree;

import java.io.File;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class NodeTest {

    public void testNewNode() {
        File file = new File("/tmp/jftclient-test.name");
        Node node = new Node(file);
        assertTrue(node.isLocal());
        assertEquals(node.getPath(), "/tmp/jftclient-test.name");
        assertEquals(node.getName(), "jftclient-test.name");
    }
}