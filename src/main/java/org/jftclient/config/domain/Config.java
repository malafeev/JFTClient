package org.jftclient.config.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author sergei.malafeev
 */
@Entity
public class Config {
    @Id
    private long id = 1L;
    private boolean showHiddenFiles;
    private boolean savePasswords;
    @Column(name = "useSystemTray", columnDefinition = "boolean default false", nullable = false)
    private boolean systemTray = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public boolean isSystemTray() {
        return systemTray;
    }

    public void setSystemTray(boolean systemTray) {
        this.systemTray = systemTray;
    }
}
