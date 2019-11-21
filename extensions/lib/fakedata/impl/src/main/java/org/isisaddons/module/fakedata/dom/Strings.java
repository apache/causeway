package org.isisaddons.module.fakedata.dom;

import org.apache.isis.applib.annotation.Programmatic;

public class Strings extends AbstractRandomValueGenerator{

    public Strings(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public String upper(final int numChars) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < numChars; i++) {
            buf.append(fake.chars().upper());
        }
        return buf.toString();
    }

    @Programmatic
    public String fixed(final int numChars) {
        return fake.lorem().javaFakerLorem.fixedString(numChars);
    }

    @Programmatic
    public String digits(final int numDigits) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < numDigits; i++) {
            buf.append(fake.chars().digit());
        }
        return buf.toString();
    }
}
