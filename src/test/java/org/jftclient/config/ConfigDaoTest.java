package org.jftclient.config;

import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ConfigDaoTest {
    private ConfigDao configDao;

    @BeforeMethod
    public void setUp() {
        configDao = new ConfigDao();
    }

    @Test
    public void testSetGetUsername() {
        String initialUser = configDao.getUserName();
        assertFalse(initialUser.isEmpty());

        configDao.setUserName(null);
        assertEquals(configDao.getUserName(), initialUser);

        configDao.setUserName("");
        assertEquals(configDao.getUserName(), initialUser);


        configDao.setUserName("testUser");
        assertEquals(configDao.getUserName(), "testUser");

        configDao.setUserName(initialUser);
        assertEquals(configDao.getUserName(), initialUser);
    }

    @Test
    public void testSetGetShowHiddetFiles() {
        configDao.setShowHiddenFiles(true);
        assertEquals(configDao.showHiddenFiles(), true);

        configDao.setShowHiddenFiles(false);
        assertEquals(configDao.showHiddenFiles(), false);
    }

    @Test
    public void testGetAddHostnames() {
        Set<String> initial = configDao.getHostNames();
        assertNotNull(initial);

        assertFalse(configDao.addHostName(null));
        assertEquals(configDao.getHostNames(), initial);

        assertFalse(configDao.addHostName(""));
        assertEquals(configDao.getHostNames(), initial);

        assertTrue(configDao.addHostName("testHostname"));
        assertTrue(configDao.getHostNames().contains("testHostname"));
        assertFalse(configDao.addHostName("testHostname"));
    }

    @Test
    public void testSave() {
        String initialUser = configDao.getUserName();
        boolean initialShow = configDao.showHiddenFiles();

        configDao.setUserName("testUserSave");
        configDao.setShowHiddenFiles(!initialShow);
        configDao.save();

        configDao = new ConfigDao();
        assertEquals(configDao.getUserName(), "testUserSave");
        assertEquals(configDao.showHiddenFiles(), !initialShow);

        configDao.setUserName(initialUser);
        configDao.setShowHiddenFiles(initialShow);
        configDao.save();

        configDao = new ConfigDao();
        assertEquals(configDao.getUserName(), initialUser);
        assertEquals(configDao.showHiddenFiles(), initialShow);
    }
}
