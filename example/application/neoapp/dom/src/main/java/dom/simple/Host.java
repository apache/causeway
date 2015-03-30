package dom.simple;

import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Title;

import javax.jdo.annotations.*;
import java.util.List;

@PersistenceCapable(identityType= IdentityType.DATASTORE)
@DatastoreIdentity(
        strategy= IdGeneratorStrategy.IDENTITY,
        column="id")
@Version(
        strategy=VersionStrategy.VERSION_NUMBER,
        column="version")
@Unique(name="HOST_NAME_UNQ", members = {"name"})
@ObjectType("HOST")
@Bookmarkable
public class Host {

    // region > Name property
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

    // region > IpAddresses property
    private List<IpAddress> ipAddresses;

    @Column(allowsNull="false")
    @Title(sequence="2")
    @MemberOrder(sequence="2")
    public List<IpAddress> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(final List<IpAddress> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }
    //endregion

    public void addIpAddress(IpAddress ipAddress){
        this.ipAddresses.add(ipAddress);
    }

    public void removeIpAddress(IpAddress ipAddress){
        this.ipAddresses.remove(ipAddress);
    }

}
