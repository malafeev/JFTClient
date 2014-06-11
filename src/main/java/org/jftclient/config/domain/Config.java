package org.jftclient.config.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author smalafeev
 */
@Entity
public class Config {
    @Id
    private long id = 1;
    private boolean showHiddenFiles;
    private boolean savePasswords;

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
}
