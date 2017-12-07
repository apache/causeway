package org.apache.isis.core.integtestsupport.scenarios;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.core.integtestsupport.IsisSystem;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.CucumberException;

public class ObjectFactoryForIntegration implements ObjectFactory {
    private final Map<Class<?>, Object> instances = Maps.newHashMap();

    public void start() { }

    public void stop() {
        this.instances.clear();
    }

    public boolean addClass(Class<?> clazz) {
        return true;
    }

    public <T> T getInstance(Class<T> type) {
        T instance = type.cast(this.instances.get(type));
        if (instance == null) {
            instance = this.newInstance(type);
            IsisSystem isisSystem = IsisSystem.getElseNull();
            if(isisSystem != null) {
                instance = this.cacheInstance(type, instance);
                isisSystem.getService(ServiceRegistry2.class).injectServicesInto(instance);
            } else {
                // don't cache
            }
        }
        return instance;
    }

    private <T> T cacheInstance(Class<T> type, T instance) {
        this.instances.put(type, instance);
        return instance;
    }

    private <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException var4) {
            throw new CucumberException(String.format("%s doesn't have an empty constructor.", type), var4);
        } catch (Exception var5) {
            throw new CucumberException(String.format("Failed to instantiate %s", type), var5);
        }
    }
}
