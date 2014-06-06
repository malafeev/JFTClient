package org.jftclient.config;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author smalafeev
 */
public class Config {
    private boolean showHiddenFiles;
    private boolean savePasswords;
    private Set<Host> hosts = new TreeSet<>();

    public boolean isShowHiddenFiles() {
        return showHiddenFiles;
    }

    public void setShowHiddenFiles(boolean showHiddenFiles) {
        this.showHiddenFiles = showHiddenFiles;
    }

    public boolean isSavePasswords() {
        return savePasswords;
    }

    public void setSavePasswords(boolean savePasswords) {
        this.savePasswords = savePasswords;
    }

    public Set<Host> getHosts() {
        return hosts;
    }

    public void setHosts(Set<Host> hosts) {
        this.hosts = hosts;
    }
}
