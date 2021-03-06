package me.raymondcai.graphql;


import graphql.language.Argument;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import me.raymondcai.graphql.model.GenericMutationType;
import me.raymondcai.graphql.model.MutationStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateJpaDataFetcher extends JpaDataFetcher {
    private PlatformTransactionManager transactionManager;

    public CreateJpaDataFetcher(PlatformTransactionManager transactionManager, EntityManager entityManager, 
                                EntityType<?> entityType) {
        super(entityManager, entityType);
        this.transactionManager = transactionManager;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Object updated = new TransactionTemplate(transactionManager).execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                return getCreate(environment, environment.getFields()
                        .iterator()
                        .next());

            }
        });

        GenericMutationType result = new GenericMutationType();
        result.setInput(updated);
        result.setStatus(new MutationStatus());

        return result;

    }

    protected Object getCreate(DataFetchingEnvironment environment, Field field) {
        try {
            for (final Argument argument : field.getArguments()) {
                if (argument.getName()
                        .equals("input")) {
                    Object input = convertValue(environment, argument, argument.getValue());

                    if (!(input instanceof Map)) {
                        return null;
                    }

                    Map inputMap = (Map) input;

                    Object entity = entityType.getJavaType()
                            .newInstance();

                    BeanInfo beanInfo = Introspector.getBeanInfo(entity.getClass());
                    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                    for (PropertyDescriptor property : propertyDescriptors) {
                        Method setter = property.getWriteMethod();
                        if (setter != null) {
                            setter.invoke(entity, inputMap.get(property.getName()));
                        }
                    }
                    
                    entityManager.persist(entity);
                    return input;

                }
            }

            return null;

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    protected CriteriaUpdate setUpdate(CriteriaUpdate cu, Root root, DataFetchingEnvironment environment, Argument 
            argument) {

        return cu.set(argument.getName(), convertValue(environment, argument, argument.getValue()));
    }

    protected Object convertValueForJpa(Object value) {
        if (value instanceof BigInteger) {
            return ((BigInteger) value).longValue();
        }

        return value;
    }
}
