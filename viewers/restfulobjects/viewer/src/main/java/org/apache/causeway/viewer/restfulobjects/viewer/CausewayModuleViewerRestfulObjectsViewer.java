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
package org.apache.causeway.viewer.restfulobjects.viewer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.applib.services.exceprecog.RootCauseFinder;
import org.apache.causeway.core.webapp.CausewayModuleCoreWebapp;
import org.apache.causeway.viewer.commons.services.CausewayModuleViewerCommonsServices;
import org.apache.causeway.viewer.restfulobjects.rendering.CausewayModuleRestfulObjectsRendering;
import org.apache.causeway.viewer.restfulobjects.rendering.exhandling.ExceptionResponseFactory;
import org.apache.causeway.viewer.restfulobjects.viewer.exhandling.ExceptionMapperForRestfulObjects;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.DomainObjectResourceServerside;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.DomainServiceResourceServerside;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.DomainTypeResourceServerside;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.HomePageResourceServerside;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.MenuBarsResourceServerside;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.SwaggerSpecResource;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.UserResourceServerside;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.VersionResourceServerside;
import org.apache.causeway.viewer.restfulobjects.viewer.webmodule.WebModuleRestfulObjects;

/**
 * @since 1.x {@index}
 */
@Configuration(proxyBeanMethods = false)
@Import({
        // Modules
        CausewayModuleCoreWebapp.class,
        CausewayModuleViewerCommonsServices.class,
        CausewayModuleRestfulObjectsRendering.class,

        // @Component's
        HomePageResourceServerside.class,
        DomainTypeResourceServerside.class,
        UserResourceServerside.class,
        MenuBarsResourceServerside.class,
        DomainObjectResourceServerside.class,
        DomainServiceResourceServerside.class,
        VersionResourceServerside.class,
        SwaggerSpecResource.class,

        ExceptionMapperForRestfulObjects.class,
        //TODO[causeway-viewer-restfulobjects-viewer-CAUSEWAY-3897] cleanup
        //AcceptHeaderServiceForRest.RequestFilter.class,
        //AcceptHeaderServiceForRest.ResponseFilter.class,

        //CausewayRestfulObjectsInteractionFilter2.class
        WebModuleRestfulObjects.class

})
public class CausewayModuleViewerRestfulObjectsViewer {

    @Bean
    ExceptionResponseFactory exceptionResponseFactory(@Autowired(required = false) List<RootCauseFinder> rootCauseFinders) {
        return new ExceptionResponseFactory(rootCauseFinders!=null ? rootCauseFinders : List.of());
    }

}
