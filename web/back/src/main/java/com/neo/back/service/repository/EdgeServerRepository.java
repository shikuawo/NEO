package com.neo.back.service.repository;

import com.neo.back.service.entity.EdgeServer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EdgeServerRepository extends JpaRepository<EdgeServer, String> {
    EdgeServer findByEdgeServerName(String EdgeServerName);
    List<EdgeServer> findAll();
    EdgeServer findByIp(String ip);
}
