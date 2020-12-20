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
package org.apache.isis.persistence.jdo.implementation;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.runtime.IsisModuleCoreRuntime;
import org.apache.isis.persistence.jdo.applib.IsisModulePersistenceJdoApplib;

@Configuration
@Import({
    // modules
    IsisModuleCoreRuntime.class,
    IsisModulePersistenceJdoApplib.class,

    // @Component's
//    JdoProgrammingModel.class,
    
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
public class IsisModuleJdoImplementation {
    
//    
//    //TODO[2033] prefix 'isis.persistence.jdo-datanucleus.impl' is (for) legacy
//    @ConfigurationProperties(prefix = "isis.persistence.jdo-datanucleus.impl")
//    @Bean("dn-settings")
//    public Map<String, String> dnSettings() {
//        return new HashMap<>();
//    }
//    
//    /**
//     * {@link TransactionAwarePersistenceManagerFactoryProxy} was retired by the Spring Framework, recommended usage is still online [1].
//     * Sources have been recovered from [2].
//     * @see [1] https://docs.spring.io/spring-framework/docs/3.0.0.RC2/reference/html/ch13s04.html
//     * @see [2] https://github.com/spring-projects/spring-framework/tree/2b3445df8134e2b0c4e4a4c4136cbaf9d58b7fc4/spring-orm/src/main/java/org/springframework/orm/jdo
//     */
//    @Bean @Named("transaction-aware-pmf-proxy")
//    public TransactionAwarePersistenceManagerFactoryProxy getTransactionAwarePersistenceManagerFactoryProxy(
//            final LocalPersistenceManagerFactoryBean lpmfBean) {
//        
//        val pmf = lpmfBean.getObject();
//        JDOPersistenceManagerFactory jdopmf = (JDOPersistenceManagerFactory) pmf;
//        final PersistenceNucleusContext nucleusContext = jdopmf.getNucleusContext();
//        final MetaDataManager metaDataManager = nucleusContext.getMetaDataManager();
//        
//        val tapmfProxy = new TransactionAwarePersistenceManagerFactoryProxy();
//        tapmfProxy.setTargetPersistenceManagerFactory(pmf);
//        tapmfProxy.setAllowCreate(false);
//        return tapmfProxy;
//    }
//    
//    @Bean 
//    public LocalPersistenceManagerFactoryBean getLocalPersistenceManagerFactoryBean(
//            final @Named("dn-settings") Map<String, String> dnSettings) {
//        
//        val jdoPropertyMap = new HashMap<String, Object>();
//        dnSettings.forEach(jdoPropertyMap::put);
//        
//        val lpmfBean = new LocalPersistenceManagerFactoryBean();
//        lpmfBean.setJdoPropertyMap(jdoPropertyMap);
//        return lpmfBean; 
//    }
    
}
