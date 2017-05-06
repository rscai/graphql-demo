package me.raymondcai.graphql;

import graphql.execution.SimpleExecutionStrategy;
import graphql.schema.GraphQLSchema;
import graphql.servlet.SimpleGraphQLServlet;
import me.raymondcai.graphql.schema.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;

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
        return new JpaObjectTypeFinder(entityManager);
    }

    @Bean
    public GraphQLObjectTypeBuilder queryObjectTypeBuilder() {
        return new JpaQueryTypeBuilder(entityManager);
    }

    @Bean
    public GraphQLSchema graphQLSchema() {
        GraphQLSchemaBuilder schemaBuilder = new GraphQLSchemaBuilder().setQueryObjectTypeFinder
                (queryObjectTypeFinder())
                .setQueryObjectTypeBuilder(queryObjectTypeBuilder());
        return schemaBuilder.build();

    }


    @Bean
    ServletRegistrationBean graphQLServlet(GraphQLSchema graphQLSchema) {
        return new ServletRegistrationBean(new SimpleGraphQLServlet(graphQLSchema, new SimpleExecutionStrategy()),
                "/graphql/*");
    }


}
