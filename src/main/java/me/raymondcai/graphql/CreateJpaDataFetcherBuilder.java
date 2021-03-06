package me.raymondcai.graphql;

import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

public class CreateJpaDataFetcherBuilder implements JpaDataFetcherBuilder {
    private PlatformTransactionManager transactionManager;
    
    public CreateJpaDataFetcherBuilder(PlatformTransactionManager transactionManager){
        this.transactionManager=transactionManager;
    }

    @Override
    public JpaDataFetcher build(EntityManager entityManager, EntityType entityType) {
        return new CreateJpaDataFetcher(transactionManager,entityManager,entityType);
    }
}
