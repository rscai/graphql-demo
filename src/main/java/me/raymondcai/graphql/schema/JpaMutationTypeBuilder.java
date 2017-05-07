package me.raymondcai.graphql.schema;

import graphql.Scalars;
import graphql.schema.*;
import me.raymondcai.graphql.CreateJpaDataFetcher;
import me.raymondcai.graphql.UpdateJpaDataFetcher;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JpaMutationTypeBuilder extends JpaQueryTypeBuilder {
    private PlatformTransactionManager transactionManager;
    public JpaMutationTypeBuilder(EntityManager entityManager) {
        super(entityManager);
    }

    public JpaMutationTypeBuilder setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        return this;
    }

    @Override
    public List<GraphQLFieldDefinition> build(Class<?> javaType) {
        
        
        return Arrays.asList(buildCreate(javaType),buildUpdate(javaType),buildDelete(javaType));

        


    }
    protected GraphQLFieldDefinition buildCreate(Class<?> javaType){
        final EntityType<?> entityType = entityManager.getMetamodel()
                .entity(javaType);
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(entityType.getName() + "Create")
                .type(
                        GraphQLObjectType.newObject()
                                .name(entityType.getName() + "Create")
                                .fields(Arrays.asList(
                                        GraphQLFieldDefinition.newFieldDefinition()
                                                .name("status")
                                                .type(getStatusType())
                                                .build(),
                                        GraphQLFieldDefinition.newFieldDefinition()
                                                .name("input")
                                                .type(getObjectType(entityType,entityType.getName()+"Payload"))
                                                .build()
                                ))
                                .build())
                .dataFetcher(new CreateJpaDataFetcher(transactionManager,entityManager,entityType))
                .argument(Arrays.asList(
                        GraphQLArgument.newArgument()
                                .name("input")
                                .type(getInputObjectType(entityType))
                                .build()
                ))
                .build();
    }
    
    protected GraphQLFieldDefinition buildUpdate(Class<?> javaType){
        final EntityType<?> entityType = entityManager.getMetamodel()
                .entity(javaType);
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(entityType.getName() + "Update")
                .type(
                        GraphQLObjectType.newObject()
                                .name(entityType.getName() + "Update")
                                .fields(Arrays.asList(
                                        GraphQLFieldDefinition.newFieldDefinition()
                                                .name("status")
                                                .type(getStatusType())
                                                .build(),
                                        GraphQLFieldDefinition.newFieldDefinition()
                                                .name("input")
                                                .type(getObjectType(entityType,entityType.getName()+"Payload"))
                                                .build()
                                ))
                                .build())
                .dataFetcher(new UpdateJpaDataFetcher(transactionManager,entityManager,entityType))
                .argument(Arrays.asList(
                        GraphQLArgument.newArgument()
                                .name("input")
                                .type(getInputObjectType(entityType))
                                .build()
                ))
                .build();
    }
    protected GraphQLFieldDefinition buildDelete(Class<?> javaType){
        final EntityType<?> entityType = entityManager.getMetamodel()
                .entity(javaType);
        // find ids
        List<SingularAttribute> idAttributes = new ArrayList<>();
        if(entityType.hasSingleIdAttribute()){
            SingularAttribute idAttribute = entityType.getId(entityType.getIdType().getJavaType());
            idAttributes.add(idAttribute);
        }else{
            idAttributes.addAll(entityType.getIdClassAttributes());
        }

        return GraphQLFieldDefinition.newFieldDefinition()
                .name(entityType.getName() + "Delete")
                .type(
                        GraphQLObjectType.newObject()
                                .name(entityType.getName() + "Delete")
                                .fields(Arrays.asList(
                                        GraphQLFieldDefinition.newFieldDefinition()
                                                .name("status")
                                                .type(getStatusType())
                                                .build(),
                                        GraphQLFieldDefinition.newFieldDefinition()
                                                .name("input")
                                                .type(getObjectType(entityType,entityType.getName()+"Payload"))
                                                .build()
                                ))
                                .build())
                .dataFetcher(new CreateJpaDataFetcher(transactionManager,entityManager,entityType))
                .argument(idAttributes.stream().map(attribute -> {
                    return GraphQLArgument.newArgument().name(attribute.getName()).type(
                            (GraphQLInputType) getAttributeType(attribute)
                    ).build();
                }).collect(Collectors.toList()))
                .build();
    }
    
    protected GraphQLInputObjectType getInputObjectType(EntityType<?> entityType){
        return GraphQLInputObjectType.newInputObject()
                .name(entityType.getName())
                .description(getSchemaDocumentation(entityType.getJavaType()))
                .fields(entityType.getAttributes()
                        .stream()
                        .filter(this::isNotIgnored)
                        .map(this::getInputObjectField)
                        .collect(Collectors.toList()))
                .build();
    }
    
    protected GraphQLInputObjectField getInputObjectField(Attribute attribute){
        GraphQLType type = getAttributeType(attribute);

        return GraphQLInputObjectField.newInputObjectField()
                .name(attribute.getName())
                .description(getSchemaDocumentation(attribute.getJavaMember()))
                .type((GraphQLInputType) type)
                .build();
    }

    protected GraphQLObjectType getStatusType() {
        return GraphQLObjectType.newObject()
                .name("status")
                .fields(
                        Arrays.asList(
                                GraphQLFieldDefinition.newFieldDefinition()
                                        .name("error")
                                        .type(Scalars.GraphQLString)
                                        .build(),
                                GraphQLFieldDefinition.newFieldDefinition()
                                        .name("message")
                                        .type(Scalars.GraphQLString)
                                        .build()
                        )
                )
                .build();
    }


}
