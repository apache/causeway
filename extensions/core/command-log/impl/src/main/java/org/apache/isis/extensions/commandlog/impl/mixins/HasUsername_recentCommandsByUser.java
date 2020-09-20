package org.apache.isis.extensions.commandlog.impl.mixins;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.services.HasUsername;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;


@Collection(
    domainEvent = HasUsername_recentCommandsByUser.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table"
)
public class HasUsername_recentCommandsByUser {

    public static class CollectionDomainEvent
            extends IsisModuleExtCommandLogImpl.CollectionDomainEvent<HasUsername_recentCommandsByUser, CommandJdo> { }

    private final HasUsername hasUsername;
    public HasUsername_recentCommandsByUser(final HasUsername hasUsername) {
        this.hasUsername = hasUsername;
    }

    @MemberOrder(name="user", sequence = "3")
    public List<CommandJdo> coll() {
        final String username = hasUsername.getUsername();
        return username != null
                ? commandServiceRepository.findRecentByUsername(username)
                : Collections.emptyList();
    }
    public boolean hideColl() {
        return hasUsername.getUsername() == null;
    }

    @Inject CommandJdoRepository commandServiceRepository;
}
