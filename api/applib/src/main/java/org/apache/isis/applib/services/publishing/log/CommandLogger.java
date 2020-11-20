package org.apache.isis.applib.services.publishing.log;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisApplib.CommandLogger")
@Order(OrderPrecedence.LATE)
@Primary
@Qualifier("Logging")
@Log4j2
public class CommandLogger implements CommandSubscriber {

    @Override
    public boolean isEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void onCompleted(Command command) {
        log.debug("completed: {}", command);
    }

}
