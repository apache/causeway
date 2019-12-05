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
package org.apache.isis.metamodel;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.config.IsisModuleConfig;
import org.apache.isis.metamodel.context.MetaModelContexts;
import org.apache.isis.metamodel.objectmanager.ObjectManagerDefault;
import org.apache.isis.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.isis.metamodel.services.ServiceInjectorDefault;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureFactory;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;
import org.apache.isis.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.isis.metamodel.services.events.MetamodelEventService;
import org.apache.isis.metamodel.services.events.MetamodelEventSupport_Spring;
import org.apache.isis.metamodel.services.exceprecog.ExceptionRecognizerDocDefault;
import org.apache.isis.metamodel.services.grid.GridLoaderServiceDefault;
import org.apache.isis.metamodel.services.grid.GridReaderUsingJaxb;
import org.apache.isis.metamodel.services.grid.GridServiceDefault;
import org.apache.isis.metamodel.services.grid.bootstrap3.GridSystemServiceBS3;
import org.apache.isis.metamodel.services.layout.LayoutServiceDefault;
import org.apache.isis.metamodel.services.metamodel.MetaModelServiceDefault;
import org.apache.isis.metamodel.services.registry.ServiceRegistryDefault;
import org.apache.isis.metamodel.services.swagger.SwaggerServiceDefault;
import org.apache.isis.metamodel.services.swagger.internal.ClassExcluderDefault;
import org.apache.isis.metamodel.services.swagger.internal.SwaggerSpecGenerator;
import org.apache.isis.metamodel.services.swagger.internal.TaggerDefault;
import org.apache.isis.metamodel.services.swagger.internal.ValuePropertyFactoryDefault;
import org.apache.isis.metamodel.services.title.TitleServiceDefault;
import org.apache.isis.metamodel.services.user.UserServiceDefault;
import org.apache.isis.metamodel.specloader.InjectorMethodEvaluatorDefault;
import org.apache.isis.metamodel.specloader.ProgrammingModelServiceDefault;
import org.apache.isis.metamodel.specloader.SpecificationLoaderDefault;
import org.apache.isis.security.api.IsisModuleSecurityApi;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        // modules
        IsisModuleApplib.class,
        IsisModuleConfig.class,
        IsisModuleSecurityApi.class,

        // @Configuration's
        MetaModelContexts.class,
        MetamodelEventSupport_Spring.class,

        // @Component's
        ProgrammingModelInitFilterDefault.class,
        ClassSubstitutorDefault.class,
        ClassExcluderDefault.class,
        SwaggerSpecGenerator.class,
        TaggerDefault.class,
        ValuePropertyFactoryDefault.class,

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
        LayoutServiceDefault.class,
        MetaModelServiceDefault.class,
        SwaggerServiceDefault.class,
        TitleServiceDefault.class,
        UserServiceDefault.class,
        InjectorMethodEvaluatorDefault.class,
        ProgrammingModelServiceDefault.class,
        SpecificationLoaderDefault.class,

        // @Repository's
        ApplicationFeatureRepositoryDefault.class,

        // @DomainService's
        ServiceRegistryDefault.class,
        UserServiceDefault.SudoServiceSpi.class,

})
public class IsisModuleMetamodel {

}
