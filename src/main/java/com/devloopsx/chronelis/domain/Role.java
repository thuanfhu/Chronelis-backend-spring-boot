package com.devloopsx.chronelis.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String roleId;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "permission_roles",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  @JsonIgnoreProperties(value = {"roles"})
  List<Permission> permissions;

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "roles")
  @JsonIgnore
  List<User> users;

  String name;

  @Column(columnDefinition = "TEXT")
  String description;

  @Builder.Default Boolean active = true;
}
