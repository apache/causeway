package org.apache.isis.testing.fakedata.applib.services;

import org.apache.isis.applib.annotation.Programmatic;

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
