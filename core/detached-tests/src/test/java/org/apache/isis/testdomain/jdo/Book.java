package org.apache.isis.testdomain.jdo;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.Property;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@PersistenceCapable
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
@Discriminator(value="CompositeFood")
@ToString(callSuper = true)
public class Book extends Product {
    
    public String title() {
        return toString();
    }
    
    public static Book of(
            String name, 
            String description, 
            double price, 
            String author, 
            String isbn, 
            String publisher) {
        
        return new Book(name, description, price, author, isbn, publisher);
    }
    
    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private String author;
    
    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private String isbn;
    
    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private String publisher;

    // -- CONSTRUCTOR
    
    private Book(
            String name, 
            String description, 
            double price, 
            String author, 
            String isbn, 
            String publisher) {
        
        super(name, description, price);
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
    }
}
