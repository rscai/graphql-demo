package me.raymondcai.graphql.schema;

import graphql.schema.GraphQLObjectType;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGraphQLObjectTypeBuilder {
    protected Map<String, GraphQLObjectType> cachedGraphQLObjectTypes = new HashMap<>();
}
