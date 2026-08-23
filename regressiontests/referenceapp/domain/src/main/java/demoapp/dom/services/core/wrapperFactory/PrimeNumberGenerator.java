package demoapp.dom.services.core.wrapperFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

//tag::class[]
@Named("demo.DemoEntityFactory")
@DomainService                                                      // <.>
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PrimeNumberGenerator {

    final PrimeNumberFactory<?> primeNumberFactory;
    final WrapperFactory wrapperFactory;

    @Action
    @ActionLayout(hidden = Where.EVERYWHERE)                        // <.>
    public void calculatePrimeNumbersAsync(final int from, final int upTo) {
        int nextPrime = nextPrime(from);
        if (nextPrime <= upTo) {
            primeNumberFactory.newPrimeNumber(nextPrime);
            wrapperFactory.asyncWrap(
                    this,
                    AsyncControl.defaults().withSkipRules())
            .acceptAsync(proxy->
                    proxy.calculatePrimeNumbersAsync(nextPrime, upTo));// <.>
        }
    }

    @SneakyThrows
    private static int nextPrime(int number) {
        Thread.sleep(200);                                          // <.>
        number++;
        while (!isPrime(number)) {
            number++;
        }
        return number;
    }

    private static boolean isPrime(final int number) {
        //...
//end::class[]
        if (number <= 1) {
            return false;
        }

        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                return false;
            }
        }

        return true;
//tag::class[]
    }
}
//end::class[]
