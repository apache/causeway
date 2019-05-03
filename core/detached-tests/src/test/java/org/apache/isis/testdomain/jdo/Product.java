package org.apache.isis.testdomain.jdo;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@PersistenceCapable(identityType=IdentityType.DATASTORE, schema = "testdomain")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.VALUE_MAP, value="Product")
@DatastoreIdentity(
        strategy=javax.jdo.annotations.IdGeneratorStrategy.INCREMENT,
        column="id")
@DomainObject
@AllArgsConstructor(access = AccessLevel.PROTECTED) @ToString
public class Product {

    public String title() {
        return toString();
    }
    
    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private String name;
    
    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private String description;
    
    @Property
    @Getter @Setter @Column(allowsNull = "false")
    private double price;
    
}
