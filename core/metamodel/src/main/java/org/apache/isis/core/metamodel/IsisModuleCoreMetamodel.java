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
import org.apache.isis.core.metamodel.objectmanager.ObjectManagerDefault;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;
import org.apache.isis.core.metamodel.specloader.ProgrammingModelServiceDefault;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderDefault;
import org.apache.isis.core.metamodel.services.ServiceInjectorDefault;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureFactory;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorForCollections;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;
import org.apache.isis.core.metamodel.services.events.MetamodelEventSupport_Spring;
import org.apache.isis.core.metamodel.services.exceprecog.ExceptionRecognizerDocDefault;
import org.apache.isis.core.metamodel.services.grid.GridLoaderServiceDefault;
import org.apache.isis.core.metamodel.services.grid.GridReaderUsingJaxb;
import org.apache.isis.core.metamodel.services.grid.GridServiceDefault;
import org.apache.isis.core.metamodel.services.grid.bootstrap3.GridSystemServiceBS3;
import org.apache.isis.core.metamodel.services.layout.LayoutServiceDefault;
import org.apache.isis.core.metamodel.services.metamodel.MetaModelServiceDefault;
import org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault;
import org.apache.isis.core.metamodel.services.title.TitleServiceDefault;
import org.apache.isis.core.metamodel.services.user.UserServiceDefault;
import org.apache.isis.core.metamodel.valuetypes.ValueTypeProviderDefault;
import org.apache.isis.core.metamodel.valuetypes.ValueTypeProviderForCollections;
import org.apache.isis.core.metamodel.valuetypes.ValueTypeRegistry;
import org.apache.isis.security.api.IsisModuleSecurityApi;

@Configuration
@Import({
        // modules
        IsisModuleApplib.class,
        IsisModuleCoreConfig.class,
        IsisModuleSecurityApi.class,

        // @Configuration's
        MetaModelContexts.class,
        MetamodelEventSupport_Spring.class,

        // @Component's
        ProgrammingModelInitFilterDefault.class,
        ClassSubstitutorDefault.class,
        ClassSubstitutorForCollections.class,
        ClassSubstitutorRegistry.class,
        ValueTypeProviderDefault.class,
        ValueTypeProviderForCollections.class,
        ValueTypeRegistry.class,

        // @Service's
        ObjectManagerDefault.class,
        ServiceInjectorDefault.class,
        ApplicationFeatureFactory.class,
        MetamodelEventService.class,
        ExceptionRecognizerDocDefault.class,
        GridLoaderServiceDefault.class,
        GridReaderUsingJaxb.class,
        GridServiceDefault.class,
        GridSystemServiceBS3.class,
        InjectorMethodEvaluatorDefault.class,
        LayoutServiceDefault.class,
        MetaModelServiceDefault.class,
        ProgrammingModelServiceDefault.class,
        ServiceRegistryDefault.class,
        TitleServiceDefault.class,
        SpecificationLoaderDefault.class,
        UserServiceDefault.class,
        UserServiceDefault.SudoServiceSpi.class,

        // @Repository's
        ApplicationFeatureRepositoryDefault.class,

})
public class IsisModuleCoreMetamodel {

}
