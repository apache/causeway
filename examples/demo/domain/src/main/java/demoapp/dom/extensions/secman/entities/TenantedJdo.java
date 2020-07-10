package demoapp.dom.extensions.secman.entities;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Bounding;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.jaxbadapters.PersistentEntityAdapter;

import lombok.Getter;
import lombok.Setter;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo" )
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
public class TenantedJdo {

    public TenantedJdo(String name) {
        this.name = name;
    }

    @Title
    @Property(editing = Editing.ENABLED)
    @MemberOrder(name = "General", sequence = "1")
    @Getter @Setter
    private String name;

    @Action(associateWith = "name", associateWithSequence = "1", semantics = SemanticsOf.SAFE)
    public TenantedJdo updateName(final String name) {
        this.name = name;
        return this;
    }
    public String default0UpdateName() {
        return this.name;
    }

}
//end::class[]
