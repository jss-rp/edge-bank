package com.jss.bank.edge.service;

import com.jss.bank.edge.domain.entity.Transaction;
import com.jss.bank.edge.repository.TransactionRepository;
import io.smallrye.mutiny.Uni;

public class TransactionService {

  private final TransactionRepository transactionRepository;

  public TransactionService(final TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Uni<Transaction> persistTransaction(final Transaction transaction) {
    return transactionRepository.persist(transaction);
  }
}
