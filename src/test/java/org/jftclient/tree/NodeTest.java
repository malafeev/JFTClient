package org.jftclient.tree;

import java.io.File;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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

    public void testIsParentOf() {
        Node node1 = new Node(new File("/tmp/t1/t2"));
        Node node2 = new Node(new File("/tmp/t1"));

        assertTrue(node2.isParentOf(node1));
        assertFalse(node1.isParentOf(node2));
    }
}