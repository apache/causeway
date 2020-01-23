package org.apache.isis.applib.value;

import org.apache.isis.core.commons.collections.Can;

public interface TypedTuple {

    Can<Class<?>> getTypes();
    Can<Can<?>> getValues();
    
}
