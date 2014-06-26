package org.jftclient.config.dao;

import java.util.List;

import org.jftclient.config.domain.Host;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author sergei.malafeev
 */
public interface HostDao extends CrudRepository<Host, Long> {

    @Query("select h from Host h where h.hostname = :name")
    Host getHostByName(@Param("name") String name);

    @Query("select h from Host h")
    List<Host> getAll();

    @Query("select h.hostname from Host h")
    List<String> getHostNames();
}
