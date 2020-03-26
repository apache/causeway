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
package org.apache.isis.core.runtime;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.metamodel.IsisModuleCoreMetamodel;
import org.apache.isis.core.runtime.events.RuntimeEventService;
import org.apache.isis.core.runtime.events.persistence.TimestampService;
import org.apache.isis.core.runtime.iactn.scope.IsisInteractionScopeBeanFactoryPostProcessor;
import org.apache.isis.core.runtime.persistence.transaction.AuditerDispatchService;
import org.apache.isis.core.runtime.persistence.transaction.ChangedObjectsService;

@Configuration
@Import({
        // modules
        IsisModuleCoreMetamodel.class,

        // @Service's
        RuntimeEventService.class,
        TimestampService.class,
        AuditerDispatchService.class,
        ChangedObjectsService.class,

        // @Configuration's

})
public class IsisModuleCoreRuntime {

    @Bean
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return new IsisInteractionScopeBeanFactoryPostProcessor();
    }
    
}
