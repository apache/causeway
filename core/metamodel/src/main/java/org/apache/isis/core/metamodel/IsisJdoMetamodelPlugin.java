package org.apache.isis.core.metamodel;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.context._Plugin;

public interface IsisJdoMetamodelPlugin {

    // -- INTERFACE

    /**
     * Equivalent to org.datanucleus.enhancement.Persistable.class.isAssignableFrom(cls).
     * @param cls
     * @return
     */
    public boolean isPersistenceEnhanced(@Nullable Class<?> cls);

    /**
     * Equivalent to org.datanucleus.enhancement.Persistable.class.getDeclaredMethods().
     * @return
     */
    public Method[] getMethodsProvidedByEnhancement();

    // -- LOOKUP

    public static IsisJdoMetamodelPlugin get() {
        return _Plugin.getOrElse(IsisJdoMetamodelPlugin.class,
                ambiguousPlugins->{
                    throw _Plugin.ambiguityNonRecoverable(IsisJdoMetamodelPlugin.class, ambiguousPlugins);
                },
                ()->{
                    throw _Plugin.absenceNonRecoverable(IsisJdoMetamodelPlugin.class);
                });
    }

}
