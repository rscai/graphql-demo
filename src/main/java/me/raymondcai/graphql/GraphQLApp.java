package me.raymondcai.graphql;

import graphql.execution.SimpleExecutionStrategy;
import graphql.schema.GraphQLSchema;
import graphql.servlet.SimpleGraphQLServlet;
import me.raymondcai.graphql.model.*;
import me.raymondcai.graphql.schema.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.stream.Stream;

@SpringBootApplication
@EntityScan
public class GraphQLApp {

    public static void main(String[] args) {
        SpringApplication.run(GraphQLApp.class, args);
    }


    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public ObjectTypeFinder queryObjectTypeFinder() {
        return new ObjectTypeFinder() {
            @Override
            public Stream<Class<?>> list() {
                return Arrays.asList(Collection.class, Product.class).stream();
            }
        };
    }

    @Bean ObjectTypeFinder mutationObjectTypeFinder(){
        return new ObjectTypeFinder() {
            @Override
            public Stream<Class<?>> list() {
                return Arrays.asList(CollectionInput.class, ProductInput.class).stream();
            }
        };
    }
    
    @Bean GraphQLObjectTypeBuilder mutationObjectTypeBuilder(){
        return new JpaMutationTypeBuilder(entityManager).setTransactionManager(transactionManager);
    }
    @Bean
    public GraphQLObjectTypeBuilder queryObjectTypeBuilder() {
        return new JpaQueryTypeBuilder(entityManager);
    }

    @Bean
    public GraphQLSchema graphQLSchema() {
        GraphQLSchemaBuilder schemaBuilder = new GraphQLSchemaBuilder().setQueryObjectTypeFinder
                (queryObjectTypeFinder())
                .setQueryObjectTypeBuilder(queryObjectTypeBuilder())
                .setMutationObjectTypeFinder(mutationObjectTypeFinder())
                .setMutationObjectTypeBuilder(mutationObjectTypeBuilder());
        return schemaBuilder.build();

    }


    @Bean
    ServletRegistrationBean graphQLServlet(GraphQLSchema graphQLSchema) {
        return new ServletRegistrationBean(new SimpleGraphQLServlet(graphQLSchema, new SimpleExecutionStrategy()),
                "/graphql/*");
    }


}
