package org.apache.isis.extensions.commandreplay.secondary.jobcallables;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.extensions.commandreplay.secondary.clock.TickingClockService;

public class IsTickingClockInitialized implements Callable<Boolean> {

    @Inject
    TransactionService transactionService;
    @Inject
    TickingClockService tickingClockService;

    @Override
    public Boolean call() {
        return transactionService.executeWithinTransaction(
                () -> tickingClockService.isInitialized());
    }
}
