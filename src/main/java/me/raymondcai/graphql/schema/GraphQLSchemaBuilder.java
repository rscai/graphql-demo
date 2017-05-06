package me.raymondcai.graphql.schema;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

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
        // build Query
        GraphQLObjectType.Builder queryType = GraphQLObjectType.newObject()
                .name("QueryType_JPA")
                .description("All encompassing schema for this JPA environment");
        queryType.fields(queryObjectTypeFinder.list()
                .map(javaType -> queryObjectTypeBuilder.build(javaType))
                .collect(Collectors.toList()));

        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
        schemaBuilder.query(queryType.build());
        
        return schemaBuilder.build();
    }
}
