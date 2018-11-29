package org.apache.isis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.context._Plugin;
import org.apache.isis.config.builder.IsisConfigurationBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;

public final class AppConfigLocator {
    
    private static final Logger LOG = LoggerFactory.getLogger(AppConfigLocator.class);
    
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
                    LOG.warn("Failed to locate AppConfig via ServiceLoader, falling back to "
                            + "search utilizing config properties.");
                    return lookupAppConfig_UsingConfigProperties();
                });
    }
    
    // to support pre 2.0.0-M2 behavior    
    private static AppConfig lookupAppConfig_UsingConfigProperties() {
        
        IsisConfigurationBuilder builder = IsisConfigurationBuilder.getDefault();
        String appManifestClassName =  builder.peekAtString("isis.appManifest");
        
        final Class<AppManifest> appManifestClass;
        try {
            appManifestClass = _Casts.uncheckedCast(_Context.loadClassAndInitialize(appManifestClassName));
        } catch (ClassNotFoundException e) {
            throw new IsisException("Failed to locate the AppManifest using config properties.", e);
        }
        
        final AppManifest appManifest;
        try {
            appManifest = appManifestClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IsisException(
                    String.format("Failed to create instance of AppManifest '%s'.", appManifestClass), e);
        }
        
        // Note: AppConfig is a FunctionalInterface
        return ()->IsisConfiguration.buildFromAppManifest(appManifest);
        
    }
    

}
