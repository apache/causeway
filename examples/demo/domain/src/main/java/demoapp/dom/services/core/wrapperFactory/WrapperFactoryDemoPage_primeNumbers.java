package demoapp.dom.services.core.wrapperFactory;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

@Collection
@CollectionLayout(paged = 20)
@RequiredArgsConstructor
public class WrapperFactoryDemoPage_primeNumbers {

    private final WrapperFactoryDemoPage page;

    @MemberSupport
    public List<? extends PrimeNumber> coll() {
        return primeNumberRepository.all();
    }

    @Inject PrimeNumberRepository primeNumberRepository;

}
