package me.raymondcai.graphql.model;

import javax.persistence.*;

@Entity
@Table(name = "product")
@SecondaryTable(name="collection_products",pkJoinColumns = {
        @PrimaryKeyJoinColumn(name="products_id", referencedColumnName = "id")
})
public class ProductInput {
    @Id
    private Long id;
    
    @Column
    private String name;
    
    @JoinTable(name = "collection_products", joinColumns = {
            @JoinColumn(name="products_id", referencedColumnName = "id")
    })
    @Column(table="collection_products", name = "collection_id")
    private Long collectionId;

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

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }
}
