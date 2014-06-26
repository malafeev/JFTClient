package org.jftclient.config.dao;

import org.jftclient.config.domain.Config;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * @author sergei.malafeev
 */
public interface ConfigDao extends CrudRepository<Config, Long> {

    @Query("select c from Config c where c.id = 1")
    Config get();
}
