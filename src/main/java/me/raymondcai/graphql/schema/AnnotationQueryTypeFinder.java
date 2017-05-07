package me.raymondcai.graphql.schema;

import me.raymondcai.graphql.model.Collection;
import me.raymondcai.graphql.model.Product;

import java.util.Arrays;
import java.util.stream.Stream;

public class AnnotationQueryTypeFinder implements ObjectTypeFinder {
    @Override
    public Stream<Class<?>> list() {
        return Arrays.asList(Collection.class,Product.class).stream();
    }
}
