package org.jftclient;

import org.jftclient.config.ConfigDao;
import org.jftclient.ssh.Connection;

/**
 * @author smalafeev
 */
public class Common {
    private final ConfigDao config;
    private final OutputPanel outputPanel;
    private final Connection connection;

    public Common() {
        config = new ConfigDao();
        outputPanel = new OutputPanel();
        connection = new Connection(config, outputPanel);

    }

    public ConfigDao getConfig() {
        return config;
    }

    public OutputPanel getOutputPanel() {
        return outputPanel;
    }

    public Connection getConnection() {
        return connection;
    }
}
