package org.apache.isis.extensions.fakedata.dom.types;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

public class Chars extends AbstractRandomValueGenerator {

    public Chars(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public char upper() {
        return anyOf("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    @Programmatic
    public char lower() {
        return anyOf("abcdefghijklmonpqrstuvwxyz");
    }

    @Programmatic
    public char any() {
        final int any = fake.shorts().any();
        final int i = any - Short.MIN_VALUE;
        return (char) i;
    }

    @Programmatic
    public char digit() {
        return anyOf("0123456789");
    }


    private char anyOf(final String s) {
        final char[] chars = s.toCharArray();
        return fake.collections().anyOf(chars);
    }

}
