package org.apache.isis.extensions.commandreplay.secondary.ui;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;
import org.apache.isis.extensions.commandreplay.secondary.IsisModuleExtCommandReplaySecondary;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.CommandsDto;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@DomainService(
    nature = NatureOfService.VIEW,
    objectType = "isisExtensionsCommandReplaySecondary.CommandReplayOnSecondaryService"
)
@DomainServiceLayout(
    named = "Activity",
    menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@Named("isisExtensionsCommandReplaySecondary.CommandReplayOnSecondaryService")
@Order(OrderPrecedence.MIDPOINT)
@RequiredArgsConstructor
@Log4j2
public class CommandReplayOnSecondaryService {

    @Inject final CommandJdoRepository commandJdoRepository;
    @Inject final JaxbService jaxbService;

    public static abstract class ActionDomainEvent
            extends IsisModuleExtCommandReplaySecondary.ActionDomainEvent<CommandReplayOnSecondaryService> { }

    public static class FindMostRecentReplayedDomainEvent extends ActionDomainEvent { }
    @Action(domainEvent = FindMostRecentReplayedDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-bath")
    @MemberOrder(sequence="60.1")
    public CommandJdo findMostRecentReplayed() {
        return commandJdoRepository.findMostRecentReplayed().orElse(null);
    }



    public static class UploadCommandsDomainEvent extends ActionDomainEvent { }
    @Action(
        domainEvent = UploadCommandsDomainEvent.class,
        semantics = SemanticsOf.NON_IDEMPOTENT
    )
    @ActionLayout(cssClassFa = "fa-upload")
    @MemberOrder(sequence="60.2")
    public void uploadCommands(final Clob commandsDtoAsXml) {
        val chars = commandsDtoAsXml.getChars();
        List<CommandDto> commandDtoList;

        try {
            val commandsDto = jaxbService.fromXml(CommandsDto.class, chars.toString());
            commandDtoList = commandsDto.getCommandDto();

        } catch(Exception ex) {
            val commandDto = jaxbService.fromXml(CommandDto.class, chars.toString());
            commandDtoList = Collections.singletonList(commandDto);
        }

        for (final CommandDto commandDto : commandDtoList) {
            commandJdoRepository.saveForReplay(commandDto);
        }
    }



}

