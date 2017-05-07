package me.raymondcai.graphql.schema;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import java.util.List;
import java.util.stream.Collectors;

public class GraphQLSchemaBuilder {
    private ObjectTypeFinder queryObjectTypeFinder;
    private ObjectTypeFinder mutationObjectTypeFinder;
    private GraphQLObjectTypeBuilder queryObjectTypeBuilder;
    private GraphQLObjectTypeBuilder mutationObjectTypeBuilder;

    public GraphQLSchemaBuilder setQueryObjectTypeFinder(ObjectTypeFinder queryObjectTypeFinder) {
        this.queryObjectTypeFinder = queryObjectTypeFinder;
        return this;
    }

    public GraphQLSchemaBuilder setMutationObjectTypeFinder(ObjectTypeFinder mutationObjectTypeFinder) {
        this.mutationObjectTypeFinder = mutationObjectTypeFinder;
        return this;
    }

    public GraphQLSchemaBuilder setQueryObjectTypeBuilder(GraphQLObjectTypeBuilder queryObjectTypeBuilder) {
        this.queryObjectTypeBuilder = queryObjectTypeBuilder;
        return this;
    }

    public GraphQLSchemaBuilder setMutationObjectTypeBuilder(GraphQLObjectTypeBuilder mutationObjectTypeBuilder) {
        this.mutationObjectTypeBuilder = mutationObjectTypeBuilder;
        return this;
    }

    public GraphQLSchema build() {
        

        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
        schemaBuilder.query(buildQuery());
        schemaBuilder.mutation(buildMutation());
        
        return schemaBuilder.build();
    }
    
    protected GraphQLObjectType buildQuery(){
        // build Query
        GraphQLObjectType.Builder queryType = GraphQLObjectType.newObject()
                .name("QueryType_JPA")
                .description("All encompassing schema for this JPA environment");
        queryType.fields(queryObjectTypeFinder.list()
                .map(javaType -> queryObjectTypeBuilder.build(javaType)).flatMap(List::stream)
                .collect(Collectors.toList()));
        
        return queryType.build();
    }
    
    protected GraphQLObjectType buildMutation(){
        // build Mutation
        GraphQLObjectType.Builder mutationType = GraphQLObjectType.newObject()
                .name("MutationType_JPA")
                .description("Mutation");
        mutationType.fields(mutationObjectTypeFinder.list().map(javaType->mutationObjectTypeBuilder.build(javaType))
                .flatMap(List::stream)
                .collect(Collectors.toList()));
        
        return mutationType.build();
    }
}
