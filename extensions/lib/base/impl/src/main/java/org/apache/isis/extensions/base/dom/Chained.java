package org.apache.isis.extensions.base.dom;

import org.apache.isis.applib.annotation.Editing;

import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Where;

import org.apache.isis.extensions.base.dom.with.WithInterval;
import org.apache.isis.extensions.base.dom.with.WithIntervalContiguous;

public interface Chained<T extends Chained<T>> {

    
    /**
     * The object (usually an {@link WithInterval}, but not necessarily) that precedes this one, if any (not
     * necessarily contiguously)..
     * 
     * <p>
     * Implementations where successive intervals are contiguous should instead implement 
     * {@link WithIntervalContiguous}.
     */
    @Property(editing = Editing.DISABLED, hidden=Where.ALL_TABLES, optionality = Optionality.OPTIONAL)
    public T getPrevious();

    /**
     * The object (usually an {@link WithInterval}, but not necessarily) that succeeds this one, if any (not 
     * necessarily contiguously).
     * 
     * <p>
     * Implementations where successive intervals are contiguous should instead implement 
     * {@link WithIntervalContiguous}.
     */
    @Property(editing = Editing.DISABLED, hidden=Where.ALL_TABLES, optionality = Optionality.OPTIONAL)
    public T getNext();
    
    
}
