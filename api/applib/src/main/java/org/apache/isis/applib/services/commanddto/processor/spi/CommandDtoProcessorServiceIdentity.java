package org.apache.isis.applib.services.commanddto.processor.spi;

import javax.inject.Named;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.schema.cmd.v2.CommandDto;

/**
 * At least one implementation is required.
 */
@Service
@Named("isisApplib.CommandDtoProcessorServiceIdentity")
@Order(OrderPrecedence.LAST)
public class CommandDtoProcessorServiceIdentity implements CommandDtoProcessorService {

    @Override
    public CommandDto process(final Object domainObject, final CommandDto commandDto) {
        return commandDto;
    }
}
