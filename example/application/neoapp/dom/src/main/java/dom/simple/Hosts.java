package dom.simple;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ParameterLayout;

@DomainService(menuOrder = "10", repositoryFor = Host.class)
public class Hosts {

    //region > create (action)
    @MemberOrder(sequence = "2")
    public Host create(
            final @ParameterLayout(named="Name") String name) {
        final Host obj = container.newTransientInstance(Host.class);
        obj.setName(name);
        container.persistIfNotAlready(obj);
        return obj;
    }

    //endregion

    //region > injected services

    @javax.inject.Inject
    DomainObjectContainer container;

    //endregion
}
