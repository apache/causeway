package dom.simple;

import javax.inject.Inject;
import javax.jdo.annotations.*;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.util.ObjectContracts;

@PersistenceCapable(identityType=IdentityType.DATASTORE)
@DatastoreIdentity(
        strategy= IdGeneratorStrategy.IDENTITY,
         column="id")
@Version(
        strategy=VersionStrategy.VERSION_NUMBER,
        column="version")
@Unique(name="IP_ADDRESS_ADDRESS_UNQ", members = {"address"})
@ObjectType("IP_ADDRESS")
@Bookmarkable
public class IpAddress implements Comparable<IpAddress> {

    // region > Address property
    private String address;

    @Column(allowsNull="false")
    @Title(sequence="1")
    @MemberOrder(sequence="1")
    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }
    //endregion


    //region > compareTo
    @Override
    public int compareTo(IpAddress other) {
        return ObjectContracts.compare(this, other, "address");
    }
    //endregion

    //region > injected services
    @Inject
    @SuppressWarnings("unused")
    private DomainObjectContainer container;
    //endregion

}
