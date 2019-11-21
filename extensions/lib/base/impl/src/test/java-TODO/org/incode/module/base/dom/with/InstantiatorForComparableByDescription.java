package org.incode.module.base.dom.with;

import org.apache.isis.core.unittestsupport.bidir.Instantiator;

public class InstantiatorForComparableByDescription implements Instantiator {
    public final Class<? extends WithDescriptionComparable<?>> cls;
    private int i;

    public InstantiatorForComparableByDescription(
            Class<? extends WithDescriptionComparable<?>> cls) {
        this.cls = cls;
    }

    @Override
    public Object instantiate() {
        WithDescriptionComparable<?> newInstance;
        try {
            newInstance = cls.newInstance();
            newInstance.setDescription(""+(++i));
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
