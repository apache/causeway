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
package org.apache.isis.persistence.jpa.integration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.runtime.IsisModuleCoreRuntime;
import org.apache.isis.persistence.jpa.integration.changetracking.PersistenceMetricsServiceJpa;
import org.apache.isis.persistence.jpa.integration.entity.JpaEntityIntegration;
import org.apache.isis.persistence.jpa.integration.services.JpaSupportServiceUsingSpring;
import org.apache.isis.persistence.jpa.integration.typeconverters.applib.IsisBookmarkConverter;
import org.apache.isis.persistence.jpa.integration.typeconverters.applib.IsisLocalResourcePathConverter;
import org.apache.isis.persistence.jpa.integration.typeconverters.applib.IsisMarkupConverter;
import org.apache.isis.persistence.jpa.integration.typeconverters.applib.IsisPasswordConverter;
import org.apache.isis.persistence.jpa.integration.typeconverters.java.awt.JavaAwtBufferedImageByteArrayConverter;
import org.apache.isis.persistence.jpa.integration.typeconverters.java.time.OffsetDateTimeConverterForJpa;
import org.apache.isis.persistence.jpa.integration.typeconverters.java.time.OffsetTimeConverterForJpa;
import org.apache.isis.persistence.jpa.integration.typeconverters.java.time.ZonedDateTimeConverterForJpa;
import org.apache.isis.persistence.jpa.integration.typeconverters.java.util.JavaUtilUuidConverter;
import org.apache.isis.persistence.jpa.integration.typeconverters.schema.v2.IsisChangesDtoConverter;
import org.apache.isis.persistence.jpa.integration.typeconverters.schema.v2.IsisCommandDtoConverter;
import org.apache.isis.persistence.jpa.integration.typeconverters.schema.v2.IsisInteractionDtoConverter;
import org.apache.isis.persistence.jpa.integration.typeconverters.schema.v2.IsisOidDtoConverter;
import org.apache.isis.persistence.jpa.metamodel.IsisModulePersistenceJpaMetamodel;

@Configuration
@Import({
        // modules
        IsisModuleCoreRuntime.class,
        IsisModulePersistenceJpaMetamodel.class,

        // @Component's
        JpaEntityIntegration.class,

        // @Service's
        JpaSupportServiceUsingSpring.class,
        PersistenceMetricsServiceJpa.class,

})
@EntityScan(basePackageClasses = {

        // @Converter's
        IsisBookmarkConverter.class,
        IsisLocalResourcePathConverter.class,
        IsisMarkupConverter.class,
        IsisPasswordConverter.class,
        IsisChangesDtoConverter.class,
        IsisCommandDtoConverter.class,
        IsisInteractionDtoConverter.class,
        IsisOidDtoConverter.class,
        JavaAwtBufferedImageByteArrayConverter.class,
        JavaUtilUuidConverter.class,
        OffsetTimeConverterForJpa.class,
        OffsetDateTimeConverterForJpa.class,
        ZonedDateTimeConverterForJpa.class

})
public class IsisModulePersistenceJpaIntegration {

}
