package org.apache.isis.core.runtimeservices.clock;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.iactn.ExecutionContext;
import org.apache.isis.core.interaction.session.InteractionTracker;

import lombok.RequiredArgsConstructor;

@Service
@Named("isis.runtimeservices.ClockServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ClockServiceDefault implements ClockService {
    
    private final InteractionTracker interactionTracker;

    @Override
    public VirtualClock getClock() {
        return interactionTracker.currentExecutionContext()
        .map(ExecutionContext::getClock)
        .orElseGet(VirtualClock::system);
    }

}
