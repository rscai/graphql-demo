package me.raymondcai.graphql;


import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

public interface JpaDataFetcherBuilder {
    JpaDataFetcher build(EntityManager entityManager, EntityType entityType);
}
