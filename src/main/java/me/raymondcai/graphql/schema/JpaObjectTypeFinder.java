package me.raymondcai.graphql.schema;

import javax.persistence.EntityManager;
import java.util.stream.Stream;

public class JpaObjectTypeFinder implements ObjectTypeFinder {
    private EntityManager entityManager;

    public JpaObjectTypeFinder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Stream<Class<?>> list() {
        return entityManager.getMetamodel()
                .getEntities()
                .stream()
                .map(entityType -> entityType.getJavaType());
    }
}
