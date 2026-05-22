package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository
    extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {
  Optional<Role> findByName(String name);

  boolean existsByName(String name);
}
