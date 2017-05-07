package me.raymondcai.graphql.schema;

import graphql.schema.DataFetcher;
import me.raymondcai.graphql.CreateJpaDataFetcher;
import me.raymondcai.graphql.JpaDataFetcher;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

public class JPADataFetcherBuilder implements DataFetcherBuilder {
    private EntityManager entityManager;
    private PlatformTransactionManager transactionManager;

    public JPADataFetcherBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public JPADataFetcherBuilder setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        return this;
    }

    @Override
    public DataFetcher<?> buildRead(Class<?> javaType) {
        return new JpaDataFetcher(entityManager, entityManager.getMetamodel()
                .entity(javaType));
    }

    @Override
    public DataFetcher<?> buildCreate(Class<?> javaType) {
        return new CreateJpaDataFetcher(transactionManager,entityManager,entityManager.getMetamodel().entity(javaType));
    }

    @Override
    public DataFetcher<?> buildUpdate(Class<?> javaType) {
        return null;
    }

    @Override
    public DataFetcher<?> buildDelete(Class<?> javaType) {
        return null;
    }
}
