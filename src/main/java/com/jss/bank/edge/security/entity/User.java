package com.jss.bank.edge.security.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
public class User implements Serializable {

  @Id
  private String username;

  @Column
  private String password;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name="users_roles",
      joinColumns = {@JoinColumn(name = "username")},
      inverseJoinColumns = {@JoinColumn(name = "role")}
  )
  private Set<Role> authorization;
}
