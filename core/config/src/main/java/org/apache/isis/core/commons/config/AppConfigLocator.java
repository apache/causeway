package org.apache.isis.core.commons.config;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.context._Plugin;

public final class AppConfigLocator {
    
    private AppConfigLocator() { }
    
    public static AppConfig getAppConfig() {
        return _Context.computeIfAbsent(AppConfig.class, __->lookupAppConfig());
    }
    
    // -- HELPER
    
    private static AppConfig lookupAppConfig() {
        return _Plugin.getOrElse(AppConfig.class,
                ambiguousPlugins->{
                    throw _Plugin.ambiguityNonRecoverable(AppConfig.class, ambiguousPlugins);
                },
                ()->{
                    return IsisConfiguration::loadDefault;
                });
    }
    

}
