package org.jftclient.tree;

import org.jftclient.config.dao.ConfigDao;
import org.jftclient.config.domain.Config;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javafx.scene.control.TreeItem;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test
public class LocalTreeTest {
    @Mock(name = "configDao")
    private ConfigDao configDao;
    @InjectMocks
    private LocalTree localTree;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(configDao.get()).thenReturn(new Config());
    }

    public void testIsLocal() {
        assertTrue(localTree.isLocal());
    }

    public void testCreateLocalTree() {
        TreeItem<Node> root = localTree.createRootNode();

        assertEquals(root.getValue().getPath(), "/");
        assertEquals(root.getValue().getName(), "/");
        assertFalse(root.getValue().isFile());
        assertTrue(root.getValue().isLocal());
        assertFalse(root.isLeaf());
        assertFalse(root.getChildren().isEmpty());
    }
}