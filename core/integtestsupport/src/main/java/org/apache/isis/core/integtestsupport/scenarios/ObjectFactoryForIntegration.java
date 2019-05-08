/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.integtestsupport.scenarios;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.runtime.headless.IsisSystem;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.CucumberException;

public class ObjectFactoryForIntegration implements ObjectFactory {
    private final Map<Class<?>, Object> instances = _Maps.newHashMap();

    @Override
    public void start() { }

    @Override
    public void stop() {
        this.instances.clear();
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        T instance = type.cast(this.instances.get(type));
        if (instance == null) {
            instance = this.newInstance(type);
            IsisSystem isisSystem = IsisSystem.getElseNull();
            if(isisSystem != null) {
                instance = this.cacheInstance(type, instance);
                isisSystem.getService(ServiceInjector.class).injectServicesInto(instance);
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
