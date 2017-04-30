package me.raymondcai.graphql;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.coxautodev.graphql.tools.SchemaParser;
import graphql.Scalars;
import graphql.execution.SimpleExecutionStrategy;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import graphql.servlet.SimpleGraphQLServlet;
import me.raymondcai.graphql.model.Droid;
import me.raymondcai.graphql.model.Episode;
import me.raymondcai.graphql.model.Human;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;
import java.util.List;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLObjectType.reference;

@SpringBootApplication
public class GraphQLApp {
    
    public static void main(String[] args){
        SpringApplication.run(GraphQLApp.class,args);
    }
    @Autowired
    private List<GraphQLResolver<?>> resolvers;
    
    @Autowired
    private EntityManager entityManager;

    @Bean
    public GraphQLSchema graphQLSchema() {
        GraphQLObjectType simpsonCharacter = newObject()
                .name("SimpsonCharacter")
                .description("A Simpson character")
                .field(newFieldDefinition()
                        .name("name")
                        .description("The name of the character.")
                        .type(Scalars.GraphQLString).staticValue("test name"))
                .field(newFieldDefinition()
                        .name("mainCharacter")
                        .description("One of the main Simpson characters?")
                        .type(Scalars.GraphQLBoolean).staticValue("test character"))
                .build();
        GraphQLObjectType query = newObject().name("Query").description("Query root")
                .field(newFieldDefinition().
                name("simpsonCharacter").type(new GraphQLList(simpsonCharacter))).build();
        
        return GraphQLSchema.newSchema().query(query).build();
    }

    @Bean
    ServletRegistrationBean graphQLServlet(GraphQLSchema graphQLSchema) {
        return new ServletRegistrationBean(new SimpleGraphQLServlet(graphQLSchema, new SimpleExecutionStrategy()), "/graphql/*");
    }
    
    
}
