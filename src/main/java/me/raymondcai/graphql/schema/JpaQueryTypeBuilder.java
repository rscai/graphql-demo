package me.raymondcai.graphql.schema;

import graphql.Scalars;
import graphql.schema.*;
import me.raymondcai.graphql.ExtendedJpaDataFetcher;
import me.raymondcai.graphql.IdentityCoercing;
import me.raymondcai.graphql.JavaScalars;
import me.raymondcai.graphql.JpaDataFetcher;
import org.apache.commons.lang3.StringUtils;
import me.raymondcai.graphql.annotation.SchemaDocumentation;
import me.raymondcai.graphql.annotation.GraphQLIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.*;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JpaQueryTypeBuilder implements GraphQLObjectTypeBuilder {
    private static final Logger log = LoggerFactory.getLogger(JpaQueryTypeBuilder.class);
    public static final String PAGINATION_REQUEST_PARAM_NAME = "paginationRequest";

    protected EntityManager entityManager;

    public JpaQueryTypeBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<GraphQLFieldDefinition> build(Class<?> javaType) {
        final EntityType<?> entityType = entityManager.getMetamodel().entity(javaType);
        
        return Arrays.asList(GraphQLFieldDefinition.newFieldDefinition()
                .name(start2Lowercase(entityType.getName()))
                .description(getSchemaDocumentation(entityType.getJavaType()))
                .type(new GraphQLList(getObjectType(entityType)))
                .dataFetcher(new JpaDataFetcher(entityManager, entityType))
                .argument(entityType.getAttributes()
                        .stream()
                        .filter(this::isValidInput)
                        .filter(this::isNotIgnored)
                        .map(this::getArgument)
                        .collect(Collectors.toList()))
                .build()
        );
    }

    protected GraphQLFieldDefinition getQueryFieldPageableDefinition(EntityType<?> entityType) {
        GraphQLObjectType pageType = GraphQLObjectType.newObject()
                .name(start2Lowercase(entityType.getName()) + "Connection")
                .description("'Connection' response wrapper object for " + entityType.getName() + ".  When pagination" +
                        " or aggregation is requested, this object will be returned with metadata about the query.")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("totalPages")
                        .description("Total number of pages calculated on the database for this pageSize.")
                        .type(Scalars.GraphQLLong)
                        .build())
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("totalElements")
                        .description("Total number of results on the database for this query.")
                        .type(Scalars.GraphQLLong)
                        .build())
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("content")
                        .description("The actual object results")
                        .type(new GraphQLList(getObjectType(entityType)))
                        .build())
                .build();

        return GraphQLFieldDefinition.newFieldDefinition()
                .name(start2Lowercase(entityType.getName()) + "Connection")
                .description("'Connection' request wrapper object for " + entityType.getName() + ".  Use this object " +
                        "in a query to request things like pagination or aggregation in an argument.  Use the " +
                        "'content' field to request actual fields ")
                .type(pageType)
                .dataFetcher(new ExtendedJpaDataFetcher(entityManager, entityType))
                .argument(paginationArgument)
                .build();
    }

    protected GraphQLArgument getArgument(Attribute attribute) {
        GraphQLType type = getAttributeType(attribute);

        if (type instanceof GraphQLInputType) {
            return GraphQLArgument.newArgument()
                    .name(attribute.getName())
                    .type((GraphQLInputType) type)
                    .build();
        }

        throw new IllegalArgumentException("Attribute " + attribute + " cannot be mapped as an Input Argument");
    }
    
    protected GraphQLObjectType getObjectType(EntityType<?> entityType){
        return getObjectType(entityType,entityType.getName());
    }

    protected GraphQLObjectType getObjectType(EntityType<?> entityType, final String name) {
        return GraphQLObjectType.newObject()
                .name(name)
                .description(getSchemaDocumentation(entityType.getJavaType()))
                .fields(entityType.getAttributes()
                        .stream()
                        .filter(this::isNotIgnored)
                        .map(this::getObjectField)
                        .collect(Collectors.toList()))
                .build();
    }

    protected GraphQLFieldDefinition getObjectField(Attribute attribute) {
        GraphQLType type = getAttributeType(attribute);

        if (type instanceof GraphQLOutputType) {
            List<GraphQLArgument> arguments = new ArrayList<>();
            arguments.add(GraphQLArgument.newArgument()
                    .name("orderBy")
                    .type(orderByDirectionEnum)
                    .build());            // Always add the orderBy argument

            // Get the fields that can be queried on (i.e. Simple Types, no Sub-Objects)
            if (attribute instanceof SingularAttribute && attribute.getPersistentAttributeType() != Attribute
                    .PersistentAttributeType.BASIC) {
                EntityType foreignType = (EntityType) ((SingularAttribute) attribute).getType();
                Stream<Attribute> attributes = findBasicAttributes(foreignType.getAttributes());

                attributes.forEach(it -> {
                    arguments.add(GraphQLArgument.newArgument()
                            .name(it.getName())
                            .type((GraphQLInputType) getAttributeType(it))
                            .build());
                });
            }

            return GraphQLFieldDefinition.newFieldDefinition()
                    .name(attribute.getName())
                    .description(getSchemaDocumentation(attribute.getJavaMember()))
                    .type((GraphQLOutputType) type)
                    .argument(arguments)
                    .build();
        }

        throw new IllegalArgumentException("Attribute " + attribute + " cannot be mapped as an Output Argument");
    }

    protected Stream<Attribute> findBasicAttributes(Collection<Attribute> attributes) {
        return attributes.stream()
                .filter(it -> it.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC);
    }

    protected GraphQLType getAttributeType(Attribute attribute) {
        if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
            if (String.class.isAssignableFrom(attribute.getJavaType()))
                return Scalars.GraphQLString;
            else if (UUID.class.isAssignableFrom(attribute.getJavaType()))
                return JavaScalars.GraphQLUUID;
            else if (Integer.class.isAssignableFrom(attribute.getJavaType()) || int.class.isAssignableFrom(attribute
                    .getJavaType()))
                return Scalars.GraphQLInt;
            else if (Short.class.isAssignableFrom(attribute.getJavaType()) || short.class.isAssignableFrom(attribute
                    .getJavaType()))
                return Scalars.GraphQLShort;
            else if (Float.class.isAssignableFrom(attribute.getJavaType()) || float.class.isAssignableFrom(attribute
                    .getJavaType())
                    || Double.class.isAssignableFrom(attribute.getJavaType()) || double.class.isAssignableFrom
                    (attribute.getJavaType()))
                return Scalars.GraphQLFloat;
            else if (Long.class.isAssignableFrom(attribute.getJavaType()) || long.class.isAssignableFrom(attribute
                    .getJavaType()))
                return Scalars.GraphQLLong;
            else if (Boolean.class.isAssignableFrom(attribute.getJavaType()) || boolean.class.isAssignableFrom
                    (attribute.getJavaType()))
                return Scalars.GraphQLBoolean;
            else if (Date.class.isAssignableFrom(attribute.getJavaType()))
                return JavaScalars.GraphQLDate;
            else if (LocalDateTime.class.isAssignableFrom(attribute.getJavaType()))
                return JavaScalars.GraphQLLocalDateTime;
            else if (LocalDate.class.isAssignableFrom(attribute.getJavaType()))
                return JavaScalars.GraphQLLocalDate;
            else if (attribute.getJavaType()
                    .isEnum()) {
                return getTypeFromJavaType(attribute.getJavaType());
            } else if (BigDecimal.class.isAssignableFrom(attribute.getJavaType())) {
                return Scalars.GraphQLBigDecimal;
            }
        } else if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY ||
                attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY) {
            EntityType foreignType = (EntityType) ((PluralAttribute) attribute).getElementType();
            return new GraphQLList(new GraphQLTypeReference(foreignType.getName()));
        } else if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE ||
                attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE) {
            EntityType foreignType = (EntityType) ((SingularAttribute) attribute).getType();
            return new GraphQLTypeReference(foreignType.getName());
        } else if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
            Type foreignType = ((PluralAttribute) attribute).getElementType();
            return new GraphQLList(getTypeFromJavaType(foreignType.getJavaType()));
        }

        final String declaringType = attribute.getDeclaringType()
                .getJavaType()
                .getName(); // fully qualified name of the entity class
        final String declaringMember = attribute.getJavaMember()
                .getName(); // field name in the entity class

        throw new UnsupportedOperationException(
                "Attribute could not be mapped to GraphQL: field '" + declaringMember + "' of entity class '" +
                        declaringType + "'");
    }

    protected boolean isValidInput(Attribute attribute) {
        return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC ||
                attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION;
    }

    protected String getSchemaDocumentation(Member member) {
        if (member instanceof AnnotatedElement) {
            return getSchemaDocumentation((AnnotatedElement) member);
        }

        return null;
    }

    protected String getSchemaDocumentation(AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            SchemaDocumentation schemaDocumentation = annotatedElement.getAnnotation(SchemaDocumentation.class);
            return schemaDocumentation != null ? schemaDocumentation.value() : null;
        }

        return null;
    }

    protected boolean isNotIgnored(Attribute attribute) {
        return isNotIgnored(attribute.getJavaMember()) && isNotIgnored(attribute.getJavaType());
    }

    protected boolean isNotIgnored(EntityType entityType) {
        return isNotIgnored(entityType.getJavaType());
    }

    protected boolean isNotIgnored(Member member) {
        return member instanceof AnnotatedElement && isNotIgnored((AnnotatedElement) member);
    }

    protected boolean isNotIgnored(AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            GraphQLIgnore schemaDocumentation = annotatedElement.getAnnotation(GraphQLIgnore.class);
            return schemaDocumentation == null;
        }

        return false;
    }

    protected GraphQLType getTypeFromJavaType(Class clazz) {
        if (clazz.isEnum()) {
            GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum()
                    .name(clazz.getSimpleName());
            int ordinal = 0;
            for (Enum enumValue : ((Class<Enum>) clazz).getEnumConstants())
                enumBuilder.value(enumValue.name(), ordinal++);

            GraphQLType answer = enumBuilder.build();
            setIdentityCoercing(answer);

            return answer;
        }

        return null;
    }

    /**
     * A bit of a hack, since JPA will deserialize our Enum's for us...we don't want GraphQL doing it.
     *
     * @param type
     */
    protected void setIdentityCoercing(GraphQLType type) {
        try {
            Field coercing = type.getClass()
                    .getDeclaredField("coercing");
            coercing.setAccessible(true);
            coercing.set(type, new IdentityCoercing());
        } catch (Exception e) {
            log.error("Unable to set coercing for " + type, e);
        }
    }

    protected String start2Lowercase(final String word) {
        if (StringUtils.isEmpty(word)) {
            return word;
        }

        return word.substring(0, 1)
                .toLowerCase() + word.substring(1);
    }

    private static final GraphQLArgument paginationArgument =
            GraphQLArgument.newArgument()
                    .name(PAGINATION_REQUEST_PARAM_NAME)
                    .type(GraphQLInputObjectType.newInputObject()
                            .name("PaginationObject")
                            .description("Query object for Pagination Requests, specifying the requested page, and " +
                                    "that page's size.\n\nNOTE: 'page' parameter is 1-indexed, NOT 0-indexed" +
                                    ".\n\nExample: paginationRequest { page: 1, size: 20 }")
                            .field(GraphQLInputObjectField.newInputObjectField()
                                    .name("page")
                                    .description("Which page should be returned, starting with 1 (1-indexed)")
                                    .type(Scalars.GraphQLInt)
                                    .build())
                            .field(GraphQLInputObjectField.newInputObjectField()
                                    .name("size")
                                    .description("How many results should this page contain")
                                    .type(Scalars.GraphQLInt)
                                    .build())
                            .build()
                    )
                    .build();

    private static final GraphQLEnumType orderByDirectionEnum =
            GraphQLEnumType.newEnum()
                    .name("OrderByDirection")
                    .description("Describes the direction (Ascending / Descending) to sort a field.")
                    .value("ASC", 0, "Ascending")
                    .value("DESC", 1, "Descending")
                    .build();
}
