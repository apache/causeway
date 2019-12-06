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
package org.apache.isis.metamodel.services;

import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.inject.ServiceInjector;

/**
 * 
 * @since 2.0
 *
 */
@Service
@Named("isisMetaModel.ServiceInjectorDefault")
@Log4j2
public class ServiceInjectorDefault implements ServiceInjector {

    @Inject private AutowireCapableBeanFactory autowireCapableBeanFactory;
    
    
//    @PostConstruct
//    public void init() {
//        if(autowireCapableBeanFactory==null) {
//            autowireCapableBeanFactory = _Spring.context().getAutowireCapableBeanFactory();
//        }
//    }
    
    @Override
    public <T> T injectServicesInto(T domainObject, Consumer<InjectionPoint> onNotResolvable) {
        injectServices(domainObject, onNotResolvable);
        return domainObject;
    }

    // -- HELPERS

    private void injectServices(final Object targetPojo, Consumer<InjectionPoint> onNotResolvable) {

        autowireCapableBeanFactory.autowireBeanProperties(
                targetPojo,
                AutowireCapableBeanFactory.AUTOWIRE_NO, false);
        
    }


}
