package com.jss.bank.edge.repository;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

public abstract class Repository<T> {

  protected final Mutiny.SessionFactory sessionFactory;

  public Repository(final Mutiny.SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }
  public Uni<T> persist(T entity) {
    return sessionFactory
        .withSession(session -> session.persist(entity))
        .map(__ -> entity);
  }

  public Uni<?> find(T entity) {
    return sessionFactory
        .withSession(session -> session.find(entity.getClass(), entity));
  }
}
