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
package org.apache.isis.persistence.jdo.datanucleus;

import java.util.Collections;
import java.util.List;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManagerFactory;
import javax.sql.DataSource;

import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.config.beans.aoppatch.TransactionInterceptorFactory;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.services.objectlifecycle.ObjectLifecyclePublisher;
import org.apache.isis.persistence.jdo.datanucleus.changetracking.JdoLifecycleListener;
import org.apache.isis.persistence.jdo.datanucleus.config.DatanucleusSettings;
import org.apache.isis.persistence.jdo.datanucleus.dialect.DnJdoDialect;
import org.apache.isis.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.isis.persistence.jdo.datanucleus.jdosupport.JdoSupportServiceDefault;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_datanucleusVersionLong;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_datanucleusVersionTimestamp;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_downloadJdoMetadata;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoByteIdValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoByteIdentityValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoCharIdValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoCharIdentityValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoDatastoreIdImplValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoDatastoreIdValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoDatastoreUniqueLongIdValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoIntIdValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoIntIdentityValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoLongIdValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoLongIdentityValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoObjectIdValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoObjectIdentityValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoShortIdValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoShortIdentityValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoStringIdValueSemantics;
import org.apache.isis.persistence.jdo.datanucleus.valuetypes.JdoStringIdentityValueSemantics;
import org.apache.isis.persistence.jdo.integration.IsisModulePersistenceJdoIntegration;
import org.apache.isis.persistence.jdo.provider.config.JdoEntityDiscoveryListener;
import org.apache.isis.persistence.jdo.spring.integration.JdoDialect;
import org.apache.isis.persistence.jdo.spring.integration.JdoTransactionManager;
import org.apache.isis.persistence.jdo.spring.integration.LocalPersistenceManagerFactoryBean;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
    // modules
    IsisModulePersistenceJdoDatanucleus.class,

    // @Mixin's
    Persistable_datanucleusVersionLong.class,
    Persistable_datanucleusVersionTimestamp.class,
    Persistable_downloadJdoMetadata.class,

})
@EnableConfigurationProperties(DatanucleusSettings.class)
@Log4j2
public class IsisModulePersistenceJdoDatanucleusMixins {

}
