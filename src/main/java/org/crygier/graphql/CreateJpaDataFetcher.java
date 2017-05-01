package org.crygier.graphql;


import graphql.language.Argument;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreateJpaDataFetcher extends JpaDataFetcher {
    private PlatformTransactionManager transactionManager;
    public CreateJpaDataFetcher(PlatformTransactionManager transactionManager,EntityManager entityManager, EntityType<?> entityType) {
        super(entityManager,entityType);
        this.transactionManager =transactionManager;
    }
    
    @Override
    public Object get(DataFetchingEnvironment environment) {
        return new TransactionTemplate(transactionManager).execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                return getCreate(environment, environment.getFields().iterator().next()).getResultList();
                
            }
        });

        
        
    }

    protected TypedQuery getCreate(DataFetchingEnvironment environment, Field field){
        try {
            Object entity = entityType.getJavaType()
                    .newInstance();

            List<Argument> arguments = new ArrayList<>();


            arguments.addAll(field.getArguments());

            for (Argument argument : arguments) {
                java.lang.reflect.Field property = entityType.getJavaType()
                        .getDeclaredField(argument.getName());
                property.setAccessible(true);
                property.set(entity, convertValueForJpa(convertValue(environment, argument, argument.getValue())));
            }


            entityManager.persist(entity);
            return getQuery(environment, field);
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage(),ex);
        }
    }

    protected CriteriaUpdate setUpdate(CriteriaUpdate cu, Root root, DataFetchingEnvironment environment, Argument argument) {
        
            return cu.set(argument.getName(), convertValue(environment, argument, argument.getValue()));
    }
    
    protected Object convertValueForJpa(Object value){
        if(value instanceof BigInteger){
            return ((BigInteger)value).longValue();
        }
        
        return value;
    }
}
