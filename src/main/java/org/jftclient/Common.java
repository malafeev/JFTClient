package org.jftclient;

import org.jftclient.config.ConfigDao;
import org.jftclient.ssh.Connection;

/**
 * Singleton
 *
 * @author smalafeev
 */
public class Common {
    private static final Common instance = new Common();
    private final ConfigDao config;
    private final OutputPanel outputPanel;
    private final Connection connection;

    private Common() {
        config = new ConfigDao();
        outputPanel = new OutputPanel();
        connection = new Connection();
    }

    public static Common getInstance() {
        return instance;
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
