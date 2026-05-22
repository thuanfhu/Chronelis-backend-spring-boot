package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.Permission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository
    extends JpaRepository<Permission, String>, JpaSpecificationExecutor<Permission> {
  boolean existsByName(String name);

  boolean existsByModule(String module);

  boolean existsByApiPathAndHttpMethod(String apiPath, String method);

  boolean existsByNameAndPermissionIdNot(String name, String id);

  boolean existsByApiPathAndHttpMethodAndPermissionIdNot(
      String apiPath, String httpMethod, String id);

  Optional<List<Permission>> findByModule(String module);

  Optional<Permission> findByApiPathAndHttpMethod(String apiPath, String httpMethod);

  @Query(
      "SELECT DISTINCT TRIM(p.module) FROM Permission p WHERE p.module IS NOT NULL AND TRIM(p.module) <> ''")
  List<String> findDistinctModules();
}
