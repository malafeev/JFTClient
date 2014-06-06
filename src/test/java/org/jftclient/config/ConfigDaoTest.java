package org.jftclient.config;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ConfigDaoTest {
    private ConfigDao configDao;
    private Config initialConfig;

    @BeforeClass
    public void beforeAllTests() {
        initialConfig = new ConfigDao().getConfig();
    }

    @AfterClass
    public void afterAllTests() {
        ConfigDao dao = new ConfigDao();
        dao.setConfig(initialConfig);
        dao.save();
    }


    @BeforeMethod
    public void beforeEachTest() {
        configDao = new ConfigDao();
    }

    @Test
    public void testSetGetShowHiddetFiles() {
        configDao.setShowHiddenFiles(true);
        assertTrue(configDao.showHiddenFiles());

        configDao.setShowHiddenFiles(false);
        assertFalse(configDao.showHiddenFiles());
    }

    @Test
    public void testSavePasswords() {
        configDao.setSavePasswords(true);
        assertTrue(configDao.isSavePasswords());

        configDao.setSavePasswords(false);
        assertFalse(configDao.isSavePasswords());
    }

    @Test
    public void testSave() {
        boolean initialShow = configDao.showHiddenFiles();
        boolean initialSavePasswords = configDao.isSavePasswords();

        configDao.setShowHiddenFiles(!initialShow);
        configDao.setSavePasswords(!initialSavePasswords);
        configDao.save();

        configDao = new ConfigDao();
        assertEquals(configDao.showHiddenFiles(), !initialShow);
        assertEquals(configDao.isSavePasswords(), !initialSavePasswords);

        configDao.setShowHiddenFiles(initialShow);
        configDao.setSavePasswords(initialSavePasswords);
        configDao.save();

        configDao = new ConfigDao();
        assertEquals(configDao.showHiddenFiles(), initialShow);
        assertEquals(configDao.isSavePasswords(), initialSavePasswords);
    }

    @Test
    public void testFindHostByName() {
        assertNull(configDao.findHostByName(""));

        String hostname = "just_a_test_host";
        assertNull(configDao.findHostByName(hostname));

        configDao.addHost("new_user", hostname, "passwd");
        Host host = configDao.findHostByName(hostname);
        assertEquals(host.getHostname(), hostname);
    }

    @Test
    public void testGetHostnames() {
        assertNotNull(configDao.getHostNames());

        String hostname = "just_a_test_host";
        assertFalse(configDao.getHostNames().contains(hostname));
        configDao.addHost("new_user", hostname, "");
        assertTrue(configDao.getHostNames().contains(hostname));
    }
}
