package org.apache.isis.core.plugins.beantyperegistry;

import java.util.Set;

import org.apache.isis.commons.internal.context._Plugin;

public interface BeanTypeRegistryPlugin {

    // -- INTERFACE
    
    Set<Class<?>> getEntityTypes();
    
    // -- LOOKUP
    
    public static BeanTypeRegistryPlugin get() {
        return _Plugin.getOrElse(BeanTypeRegistryPlugin.class,
                ambiguousPlugins->{
                    return _Plugin.pickAnyAndWarn(BeanTypeRegistryPlugin.class, ambiguousPlugins);
                },
                ()->{
                    throw _Plugin.absenceNonRecoverable(BeanTypeRegistryPlugin.class);
                });
    }

}
