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
package org.apache.isis.persistence.jdo.integration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.runtime.IsisModuleCoreRuntime;
import org.apache.isis.persistence.jdo.applib.IsisModulePersistenceJdoApplib;
import org.apache.isis.persistence.jdo.integration.metamodel.JdoProgrammingModel;

@Configuration
@Import({
    // modules
    IsisModuleCoreRuntime.class,
    IsisModulePersistenceJdoApplib.class,

    // @Component's
    JdoProgrammingModel.class,
    
//    // @Service's
//    DataNucleusSettings.class,
//    ExceptionRecognizerForSQLIntegrityConstraintViolationUniqueOrIndexException.class,
//    ExceptionRecognizerForJDODataStoreExceptionIntegrityConstraintViolationForeignKeyNoActionException.class,
//    ExceptionRecognizerForJDOObjectNotFoundException.class,
//    ExceptionRecognizerForJDODataStoreException.class,
//    
//    IsisJdoSupportDN5.class,
//    IsisPlatformTransactionManagerForJdo.class,
//    JdoPersistenceLifecycleService.class,
//    PersistenceSessionFactory5.class,
//    JdoMetamodelMenu.class,
//
//    // @Mixin's
//    Persistable_datanucleusIdLong.class,
//    Persistable_datanucleusVersionLong.class,
//    Persistable_datanucleusVersionTimestamp.class,
//    Persistable_downloadJdoMetadata.class,
})
public class IsisModuleJdoIntegration {
    
    
    
    
}
