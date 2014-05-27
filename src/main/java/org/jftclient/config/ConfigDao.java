package org.jftclient.config;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.io.Files;

/**
 * @author smalafeev
 */
public class ConfigDao {
    private Config config;
    private ObjectMapper mapper = new ObjectMapper();
    private File configFile;
    private static final Logger logger = LoggerFactory.getLogger(ConfigDao.class);

    public ConfigDao() {
        configFile = new File(System.getProperty("user.home") + "/.jftclient/jftclient.json");

        if (configFile.exists()) {
            try {
                config = mapper.readValue(configFile, Config.class);
            } catch (IOException e) {
                logger.error("cannot read config file", e);
            }
        } else {
            try {
                Files.createParentDirs(configFile);
            } catch (IOException e) {
                logger.error("cannot create dir: {}", configFile.getParentFile().getAbsolutePath());
            }
        }
        if (config == null) {
            config = new Config();
        }
    }

    public synchronized void save() {
        try {
            mapper.writer().withDefaultPrettyPrinter().writeValue(configFile, config);
        } catch (IOException e) {
            logger.error("cannot save config file", e);
        }
    }

    public synchronized Set<String> getHostNames() {
        return config.getHostnames();
    }

    /**
     * add host name if not added already
     *
     * @param hostname
     * @return true if hostname was added
     */
    public synchronized boolean addHostName(String hostname) {
        if (Strings.isNullOrEmpty(hostname)) {
            return false;
        }
        return config.addHostname(hostname);
    }

    /**
     * get user name
     *
     * @return user name if saved before or system user name
     */
    public synchronized String getUserName() {
        if (Strings.isNullOrEmpty(config.getUser())) {
            config.setUser(System.getProperty("user.name"));
        }
        return config.getUser();
    }

    public synchronized void setUserName(String username) {
        if (!Strings.isNullOrEmpty(username)) {
            config.setUser(username);
        }

    }

    public synchronized boolean showHiddenFiles() {
        return config.isShowHiddenFiles();
    }

    public synchronized void setShowHiddenFiles(boolean showHiddenFiles) {
        config.setShowHiddenFiles(showHiddenFiles);
    }
}

