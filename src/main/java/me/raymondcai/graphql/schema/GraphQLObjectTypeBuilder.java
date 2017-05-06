package me.raymondcai.graphql.schema;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

public interface GraphQLObjectTypeBuilder {
    GraphQLFieldDefinition build(Class<?> javaType);
}
