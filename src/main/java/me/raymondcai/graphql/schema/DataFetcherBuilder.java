package me.raymondcai.graphql.schema;

import graphql.schema.DataFetcher;

public interface DataFetcherBuilder {
    DataFetcher<?> build(Class<?> javaType);
}
