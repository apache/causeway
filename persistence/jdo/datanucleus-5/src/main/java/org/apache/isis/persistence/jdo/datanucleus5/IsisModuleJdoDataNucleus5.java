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
package org.apache.isis.persistence.jdo.datanucleus5;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.runtime.IsisModuleCoreRuntime;
import org.apache.isis.persistence.jdo.applib.IsisModulePersistenceJdoApplib;
import org.apache.isis.persistence.jdo.datanucleus5.datanucleus.DataNucleusSettings;
import org.apache.isis.persistence.jdo.datanucleus5.datanucleus.service.JdoPersistenceLifecycleService;
import org.apache.isis.persistence.jdo.datanucleus5.exceprecog.ExceptionRecognizerForJDODataStoreException;
import org.apache.isis.persistence.jdo.datanucleus5.exceprecog.ExceptionRecognizerForJDODataStoreExceptionIntegrityConstraintViolationForeignKeyNoActionException;
import org.apache.isis.persistence.jdo.datanucleus5.exceprecog.ExceptionRecognizerForJDOObjectNotFoundException;
import org.apache.isis.persistence.jdo.datanucleus5.exceprecog.ExceptionRecognizerForSQLIntegrityConstraintViolationUniqueOrIndexException;
import org.apache.isis.persistence.jdo.datanucleus5.jdosupport.IsisJdoSupportDN5;
import org.apache.isis.persistence.jdo.datanucleus5.jdosupport.mixins.Persistable_datanucleusIdLong;
import org.apache.isis.persistence.jdo.datanucleus5.jdosupport.mixins.Persistable_datanucleusVersionLong;
import org.apache.isis.persistence.jdo.datanucleus5.jdosupport.mixins.Persistable_datanucleusVersionTimestamp;
import org.apache.isis.persistence.jdo.datanucleus5.jdosupport.mixins.Persistable_downloadJdoMetadata;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.JdoMetamodelMenu;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.JdoProgrammingModelPlugin;
import org.apache.isis.persistence.jdo.datanucleus5.persistence.IsisPlatformTransactionManagerForJdo;
import org.apache.isis.persistence.jdo.datanucleus5.persistence.PersistenceSessionFactory5;

@Configuration
@Import({
        // modules
        IsisModuleCoreRuntime.class,
        IsisModulePersistenceJdoApplib.class,

        // @Component's
        JdoProgrammingModelPlugin.class,
        
        // @Service's
        DataNucleusSettings.class,
        ExceptionRecognizerForSQLIntegrityConstraintViolationUniqueOrIndexException.class,
        ExceptionRecognizerForJDODataStoreExceptionIntegrityConstraintViolationForeignKeyNoActionException.class,
        ExceptionRecognizerForJDOObjectNotFoundException.class,
        ExceptionRecognizerForJDODataStoreException.class,
        
        IsisJdoSupportDN5.class,
        IsisPlatformTransactionManagerForJdo.class,
        JdoPersistenceLifecycleService.class,
        PersistenceSessionFactory5.class,
        JdoMetamodelMenu.class,

        // @Mixin's
        Persistable_datanucleusIdLong.class,
        Persistable_datanucleusVersionLong.class,
        Persistable_datanucleusVersionTimestamp.class,
        Persistable_downloadJdoMetadata.class,
})
public class IsisModuleJdoDataNucleus5 {
    
    // reserved for datanucleus' own config props
    @ConfigurationProperties(prefix = "isis.persistence.jdo-datanucleus.impl")
    @Bean("dn-settings")
    public Map<String, String> getAsMap() {
        return new HashMap<>();
    }
    
}
