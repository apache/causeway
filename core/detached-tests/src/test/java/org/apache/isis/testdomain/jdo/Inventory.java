package org.apache.isis.testdomain.jdo;

import java.util.Set;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.Auditing;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@PersistenceCapable(identityType=IdentityType.DATASTORE, schema = "testdomain")
@DatastoreIdentity(strategy=IdGeneratorStrategy.IDENTITY, column="id")
@Version(strategy= VersionStrategy.DATE_TIME, column="version")
@DomainObject(auditing = Auditing.ENABLED)
@DomainObjectLayout()  // causes UI events to be triggered
@AllArgsConstructor(staticName = "of") @ToString
public class Inventory {
    
    public String title() {
        return toString();
    }
    
    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private String name;
    
    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private Set<Product> products;
}
