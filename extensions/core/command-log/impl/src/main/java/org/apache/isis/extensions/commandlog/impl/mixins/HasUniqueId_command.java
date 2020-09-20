package org.apache.isis.extensions.commandlog.impl.mixins;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.applib.services.command.Command;

import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;


/**
 * This mixin contributes a <tt>command</tt> action to any (non-command) implementation of
 * {@link org.apache.isis.applib.services.HasUniqueId}; that is: audit entries, and published events.  Thus, it
 * is possible to navigate from the effect back to the cause.
 */
@Action(
    semantics = SemanticsOf.SAFE
    , domainEvent = HasUniqueId_command.ActionDomainEvent.class
)
public class HasUniqueId_command {

    public static class ActionDomainEvent
            extends IsisModuleExtCommandLogImpl.ActionDomainEvent<HasUniqueId_command> { }

    private final HasUniqueId hasUniqueId;
    public HasUniqueId_command(final HasUniqueId hasUniqueId) {
        this.hasUniqueId = hasUniqueId;
    }


    @MemberOrder(name="transactionId", sequence="1")
    public CommandJdo act() {
        return findCommand();
    }
    /**
     * Hide if the contributee is a {@link Command}, because {@link Command}s already have a
     * {@link Command#getParent() parent} property.
     */
    public boolean hideAct() {
        return (hasUniqueId instanceof CommandJdo);
    }
    public String disableAct() {
        return findCommand() == null ? "No command found for unique Id": null;
    }

    private CommandJdo findCommand() {
        final UUID transactionId = hasUniqueId.getUniqueId();
        return commandServiceRepository
                .findByUniqueId(transactionId)
                .orElse(null);
    }

    @Inject CommandJdoRepository commandServiceRepository;
}
