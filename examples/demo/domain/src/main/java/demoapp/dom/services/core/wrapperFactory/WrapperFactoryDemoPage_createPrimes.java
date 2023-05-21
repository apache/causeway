package demoapp.dom.services.core.wrapperFactory;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;

import demoapp.dom.services.core.wrapperFactory.jdo.PrimeNumberJdo;

import lombok.RequiredArgsConstructor;

import lombok.val;

@Action
@RequiredArgsConstructor
public class WrapperFactoryDemoPage_createPrimes {

    private final WrapperFactoryDemoPage page;

// tag::class[]
    @Inject WrapperFactory wrapperFactory;
    @Inject PrimeNumberGenerator primeNumberGenerator;

    @MemberSupport
    public WrapperFactoryDemoPage act(Integer upTo) {
        primeNumberRepository.removeAll();
        wrapperFactory.asyncWrap(                               // <.>
                primeNumberGenerator,                           // <.>
                AsyncControl.returningVoid().withSkipRules()    // <.>
        ).calculatePrimeNumbersAsync(1, upTo);                  // <.>
        return page;
    }
// end::class[]
    public String validateUpTo(Integer upTo) {
        if (upTo < 1) return "cannot be less than 1";
        if (upTo > 10000) return "can only calculate primes up to than 10000";
        return null;
    }

    @Inject PrimeNumberRepository<?> primeNumberRepository;

}
