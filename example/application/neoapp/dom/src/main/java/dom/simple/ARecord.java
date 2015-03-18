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
@Unique(name="A_RECORD_NAME_UNQ", members = {"name"})
@ObjectType("A_RECORD")
@Bookmarkable
public class ARecord implements Comparable<ARecord> {

    //region > name (property)
    private String name;

    @Column(allowsNull="false")
    @Title(sequence="1")
    @MemberOrder(sequence="1")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
    //endregion

    // region > IpAddress property
    private IpAddress ipAddress;

    @Column(allowsNull="false")
    @Title(sequence="2")
    @MemberOrder(sequence="2")
    public IpAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final IpAddress ipAddress) {
        this.ipAddress = ipAddress;
    }
    //endregion


    //region > compareTo
    @Override
    public int compareTo(ARecord other) {
        return ObjectContracts.compare(this, other, "name");
    }
    //endregion

    //region > injected services
    @Inject
    @SuppressWarnings("unused")
    private DomainObjectContainer container;
    //endregion

}
