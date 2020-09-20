package org.apache.isis.extensions.commandreplay.primary.restapi;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;
import org.apache.isis.extensions.commandreplay.primary.IsisModuleExtCommandReplayPrimary;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@DomainService(
    nature = NatureOfService.REST,
    objectType = "isisExtensionsCommandReplayPrimary.CommandRetrievalService"
)
@Named("isisExtensionsCommandReplayPrimary.CommandRetrievalService")
@Order(OrderPrecedence.MIDPOINT)
@Log4j2
public class CommandRetrievalService {

    public static abstract class ActionDomainEvent
            extends IsisModuleExtCommandReplayPrimary.ActionDomainEvent<CommandRetrievalService> { }

    public static class FindCommandsOnPrimaryFromDomainEvent extends ActionDomainEvent { }
    public static class NotFoundException extends ApplicationException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID uniqueId;
        public NotFoundException(final UUID uniqueId) {
            super("Command not found");
            this.uniqueId = uniqueId;
        }
    }

    /**
     * These actions should be called with HTTP Accept Header set to:
     * <code>application/xml;profile="urn:org.restfulobjects:repr-types/action-result";x-ro-domain-type="org.apache.isis.schema.cmd.v1.CommandsDto"</code>
     *
     * @param uniqueId - to search from.  This transactionId will <i>not</i> be included in the response.
     * @param batchSize - the maximum number of commands to return.  If not specified, all found will be returned.
     *
     * @return
     * @throws NotFoundException - if the command with specified transaction cannot be found.
     */
    @Action(domainEvent = FindCommandsOnPrimaryFromDomainEvent.class, semantics = SemanticsOf.SAFE)
    public List<CommandJdo> findCommandsOnPrimaryFrom(
            @Nullable
            @ParameterLayout(named="Unique Id")
            final UUID uniqueId,
            @Nullable
            @ParameterLayout(named="Batch size")
            final Integer batchSize)
            throws NotFoundException {
        final List<CommandJdo> commands = commandServiceRepository.findSince(uniqueId, batchSize);
        if(commands == null) {
            throw new NotFoundException(uniqueId);
        }
        return commands;
    }
    public Integer default1FindCommandsOnPrimaryFrom() {
        return 25;
    }



    @Inject CommandJdoRepository commandServiceRepository;
}

