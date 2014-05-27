package org.jftclient.config;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author smalafeev
 */
public class Config {
    private String user;
    private boolean showHiddenFiles = false;
    private Set<String> hostnames = new TreeSet<>();

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isShowHiddenFiles() {
        return showHiddenFiles;
    }

    public void setShowHiddenFiles(boolean showHiddenFiles) {
        this.showHiddenFiles = showHiddenFiles;
    }

    public boolean addHostname(String hostname) {
        return this.hostnames.add(hostname);
    }

    public Set<String> getHostnames() {
        return hostnames;
    }

    public void setHostnames(Set<String> hostnames) {
        this.hostnames = hostnames;
    }
}
