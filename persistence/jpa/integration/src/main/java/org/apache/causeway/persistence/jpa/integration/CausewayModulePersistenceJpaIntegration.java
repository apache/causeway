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
package org.apache.causeway.persistence.jpa.integration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.core.runtime.CausewayModuleCoreRuntime;
import org.apache.causeway.persistence.commons.CausewayModulePersistenceCommons;
import org.apache.causeway.persistence.jpa.integration.entity.JpaEntityIntegration;
import org.apache.causeway.persistence.jpa.integration.services.JpaSupportServiceUsingSpring;
import org.apache.causeway.persistence.jpa.integration.typeconverters.applib.CausewayBookmarkConverter;
import org.apache.causeway.persistence.jpa.integration.typeconverters.applib.CausewayLocalResourcePathConverter;
import org.apache.causeway.persistence.jpa.integration.typeconverters.applib.CausewayMarkupConverter;
import org.apache.causeway.persistence.jpa.integration.typeconverters.applib.CausewayPasswordConverter;
import org.apache.causeway.persistence.jpa.integration.typeconverters.java.awt.JavaAwtBufferedImageByteArrayConverter;
import org.apache.causeway.persistence.jpa.integration.typeconverters.java.time.OffsetDateTimeConverterForJpa;
import org.apache.causeway.persistence.jpa.integration.typeconverters.java.time.OffsetTimeConverterForJpa;
import org.apache.causeway.persistence.jpa.integration.typeconverters.java.time.ZonedDateTimeConverterForJpa;
import org.apache.causeway.persistence.jpa.integration.typeconverters.java.util.JavaUtilUuidConverter;
import org.apache.causeway.persistence.jpa.integration.typeconverters.schema.v2.CausewayChangesDtoConverter;
import org.apache.causeway.persistence.jpa.integration.typeconverters.schema.v2.CausewayCommandDtoConverter;
import org.apache.causeway.persistence.jpa.integration.typeconverters.schema.v2.CausewayInteractionDtoConverter;
import org.apache.causeway.persistence.jpa.integration.typeconverters.schema.v2.CausewayOidDtoConverter;
import org.apache.causeway.persistence.jpa.metamodel.CausewayModulePersistenceJpaMetamodel;

@Configuration
@Import({
        // modules
        CausewayModuleCoreRuntime.class,
        CausewayModulePersistenceCommons.class,
        CausewayModulePersistenceJpaMetamodel.class,

        // @Component's
        JpaEntityIntegration.class,

        // @Service's
        JpaSupportServiceUsingSpring.class,

})
@EntityScan(basePackageClasses = {

        // @Converter's
        CausewayBookmarkConverter.class,
        CausewayLocalResourcePathConverter.class,
        CausewayMarkupConverter.class,
        CausewayPasswordConverter.class,
        CausewayChangesDtoConverter.class,
        CausewayCommandDtoConverter.class,
        CausewayInteractionDtoConverter.class,
        CausewayOidDtoConverter.class,
        JavaAwtBufferedImageByteArrayConverter.class,
        JavaUtilUuidConverter.class,
        OffsetTimeConverterForJpa.class,
        OffsetDateTimeConverterForJpa.class,
        ZonedDateTimeConverterForJpa.class

})
public class CausewayModulePersistenceJpaIntegration {

}
