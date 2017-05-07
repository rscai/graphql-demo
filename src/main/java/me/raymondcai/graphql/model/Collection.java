package me.raymondcai.graphql.model;

import graphql.annotations.GraphQLField;

import javax.persistence.*;
import java.util.List;

@Entity
public class Collection {
    @Id
    @GraphQLField
    private Long id;
    
    @Column
    @GraphQLField
    private String name;
    
    @OneToMany(fetch = FetchType.EAGER)
    private List<Product> products;

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

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
