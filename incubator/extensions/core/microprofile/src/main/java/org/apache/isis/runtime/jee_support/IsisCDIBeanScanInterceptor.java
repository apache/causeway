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
package org.apache.isis.runtime.jee_support;

import java.util.List;
import java.util.function.Predicate;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.reflection._Annotations;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 *
 * A CDI inject extension @see <a href="https://docs.jboss.org/weld/reference/latest/en-US/html/extend.html">weld</a>,
 * that lets CDI ignore certain Beans we declare vetoed.
 * <p>
 * This extension is registered as a service provider by creating a file named
 * {@code META-INF/services/javax.enterprise.inject.spi.Extension},
 * which contains the name of this extension class.
 * </p>
 *
 * <p>
 * Beans declared vetoed are managed (meaning instantiation and dependency injection)
 * by Isis itself. All other Beans are allowed to be managed by CDI.
 * </p>
 *
 *
 */
@Log4j2
public final class IsisCDIBeanScanInterceptor implements Extension {

    /**
     * Declaration of Beans that are managed by Isis and should be ignored by CDI.
     * (in addition to those that have the @DomainService annotation)
     */
    private static final List<Predicate<Class<?>>> vetoTests = _Lists.of(
        ExceptionRecognizer.class::isAssignableFrom,
        ServletContextListener.class::isAssignableFrom, // seems this has no effect anyway
        HttpServlet.class::isAssignableFrom, // seems this has no effect either
        type->type.getName().startsWith("org.springframework."),
        type->type.getName().startsWith("org.apache.isis."), 
        type->_Annotations.isPresent(type, Component.class));

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
        log.info("beginning the scanning process");
    }

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event) {

        final Class<?> type = event.getAnnotatedType().getJavaClass();
        final String className = type.getName();

        val isVetoed = isVetoed(type);
        if(isVetoed) {
            event.veto();
        }

        if(log.isDebugEnabled()) {
            if(isVetoed) {
                log.debug("vetoing type: {}", className);
            } else {
                log.debug("allowing type: {}", className);
            }
        }

    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        log.info("finished the scanning process");
    }

    // -- HELPER

    private boolean isVetoed(Class<?> type) {
        
        for(val vetoTest : vetoTests) {
            if(vetoTest.test(type)) {
                return true;
            }
        }
        return false;
    }

}
