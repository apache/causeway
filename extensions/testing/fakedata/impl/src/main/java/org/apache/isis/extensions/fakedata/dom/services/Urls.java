package org.apache.isis.extensions.fakedata.dom.services;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.isis.applib.annotation.Programmatic;

public class Urls extends AbstractRandomValueGenerator {

    public Urls(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public URL any() {
        try {
            final String protocol = fake.booleans().coinFlip() ? "http" : "https:";
            final String url = fake.comms().url();
            return new URL(String.format("%s://%s", protocol, url));
        } catch (MalformedURLException e) {
            // not expected
            throw new RuntimeException(e);
        }
    }
}
