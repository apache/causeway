package org.apache.isis.extensions.base.dom.with;

import org.joda.time.LocalDate;

import org.apache.isis.unittestsupport.bidir.Instantiator;

public class InstantiatorForWithStartDate implements Instantiator {
    public final Class<? extends WithStartDate> cls;
    private int i;

    public InstantiatorForWithStartDate(Class<? extends WithStartDate> cls) {
        this.cls = cls;
    }

    @Override
    public Object instantiate() {
        WithStartDate newInstance;
        try {
            newInstance = cls.newInstance();
            newInstance.setStartDate(new LocalDate(2013,1,1).plusDays(i++));
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
