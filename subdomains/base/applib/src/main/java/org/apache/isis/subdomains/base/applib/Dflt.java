package org.apache.isis.subdomains.base.applib;

import java.util.Collection;

import org.apache.isis.applib.annotation.Programmatic;

public class Dflt {

    private Dflt(){}

    @Programmatic
    public static <T> T of(final Collection<T> choices) {
        switch(choices.size()) {
        case 0: return null;
        case 1: return choices.iterator().next();
        default: return null;
        }
    }

}
