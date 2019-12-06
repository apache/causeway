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
package org.apache.isis.runtime;

import org.apache.isis.metamodel.IsisModuleMetamodel;
import org.apache.isis.runtime.memento.ObjectMementoServiceDefault;
import org.apache.isis.runtime.system.context.session.RuntimeEventService;
import org.apache.isis.runtime.system.context.session.RuntimeEventSupport_Spring;
import org.apache.isis.runtime.system.persistence.events.PersistenceEventService;
import org.apache.isis.runtime.system.persistence.events.PersistenceEventService_Spring;
import org.apache.isis.runtime.system.persistence.events.TimestampService;
import org.apache.isis.runtime.system.session.IsisSessionFactoryDefault;
import org.apache.isis.runtime.system.transaction.AuditingServiceInternal;
import org.apache.isis.runtime.system.transaction.ChangedObjectsServiceInternal;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        // modules
        IsisModuleMetamodel.class,

        // @Service's
        ObjectMementoServiceDefault.class,
        RuntimeEventService.class,
        PersistenceEventService.class,
        TimestampService.class,
        IsisSessionFactoryDefault.class,
        AuditingServiceInternal.class,
        ChangedObjectsServiceInternal.class,

        // @Configuration's
        RuntimeEventSupport_Spring.class,
        PersistenceEventService_Spring.class,

})
public class IsisModuleRuntime {

}
