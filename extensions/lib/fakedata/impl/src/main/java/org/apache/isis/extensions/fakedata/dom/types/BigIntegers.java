package org.apache.isis.extensions.fakedata.dom.types;

import java.math.BigInteger;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

public class BigIntegers extends AbstractRandomValueGenerator {

    public BigIntegers(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public BigInteger any() {
        final long x = fake.longs().any();
        return new BigInteger(""+x);
    }
}
