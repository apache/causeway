package org.apache.isis.subdomains.base.applib.with;


import org.apache.isis.testing.unittestsupport.applib.core.bidir.Instantiator;

public class InstantiatorForComparableByName implements Instantiator {
    public final Class<? extends WithNameComparable<?>> cls;
    private int i;

    public InstantiatorForComparableByName(Class<? extends WithNameComparable<?>> cls) {
        this.cls = cls;
    }

    @Override
    public Object instantiate() {
        WithNameComparable<?> newInstance;
        try {
            newInstance = cls.newInstance();
            newInstance.setName(""+(++i));
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
