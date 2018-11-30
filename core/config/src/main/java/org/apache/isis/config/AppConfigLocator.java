package org.apache.isis.config;

import javax.enterprise.inject.spi.CDI;

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
        
        AppConfig appConfig;
        
        appConfig = lookupAppConfig_UsingCDI();
        if(appConfig!=null) {
            LOG.info(String.format("Located AppConfig '%s' via CDI.", appConfig.getClass().getName()));
            return appConfig;
        }
        
        appConfig = lookupAppConfig_UsingServiceLoader();
        if(appConfig!=null) {
            LOG.info(String.format("Located AppConfig '%s' via ServiceLoader.", appConfig.getClass().getName()));
            return appConfig;
        }
        
        appConfig = lookupAppConfig_UsingConfigProperties();
        if(appConfig!=null) {
            LOG.info(String.format("Located AppConfig '%s' using config properties.", appConfig.getClass().getName()));
            return appConfig;    
        }
        
        throw new IsisException("Failed to locate the AppManifest");
    }
    
    private static AppConfig lookupAppConfig_UsingCDI() {
        try {
            final CDI<Object> cdi = CDI.current();
            if(cdi==null) {
                return null;
            }
            return cdi.select(AppConfig.class).get();
        } catch (Exception e) {
            // ignore
        }
        return null;        
    }
    
    private static AppConfig lookupAppConfig_UsingServiceLoader() {
        
        return _Plugin.getOrElse(AppConfig.class,
                ambiguousPlugins->{
                    throw _Plugin.ambiguityNonRecoverable(AppConfig.class, ambiguousPlugins);
                },
                ()->null);
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
