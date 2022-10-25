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
package demoapp.dom;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import org.apache.causeway.extensions.commandlog.jpa.CausewayModuleExtCommandLogPersistenceJpa;
import org.apache.causeway.persistence.jpa.eclipselink.CausewayModulePersistenceJpaEclipselink;

import demoapp.dom.domain.actions.Action.commandPublishing.jpa.ActionCommandPublishingJpa;
import demoapp.dom.domain.actions.Action.executionPublishing.jpa.ActionExecutionPublishingJpa;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.disabled.jpa.DomainObjectEntityChangePublishingDisabledJpa;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.enabled.jpa.DomainObjectEntityChangePublishingEnabledJpa;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnot.enabled.jpa.DomainObjectEntityChangePublishingEnabledMetaAnnotatedJpa;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.metaAnnotOverridden.enabled.jpa.DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJpa;
import demoapp.dom.domain.objects.DomainObject.nature.viewmodels.jaxbrefentity.jpa.JaxbRefJpa;
import demoapp.dom.domain.objects.other.embedded.jpa.NumberConstantJpa;
import demoapp.dom.domain.properties.Property.commandPublishing.jpa.PropertyCommandPublishingJpa;
import demoapp.dom.domain.properties.Property.executionPublishing.jpa.PropertyExecutionPublishingJpa;
import demoapp.dom.domain.properties.Property.projecting.jpa.PropertyProjectingChildJpa;
import demoapp.dom.services.core.eventbusservice.EventLogEntryJpa;
import demoapp.dom.services.core.wrapperFactory.jpa.WrapperFactoryJpa;
import demoapp.dom.services.extensions.secman.apptenancy.jpa.TenantedJpa;
import demoapp.dom.types.causeway.blobs.jpa.CausewayBlobJpa;
import demoapp.dom.types.causeway.clobs.jpa.CausewayClobJpa;
import demoapp.dom.types.causeway.localresourcepaths.jpa.CausewayLocalResourcePathJpa;
import demoapp.dom.types.causeway.markups.jpa.CausewayMarkupJpa;
import demoapp.dom.types.causeway.passwords.jpa.CausewayPasswordJpa;
import demoapp.dom.types.causewayext.cal.jpa.CausewayCalendarEventJpa;
import demoapp.dom.types.causewayval.asciidocs.jpa.CausewayAsciiDocJpa;
import demoapp.dom.types.causewayval.markdowns.jpa.CausewayMarkdownJpa;
import demoapp.dom.types.causewayval.vegas.jpa.CausewayVegaJpa;
import demoapp.dom.types.javaawt.images.jpa.JavaAwtBufferedImageJpa;
import demoapp.dom.types.javalang.booleans.jpa.WrapperBooleanJpa;
import demoapp.dom.types.javalang.bytes.jpa.WrapperByteJpa;
import demoapp.dom.types.javalang.characters.jpa.WrapperCharacterJpa;
import demoapp.dom.types.javalang.doubles.jpa.WrapperDoubleJpa;
import demoapp.dom.types.javalang.enums.jpa.JavaLangEnumJpa;
import demoapp.dom.types.javalang.floats.jpa.WrapperFloatJpa;
import demoapp.dom.types.javalang.integers.jpa.WrapperIntegerJpa;
import demoapp.dom.types.javalang.longs.jpa.WrapperLongJpa;
import demoapp.dom.types.javalang.shorts.jpa.WrapperShortJpa;
import demoapp.dom.types.javalang.strings.jpa.JavaLangStringJpa;
import demoapp.dom.types.javamath.bigdecimals.jpa.JavaMathBigDecimalJpa;
import demoapp.dom.types.javamath.bigintegers.jpa.JavaMathBigIntegerJpa;
import demoapp.dom.types.javanet.urls.jpa.JavaNetUrlJpa;
import demoapp.dom.types.javasql.javasqldate.jpa.JavaSqlDateJpa;
import demoapp.dom.types.javasql.javasqltimestamp.jpa.JavaSqlTimestampJpa;
import demoapp.dom.types.javatime.javatimelocaldate.jpa.JavaTimeLocalDateJpa;
import demoapp.dom.types.javatime.javatimelocaldatetime.jpa.JavaTimeLocalDateTimeJpa;
import demoapp.dom.types.javatime.javatimelocaltime.jpa.JavaTimeLocalTimeJpa;
import demoapp.dom.types.javatime.javatimeoffsetdatetime.jpa.JavaTimeOffsetDateTimeJpa;
import demoapp.dom.types.javatime.javatimeoffsettime.jpa.JavaTimeOffsetTimeJpa;
import demoapp.dom.types.javatime.javatimezoneddatetime.jpa.JavaTimeZonedDateTimeJpa;
import demoapp.dom.types.javautil.javautildate.jpa.JavaUtilDateJpa;
import demoapp.dom.types.javautil.uuids.jpa.JavaUtilUuidJpa;
import demoapp.dom.types.primitive.booleans.jpa.PrimitiveBooleanJpa;
import demoapp.dom.types.primitive.bytes.jpa.PrimitiveByteJpa;
import demoapp.dom.types.primitive.chars.jpa.PrimitiveCharJpa;
import demoapp.dom.types.primitive.doubles.jpa.PrimitiveDoubleJpa;
import demoapp.dom.types.primitive.floats.jpa.PrimitiveFloatJpa;
import demoapp.dom.types.primitive.ints.jpa.PrimitiveIntJpa;
import demoapp.dom.types.primitive.longs.jpa.PrimitiveLongJpa;
import demoapp.dom.types.primitive.shorts.jpa.PrimitiveShortJpa;

@Configuration
@Profile("demo-jpa")
@Import({
    DemoModuleCommon.class,
    CausewayModulePersistenceJpaEclipselink.class,
    CausewayModuleExtCommandLogPersistenceJpa.class,
})
@EntityScan(basePackageClasses = {

        CausewayBlobJpa.class,
        CausewayClobJpa.class,
        CausewayLocalResourcePathJpa.class,
        CausewayMarkupJpa.class,
        CausewayPasswordJpa.class,
        CausewayAsciiDocJpa.class,
        CausewayMarkdownJpa.class,
        CausewayVegaJpa.class,
        CausewayCalendarEventJpa.class,
        EventLogEntryJpa.class,

        JavaAwtBufferedImageJpa.class,

        JavaLangEnumJpa.class,
        JavaLangStringJpa.class,

        JavaMathBigDecimalJpa.class,
        JavaMathBigIntegerJpa.class,
        JavaNetUrlJpa.class,
        JavaSqlDateJpa.class,
        JavaSqlTimestampJpa.class,
        JavaTimeLocalTimeJpa.class,
        JavaTimeLocalDateJpa.class,
        JavaTimeLocalDateTimeJpa.class,
        JavaTimeOffsetDateTimeJpa.class,
        JavaTimeOffsetTimeJpa.class,
        JavaTimeZonedDateTimeJpa.class,
        JavaUtilDateJpa.class,
        JavaUtilUuidJpa.class,

        PrimitiveBooleanJpa.class,
        PrimitiveDoubleJpa.class,
        PrimitiveFloatJpa.class,
        PrimitiveCharJpa.class,
        PrimitiveLongJpa.class,
        PrimitiveIntJpa.class,
        PrimitiveShortJpa.class,
        PrimitiveByteJpa.class,

        WrapperBooleanJpa.class,
        WrapperDoubleJpa.class,
        WrapperFloatJpa.class,
        WrapperCharacterJpa.class,
        WrapperLongJpa.class,
        WrapperIntegerJpa.class,
        WrapperShortJpa.class,
        WrapperByteJpa.class,

        TenantedJpa.class,
        WrapperFactoryJpa.class,

        DomainObjectEntityChangePublishingDisabledJpa.class,
        DomainObjectEntityChangePublishingEnabledJpa.class,
        DomainObjectEntityChangePublishingEnabledMetaAnnotatedJpa.class,
        DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenJpa.class,
        ActionCommandPublishingJpa.class,
        ActionExecutionPublishingJpa.class,
        PropertyCommandPublishingJpa.class,
        PropertyExecutionPublishingJpa.class,
        PropertyProjectingChildJpa.class,

        JaxbRefJpa.class,
        NumberConstantJpa.class,

})
public class DemoModuleJpa {

}
