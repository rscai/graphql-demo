package me.raymondcai.graphql.model;

import graphql.annotations.GraphQLField;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Product {
    
    @Id
    @GraphQLField
    private Long id;
    @Column
    @GraphQLField
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
