package me.raymondcai.graphql;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.coxautodev.graphql.tools.SchemaParser;
import graphql.Scalars;
import graphql.execution.ExecutionStrategy;
import graphql.execution.SimpleExecutionStrategy;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import graphql.servlet.SimpleGraphQLServlet;
import me.raymondcai.graphql.model.Droid;
import me.raymondcai.graphql.model.Episode;
import me.raymondcai.graphql.model.Human;
import org.crygier.graphql.CreateJpaDataFetcherBuilder;
import org.crygier.graphql.GraphQLSchemaBuilder;
import org.crygier.graphql.TransactionalSimpleExecutionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLObjectType.reference;

@SpringBootApplication
@EntityScan
public class GraphQLApp {
    
    public static void main(String[] args){
        SpringApplication.run(GraphQLApp.class,args);
    }
    @Autowired
    private List<GraphQLResolver<?>> resolvers;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public GraphQLSchema graphQLSchema() {
        final CreateJpaDataFetcherBuilder createDataFetcherBuilder = new CreateJpaDataFetcherBuilder(transactionManager);
        final GraphQLSchemaBuilder builder =new GraphQLSchemaBuilder(entityManager);
        builder.setCreateDataFetcherBuilder(createDataFetcherBuilder);
        return builder.getGraphQLSchema();
    }
    

    @Bean
    ServletRegistrationBean graphQLServlet(GraphQLSchema graphQLSchema) {
        return new ServletRegistrationBean(new SimpleGraphQLServlet(graphQLSchema, new SimpleExecutionStrategy()), "/graphql/*");
    }
    
    
}
