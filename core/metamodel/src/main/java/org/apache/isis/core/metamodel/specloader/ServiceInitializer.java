/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.specloader;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.isis.commons.internal.collections._Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.lang.MethodExtensions;

public class ServiceInitializer {

    private final static Logger LOG = LoggerFactory.getLogger(ServiceInitializer.class);

    private final List<Object> services;

    private final Map<String, String> props;

    private Map<Object, Method> postConstructMethodsByService = _Maps.newLinkedHashMap();
    private Map<Object, Method> preDestroyMethodsByService = _Maps.newLinkedHashMap();

    public ServiceInitializer(
            final IsisConfiguration configuration,
            final List<Object> services) {
        this.props = configuration.asMap();
        this.services = services;
    }

    // -- validate

    public void validate() {

        for (final Object service : services) {
            LOG.debug("checking for @PostConstruct and @PostDestroy methods on {}", service.getClass().getName());
            final Method[] methods = service.getClass().getMethods();

            // @PostConstruct
            for (final Method method : methods) {

                final PostConstruct postConstructAnnotation = method.getAnnotation(PostConstruct.class);
                if(postConstructAnnotation == null) {
                    continue;
                }
                final Method existing = postConstructMethodsByService.get(service);
                if(existing != null) {
                    throw new RuntimeException("Found more than one @PostConstruct method; service is: " + service.getClass().getName() + ", found " + existing.getName() + " and " + method.getName());
                }

                final Class<?>[] parameterTypes = method.getParameterTypes();
                switch(parameterTypes.length) {
                case 0:
                    break;
                case 1:
                    if(Map.class != parameterTypes[0]) {
                        throw new RuntimeException("@PostConstruct method must be no-arg or 1-arg accepting java.util.Map; method is: " + service.getClass().getName() + "#" + method.getName());
                    }
                    break;
                default:
                    throw new RuntimeException("@PostConstruct method must be no-arg or 1-arg accepting java.util.Map; method is: " + service.getClass().getName() + "#" + method.getName());
                }
                postConstructMethodsByService.put(service, method);
            }

            // @PreDestroy
            for (final Method method : methods) {
                final PreDestroy preDestroyAnnotation = method.getAnnotation(PreDestroy.class);
                if(preDestroyAnnotation == null) {
                    continue;
                }
                final Method existing = preDestroyMethodsByService.get(service);
                if(existing != null) {
                    throw new RuntimeException("Found more than one @PreDestroy method; service is: " + service.getClass().getName() + ", found " + existing.getName() + " and " + method.getName());
                }

                final Class<?>[] parameterTypes = method.getParameterTypes();
                switch(parameterTypes.length) {
                case 0:
                    break;
                default:
                    throw new RuntimeException("@PreDestroy method must be no-arg; method is: " + service.getClass().getName() + "#" + method.getName());
                }
                preDestroyMethodsByService.put(service, method);
            }
        }

    }


    // -- postConstruct

    public void postConstruct() {
        if(LOG.isInfoEnabled()) {
            LOG.info("calling @PostConstruct on all domain services");
        }

        Exception firstExceptionIfAny = null;
        for (final Map.Entry<Object, Method> entry : postConstructMethodsByService.entrySet()) {
            final Object service = entry.getKey();
            final Method method = entry.getValue();

            if(LOG.isDebugEnabled()) {
                LOG.debug(
                        "... calling @PostConstruct method: " + service.getClass().getName() + ": " + method.getName());
            }

            final int numParams = method.getParameterTypes().length;

            // unlike shutdown, we don't swallow exceptions; would rather fail early
            try {
                if(numParams == 0) {
                    MethodExtensions.invoke(method, service);
                } else {
                    MethodExtensions.invoke(method, service, new Object[] { props });
                }
            } catch(Exception ex) {
                LOG.error(String.format(
                        "@PostConstruct on %s#%s: failed",
                        service.getClass().getName(), method.getName()),
                        ex);
                if(firstExceptionIfAny == null) {
                    firstExceptionIfAny = ex;
                }
            }
        }
        if(firstExceptionIfAny != null) {
            throw new RuntimeException(firstExceptionIfAny);
        }
    }



    // -- preDestroy

    public void preDestroy() {
        if(LOG.isInfoEnabled()) {
            LOG.info("calling @PreDestroy on all domain services");
        }
        for (final Map.Entry<Object, Method> entry : preDestroyMethodsByService.entrySet()) {
            final Object service = entry.getKey();
            final Method method = entry.getValue();

            if(LOG.isDebugEnabled()) {
                LOG.debug("... calling @PreDestroy method: {}: {}", service.getClass().getName(), method.getName());
            }

            try {
                MethodExtensions.invoke(method, service);
            } catch(Exception ex) {
                // do nothing
                LOG.warn(String.format(
                        "@PreDestroy on %s#%s: failed, continuing anyway",
                        service.getClass().getName(), method.getName()));
            }
        }
    }



}
