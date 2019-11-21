package org.isisaddons.module.fakedata.dom;

import java.math.BigInteger;
import org.apache.isis.applib.annotation.Programmatic;

public class BigIntegers extends AbstractRandomValueGenerator{

    public BigIntegers(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public BigInteger any() {
        final long x = fake.longs().any();
        return new BigInteger(""+x);
    }
}
