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
package org.apache.isis.core.metamodel;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.core.config.IsisModuleCoreConfig;
import org.apache.isis.core.metamodel.context.MetaModelContexts;
import org.apache.isis.core.metamodel.facets.object.logicaltype.LogicalTypeMalformedValidator;
import org.apache.isis.core.metamodel.inspect.IsisModuleCoreMetamodelInspection;
import org.apache.isis.core.metamodel.objectmanager.ObjectManagerDefault;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.isis.core.metamodel.services.ServiceInjectorDefault;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorForCollections;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorForDomainObjects;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;
import org.apache.isis.core.metamodel.services.exceprecog.ExceptionRecognizerForRecoverableException;
import org.apache.isis.core.metamodel.services.grid.GridLoaderServiceDefault;
import org.apache.isis.core.metamodel.services.grid.GridReaderUsingJaxb;
import org.apache.isis.core.metamodel.services.grid.GridServiceDefault;
import org.apache.isis.core.metamodel.services.grid.bootstrap3.GridSystemServiceBootstrap;
import org.apache.isis.core.metamodel.services.layout.LayoutServiceDefault;
import org.apache.isis.core.metamodel.services.metamodel.MetaModelServiceDefault;
import org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault;
import org.apache.isis.core.metamodel.services.tablecol.TableColumnOrderServiceDefault;
import org.apache.isis.core.metamodel.services.tablecol.TableColumnOrderServiceUsingTxtFile;
import org.apache.isis.core.metamodel.services.title.TitleServiceDefault;
import org.apache.isis.core.metamodel.specloader.ProgrammingModelServiceDefault;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderDefault;
import org.apache.isis.core.metamodel.valuesemantics.BigDecimalValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.BigIntegerValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.BlobValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.BookmarkValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.BooleanValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.BufferedImageValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.ByteValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.ChangesDtoValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.CharacterValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.ClobValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.CommandDtoValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.DoubleValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.FloatValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.IntValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.InteractionDtoValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.LocalResourcePathValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.LocaleValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.LongValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.MarkupValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.OidDtoValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.PasswordValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.ShortValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.StringValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.TreeNodeValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.URLValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.UUIDValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.LocalDateTimeValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.LocalDateValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.LocalTimeValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.OffsetDateTimeValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.OffsetTimeValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.ZonedDateTimeValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.JavaSqlDateValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.JavaSqlTimeStampValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.JavaSqlTimeValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.JavaUtilDateValueSemantics;
import org.apache.isis.core.metamodel.valuetypes.ValueSemanticsResolverDefault;
import org.apache.isis.core.security.IsisModuleCoreSecurity;

@Configuration
@Import({
        // modules
        IsisModuleApplib.class,
        IsisModuleCoreConfig.class,
        IsisModuleCoreSecurity.class,

        // @Configuration's
        MetaModelContexts.class,
        IsisModuleCoreMetamodelInspection.class,

        // @Component's
        ProgrammingModelInitFilterDefault.class,
        ClassSubstitutorDefault.class,
        ClassSubstitutorForCollections.class,
        ClassSubstitutorForDomainObjects.class,
        ClassSubstitutorRegistry.class,
        ValueSemanticsResolverDefault.class,

        // Value Semantics (built-in defaults)
        BooleanValueSemantics.class,
        ByteValueSemantics.class,
        ShortValueSemantics.class,
        IntValueSemantics.class,
        LongValueSemantics.class,
        DoubleValueSemantics.class,
        FloatValueSemantics.class,
        BigDecimalValueSemantics.class,
        BigIntegerValueSemantics.class,
        CharacterValueSemantics.class,
        StringValueSemantics.class,
        PasswordValueSemantics.class,
        BufferedImageValueSemantics.class,
        BlobValueSemantics.class,
        ClobValueSemantics.class,
        MarkupValueSemantics.class,
        URLValueSemantics.class,
        LocalResourcePathValueSemantics.class,
        BookmarkValueSemantics.class,
        OidDtoValueSemantics.class,
        UUIDValueSemantics.class,
        LocaleValueSemantics.class,
        TreeNodeValueSemantics.class,
        ChangesDtoValueSemantics.class,
        CommandDtoValueSemantics.class,
        InteractionDtoValueSemantics.class,
        // Value Semantics (temporal)
        LocalDateTimeValueSemantics.class,
        LocalDateValueSemantics.class,
        LocalTimeValueSemantics.class,
        OffsetDateTimeValueSemantics.class,
        OffsetTimeValueSemantics.class,
        ZonedDateTimeValueSemantics.class,
        // Value Semantics (temporal legacy)
        JavaSqlDateValueSemantics.class,
        JavaSqlTimeValueSemantics.class,
        JavaSqlTimeStampValueSemantics.class,
        JavaUtilDateValueSemantics.class,

        // @Service's
        ObjectManagerDefault.class,
        ServiceInjectorDefault.class,
        MetamodelEventService.class,
        ExceptionRecognizerForRecoverableException.class,
        GridLoaderServiceDefault.class,
        GridReaderUsingJaxb.class,
        GridServiceDefault.class,
        GridSystemServiceBootstrap.class,
        LayoutServiceDefault.class,
        MetaModelServiceDefault.class,
        ProgrammingModelServiceDefault.class,
        ServiceRegistryDefault.class,
        TableColumnOrderServiceDefault.class,
        TableColumnOrderServiceUsingTxtFile.class,
        TitleServiceDefault.class,
        SpecificationLoaderDefault.class,

        // @Repository's
        ApplicationFeatureRepositoryDefault.class,

        LogicalTypeMalformedValidator.class,

})
public class IsisModuleCoreMetamodel {

}
