package me.raymondcai.graphql.schema;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import java.util.List;

public interface GraphQLObjectTypeBuilder {
    List<GraphQLFieldDefinition> build(Class<?> javaType);
}
