package com.jss.bank.edge.security.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

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

  @ManyToOne
  @JoinTable(
      name="users_roles",
      joinColumns = {@JoinColumn(name = "username")},
      inverseJoinColumns = {@JoinColumn(name = "role")}
  )
  private Role authorization;
}
