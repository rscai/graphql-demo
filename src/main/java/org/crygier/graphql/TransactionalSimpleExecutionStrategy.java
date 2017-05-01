package org.crygier.graphql;

import graphql.ExecutionResult;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategy;
import graphql.language.Field;
import graphql.schema.GraphQLObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

public class TransactionalSimpleExecutionStrategy extends ExecutionStrategy {
    private ExecutionStrategy delegate;

    @Autowired
    private PlatformTransactionManager transactionManager;
    
    public TransactionalSimpleExecutionStrategy(final ExecutionStrategy delegate){
        this.delegate =delegate;
    }

    @Override
    public ExecutionResult execute(ExecutionContext executionContext, GraphQLObjectType parentType, Object source, Map<String, List<Field>> fields) {
       return  new TransactionTemplate(transactionManager).execute(new TransactionCallback<ExecutionResult>() {
            @Override
            public ExecutionResult doInTransaction(TransactionStatus status) {
                return delegate.execute(executionContext,parentType,source,fields);
            }
        });
        
    }
}
