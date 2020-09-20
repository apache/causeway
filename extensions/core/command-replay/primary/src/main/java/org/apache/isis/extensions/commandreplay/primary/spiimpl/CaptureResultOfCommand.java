package org.apache.isis.extensions.commandreplay.primary.spiimpl;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.isis.applib.services.commanddto.processor.spi.CommandDtoProcessorService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.common.v2.PeriodDto;

import lombok.val;

/**
 * Uses the SPI infrastructure to copy over standard properties from {@link Command} to {@link CommandDto}.
 */
@Service
@Named("isisExtensionsCommandReplayPrimary.CaptureResultOfCommand")
// specify quite a high priority since custom processors will probably want to run after this one
// (but can choose to run before if they wish)
@Order(OrderPrecedence.EARLY)
public class CaptureResultOfCommand implements CommandDtoProcessorService {

    @Override
    public CommandDto process(final Object domainObject, CommandDto commandDto) {

        if (!(domainObject instanceof CommandJdo)) {
            return commandDto;
        }

        val commandJdo = (CommandJdo) domainObject;
        if(commandDto == null) {
            commandDto = commandJdo.getCommandDto();
        }

        final Bookmark result = commandJdo.getResult();
        CommandDtoUtils.setUserData(commandDto, UserDataKeys.RESULT, result);

        // knowing whether there was an exception is on the primary is
        // used to determine whether to continue when replayed on the
        // secondary if an exception occurs there also
        CommandDtoUtils.setUserData(commandDto,
                UserDataKeys.EXCEPTION,
                commandJdo.getException());

        val timings = CommandDtoUtils.timingsFor(commandDto);
        timings.setStartedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(commandJdo.getStartedAt()));
        timings.setCompletedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(commandJdo.getCompletedAt()));

        return commandDto;
    }
}
