package me.raymondcai.graphql.schema;

import graphql.schema.DataFetcher;

public interface DataFetcherBuilder {
    DataFetcher<?> buildRead(Class<?> javaType);
    DataFetcher<?> buildCreate(Class<?> javaType);
    DataFetcher<?> buildUpdate(Class<?> javaType);
    DataFetcher<?> buildDelete(Class<?> javaType);
}
