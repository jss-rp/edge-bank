package com.jss.bank.edge.repository;

import com.jss.bank.edge.domain.entity.Person;
import org.hibernate.reactive.mutiny.Mutiny;

public class PersonRepository extends Repository<Person> {

  public PersonRepository(Mutiny.SessionFactory sessionFactory) {
    super(sessionFactory);
  }
}
