package org.jftclient.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.io.Files;

/**
 * @author smalafeev
 */
public class ConfigDao {
    private static final Logger logger = LoggerFactory.getLogger(ConfigDao.class);
    private Config config;
    private ObjectMapper mapper = new ObjectMapper();
    private File configFile;

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

    public synchronized Host findHostByName(String hostname) {
        for (Host host : config.getHosts()) {
            if (host.getHostname().equals(hostname)) {
                return host;
            }
        }
        return null;
    }

    public synchronized List<String> getHostNames() {
        List<String> hostnames = new ArrayList<>();
        for (Host host : config.getHosts()) {
            hostnames.add(host.getHostname());
        }
        return hostnames;
    }

    public synchronized void addHost(String username, String hostname, String password) {
        if (Strings.isNullOrEmpty(hostname)) {
            return;
        }
        Host host = findHostByName(hostname);
        if (host == null) {
            host = new Host();
            host.setHostname(hostname);
        }
        host.setUsername(username);

        host.setPassword(password);
        config.getHosts().add(host);
    }

    public synchronized boolean showHiddenFiles() {
        return config.isShowHiddenFiles();
    }

    public synchronized void setShowHiddenFiles(boolean showHiddenFiles) {
        config.setShowHiddenFiles(showHiddenFiles);
    }

    public synchronized boolean isSavePasswords() {
        return config.isSavePasswords();
    }

    public synchronized void setSavePasswords(boolean savePasswords) {
        config.setSavePasswords(savePasswords);
    }

    public synchronized Config getConfig() {
        return config;
    }

    public synchronized void setConfig(Config config) {
        this.config = config;
    }
}

