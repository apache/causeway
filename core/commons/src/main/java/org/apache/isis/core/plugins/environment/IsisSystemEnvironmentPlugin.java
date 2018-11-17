package org.apache.isis.core.plugins.environment;

import org.apache.isis.commons.internal.context._Plugin;

public interface IsisSystemEnvironmentPlugin {
    
    // -- INTERFACE
    
    public IsisSystemEnvironment getIsisSystemEnvironment();
    

    // -- PLUGIN LOOKUP

    public static IsisSystemEnvironmentPlugin get() {
        return _Plugin.getOrElse(IsisSystemEnvironmentPlugin.class,
                ambiguousPlugins->{
                    throw _Plugin.ambiguityNonRecoverable(IsisSystemEnvironmentPlugin.class, ambiguousPlugins);
                },
                ()->{
                    return IsisSystemEnvironment::getDefault;
                });
    }
    
}
