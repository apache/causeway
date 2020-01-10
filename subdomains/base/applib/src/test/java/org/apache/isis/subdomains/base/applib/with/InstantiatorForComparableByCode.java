package org.apache.isis.subdomains.base.applib.with;

import org.apache.isis.core.unittestsupport.bidir.Instantiator;

public class InstantiatorForComparableByCode implements Instantiator {
    public final Class<? extends WithCodeComparable<?>> cls;
    private int i;

    public InstantiatorForComparableByCode(Class<? extends WithCodeComparable<?>> cls) {
        this.cls = cls;
    }

    @Override
    public Object instantiate() {
        WithCodeComparable<?> newInstance;
        try {
            newInstance = cls.newInstance();
            newInstance.setCode(""+(++i));
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
