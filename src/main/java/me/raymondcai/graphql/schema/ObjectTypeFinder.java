package me.raymondcai.graphql.schema;

import java.util.stream.Stream;

public interface ObjectTypeFinder {
    Stream<Class> list();
}
