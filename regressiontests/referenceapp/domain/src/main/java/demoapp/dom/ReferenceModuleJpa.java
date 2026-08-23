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

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import org.apache.causeway.extensions.commandlog.jpa.CausewayModuleExtCommandLogPersistenceJpa;
import org.apache.causeway.persistence.jpa.eclipselink.CausewayModulePersistenceJpaEclipselink;

import demoapp.dom.domain.actions.Action.choicesFrom.jpa.ActionChoicesFromEntityImpl;
import demoapp.dom.domain.actions.Action.commandPublishing.jpa.ActionCommandPublishingEntityImpl;
import demoapp.dom.domain.actions.Action.executionPublishing.jpa.ActionExecutionPublishingEntityImpl;
import demoapp.dom.domain.actions.ActionLayout.hidden.jpa.ActionLayoutHiddenEntityImpl;
import demoapp.dom.domain.actions.ActionLayout.redirectPolicy.jpa.ActionLayoutRedirectPolicyEntityImpl;
import demoapp.dom.domain.objects.DomainObject.aliased.jpa.DomainObjectAliasedEntityImpl;
import demoapp.dom.domain.objects.DomainObject.autoComplete.jpa.DomainObjectAutoCompleteEntityImpl;
import demoapp.dom.domain.objects.DomainObject.bounded.jpa.DomainObjectBoundingEntityImpl;
import demoapp.dom.domain.objects.DomainObject.editing.jpa.DomainObjectEditingEntityImpl;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.jpa.DomainObjectEntityChangePublishingEntityImpl;
import demoapp.dom.domain.objects.DomainObject.introspection.annotOpt.jpa.DomainObjectIntrospectionAnnotOptEntityImpl;
import demoapp.dom.domain.objects.DomainObject.introspection.annotReqd.jpa.DomainObjectIntrospectionAnnotReqdEntityImpl;
import demoapp.dom.domain.objects.DomainObject.introspection.encapsulated.jpa.DomainObjectIntrospectionEncapsulatedEntityImpl;
import demoapp.dom.domain.objects.DomainObject.mixinMethod.jpa.DomainObjectMixinMethodEntityImpl;
import demoapp.dom.domain.objects.DomainObject.nature.entity.jpa.DomainObjectNatureEntityImpl;
import demoapp.dom.domain.objects.DomainObject.xxxLifecycleEvent.jpa.DomainObjectXxxLifecycleEventEntityImpl;
import demoapp.dom.domain.objects.DomainObjectLayout.bookmarking.jpa.DomainObjectLayoutBookmarkingChildEntityImpl;
import demoapp.dom.domain.objects.DomainObjectLayout.bookmarking.jpa.DomainObjectLayoutBookmarkingEntityImpl;
import demoapp.dom.domain.objects.DomainObjectLayout.cssClass.jpa.DomainObjectLayoutCssClassEntityImpl;
import demoapp.dom.domain.objects.DomainObjectLayout.cssClassFa.jpa.DomainObjectLayoutCssClassFaEntityImpl;
import demoapp.dom.domain.objects.DomainObjectLayout.describedAs.jpa.DomainObjectLayoutDescribedAsEntityImpl;
import demoapp.dom.domain.objects.DomainObjectLayout.named.jpa.DomainObjectLayoutNamedEntityImpl;
import demoapp.dom.domain.objects.DomainObjectLayout.paged.jpa.DomainObjectLayoutPagedEntityImpl;
import demoapp.dom.domain.objects.DomainObjectLayout.tabledec.jpa.DomainObjectLayoutTableDecoratorEntityImpl;
import demoapp.dom.domain.objects.DomainObjectLayout.xxxUiEvent.jpa.DomainObjectLayoutXxxUiEventEntityImpl;
import demoapp.dom.domain.properties.Property.commandPublishing.jpa.PropertyCommandPublishingEntityImpl;
import demoapp.dom.domain.properties.Property.editing.jpa.PropertyEditingEntityImpl;
import demoapp.dom.domain.properties.Property.executionPublishing.jpa.PropertyExecutionPublishingEntityImpl;
import demoapp.dom.domain.properties.Property.projecting.jpa.PropertyProjectingChildEntityImpl;
import demoapp.dom.domain.properties.Property.snapshot.jpa.PropertySnapshotEntityImpl;
import demoapp.dom.domain.properties.PropertyLayout.hidden.jpa.PropertyLayoutHiddenEntityImpl;
import demoapp.dom.featured.causewayext.cal.jpa.CausewayCalendarEventJpa;
import demoapp.dom.progmodel.actions.bulk.jpa.BulkActionItemEntityImpl;
import demoapp.dom.progmodel.customvaluetypes.embeddedvalues.jpa.NumberConstantJpa;
import demoapp.dom.services.core.eventbusservice.EventLogEntryJpa;
import demoapp.dom.services.core.wrapperFactory.jpa.PrimeNumberJpa;
import demoapp.dom.services.extensions.secman.apptenancy.jpa.TenantedJpa;
import demoapp.dom.types.causeway.blobs.jpa.CausewayBlobJpa;
import demoapp.dom.types.causeway.clobs.jpa.CausewayClobJpa;
import demoapp.dom.types.causeway.localresourcepaths.jpa.CausewayLocalResourcePathJpa;
import demoapp.dom.types.causeway.markups.jpa.CausewayMarkupJpa;
import demoapp.dom.types.causeway.passwords.jpa.CausewayPasswordJpa;
import demoapp.dom.types.causewayval.asciidocs.jpa.CausewayAsciiDocJpa;
import demoapp.dom.types.causewayval.markdowns.jpa.CausewayMarkdownJpa;
import demoapp.dom.types.causewayval.vegas.jpa.CausewayVegaJpa;
import demoapp.dom.types.javaawt.images.jpa.BufferedImageJpa;
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
import demoapp.dom.types.javamath.bigdecimals.jpa.BigDecimalJpa;
import demoapp.dom.types.javamath.bigintegers.jpa.BigIntegerJpa;
import demoapp.dom.types.javanet.urls.jpa.UrlJpa;
import demoapp.dom.types.javasql.javasqldate.jpa.JavaSqlDateJpa;
import demoapp.dom.types.javasql.javasqltimestamp.jpa.JavaSqlTimestampJpa;
import demoapp.dom.types.javatime.javatimelocaldate.jpa.LocalDateJpa;
import demoapp.dom.types.javatime.javatimelocaldatetime.jpa.LocalDateTimeJpa;
import demoapp.dom.types.javatime.javatimelocaltime.jpa.LocalTimeJpa;
import demoapp.dom.types.javatime.javatimeoffsetdatetime.jpa.OffsetDateTimeJpa;
import demoapp.dom.types.javatime.javatimeoffsettime.jpa.OffsetTimeJpa;
import demoapp.dom.types.javatime.javatimezoneddatetime.jpa.ZonedDateTimeJpa;
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
    ReferenceModuleCommon.class,
    CausewayModulePersistenceJpaEclipselink.class,
    CausewayModuleExtCommandLogPersistenceJpa.class,
})
@EntityScan(basePackageClasses = {

        DomainObjectAliasedEntityImpl.class,
        DomainObjectAutoCompleteEntityImpl.class,
        DomainObjectBoundingEntityImpl.class,
        DomainObjectEditingEntityImpl.class,
        DomainObjectEntityChangePublishingEntityImpl.class,
        DomainObjectIntrospectionAnnotOptEntityImpl.class,
        DomainObjectIntrospectionAnnotReqdEntityImpl.class,
        DomainObjectIntrospectionEncapsulatedEntityImpl.class,
        DomainObjectMixinMethodEntityImpl.class,
        DomainObjectNatureEntityImpl.class,
        DomainObjectXxxLifecycleEventEntityImpl.class,
        DomainObjectLayoutBookmarkingEntityImpl.class,
        DomainObjectLayoutBookmarkingChildEntityImpl.class,
        DomainObjectLayoutCssClassEntityImpl.class,
        DomainObjectLayoutCssClassFaEntityImpl.class,
        DomainObjectLayoutDescribedAsEntityImpl.class,
        DomainObjectLayoutNamedEntityImpl.class,
        DomainObjectLayoutPagedEntityImpl.class,
        DomainObjectLayoutTableDecoratorEntityImpl.class,
        DomainObjectLayoutXxxUiEventEntityImpl.class,

        ActionChoicesFromEntityImpl.class,
        ActionCommandPublishingEntityImpl.class,
        ActionExecutionPublishingEntityImpl.class,

        BulkActionItemEntityImpl.class,

        ActionLayoutHiddenEntityImpl.class,
        ActionLayoutRedirectPolicyEntityImpl.class,

        PropertyCommandPublishingEntityImpl.class,
        PropertyEditingEntityImpl.class,
        PropertyExecutionPublishingEntityImpl.class,
        PropertyProjectingChildEntityImpl.class,
        PropertySnapshotEntityImpl.class,

        PropertyLayoutHiddenEntityImpl.class,

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

        BufferedImageJpa.class,

        JavaLangEnumJpa.class,
        JavaLangStringJpa.class,

        BigDecimalJpa.class,
        BigIntegerJpa.class,
        UrlJpa.class,
        JavaSqlDateJpa.class,
        JavaSqlTimestampJpa.class,
        LocalTimeJpa.class,
        LocalDateJpa.class,
        LocalDateTimeJpa.class,
        OffsetDateTimeJpa.class,
        OffsetTimeJpa.class,
        ZonedDateTimeJpa.class,
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
        PrimeNumberJpa.class,

        NumberConstantJpa.class,

})
public class ReferenceModuleJpa {

}
