package org.apache.isis.extensions.base.dom.with;


import org.apache.isis.extensions.base.dom.with.WithReferenceComparable;
import org.apache.isis.unittestsupport.bidir.Instantiator;

public class InstantiatorForComparableByReference implements Instantiator {
    public final Class<? extends WithReferenceComparable<?>> cls;
    private int i;

    public InstantiatorForComparableByReference(
            Class<? extends WithReferenceComparable<?>> cls) {
        this.cls = cls;
    }

    @Override
    public Object instantiate() {
        WithReferenceComparable<?> newInstance;
        try {
            newInstance = cls.newInstance();
            newInstance.setReference(""+(++i));
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
