/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.isis.core.webapp.jee;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.metrics.MetricsService;

/**
 *
 * A CDI inject extension @see <a href="https://docs.jboss.org/weld/reference/latest/en-US/html/extend.html">weld</a>,
 * that lets CDI ignore certain Beans we declare tabu.
 * <p>
 * This extension is registered as a service provider by creating a file named
 * {@code META-INF/services/javax.enterprise.inject.spi.Extension},
 * which contains the name of this extension class.
 * </p>
 *
 * <p>
 * Beans declared tabu are managed (meaning instantiation and dependency injection)
 * by Isis itself. All other Beans are allowed to be managed by CDI.
 * </p>
 *
 *
 */
public final class IsisCDIBeanScanInterceptor implements Extension {

    private static final Logger log = LoggerFactory.getLogger(IsisCDIBeanScanInterceptor.class);

    /**
     * Declaration of Beans that are managed by Isis and should be ignored by CDI.
     * (in addition to those that have the @DomainService annotation)
     */
    private static final List<Predicate<Class<?>>> tabu = new ArrayList<>();
    {
        tabu.add(MetricsService.class::equals);
        tabu.add(ExceptionRecognizer.class::isAssignableFrom);
    }

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
        log.info("beginning the scanning process");
    }

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event) {

        final Class<?> clazz = event.getAnnotatedType().getJavaClass();
        final String className = clazz.getName();

        if(isTabu(clazz, event)) {
            log.debug("veto type: " + className);
            event.veto();
        } else {
            log.debug("allowing type: " + className);
        }
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        log.info("finished the scanning process");
    }

    // -- HELPER

    private boolean isTabu(Class<?> clazz, ProcessAnnotatedType<?> event) {
        if(event.getAnnotatedType().isAnnotationPresent(DomainService.class))
            return true;
        for(Predicate<Class<?>> isTabu : tabu) {
            if(isTabu.test(clazz))
                return true;
        }
        return false;
    }

}
