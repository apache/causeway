package demoapp.dom.types.tuple;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo" )
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject
public class NumberConstant {

    @javax.jdo.annotations.Column(allowsNull = "false")
    @Property
    @Getter @Setter
    private String name;

    @javax.jdo.annotations.Column(allowsNull = "false")
    @Property
    @Getter @Setter
    private ComplexNumber number;
    
    @NotPersistent
    @Property
    public String getStringified() {
        return "" + number;
    }
    
}
