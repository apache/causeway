package org.apache.isis.core.metamodel.adapter;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.isis.applib.profiles.Localization;

public final class LocalizationDefault implements Localization {
    
    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }
}