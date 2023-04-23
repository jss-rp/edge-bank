package com.jss.bank.edge.repository;

import com.jss.bank.edge.domain.entity.Document;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

public class DocumentRepository extends Repository<Document> {

  public DocumentRepository(final Mutiny.SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  @Override
  public Uni<Document> find(final Document document) {
    return sessionFactory
        .withSession(session -> session.createQuery("from documents where document = :document", Document.class)
            .setParameter("document", document.getDocument())
            .getSingleResultOrNull()
        );
  }
}
