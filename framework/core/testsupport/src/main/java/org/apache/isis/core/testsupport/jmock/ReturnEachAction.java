package org.apache.isis.core.testsupport.jmock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

public class ReturnEachAction<T> implements Action {
    
    private final Collection<T> collection;
    private final Iterator<T> iterator;
    
    public ReturnEachAction(Collection<T> collection) {
        this.collection = collection;
        this.iterator = collection.iterator();
    }
    
    public ReturnEachAction(T... array) {
        this(Arrays.asList(array));
    }
    
    public T invoke(Invocation invocation) throws Throwable {
        return iterator.next();
    }
    
    public void describeTo(Description description) {
        description.appendValueList("return iterator.next() over ", ", ", "", collection);
    }
    
    /**
     * Factory
     */
    public static <T> Action returnEach(final T... values) {
        return new ReturnEachAction<T>(values);
    }

}
