package org.apache.isis.extensions.fakedata.dom.types;

import java.math.BigDecimal;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

public class BigDecimals extends AbstractRandomValueGenerator {

    public BigDecimals(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public BigDecimal any() {
        final long x = fake.longs().any();
        final long y = fake.ints().upTo(4);
        return new BigDecimal(String.format("%d.%d", x, y));
    }

    @Programmatic
    public BigDecimal any(final int precision, final int scale) {
        final String sign = fake.booleans().coinFlip()? "": "-";
        final String x = fake.strings().digits(precision-scale);
        final String y = fake.strings().digits(scale);
        return new BigDecimal(String.format("%s%s.%s", sign, x, y));
    }

}
