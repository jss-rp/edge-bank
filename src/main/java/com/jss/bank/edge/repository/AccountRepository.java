package com.jss.bank.edge.repository;

import com.jss.bank.edge.domain.entity.Account;
import org.hibernate.reactive.mutiny.Mutiny;

public class AccountRepository extends Repository<Account> {
  public AccountRepository(Mutiny.SessionFactory sessionFactory) {
    super(sessionFactory);
  }
}
