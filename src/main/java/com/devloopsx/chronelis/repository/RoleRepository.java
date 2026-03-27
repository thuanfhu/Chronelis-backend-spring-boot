package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {
	Optional<Role> findByName(String name);

	boolean existsByName(String name);
}
