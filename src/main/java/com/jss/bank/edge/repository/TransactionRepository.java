package com.jss.bank.edge.repository;

import com.jss.bank.edge.domain.entity.Transaction;
import org.hibernate.reactive.mutiny.Mutiny;

public class TransactionRepository extends Repository<Transaction>{
  public TransactionRepository(Mutiny.SessionFactory sessionFactory) {
    super(sessionFactory);
  }
}
